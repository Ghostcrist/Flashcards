package englishgame;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.SwingUtilities;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

/**
 * Reconocimiento de voz con el micrófono (librería Vosk, funciona sin internet).
 *
 * El usuario dice la palabra, Vosk la convierte a texto y se calcula un puntaje 0-100 %
 * combinando:
 *   - qué tan parecida es la palabra escuchada a la palabra esperada, y
 *   - la confianza (0-1) que Vosk tiene de haber escuchado esa palabra.
 *
 * El modelo en inglés va en la carpeta "modelo" junto al proyecto (ver README.txt).
 */
public class Pronunciation {

    /** Puntaje mínimo para considerar la pronunciación correcta. */
    public static final int PASS_SCORE = 70;

    private static final float SAMPLE_RATE = 16000f;
    private static final int MAX_RECORD_MS = 4000;      // tiempo máximo de grabación
    private static final int SILENCE_END_MS = 700;      // silencio después de hablar = terminó
    private static final int NO_SPEECH_TIMEOUT_MS = 3000;
    private static final double SPEECH_RMS = 600;       // nivel de volumen que cuenta como voz

    /** Resultado de un intento. */
    public static class Result {
        public final int score;        // 0 - 100
        public final String heard;     // lo que entendió el reconocedor ("" si nada)
        public final boolean passed;
        public final String error;     // null si todo salió bien

        Result(int score, String heard, String error) {
            this.score = score;
            this.heard = heard;
            this.error = error;
            this.passed = error == null && score >= PASS_SCORE;
        }
    }

    // ---------------------------------------------------------------- modelo

    private static volatile Model model;
    private static volatile String loadError;
    private static volatile boolean loading;
    private static volatile boolean recording;

    /** Carga el modelo en segundo plano (tarda 1-2 segundos). Se puede llamar varias veces. */
    public static synchronized void preload() {
        if (model != null || loading || loadError != null) return;
        loading = true;
        Thread t = new Thread(() -> {
            try {
                File dir = findModelDir();
                if (dir == null) {
                    loadError = "No se encontró el modelo de voz.\n\n"
                            + "Descarga \"vosk-model-small-en-us-0.15\" de\n"
                            + "https://alphacephei.com/vosk/models\n"
                            + "y descomprímelo dentro de la carpeta \"modelo\" del proyecto:\n"
                            + new File("modelo").getAbsolutePath();
                    return;
                }
                LibVosk.setLogLevel(LogLevel.WARNINGS);
                model = new Model(dir.getAbsolutePath());
            } catch (Throwable ex) {
                loadError = "No se pudo cargar el reconocimiento de voz:\n" + ex;
            } finally {
                loading = false;
            }
        }, "vosk-load");
        t.setDaemon(true);
        t.start();
    }

    public static boolean isReady() { return model != null; }
    public static boolean isLoading() { return loading; }
    public static String getLoadError() { return loadError; }
    public static boolean isRecording() { return recording; }

    /** Busca la carpeta del modelo: modelo/, modelo/vosk-model-*, o la variable TALKSTALK_MODEL. */
    private static File findModelDir() {
        List<File> candidates = new ArrayList<>();
        String env = System.getenv("TALKSTALK_MODEL");
        if (env != null) candidates.add(new File(env));
        File base = new File("modelo");
        candidates.add(base);
        File[] subs = base.listFiles(File::isDirectory);
        if (subs != null) {
            Arrays.sort(subs);
            candidates.addAll(Arrays.asList(subs));
        }
        for (File f : candidates) {
            if (new File(f, "am").isDirectory() || new File(f, "conf").isDirectory()) return f;
        }
        return null;
    }

    // ---------------------------------------------------------------- grabar y puntuar

    /**
     * Graba al usuario diciendo {@code expected} y devuelve el resultado en el hilo de Swing.
     * La grabación termina sola cuando el usuario deja de hablar.
     */
    public static void listenAndScore(final String expected, final Consumer<Result> onDone) {
        if (recording) return;
        if (model == null) {
            String msg = loadError != null ? loadError : "El reconocimiento de voz todavía se está cargando...";
            SwingUtilities.invokeLater(() -> onDone.accept(new Result(0, "", msg)));
            return;
        }
        recording = true;
        Thread t = new Thread(() -> {
            Result r;
            try {
                r = recordAndScore(expected);
            } catch (Throwable ex) {
                r = new Result(0, "", "No se pudo usar el micrófono:\n" + ex.getMessage());
            } finally {
                recording = false;
            }
            final Result fr = r;
            SwingUtilities.invokeLater(() -> onDone.accept(fr));
        }, "vosk-listen");
        t.setDaemon(true);
        t.start();
    }

