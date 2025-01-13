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
 *
 * Aquest programa realitza el següent:
 * 1. Genera la clau simètrica AES a partir de la contrasenya comuna "projecte2025"
 *    utilitzant el mètode PBKDF2 amb HMAC-SHA256 per derivar una clau segura, afegint un salt.
 * 2. Xifra el text "Document confidencial" utilitzant AES en mode CBC amb PKCS5Padding.
 * 3. Desa els resultats (text xifrat, IV i salt) en format CSV.
 * 4. Desxifra el text i verifica que coincideix amb l'original.
 */
public class Activitat2 {

    // Contrasenya comuna per derivar la clau AES
    private static final String PASSWORD = "projecte2025";
    // Text que volem xifrar
    private static final String TEXT_TO_ENCRYPT = "Document confidencial";
    // Mida del salt (en bytes)
    private static final int SALT_LENGTH = 16;
    // Mida del vector d'inicialització (IV) (en bytes); per AES, 16 bytes són 128 bits
    private static final int IV_LENGTH = 16;
    // Nombre d'iteracions per al mètode PBKDF2 (com a mesura de seguretat)
    private static final int ITERATIONS = 65536;
    // Mida de la clau generada (en bits): 128 bits
    private static final int KEY_LENGTH = 128;

    /**
     * Mètode principal per iniciar l'activitat.
     *
     * Els passos realitzats són:
     * 1. Generar un salt aleatori i derivar la clau AES a partir de la contrasenya.
     * 2. Xifrar el text "Document confidencial" amb AES/CBC.
     * 3. Desa els resultats (text xifrat, IV i salt) en un fitxer CSV.
     * 4. Desxifra el text i verifica que coincideix amb l'original.
     */
    public static void iniciar() {
        try {
            // Mostrem un encapçalament informatiu per a l'activitat
            Utilities.imprimirEncabezado("ACTIVITAT 2: CIFRAT I DESCIFRAT DE TEXT");

            // 1. Generem un salt aleatori per a la derivació de la clau
            byte[] salt = generateSalt();
            // Derivem la clau AES a partir de la contrasenya i el salt
            SecretKey secretKey = deriveKey(PASSWORD, salt);
            Utilities.imprimirExito("Clau AES generada a partir de la contrasenya.");

            // 2. Configuració del xifrat: creem el Cipher amb AES en mode CBC i PKCS5Padding
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            // Generem un IV aleatori de 16 bytes
            byte[] iv = generateIV();
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            // Inicialitzem el Cipher en mode xifrat amb la clau derivada i el IV
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            // Xifrem el text (convertit a bytes)
            byte[] encryptedBytes = cipher.doFinal(TEXT_TO_ENCRYPT.getBytes(StandardCharsets.UTF_8));
            // Codifiquem el text xifrat en Base64 per poder-lo desar com a String
            String encryptedText = Base64.getEncoder().encodeToString(encryptedBytes);

            // 3. Desa els resultats del xifrat (text xifrat, IV i salt) en un fitxer CSV
            guardarResultatsCSV(encryptedText, iv, salt);

            // 4. Configuració per al desxifrat:
            // Inicialitzem un nou Cipher en mode desxifrat amb la mateixa clau i el mateix IV
            Cipher decipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            decipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            // Desxifrem el text codificat en Base64
            byte[] decryptedBytes = decipher.doFinal(Base64.getDecoder().decode(encryptedText));
            // Convertim el resultat desxifrat a String utilitzant UTF-8
            String decryptedText = new String(decryptedBytes, StandardCharsets.UTF_8);

            // Mostrem la informació rellevant: text original, text xifrat i text desxifrat
            Utilities.imprimirInfo("Text original: " + TEXT_TO_ENCRYPT);
            Utilities.imprimirInfo("Text xifrat (Base64): " + encryptedText);
            Utilities.imprimirInfo("Text desxifrat: " + decryptedText);

            // Verifiquem que el text desxifrat és igual a l'original
            if (TEXT_TO_ENCRYPT.equals(decryptedText)) {
                Utilities.imprimirExito("Verificació correcta: el text desxifrat coincideix amb l'original.");
            } else {
                Utilities.imprimirError("Verificació fallida: el text desxifrat no coincideix.");
            }
        } catch (Exception e) {
            // En cas que passi alguna excepció, es mostra l'error per consola
            Utilities.imprimirError("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Genera un salt aleatori, que s'utilitza per derivar la clau AES.
     *
     * @return Array de bytes que representa el salt.
     */
    private static byte[] generateSalt() {
        // Creem un SecureRandom per generar valors aleatoris segurs
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt); // Omple l'array amb valors aleatoris
        return salt;
    }

    /**
     * Genera un vector d'inicialització (IV) aleatori.
     *
     * @return Array de bytes que representa el IV.
     */
    private static byte[] generateIV() {
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[IV_LENGTH];
        random.nextBytes(iv);
        return iv;
    }

    /**
     * Deriva una clau secreta AES a partir d'una contrasenya i un salt,
     * utilitzant el mètode PBKDF2 amb HMAC-SHA256.
     *
     * @param password La contrasenya comuna ("projecte2025").
     * @param salt     El salt aleatori generat.
     * @return La clau AES derivada.
     * @throws Exception Si hi ha un error durant el procés de derivació.
     */
    private static SecretKey deriveKey(String password, byte[] salt) throws Exception {
        // Creem un SecretKeyFactory utilitzant l'algorisme PBKDF2 amb HMAC-SHA256
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        // Especificació de la clau: contrasenya, salt, número d'iteracions i mida en bits
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        // Convertim la clau temporal al format especific per AES
        return new javax.crypto.spec.SecretKeySpec(tmp.getEncoded(), "AES");
    }

    /**
     * Desa els resultats del xifrat en un fitxer CSV.
     * El fitxer conté 3 columnes: Text Xifrat, IV i Salt (tots codificats en Base64).
     *
     * @param encryptedText El text xifrat (Base64).
     * @param iv            El vector d'inicialització utilitzat (bytes).
     * @param salt          El salt utilitzat per derivar la clau (bytes).
     * @throws IOException Si hi ha un error en escriure el fitxer.
     */
    private static void guardarResultatsCSV(String encryptedText, byte[] iv, byte[] salt) throws IOException {
        // Obtenim la ruta del directori de l'activitat 2 amb Utilities i la concatenem amb el nom del fitxer CSV
        Path ruta = Utilities.obtenerRutaActividad(2).resolve("resultats_activitat2.csv");

        // Obrim un FileWriter per escriure el contingut en format CSV
        try (FileWriter writer = new FileWriter(ruta.toFile())) {
            writer.append("Text Xifrat,IV,Salt\n"); // Capçalera del CSV
            // Escrivim cada camp separant-los amb comes i afegim un salt de línia al final
            writer.append(encryptedText).append(",")
                    .append(Base64.getEncoder().encodeToString(iv)).append(",")
                    .append(Base64.getEncoder().encodeToString(salt)).append("\n");
            Utilities.imprimirExito("Resultats guardats a: " + ruta.toString());
        }
    }
}
