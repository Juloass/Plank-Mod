package fr.juloass.plank;

import java.io.*;
import java.security.MessageDigest;

public class MD5Checksum {

    public static String getMD5Checksum(File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");

        // Use try-with-resources to ensure FileInputStream is closed automatically
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] dataBytes = new byte[1024];
            int bytesRead;

            // Read file and update the digest
            while ((bytesRead = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, bytesRead);
            }
        }

        // Convert the byte array into a hex string
        byte[] mdBytes = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : mdBytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

}