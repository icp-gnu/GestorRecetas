package GestorRecetas;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.stream.Collectors;

public class GestorRecetas {

    private static final int OPCION_SALIR = 6;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        GestorFicheros.verificarEInicializarCatalogo();

        Configuracion config = GestorFicheros.cargarConfiguracion();
        mostrarCabecera(config);

        List<Receta> catalogo = GestorFicheros.cargarRecetasBinario();

        int opcion = 0;
        try {
            do {
                mostrarMenu();
                opcion = leerEntero(sc, "Selecciona una opción (1-6): ", -1);

                switch (opcion) {
                    case 1 -> mostrarTodas(catalogo);
                    case 2 -> buscarPorId(sc, catalogo);
                    case 3 -> modificarTiempo(sc, catalogo);
                    case 4 -> agregarReceta(sc, catalogo);
                    case 5 -> GestorFicheros.exportarAXML(catalogo);
                    case OPCION_SALIR -> System.out.println("Guardando estado del sistema antes de cerrar...");
                    default -> System.out.println("[!] Opción no disponible.");
                }
            } while (opcion != OPCION_SALIR);
        } catch (NoSuchElementException e) {
            System.out.println("\nEntrada cerrada. Saliendo...");
            opcion = OPCION_SALIR;
        }

        config.setUltimaOpcion(opcion);
        config.setTotalRecetas(catalogo.size());
        GestorFicheros.guardarConfiguracion(config);

        System.out.println("=================================================");
        System.out.println("¡Hasta luego!");
        sc.close();
    }

    private static void mostrarCabecera(Configuracion config) {
        System.out.println("=================================================");
        System.out.println("            GESTOR DE RECETAS v1.0               ");
        System.out.println("=================================================");
        if (config.getUltimaOpcion() > 0) {
            System.out.println("[SESIÓN ANTERIOR] Última opción usada: " + config.getUltimaOpcion()
                    + " | Total recetas en ese momento: " + config.getTotalRecetas());
        } else {
            System.out.println("[SESIÓN ANTERIOR] Primera ejecución o sin historial registrado.");
        }
    }

    private static void mostrarMenu() {
        System.out.println("\n----------------- MENÚ PRINCIPAL -----------------");
        System.out.println("1. Mostrar todas las recetas");
        System.out.println("2. Buscar receta por ID");
        System.out.println("3. Modificar tiempo de preparación de una receta");
        System.out.println("4. Añadir nueva receta al catálogo");
        System.out.println("5. Exportar catálogo a XML (recetas.xml)");
        System.out.println("6. Salir de la aplicación");
        System.out.println("--------------------------------------------------");
    }

    private static int leerEntero(Scanner sc, String mensaje, int porDefecto) {
        System.out.print(mensaje);
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("[!] Introduce un número entero válido.");
            return porDefecto;
        }
    }

    private static void mostrarTodas(List<Receta> catalogo) {
        System.out.println("\n--- CATÁLOGO DE RECETAS REGISTRADAS (" + catalogo.size() + ") ---");
        if (catalogo.isEmpty()) {
            System.out.println("El catálogo está vacío.");
            return;
        }
        for (Receta r : catalogo) {
            System.out.println(r);
        }
    }

    private static void buscarPorId(Scanner sc, List<Receta> catalogo) {
        int id = leerEntero(sc, "Introduce el ID de la receta a buscar: ", -1);
        if (id < 0) {
            return;
        }
        Receta r = localizar(catalogo, id);
        if (r != null) {
            System.out.println("\n[ENCONTRADA] " + r);
        } else {
            System.out.println("[!] No existe ninguna receta con el ID " + id);
        }
    }

    private static void modificarTiempo(Scanner sc, List<Receta> catalogo) {
        int id = leerEntero(sc, "Introduce el ID de la receta a modificar: ", -1);
        Receta r = localizar(catalogo, id);
        if (r == null) {
            System.out.println("[!] Receta no localizada.");
            return;
        }
        System.out.println("Receta actual: " + r.getNombre() + " (tiempo actual: " + r.getTiempoPreparacion() + " min)");
        int nuevoTiempo = leerEntero(sc, "Introduce el nuevo tiempo de preparación (minutos): ", 0);
        if (nuevoTiempo <= 0) {
            System.out.println("[!] El tiempo debe ser un número mayor que 0. No se han hecho cambios.");
            return;
        }
        r.setTiempoPreparacion(nuevoTiempo);
        if (GestorFicheros.guardarRecetasBinario(catalogo)) {
            System.out.println("[OK] Tiempo actualizado correctamente en 'recetas.dat'.");
        }
    }

    private static void agregarReceta(Scanner sc, List<Receta> catalogo) {
        int nuevoId = catalogo.stream().mapToInt(Receta::getId).max().orElse(0) + 1;
        System.out.println("\n--- REGISTRAR NUEVA RECETA (ID asignado: " + nuevoId + ") ---");

        System.out.print("Nombre de la receta: ");
        String nombre = sc.nextLine().trim();
        if (nombre.isEmpty()) {
            System.out.println("[!] El nombre no puede estar vacío. Operación cancelada.");
            return;
        }

        System.out.print("Categoría (Entrante / Plato Principal / Postre / etc.): ");
        String categoria = sc.nextLine().trim();

        int tiempo = leerEntero(sc, "Tiempo de preparación (minutos): ", 0);
        if (tiempo <= 0) {
            System.out.println("[!] El tiempo debe ser un número mayor que 0. Operación cancelada.");
            return;
        }

        System.out.print("Dificultad (Fácil / Media / Difícil): ");
        String dificultad = sc.nextLine().trim();

        System.out.print("Ingredientes (separados por comas): ");
        List<String> ingredientes = Arrays.stream(sc.nextLine().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        catalogo.add(new Receta(nuevoId, nombre, categoria, tiempo, dificultad, ingredientes));
        if (GestorFicheros.guardarRecetasBinario(catalogo)) {
            System.out.println("[OK] Receta '" + nombre + "' agregada con éxito a 'recetas.dat'.");
        }
    }

    private static Receta localizar(List<Receta> catalogo, int id) {
        for (Receta r : catalogo) {
            if (r.getId() == id) {
                return r;
            }
        }
        return null;
    }
}
