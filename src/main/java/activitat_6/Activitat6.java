package activitat_6;

import Functions.Utilities;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Path;
import java.security.*;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Scanner;

/**
 * Activitat 6:
 *
 * Aquest programa implementa un sistema de missatgeria bàsic on:
 * 1. Es generen dos parells de claus RSA (un per a l’emissor i un altre per al receptor).
 * 2. L’emissor introdueix un missatge.
 * 3. El missatge es xifra amb la clau pública del receptor.
 * 4. El receptor desxifra el missatge amb la seva clau privada.
 *
 * A més, es desa un registre net del procés en el fitxer "process_log.txt"
 * dins de la carpeta Activitat6 (per exemple, output/activitat6/process_log.txt).
 */
public class Activitat6 {

    // Noms dels fitxers (aquestes constant es concatenaran amb la ruta d'Activitat6)
    private static final String FITXER_ORIGINAL = "document.txt";
    private static final String FITXER_XIFRAT = "document_xifrat.txt";
    private static final String FITXER_SIGNATURA = "firmaDigital.txt";
    private static final String FITXER_CLAU_AES = "clauAES.txt";
    private static final String LOG_FILE_NAME = "process_log.txt";

    // Un writer per escriure el registre
    private static PrintWriter logWriter;

    public static void iniciar() {
        try {
            // Obtenim la ruta completa per a Activitat6 (ex.: output/activitat6/)
            // Tota la gestió de fitxers es farà respecte a aquesta ruta
            File activitat6Dir = Utilities.obtenerRutaActividad(6).toFile();

            // Configura el sistema de registre (log), que es desa dins del directori d'Activitat6
            setupLogging(Utilities.obtenerRutaActividad(6));

            Utilities.imprimirEncabezado("ACTIVITAT 6: XIFRAT, SIGNATURA I VERIFICACIÓ DE FITXERS AMB RSA I AES");
            log("Inici de l'activitat 6.");

            // 1. Generar parell de claus RSA per a l’emissor
            KeyPair senderKeyPair = generateRSAKeyPair();
            log("Parell de claus RSA generat per a l'emissor.");
            Utilities.imprimirExito("Parell de claus RSA generat per a l'emissor.");

            // Generar parell de claus RSA per al receptor
            KeyPair receiverKeyPair = generateRSAKeyPair();
            log("Parell de claus RSA generat per al receptor.");
            Utilities.imprimirExito("Parell de claus RSA generat per al receptor.");

            // 2. L'emissor introdueix un missatge per enviar
            Scanner scanner = new Scanner(System.in);
            Utilities.imprimirInfo("Introdueix el missatge per enviar:");
            String message = scanner.nextLine();
            log("Missatge introduït per l'emissor: " + message);

            // 3. Xifrar el missatge amb la clau pública del receptor
            String encryptedMessage = encryptMessage(message, receiverKeyPair.getPublic());
            log("Missatge xifrat amb clau pública del receptor: " + encryptedMessage);
            Utilities.imprimirExito("Missatge xifrat amb la clau pública del receptor:");
            System.out.println(encryptedMessage);

            // 4. Generar la firma digital del missatge utilitzant la clau privada de l’emissor
            byte[] signature = signMessage(message.getBytes(), senderKeyPair.getPrivate());
            String signatureBase64 = Base64.getEncoder().encodeToString(signature);
            log("Firma digital generada: " + signatureBase64);
            Utilities.imprimirExito("Firma digital generada:");
            System.out.println(signatureBase64);

            // 5. Xifrat simètric complementari: (Opcional per demostrar un procés mixt)
            // Generem una clau AES per xifrar el missatge
            KeyGenerator aesKeyGen = KeyGenerator.getInstance("AES");
            aesKeyGen.init(128);
            SecretKey aesKey = aesKeyGen.generateKey();
            byte[] encryptedData = encryptData(message.getBytes(), aesKey);
            // Desa el fitxer xifrat dins de la carpeta d'Activitat6
            saveFile(activitat6Dir, FITXER_XIFRAT, encryptedData);
            log("Fitxer xifrat (simètric) desat: " + FITXER_XIFRAT);
            Utilities.imprimirExito("Fitxer xifrat desat a: " + FITXER_XIFRAT);

            // Envoltem la clau AES amb la clau pública del receptor i la desam
            byte[] wrappedAESKey = wrapKey(aesKey, receiverKeyPair.getPublic());
            saveFile(activitat6Dir, FITXER_CLAU_AES, wrappedAESKey);
            log("Clau AES embolcallada desada: " + FITXER_CLAU_AES);
            Utilities.imprimirExito("Clau AES embolcallada desada a: " + FITXER_CLAU_AES);

            // 6. Desxifrar el fitxer: desempaquetar la clau AES amb la clau privada del receptor i desxifrar el missatge
            byte[] unwrappedAESKeyBytes = unwrapKey(wrappedAESKey, receiverKeyPair.getPrivate());
            SecretKey unwrappedAESKey = new SecretKeySpec(unwrappedAESKeyBytes, "AES");
            byte[] decryptedData = decryptData(encryptedData, unwrappedAESKey);
            String decryptedMessage = new String(decryptedData);
            log("Missatge desxifrat: " + decryptedMessage);
            Utilities.imprimirExito("Fitxer desxifrat:");
            System.out.println(decryptedMessage);

            // 7. Verificar la firma digital amb la clau pública de l'emissor
            boolean isSignatureValid = verifySignature(message.getBytes(), signature, senderKeyPair.getPublic());
            if (isSignatureValid) {
                log("La firma és vàlida: el missatge no ha estat manipulat.");
                Utilities.imprimirExito("La firma és vàlida i el missatge no ha estat manipulat.");
            } else {
                log("La firma NO és vàlida: el missatge ha estat manipulat.");
                Utilities.imprimirError("La firma NO és vàlida. El missatge podria haver estat manipulat.");
            }

            // Tanca el registre
            closeLogging();

        } catch (Exception e) {
            Utilities.imprimirError("Error durant l'activitat: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Configura el registre (log) de procés.
     * Es desa un fitxer "process_log.txt" dins del directori d'Activitat6.
     *
     * @param activitat6Path La ruta (Path) de la carpeta Activitat6.
     * @throws IOException Si hi ha un error al crear o escriure el fitxer de log.
     */
    private static void setupLogging(Path activitat6Path) throws IOException {
        // Definim la ruta completa del fitxer de log dins de la carpeta Activitat6
        File logFile = activitat6Path.resolve(LOG_FILE_NAME).toFile();
        // Obrim un PrintWriter en mode "append" (per afegir nova informació sense esborrar la prèvia)
        logWriter = new PrintWriter(new FileOutputStream(logFile, true));
        log("=== Inici del Procés (Activitat 6) ===");
    }

    /**
     * Escriu un missatge de log en el fitxer amb un timestamp.
     *
     * @param message El missatge a escriure.
     */
    private static void log(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        logWriter.println("[" + timestamp + "] " + message);
        logWriter.flush();
    }

    /**
     * Tanca el PrintWriter del log.
     */
    private static void closeLogging() {
        if (logWriter != null) {
            log("=== Fi del Procés (Activitat 6) ===");
            logWriter.close();
        }
    }

    /**
     * Genera un parell de claus RSA.
     *
     * @return Un objecte KeyPair amb la clau pública i la clau privada RSA.
     * @throws NoSuchAlgorithmException Si l'algorisme RSA no està disponible.
     */
    private static KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    /**
     * Xifra un missatge utilitzant RSA amb la clau pública.
     *
     * @param message   El missatge a xifrar.
     * @param publicKey La clau pública del receptor.
     * @return El missatge xifrat en format Base64.
     * @throws Exception Si ocorre un error durant el xifrat.
     */
    private static String encryptMessage(String message, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * Desxifra un missatge xifrat utilitzant RSA amb la clau privada.
     *
     * @param encryptedMessage El missatge xifrat en format Base64.
     * @param privateKey       La clau privada del receptor.
     * @return El missatge desxifrat.
     * @throws Exception Si ocorre un error durant el descifrat.
     */
    private static String decryptMessage(String encryptedMessage, PrivateKey privateKey) throws Exception {
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedMessage);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);
    }

    /**
     * Xifra dades amb AES.
     *
     * @param data   Les dades a xifrar.
     * @param aesKey La clau AES per al xifrat.
     * @return Les dades xifrades.
     * @throws Exception Si ocorre un error durant el xifrat.
     */
    private static byte[] encryptData(byte[] data, SecretKey aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        return cipher.doFinal(data);
    }

    /**
     * Desxifra dades amb AES.
     *
     * @param encryptedData Les dades xifrades.
     * @param aesKey        La clau AES per al descifrat.
     * @return Les dades desxifrades.
     * @throws Exception Si ocorre un error durant el descifrat.
     */
    private static byte[] decryptData(byte[] encryptedData, SecretKey aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        return cipher.doFinal(encryptedData);
    }

    /**
     * Envolta (xifra) la clau AES utilitzant RSA amb la clau pública del receptor.
     *
     * @param aesKey    La clau AES a embolcallar.
     * @param publicKey La clau pública del receptor.
     * @return La clau AES embolcallada.
     * @throws Exception Si ocorre un error durant el procés.
     */
    private static byte[] wrapKey(SecretKey aesKey, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.WRAP_MODE, publicKey);
        return cipher.wrap(aesKey);
    }

    /**
     * Desembolcalla (desxifra) la clau AES embolcallada utilitzant RSA amb la clau privada del receptor.
     *
     * @param wrappedKey La clau AES embolcallada.
     * @param privateKey La clau privada del receptor.
     * @return La clau AES desempaquetada (en bytes).
     * @throws Exception Si ocorre un error durant el procés.
     */
    private static byte[] unwrapKey(byte[] wrappedKey, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.UNWRAP_MODE, privateKey);
        Key aesKey = cipher.unwrap(wrappedKey, "AES", Cipher.SECRET_KEY);
        return aesKey.getEncoded();
    }

