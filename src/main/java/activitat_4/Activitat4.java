package activitat_4;

import Functions.Utilities;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Clase que maneja la Actividad 4: Cifrado y Descifrado de Archivos con AES y RSA.
 */
public class Activitat4 {

    // Nombres de archivos
    private static final String INPUT_FILE = "entrada.txt";
    private static final String ENCRYPTED_FILE = "sortida.enc";
    private static final String WRAPPED_KEY_FILE = "clau_embolcallada.dat";
    private static final String IV_FILE = "iv.dat";
    private static final String RSA_PUBLIC_KEY = "clau_publica.pem";
    private static final String RSA_PRIVATE_KEY = "clau_privada.pem";

    /**
     * Iniciar la actividad.
     */
    public static void iniciar() {
        try {
            Utilities.imprimirEncabezado("ACTIVITAT 4: CIFRAT I DESXIFRAT DE FITXERS AMB AES I RSA");

            // Obtener la ruta de la actividad 4
            Path activitat4Dir = Utilities.obtenerRutaActividad(4);

            // Crear el archivo de entrada con información si no existe
            crearFitxerEntrada(activitat4Dir);

            // Generar o cargar claves RSA
            KeyPair rsaKeys = obtenirClausRSA(activitat4Dir);

            // Generar clave simétrica AES
            SecretKey aesKey = generarClauAES();

            // Cifrar el archivo de entrada con AES y guardar el archivo cifrado
            xifrarFitxer(activitat4Dir, aesKey);

            // Cifrar (envolver) la clave AES con la clave pública RSA y guardar la clave envolvuelta
            xifrarClauAES(activitat4Dir, aesKey, rsaKeys.getPublic());

            // Descifrar el archivo cifrado
            desxifrarFitxer(activitat4Dir, rsaKeys.getPrivate());

        } catch (Exception e) {
            Utilities.imprimirError("Error en executar l'activitat: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Crear el archivo de entrada con información si no existe.
     *
     * @param activitat4Dir Ruta del directorio de la actividad 4.
     */
    private static void crearFitxerEntrada(Path activitat4Dir) {
        Path inputFile = activitat4Dir.resolve(INPUT_FILE);
        String contenido = "Hello World :D. Aquest text serà xifrat i desxifrat per AES i RSA.";

        File file = inputFile.toFile();
        if (file.exists()) {
            Utilities.imprimirAdvertencia("El fitxer d'entrada ja existeix: " + inputFile.toString());
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(contenido);
            Utilities.imprimirExito("Fitxer d'entrada creat a: " + inputFile.toString());
        } catch (IOException e) {
            Utilities.imprimirError("Error creant el fitxer d'entrada: " + e.getMessage());
        }
    }

    /**
     * Generar o cargar claves RSA.
     *
     * @param activitat4Dir Ruta del directorio de la actividad 4.
     * @return Parell de claus RSA.
     * @throws Exception Si ocurre un error durante la generación o carga de claves.
     */
    private static KeyPair obtenirClausRSA(Path activitat4Dir) throws Exception {
        Path publicKeyPath = activitat4Dir.resolve(RSA_PUBLIC_KEY);
        Path privateKeyPath = activitat4Dir.resolve(RSA_PRIVATE_KEY);

        File publicKeyFile = publicKeyPath.toFile();
        File privateKeyFile = privateKeyPath.toFile();

        if (publicKeyFile.exists() && privateKeyFile.exists()) {
            // Cargar claves RSA existentes
            PublicKey publicKey = carregarClauPublica(publicKeyPath);
            PrivateKey privateKey = carregarClauPrivada(privateKeyPath);
            Utilities.imprimirExito("Claus RSA carregades.");
            return new KeyPair(publicKey, privateKey);
        } else {
            // Generar un nou parell de claus RSA
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();

            // Desa les claus RSA en format Base64
            guardarClau(publicKeyPath, keyPair.getPublic().getEncoded());
            guardarClau(privateKeyPath, keyPair.getPrivate().getEncoded());

            Utilities.imprimirExito("Parell de claus RSA generat i desat.");
            return keyPair;
        }
    }

    /**
     * Generar una clau simètrica AES.
     *
     * @return Clau AES generada.
     * @throws NoSuchAlgorithmException Si el algoritmo AES no está disponible.
     */
    private static SecretKey generarClauAES() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128); // Clau de 128 bits
        SecretKey secretKey = keyGen.generateKey();
        Utilities.imprimirExito("Clau AES generada.");
        return secretKey;
    }

    /**
     * Cifrar el archivo de entrada con AES y guardar el archivo cifrado.
     *
     * @param activitat4Dir Ruta del directorio de la actividad 4.
     * @param aesKey        Clave AES para el cifrado.
     * @throws Exception Si ocurre un error durante el cifrado.
     */
    private static void xifrarFitxer(Path activitat4Dir, SecretKey aesKey) throws Exception {
        Path inputFile = activitat4Dir.resolve(INPUT_FILE);
        Path encryptedFile = activitat4Dir.resolve(ENCRYPTED_FILE);
        Path ivFile = activitat4Dir.resolve(IV_FILE);

        // Generar IV aleatorio
        byte[] iv = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // Inicializar Cipher para cifrado
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);

        // Cifrar el contenido del archivo
        try (FileInputStream fis = new FileInputStream(inputFile.toFile());
             FileOutputStream fos = new FileOutputStream(encryptedFile.toFile());
             CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                cos.write(buffer, 0, bytesRead);
            }
        }

