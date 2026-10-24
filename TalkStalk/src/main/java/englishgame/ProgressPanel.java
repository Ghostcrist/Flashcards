package englishgame;

import java.awt.*;
import javax.swing.*;

/** Estadísticas y estado de cada verbo. */
public class ProgressPanel extends Theme.Screen implements MainFrame.Refreshable {
    private final JLabel summary = new JLabel();
    private final JPanel grid = new JPanel(new GridLayout(3, 5, 12, 12));

    public ProgressPanel(final MainFrame frame) {
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(18, 28, 22, 28));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        Theme.RoundButton home = new Theme.RoundButton("< HOME", Theme.WOOD, Theme.CREAM);
        home.addActionListener(e -> frame.show(MainFrame.DASHBOARD));
        JLabel title = new JLabel("My Progress", SwingConstants.CENTER);
        title.setFont(Theme.font(Font.BOLD, 18)); title.setForeground(Theme.WOOD);
        Theme.RoundButton reset = new Theme.RoundButton("RESET", Theme.RED, Color.WHITE);
        reset.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Delete all your progress?", "Reset",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                Progress.get().reset();
                onShow();
            }
        });
        top.add(home, BorderLayout.WEST); top.add(title, BorderLayout.CENTER); top.add(reset, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        Theme.RoundPanel card = new Theme.RoundPanel(Theme.CARD, Theme.WOOD_LIGHT, 30);
        card.setLayout(new BorderLayout(0, 16));
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        summary.setFont(Theme.font(Font.BOLD, 15)); summary.setForeground(Theme.WOOD);
        summary.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(summary, BorderLayout.NORTH);
        grid.setOpaque(false);
        card.add(grid, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
    }

    @Override public void onShow() {
        Progress p = Progress.get();
        summary.setText("Mastered " + p.masteredCount() + "/" + p.total() + " verbs  |  Sentences "
                + p.sentenceCount() + "/" + p.total() + "  |  Accuracy " + p.accuracy() + "%  |  Attempts " + p.attempts()
                + (p.pronunciationAverage() >= 0 ? "  |  Pronunciation " + p.pronunciationAverage() + "%" : ""));
        grid.removeAll();
        for (Verb v : VerbData.ALL) {
            boolean done = p.isMastered(v.en);
            Theme.RoundPanel c = new Theme.RoundPanel(done ? Theme.OK_BG : new Color(252, 240, 218),
                    done ? Theme.OK_LINE : Theme.TAN, 18);
            c.setLayout(new BorderLayout());
            c.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
            Theme.ImageView img = new Theme.ImageView(true, 14);
            img.setImage(v.imageName());
            int pr = p.pronunciation(v.en);
            JLabel name = new JLabel(v.en + (done ? "  \u2713" : "") + (pr >= 0 ? "  " + pr + "%" : ""),
                    SwingConstants.CENTER);
            name.setFont(Theme.font(Font.BOLD, 12));
            name.setForeground(done ? Theme.OK_TEXT : Theme.MUTED);
            c.add(img, BorderLayout.CENTER); c.add(name, BorderLayout.SOUTH);
            grid.add(c);
        }
        grid.revalidate(); grid.repaint();
    }
}
