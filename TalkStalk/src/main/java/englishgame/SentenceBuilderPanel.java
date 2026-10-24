package englishgame;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;

/** Ordena las palabras (clic para mover) y forma la oración correcta. */
public class SentenceBuilderPanel extends Theme.Screen implements MainFrame.Refreshable {
    private final MainFrame frame;
    private List<Verb> deck = new ArrayList<>();
    private int index;
    private boolean solved;
    private final List<String> bank = new ArrayList<>();
    private final List<String> answer = new ArrayList<>();

    private final JLabel counter = new JLabel("", SwingConstants.CENTER);
    private final Theme.ImageView image = new Theme.ImageView(false, 30);
    private final JPanel answerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
    private final JPanel bankPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
    private final Theme.RoundPanel feedback = new Theme.RoundPanel(Theme.OK_BG, Theme.OK_LINE, 14);
    private final JLabel fbTitle = new JLabel(), fbMsg = new JLabel();
    private final Theme.RoundButton listen = new Theme.RoundButton("LISTEN", Theme.WOOD, Theme.CREAM);
    private final Theme.RoundButton verify = new Theme.RoundButton("VERIFY", Theme.GREEN, Color.WHITE);
    private final Theme.RoundButton next = new Theme.RoundButton("NEXT  >", Theme.ORANGE, Color.WHITE);
    private final Theme.Bar bar = new Theme.Bar(new Color(236, 214, 178), Theme.GREEN);

