import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.Stack;

public class Solitario implements Iniciable {
    private final ArrayList<ArrayList<Carta>> columnas = new ArrayList<>();
    private final Stack<Carta> mazo = new Stack<>();
    private final Estadisticas estadisticas;
    private final Scanner scanner;
    private Carta cartaDelMazo;
    private int puntos;
    private int cartasRetiradas;
    private long inicio;
    private boolean partidaTerminada;

    public Solitario(Scanner scanner, Estadisticas estadisticas) {
        this.scanner = scanner;
        this.estadisticas = estadisticas;
    }

    @Override
    public void start() {
        boolean jugar = true;
        while (jugar) {
            iniciarJuego();
            jugar = buclePartida();
        }
    }

    private void iniciarJuego() {
        columnas.clear();
        mazo.clear();
        cartaDelMazo = null;
        puntos = 0;
        cartasRetiradas = 0;
        partidaTerminada = false;
        inicio = System.currentTimeMillis();

        ArrayList<Carta> baraja = new ArrayList<>();
        String[] palos = {"♥", "♦", "♣", "♠"};
        String[] valores = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        for (String palo : palos) {
            for (String valor : valores) baraja.add(new Carta(valor, palo));
        }
        Collections.shuffle(baraja);

        for (int i = 0; i < 5; i++) {
            ArrayList<Carta> columna = new ArrayList<>();
            Carta oculta = baraja.remove(0);
            oculta.visible = false;
            columna.add(oculta);
            Carta visible = baraja.remove(0);
            visible.visible = true;
            columna.add(visible);
            columnas.add(columna);
        }
        while (!baraja.isEmpty()) mazo.push(baraja.remove(0));
    }

    private boolean buclePartida() {
        while (!partidaTerminada) {
            mostrarMesa();
            System.out.println("\nComandos: sacar | retirar | quitar 1-5 | estadisticas | nuevo | ayuda | salir");
            System.out.print("> ");
            if (!scanner.hasNextLine()) {
                registrarAbandono();
                return false;
            }
            String entrada = scanner.nextLine().trim().toLowerCase();
            if (entrada.isEmpty()) continue;
            String[] partes = entrada.split("\\s+");
            switch (partes[0]) {
                case "sacar":
                    sacarDelMazo();
                    break;
                case "retirar":
                    retirarCartaDelMazo();
                    break;
                case "quitar":
                    if (partes.length < 2) {
                        System.out.println("Indicá la columna: quitar 1, quitar 2, ... quitar 5.");
                    } else {
                        try {
                            int columna = Integer.parseInt(partes[1]);
                            retirarCartaColumna(columna - 1);
                        } catch (NumberFormatException e) {
                            System.out.println("La columna debe ser un número del 1 al 5.");
                        }
                    }
                    break;
                case "estadisticas":
                case "estadística":
                case "stats":
                    estadisticas.mostrarSolitario();
                    break;
                case "nuevo":
                    registrarAbandono();
                    return true;
                case "ayuda":
                    mostrarAyuda();
                    break;
                case "salir":
                    registrarAbandono();
                    return false;
                default:
                    System.out.println("Comando no reconocido. Escribí 'ayuda' para ver los comandos.");
            }
        }
        return preguntarOtraPartida();
    }

    private void mostrarMesa() {
        System.out.println("\n========== SOLITARIO ==========");
        System.out.printf("Puntaje: %d | Cartas retiradas: %d/52 | Tiempo: %s | Mazo: %d cartas%n",
                puntos, cartasRetiradas, formatearTiempo(segundos()), mazo.size());
        System.out.println("Carta actual del mazo: " + (cartaDelMazo == null ? "ninguna" : cartaDelMazo));
        System.out.println("\nColumnas:");
        for (int i = 0; i < columnas.size(); i++) {
            ArrayList<Carta> columna = columnas.get(i);
            System.out.print((i + 1) + ": ");
            if (columna.isEmpty()) {
                System.out.println("[vacía]");
            } else {
                for (int j = 0; j < columna.size(); j++) {
                    Carta carta = columna.get(j);
                    System.out.print(carta.visible ? "[" + carta + "] " : "[##] ");
                }
                System.out.println();
            }
        }
    }

