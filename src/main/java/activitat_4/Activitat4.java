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
 * Activitat 4:
 *
 * Aquest programa implementa un procés de xifrat i desxifrat utilitzant una combinació d’AES i RSA.
 *
 * El procediment és el següent:
 * 1. Es genera una clau simètrica AES que s'utilitza per xifrar el contingut del fitxer "entrada.txt".
 * 2. La clau AES es xifra (envolta) amb la clau pública RSA. El concepte de "parell de claus" RSA
 *    implica que tenim una clau privada (que es manté en secret) i una clau pública (que es pot distribuir).
 *    La clau privada s'utilitza per desxifrar la clau embolcallada, i la clau pública per xifrar-la.
 * 3. Es desa el fitxer xifrat ("sortida.enc"), el vector d’inicialització (iv.dat) i la clau embolcallada
 *    ("clau_embolcallada.dat") en fitxers separats.
 * 4. Per desxifrar, es recupera primer la clau AES desempaquetant-la amb la clau privada RSA, i després
 *    s'utilitza aquesta clau AES per desxifrar el fitxer "sortida.enc", recuperant així el contingut original.
 */
public class Activitat4 {

    // Noms dels fitxers que s’utilitzen en el procés
    private static final String INPUT_FILE = "entrada.txt";
    private static final String ENCRYPTED_FILE = "sortida.enc";
    private static final String WRAPPED_KEY_FILE = "clau_embolcallada.dat";
    private static final String IV_FILE = "iv.dat";
    private static final String RSA_PUBLIC_KEY = "clau_publica.pem";
    private static final String RSA_PRIVATE_KEY = "clau_privada.pem";

