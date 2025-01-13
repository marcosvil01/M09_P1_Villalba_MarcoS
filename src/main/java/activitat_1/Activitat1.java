package activitat_1;

import Functions.Utilities;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.Path; // Importación añadida
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Clase que maneja la Actividad 1: Gestión de claves de usuarios.
 */
public class Activitat1 {

    // Lista predeterminada de usuarios
    private static final String[] USUARIS_PREDETERMINATS = {"Anna", "Joan", "Laura", "Pau"};
    private static final Map<String, String> usuarisClaus = new LinkedHashMap<>();
    private static final Path CSV_FILE = Utilities.obtenerRutaActividad(1).resolve("claus_usuaris.csv");

    /**
     * Iniciar la actividad.
     */
    public static void iniciar() {
        Scanner scanner = new Scanner(System.in);
        int opcio = -1;

        // Generar claves iniciales para los usuarios predeterminados
        inicialitzarClaus();

        while (opcio != 0) {
            Utilities.mostrarMenu("MENÚ ACTIVITAT 1", new String[]{
                    "Regenerar claus d'un usuari",
                    "Llistar usuaris i claus",
                    "Guardar claus al fitxer CSV",
                    "Carregar claus del fitxer CSV",
                    "Tornar al menú principal"
            });

            try {
                String entrada = scanner.nextLine();
                opcio = Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                Utilities.imprimirError("Si us plau, introdueix un número vàlid.");
                continue;
            }

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
     * Inicializa las claves para los usuarios predeterminados.
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
     * Regenera la clave de un usuario.
     *
     * @param scanner Scanner para la entrada del usuario.
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
     * Lista las claves de todos los usuarios.
     */
    private static void llistarClaus() {
        if (usuarisClaus.isEmpty()) {
            Utilities.imprimirAdvertencia("No hi ha cap clau generada.");
            return;
        }

        // Preparar datos para la tabla con truncamiento de claves largas
        String[] encabezados = {"Usuari", "Clau"};
        List<String[]> filas = new ArrayList<>();
        for (Map.Entry<String, String> entry : usuarisClaus.entrySet()) {
            String clauTruncada = truncate(entry.getValue(), 20); // Trunca a 20 caracteres
            filas.add(new String[]{entry.getKey(), clauTruncada});
        }

        // Imprimir la tabla utilizando el método mejorado
        Utilities.imprimirTabla("LISTAT D'USUARIS I CLAUS", encabezados, filas);
    }

    /**
     * Trunca una cadena a una longitud específica.
     *
     * @param value  La cadena original.
     * @param length La longitud máxima.
     * @return La cadena truncada si es necesario.
     */
    private static String truncate(String value, int length) {
        if (value.length() <= length) {
            return value;
        } else {
            return value.substring(0, length - 3) + "...";
        }
    }

    /**
     * Guarda las claves en un archivo CSV.
     */
    private static void guardarClaus() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE.toFile()))) {
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
     * Carga las claves desde un archivo CSV.
     */
    private static void carregarClaus() {
        File file = CSV_FILE.toFile();
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                int carregades = 0;
                while ((line = reader.readLine()) != null) {
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
     * Genera una clave AES de 128 bits y la codifica en Base64.
     *
     * @return Clave AES codificada en Base64 o null si falla.
     */
    private static String generarClauAES() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            SecretKey secretKey = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            Utilities.imprimirError("Error generant la clau AES: " + e.getMessage());
            return null;
        }
    }
}
