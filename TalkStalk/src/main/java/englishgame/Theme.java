package englishgame;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Tema tropical de TalkStalk: madera, crema, hojas verdes y colores de fruta. */
public class Theme {
    // ---------- Paleta ----------
    public static final Color BG = new Color(255, 244, 222);
    public static final Color BG2 = new Color(249, 221, 178);
    public static final Color WOOD = new Color(104, 60, 32);
    public static final Color WOOD_LIGHT = new Color(156, 94, 52);
    public static final Color GOLD = new Color(238, 186, 82);
    public static final Color CREAM = new Color(250, 232, 198);
    public static final Color CARD = new Color(255, 250, 240);
    public static final Color TAN = new Color(214, 176, 124);
    public static final Color GREEN = new Color(104, 176, 84);
    public static final Color GREEN_DARK = new Color(46, 104, 46);
    public static final Color ORANGE = new Color(232, 128, 60);
    public static final Color RED = new Color(206, 62, 56);
    public static final Color TEXT = new Color(84, 46, 24);
    public static final Color MUTED = new Color(150, 110, 80);
    public static final Color DARK = WOOD;
    public static final Color OK_BG = new Color(226, 247, 214), OK_LINE = new Color(104, 176, 84), OK_TEXT = new Color(40, 100, 40);
    public static final Color BAD_BG = new Color(255, 228, 220), BAD_LINE = new Color(226, 90, 70), BAD_TEXT = new Color(150, 40, 30);

    public static Font font(int style, int size) { return new Font("SansSerif", style, size); }
    public static Font serif(int style, int size) { return new Font("Serif", style, size); }

