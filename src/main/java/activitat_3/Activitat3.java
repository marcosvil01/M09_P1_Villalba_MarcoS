package activitat_3;

import Functions.Utilities;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Classe que gestiona l'Activitat 3: Xifrat i Desxifrat d'un fitxer de text.
 * Aquesta classe inclou funcionalitats per a:
 * - Crear un fitxer d'entrada amb contingut textual
 * - Xifrar aquest fitxer utilitzant AES en mode CBC
 * - Desxifrar el fitxer xifrat i mostrar el contingut original
 */
public class Activitat3 {

    // Constants que defineixen els noms dels fitxers utilitzats
    private static final String INPUT_FILE_NAME = "entrada.txt"; // Nom del fitxer d'entrada (original)
    private static final String ENCRYPTED_FILE_NAME = "sortida.enc"; // Nom del fitxer que contindrà les dades xifrades
    private static final String KEY_FILE_NAME = "key.key"; // Nom del fitxer que guardarà la clau AES
    private static final String IV_FILE_NAME = "iv.iv"; // Nom del fitxer que guardarà el vector d'inicialització (IV)

    /**
     * Mètode principal per iniciar l'activitat.
     * Executa les accions següents:
     * - Crea el fitxer d'entrada amb contingut
     * - Xifra el fitxer d'entrada
     * - Desxifra el fitxer xifrat i mostra el contingut original
     */
    public static void iniciar() {
        try {
            // Mostra un missatge informatiu al principi de l'activitat
            Utilities.imprimirEncabezado("ACTIVITAT 3: CIFRAT I DESCIFRAT DE FITXER DE TEXT");

            // Obté la ruta del directori de treball per a l'activitat 3
            Path activitat3Dir = Utilities.obtenerRutaActividad(3);

            // Crea el fitxer d'entrada amb un text predeterminat
            crearFitxerEntrada(activitat3Dir);

            // Xifra el contingut del fitxer d'entrada
            xifrarFitxer(activitat3Dir);

            // Desxifra el contingut xifrat i el mostra per pantalla
            desxifrarFitxer(activitat3Dir);

        } catch (Exception e) {
            // Si ocorre un error, es mostra el missatge i es detalla la traça de l'excepció
            Utilities.imprimirError("S'ha produït un error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Crea un fitxer d'entrada amb contingut textual.
     * Aquest fitxer serà utilitzat posteriorment per al xifrat i desxifrat.
     *
     * @param activitat3Dir Ruta del directori on es crearà el fitxer.
     */
    private static void crearFitxerEntrada(Path activitat3Dir) {
        // Defineix la ruta completa per al fitxer d'entrada
        Path inputFile = activitat3Dir.resolve(INPUT_FILE_NAME);

        // Text que es guardarà dins del fitxer d'entrada
        String contenido = "Aquest és el contingut del fitxer d'entrada. Aquest text serà xifrat i desxifrat per AES en mode CBC.";

        // Escrivim el contingut dins del fitxer utilitzant un BufferedWriter
        try (BufferedWriter writer = Files.newBufferedWriter(inputFile, StandardCharsets.UTF_8)) {
            writer.write(contenido); // Escriu el contingut al fitxer
            Utilities.imprimirExito("Fitxer d'entrada creat a: " + inputFile.toString()); // Mostra un missatge d'èxit
        } catch (IOException e) {
            // Si ocorre un error durant la creació, es mostra un missatge descriptiu
            Utilities.imprimirError("Error creant el fitxer d'entrada: " + e.getMessage());
        }
    }

    /**
     * Xifra el fitxer d'entrada utilitzant AES en mode CBC (Cipher Block Chaining).
     * El fitxer xifrat es guarda en un nou fitxer, juntament amb la clau i el IV.
     *
     * @param activitat3Dir Ruta del directori on es troben els fitxers.
     */
    private static void xifrarFitxer(Path activitat3Dir) {
        // Es defineixen les rutes per al fitxer d'entrada, fitxer xifrat, clau i IV
        Path inputFile = activitat3Dir.resolve(INPUT_FILE_NAME); // Fitxer d'entrada
        Path encryptedFile = activitat3Dir.resolve(ENCRYPTED_FILE_NAME); // Fitxer xifrat
        Path keyFile = activitat3Dir.resolve(KEY_FILE_NAME); // Fitxer on es guardarà la clau
        Path ivFile = activitat3Dir.resolve(IV_FILE_NAME); // Fitxer on es guardarà el vector d'inicialització

        try {
            // Generem una clau AES de 128 bits
            KeyGenerator keyGen = KeyGenerator.getInstance("AES"); // Utilitza l'algorisme AES
            keyGen.init(128); // Configura la mida de la clau (128 bits)
            SecretKey secretKey = keyGen.generateKey(); // Genera la clau secreta

            // Generem un vector d'inicialització (IV) de 16 bytes
            byte[] iv = new byte[16]; // IV ha de tenir 16 bytes per a AES
            SecureRandom secureRandom = new SecureRandom(); // Crea un generador de números aleatoris
            secureRandom.nextBytes(iv); // Genera valors aleatoris per al IV
            IvParameterSpec ivSpec = new IvParameterSpec(iv); // Crea l'objecte IV

            // Inicialitzem el Cipher en mode xifrat utilitzant AES/CBC/PKCS5Padding
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec); // Configura el Cipher amb la clau i el IV

            // Llegim les dades del fitxer d'entrada
            byte[] inputBytes = Files.readAllBytes(inputFile);

            // Xifrem les dades utilitzant el Cipher
            byte[] encryptedBytes = cipher.doFinal(inputBytes);

            // Guardem les dades xifrades al fitxer de sortida
            Files.write(encryptedFile, encryptedBytes);
            Utilities.imprimirExito("Fitxer xifrat guardat a: " + encryptedFile.toString());

            // Codifiquem la clau i el IV en Base64 per facilitar l'emmagatzematge
            String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
            String encodedIV = Base64.getEncoder().encodeToString(iv);

            // Guardem la clau i el IV en fitxers separats
            Files.write(keyFile, encodedKey.getBytes(StandardCharsets.UTF_8));
            Files.write(ivFile, encodedIV.getBytes(StandardCharsets.UTF_8));
            Utilities.imprimirExito("Clau AES guardada a: " + keyFile.toString());
            Utilities.imprimirExito("Vector d'inicialització (IV) guardat a: " + ivFile.toString());

        } catch (Exception e) {
            // Mostrem un missatge d'error en cas que falli algun pas del procés
            Utilities.imprimirError("Error durant el xifrat: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Desxifra el fitxer xifrat utilitzant la clau i el IV guardats.
     * Mostra el contingut desxifrat a la sortida estàndard.
     *
     * @param activitat3Dir Ruta del directori on es troben els fitxers.
     */
    private static void desxifrarFitxer(Path activitat3Dir) {
        // Es defineixen les rutes per als fitxers necessaris
        Path encryptedFile = activitat3Dir.resolve(ENCRYPTED_FILE_NAME); // Fitxer xifrat
        Path keyFile = activitat3Dir.resolve(KEY_FILE_NAME); // Fitxer amb la clau
        Path ivFile = activitat3Dir.resolve(IV_FILE_NAME); // Fitxer amb el IV

        try {
            // Llegim la clau AES codificada en Base64 des del fitxer
            String encodedKey = new String(Files.readAllBytes(keyFile), StandardCharsets.UTF_8);
            // Decodifiquem la clau a bytes
            byte[] decodedKey = Base64.getDecoder().decode(encodedKey);

            // Llegim el IV codificat en Base64 des del fitxer
            String encodedIV = new String(Files.readAllBytes(ivFile), StandardCharsets.UTF_8);
            // Decodifiquem el IV a bytes
            byte[] decodedIV = Base64.getDecoder().decode(encodedIV);

            // Reconstruïm la clau i el IV
            SecretKey secretKey = new javax.crypto.spec.SecretKeySpec(decodedKey, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(decodedIV);

            // Inicialitzem el Cipher en mode desxifrat
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            // Llegim les dades xifrades del fitxer
            byte[] encryptedBytes = Files.readAllBytes(encryptedFile);

            // Desxifrem les dades utilitzant el Cipher
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            // Convertim les dades desxifrades a un String
            String decryptedContent = new String(decryptedBytes, StandardCharsets.UTF_8);

            // Mostrem el contingut original desxifrat
            Utilities.imprimirInfo("Contingut desxifrat del fitxer:");
            System.out.println(decryptedContent); // Es mostra el contingut al terminal
            Utilities.imprimirExito("Desxifrat completat correctament.");

        } catch (Exception e) {
            // Mostrem un missatge d'error si alguna cosa falla durant el desxifrat
            Utilities.imprimirError("Error durant el descifrat: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
