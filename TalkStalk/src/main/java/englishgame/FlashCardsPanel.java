package englishgame;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;

/**
 * Muestra una imagen; el jugador escribe el verbo en inglés y después lo pronuncia con el micrófono.
 * Solo avanza a la siguiente tarjeta si la pronunciación saca {@link Pronunciation#PASS_SCORE}% o más.
 */
public class FlashCardsPanel extends Theme.Screen implements MainFrame.Refreshable {
    private final MainFrame frame;
    private List<Verb> deck = new ArrayList<>();
    private int index;
    private boolean solved;      // escribió bien el verbo
    private boolean pronounced;  // lo pronunció bien

    private final JLabel counter = new JLabel("", SwingConstants.CENTER);
    private final Theme.ImageView image = new Theme.ImageView(false, 30);
    private final Theme.PillField inputBox = new Theme.PillField("Type the verb here...", Theme.ICON_NONE, false);
    private final JTextField input = inputBox.field();
    private final Theme.RoundPanel feedback = new Theme.RoundPanel(Theme.OK_BG, Theme.OK_LINE, 14);
    private final JLabel fbTitle = new JLabel(), fbMsg = new JLabel();
    private final Theme.RoundButton listen = new Theme.RoundButton("LISTEN", Theme.WOOD, Theme.CREAM);
    private final Theme.RoundButton verify = new Theme.RoundButton("VERIFY", Theme.GREEN, Color.WHITE);
    private final Theme.RoundButton speak = new Theme.RoundButton("SPEAK", Theme.RED, Color.WHITE);
    private final Theme.RoundButton next = new Theme.RoundButton("NEXT  >", Theme.ORANGE, Color.WHITE);
    private final Theme.Bar bar = new Theme.Bar(new Color(236, 214, 178), Theme.GREEN);

    // Puntaje de pronunciación
    private final Theme.RoundPanel scoreBox = new Theme.RoundPanel(new Color(252, 240, 218), Theme.TAN, 14);
    private final JLabel scoreTitle = new JLabel("Pronunciation");
    private final JLabel scoreValue = new JLabel("--%");
    private final JLabel scoreMsg = new JLabel(" ");
    private final Theme.Bar scoreBar = new Theme.Bar(new Color(236, 214, 178), Theme.GREEN);
    private final Timer autoNext = new Timer(1800, e -> goNext());

