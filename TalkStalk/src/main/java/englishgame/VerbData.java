package englishgame;

import java.util.ArrayList;
import java.util.List;

/** Lista de verbos (una imagen por verbo). Para agregar uno: añade una línea y su imagen en images/<verbo>.jpg */
public class VerbData {
    public static final List<Verb> ALL = new ArrayList<>();

    static {
        ALL.add(new Verb("play",     "jugar",     "She plays soccer near the ice cream shop"));
        ALL.add(new Verb("eat",      "comer",     "She eats a pink ice cream"));
        ALL.add(new Verb("read",     "leer",      "She reads a book in the cafe"));
        ALL.add(new Verb("sing",     "cantar",    "He sings a happy song"));
        ALL.add(new Verb("write",    "escribir",  "She writes in her notebook"));
        ALL.add(new Verb("sleep",    "dormir",    "He sleeps at the table"));
        ALL.add(new Verb("run",      "correr",    "He runs very fast"));
        ALL.add(new Verb("talk",     "hablar",    "She talks with her friend"));
        ALL.add(new Verb("dance",    "bailar",    "She dances to the music"));
        ALL.add(new Verb("listen",   "escuchar",  "He listens to music"));
        ALL.add(new Verb("ask",      "preguntar", "She asks a question"));
        ALL.add(new Verb("answer",   "responder", "She answers the question in class"));
        ALL.add(new Verb("learn",    "aprender",  "I learn new words every day"));
        ALL.add(new Verb("teach",    "enseñar",   "The teacher teaches the class"));
        ALL.add(new Verb("practice", "practicar", "She practices the guitar"));
    }

    private VerbData() { }
}
