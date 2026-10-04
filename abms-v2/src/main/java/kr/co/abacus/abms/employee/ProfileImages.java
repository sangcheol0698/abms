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
        BufferedImage source = read(content);
        int side = Math.min(source.getWidth(), source.getHeight());
        BufferedImage square = source.getSubimage((source.getWidth() - side) / 2, (source.getHeight() - side) / 2, side, side);
        return writeJpeg(scale(square, OUTPUT_SIZE));
    }

    /** 해상도를 먼저 확인한 뒤 디코딩한다. (압축 폭탄 방지) */
    private static BufferedImage read(InputStream content) throws IOException {
        try (ImageInputStream in = ImageIO.createImageInputStream(content)) {
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
                return reader.read(0);
            } finally {
                reader.dispose();
            }
        }
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
