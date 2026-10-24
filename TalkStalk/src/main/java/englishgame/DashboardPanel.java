package englishgame;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

/** Pantalla principal: estadísticas y 3 opciones. */
public class DashboardPanel extends Theme.Screen implements MainFrame.Refreshable {
    private final MainFrame frame;
    private final JLabel welcome = new JLabel();
    private final JLabel mastered = new JLabel(), attempts = new JLabel(), percent = new JLabel();
    private final Theme.Bar masteredBar = new Theme.Bar(new Color(236, 214, 178), Theme.GREEN);
    private final Theme.Bar percentBar = new Theme.Bar(new Color(236, 214, 178), Theme.ORANGE);

    public DashboardPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(0, 18));
        setBorder(BorderFactory.createEmptyBorder(20, 28, 28, 28));
        add(header(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 18));
        center.setOpaque(false);
        center.add(stats(), BorderLayout.NORTH);
        center.add(menu(), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private JPanel header() {
        Theme.RoundPanel p = new Theme.RoundPanel(Theme.WOOD, Theme.GOLD, 28);
        p.setLayout(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JLabel avatar = new JLabel(Theme.circle("sing.jpg", 56));
        JLabel title = new JLabel("TalkStalk");
        title.setFont(Theme.serif(Font.BOLD, 26)); title.setForeground(Theme.GOLD);
        welcome.setFont(Theme.font(Font.PLAIN, 13)); welcome.setForeground(Theme.CREAM);
        JPanel txt = new JPanel(); txt.setOpaque(false);
        txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
        txt.add(title); txt.add(welcome);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0)); left.setOpaque(false);
        left.add(avatar); left.add(txt);
        p.add(left, BorderLayout.WEST);

        Theme.RoundButton exit = new Theme.RoundButton("Sign Out", Theme.RED, Color.WHITE);
        exit.addActionListener(e -> frame.logout());
        JPanel right = new JPanel(new GridBagLayout()); right.setOpaque(false);
        right.add(exit);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JPanel stats() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);
        row.add(statCard(mastered, "Mastered Verbs", masteredBar));
        row.add(statCard(attempts, "Total Attempts", null));
        row.add(statCard(percent, "Total Progress", percentBar));
        return row;
    }

    private JPanel statCard(JLabel value, String caption, Theme.Bar bar) {
        Theme.RoundPanel c = new Theme.RoundPanel(Theme.CARD, Theme.TAN, 20);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        value.setFont(Theme.font(Font.BOLD, 26)); value.setForeground(Theme.TEXT);
        JLabel cap = new JLabel(caption);
        cap.setFont(Theme.font(Font.PLAIN, 12)); cap.setForeground(Theme.MUTED);
        c.add(value); c.add(cap);
        if (bar != null) { c.add(Box.createVerticalStrut(8)); c.add(bar); }
        return c;
    }

    private JPanel menu() {
        Theme.RoundPanel card = new Theme.RoundPanel(Theme.CARD, Theme.WOOD_LIGHT, 30);
        card.setLayout(new BorderLayout(0, 24));
        card.setBorder(BorderFactory.createEmptyBorder(26, 28, 28, 28));
        JLabel q = new JLabel("What do you want to do today?", SwingConstants.CENTER);
        q.setFont(Theme.serif(Font.BOLD, 26)); q.setForeground(Theme.WOOD);
        card.add(q, BorderLayout.NORTH);

        JPanel tiles = new JPanel(new GridLayout(1, 3, 20, 0));
        tiles.setOpaque(false);
        tiles.add(tile("FLASH CARDS", "Learn verbs with images", "learn.jpg", true, MainFrame.FLASH));
        tiles.add(tile("SENTENCE BUILDER", "Build sentences with words", "write.jpg", false, MainFrame.SENTENCE));
        tiles.add(tile("MY PROGRESS", "Review your statistics", "run.jpg", false, MainFrame.PROGRESS));
        card.add(tiles, BorderLayout.CENTER);
        return card;
    }

    private JPanel tile(String title, String subtitle, String image, boolean highlight, final String target) {
        final Color base = highlight ? Theme.GREEN : Theme.CREAM;
        final Theme.RoundPanel t = new Theme.RoundPanel(base, highlight ? Theme.GREEN_DARK : Theme.TAN, 26);
        t.setLayout(new BoxLayout(t, BoxLayout.Y_AXIS));
        t.setBorder(BorderFactory.createEmptyBorder(26, 12, 26, 12));
        t.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel ic = new JLabel(Theme.circle(image, 128));
        ic.setAlignmentX(CENTER_ALIGNMENT);
        JLabel l1 = new JLabel(title); l1.setFont(Theme.font(Font.BOLD, 15)); l1.setAlignmentX(CENTER_ALIGNMENT);
        JLabel l2 = new JLabel(subtitle); l2.setFont(Theme.font(Font.PLAIN, 12)); l2.setAlignmentX(CENTER_ALIGNMENT);
        l1.setForeground(highlight ? Color.WHITE : Theme.WOOD);
        l2.setForeground(highlight ? new Color(232, 250, 220) : Theme.MUTED);
        t.add(Box.createVerticalGlue()); t.add(ic); t.add(Box.createVerticalStrut(14));
        t.add(l1); t.add(Box.createVerticalStrut(4)); t.add(l2); t.add(Box.createVerticalGlue());

        t.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { frame.show(target); }
            @Override public void mouseEntered(MouseEvent e) { t.setColors(Theme.blend(base, Color.WHITE, 0.25f), highlight ? Theme.GREEN_DARK : Theme.GOLD); }
            @Override public void mouseExited(MouseEvent e) { t.setColors(base, highlight ? Theme.GREEN_DARK : Theme.TAN); }
        });
        return t;
    }

    @Override public void onShow() {
        Progress p = Progress.get();
        welcome.setText("Welcome, " + p.getPlayerName() + "!");
        mastered.setText(p.masteredCount() + "/" + p.total());
        attempts.setText(String.valueOf(p.attempts()));
        percent.setText(p.percent() + "%");
        masteredBar.setValue((double) p.masteredCount() / p.total());
        percentBar.setValue(p.percent() / 100.0);
    }
}