    public static void hq(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    public static Color blend(Color a, Color b, float t) {
        return new Color((int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                         (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                         (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    // ---------- Pantalla con fondo degradado cálido ----------
    public static class Screen extends JPanel {
        public Screen() { setOpaque(true); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new GradientPaint(0, 0, BG, 0, getHeight(), BG2));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    // ---------- Panel redondeado ----------
    public static class RoundPanel extends JPanel {
        private Color fill, line;
        private final int arc;
        public RoundPanel(Color fill, Color line, int arc) {
            this.fill = fill; this.line = line; this.arc = arc; setOpaque(false);
        }
        public void setColors(Color f, Color l) { fill = f; line = l; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            hq(g2);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            if (line != null) {
                g2.setColor(line);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ---------- Botón redondeado simple ----------
    public static class RoundButton extends JButton {
        private final Color bg, fg;
        public RoundButton(String text, Color bg, Color fg) {
            super(text);
            this.bg = bg; this.fg = fg;
            setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setFont(font(Font.BOLD, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(11, 20, 11, 20));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            hq(g2);
            Color c = bg, t = fg;
            if (!isEnabled()) { c = blend(bg, Color.WHITE, 0.6f); t = blend(fg, Color.WHITE, 0.4f); }
            else if (getModel().isRollover()) c = blend(bg, Color.WHITE, 0.15f);
            g2.setColor(c);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.setFont(getFont());
            g2.setColor(t);
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    // ---------- Barra de progreso ----------
    public static class Bar extends JComponent {
        private double value = 0;
        private final Color track;
        private Color fill;
        public Bar(Color track, Color fill) {
            this.track = track; this.fill = fill; setPreferredSize(new Dimension(100, 10));
        }
        public void setFill(Color c) { fill = c; repaint(); }
        public void setValue(double v) { value = Math.max(0, Math.min(1, v)); repaint(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            hq(g2);
            g2.setColor(track); g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.setColor(fill);  g2.fillRoundRect(0, 0, (int) (getWidth() * value), getHeight(), getHeight(), getHeight());
            g2.dispose();
        }
    }

    // ---------- Imágenes ----------
    private static final Map<String, BufferedImage> SRC = new HashMap<>();

    /** Carga /englishgame/images/<name>. Si no existe devuelve un marcador. */
    public static BufferedImage load(String name) {
        BufferedImage img = SRC.get(name);
        if (img == null) {
            try {
                img = ImageIO.read(Theme.class.getResourceAsStream("/englishgame/images/" + name));
            } catch (Exception e) {
                img = new BufferedImage(420, 420, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = img.createGraphics();
                g.setColor(CREAM); g.fillRect(0, 0, 420, 420);
                g.setColor(MUTED); g.setFont(font(Font.BOLD, 28));
                g.drawString("sin imagen", 120, 215);
                g.dispose();
            }
            SRC.put(name, img);
        }
        return img;
    }

    private static BufferedImage draw(BufferedImage src, int w, int h) {
        BufferedImage o = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = o.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return o;
    }

    /** Reduce una imagen con buena calidad (pasos de a mitades). */
    public static BufferedImage scaleHQ(BufferedImage src, int tw, int th) {
        tw = Math.max(1, tw); th = Math.max(1, th);
        BufferedImage cur = src;
        int w = src.getWidth(), h = src.getHeight();
        while (w >= tw * 2 && h >= th * 2) { w = (w + 1) / 2; h = (h + 1) / 2; cur = draw(cur, w, h); }
        if (w != tw || h != th) cur = draw(cur, tw, th);
        return cur;
    }

    private static BufferedImage mask(BufferedImage img, Shape shape) {
        BufferedImage o = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = o.createGraphics();
        hq(g);
        g.fill(shape);
        g.setComposite(AlphaComposite.SrcIn);
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return o;
    }

    /** Imagen redonda (recorte cuadrado hacia arriba, donde suele estar la cara) con aro de color. */
    public static ImageIcon circle(String name, int size) {
        BufferedImage src = load(name);
        int side = Math.min(src.getWidth(), src.getHeight());
        int x = (src.getWidth() - side) / 2, y = (int) ((src.getHeight() - side) * 0.18);
        BufferedImage sq = scaleHQ(src.getSubimage(x, y, side, side), size, size);
        BufferedImage c = mask(sq, new Ellipse2D.Float(0, 0, size, size));
        Graphics2D g = c.createGraphics();
        hq(g);
        g.setColor(GOLD); g.setStroke(new BasicStroke(Math.max(2f, size / 32f)));
        g.draw(new Ellipse2D.Float(1.5f, 1.5f, size - 3f, size - 3f));
        g.dispose();
        return new ImageIcon(c);
    }

    /** Componente que dibuja una imagen con esquinas redondeadas (ajustada o recortada) al tamaño disponible. */
    public static class ImageView extends JComponent {
        private BufferedImage src, cache;
        private final boolean cover;
        private final int arc;
        private int cw = -1, ch = -1, dx, dy;

        public ImageView(boolean cover, int arc) { this.cover = cover; this.arc = arc; setOpaque(false); }
        public void setImage(String name) { src = load(name); cache = null; repaint(); }

        private void build(int w, int h) {
            int sw = src.getWidth(), sh = src.getHeight();
            BufferedImage scaled;
            if (cover) {
                double s = Math.max((double) w / sw, (double) h / sh);
                int vw = Math.min(sw, (int) Math.round(w / s)), vh = Math.min(sh, (int) Math.round(h / s));
                int vx = (sw - vw) / 2, vy = (int) ((sh - vh) * 0.15);
                scaled = scaleHQ(src.getSubimage(vx, vy, vw, vh), w, h);
                dx = 0; dy = 0;
            } else {
                double s = Math.min((double) w / sw, (double) h / sh);
                int tw = Math.max(1, (int) Math.round(sw * s)), th = Math.max(1, (int) Math.round(sh * s));
                scaled = scaleHQ(src, tw, th);
                dx = (w - tw) / 2; dy = (h - th) / 2;
            }
            cache = mask(scaled, new RoundRectangle2D.Float(0, 0, scaled.getWidth(), scaled.getHeight(), arc, arc));
            Graphics2D g = cache.createGraphics();
            hq(g);
            g.setColor(GOLD); g.setStroke(new BasicStroke(3f));
            g.draw(new RoundRectangle2D.Float(1.5f, 1.5f, scaled.getWidth() - 3f, scaled.getHeight() - 3f, arc, arc));
            g.dispose();
            cw = w; ch = h;
        }

        @Override protected void paintComponent(Graphics g) {
            int w = getWidth(), h = getHeight();
            if (src == null || w < 8 || h < 8) return;
            if (cache == null || cw != w || ch != h) build(w, h);
            g.drawImage(cache, dx, dy, null);
        }
    }

    // ---------- Madera ----------
    public static void paintWood(Graphics2D g, int w, int h) {
        g.drawImage(load("wood.jpg"), 0, 0, w, h, null);
    }

    // ---------- Iconos dibujados ----------
    public static final int ICON_NONE = -1, ICON_MAIL = 0, ICON_LOCK = 1, ICON_USER = 2, ICON_HELP = 3,
            ICON_USER_PLUS = 4, ICON_HEART = 5, ICON_EYE = 6, ICON_EYE_OFF = 7;

    public static void drawIcon(Graphics2D g0, int kind, float x, float y, float s, Color c) {
        Graphics2D g = (Graphics2D) g0.create();
        hq(g);
        g.setColor(c);
        g.setStroke(new BasicStroke(Math.max(1.5f, s * 0.09f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Color light = new Color(252, 236, 206);
        switch (kind) {
            case ICON_MAIL:
                g.fill(new RoundRectangle2D.Float(x, y + s * 0.18f, s, s * 0.64f, s * 0.14f, s * 0.14f));
                g.setColor(light);
                Path2D v = new Path2D.Float();
                v.moveTo(x + s * 0.12f, y + s * 0.3f); v.lineTo(x + s * 0.5f, y + s * 0.58f); v.lineTo(x + s * 0.88f, y + s * 0.3f);
                g.draw(v);
                break;
            case ICON_LOCK:
                g.draw(new Arc2D.Float(x + s * 0.27f, y + s * 0.04f, s * 0.46f, s * 0.7f, 0, 180, Arc2D.OPEN));
                g.fill(new RoundRectangle2D.Float(x + s * 0.14f, y + s * 0.42f, s * 0.72f, s * 0.52f, s * 0.14f, s * 0.14f));
                g.setColor(light);
                g.fill(new Ellipse2D.Float(x + s * 0.43f, y + s * 0.58f, s * 0.14f, s * 0.14f));
                break;
            case ICON_USER:
            case ICON_USER_PLUS: {
                float k = kind == ICON_USER ? 1f : 0.82f;
                float cx = x + s * (kind == ICON_USER ? 0.5f : 0.42f);
                g.draw(new Ellipse2D.Float(cx - s * 0.18f * k, y + s * 0.1f, s * 0.36f * k, s * 0.36f * k));
                g.draw(new Arc2D.Float(cx - s * 0.34f * k, y + s * 0.56f, s * 0.68f * k, s * 0.8f * k, 0, 180, Arc2D.OPEN));
                if (kind == ICON_USER_PLUS) {
                    g.draw(new Line2D.Float(x + s * 0.82f, y + s * 0.5f, x + s * 0.82f, y + s * 0.82f));
                    g.draw(new Line2D.Float(x + s * 0.66f, y + s * 0.66f, x + s, y + s * 0.66f));
                }
                break;
            }
            case ICON_HELP:
                g.fill(new Ellipse2D.Float(x, y, s, s));
                g.setColor(light);
                g.setFont(font(Font.BOLD, Math.round(s * 0.78f)));
                FontMetrics fm = g.getFontMetrics();
                g.drawString("?", x + (s - fm.stringWidth("?")) / 2f, y + (s - fm.getHeight()) / 2f + fm.getAscent());
                break;
            case ICON_HEART:
                g.fill(new Ellipse2D.Float(x, y + s * 0.1f, s * 0.54f, s * 0.54f));
                g.fill(new Ellipse2D.Float(x + s * 0.46f, y + s * 0.1f, s * 0.54f, s * 0.54f));
                g.fill(new Polygon(new int[]{Math.round(x + s * 0.05f), Math.round(x + s * 0.95f), Math.round(x + s * 0.5f)},
                        new int[]{Math.round(y + s * 0.45f), Math.round(y + s * 0.45f), Math.round(y + s * 0.95f)}, 3));
                break;
            case ICON_EYE:
            case ICON_EYE_OFF:
                g.draw(new Ellipse2D.Float(x, y + s * 0.2f, s, s * 0.6f));
                g.fill(new Ellipse2D.Float(x + s * 0.34f, y + s * 0.34f, s * 0.32f, s * 0.32f));
                if (kind == ICON_EYE_OFF) g.draw(new Line2D.Float(x + s * 0.1f, y + s * 0.9f, x + s * 0.9f, y + s * 0.1f));
                break;
            default:
        }
        g.dispose();
    }

    // ---------- Campo de texto en forma de píldora (como en el diseño del login) ----------
    public static class PillField extends JPanel {
        private final JTextField field;
        private final String placeholder;
        private final int icon;
        private final boolean password;
        private boolean reveal = false;

        public PillField(String placeholder, int icon, boolean password) {
            this.placeholder = placeholder; this.icon = icon; this.password = password;
            setOpaque(false); setLayout(null);
            field = password ? new JPasswordField() : new JTextField();
            field.setOpaque(false); field.setBorder(null);
            field.setForeground(TEXT); field.setCaretColor(TEXT);
            if (password) ((JPasswordField) field).setEchoChar('\u2022');
            add(field);
            field.addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { repaint(); }
                @Override public void focusLost(FocusEvent e) { repaint(); }
            });
            field.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { repaint(); }
                public void removeUpdate(DocumentEvent e) { repaint(); }
                public void changedUpdate(DocumentEvent e) { repaint(); }
            });
            if (password) {
                setCursor(Cursor.getDefaultCursor());
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseClicked(MouseEvent e) {
                        if (e.getX() > getWidth() - getHeight() * 0.95) toggleReveal();
                    }
                });
                addMouseMotionListener(new MouseMotionAdapter() {
                    @Override public void mouseMoved(MouseEvent e) {
                        setCursor(e.getX() > getWidth() - getHeight() * 0.95
                                ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
                    }
                });
            }
        }

        private void toggleReveal() {
            reveal = !reveal;
            ((JPasswordField) field).setEchoChar(reveal ? (char) 0 : '\u2022');
            repaint();
        }

        public JTextField field() { return field; }
        public String getText() {
            return password ? new String(((JPasswordField) field).getPassword()) : field.getText();
        }
        public void setText(String t) { field.setText(t); }
        @Override public void setEnabled(boolean b) { super.setEnabled(b); field.setEnabled(b); repaint(); }

        private int leftPad() { return icon >= 0 ? Math.round(getHeight() * 0.98f) : Math.round(getHeight() * 0.38f); }
        private int rightPad() { return password ? Math.round(getHeight() * 0.95f) : Math.round(getHeight() * 0.38f); }

        @Override public void doLayout() {
            int h = getHeight(), w = getWidth();
            field.setFont(font(Font.PLAIN, Math.max(12, Math.round(h * 0.32f))));
            field.setBounds(leftPad(), 0, Math.max(10, w - leftPad() - rightPad()), h);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            hq(g2);
            int w = getWidth(), h = getHeight();
            boolean on = isEnabled();
            Color top = on ? new Color(253, 240, 214) : new Color(226, 210, 186);
            Color bot = on ? new Color(241, 216, 174) : new Color(214, 196, 168);
            g2.setPaint(new GradientPaint(0, 0, top, 0, h, bot));
            g2.fillRoundRect(0, 0, w - 1, h - 1, h, h);
            boolean focus = field.hasFocus();
            g2.setColor(focus ? GOLD : new Color(122, 72, 40));
            g2.setStroke(new BasicStroke(focus ? 3f : 2.2f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, h, h);
            g2.setColor(new Color(255, 250, 235, 140));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(4, 4, w - 9, h - 9, h, h);
            if (icon >= 0) {
                float s = h * 0.44f;
                drawIcon(g2, icon, h * 0.3f, (h - s) / 2f, s, WOOD);
            }
            if (password) {
                float s = h * 0.42f;
                drawIcon(g2, reveal ? ICON_EYE_OFF : ICON_EYE, w - h * 0.72f, (h - s) / 2f, s, WOOD);
            }
            if (field.getText().isEmpty() && placeholder != null) {
                g2.setFont(field.getFont());
                g2.setColor(new Color(150, 100, 64, on ? 230 : 140));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(placeholder, leftPad() + 2, (h - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
        }
    }

    // ---------- Botón estilo madera/crema ----------
    public static final int BTN_PRIMARY = 0, BTN_SECONDARY = 1, BTN_DANGER = 2;

    public static class PillButton extends JButton {
        private final int style, icon;

        public PillButton(String text, int style, int icon) {
            super(text);
            this.style = style; this.icon = icon;
            setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
            setPreferredSize(new Dimension(220, 46));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            hq(g2);
            int w = getWidth(), h = getHeight();
            Color top, bot, line, fg;
            if (style == BTN_PRIMARY) { top = new Color(208, 120, 70); bot = new Color(160, 80, 40); line = new Color(112, 56, 28); fg = new Color(255, 240, 216); }
            else if (style == BTN_DANGER) { top = new Color(224, 96, 80); bot = new Color(184, 52, 46); line = new Color(120, 30, 26); fg = Color.WHITE; }
            else { top = new Color(252, 236, 206); bot = new Color(236, 206, 158); line = new Color(150, 96, 56); fg = WOOD; }
            if (!isEnabled()) { top = blend(top, Color.GRAY, 0.35f); bot = blend(bot, Color.GRAY, 0.35f); fg = blend(fg, Color.GRAY, 0.4f); }
            else if (getModel().isPressed()) { top = blend(top, Color.BLACK, 0.12f); bot = blend(bot, Color.BLACK, 0.12f); }
            else if (getModel().isRollover()) { top = blend(top, Color.WHITE, 0.18f); bot = blend(bot, Color.WHITE, 0.12f); }
            g2.setColor(new Color(0, 0, 0, 50));
            g2.fillRoundRect(1, 3, w - 2, h - 3, h, h);
            g2.setPaint(new GradientPaint(0, 0, top, 0, h, bot));
            g2.fillRoundRect(0, 0, w - 1, h - 3, h, h);
            g2.setColor(line);
            g2.setStroke(new BasicStroke(2.2f));
            g2.drawRoundRect(1, 1, w - 3, h - 6, h, h);
            g2.setFont(font(Font.BOLD, Math.max(11, Math.round(h * 0.34f))));
            FontMetrics fm = g2.getFontMetrics();
            float isz = icon >= 0 ? h * 0.42f : 0, gap = icon >= 0 ? h * 0.22f : 0;
            float total = isz + gap + fm.stringWidth(getText());
            float x = (w - total) / 2f;
            int bh = h - 3;
            if (icon >= 0) { drawIcon(g2, icon, x, (bh - isz) / 2f, isz, fg); x += isz + gap; }
            g2.setColor(fg);
            g2.drawString(getText(), x, (bh - fm.getHeight()) / 2f + fm.getAscent());
            g2.dispose();
        }
    }

    private Theme() { }
}
