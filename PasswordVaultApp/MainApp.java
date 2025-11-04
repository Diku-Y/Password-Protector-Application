import gui.AuthScreen;
import javax.swing.SwingUtilities;

public class MainApp {
    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new AuthScreen());
    }
}