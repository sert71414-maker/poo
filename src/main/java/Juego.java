import java.util.Scanner;

public class Juego implements Iniciable {
    private static final int FILAS = 10;
    private static final int COLUMNAS = 10;
    private static final int MINAS = 12;

    private final Tablero tablero = new Tablero(FILAS, COLUMNAS, MINAS);
    private final Scanner scanner;
    private final Estadisticas estadisticas;
    private long inicio;

    public Juego(Scanner scanner, Estadisticas estadisticas) {
        this.scanner = scanner;
        this.estadisticas = estadisticas;
    }

    @Override
    public void start() {
        jugar();
    }

    public void jugar() {
        boolean otra = true;
        while (otra) {
            nuevaPartida();
            otra = buclePartida();
        }
    }

    private void nuevaPartida() {
        tablero.reiniciar();
        inicio = 0;
        System.out.println();
        mostrarAyuda();
    }

    private boolean buclePartida() {
        while (true) {
            dibujarTablero();
            if (tablero.getEstado() != Tablero.Estado.JUGANDO) {
                long tiempo = segundos();
                boolean gano = tablero.getEstado() == Tablero.Estado.GANADO;
                String nombre = "Jugador";
                if (gano) {
                    System.out.print("¡Victoria! Escribe tu nombre para el ranking: ");
                    if (scanner.hasNextLine()) {
                        String entradaNombre = scanner.nextLine().trim();
                        if (!entradaNombre.isEmpty()) nombre = entradaNombre;
                    }
                }
                estadisticas.registrarBuscaminas(gano, tiempo, nombre);
                anunciarResultado(tiempo, gano);
                return preguntarOtraPartida();
            }

            System.out.print("> ");
            if (!scanner.hasNextLine()) return false;
            String entrada = scanner.nextLine().trim().toLowerCase();
            if (entrada.isEmpty()) continue;
            String[] partes = entrada.split("\\s+");

            switch (partes[0]) {
                case "mostrar":
                case "m":
                    ejecutarMostrar(partes);
                    break;
                case "bandera":
                case "b":
                    ejecutarBandera(partes);
                    break;
                case "estadisticas":
                case "estadística":
                case "stats":
                    estadisticas.mostrarBuscaminas();
                    break;
                case "reiniciar":
                    nuevaPartida();
                    break;
                case "ayuda":
                    mostrarAyuda();
                    break;
                case "salir":
                    return false;
                default:
                    System.out.println("Comando no reconocido. Escribe 'ayuda'.");
            }
        }
    }

    private void ejecutarMostrar(String[] partes) {
        int[] pos = leerCasilla(partes);
        if (pos == null) return;
        boolean primer = tablero.esPrimerClic();
        tablero.revelar(pos[0], pos[1]);
        if (primer && !tablero.esPrimerClic()) inicio = System.currentTimeMillis();
    }

    private void ejecutarBandera(String[] partes) {
        int[] pos = leerCasilla(partes);
        if (pos == null) return;
        if (tablero.estaRevelada(pos[0], pos[1])) {
            System.out.println("Esa casilla ya está descubierta.");
            return;
        }
        tablero.alternarBandera(pos[0], pos[1]);
    }

    private int[] leerCasilla(String[] partes) {
        if (partes.length < 2) {
            System.out.println("Falta la casilla. Ejemplo: " + partes[0] + " D5");
            return null;
        }
        String texto = partes[1];
        if (texto.length() < 2) {
            System.out.println("Casilla no válida.");
            return null;
        }
        char letra = texto.charAt(0);
        int columna = Character.toLowerCase(letra) - 'a';
        int fila;
        try {
            fila = Integer.parseInt(texto.substring(1)) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Casilla no válida: " + texto.toUpperCase());
            return null;
        }
        if (columna < 0 || columna >= COLUMNAS || fila < 0 || fila >= FILAS) {
            System.out.println("Fuera del tablero. Usa A-J y 1-10.");
            return null;
        }
        return new int[]{fila, columna};
    }

    private void dibujarTablero() {
        System.out.printf("%nMinas restantes: %d | Tiempo: %ds%n%n",
                tablero.minasRestantes(), segundos());
        System.out.print("     ");
        for (int c = 0; c < COLUMNAS; c++) System.out.print((char) ('A' + c) + " ");
        System.out.println("\n   +" + "-".repeat(COLUMNAS * 2 + 1) + "+");
        for (int f = 0; f < FILAS; f++) {
            System.out.printf("%2d | ", f + 1);
            for (int c = 0; c < COLUMNAS; c++) System.out.print(simbolo(f, c) + " ");
            System.out.printf("| %d%n", f + 1);
        }
        System.out.println("   +" + "-".repeat(COLUMNAS * 2 + 1) + "+");
        System.out.print("     ");
        for (int c = 0; c < COLUMNAS; c++) System.out.print((char) ('A' + c) + " ");
        System.out.println();
    }

    private char simbolo(int f, int c) {
        Tablero.Estado estado = tablero.getEstado();
        if (tablero.estaRevelada(f, c)) {
            int n = tablero.minasVecinas(f, c);
            return n > 0 ? (char) ('0' + n) : '.';
        }
        boolean mina = tablero.tieneMina(f, c);
        boolean bandera = tablero.tieneBandera(f, c);
        if (estado == Tablero.Estado.PERDIDO) {
            if (f == tablero.getFilaExplotada() && c == tablero.getColumnaExplotada()) return '@';
            if (mina && !bandera) return '*';
            if (!mina && bandera) return 'X';
        }
        if (estado == Tablero.Estado.GANADO && mina) return 'F';
        return bandera ? 'F' : '#';
    }

    private long segundos() {
        return inicio == 0 ? 0 : (System.currentTimeMillis() - inicio) / 1000;
    }

    private void mostrarAyuda() {
        System.out.println("=== BUSCAMINAS ===");
        System.out.println("mostrar D5    -> descubrir casilla (atajo: m D5)");
        System.out.println("bandera D5    -> poner/quitar bandera (atajo: b D5)");
        System.out.println("estadisticas  -> ver estadísticas y top 3 desde el juego");
        System.out.println("reiniciar     -> empezar otra partida");
        System.out.println("ayuda         -> ver instrucciones");
        System.out.println("salir          -> volver al menú");
        System.out.println("Símbolos: # oculta | . vacía | 1-8 minas cercanas | F bandera | * mina | @ mina explotada");
        System.out.println("La primera casilla que descubras nunca contiene una mina.");
    }

    private void anunciarResultado(long tiempo, boolean gano) {
        if (gano) System.out.println("¡Ganaste en " + tiempo + " segundos!");
        else System.out.println("¡Pisaste una mina! Perdiste.");
    }

    private boolean preguntarOtraPartida() {
        while (true) {
            System.out.print("¿Jugar otra vez? (s/n): ");
            if (!scanner.hasNextLine()) return false;
            String r = scanner.nextLine().trim().toLowerCase();
            if (r.equals("s") || r.equals("si") || r.equals("sí")) return true;
            if (r.equals("n") || r.equals("no")) return false;
        }
    }
}
