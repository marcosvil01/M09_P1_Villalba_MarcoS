package activitat_1;

import Functions.Utilities;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.Path; // Importació per manejar rutes de fitxers
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Classe que gestiona l'Activitat 1: Generació i Gestió de Claus AES per a Usuaris.
 *
 * Aquest programa permet:
 * 1. Generar una clau AES de 128 bits per a cada usuari d'una llista predeterminada.
 * 2. Mostrar el nom de l'usuari i la seva clau en format hexadecimal.
 * 3. Regenerar claus per a usuaris específics.
 * 4. Guardar i carregar claus des d'un fitxer CSV.
 */
public class Activitat1 {

    // Llista predeterminada d'usuaris
    private static final String[] USUARIS_PREDETERMINATS = {"Anna", "Joan", "Laura", "Pau"};
    // Mapa per associar cada usuari amb la seva clau AES en format hexadecimal
    private static final Map<String, String> usuarisClaus = new LinkedHashMap<>();
    // Ruta del fitxer CSV on es desaran les claus
    private static final Path CSV_FILE = Utilities.obtenerRutaActividad(1).resolve("claus_usuaris.csv");

    /**
     * Mètode principal que inicia l'activitat.
     * Presenta un menú interactiu per gestionar les claus dels usuaris.
     */
    public static void iniciar() {
        Scanner scanner = new Scanner(System.in);
        int opcio = -1;

        // Generar claus inicials per als usuaris predeterminats
        inicialitzarClaus();

        // Bucle principal del menú fins que l'usuari decideixi sortir
        while (opcio != 0) {
            Utilities.mostrarMenu("MENÚ ACTIVITAT 1", new String[]{
                    "Regenerar clau d'un usuari",
                    "Llistar usuaris i claus",
                    "Guardar claus al fitxer CSV",
                    "Carregar claus del fitxer CSV",
                    "Tornar al menú principal"
            });

            try {
                // Llegeix l'opció seleccionada per l'usuari
                String entrada = scanner.nextLine();
                opcio = Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                Utilities.imprimirError("Si us plau, introdueix un número vàlid.");
                continue;
            }

            // Executa l'acció corresponent a l'opció seleccionada
            switch (opcio) {
                case 1:
                    regenerarClau(scanner);
                    break;
                case 2:
                    llistarClaus();
                    break;
                case 3:
                    guardarClaus();
                    break;
                case 4:
                    carregarClaus();
                    break;
                case 5:
                    Utilities.imprimirExito("Tornant al menú principal...");
                    return; // Retorna al menú principal
                default:
                    Utilities.imprimirError("Opció no vàlida. Torna-ho a intentar.");
            }
        }

        scanner.close();
    }

    /**
     * Inicialitza les claus per als usuaris predeterminats.
     * Genera una clau AES de 128 bits per a cada usuari i la desa en format hexadecimal.
     */
    private static void inicialitzarClaus() {
        for (String usuari : USUARIS_PREDETERMINATS) {
            if (!usuarisClaus.containsKey(usuari)) {
                String clau = generarClauAES();
                if (clau != null) {
                    usuarisClaus.put(usuari, clau);
                } else {
                    Utilities.imprimirError("No s'ha pogut generar la clau per a " + usuari + ".");
                }
            }
        }
        Utilities.imprimirExito("Claus inicialitzades per als usuaris predeterminats.");
    }

    /**
     * Permet regenerar la clau AES d'un usuari específic.
     *
     * @param scanner Scanner per a la lectura d'entrada de l'usuari.
     */
    private static void regenerarClau(Scanner scanner) {
        System.out.print("Introdueix el nom de l'usuari per regenerar la clau: ");
        String nom = scanner.nextLine().trim();
        if (usuarisClaus.containsKey(nom)) {
            String novaClau = generarClauAES();
            if (novaClau != null) {
                usuarisClaus.put(nom, novaClau);
                Utilities.imprimirExito("Clau regenerada per a " + nom + ".");
            } else {
                Utilities.imprimirError("Error al generar la nova clau per a " + nom + ".");
            }
        } else {
            Utilities.imprimirAdvertencia("L'usuari \"" + nom + "\" no existeix a la llista predeterminada.");
        }
    }

