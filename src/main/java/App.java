import javax.swing.*;

public class App {

    private static App app;

    public static void main(String[] args) {
        app = new App();
        app.start();
    }

    private void start() {
        MainFrame frame = new MainFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle("EEG App");
        frame.setVisible(true);
    }
}
