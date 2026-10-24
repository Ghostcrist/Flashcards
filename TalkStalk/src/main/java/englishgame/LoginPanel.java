package englishgame;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.*;

/**
 * Pantalla de inicio de sesión: la ilustración de TalkStalk (personajes + cartel de madera)
 * es el fondo y los campos reales se colocan encima del tablero de madera.
 */
public class LoginPanel extends JPanel implements MainFrame.Refreshable {
    public interface Listener { void onLogin(UserStore.User user); }

    // Tamaño de la ilustración original; todas las posiciones están en estas coordenadas.
    private static final int IW = 1456, IH = 818;
    private static final Color CREAM_TEXT = new Color(250, 232, 198);

    private final Listener listener;
    private final BufferedImage bg = Theme.load("login_bg.jpg");
    private BufferedImage bgScaled;
    private int bgW = -1, bgH = -1;
    private final Color letterbox;
    private int attempts = 0;

    private final Theme.PillField email = new Theme.PillField("Correo electrónico", Theme.ICON_MAIL, false);
    private final Theme.PillField pass = new Theme.PillField("Contraseña", Theme.ICON_LOCK, true);
    private final RememberCheck remember = new RememberCheck("Recordarme");
    private final LinkButton forgot = new LinkButton("¿Olvidaste tu contraseña?");
    private final Theme.PillButton login = new Theme.PillButton("Iniciar sesión  \u2192", Theme.BTN_PRIMARY, Theme.ICON_HEART);
    private final Divider divider = new Divider();
    private final Theme.PillButton register = new Theme.PillButton("Registrarse", Theme.BTN_SECONDARY, Theme.ICON_USER_PLUS);
    private final BottomRow bottom = new BottomRow();

    public LoginPanel(Listener listener) {
        this.listener = listener;
        setLayout(null);
        setOpaque(true);
        letterbox = edgeColor();
        add(email); add(pass); add(remember); add(forgot); add(login); add(divider); add(register); add(bottom);

        login.addActionListener(e -> doLogin());
        email.field().addActionListener(e -> pass.field().requestFocusInWindow());
        pass.field().addActionListener(e -> doLogin());
        forgot.addActionListener(e -> {
            if (AuthDialogs.recovery(this, email.getText().trim())) pass.setText("");
        });
        register.addActionListener(e -> {
            String created = AuthDialogs.register(this);
            if (created != null) { email.setText(created); pass.setText(""); pass.field().requestFocusInWindow(); }
        });
        bottom.setClickListener(() -> email.field().requestFocusInWindow());
    }

    /** Color medio del borde de la imagen: se usa para rellenar las franjas si la ventana tiene otra proporción. */
    private Color edgeColor() {
        long r = 0, g = 0, b = 0; int n = 0;
        for (int x = 0; x < bg.getWidth(); x += 8) {
            for (int y : new int[]{bg.getHeight() - 1, bg.getHeight() - 2}) {
                int p = bg.getRGB(x, y);
                r += (p >> 16) & 255; g += (p >> 8) & 255; b += p & 255; n++;
            }
        }
        return new Color((int) (r / n), (int) (g / n), (int) (b / n));
    }

    @Override public void onShow() {
        attempts = 0;
        pass.setText("");
        String saved = UserStore.loadRemembered();
        email.setText(saved);
        remember.setSelected(!saved.isEmpty());
        SwingUtilities.invokeLater(() -> (saved.isEmpty() ? email : pass).field().requestFocusInWindow());
    }