    /**
     * Mètode principal que coordina totes les operacions de xifrat i desxifrat.
     */
    public static void iniciar() {
        try {
            // Mostrem un encapçalament informatiu per indicar l'inici de l'activitat
            Utilities.imprimirEncabezado("ACTIVITAT 4: CIFRAT I DESXIFRAT DE FITXERS AMB AES I RSA");

            // Obtenim el directori on treballarem per a aquesta activitat (per exemple, output/activitat4/)
            Path activitat4Dir = Utilities.obtenerRutaActividad(4);

            // Creem el fitxer d'entrada "entrada.txt" amb informació predefinida si no existeix
            crearFitxerEntrada(activitat4Dir);

            /*
             * Generem o carreguem el parell de claus RSA.
             * El parell de claus està format per:
             *  - Una clau privada RSA: utilitzada per desxifrar o signar.
             *  - Una clau pública RSA: utilitzada per xifrar o verificar.
             */
            KeyPair rsaKeys = obtenirClausRSA(activitat4Dir);

            // Generem una clau simètrica AES que s'utilitzarà per xifrar el contingut del fitxer.
            SecretKey aesKey = generarClauAES();

            // Xifrem el fitxer d'entrada amb AES en mode CBC i desem el fitxer xifrat "sortida.enc".
            // També es guarda el vector d'inicialització (IV) en "iv.dat".
            xifrarFitxer(activitat4Dir, aesKey);

            // Xifrem (envoltem) la clau AES amb la clau pública RSA i desem aquesta clau embolcallada.
            xifrarClauAES(activitat4Dir, aesKey, rsaKeys.getPublic());

            // Desxifrem el fitxer: primer desempaquetem (unwrap) la clau AES amb la clau privada RSA,
            // i després utilitzem la clau AES per desxifrar "sortida.enc" i recuperar el contingut original.
            desxifrarFitxer(activitat4Dir, rsaKeys.getPrivate());

        } catch (Exception e) {
            Utilities.imprimirError("Error en executar l'activitat: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Crea el fitxer d'entrada ("entrada.txt") amb contingut predefinit si no existeix.
     *
     * @param activitat4Dir La ruta del directori de la activitat 4.
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
     * Genera o carrega un parell de claus RSA.
     *
     * Si els fitxers RSA (clau_publica.pem i clau_privada.pem) ja existeixen a la carpeta,
     * les carrega; en cas contrari, genera un nou parell de claus RSA i desa les claus en format Base64.
     *
     * @param activitat4Dir La ruta del directori de la activitat 4.
     * @return Un objecte KeyPair que conté la clau pública i privada RSA.
     * @throws Exception En cas d'error durant la generació o càrrega de claus.
     */
    private static KeyPair obtenirClausRSA(Path activitat4Dir) throws Exception {
        Path publicKeyPath = activitat4Dir.resolve(RSA_PUBLIC_KEY);
        Path privateKeyPath = activitat4Dir.resolve(RSA_PRIVATE_KEY);

        File publicKeyFile = publicKeyPath.toFile();
        File privateKeyFile = privateKeyPath.toFile();

        if (publicKeyFile.exists() && privateKeyFile.exists()) {
            // Carreguem les claus RSA existents
            PublicKey publicKey = carregarClauPublica(publicKeyPath);
            PrivateKey privateKey = carregarClauPrivada(privateKeyPath);
            Utilities.imprimirExito("Claus RSA carregades.");
            return new KeyPair(publicKey, privateKey);
        } else {
            // Generem un nou parell de claus RSA
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
     * Genera una clau simètrica AES de 128 bits.
     *
     * @return La clau AES generada.
     * @throws NoSuchAlgorithmException Si l'algoritme AES no està disponible.
     */
    private static SecretKey generarClauAES() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128); // Especifica una clau de 128 bits
        SecretKey secretKey = keyGen.generateKey();
        Utilities.imprimirExito("Clau AES generada.");
        return secretKey;
    }

    /**
     * Xifra el contingut del fitxer d'entrada utilitzant AES en mode CBC.
     * També genera un vector d'inicialització (IV) aleatori i el desa en un fitxer separat.
     *
     * @param activitat4Dir La ruta del directori de la activitat 4.
     * @param aesKey        La clau AES que s'utilitzarà per al xifrat.
     * @throws Exception En cas d'error durant el procés de xifrat.
     */
    private static void xifrarFitxer(Path activitat4Dir, SecretKey aesKey) throws Exception {
        Path inputFile = activitat4Dir.resolve(INPUT_FILE);
        Path encryptedFile = activitat4Dir.resolve(ENCRYPTED_FILE);
        Path ivFile = activitat4Dir.resolve(IV_FILE);

        // Generem un IV aleatori (vector d'inicialització) de 16 bytes per AES en mode CBC
        byte[] iv = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // Inicialitzem el Cipher per xifrar utilitzant AES/CBC/PKCS5Padding
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);

        // Llegeix el contingut del fitxer d'entrada i xifra el seu contingut
        try (FileInputStream fis = new FileInputStream(inputFile.toFile());
             FileOutputStream fos = new FileOutputStream(encryptedFile.toFile());
             CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                cos.write(buffer, 0, bytesRead);
            }
        }

        // Desa el IV en un fitxer separat perquè serà necessari per al descifrat
        try (FileOutputStream fosIV = new FileOutputStream(ivFile.toFile())) {
            fosIV.write(iv);
        }

        Utilities.imprimirExito("Fitxer xifrat desat a: " + encryptedFile.toString());
        Utilities.imprimirExito("Vector d'inicialització (IV) guardat a: " + ivFile.toString());
    }

    /**
     * Envolta (xifra) la clau AES utilitzant la clau pública RSA.
     * Aquest mètode és important perquè protegeix la clau simètrica: només es podrà recuperar
     * amb la clau privada RSA corresponent.
     *
     * @param activitat4Dir La ruta del directori de la activitat 4.
     * @param aesKey        La clau AES a embolcallar.
     * @param publicKey     La clau pública RSA.
     * @throws Exception En cas d'error durant el procés d'envoltament.
     */
    private static void xifrarClauAES(Path activitat4Dir, SecretKey aesKey, PublicKey publicKey) throws Exception {
        Path wrappedKeyPath = activitat4Dir.resolve(WRAPPED_KEY_FILE);

        // Inicialitzem el Cipher en mode WRAP per embolcallar la clau AES
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.WRAP_MODE, publicKey);

        // Envoltem (xifrem) la clau AES
        byte[] wrappedKey = cipher.wrap(aesKey);

        // Desa la clau embolcallada en un fitxer
        try (FileOutputStream fos = new FileOutputStream(wrappedKeyPath.toFile())) {
            fos.write(wrappedKey);
        }

        Utilities.imprimirExito("Clau AES embolcallada desada a: " + wrappedKeyPath.toString());
    }

