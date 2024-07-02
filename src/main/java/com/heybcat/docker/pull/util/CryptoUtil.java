package com.heybcat.docker.pull.util;

/**
 * @author Fetters
 */
public class CryptoUtil {

    private static final int KEY = 5;
    private static final int MODULUS = 26;

    public static String moduloEncrypt(String plaintext) {
        StringBuilder encryptedText = new StringBuilder();
        for (char character : plaintext.toCharArray()) {
            if (Character.isLetter(character)) {
                char base = Character.isUpperCase(character) ? 'A' : 'a';
                char encryptedChar = (char) ((character - base + KEY) % MODULUS + base);
                encryptedText.append(encryptedChar);
            } else {
                encryptedText.append(character);
            }
        }
        char[] charArray = encryptedText.toString().toCharArray();
        StringBuilder result = new StringBuilder();
        for (char c : charArray) {
            StringBuilder current = new StringBuilder();
            int i = c;
            while (i > 0){
                if (i % 2 == 0){
                    current.append("_");
                }else {
                    current.append("+");
                }
                i = i >> 1;
            }
            while (current.length() % 8 > 0){
                current.append("_");
            }
            result.append(current.reverse());
        }
        return result.toString();
    }

    public static String moduloDecrypt(String ciphertext) {
        if (!ciphertext.matches("^[_+]+$")) {
            return ciphertext;
        }

        char[] charArray = ciphertext.toCharArray();
        int length = charArray.length / 8;
        char[] orgCharArray = new char[length];

        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            int pos = i % 8;
            if (c == '+') {
                orgCharArray[i / 8] += (char) (1 << (7 - pos));
            }
        }
        ciphertext = new String(orgCharArray);
        StringBuilder decryptedText = new StringBuilder();
        for (char character : ciphertext.toCharArray()) {
            if (Character.isLetter(character)) {
                char base = Character.isUpperCase(character) ? 'A' : 'a';
                char decryptedChar = (char) ((character - base - KEY + MODULUS) % MODULUS + base);
                decryptedText.append(decryptedChar);
            } else {
                decryptedText.append(character);
            }
        }
        return decryptedText.toString();
    }


}
