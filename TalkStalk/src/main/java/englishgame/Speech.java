package englishgame;

import java.awt.Toolkit;

/** Pronunciación usando el sintetizador de voz del sistema operativo (sin librerías externas). */
public class Speech {
    public static void speak(final String text) {
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    String os = System.getProperty("os.name").toLowerCase();
                    String safe = text.replace("'", "").replace("\"", "");
                    ProcessBuilder pb;
                    if (os.contains("win")) {
                        pb = new ProcessBuilder("powershell", "-NoProfile", "-Command",
                            "Add-Type -AssemblyName System.Speech; "
                            + "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; "
                            + "$s.Rate = -1; $s.Speak('" + safe + "')");
                    } else if (os.contains("mac")) {
                        pb = new ProcessBuilder("say", "-v", "Samantha", safe);
                    } else {
                        pb = new ProcessBuilder("espeak", "-v", "en-us", safe);
                    }
                    pb.start().waitFor();
                } catch (Exception ex) {
                    Toolkit.getDefaultToolkit().beep(); // si no hay voz disponible
                }
            }
        }, "speech").start();
    }

    private Speech() { }
}
