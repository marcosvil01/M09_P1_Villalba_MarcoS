import Functions.Utilities;

import activitat_1.Activitat1;
import activitat_2.Activitat2;
import activitat_3.Activitat3;
import activitat_4.Activitat4;
import activitat_5.Activitat5;
import activitat_6.Activitat6;
// Si hi ha més activitats, s’afegeixen aquí

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Classe principal de l'aplicació.
 * Aquesta classe gestiona el menú d'opcions i l'execució de cada activitat.
 *
 * La interfície està dissenyada per ser intuïtiva, amb textos en català i missatges
 * estilitzats gràcies als mètodes de la classe Utilities.
 */
public class MainApp {
    // Mapa per associar opcions amb les activitats (1 a 6)
    private static final Map<Integer, Runnable> activitats = new HashMap<>();

    public static void main(String[] args) {
        // Inicialitzar les carpetes de sortida per a totes les activitats
        Utilities.crearCarpetasActividades();

        // Registrem les activitats al mapa
        inicialitzarActivitats();

        Scanner scanner = new Scanner(System.in);
        int opcio = -1;

        do {
            mostrarMenuPrincipal();
            try {
                String entrada = scanner.nextLine();
                opcio = Integer.parseInt(entrada);
                executarOpcio(opcio);
            } catch (NumberFormatException e) {
                Utilities.imprimirError("Si us plau, introdueix un número vàlid.");
            }
            // Pausa abans de tornar al menú: sol·licitem que l'usuari premi Enter
            Utilities.imprimirInfo("Prem Enter per tornar al menú...");
            scanner.nextLine();
        } while (opcio != 0);

        scanner.close();
    }

    /**
     * Inicialitza el mapa d'activitats, associant cada número amb la seva execució.
     */
    private static void inicialitzarActivitats() {
        activitats.put(1, Activitat1::iniciar);
        activitats.put(2, Activitat2::iniciar);
        activitats.put(3, Activitat3::iniciar);
        activitats.put(4, Activitat4::iniciar);
        activitats.put(5, Activitat5::iniciar);
        activitats.put(6, Activitat6::iniciar);
        // Afegeix més activitats aquí si n'hi ha
    }

    /**
     * Mostra el menú principal en català amb totes les opcions disponibles.
     */
    private static void mostrarMenuPrincipal() {
        String[] opcions = {
                "Executar Activitat 1",
                "Executar Activitat 2",
                "Executar Activitat 3",
                "Executar Activitat 4",
                "Executar Activitat 5",
                "Executar Activitat 6",
                "Sortir"
        };

        // Mostrem un encapçalament i el menú d'opcions
        Utilities.imprimirEncabezado("MENÚ PRINCIPAL");
        Utilities.mostrarMenu("Tria una opció", opcions);
    }

    /**
     * Executa l'opció seleccionada per l'usuari.
     *
     * @param opcio L'opció introduïda per l'usuari.
     */
    private static void executarOpcio(int opcio) {
        if (opcio >= 1 && opcio <= activitats.size()) {
            Utilities.imprimirInfo("S'executarà l'activitat " + opcio + "...");
            Runnable activitat = activitats.get(opcio);
            if (activitat != null) {
                activitat.run();
            } else {
                Utilities.imprimirError("L'activitat seleccionada no està disponible.");
            }
        } else if (opcio == activitats.size() + 1) { // Suposant que "Sortir" és l'última opció
            Utilities.imprimirExito("Sortint del programa. Adéu!");
            System.exit(0);
        } else {
            Utilities.imprimirError("Opció no vàlida. Torna-ho a intentar.");
        }
    }
}
