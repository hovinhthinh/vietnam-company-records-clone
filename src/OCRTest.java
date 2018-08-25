
import java.io.File;
import site.Thongtincongty;

public class OCRTest {
    public static void main(String[] args) throws Exception {
        System.out.println(Thongtincongty.OCR.detect(new File("ocr_test.png")));
    }
}