    private static Result recordAndScore(String expected) throws Exception {
        AudioFormat fmt = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, fmt);
        if (!AudioSystem.isLineSupported(info)) {
            return new Result(0, "", "No se encontró un micrófono compatible.");
        }
        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
        String json;
        boolean heardVoice = false;
        try (Recognizer rec = new Recognizer(model, SAMPLE_RATE)) {
            rec.setWords(true);
            line.open(fmt);
            line.start();
            byte[] buf = new byte[3200]; // 100 ms de audio
            long start = System.currentTimeMillis();
            long lastVoice = 0;
            StringBuilder partials = new StringBuilder();
            while (true) {
                int n = line.read(buf, 0, buf.length);
                if (n <= 0) break;
                long now = System.currentTimeMillis();
                if (rms(buf, n) > SPEECH_RMS) { heardVoice = true; lastVoice = now; }
                if (rec.acceptWaveForm(buf, n)) partials.append(rec.getResult());
                long elapsed = now - start;
                if (elapsed > MAX_RECORD_MS) break;
                if (heardVoice && now - lastVoice > SILENCE_END_MS) break;
                if (!heardVoice && elapsed > NO_SPEECH_TIMEOUT_MS) break;
            }
            line.stop();
            json = partials + rec.getFinalResult();
        } finally {
            line.close();
        }
        if (!heardVoice) {
            return new Result(0, "", "No escuchamos nada. Habla más cerca del micrófono.");
        }
        return score(expected, json);
    }

    /** Volumen promedio del bloque de audio (16 bits, little endian). */
    private static double rms(byte[] b, int len) {
        long sum = 0;
        int samples = len / 2;
        for (int i = 0; i + 1 < len; i += 2) {
            int s = (short) ((b[i] & 0xff) | (b[i + 1] << 8));
            sum += (long) s * s;
        }
        return samples == 0 ? 0 : Math.sqrt((double) sum / samples);
    }

    // ---------------------------------------------------------------- cálculo del puntaje

    private static final Pattern WORD_OBJ = Pattern.compile("\\{[^{}]*\"word\"[^{}]*\\}");
    private static final Pattern CONF = Pattern.compile("\"conf\"\\s*:\\s*([0-9.eE+-]+)");
    private static final Pattern WORD = Pattern.compile("\"word\"\\s*:\\s*\"([^\"]*)\"");

    /** Palabras que suenan igual (el reconocedor puede escribir cualquiera). */
    private static final Map<String, Set<String>> HOMOPHONES = new HashMap<>();
    static {
        homophones("read", "red", "reed");
        homophones("write", "right", "rite", "wright");
    }
    private static void homophones(String w, String... alts) {
        HOMOPHONES.put(w, new HashSet<>(Arrays.asList(alts)));
    }

    /**
     * Puntaje = parecido (0-1) x (0.5 + 0.5 x confianza) x 100.
     * Ejemplos: dijo "run" con confianza alta -> ~100 %. Dijo "ron" -> ~60 %. Dijo "it" en vez de "eat" -> ~30 %.
     */
    static Result score(String expected, String json) {
        String target = normalize(expected);
        List<String> words = new ArrayList<>();
        List<Double> confs = new ArrayList<>();
        Matcher m = WORD_OBJ.matcher(json);
        while (m.find()) {
            String obj = m.group();
            Matcher w = WORD.matcher(obj), c = CONF.matcher(obj);
            if (!w.find()) continue;
            words.add(normalize(w.group(1)));
            confs.add(c.find() ? Double.parseDouble(c.group(1)) : 1.0);
        }
        if (words.isEmpty()) return new Result(0, "", null);

        double best = 0;
        for (int i = 0; i < words.size(); i++) {
            double sim = similarity(words.get(i), target);
            double conf = Math.max(0, Math.min(1, confs.get(i)));
            double s = sim * (0.5 + 0.5 * conf);
            if (s > best) best = s;
        }
        int pct = (int) Math.round(best * 100);
        return new Result(Math.max(0, Math.min(100, pct)), String.join(" ", words), null);
    }

    private static double similarity(String heard, String target) {
        if (heard.equals(target)) return 1.0;
        Set<String> h = HOMOPHONES.get(target);
        if (h != null && h.contains(heard)) return 1.0;
        // "plays", "played", "playing" -> casi correcto
        if (heard.startsWith(target) && heard.length() - target.length() <= 3) return 0.9;
        int d = levenshtein(heard, target);
        return Math.max(0, 1.0 - (double) d / Math.max(heard.length(), target.length()));
    }

    private static String normalize(String s) {
        return s.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
    }

    private static int levenshtein(String a, String b) {
        int[] prev = new int[b.length() + 1], cur = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) prev[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            cur[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                cur[j] = Math.min(Math.min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] t = prev; prev = cur; cur = t;
        }
        return prev[b.length()];
    }

    private Pronunciation() { }
}