    private void sacarDelMazo() {
        if (cartaDelMazo != null) {
            System.out.println("Primero usá 'retirar' para retirar la carta actual.");
            return;
        }
        if (mazo.isEmpty()) {
            System.out.println("El mazo está vacío.");
            return;
        }
        cartaDelMazo = mazo.pop();
        cartaDelMazo.visible = true;
        System.out.println("Sacaste: " + cartaDelMazo + ". Escribí 'retirar' para retirarla y sumar 10 puntos.");
    }

    private void retirarCartaDelMazo() {
        if (cartaDelMazo == null) {
            System.out.println("Primero escribí 'sacar' para sacar una carta del mazo.");
            return;
        }
        cartaDelMazo = null;
        sumarCartaRetirada();
        System.out.println("Carta retirada: +10 puntos.");
    }

    private void retirarCartaColumna(int indice) {
        if (indice < 0 || indice >= columnas.size()) {
            System.out.println("Columna inválida. Usá un número del 1 al 5.");
            return;
        }
        ArrayList<Carta> columna = columnas.get(indice);
        if (columna.isEmpty()) {
            System.out.println("Esa columna está vacía.");
            return;
        }
        Carta arriba = columna.get(columna.size() - 1);
        if (!arriba.visible) {
            System.out.println("La carta de esa columna está oculta.");
            return;
        }
        columna.remove(columna.size() - 1);
        if (!columna.isEmpty()) columna.get(columna.size() - 1).visible = true;
        sumarCartaRetirada();
        System.out.println("Retiraste " + arriba + ": +10 puntos.");
    }

    private void sumarCartaRetirada() {
        puntos += 10;
        cartasRetiradas++;
        if (cartasRetiradas == 52) {
            partidaTerminada = true;
            long tiempo = segundos();
            System.out.println("\n¡Ganaste el Solitario!");
            System.out.println("Puntaje: " + puntos + " | Tiempo: " + formatearTiempo(tiempo));
            System.out.print("Escribí tu nombre para el ranking: ");
            String nombre = scanner.hasNextLine() ? scanner.nextLine().trim() : "Jugador";
            estadisticas.registrarSolitario(true, tiempo, puntos, nombre);
        }
    }

    private void registrarAbandono() {
        if (!partidaTerminada) {
            estadisticas.registrarSolitario(false, segundos(), puntos, "Jugador");
            partidaTerminada = true;
        }
    }

    private boolean preguntarOtraPartida() {
        while (true) {
            System.out.print("¿Querés jugar otra partida de Solitario? (s/n): ");
            if (!scanner.hasNextLine()) return false;
            String respuesta = scanner.nextLine().trim().toLowerCase();
            if (respuesta.equals("s") || respuesta.equals("si") || respuesta.equals("sí")) return true;
            if (respuesta.equals("n") || respuesta.equals("no")) return false;
            System.out.println("Respondé s o n.");
        }
    }

    private void mostrarAyuda() {
        System.out.println("\n=== AYUDA DE SOLITARIO ===");
        System.out.println("sacar          : sacar una carta del mazo");
        System.out.println("retirar        : retirar la carta del mazo que acabás de sacar (+10 puntos)");
        System.out.println("quitar 1       : retirar la carta visible de la columna 1 (+10 puntos)");
        System.out.println("estadisticas   : mostrar partidas, victorias, derrotas y top 3");
        System.out.println("nuevo          : abandonar esta partida y empezar otra");
        System.out.println("salir          : volver al menú principal");
        System.out.println("Cada columna inicia con una carta oculta y una visible.");
    }

    private long segundos() {
        return Math.max(0, (System.currentTimeMillis() - inicio) / 1000);
    }

    private String formatearTiempo(long segundos) {
        return String.format("%02d:%02d", segundos / 60, segundos % 60);
    }

    private static class Carta {
        private final String valor;
        private final String palo;
        private boolean visible;
        private Carta(String valor, String palo) { this.valor = valor; this.palo = palo; }
        @Override public String toString() { return valor + palo; }
    }
}
