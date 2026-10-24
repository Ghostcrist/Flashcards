package englishgame;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Guarda el progreso del jugador (en memoria). */
public class Progress {
    private static final Progress INSTANCE = new Progress();
    public static Progress get() { return INSTANCE; }

    private final Set<String> mastered = new HashSet<>();
    private final Set<String> sentencesDone = new HashSet<>();
    private final Map<String, Integer> bestPronunciation = new HashMap<>();
    private int attempts = 0;
    private int correct = 0;
    private String playerName = "Player";

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String n) { if (n != null && !n.trim().isEmpty()) playerName = n.trim(); }

    public void registerAttempt(boolean ok) { attempts++; if (ok) correct++; }
    public void markMastered(String verb) { mastered.add(verb); }
    public void markSentence(String verb) { sentencesDone.add(verb); }

    /** Guarda el mejor puntaje de pronunciación (0-100) de cada verbo. */
    public void registerPronunciation(String verb, int score) {
        Integer old = bestPronunciation.get(verb);
        if (old == null || score > old) bestPronunciation.put(verb, score);
    }
    /** Mejor puntaje del verbo, o -1 si nunca lo pronunció. */
    public int pronunciation(String verb) {
        Integer v = bestPronunciation.get(verb);
        return v == null ? -1 : v;
    }
    /** Promedio de los mejores puntajes de pronunciación, o -1 si no hay ninguno. */
    public int pronunciationAverage() {
        if (bestPronunciation.isEmpty()) return -1;
        int sum = 0;
        for (int v : bestPronunciation.values()) sum += v;
        return Math.round((float) sum / bestPronunciation.size());
    }

    public boolean isMastered(String verb) { return mastered.contains(verb); }
    public boolean isSentenceDone(String verb) { return sentencesDone.contains(verb); }
    public int masteredCount() { return mastered.size(); }
    public int sentenceCount() { return sentencesDone.size(); }
    public int attempts() { return attempts; }
    public int correct() { return correct; }
    public int total() { return VerbData.ALL.size(); }
    public int percent() { return total() == 0 ? 0 : Math.round(100f * mastered.size() / total()); }
    public int accuracy() { return attempts == 0 ? 0 : Math.round(100f * correct / attempts); }

    public void reset() { mastered.clear(); sentencesDone.clear(); bestPronunciation.clear(); attempts = 0; correct = 0; }
}