    // ---------- Lógica de inicio de sesión (puerto de login()) ----------
    private void doLogin() {
        String mail = email.getText().trim().toLowerCase();
        String pw = pass.getText().trim();
        if (mail.isEmpty() || pw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa tu correo y contraseña.", "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<UserStore.User> users = UserStore.readUsers();
        for (UserStore.User u : users) {
            if (u.correo.toLowerCase().equals(mail) && u.password.equals(pw)) {
                attempts = 0;
                UserStore.saveRemembered(remember.isSelected() ? mail : "");
                pass.setText("");
                listener.onLogin(u);
                return;
            }
        }
        attempts++;
        if (attempts >= UserStore.MAX_LOGIN_ATTEMPTS) {
            int r = JOptionPane.showConfirmDialog(this,
                    "Has ingresado una contraseña incorrecta 3 veces.\n\n¿Deseas restablecer tu contraseña?",
                    "Recuperar contraseña", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            attempts = 0;
            if (r == JOptionPane.YES_OPTION) AuthDialogs.recovery(this, mail);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Correo o contraseña incorrectos.\n\nIntentos restantes: " + (UserStore.MAX_LOGIN_ATTEMPTS - attempts),
                    "Inicio de sesión", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- Diseño: posiciona los componentes sobre la ilustración ----------
    private double scale() { return Math.min((double) getWidth() / IW, (double) getHeight() / IH); }
    private int offX() { return (int) ((getWidth() - IW * scale()) / 2); }
    private int offY() { return (int) ((getHeight() - IH * scale()) / 2); }

    private void place(Component c, int x, int y, int w, int h) {
        double s = scale();
        c.setBounds(offX() + (int) Math.round(x * s), offY() + (int) Math.round(y * s),
                (int) Math.round(w * s), (int) Math.round(h * s));
    }

    @Override public void doLayout() {
        place(email, 880, 225, 450, 70);
        place(pass, 880, 315, 450, 70);
        place(remember, 884, 404, 200, 30);
        place(forgot, 1105, 404, 220, 30);
        place(login, 888, 458, 432, 72);
        place(divider, 888, 540, 432, 30);
        place(register, 890, 580, 428, 72);
        place(bottom, 888, 664, 432, 34);
        float px = (float) (15 * scale());
        forgot.setPx(px); bottom.setPx(px);
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.hq(g2);
        g2.setColor(letterbox);
        g2.fillRect(0, 0, getWidth(), getHeight());
        int w = (int) Math.round(IW * scale()), h = (int) Math.round(IH * scale());
        if (w < 1 || h < 1) { g2.dispose(); return; }
        if (bgScaled == null || bgW != w || bgH != h) { bgScaled = Theme.scaleHQ(bg, w, h); bgW = w; bgH = h; }
        g2.drawImage(bgScaled, offX(), offY(), null);
        g2.dispose();
    }

    // ================= Componentes pequeños =================

    /** Casilla "Recordarme" con el estilo del diseño. */
    private static class RememberCheck extends JComponent {
        private boolean selected;
        private final String text;
        RememberCheck(String text) {
            this.text = text;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { selected = !selected; repaint(); }
            });
        }
        boolean isSelected() { return selected; }
        void setSelected(boolean b) { selected = b; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.hq(g2);
            int h = getHeight(), box = Math.round(h * 0.86f), by = (h - box) / 2;
            g2.setColor(new Color(236, 208, 160));
            g2.fillRoundRect(2, by, box, box, box / 3, box / 3);
            g2.setColor(new Color(122, 72, 40));
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawRoundRect(2, by, box, box, box / 3, box / 3);
            if (selected) {
                g2.setColor(Theme.WOOD);
                g2.setStroke(new BasicStroke(Math.max(2f, box * 0.13f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(new Line2D.Float(2 + box * 0.24f, by + box * 0.52f, 2 + box * 0.43f, by + box * 0.72f));
                g2.draw(new Line2D.Float(2 + box * 0.43f, by + box * 0.72f, 2 + box * 0.78f, by + box * 0.28f));
            }
            g2.setFont(Theme.font(Font.PLAIN, Math.max(10, Math.round(h * 0.56f))));
            g2.setColor(CREAM_TEXT);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, box + h * 0.4f, (h - fm.getHeight()) / 2f + fm.getAscent());
            g2.dispose();
        }
    }

    /** Enlace subrayado. */
    private static class LinkButton extends JButton {
        private float px = 13;
        LinkButton(String text) {
            super(text);
            setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        void setPx(float p) { px = Math.max(10f, p); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.hq(g2);
            g2.setFont(Theme.font(Font.PLAIN, Math.round(px)));
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(getText()), x = getWidth() - w, y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(getModel().isRollover() ? Color.WHITE : CREAM_TEXT);
            g2.drawString(getText(), x, y);
            g2.drawLine(x, y + 3, x + w, y + 3);
            g2.dispose();
        }
    }

    /** Línea — o — entre los dos botones. */
    private static class Divider extends JComponent {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.hq(g2);
            int w = getWidth(), h = getHeight();
            g2.setFont(Theme.font(Font.BOLD, Math.max(11, Math.round(h * 0.6f))));
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth("o");
            g2.setColor(CREAM_TEXT);
            g2.drawString("o", (w - tw) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.setStroke(new BasicStroke(1.6f));
            g2.draw(new Line2D.Float(0, h / 2f, w / 2f - tw - 10, h / 2f));
            g2.draw(new Line2D.Float(w / 2f + tw + 10, h / 2f, w, h / 2f));
            g2.dispose();
        }
    }

    /** "¿Ya tienes una cuenta?  Inicia sesión" */
    private static class BottomRow extends JComponent {
        private float px = 13;
        private int linkX = Integer.MAX_VALUE;
        private Runnable click;
        BottomRow() {
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { if (e.getX() >= linkX && click != null) click.run(); }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    setCursor(e.getX() >= linkX ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
                }
            });
        }
        void setPx(float p) { px = Math.max(10f, p); }
        void setClickListener(Runnable r) { click = r; }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.hq(g2);
            g2.setFont(Theme.font(Font.PLAIN, Math.round(px)));
            FontMetrics fm = g2.getFontMetrics();
            String a = "¿Ya tienes una cuenta?", b = "Inicia sesión";
            int gap = Math.round(px * 1.0f), total = fm.stringWidth(a) + gap + fm.stringWidth(b);
            int x = (getWidth() - total) / 2, y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(CREAM_TEXT);
            g2.drawString(a, x, y);
            linkX = x + fm.stringWidth(a) + gap;
            g2.drawString(b, linkX, y);
            g2.drawLine(linkX, y + 3, linkX + fm.stringWidth(b), y + 3);
            g2.dispose();
        }
    }
}
