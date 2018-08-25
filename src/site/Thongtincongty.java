/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package site;

import java.util.Base64;
import java.awt.image.BufferedImage;
import java.awt.image.ImagingOpException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.imgscalr.Scalr;
import util.Crawler;
import util.Miscellaneous;
import util.TParser;

/**
 *
 * @author Administrator
 */
public class Thongtincongty {

    public static class OCR {

        private static final BlockingQueue<ITesseract> OCR_INSTANCES;

        static {
            OCR_INSTANCES = new LinkedBlockingDeque<>();
            for (int i = 0; i < Runtime.getRuntime().availableProcessors(); ++i) {
                ITesseract instance = new Tesseract();
                instance.setTessVariable("tessedit_char_whitelist", "0123456789()+/-");
                instance.setTessVariable("load_system_dawg", "F");
                instance.setTessVariable("load_freq_dawg", "F");
                OCR_INSTANCES.add(instance);
            }
        }

        public static String detect(BufferedImage img) {
            ITesseract instance = null;
            try {
                img = Scalr.resize(img, 750);
                instance = OCR_INSTANCES.take();
                return instance.doOCR(img).trim().replaceAll("\\s++", " ");
            } catch (InterruptedException | ImagingOpException | IllegalArgumentException | TesseractException e) {
                return null;
            } finally {
                if (instance != null) {
                    OCR_INSTANCES.add(instance);
                }
            }
        }

        public static String detect(File f) {
            try {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(Miscellaneous.getContentBytesFromFile(f)));
                return detect(img);
            } catch (IOException e) {
                return null;
            }
        }

        public static String detect(String base64EncodedString) {
            try {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(base64EncodedString)));
                return detect(img);
            } catch (IllegalArgumentException | IOException ex) {
                Logger.getLogger(Thongtincongty.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
    }

    public String congty, tengiaodich, masothue, diachi, daidienphapluat, ngaycapgiayphep, ngayhoatdong, dienthoai, nganhnghekinhdoanh;

    public static String getTSVHeader() {
        return "CongTy" + "\t"
                + "TenGiaoDich" + "\t"
                + "MaSoThue" + "\t"
                + "DiaChi" + "\t"
                + "DaiDienPhapLuat" + "\t"
                + "NgayCapGiayPhep" + "\t"
                + "NgayHoatDong" + "\t"
                + "DienThoai" + "\t"
                + "NganhNgheKinhDoanh";
    }

    public String toTSVLine() {
        return (congty + "\t"
                + tengiaodich + "\t"
                + masothue + "\t"
                + diachi + "\t"
                + daidienphapluat + "\t"
                + ngaycapgiayphep + "\t"
                + ngayhoatdong + "\t"
                + dienthoai + "\t"
                + nganhnghekinhdoanh).replaceAll("null", "");
    }

    public static Thongtincongty parseFromPageContent(URL url, String content) {
        if (!url.getHost().equals("www.thongtincongty.com") || !url.getPath().startsWith("/company/")) {
            return null;
        }
        try {
            Thongtincongty data = new Thongtincongty();
            data.congty = TParser.getContent(content, "<span title=\"", "\">");
            data.tengiaodich = TParser.getContent(content, "Tên giao dịch: ", "<br/>");

            String masothueEncoded = TParser.getContent(content, "Mã số thuế: <a href=\"", "\"></a><br/>");
            if (masothueEncoded != null) {
                masothueEncoded = masothueEncoded.substring(masothueEncoded.indexOf("data:image/png;base64,") + "data:image/png;base64,".length());
                data.masothue = OCR.detect(masothueEncoded);
            }
            data.diachi = TParser.getContent(content, "Địa chỉ: ", "\">");
            data.daidienphapluat = TParser.getContent(content, "Đại diện pháp luật: ", "<br/>");
            data.ngaycapgiayphep = TParser.getContent(content, "Ngày cấp giấy phép: ", "<br/>");
            data.ngayhoatdong = TParser.getContent(content, "Ngày hoạt động: ", " \\(<em>Đã hoạt động");

            String dienthoaiEncoded = TParser.getContent(content, "Điện thoại: <img src=\"data:image/png;base64,", "\"><br/>");
            if (dienthoaiEncoded != null) {
                data.dienthoai = OCR.detect(dienthoaiEncoded);
            }
            
            StringBuilder nganhnghe = new StringBuilder();
            List<String> nganhngheList = TParser.getContentList(content, "<td class=\"col-md-9\">", "</td>");
            if (nganhngheList != null) {
                for (String s : nganhngheList) {
                    if (nganhnghe.length() != 0) {
                        nganhnghe.append(";");
                    }
                    if (s.startsWith("<strong>")) {
                        s = TParser.getContent(s, "<strong>", "</strong>") + " (Chính)";
                    }
                    nganhnghe.append(s);
                }
            }
            data.nganhnghekinhdoanh = nganhnghe.toString();
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<URL> extractOutLinksFromPageContent(URL url, String content) {
        if (!url.getHost().equals("www.thongtincongty.com") || !url.getPath().startsWith("/search/")) {
            return null;
        }

        List<URL> result = new LinkedList<>();
        for (String r : TParser.getContentList(content, "Mã số thuế: <a href=\"", "\">")) {
            try {
                result.add(new URL(r));
            } catch (MalformedURLException e) {

            }
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        String url = "http://www.thongtincongty.com/company/3f2363f0-cong-ty-tnhh-bao-tin-minh-chau/";
        String content = Crawler.getContentFromUrl(url);
        Thongtincongty cty = parseFromPageContent(new URL(url), content);
        System.out.println(cty.toTSVLine().replaceAll("\t", "\r\n"));
    }
}
