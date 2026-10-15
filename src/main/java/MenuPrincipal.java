import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Supplier;

public class MenuPrincipal implements Iniciable {
    private final Scanner scanner = new Scanner(System.in);
    private final Estadisticas estadisticas = new Estadisticas();
    private final List<OpcionJuego> juegos = new ArrayList<>();

    public MenuPrincipal() {
        registrarJuego("Solitario", () -> new Solitario(scanner, estadisticas));
        registrarJuego("Buscaminas", () -> new Juego(scanner, estadisticas));
    }

    public void registrarJuego(String nombre, Supplier<? extends Iniciable> creador) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del juego no puede estar vacío.");
        }
        if (creador == null) {
            throw new IllegalArgumentException("El creador del juego no puede ser null.");
        }
        juegos.add(new OpcionJuego(nombre.trim(), creador));
    }

    @Override
    public void start() {
        boolean salir = false;
        while (!salir) {
            System.out.println("\n==============================");
            System.out.println("        MENÚ DE JUEGOS");
            System.out.println("==============================");
            for (int i = 0; i < juegos.size(); i++) {
                System.out.println((i + 1) + ". " + juegos.get(i).nombre);
            }
            System.out.println("0. Salir");
            System.out.print("Elegí una opción: ");

            if (!scanner.hasNextLine()) break;
            String entrada = scanner.nextLine().trim();
            int opcion;
            try {
                opcion = Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Opción inválida. Escribí un número del menú.");
                continue;
            }

            if (opcion == 0) {
                salir = true;
            } else if (opcion >= 1 && opcion <= juegos.size()) {
                OpcionJuego seleccionado = juegos.get(opcion - 1);
                try {
                    Iniciable juego = seleccionado.creador.get();
                    juego.start();
                } catch (RuntimeException e) {
                    System.out.println("No se pudo iniciar " + seleccionado.nombre + ": " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("Opción inválida. Elegí una opción que aparezca en el menú.");
            }
        }
        System.out.println("Programa finalizado.");
    }

    private static class OpcionJuego {
        private final String nombre;
        private final Supplier<? extends Iniciable> creador;

        private OpcionJuego(String nombre, Supplier<? extends Iniciable> creador) {
            this.nombre = nombre;
            this.creador = creador;
        }
    }
}
