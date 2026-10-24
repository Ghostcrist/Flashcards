package englishgame;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;

/** Ventana principal: cambia entre pantallas con CardLayout. Empieza en el login. */
public class MainFrame extends JFrame {
    public static final String LOGIN = "login", DASHBOARD = "dashboard", FLASH = "flash",
            SENTENCE = "sentence", PROGRESS = "progress";

    /** Las pantallas que implementan esto se actualizan cada vez que se muestran. */
    public interface Refreshable { void onShow(); }

    private final CardLayout layout = new CardLayout();
    private final JPanel root = new JPanel(layout);
    private final Map<String, Component> screens = new HashMap<>();
    private UserStore.User currentUser;

    public MainFrame() {
        super("TalkStalk");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 600));
        root.setBackground(Theme.BG);
        root.setPreferredSize(new Dimension(1200, 675));

        register(LOGIN, new LoginPanel(this::onLogin));
        register(DASHBOARD, new DashboardPanel(this));
        register(FLASH, new FlashCardsPanel(this));
        register(SENTENCE, new SentenceBuilderPanel(this));
        register(PROGRESS, new ProgressPanel(this));
        setContentPane(root);
        show(LOGIN);
        pack();
        setLocationRelativeTo(null);
    }

    private void register(String name, Component c) {
        screens.put(name, c);
        root.add(c, name);
    }

    public void show(String name) {
        Component c = screens.get(name);
        if (c instanceof Refreshable) ((Refreshable) c).onShow();
        layout.show(root, name);
    }

    public UserStore.User getCurrentUser() { return currentUser; }

    public void onLogin(UserStore.User user) {
        currentUser = user;
        Progress.get().reset();
        Progress.get().setPlayerName(user.nombre);
        show(DASHBOARD);
    }

    /** Cierra la sesión y vuelve al login. */
    public void logout() {
        currentUser = null;
        Progress.get().reset();
        show(LOGIN);
    }
}