    public SentenceBuilderPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(18, 28, 22, 28));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        Theme.RoundButton home = new Theme.RoundButton("< HOME", Theme.WOOD, Theme.CREAM);
        home.addActionListener(e -> frame.show(MainFrame.DASHBOARD));
        counter.setFont(Theme.font(Font.BOLD, 13)); counter.setForeground(Theme.WOOD);
        top.add(home, BorderLayout.WEST); top.add(counter, BorderLayout.CENTER);
        top.add(Box.createRigidArea(new Dimension(home.getPreferredSize().width, 1)), BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        Theme.RoundPanel card = new Theme.RoundPanel(Theme.CARD, Theme.WOOD_LIGHT, 30);
        card.setLayout(new GridLayout(1, 2, 30, 0));
        card.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        card.add(image);
        card.add(form());
        JPanel wrap = new JPanel(new GridBagLayout()); wrap.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH; gc.weightx = 1; gc.weighty = 1;
        wrap.add(card, gc);
        add(wrap, BorderLayout.CENTER);
        add(bar, BorderLayout.SOUTH);

        listen.addActionListener(e -> Speech.speak(deck.get(index).sentence));
        verify.addActionListener(e -> check());
        next.addActionListener(e -> goNext());
    }

    private JPanel form() {
        JPanel f = new JPanel();
        f.setOpaque(false);
        f.setLayout(new BoxLayout(f, BoxLayout.Y_AXIS));

        JLabel q = new JLabel("Build the sentence:");
        q.setFont(Theme.font(Font.BOLD, 15)); q.setForeground(Theme.WOOD);
        q.setAlignmentX(LEFT_ALIGNMENT);

        answerPanel.setBackground(new Color(255, 246, 228));
        answerPanel.setBorder(BorderFactory.createLineBorder(Theme.TAN, 2, true));
        answerPanel.setAlignmentX(LEFT_ALIGNMENT);
        answerPanel.setPreferredSize(new Dimension(300, 110));
        answerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        JLabel hint = new JLabel("Click the words to order them:");
        hint.setFont(Theme.font(Font.PLAIN, 12)); hint.setForeground(Theme.MUTED);
        hint.setAlignmentX(LEFT_ALIGNMENT);

        bankPanel.setOpaque(false);
        bankPanel.setAlignmentX(LEFT_ALIGNMENT);
        bankPanel.setPreferredSize(new Dimension(300, 110));
        bankPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        feedback.setLayout(new BoxLayout(feedback, BoxLayout.Y_AXIS));
        feedback.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        fbTitle.setFont(Theme.font(Font.BOLD, 13)); fbMsg.setFont(Theme.font(Font.PLAIN, 12));
        feedback.add(fbTitle); feedback.add(fbMsg);
        feedback.setAlignmentX(LEFT_ALIGNMENT);
        feedback.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        feedback.setVisible(false);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false); buttons.setAlignmentX(LEFT_ALIGNMENT);
        buttons.add(listen); buttons.add(verify); buttons.add(next);

        f.add(q); f.add(Box.createVerticalStrut(8));
        f.add(answerPanel); f.add(Box.createVerticalStrut(10));
        f.add(hint); f.add(bankPanel); f.add(Box.createVerticalStrut(6));
        f.add(feedback); f.add(Box.createVerticalGlue());
        f.add(buttons);
        return f;
    }

    @Override public void onShow() {
        deck = new ArrayList<>(VerbData.ALL);
        Collections.shuffle(deck);
        index = 0;
        loadCard();
    }

    private void loadCard() {
        Verb v = deck.get(index);
        solved = false;
        counter.setText("Sentence " + (index + 1) + " of " + deck.size());
        image.setImage(v.imageName());
        bank.clear(); answer.clear();
        Collections.addAll(bank, v.sentence.split(" "));
        Collections.shuffle(bank);
        feedback.setVisible(false);
        verify.setEnabled(true);
        next.setText(index == deck.size() - 1 ? "FINISH  >" : "NEXT  >");
        bar.setValue((double) index / deck.size());
        refreshChips();
    }

    private void refreshChips() {
        answerPanel.removeAll(); bankPanel.removeAll();
        for (int i = 0; i < answer.size(); i++) {
            final int pos = i;
            Theme.RoundButton chip = new Theme.RoundButton(answer.get(i), Theme.GREEN, Color.WHITE);
            chip.addActionListener(e -> { if (!solved) { bank.add(answer.remove(pos)); refreshChips(); } });
            answerPanel.add(chip);
        }
        for (int i = 0; i < bank.size(); i++) {
            final int pos = i;
            Theme.RoundButton chip = new Theme.RoundButton(bank.get(i), Theme.WOOD_LIGHT, Theme.CREAM);
            chip.addActionListener(e -> { if (!solved) { answer.add(bank.remove(pos)); refreshChips(); } });
            bankPanel.add(chip);
        }
        revalidate(); repaint();
    }

    private void check() {
        if (solved || !bank.isEmpty()) {
            if (!solved) showFeedback(false, "Use all the words first!", "Move every word to the answer box.");
            return;
        }
        Verb v = deck.get(index);
        boolean ok = String.join(" ", answer).equals(v.sentence);
        Progress.get().registerAttempt(ok);
        if (ok) {
            solved = true;
            Progress.get().markSentence(v.en);
            verify.setEnabled(false);
            showFeedback(true, "Correct!", v.sentence);
            Speech.speak(v.sentence);
        } else {
            showFeedback(false, "Not quite, try again!", "Click a word to take it out and reorder.");
        }
    }

    private void showFeedback(boolean ok, String title, String msg) {
        feedback.setColors(ok ? Theme.OK_BG : Theme.BAD_BG, ok ? Theme.OK_LINE : Theme.BAD_LINE);
        Color t = ok ? Theme.OK_TEXT : Theme.BAD_TEXT;
        fbTitle.setForeground(t); fbMsg.setForeground(t);
        fbTitle.setText(title); fbMsg.setText(msg);
        feedback.setVisible(true);
        revalidate(); repaint();
    }

    private void goNext() {
        if (index < deck.size() - 1) {
            index++;
            loadCard();
        } else {
            bar.setValue(1);
            JOptionPane.showMessageDialog(this,
                    "Well done! You built " + Progress.get().sentenceCount() + " sentences correctly.",
                    "Finished", JOptionPane.INFORMATION_MESSAGE);
            frame.show(MainFrame.DASHBOARD);
        }
    }
}
