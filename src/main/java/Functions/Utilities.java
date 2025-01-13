package Functions;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Clase Utilities que proporciona constantes y métodos útiles para la aplicación.
 */
public class Utilities {
    // Enumeración para estilos de texto
    public enum Estilo {
        RESET("\u001B[0m"),
        NEGRITA("\u001B[1m"),
        SUBRAYADO("\u001B[4m");

        private final String codigo;

        Estilo(String codigo) {
            this.codigo = codigo;
        }

        public String getCodigo() {
            return codigo;
        }
    }

    // Enumeración para colores de texto
    public enum ColorTexto {
        NEGRO("\u001B[30m"),
        ROJO("\u001B[31m"),
        VERDE("\u001B[32m"),
        AMARILLO("\u001B[33m"),
        AZUL("\u001B[34m"),
        MAGENTA("\u001B[35m"),
        CIAN("\u001B[36m"),
        BLANCO("\u001B[37m"),
        BRILLANTE_ROJO("\u001B[91m"),
        BRILLANTE_VERDE("\u001B[92m"),
        BRILLANTE_AMARILLO("\u001B[93m");

        private final String codigo;

        ColorTexto(String codigo) {
            this.codigo = codigo;
        }

        public String getCodigo() {
            return codigo;
        }
    }

    // Enumeración para colores de fondo
    public enum ColorFondo {
        FONDO_NEGRO("\u001B[40m"),
        FONDO_ROJO("\u001B[41m"),
        FONDO_VERDE("\u001B[42m"),
        FONDO_AMARILLO("\u001B[43m"),
        FONDO_AZUL("\u001B[44m"),
        FONDO_MAGENTA("\u001B[45m"),
        FONDO_CIAN("\u001B[46m"),
        FONDO_BLANCO("\u001B[47m");

        private final String codigo;

        ColorFondo(String codigo) {
            this.codigo = codigo;
        }

        public String getCodigo() {
            return codigo;
        }
    }

    // Símbolos útiles
    public static final String FLECHA = "→";
    public static final String CHECK = "\u2713"; // ✓
    public static final String CRUZ = "\u2717";  // ✗
    public static final String ESTRELLA = "\u2605"; // ★
    public static final String INFO = "\u2139\uFE0F"; // ℹ️
    public static final String WARN = "\u26A0\uFE0F"; // ⚠️
    public static final String SUCCESS = "\u2714\uFE0F"; // ✔️

    // Ruta base para las salidas
    private static final Path RUTA_BASE = Paths.get("output");

    /**
     * Crear una carpeta si no existe.
     *
     * @param ruta Ruta de la carpeta a crear.
     */
    public static void crearCarpeta(Path ruta) {
        File carpeta = ruta.toFile();
        if (!carpeta.exists()) {
            if (carpeta.mkdirs()) {
                imprimirMensaje("Carpeta creada: " + ruta.toString(), ColorTexto.BRILLANTE_VERDE, Estilo.NEGRITA);
            } else {
                imprimirMensaje("Error al crear la carpeta: " + ruta.toString(), ColorTexto.BRILLANTE_ROJO, Estilo.NEGRITA);
            }
        } else {
            imprimirMensaje("La carpeta ya existe: " + ruta.toString(), ColorTexto.AZUL, Estilo.SUBRAYADO);
        }
    }

    /**
     * Obtener la ruta base de las carpetas de salida.
     *
     * @return Ruta de la carpeta base.
     */
    public static Path obtenerRutaOutput() {
        crearCarpeta(RUTA_BASE);
        return RUTA_BASE;
    }

    /**
     * Obtener la ruta específica para una actividad.
     *
     * @param numeroActividad Número de la actividad (1 a 6).
     * @return Ruta de la carpeta de la actividad.
     */
    public static Path obtenerRutaActividad(int numeroActividad) {
        Path rutaActividad = RUTA_BASE.resolve("activitat" + numeroActividad);
        crearCarpeta(rutaActividad);
        return rutaActividad;
    }

    /**
     * Crear automáticamente las carpetas de las actividades (activitat1 a activitat6).
     */
    public static void crearCarpetasActividades() {
        boolean algunaCarpetaCreada = false;

        for (int i = 1; i <= 6; i++) {
            Path rutaActividad = obtenerRutaActividad(i);
            File carpeta = rutaActividad.toFile();

            if (!carpeta.exists()) {
                crearCarpeta(rutaActividad);
                algunaCarpetaCreada = true;
            }
        }

        if (algunaCarpetaCreada) {
            imprimirMensaje("Carpetas de las actividades creadas automáticamente.", ColorTexto.BRILLANTE_VERDE, Estilo.NEGRITA);
        } else {
            imprimirMensaje("Todas las carpetas ya existían, no se ha creado ninguna carpeta nueva.", ColorTexto.AZUL, Estilo.SUBRAYADO);
        }
    }

    /**
     * Imprimir un mensaje con color y estilo.
     *
     * @param texto  El texto a imprimir.
     * @param color  El color del texto.
     * @param estilo El estilo del texto.
     */
    public static void imprimirMensaje(String texto, ColorTexto color, Estilo estilo) {
        System.out.println(estilo.getCodigo() + color.getCodigo() + texto + Estilo.RESET.getCodigo());
    }

