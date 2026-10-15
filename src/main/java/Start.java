public class Start implements Iniciable {
    public static void main(String[] args) {
        new Start().start();
    }

    @Override
    public void start() {
        new MenuPrincipal().start();
    }
}
