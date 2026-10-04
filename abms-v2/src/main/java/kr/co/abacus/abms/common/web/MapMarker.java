package kr.co.abacus.abms.common.web;

import java.math.BigDecimal;
import java.util.List;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.common.domain.Location;

/**
 * 지도에 찍을 지점. 좌표가 있는 위치만 마커로 만든다.
 */
public record MapMarker(String label, @Nullable String sub, BigDecimal latitude, BigDecimal longitude, @Nullable String href) {

    public static @Nullable MapMarker of(String label, @Nullable String sub, Location location, @Nullable String href) {
        if (!location.hasCoordinates()) {
            return null;
        }
        return new MapMarker(label, sub, location.latitude(), location.longitude(), href);
    }

    /** data-map 속성에 넣을 JSON: {"markers":[{label, sub, lat, lng, href}]} */
    public static String json(List<MapMarker> markers) {
        StringBuilder sb = new StringBuilder("{\"markers\":[");
        for (int i = 0; i < markers.size(); i++) {
            MapMarker m = markers.get(i);
            if (i > 0) {
                sb.append(',');
            }
            sb.append("{\"label\":").append(quote(m.label()))
                    .append(",\"sub\":").append(quote(m.sub()))
                    .append(",\"lat\":").append(m.latitude().toPlainString())
                    .append(",\"lng\":").append(m.longitude().toPlainString())
                    .append(",\"href\":").append(quote(m.href()))
                    .append('}');
        }
        return sb.append("]}").toString();
    }

    private static String quote(@Nullable String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20 || c == '<' || c == '>' || c == '&') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }

}
