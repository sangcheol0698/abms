package kr.co.abacus.abms.employee;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.jspecify.annotations.Nullable;
import org.springframework.web.multipart.MultipartFile;

import kr.co.abacus.abms.common.domain.BusinessException;

/**
 * 프로필 사진 정규화: 가운데를 정사각형으로 잘라 320px JPEG 로 다시 만든다.
 * 원본을 그대로 저장하지 않으므로 촬영 위치 등 메타데이터(EXIF)가 남지 않고, 크기·형식이 일정해진다.
 * 휴대폰 사진은 EXIF 방향값(Orientation)을 읽어 똑바로 세운 뒤 자른다.
 */
public final class ProfileImages {

    public static final long MAX_SIZE = 5L * 1024 * 1024;
    static final int OUTPUT_SIZE = 320;
    private static final long MAX_PIXELS = 40_000_000L;
    private static final Set<String> FORMATS = Set.of("jpeg", "jpg", "png", "gif");

    private ProfileImages() {
    }

    public static byte[] normalize(@Nullable MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("사진 파일을 선택하세요.");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException("사진은 5MB 이하만 등록할 수 있습니다.");
        }
        try (InputStream content = file.getInputStream()) {
            return normalize(content);
        } catch (IOException e) {
            throw new BusinessException("사진을 읽을 수 없습니다.");
        }
    }

    static byte[] normalize(InputStream content) throws IOException {
        BufferedImage source = read(content.readAllBytes());
        int side = Math.min(source.getWidth(), source.getHeight());
        BufferedImage square = source.getSubimage((source.getWidth() - side) / 2, (source.getHeight() - side) / 2, side, side);
        return writeJpeg(scale(square, OUTPUT_SIZE));
    }

    /** 해상도를 먼저 확인한 뒤 디코딩한다. (압축 폭탄 방지) */
    private static BufferedImage read(byte[] content) throws IOException {
        try (ImageInputStream in = ImageIO.createImageInputStream(new java.io.ByteArrayInputStream(content))) {
            Iterator<ImageReader> readers = in == null ? null : ImageIO.getImageReaders(in);
            if (readers == null || !readers.hasNext()) {
                throw new BusinessException("JPG·PNG·GIF 이미지만 등록할 수 있습니다.");
            }
            ImageReader reader = readers.next();
            try {
                if (!FORMATS.contains(reader.getFormatName().toLowerCase(Locale.ROOT))) {
                    throw new BusinessException("JPG·PNG·GIF 이미지만 등록할 수 있습니다.");
                }
                reader.setInput(in, true, true);
                long pixels = (long) reader.getWidth(0) * reader.getHeight(0);
                if (pixels > MAX_PIXELS) {
                    throw new BusinessException("사진 해상도가 너무 큽니다. (최대 4천만 화소)");
                }
                BufferedImage image = reader.read(0);
                return reader.getFormatName().toLowerCase(Locale.ROOT).startsWith("jp") ? orient(image, exifOrientation(content)) : image;
            } finally {
                reader.dispose();
            }
        }
    }

    /** JPEG APP1(Exif) 의 IFD0 에서 Orientation(0x0112) 값을 읽는다. 없거나 읽을 수 없으면 1(정방향). */
    static int exifOrientation(byte[] jpeg) {
        int i = 2;
        while (i + 4 < jpeg.length && (jpeg[i] & 0xFF) == 0xFF) {
            int marker = jpeg[i + 1] & 0xFF;
            int length = ((jpeg[i + 2] & 0xFF) << 8) | (jpeg[i + 3] & 0xFF);
            if (marker == 0xE1 && i + 10 < jpeg.length && new String(jpeg, i + 4, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("Exif")) {
                return orientationFromTiff(jpeg, i + 10, Math.min(jpeg.length, i + 2 + length));
            }
            if (marker == 0xDA) {
                break;
            }
            i += 2 + length;
        }
        return 1;
    }

    private static int orientationFromTiff(byte[] b, int start, int end) {
        if (start + 8 > end) {
            return 1;
        }
        boolean little = b[start] == 'I';
        int ifd = start + read32(b, start + 4, little);
        if (ifd + 2 > end) {
            return 1;
        }
        int entries = read16(b, ifd, little);
        for (int n = 0; n < entries; n++) {
            int entry = ifd + 2 + n * 12;
            if (entry + 12 > end) {
                break;
            }
            if (read16(b, entry, little) == 0x0112) {
                int value = read16(b, entry + 8, little);
                return value >= 1 && value <= 8 ? value : 1;
            }
        }
        return 1;
    }

    private static int read16(byte[] b, int i, boolean little) {
        return little ? (b[i] & 0xFF) | ((b[i + 1] & 0xFF) << 8) : ((b[i] & 0xFF) << 8) | (b[i + 1] & 0xFF);
    }

    private static int read32(byte[] b, int i, boolean little) {
        return little ? read16(b, i, true) | (read16(b, i + 2, true) << 16) : (read16(b, i, false) << 16) | read16(b, i + 2, false);
    }

    /** EXIF 방향값대로 회전·반전해 똑바로 세운다. (2·4·5·7 은 반전 포함) */
    static BufferedImage orient(BufferedImage image, int orientation) {
        if (orientation <= 1 || orientation > 8) {
            return image;
        }
        int w = image.getWidth();
        int h = image.getHeight();
        boolean swap = orientation >= 5;
        java.awt.geom.AffineTransform t = new java.awt.geom.AffineTransform();
        switch (orientation) {
            case 2 -> { t.translate(w, 0); t.scale(-1, 1); }
            case 3 -> { t.translate(w, h); t.rotate(Math.PI); }
            case 4 -> { t.translate(0, h); t.scale(1, -1); }
            case 5 -> { t.rotate(Math.PI / 2); t.scale(1, -1); }
            case 6 -> { t.translate(h, 0); t.rotate(Math.PI / 2); }
            case 7 -> { t.scale(-1, 1); t.translate(-h, 0); t.translate(0, w); t.rotate(3 * Math.PI / 2); }
            case 8 -> { t.translate(0, w); t.rotate(3 * Math.PI / 2); }
            default -> { }
        }
        BufferedImage out = new BufferedImage(swap ? h : w, swap ? w : h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, out.getWidth(), out.getHeight());
        g.drawImage(image, t, null);
        g.dispose();
        return out;
    }

    /** 큰 사진은 절반씩 줄여 계단 현상을 줄인다. 투명 배경은 흰색으로 채운다. */
    private static BufferedImage scale(BufferedImage image, int target) {
        BufferedImage current = image;
        int size = image.getWidth();
        do {
            size = Math.max(target, size / 2 >= target ? size / 2 : target);
            BufferedImage next = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = next.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, size, size);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(current, 0, 0, size, size, null);
            g.dispose();
            current = next;
        } while (size > target);
        return current;
    }

    private static byte[] writeJpeg(BufferedImage image) throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ImageOutputStream out = ImageIO.createImageOutputStream(bytes)) {
            writer.setOutput(out);
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.88f);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
        return bytes.toByteArray();
    }

}
