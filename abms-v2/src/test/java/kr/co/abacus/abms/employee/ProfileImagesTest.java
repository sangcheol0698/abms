package kr.co.abacus.abms.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import kr.co.abacus.abms.common.domain.BusinessException;

class ProfileImagesTest {

    /** 가로 4 × 세로 2, 왼쪽 위 한 칸만 빨강 */
    private static BufferedImage marked() {
        BufferedImage image = new BufferedImage(4, 2, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 2; y++) {
                image.setRGB(x, y, Color.WHITE.getRGB());
            }
        }
        image.setRGB(0, 0, Color.RED.getRGB());
        return image;
    }

    private static boolean red(BufferedImage image, int x, int y) {
        Color c = new Color(image.getRGB(x, y));
        return c.getRed() > 200 && c.getGreen() < 80;
    }

    @ParameterizedTest(name = "방향값 {0} → 빨간 칸 ({1}, {2}), 크기 {3}×{4}")
    @CsvSource({
            "1, 0, 0, 4, 2",
            "2, 3, 0, 4, 2",
            "3, 3, 1, 4, 2",
            "4, 0, 1, 4, 2",
            "5, 0, 0, 2, 4",
            "6, 1, 0, 2, 4",
            "7, 1, 3, 2, 4",
            "8, 0, 3, 2, 4"
    })
    void EXIF_방향값대로_사진을_똑바로_세운다(int orientation, int x, int y, int width, int height) {
        BufferedImage result = ProfileImages.orient(marked(), orientation);

        assertThat(result.getWidth()).isEqualTo(width);
        assertThat(result.getHeight()).isEqualTo(height);
        assertThat(red(result, x, y)).as("빨간 칸 위치").isTrue();
    }

    @Test
    void JPEG_의_Exif_방향값을_읽는다() throws IOException {
        assertThat(ProfileImages.exifOrientation(withOrientation(jpeg(10, 6), 6))).isEqualTo(6);
        assertThat(ProfileImages.exifOrientation(jpeg(10, 6))).isEqualTo(1);
    }

    @Test
    void 가운데를_정사각형으로_잘라_320px_JPEG_로_다시_만든다() throws IOException {
        byte[] result = ProfileImages.normalize(new MockMultipartFile("file", "me.png", "image/png", png(800, 400)));

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(result));
        assertThat(image.getWidth()).isEqualTo(320);
        assertThat(image.getHeight()).isEqualTo(320);
        assertThat(result[0] & 0xFF).isEqualTo(0xFF);
        assertThat(result[1] & 0xFF).isEqualTo(0xD8);
    }

    @Test
    void 이미지가_아니거나_비어_있으면_거부한다() {
        assertThatThrownBy(() -> ProfileImages.normalize(new MockMultipartFile("file", "a.png", "image/png", "not image".getBytes())))
                .isInstanceOf(BusinessException.class).hasMessageContaining("JPG·PNG·GIF");
        assertThatThrownBy(() -> ProfileImages.normalize(new MockMultipartFile("file", "a.png", "image/png", new byte[0])))
                .isInstanceOf(BusinessException.class).hasMessageContaining("선택");
    }

    static byte[] png(int width, int height) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), "png", out);
        return out.toByteArray();
    }

    private static byte[] jpeg(int width, int height) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), "jpg", out);
        return out.toByteArray();
    }

    /** SOI 바로 뒤에 Orientation 하나만 든 APP1(Exif, 빅엔디언) 세그먼트를 넣는다. */
    private static byte[] withOrientation(byte[] jpeg, int orientation) {
        byte[] tiff = {'M', 'M', 0, 42, 0, 0, 0, 8, 0, 1, 0x01, 0x12, 0, 3, 0, 0, 0, 1, 0, (byte) orientation, 0, 0, 0, 0, 0, 0};
        byte[] exif = new byte[6 + tiff.length];
        System.arraycopy("Exif\0\0".getBytes(), 0, exif, 0, 6);
        System.arraycopy(tiff, 0, exif, 6, tiff.length);
        int length = exif.length + 2;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(jpeg, 0, 2);
        out.write(0xFF);
        out.write(0xE1);
        out.write(length >> 8);
        out.write(length & 0xFF);
        out.write(exif, 0, exif.length);
        out.write(jpeg, 2, jpeg.length - 2);
        return out.toByteArray();
    }

}