    public FlashCardsPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(18, 28, 22, 28));
        autoNext.setRepeats(false);

        // Barra superior
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        Theme.RoundButton home = new Theme.RoundButton("< HOME", Theme.WOOD, Theme.CREAM);
        home.addActionListener(e -> { autoNext.stop(); frame.show(MainFrame.DASHBOARD); });
        counter.setFont(Theme.font(Font.BOLD, 13)); counter.setForeground(Theme.WOOD);
        top.add(home, BorderLayout.WEST); top.add(counter, BorderLayout.CENTER);
        top.add(Box.createRigidArea(new Dimension(home.getPreferredSize().width, 1)), BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Tarjeta blanca
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

        listen.addActionListener(e -> Speech.speak(deck.get(index).en));
        verify.addActionListener(e -> check());
        speak.addActionListener(e -> startSpeaking());
        input.addActionListener(e -> {
            if (!solved) check();
            else if (!pronounced && speechAvailable()) startSpeaking();
            else goNext();
        });
        next.addActionListener(e -> goNext());
    }

    private JPanel form() {
        JPanel f = new JPanel();
        f.setOpaque(false);
        f.setLayout(new BoxLayout(f, BoxLayout.Y_AXIS));

        JLabel q = new JLabel("Write the verb in English, then say it:");
        q.setFont(Theme.font(Font.BOLD, 15)); q.setForeground(Theme.WOOD);
        q.setAlignmentX(LEFT_ALIGNMENT);

        inputBox.setPreferredSize(new Dimension(300, 54));
        inputBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        inputBox.setAlignmentX(LEFT_ALIGNMENT);

        feedback.setLayout(new BoxLayout(feedback, BoxLayout.Y_AXIS));
        feedback.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        fbTitle.setFont(Theme.font(Font.BOLD, 13)); fbMsg.setFont(Theme.font(Font.PLAIN, 12));
        feedback.add(fbTitle); feedback.add(fbMsg);
        feedback.setAlignmentX(LEFT_ALIGNMENT);
        feedback.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        feedback.setVisible(false);

        // Caja del puntaje: "Pronunciation   85%" + barra + mensaje
        scoreBox.setLayout(new BorderLayout(0, 6));
        scoreBox.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        scoreTitle.setFont(Theme.font(Font.BOLD, 13)); scoreTitle.setForeground(Theme.WOOD);
        scoreValue.setFont(Theme.font(Font.BOLD, 26)); scoreValue.setForeground(Theme.WOOD);
        scoreMsg.setFont(Theme.font(Font.PLAIN, 12)); scoreMsg.setForeground(Theme.MUTED);
        JPanel scoreTop = new JPanel(new BorderLayout()); scoreTop.setOpaque(false);
        scoreTop.add(scoreTitle, BorderLayout.WEST); scoreTop.add(scoreValue, BorderLayout.EAST);
        scoreBar.setPreferredSize(new Dimension(100, 12));
        scoreBox.add(scoreTop, BorderLayout.NORTH);
        scoreBox.add(scoreBar, BorderLayout.CENTER);
        scoreBox.add(scoreMsg, BorderLayout.SOUTH);
        scoreBox.setAlignmentX(LEFT_ALIGNMENT);
        scoreBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        scoreBox.setVisible(false);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false); buttons.setAlignmentX(LEFT_ALIGNMENT);
        buttons.add(listen); buttons.add(verify); buttons.add(speak); buttons.add(next);

        f.add(Box.createVerticalGlue());
        f.add(q); f.add(Box.createVerticalStrut(10));
        f.add(inputBox); f.add(Box.createVerticalStrut(14));
        f.add(feedback); f.add(Box.createVerticalStrut(10));
        f.add(scoreBox);
        f.add(Box.createVerticalGlue());
        f.add(buttons);
        return f;
    }

    @Override public void onShow() {
        Pronunciation.preload();
        autoNext.stop();
        deck = new ArrayList<>(VerbData.ALL);
        Collections.shuffle(deck);
        index = 0;
        loadCard();
    }

    /** Hay micrófono/modelo, o todavía se está cargando. Si falló, el juego sigue sin voz. */
    private boolean speechAvailable() {
        return Pronunciation.getLoadError() == null;
    }

    private void loadCard() {
        autoNext.stop();
        Verb v = deck.get(index);
        solved = false;
        pronounced = false;
        counter.setText("Card " + (index + 1) + " of " + deck.size());
        image.setImage(v.imageName());
        input.setText(""); input.setEnabled(true); input.setEditable(true); inputBox.repaint();
        feedback.setVisible(false);
        scoreBox.setVisible(false);
        verify.setEnabled(true);
        speak.setText("SPEAK"); speak.setEnabled(false);
        next.setEnabled(!speechAvailable()); // con voz, solo avanza si pronuncia bien
        next.setText(index == deck.size() - 1 ? "FINISH  >" : "NEXT  >");
        bar.setValue((double) index / deck.size());
        revalidate(); repaint();
        input.requestFocusInWindow();
    }

    private void check() {
        if (solved) return;
        Verb v = deck.get(index);
        String answer = input.getText().trim().toLowerCase();
        if (answer.isEmpty()) return;
        boolean ok = answer.equals(v.en);
        Progress.get().registerAttempt(ok);
        if (ok) {
            solved = true;
            Progress.get().markMastered(v.en);
            verify.setEnabled(false); input.setEditable(false);
            if (speechAvailable()) {
                showFeedback(true, "Correct! Now say it out loud.",
                        "Press SPEAK and say: \"" + v.en + "\"  (LISTEN to hear it first)");
                speak.setEnabled(true);
                speak.requestFocusInWindow();
            } else {
                showFeedback(true, "Correct!", "The answer is: " + v.en + "   (microphone not available)");
                next.setEnabled(true);
            }
            Speech.speak(v.en);
        } else {
            showFeedback(false, "Not quite, try again!",
                    "Hint: starts with \"" + v.en.charAt(0) + "\"  (" + v.es + ")");
            input.selectAll();
        }
    }

    // ---------------------------------------------------------------- micrófono

    private void startSpeaking() {
        if (!solved || pronounced || Pronunciation.isRecording()) return;
        if (!Pronunciation.isReady()) {
            String err = Pronunciation.getLoadError();
            if (err != null) {
                JOptionPane.showMessageDialog(this, err, "Microphone", JOptionPane.WARNING_MESSAGE);
                speak.setEnabled(false);
                next.setEnabled(true); // sin voz no bloqueamos el juego
            } else {
                showScoreMessage("Loading the voice engine, try again in a second...");
            }
            return;
        }
        final Verb v = deck.get(index);
        speak.setText("LISTENING..."); speak.setEnabled(false);
        listen.setEnabled(false);
        showScoreMessage("Speak now: \"" + v.en + "\"");
        Pronunciation.listenAndScore(v.en, r -> onScore(v, r));
    }

    private void onScore(Verb v, Pronunciation.Result r) {
        // Si el jugador cambió de tarjeta mientras grababa, ignoramos el resultado
        if (deck.isEmpty() || deck.get(index) != v || !solved) return;
        listen.setEnabled(true);
        speak.setText("SPEAK");
        if (r.error != null) {
            speak.setEnabled(true);
            showScoreMessage(r.error.replace("\n", " "));
            return;
        }
        Progress.get().registerPronunciation(v.en, r.score);
        scoreValue.setText(r.score + "%");
        scoreBar.setValue(r.score / 100.0);
        Color c = r.score >= Pronunciation.PASS_SCORE ? Theme.GREEN
                : r.score >= 40 ? Theme.ORANGE : Theme.RED;
        scoreBar.setFill(c);
        scoreValue.setForeground(c.darker());
        String heard = r.heard.isEmpty() ? "nothing clear" : "\"" + r.heard + "\"";
        if (r.passed) {
            pronounced = true;
            scoreBox.setColors(Theme.OK_BG, Theme.OK_LINE);
            scoreMsg.setForeground(Theme.OK_TEXT);
            scoreMsg.setText(label(r.score) + "  We heard " + heard + ".");
            next.setEnabled(true);
            autoNext.restart(); // pasa sola a la siguiente tarjeta
        } else {
            scoreBox.setColors(Theme.BAD_BG, Theme.BAD_LINE);
            scoreMsg.setForeground(Theme.BAD_TEXT);
            scoreMsg.setText("We heard " + heard + ". You need " + Pronunciation.PASS_SCORE
                    + "%. Press LISTEN and try again!");
            speak.setEnabled(true);
        }
        scoreBox.setVisible(true);
        revalidate(); repaint();
    }

    private static String label(int score) {
        if (score >= 95) return "Perfect!";
        if (score >= 85) return "Excellent!";
        return "Good job!";
    }

    private void showScoreMessage(String msg) {
        scoreBox.setColors(new Color(252, 240, 218), Theme.TAN);
        scoreValue.setText("--%"); scoreValue.setForeground(Theme.WOOD);
        scoreBar.setValue(0);
        scoreMsg.setForeground(Theme.MUTED);
        scoreMsg.setText(msg);
        scoreBox.setVisible(true);
        revalidate(); repaint();
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
        autoNext.stop();
        if (!next.isEnabled()) return; // todavía no pronunció bien
        if (index < deck.size() - 1) {
            index++;
            loadCard();
        } else {
            bar.setValue(1);
            Progress p = Progress.get();
            int avg = p.pronunciationAverage();
            JOptionPane.showMessageDialog(this,
                    "Great job! You mastered " + p.masteredCount() + " of " + p.total() + " verbs."
                    + (avg >= 0 ? "\nAverage pronunciation: " + avg + "%" : ""),
                    "Finished", JOptionPane.INFORMATION_MESSAGE);
            frame.show(MainFrame.DASHBOARD);
        }
    }
}