        // Guardar el IV en un archivo separado
        try (FileOutputStream fosIV = new FileOutputStream(ivFile.toFile())) {
            fosIV.write(iv);
        }

        Utilities.imprimirExito("Fitxer xifrat desat a: " + encryptedFile.toString());
        Utilities.imprimirExito("Vector d'inicialització (IV) guardat a: " + ivFile.toString());
    }

    /**
     * Cifrar (envolver) la clau AES amb la clau pública RSA i guardar-la en un fitxer.
     *
     * @param activitat4Dir Ruta del directorio de la actividad 4.
     * @param aesKey        Clave AES a envolver.
     * @param publicKey     Clave pública RSA.
     * @throws Exception Si ocurre un error durante el envolvimiento de la clave.
     */
    private static void xifrarClauAES(Path activitat4Dir, SecretKey aesKey, PublicKey publicKey) throws Exception {
        Path wrappedKeyPath = activitat4Dir.resolve(WRAPPED_KEY_FILE);

        // Inicializar Cipher para envolvimiento
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.WRAP_MODE, publicKey);

        // Envolver la clave AES
        byte[] wrappedKey = cipher.wrap(aesKey);

        // Guardar la clave envolvuelta en un archivo
        try (FileOutputStream fos = new FileOutputStream(wrappedKeyPath.toFile())) {
            fos.write(wrappedKey);
        }

        Utilities.imprimirExito("Clau AES embolcallada desada a: " + wrappedKeyPath.toString());
    }

    /**
     * Descifrar el archivo cifrado utilizando la clave AES recuperada.
     *
     * @param activitat4Dir Ruta del directorio de la actividad 4.
     * @param privateKey    Clave privada RSA para desenvolver la clave AES.
     * @throws Exception Si ocurre un error durante el descifrado.
     */
    private static void desxifrarFitxer(Path activitat4Dir, PrivateKey privateKey) throws Exception {
        Path encryptedFile = activitat4Dir.resolve(ENCRYPTED_FILE);
        Path wrappedKeyPath = activitat4Dir.resolve(WRAPPED_KEY_FILE);
        Path ivFile = activitat4Dir.resolve(IV_FILE);

        // Leer la clave AES envolvuelta
        byte[] wrappedKey = carregarBytes(wrappedKeyPath.toFile());

        // Desenvolver la clave AES con la clave privada RSA
        Cipher cipherRSA = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipherRSA.init(Cipher.UNWRAP_MODE, privateKey);
        SecretKey aesKey = (SecretKey) cipherRSA.unwrap(wrappedKey, "AES", Cipher.SECRET_KEY);

        // Leer el IV
        byte[] iv = carregarBytes(ivFile.toFile());
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // Inicializar Cipher para descifrado
        Cipher cipherAES = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipherAES.init(Cipher.DECRYPT_MODE, aesKey, ivSpec);

        // Descifrar el contenido del archivo cifrado
        Path decryptedFile = activitat4Dir.resolve("fitxer_desxifrat.txt");
        try (FileInputStream fis = new FileInputStream(encryptedFile.toFile());
             CipherInputStream cis = new CipherInputStream(fis, cipherAES);
             FileOutputStream fos = new FileOutputStream(decryptedFile.toFile())) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = cis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }

        Utilities.imprimirExito("Fitxer desxifrat desat a: " + decryptedFile.toString());

        // Mostrar el contenido desxifrat en la consola
        String contenidoDesxifrat = new String(Files.readAllBytes(decryptedFile), StandardCharsets.UTF_8);
        Utilities.imprimirInfo("Contingut desxifrat del fitxer:");
        System.out.println(contenidoDesxifrat);
    }

    /**
     * Cargar los bytes de un archivo.
     *
     * @param file Archivo a leer.
     * @return Array de bytes leídos.
     * @throws IOException Si ocurre un error al leer el archivo.
     */
    private static byte[] carregarBytes(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return fis.readAllBytes();
        }
    }

    /**
     * Cargar una clau pública RSA desde un archivo.
     *
     * @param publicKeyPath Ruta del archivo de la clau pública.
     * @return Clau pública RSA.
     * @throws Exception Si ocurre un error al cargar la clau pública.
     */
    private static PublicKey carregarClauPublica(Path publicKeyPath) throws Exception {
        byte[] keyBytes = carregarBytes(publicKeyPath.toFile());
        byte[] decodedKey = Base64.getDecoder().decode(keyBytes);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    /**
     * Cargar una clau privada RSA desde un archivo.
     *
     * @param privateKeyPath Ruta del archivo de la clau privada.
     * @return Clau privada RSA.
     * @throws Exception Si ocurre un error al cargar la clau privada.
     */
    private static PrivateKey carregarClauPrivada(Path privateKeyPath) throws Exception {
        byte[] keyBytes = carregarBytes(privateKeyPath.toFile());
        byte[] decodedKey = Base64.getDecoder().decode(keyBytes);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    /**
     * Guardar una clau en un archivo en formato Base64.
     *
     * @param keyPath Ruta del archivo donde se guardará la clau.
     * @param keyBytes Array de bytes de la clau.
     * @throws IOException Si ocurre un error al escribir el archivo.
     */
    private static void guardarClau(Path keyPath, byte[] keyBytes) throws IOException {
        String encodedKey = Base64.getEncoder().encodeToString(keyBytes);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(keyPath.toFile()))) {
            writer.write(encodedKey);
        }
    }
}
