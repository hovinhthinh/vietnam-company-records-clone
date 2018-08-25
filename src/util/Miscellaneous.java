package util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author hovinhthinh
 */
public class Miscellaneous {

    public final static String CSV = "csv";
    public final static String TSV = "tsv";

    /*
     * Get the extension of a file.
     */
    public static String getFileExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    public static ArrayList<String> getLinesFromFile(File f) {
        ArrayList<String> lines;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"))) {
            String line;
            lines = new ArrayList<>();
            while ((line = in.readLine()) != null) {
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
            return lines;
        } catch (IOException ex) {
            return null;
        }
    }

    public static byte[] getContentBytesFromFile(File f) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream is = new FileInputStream(f);
            byte[] buffer = new byte[1024 * 8];
            int c;
            while ((c = is.read(buffer)) >= 0) {
                out.write(buffer, 0, c);
            }
            return out.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }
}
