package cat.copernic.proyecto2.Utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;

public class CryptoUtils {
    
    // Usamos AES con ECB y PKCS5Padding para simplificar la clave simétrica
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    /**
     * Genera una clave de 16 bytes a partir del DNI del paciente.
     * AES-128 requiere exactamente 16 bytes.
     */
    private static SecretKeySpec buildKey(String password) {
        byte[] key = password.getBytes(StandardCharsets.UTF_8);
        // Ajustamos la clave a 16 bytes (rellena con ceros o corta si es necesario)
        key = Arrays.copyOf(key, 16); 
        return new SecretKeySpec(key, ALGORITHM);
    }

    /**
     * Cifrado Simétrico AES
     */
    public static String encrypt(String data, String password) throws Exception {
        // CORRECCIÓN: Llamada directa a Cipher.getInstance
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, buildKey(password));
        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * Descifrado Simétrico AES
     */
    public static String decrypt(String encryptedData, String password) throws Exception {
        // CORRECCIÓN: Llamada directa a Cipher.getInstance
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, buildKey(password));
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}