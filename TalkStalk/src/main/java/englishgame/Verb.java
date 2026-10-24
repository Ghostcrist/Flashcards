package englishgame;

/** Un verbo del juego: palabra en inglés, traducción, oración de ejemplo e imagen. */
public class Verb {
    public final String en;        // "play"
    public final String es;        // "jugar"
    public final String sentence;  // "She plays soccer near the ice cream shop"

    public Verb(String en, String es, String sentence) {
        this.en = en;
        this.es = es;
        this.sentence = sentence;
    }

    /** La imagen se busca en /englishgame/images/<verbo>.jpg */
    public String imageName() {
        return en + ".jpg";
    }
}