    /**
     * Llista tots els usuaris i les seves claus AES en format hexadecimal.
     * Utilitza un format de taula per millorar la legibilitat.
     */
    private static void llistarClaus() {
        if (usuarisClaus.isEmpty()) {
            Utilities.imprimirAdvertencia("No hi ha cap clau generada.");
            return;
        }

        // Preparar dades per a la taula amb truncament de claus llargues
        String[] encabezados = {"Usuari", "Clau AES (Hexadecimal)"};
        List<String[]> filas = new ArrayList<>();
        for (Map.Entry<String, String> entry : usuarisClaus.entrySet()) {
            String clauTruncada = truncate(entry.getValue(), 20); // Trunca a 20 caràcters per facilitar la lectura
            filas.add(new String[]{entry.getKey(), clauTruncada});
        }

        // Imprimeix la taula utilitzant el mètode millorament de Utilities
        Utilities.imprimirTabla("LISTAT D'USUARIS I CLAUS AES", encabezados, filas);
    }

    /**
     * Trunca una cadena a una longitud específica i afegeix "..." si és necessari.
     *
     * @param value  La cadena original.
     * @param length La longitud màxima desitjada.
     * @return La cadena truncada si és més llarga que la longitud especificada, altrament retorna la cadena original.
     */
    private static String truncate(String value, int length) {
        if (value.length() <= length) {
            return value;
        } else {
            return value.substring(0, length - 3) + "...";
        }
    }

    /**
     * Desa les claus AES dels usuaris en un fitxer CSV.
     * Cada fila conté el nom de l'usuari i la seva clau AES en format hexadecimal.
     */
    private static void guardarClaus() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE.toFile()))) {
            // Escriu les capçaleres del CSV
            writer.write("Usuari,Clau_AES_Hexadecimal\n");
            for (Map.Entry<String, String> entry : usuarisClaus.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
            Utilities.imprimirExito("Claus guardades al fitxer: " + CSV_FILE.toString());
        } catch (IOException e) {
            Utilities.imprimirError("Error guardant el fitxer: " + e.getMessage());
        }
    }

    /**
     * Carrega les claus AES desades en un fitxer CSV.
     * Actualitza el mapa usuarisClaus amb les claus carregades.
     */
    private static void carregarClaus() {
        File file = CSV_FILE.toFile();
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                int carregades = 0;
                // Llegeix el fitxer línia per línia
                while ((line = reader.readLine()) != null) {
                    // Ignora la primera línia que conté les capçaleres
                    if (line.startsWith("Usuari,")) {
                        continue;
                    }
                    String[] parts = line.split(",");
                    if (parts.length == 2 && Arrays.asList(USUARIS_PREDETERMINATS).contains(parts[0])) {
                        usuarisClaus.put(parts[0], parts[1]);
                        carregades++;
                    }
                }
                Utilities.imprimirExito("Claus carregades des del fitxer. Total carregades: " + carregades + ".");
            } catch (IOException e) {
                Utilities.imprimirError("Error llegint el fitxer: " + e.getMessage());
            }
        } else {
            Utilities.imprimirAdvertencia("No s'ha trobat cap fitxer CSV per carregar.");
        }
    }

    /**
     * Genera una clau AES de 128 bits i la codifica en format hexadecimal.
     *
     * @return Clau AES codificada en hexadecimal o null si falla.
     */
    private static String generarClauAES() {
        try {
            // Creació d'un generador de claus AES
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128); // Especifica una clau de 128 bits
            SecretKey secretKey = keyGen.generateKey(); // Genera la clau secreta

            // Obtenir l'array de bytes de la clau AES
            byte[] keyBytes = secretKey.getEncoded();

            // Convertir els bytes de la clau a format hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : keyBytes) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0'); // Afegeix un zero per a nombres menors de 16
                }
                hexString.append(hex);
            }

            return hexString.toString(); // Retorna la clau en format hexadecimal
        } catch (NoSuchAlgorithmException e) {
            Utilities.imprimirError("Error generant la clau AES: " + e.getMessage());
            return null;
        }
    }
}
