import java.io.*;
import java.util.*;

public class Estadisticas {
    private static final String ARCHIVO = "records_juegos.txt";
    private final ArrayList<Record> recordsSolitario = new ArrayList<>();
    private final ArrayList<Record> recordsBuscaminas = new ArrayList<>();
    private int partidasSolitario;
    private int victoriasSolitario;
    private int derrotasSolitario;
    private int partidasBuscaminas;
    private int victoriasBuscaminas;
    private int derrotasBuscaminas;

    public Estadisticas() {
        cargar();
    }

    public synchronized void registrarSolitario(boolean gano, long segundos, int puntos, String nombre) {
        partidasSolitario++;
        if (gano) {
            victoriasSolitario++;
            recordsSolitario.add(new Record(nombre == null || nombre.trim().isEmpty() ? "Jugador" : nombre.trim(), segundos, puntos));
            ordenarYLimitar(recordsSolitario);
            guardar();
        } else {
            derrotasSolitario++;
        }
    }

    public synchronized void registrarBuscaminas(boolean gano, long segundos, String nombre) {
        partidasBuscaminas++;
        if (gano) {
            victoriasBuscaminas++;
            recordsBuscaminas.add(new Record(nombre == null || nombre.trim().isEmpty() ? "Jugador" : nombre.trim(), segundos, 0));
            ordenarYLimitar(recordsBuscaminas);
            guardar();
        } else {
            derrotasBuscaminas++;
        }
    }

    public synchronized void mostrarSolitario() {
        StringBuilder s = new StringBuilder();
        s.append("ESTADÍSTICAS DE SOLITARIO\n\n")
         .append("Partidas terminadas en esta sesión: ").append(partidasSolitario).append('\n')
         .append("Victorias: ").append(victoriasSolitario).append('\n')
         .append("Derrotas/partidas abandonadas: ").append(derrotasSolitario).append('\n')
         .append("\nTOP 3 — MAYOR PUNTAJE (desempate: menor tiempo)\n");
        agregarTop(s, recordsSolitario, true);
        System.out.println("\n" + s);
    }

    public synchronized void mostrarBuscaminas() {
        StringBuilder s = new StringBuilder();
        s.append("ESTADÍSTICAS DE BUSCAMINAS\n\n")
         .append("Partidas terminadas en esta sesión: ").append(partidasBuscaminas).append('\n')
         .append("Victorias: ").append(victoriasBuscaminas).append('\n')
         .append("Derrotas: ").append(derrotasBuscaminas).append('\n')
         .append("\nTOP 3 — VICTORIAS MÁS RÁPIDAS\n");
        agregarTop(s, recordsBuscaminas, false);
        System.out.println("\n" + s);
    }

    private void agregarTop(StringBuilder s, ArrayList<Record> records, boolean mostrarPuntos) {
        if (records.isEmpty()) {
            s.append("Todavía no hay victorias registradas.\n");
            return;
        }
        for (int i = 0; i < records.size() && i < 3; i++) {
            Record r = records.get(i);
            s.append(i + 1).append(". ").append(r.nombre)
             .append(" — ").append(formatearTiempo(r.segundos));
            if (mostrarPuntos) s.append(" — ").append(r.puntos).append(" puntos");
            s.append('\n');
        }
    }

    private void ordenarYLimitar(ArrayList<Record> records) {
        records.sort((a, b) -> {
            int porPuntos = Integer.compare(b.puntos, a.puntos);
            if (porPuntos != 0) return porPuntos;
            return Long.compare(a.segundos, b.segundos);
        });
        while (records.size() > 3) records.remove(records.size() - 1);
    }

    private String formatearTiempo(long segundos) {
        return String.format("%02d:%02d", segundos / 60, segundos % 60);
    }

    private void cargar() {
        File f = new File(ARCHIVO);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] p = linea.split("\\|");
                if (p.length != 4) continue;
                Record r = new Record(p[0], Long.parseLong(p[1]), Integer.parseInt(p[2]));
                if (p[3].equals("S")) recordsSolitario.add(r);
                if (p[3].equals("B")) recordsBuscaminas.add(r);
            }
            ordenarYLimitar(recordsSolitario);
            ordenarYLimitar(recordsBuscaminas);
        } catch (Exception e) {
            System.err.println("No se pudieron leer los récords guardados: " + e.getMessage());
        }
    }

    private void guardar() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO))) {
            for (Record r : recordsSolitario) pw.println(r.nombre + "|" + r.segundos + "|" + r.puntos + "|S");
            for (Record r : recordsBuscaminas) pw.println(r.nombre + "|" + r.segundos + "|" + r.puntos + "|B");
        } catch (IOException e) {
            System.err.println("No se pudieron guardar los récords: " + e.getMessage());
        }
    }

    private static class Record {
        String nombre;
        long segundos;
        int puntos;
        Record(String nombre, long segundos, int puntos) {
            this.nombre = nombre;
            this.segundos = segundos;
            this.puntos = puntos;
        }
    }
}
