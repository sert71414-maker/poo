# Proyecto Java: Solitario y Buscaminas en consola

Proyecto Maven para abrir directamente en IntelliJ IDEA (JDK 17+).

## Ejecutar
Abra la carpeta que contiene `pom.xml`, luego ejecute `src/main/java/Start.java`.

## Estructura
- `Start.java`: punto de entrada.
- `Iniciable.java`: interfaz con `void start()`.
- `MenuPrincipal.java`: menú de consola extensible.
- `Solitario.java`: Solitario simplificado en consola.
- `Juego.java` y `Tablero.java`: Buscaminas en consola.
- `Estadisticas.java`: estadísticas y top 3 guardados en `records_juegos.txt`.

## Comandos de Solitario
- `sacar`: sacar carta del mazo.
- `retirar`: retirar la carta del mazo (+10 puntos).
- `quitar 1` hasta `quitar 5`: retirar la carta visible de una columna (+10 puntos).
- `estadisticas`, `nuevo`, `ayuda`, `salir`.

El ranking de Solitario ordena por mayor puntaje y, en caso de empate, menor tiempo.

## Agregar otro juego
La clase debe implementar `Iniciable` y definir `public void start()`. Luego registre el juego en el constructor de `MenuPrincipal`, por ejemplo `registrarJuego("Ajedrez", Ajedrez::new);`.
