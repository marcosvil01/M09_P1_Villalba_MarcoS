package activitat_5;

import Functions.Utilities;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.util.Base64;

/**
 * Activitat 5:
 * 1. Llegeix el contingut d’un fitxer de text (document.txt).
 * 2. Genera una firma digital del document utilitzant una clau privada RSA.
 * 3. Verifica la firma amb la clau pública RSA.
 *
 * En aquest exemple:
 * - Es genera un parell de claus RSA (una clau privada i una clau pública).
 * - La clau privada s’utilitza per signar el contingut del fitxer.
 * - La clau pública s’utilitza per verificar la signatura.
 */
public class Activitat5 {

    // Definim els noms dels fitxers
    private static final String FITXER_ENTRADA = "document.txt";
    private static final String FITXER_FIRMA = "firmaDigital.txt";

    public static void iniciar() {
        try {
            Utilities.imprimirEncabezado("ACTIVITAT 5: FIRMA DIGITAL DEL DOCUMENT");

            // Obtenim la ruta del directori per a l'activitat 5 utilitzant Utilities (per exemple, output/activitat5/)
            Path activitat5Dir = Utilities.obtenerRutaActividad(5);

            // Comprovem si el fitxer d'entrada existeix; si no, el creem amb contingut predefinit.
            Path documentPath = activitat5Dir.resolve(FITXER_ENTRADA);
            if (!documentPath.toFile().exists()) {
                crearFitxerDocument(documentPath);
            } else {
                Utilities.imprimirAdvertencia("El fitxer d'entrada ja existeix: " + documentPath.toString());
            }

            // Llegim el contingut del document (document.txt)
            String contingut = llegirFitxer(documentPath);

            /*
              Generem un parell de claus RSA.
              Això vol dir que es creen dues claus:
                - La clau privada (es guarda en clauPrivada): s'utilitza per signar el document.
                - La clau pública (es guarda en clauPublica): s'utilitza per verificar la signatura.
            */
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048); // RSA de 2048 bits per a una seguretat acceptable
            KeyPair keyPair = keyGen.generateKeyPair();
            PrivateKey clauPrivada = keyPair.getPrivate();
            PublicKey clauPublica = keyPair.getPublic();
            Utilities.imprimirExito("Parell de claus RSA generat.");

            // Signem digitalment el document utilitzant la clau privada RSA.
            // El mètode signarDocument es fa servir per calcular el hash del contingut i, a continuació, cifrar-lo.
            byte[] firma = signarDocument(contingut.getBytes(StandardCharsets.UTF_8), clauPrivada);

            // Desa la firma en el fitxer firmaDigital.txt (codificant-la en Base64 perquè sigui llegible)
            Path firmaPath = activitat5Dir.resolve(FITXER_FIRMA);
            guardarFitxer(firmaPath, Base64.getEncoder().encodeToString(firma));
            Utilities.imprimirExito("Firma digital generada i guardada a: " + firmaPath.toString());

            // Verifiquem la firma utilitzant la clau pública RSA
            boolean esValida = verificarFirma(contingut.getBytes(StandardCharsets.UTF_8), firma, clauPublica);
            if (esValida) {
                Utilities.imprimirExito("La firma digital és vàlida.");
            } else {
                Utilities.imprimirError("La firma digital NO és vàlida.");
            }
        } catch (Exception e) {
            Utilities.imprimirError("Error durant el procés: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Si el fitxer document.txt no existeix, crea'l amb un contingut predefinit.
     *
     * @param documentPath La ruta del fitxer document.txt.
     */
    private static void crearFitxerDocument(Path documentPath) {
        String contingut = "Aquest és el contingut del document. Aquest text es signarà digitalment amb RSA.";
        try (BufferedWriter bw = Files.newBufferedWriter(documentPath, StandardCharsets.UTF_8)) {
            bw.write(contingut);
            Utilities.imprimirExito("Fitxer d'entrada creat a: " + documentPath.toString());
        } catch (IOException e) {
            Utilities.imprimirError("Error creant el fitxer d'entrada: " + e.getMessage());
        }
    }

    /**
     * Llegeix el contingut d'un fitxer de text.
     *
     * @param fitxer La ruta del fitxer a llegir.
     * @return El contingut del fitxer en forma de String.
     * @throws IOException Si hi ha un error durant la lectura.
     */
    private static String llegirFitxer(Path fitxer) throws IOException {
        StringBuilder contingut = new StringBuilder();
        try (BufferedReader br = Files.newBufferedReader(fitxer, StandardCharsets.UTF_8)) {
            String linia;
            while ((linia = br.readLine()) != null) {
                contingut.append(linia).append("\n");
            }
        }
        return contingut.toString().trim();
    }

    /**
     * Desa un contingut en un fitxer de text.
     *
     * @param fitxer    La ruta del fitxer on desar.
     * @param contingut El contingut a desar.
     * @throws IOException Si hi ha un error durant l'escriptura.
     */
    private static void guardarFitxer(Path fitxer, String contingut) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(fitxer, StandardCharsets.UTF_8)) {
            bw.write(contingut);
        }
    }

    /**
     * Signa digitalment un document utilitzant la clau privada RSA.
     *
     * @param dades       Els bytes del document.
     * @param clauPrivada La clau privada RSA.
     * @return La firma digital generada (un array de bytes).
     * @throws Exception Si hi ha un error en el procés de signatura.
     */
    private static byte[] signarDocument(byte[] dades, PrivateKey clauPrivada) throws Exception {
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(clauPrivada);
        signer.update(dades);
        return signer.sign();
    }

    /**
     * Verifica la firma digital d'un document utilitzant la clau pública RSA.
     *
     * @param dades       Els bytes del document.
     * @param firma       La firma digital (un array de bytes).
     * @param clauPublica La clau pública RSA.
     * @return Cert si la firma és vàlida, fals altrament.
     * @throws Exception Si hi ha un error en el procés de verificació.
     */
    private static boolean verificarFirma(byte[] dades, byte[] firma, PublicKey clauPublica) throws Exception {
        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(clauPublica);
        verifier.update(dades);
        return verifier.verify(firma);
    }
}