    /**
     * Imprimir un mensaje con color.
     *
     * @param texto El texto a imprimir.
     * @param color El color del texto.
     */
    public static void imprimirConColor(String texto, ColorTexto color) {
        System.out.println(color.getCodigo() + texto + Estilo.RESET.getCodigo());
    }

    /**
     * Imprimir un mensaje con color y fondo.
     *
     * @param texto El texto a imprimir.
     * @param color El color del texto.
     * @param fondo El color de fondo.
     */
    public static void imprimirConColorFondo(String texto, ColorTexto color, ColorFondo fondo) {
        System.out.println(color.getCodigo() + fondo.getCodigo() + texto + Estilo.RESET.getCodigo());
    }

    /**
     * Imprimir un encabezado con estilo.
     *
     * @param texto El texto del encabezado.
     */
    public static void imprimirEncabezado(String texto) {
        imprimirMensaje(texto, ColorTexto.CIAN, Estilo.NEGRITA);
    }

    /**
     * Imprimir un mensaje de éxito.
     *
     * @param texto El texto del mensaje.
     */
    public static void imprimirExito(String texto) {
        System.out.println(ColorTexto.BRILLANTE_VERDE.getCodigo() + "✔ " + texto + Estilo.RESET.getCodigo());
    }

    /**
     * Imprimir un mensaje de error.
     *
     * @param texto El texto del mensaje.
     */
    public static void imprimirError(String texto) {
        System.out.println(ColorTexto.BRILLANTE_ROJO.getCodigo() + "✘ " + texto + Estilo.RESET.getCodigo());
    }

    /**
     * Imprimir un mensaje de advertencia.
     *
     * @param texto El texto del mensaje.
     */
    public static void imprimirAdvertencia(String texto) {
        System.out.println(ColorTexto.AMARILLO.getCodigo() + "⚠ " + texto + Estilo.RESET.getCodigo());
    }

    /**
     * Imprimir un mensaje de información.
     *
     * @param texto El texto del mensaje.
     */
    public static void imprimirInfo(String texto) {
        System.out.println(ColorTexto.AZUL.getCodigo() + "ℹ " + texto + Estilo.RESET.getCodigo());
    }

    /**
     * Mostrar un menú interactivo simple.
     *
     * @param titulo   El título del menú.
     * @param opciones Array de opciones del menú.
     */
    public static void mostrarMenu(String titulo, String[] opciones) {
        imprimirEncabezado("\n" + ESTRELLA + " " + titulo + " " + ESTRELLA);
        for (int i = 0; i < opciones.length; i++) {
            System.out.println(FLECHA + " " + (i + 1) + ". " + opciones[i]);
        }
        System.out.print("Seleccione una opción: ");
    }

    /**
     * Imprimir una tabla con colores suaves.
     *
     * @param titulo      El título de la tabla.
     * @param encabezados Array de encabezados de la tabla.
     * @param filas        Lista de filas de la tabla.
     */
    public static void imprimirTabla(String titulo, String[] encabezados, List<String[]> filas) {
        imprimirEncabezado("\n" + titulo);

        // Calcular el ancho de cada columna
        int[] anchos = new int[encabezados.length];
        for (int i = 0; i < encabezados.length; i++) {
            anchos[i] = encabezados[i].length();
        }

        for (String[] fila : filas) {
            for (int i = 0; i < fila.length; i++) {
                if (fila[i].length() > anchos[i]) {
                    anchos[i] = fila[i].length();
                }
            }
        }

        // Añadir un poco de espacio extra
        for (int i = 0; i < anchos.length; i++) {
            anchos[i] += 2;
        }

        // Construir el separador de la tabla
        StringBuilder separador = new StringBuilder();
        separador.append("+");
        for (int ancho : anchos) {
            separador.append("-".repeat(Math.max(0, ancho)));
            separador.append("+");
        }

        // Imprimir el encabezado de la tabla
        System.out.println(separador.toString());
        System.out.print("|");
        for (int i = 0; i < encabezados.length; i++) {
            System.out.print(" " + Estilo.SUBRAYADO.getCodigo() + encabezados[i] + Estilo.RESET.getCodigo());
            // Rellenar el espacio restante
            int espacios = anchos[i] - encabezados[i].length() - 1;
            System.out.print(" ".repeat(Math.max(0, espacios)));
            System.out.print("|");
        }
        System.out.println();
        System.out.println(separador.toString());

        // Imprimir las filas de la tabla
        for (String[] fila : filas) {
            System.out.print("|");
            for (int i = 0; i < fila.length; i++) {
                System.out.print(" " + fila[i]);
                // Rellenar el espacio restante
                int espacios = anchos[i] - fila[i].length() - 1;
                System.out.print(" ".repeat(Math.max(0, espacios)));
                System.out.print("|");
            }
            System.out.println();
        }
        System.out.println(separador.toString());
    }
}
