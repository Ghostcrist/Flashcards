package englishgame;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import javax.swing.*;

/** Ventanas de registro, recuperación y nueva contraseña (puerto de open_recovery / ask_new_password). */
public class AuthDialogs {

    // ---------- Base: ventana con textura de madera ----------
    static class WoodDialog extends JDialog {
        protected final JPanel body = new JPanel();

        WoodDialog(Component parent, String title, String subtitle, int w, int h) {
            super(SwingUtilities.getWindowAncestor(parent), title, ModalityType.APPLICATION_MODAL);
            setResizable(false);
            JPanel root = new JPanel(new BorderLayout(0, 14)) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    Theme.hq(g2);
                    Theme.paintWood(g2, getWidth(), getHeight());
                    g2.setColor(Theme.GOLD);
                    g2.setStroke(new BasicStroke(3f));
                    g2.draw(new RoundRectangle2D.Float(8, 8, getWidth() - 16, getHeight() - 16, 26, 26));
                    g2.dispose();
                }
            };
            root.setBorder(BorderFactory.createEmptyBorder(26, 38, 24, 38));

            JPanel head = new JPanel();
            head.setOpaque(false);
            head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
            JLabel t = new JLabel(title);
            t.setFont(Theme.serif(Font.BOLD, 26)); t.setForeground(Theme.GOLD);
            JLabel s = new JLabel(subtitle);
            s.setFont(Theme.font(Font.PLAIN, 12)); s.setForeground(new Color(250, 232, 198));
            head.add(t); head.add(Box.createVerticalStrut(4)); head.add(s);
            root.add(head, BorderLayout.NORTH);

