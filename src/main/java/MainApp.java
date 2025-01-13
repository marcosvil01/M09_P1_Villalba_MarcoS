import Functions.Utilities;

import activitat_1.Activitat1;
import activitat_2.Activitat2;
import activitat_3.Activitat3;
import activitat_4.Activitat4;
// Importa las demás actividades si las hay, hasta activitat6

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Clase principal de la aplicación que maneja el menú y la ejecución de actividades.
 */
public class MainApp {
    // Mapa para asociar opciones con sus respectivas actividades
    private static final Map<Integer, Runnable> actividades = new HashMap<>();

    public static void main(String[] args) {
        // Inicializar carpetas de actividades al inicio del programa
        Utilities.crearCarpetasActividades();

        // Inicializar actividades
        inicializarActividades();

        Scanner scanner = new Scanner(System.in);
        int opcion = -1;

        while (opcion != 0) {
            mostrarMenuPrincipal();
            try {
                opcion = Integer.parseInt(scanner.nextLine());
                ejecutarOpcion(opcion);
            } catch (NumberFormatException e) {
                Utilities.imprimirError("Por favor, introduce un número válido.");
            }
        }

        scanner.close();
    }

    /**
     * Inicializa el mapa de actividades con sus respectivas acciones.
     */
    private static void inicializarActividades() {
        actividades.put(1, Activitat1::iniciar);
        actividades.put(2, Activitat2::iniciar);
        actividades.put(3, Activitat3::iniciar);
        actividades.put(4, Activitat4::iniciar);
        // Agrega más actividades aquí si las hay, hasta la 6
    }

    /**
     * Muestra el menú principal al usuario.
     */
    private static void mostrarMenuPrincipal() {
        String[] opciones = {
                "Ejecutar Actividad 1",
                "Ejecutar Actividad 2",
                "Ejecutar Actividad 3",
                "Ejecutar Actividad 4",
                "Ejecutar Actividad 5",
                "Ejecutar Actividad 6",
                "Salir"
        };

        Utilities.mostrarMenu("MENÚ PRINCIPAL", opciones);
    }

    /**
     * Ejecuta la opción seleccionada por el usuario.
     *
     * @param opcion La opción seleccionada.
     */
    private static void ejecutarOpcion(int opcion) {
        if (opcion >= 1 && opcion <= actividades.size()) {
            Runnable actividad = actividades.get(opcion);
            if (actividad != null) {
                actividad.run();
            } else {
                Utilities.imprimirError("La actividad seleccionada no está disponible.");
            }
        } else if (opcion == actividades.size() + 1) { // Asumiendo que "Salir" es la última opción
            Utilities.imprimirExito("Saliendo del programa. ¡Adiós!");
            System.exit(0);
        } else {
            Utilities.imprimirError("Opción no válida. Inténtalo de nuevo.");
        }
    }
}
