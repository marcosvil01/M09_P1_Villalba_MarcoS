package activitat_2;

import Functions.Utilities;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Classe que gestiona l'Activitat 2: Xifrat i Desxifrat de Text.
 * Aquesta classe inclou funcionalitats per:
 * - Generar una clau AES a partir d'una contrasenya i un salt
 * - Xifrar un text utilitzant AES en mode CBC
 * - Guardar els resultats en un fitxer CSV
 * - Desxifrar el text i verificar que coincideix amb l'original
 */
public class Activitat2 {

    // Contrasenya utilitzada per generar la clau
    private static final String PASSWORD = "projecte2025";
    // Text que serà xifrat
    private static final String TEXT_TO_ENCRYPT = "Document confidencial";
    // Longitud del salt en bytes
    private static final int SALT_LENGTH = 16;
    // Longitud del vector d'inicialització (IV) en bytes
    private static final int IV_LENGTH = 16;
    // Nombre d'iteracions per al procés de derivació de clau
    private static final int ITERATIONS = 65536;
    // Longitud de la clau generada (en bits)
    private static final int KEY_LENGTH = 128;

    /**
     * Mètode principal per iniciar l'activitat.
     * Realitza els passos següents:
     * - Genera un salt i deriva una clau a partir de la contrasenya
     * - Xifra un text utilitzant AES/CBC
     * - Guarda els resultats en un fitxer CSV
     * - Desxifra el text i verifica que coincideix amb l'original
     */
    public static void iniciar() {
        try {
            // Mostra un encapçalament informatiu per a l'activitat
            Utilities.imprimirEncabezado("ACTIVITAT 2: CIFRAT I DESCIFRAT DE TEXT");

            // 1. Generar el salt i derivar una clau a partir de la contrasenya
            byte[] salt = generateSalt(); // Genera un salt aleatori
            SecretKey secretKey = deriveKey(PASSWORD, salt); // Deriva la clau AES utilitzant el salt i la contrasenya
            Utilities.imprimirExito("Clau AES generada a partir de la contrasenya.");

            // 2. Configurar el xifrat amb AES/CBC/PKCS5Padding
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] iv = generateIV(); // Genera un vector d'inicialització (IV) aleatori
            IvParameterSpec ivSpec = new IvParameterSpec(iv); // Especifica el IV per al xifrat
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec); // Inicialitza el Cipher en mode xifrat
            byte[] encryptedBytes = cipher.doFinal(TEXT_TO_ENCRYPT.getBytes(StandardCharsets.UTF_8)); // Xifra el text
            String encryptedText = Base64.getEncoder().encodeToString(encryptedBytes); // Codifica el text xifrat en Base64

            // 3. Guardar els resultats del xifrat en un fitxer CSV
            guardarResultatsCSV(encryptedText, iv, salt);

            // 4. Configurar el desxifrat i recuperar el text original
            Cipher decipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            decipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec); // Inicialitza el Cipher en mode desxifrat
            byte[] decryptedBytes = decipher.doFinal(Base64.getDecoder().decode(encryptedText)); // Desxifra el text
            String decryptedText = new String(decryptedBytes, StandardCharsets.UTF_8); // Converteix el text desxifrat a String

            // Mostra informació sobre el procés
            Utilities.imprimirInfo("Text original: " + TEXT_TO_ENCRYPT);
            Utilities.imprimirInfo("Text xifrat (Base64): " + encryptedText);
            Utilities.imprimirInfo("Text desxifrat: " + decryptedText);

            // Verifica que el text desxifrat coincideix amb l'original
            if (TEXT_TO_ENCRYPT.equals(decryptedText)) {
                Utilities.imprimirExito("Verificació correcta: el text desxifrat coincideix amb l'original.");
            } else {
                Utilities.imprimirError("Verificació fallida: el text desxifrat no coincideix.");
            }

        } catch (Exception e) {
            // Mostra un error detallat si alguna cosa falla
            Utilities.imprimirError("Error: " + e.getMessage());
        }
    }

    /**
     * Genera un salt aleatori utilitzat per derivar la clau.
     *
     * @return Array de bytes del salt.
     */
    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom(); // Inicialitza un generador de números aleatoris
        byte[] salt = new byte[SALT_LENGTH]; // Crea un array de bytes de la longitud especificada
        random.nextBytes(salt); // Omple l'array amb valors aleatoris
        return salt; // Retorna el salt generat
    }

    /**
     * Genera un vector d'inicialització (IV) aleatori utilitzat per al xifrat.
     *
     * @return Array de bytes del IV.
     */
    private static byte[] generateIV() {
        SecureRandom random = new SecureRandom(); // Inicialitza un generador de números aleatoris
        byte[] iv = new byte[IV_LENGTH]; // Crea un array de bytes de la longitud especificada
        random.nextBytes(iv); // Omple l'array amb valors aleatoris
        return iv; // Retorna el IV generat
    }

    /**
     * Deriva una clau secreta a partir d'una contrasenya i un salt utilitzant PBKDF2.
     *
     * @param password Contrasenya utilitzada per derivar la clau.
     * @param salt     Salt utilitzat per assegurar la derivació.
     * @return Clau secreta derivada.
     * @throws Exception Si ocorre un error durant el procés de derivació.
     */
    private static SecretKey deriveKey(String password, byte[] salt) throws Exception {
        // Utilitza l'algorisme PBKDF2 amb HMAC-SHA256 per derivar la clau
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH); // Especifica els paràmetres
        SecretKey tmp = factory.generateSecret(spec); // Genera una clau secreta temporal
        return new javax.crypto.spec.SecretKeySpec(tmp.getEncoded(), "AES"); // Converteix la clau a un format compatible amb AES
    }

    /**
     * Guarda els resultats del xifrat en un fitxer CSV.
     * Cada fila conté el text xifrat, el IV i el salt codificats en Base64.
     *
     * @param encryptedText Text xifrat codificat en Base64.
     * @param iv            Vector d'inicialització (IV) utilitzat durant el xifrat.
     * @param salt          Salt utilitzat per derivar la clau.
     * @throws IOException Si ocorre un error al escriure el fitxer.
     */
    private static void guardarResultatsCSV(String encryptedText, byte[] iv, byte[] salt) throws IOException {
        // Ruta del fitxer CSV dins del directori de treball de l'activitat
        Path ruta = Utilities.obtenerRutaActividad(2).resolve("resultats_activitat2.csv");

        // Escriu les dades en format CSV
        try (FileWriter writer = new FileWriter(ruta.toFile())) {
            writer.append("Text Xifrat,IV,Salt\n"); // Capçalera del CSV
            writer.append(encryptedText).append(",") // Afegeix el text xifrat
                    .append(Base64.getEncoder().encodeToString(iv)).append(",") // Afegeix el IV en Base64
                    .append(Base64.getEncoder().encodeToString(salt)).append("\n"); // Afegeix el salt en Base64
            Utilities.imprimirExito("Resultats guardats a: " + ruta.toString()); // Missatge d'èxit
        }
    }
}