            body.setOpaque(false);
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
            root.add(body, BorderLayout.CENTER);
            setContentPane(root);
            setSize(w, h);
            setLocationRelativeTo(parent);
        }

        JLabel label(String text) {
            JLabel l = new JLabel(text);
            l.setFont(Theme.font(Font.BOLD, 12)); l.setForeground(new Color(250, 232, 198));
            l.setAlignmentX(LEFT_ALIGNMENT);
            return l;
        }

        Theme.PillField field(String placeholder, int icon, boolean password) {
            Theme.PillField f = new Theme.PillField(placeholder, icon, password);
            f.setPreferredSize(new Dimension(300, 46));
            f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            f.setAlignmentX(LEFT_ALIGNMENT);
            return f;
        }

        Theme.PillButton button(String text, int style, int icon) {
            Theme.PillButton b = new Theme.PillButton(text, style, icon);
            b.setPreferredSize(new Dimension(300, 46));
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            b.setAlignmentX(LEFT_ALIGNMENT);
            return b;
        }

        void gap(int px) { body.add(Box.createVerticalStrut(px)); }
    }

    // ---------- Nueva contraseña ----------
    /** Devuelve la nueva contraseña o null si se cancela. */
    public static String newPassword(Component parent) {
        final String[] result = {null};
        final WoodDialog d = new WoodDialog(parent, "Nueva contraseña", "Ingresa y confirma tu nueva contraseña.", 460, 420);
        d.body.add(d.label("Nueva contraseña")); d.gap(5);
        final Theme.PillField p1 = d.field("Mínimo 6 caracteres", Theme.ICON_LOCK, true);
        d.body.add(p1); d.gap(12);
        d.body.add(d.label("Confirmar contraseña")); d.gap(5);
        final Theme.PillField p2 = d.field("Repite la contraseña", Theme.ICON_LOCK, true);
        d.body.add(p2); d.gap(18);
        Theme.PillButton save = d.button("GUARDAR NUEVA CONTRASEÑA", Theme.BTN_PRIMARY, Theme.ICON_NONE);
        d.body.add(save);
        save.addActionListener(e -> {
            String a = p1.getText().trim(), b = p2.getText().trim();
            if (a.length() < 6) {
                JOptionPane.showMessageDialog(d, "La contraseña debe tener al menos 6 caracteres.", "Contraseña", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!a.equals(b)) {
                JOptionPane.showMessageDialog(d, "Las contraseñas no coinciden.", "Contraseña", JOptionPane.ERROR_MESSAGE);
                return;
            }
            result[0] = a;
            d.dispose();
        });
        d.getRootPane().setDefaultButton(save);
        d.setVisible(true);
        return result[0];
    }

    // ---------- Recuperar contraseña ----------
    /** Verifica identidad con la pregunta de seguridad y permite crear una contraseña nueva. */
    public static boolean recovery(Component parent, String prefillEmail) {
        final boolean[] done = {false};
        final WoodDialog d = new WoodDialog(parent, "Restablecer contraseña",
                "Verifica tu identidad para crear una nueva contraseña.", 520, 470);
        d.body.add(d.label("Correo electrónico")); d.gap(5);
        final Theme.PillField email = d.field("Correo electrónico", Theme.ICON_MAIL, false);
        email.setText(prefillEmail == null ? "" : prefillEmail);
        d.body.add(email); d.gap(12);

        final Theme.PillButton verify = d.button("VERIFICAR CORREO", Theme.BTN_SECONDARY, Theme.ICON_NONE);
        d.body.add(verify); d.gap(14);

        final JLabel question = new JLabel(" ");
        question.setFont(Theme.font(Font.BOLD, 13)); question.setForeground(Theme.GOLD);
        question.setAlignmentX(Component.LEFT_ALIGNMENT);
        d.body.add(question); d.gap(6);

        final Theme.PillField answer = d.field("Tu respuesta", Theme.ICON_HELP, false);
        answer.setEnabled(false);
        d.body.add(answer); d.gap(8);

        final JLabel status = new JLabel(" ");
        status.setFont(Theme.font(Font.BOLD, 12)); status.setForeground(new Color(255, 170, 150));
        status.setAlignmentX(Component.LEFT_ALIGNMENT);
        d.body.add(status); d.gap(8);

        final Theme.PillButton reset = d.button("RESTABLECER CONTRASEÑA", Theme.BTN_PRIMARY, Theme.ICON_NONE);
        reset.setEnabled(false);
        d.body.add(reset);

        verify.addActionListener(e -> {
            UserStore.User u = UserStore.find(UserStore.readUsers(), email.getText());
            if (u == null) {
                JOptionPane.showMessageDialog(d, "No existe una cuenta registrada con ese correo.", "Cuenta no encontrada", JOptionPane.ERROR_MESSAGE);
                return;
            }
            question.setText("<html>" + u.pregunta + "</html>");
            answer.setEnabled(true);
            verify.setEnabled(false);
            reset.setEnabled(true);
            answer.field().requestFocusInWindow();
        });

        reset.addActionListener(e -> {
            List<UserStore.User> users = UserStore.readUsers();
            UserStore.User found = UserStore.find(users, email.getText());
            if (found == null) {
                JOptionPane.showMessageDialog(d, "No se encontró la cuenta.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!found.respuesta.trim().equalsIgnoreCase(answer.getText().trim())) {
                status.setText("La respuesta de recuperación es incorrecta.");
                return;
            }
            String np = newPassword(d);
            if (np == null) return;
            found.password = UserStore.clean(np);
            UserStore.saveUsers(users);
            JOptionPane.showMessageDialog(d, "Tu contraseña fue restablecida correctamente.\n\nYa puedes iniciar sesión.",
                    "Contraseña actualizada", JOptionPane.INFORMATION_MESSAGE);
            done[0] = true;
            d.dispose();
        });
        d.setVisible(true);
        return done[0];
    }

    // ---------- Registro ----------
    private static final String[] QUESTIONS = {
        "¿Cuál es tu fruta favorita?", "¿Cuál es tu comida favorita?", "¿Cómo se llama tu primera mascota?",
        "¿En qué ciudad naciste?", "¿Cómo se llama tu mejor amigo o amiga?"
    };

    /** Muestra el formulario de registro. Devuelve el correo creado o null si se cancela. */
    public static String register(Component parent) {
        final String[] created = {null};
        final WoodDialog d = new WoodDialog(parent, "Crear cuenta", "Únete a TalkStalk y aprende inglés jugando.", 500, 630);
        d.body.add(d.label("Nombre")); d.gap(4);
        final Theme.PillField name = d.field("Tu nombre", Theme.ICON_USER, false);
        d.body.add(name); d.gap(8);
        d.body.add(d.label("Correo electrónico")); d.gap(4);
        final Theme.PillField mail = d.field("correo@ejemplo.com", Theme.ICON_MAIL, false);
        d.body.add(mail); d.gap(8);
        d.body.add(d.label("Contraseña")); d.gap(4);
        final Theme.PillField p1 = d.field("Mínimo 6 caracteres", Theme.ICON_LOCK, true);
        d.body.add(p1); d.gap(8);
        d.body.add(d.label("Confirmar contraseña")); d.gap(4);
        final Theme.PillField p2 = d.field("Repite la contraseña", Theme.ICON_LOCK, true);
        d.body.add(p2); d.gap(8);
        d.body.add(d.label("Pregunta de recuperación")); d.gap(4);
        final JComboBox<String> q = new JComboBox<>(QUESTIONS);
        q.setFont(Theme.font(Font.PLAIN, 13));
        q.setBackground(Theme.CREAM); q.setForeground(Theme.TEXT);
        q.setPreferredSize(new Dimension(300, 38)); q.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        q.setAlignmentX(Component.LEFT_ALIGNMENT);
        d.body.add(q); d.gap(8);
        final Theme.PillField ans = d.field("Tu respuesta", Theme.ICON_HELP, false);
        d.body.add(ans); d.gap(14);
        Theme.PillButton go = d.button("CREAR CUENTA", Theme.BTN_PRIMARY, Theme.ICON_USER_PLUS);
        d.body.add(go);

        go.addActionListener(e -> {
            String n = UserStore.clean(name.getText()), m = UserStore.clean(mail.getText()).toLowerCase();
            String a = p1.getText().trim(), b = p2.getText().trim(), r = UserStore.clean(ans.getText());
            if (n.isEmpty() || m.isEmpty() || a.isEmpty() || r.isEmpty()) {
                JOptionPane.showMessageDialog(d, "Completa todos los campos.", "Datos incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!m.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
                JOptionPane.showMessageDialog(d, "Ingresa un correo electrónico válido.", "Correo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (a.length() < 6) {
                JOptionPane.showMessageDialog(d, "La contraseña debe tener al menos 6 caracteres.", "Contraseña", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!a.equals(b)) {
                JOptionPane.showMessageDialog(d, "Las contraseñas no coinciden.", "Contraseña", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                UserStore.User u = UserStore.register(n, m, a, (String) q.getSelectedItem(), r);
                created[0] = u.correo;
                JOptionPane.showMessageDialog(d, "¡Cuenta creada! Ya puedes iniciar sesión.", "Registro", JOptionPane.INFORMATION_MESSAGE);
                d.dispose();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(d, ex.getMessage(), "Registro", JOptionPane.ERROR_MESSAGE);
            }
        });
        d.getRootPane().setDefaultButton(go);
        d.setVisible(true);
        return created[0];
    }

    private AuthDialogs() { }
}