    /**
     * Desxifra el fitxer xifrat utilitzant la clau AES recuperada
     * desempaquetant-la amb la clau privada RSA.
     *
     * El procediment és:
     * 1. Llegir el fitxer amb la clau AES embolcallada i desempaquetar-la amb la clau privada RSA.
     * 2. Llegir el vector d'inicialització (IV).
     * 3. Inicialitzar el Cipher per descifrar utilitzant AES/CBC/PKCS5Padding.
     * 4. Descifrar el fitxer "sortida.enc" i desar el contingut en "fitxer_desxifrat.txt".
     * 5. Mostrar el contingut desxifrat per la consola.
     *
     * @param activitat4Dir La ruta del directori de la activitat 4.
     * @param privateKey    La clau privada RSA.
     * @throws Exception En cas d'error durant el procés de descifrat.
     */
    private static void desxifrarFitxer(Path activitat4Dir, PrivateKey privateKey) throws Exception {
        Path encryptedFile = activitat4Dir.resolve(ENCRYPTED_FILE);
        Path wrappedKeyPath = activitat4Dir.resolve(WRAPPED_KEY_FILE);
        Path ivFile = activitat4Dir.resolve(IV_FILE);

        // Llegim la clau AES embolcallada del fitxer
        byte[] wrappedKey = carregarBytes(wrappedKeyPath.toFile());

        // Desenvolpem la clau AES amb la clau privada RSA
        Cipher cipherRSA = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipherRSA.init(Cipher.UNWRAP_MODE, privateKey);
        SecretKey aesKey = (SecretKey) cipherRSA.unwrap(wrappedKey, "AES", Cipher.SECRET_KEY);

        // Llegim el vector d'inicialització (IV) del fitxer
        byte[] iv = carregarBytes(ivFile.toFile());
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // Inicialitzem el Cipher per descifrar utilitzant AES/CBC/PKCS5Padding
        Cipher cipherAES = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipherAES.init(Cipher.DECRYPT_MODE, aesKey, ivSpec);

        // Desxifrem el fitxer xifrat i desem el contingut original en "fitxer_desxifrat.txt"
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

        // Llegim i mostrem el contingut desxifrat del fitxer per consola
        String contenidoDesxifrat = new String(Files.readAllBytes(decryptedFile), StandardCharsets.UTF_8);
        Utilities.imprimirInfo("Contingut desxifrat del fitxer:");
        System.out.println(contenidoDesxifrat);
    }

    /**
     * Carrega els bytes d'un fitxer.
     *
     * @param file El fitxer a llegir.
     * @return Un array de bytes amb el contingut del fitxer.
     * @throws IOException Si hi ha un error en la lectura del fitxer.
     */
    private static byte[] carregarBytes(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return fis.readAllBytes();
        }
    }

    /**
     * Carrega una clau pública RSA des d'un fitxer en format Base64.
     *
     * @param publicKeyPath La ruta del fitxer que conté la clau pública.
     * @return La clau pública RSA.
     * @throws Exception Si hi ha un error en la càrrega.
     */
    private static PublicKey carregarClauPublica(Path publicKeyPath) throws Exception {
        byte[] keyBytes = carregarBytes(publicKeyPath.toFile());
        byte[] decodedKey = Base64.getDecoder().decode(keyBytes);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    /**
     * Carrega una clau privada RSA des d'un fitxer en format Base64.
     *
     * @param privateKeyPath La ruta del fitxer que conté la clau privada.
     * @return La clau privada RSA.
     * @throws Exception Si hi ha un error en la càrrega.
     */
    private static PrivateKey carregarClauPrivada(Path privateKeyPath) throws Exception {
        byte[] keyBytes = carregarBytes(privateKeyPath.toFile());
        byte[] decodedKey = Base64.getDecoder().decode(keyBytes);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    /**
     * Desa una clau en un fitxer en format Base64.
     *
     * @param keyPath  La ruta del fitxer on es desa la clau.
     * @param keyBytes Un array de bytes que representa la clau.
     * @throws IOException Si hi ha un error en l'escriptura del fitxer.
     */
    private static void guardarClau(Path keyPath, byte[] keyBytes) throws IOException {
        String encodedKey = Base64.getEncoder().encodeToString(keyBytes);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(keyPath.toFile()))) {
            writer.write(encodedKey);
        }
    }
}