    /**
     * Signa digitalment un document utilitzant RSA amb la clau privada de l'emissor.
     *
     * @param data       Les dades del document.
     * @param privateKey La clau privada de l'emissor.
     * @return La firma digital.
     * @throws Exception Si ocorre un error durant el procés de signatura.
     */
    private static byte[] signMessage(byte[] data, PrivateKey privateKey) throws Exception {
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(privateKey);
        signer.update(data);
        return signer.sign();
    }

    /**
     * Verifica la firma d'un document utilitzant RSA amb la clau pública de l'emissor.
     *
     * @param data      Les dades del document.
     * @param signature La firma digital.
     * @param publicKey La clau pública de l'emissor.
     * @return Cert si la firma és vàlida, fals si no ho és.
     * @throws Exception Si ocorre un error durant el procés de verificació.
     */
    private static boolean verifySignature(byte[] data, byte[] signature, PublicKey publicKey) throws Exception {
        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(publicKey);
        verifier.update(data);
        return verifier.verify(signature);
    }

    /**
     * Desa les dades en un fitxer dins del directori d'Activitat6.
     *
     * @param directory La carpeta on es desa el fitxer.
     * @param filename  El nom del fitxer.
     * @param data      Les dades a desar.
     * @throws IOException Si hi ha un error durant l'escriptura.
     */
    private static void saveFile(File directory, String filename, byte[] data) throws IOException {
        // Creem la ruta completa a partir de la carpeta d'Activitat6 i el nom del fitxer
        File file = new File(directory, filename);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        }
    }
}
