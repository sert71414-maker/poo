import java.util.ArrayDeque;
import java.util.Random;

public class Tablero {

    public enum Estado { JUGANDO, GANADO, PERDIDO }

    private final int filas;
    private final int columnas;
    private final int totalMinas;

    private final boolean[][] mina;
    private final int[][] vecinas;
    private final boolean[][] revelada;
    private final boolean[][] bandera;

    private Estado estado;
    private boolean primerClic;
    private int reveladas;
    private int banderas;
    private int filaExplotada;
    private int columnaExplotada;

    public Tablero(int filas, int columnas, int totalMinas) {
        this.filas = filas;
        this.columnas = columnas;
        this.totalMinas = totalMinas;
        this.mina = new boolean[filas][columnas];
        this.vecinas = new int[filas][columnas];
        this.revelada = new boolean[filas][columnas];
        this.bandera = new boolean[filas][columnas];
        reiniciar();
    }

    public void reiniciar() {
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                mina[f][c] = false;
                vecinas[f][c] = 0;
                revelada[f][c] = false;
                bandera[f][c] = false;
            }
        }
        estado = Estado.JUGANDO;
        primerClic = true;
        reveladas = 0;
        banderas = 0;
        filaExplotada = -1;
        columnaExplotada = -1;
    }

    public void revelar(int f, int c) {
        if (estado != Estado.JUGANDO || !dentro(f, c) || bandera[f][c] || revelada[f][c]) return;

        if (primerClic) {
            colocarMinas(f, c);
            primerClic = false;
        }

        if (mina[f][c]) {
            estado = Estado.PERDIDO;
            filaExplotada = f;
            columnaExplotada = c;
            return;
        }

        expandir(f, c);

        if (reveladas == filas * columnas - totalMinas) {
            estado = Estado.GANADO;
        }
    }

    public void alternarBandera(int f, int c) {
        if (estado != Estado.JUGANDO || !dentro(f, c) || revelada[f][c]) return;
        bandera[f][c] = !bandera[f][c];
        banderas += bandera[f][c] ? 1 : -1;
    }

    public int getFilas() { return filas; }
    public int getColumnas() { return columnas; }
    public int getTotalMinas() { return totalMinas; }
    public Estado getEstado() { return estado; }
    public boolean esPrimerClic() { return primerClic; }
    public int getFilaExplotada() { return filaExplotada; }
    public int getColumnaExplotada() { return columnaExplotada; }

    public boolean estaRevelada(int f, int c) { return revelada[f][c]; }
    public boolean tieneBandera(int f, int c) { return bandera[f][c]; }
    public boolean tieneMina(int f, int c) { return mina[f][c]; }
    public int minasVecinas(int f, int c) { return vecinas[f][c]; }

    public int minasRestantes() {
        return estado == Estado.GANADO ? 0 : totalMinas - banderas;
    }

    private boolean dentro(int f, int c) {
        return f >= 0 && f < filas && c >= 0 && c < columnas;
    }

    private void colocarMinas(int fSegura, int cSegura) {
        Random rnd = new Random();
        int colocadas = 0;
        while (colocadas < totalMinas) {
            int f = rnd.nextInt(filas);
            int c = rnd.nextInt(columnas);
            boolean cercaDelClic = Math.abs(f - fSegura) <= 1 && Math.abs(c - cSegura) <= 1;
            if (!mina[f][c] && !cercaDelClic) {
                mina[f][c] = true;
                colocadas++;
            }
        }
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                int n = 0;
                for (int df = -1; df <= 1; df++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        int nf = f + df, nc = c + dc;
                        if (dentro(nf, nc) && mina[nf][nc]) n++;
                    }
                }
                vecinas[f][c] = n;
            }
        }
    }

    private void expandir(int fInicio, int cInicio) {
        ArrayDeque<int[]> pila = new ArrayDeque<>();
        pila.push(new int[]{fInicio, cInicio});

        while (!pila.isEmpty()) {
            int[] p = pila.pop();
            int f = p[0], c = p[1];
            if (!dentro(f, c) || revelada[f][c] || bandera[f][c]) continue;

            revelada[f][c] = true;
            reveladas++;

            if (vecinas[f][c] == 0) {
                for (int df = -1; df <= 1; df++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (df != 0 || dc != 0) pila.push(new int[]{f + df, c + dc});
                    }
                }
            }
        }
    }
}
