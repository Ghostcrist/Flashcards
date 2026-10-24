package englishgame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                UserStore.ensureFiles();
                Pronunciation.preload(); // carga el modelo de voz en segundo plano
                UIManager.put("OptionPane.background", Theme.BG);
                UIManager.put("Panel.background", Theme.BG);
                UIManager.put("OptionPane.messageForeground", Theme.TEXT);
                UIManager.put("OptionPane.messageFont", Theme.font(java.awt.Font.PLAIN, 13));
                new MainFrame().setVisible(true);
            }
        });
    }
}
