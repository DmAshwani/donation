package in.dataman.donation.uitl;

import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class DmPasswaord {
    private static String codifyKey1 = "~!≡s5*.d", codifyKey2 = "T←mÄn╤sY";
    private static String codifyAlgorithm = "AES";
    
    private static byte[] convertStringToBytes(String str) {
        char[] charArray = str.toCharArray();
        byte[] bytes = new byte[str.length()];
        for (int i = 0; i < charArray.length; i++) {
            bytes[i] = (byte) charArray[i];
        }
        return bytes;
    }

    public static String codify(String stringToCodify) throws Exception {
        byte[] keyBytes = convertStringToBytes(codifyKey1 + codifyKey2);
        SecretKey key = new SecretKeySpec(keyBytes, codifyAlgorithm);
        Cipher cipher = Cipher.getInstance(codifyAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(stringToCodify.getBytes("utf-8"));
        return Base64.getEncoder().encodeToString(encryptedBytes); // Corrected line
    }

    public static String dCodify(String stringToDCodify) throws Exception {
        byte[] keyBytes = convertStringToBytes(codifyKey1 + codifyKey2);
        SecretKey key = new SecretKeySpec(keyBytes, codifyAlgorithm);
        Cipher cipher = Cipher.getInstance(codifyAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedValue64 = Base64.getDecoder().decode(stringToDCodify);
        byte[] decryptedValue = cipher.doFinal(decryptedValue64);
        return new String(decryptedValue, "utf-8");
    }
   
}

