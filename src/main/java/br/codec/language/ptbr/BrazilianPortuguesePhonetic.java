package br.codec.language.ptbr;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;

/**
 * A phonetic matching key for Brazilian Portuguese, in the family of the {@code org.apache.commons.codec.language}
 * encoders ({@code Metaphone}, {@code Nysiis}, {@code Caverphone}...): two spellings that sound the same in PB get
 * the same key. "Luiz" and "Luís" are both {@code LUIS}, "Souza" and "Sousa" {@code SOZA}, "Thiago" and "Tiago"
 * {@code TIAGU}, "Ruth" and "Rute" {@code HUTI}; "caro" ({@code KARU}) and "carro" ({@code KAHU}) differ, as do
 * "soco" and "choco", "pala" and "palha".
 *
 * <p>The key is built from the phonological transcription of {@link BrazilianPortugueseTranscriber} by keeping
 * only what distinguishes words in PB (the phonemes) and dropping what does not (allophones, regional
 * variation), following Seara, Nunes &amp; Lazzarotto-Volcão, <i>Fonética e Fonologia do Português Brasileiro</i>
 * (UFSC, 2011):
 * <ul>
 *   <li>neutralizations become one token: the archiphonemes /S/ (s, z, x in coda: Luiz = Luís), /R/, /N/
 *       (m or n in coda: Campos = Canpos), /E/ (e = ɛ), /O/ (o = ɔ) — section 4.4;</li>
 *   <li>final unstressed e and o are I and U (Regra II: júri = jure);</li>
 *   <li>coda l is vocalized: U (Brasil = Brasiu; section 5.2, the majority pronunciation);</li>
 *   <li>allophones are ignored: palatalized t/d, nasalized vowels before nasal consonants, the many
 *       realizations of "r" forte (token H) and of /S/, /R/;</li>
 *   <li>an illicit coda consonant gets its epenthetic I (Edson = Edison, Ruth = Rute);</li>
 *   <li>optionally, ou and ei/ai before r, x, j are monophthongized (Moura = Mora, Ribeiro = Ribero) and
 *       e/o merge with i/u (Felipe = Filipe, the pretonic raising of Regra VIII).</li>
 * </ul>
 *
 * <p>A nasal vowel is written as its vowel followed by N, after its glide if it has one ("canta" {@code KANTA},
 * "mão" {@code MAUN}, "Belém" {@code BELEIN}): an N followed by a vowel is the consonant n, any other N is nasality.
 * Other tokens: X = ʃ, J = ʒ, Y = ɲ (nh), LH = ʎ, R = ɾ or /R/, H = "r" forte. Consecutive equal tokens are
 * collapsed (Isaac = Isac). Each word gets its key; keys are joined by a space. Prepositions and the conjunction
 * "e" of names (de, da, do, das, dos, e) are dropped unless {@link Builder#keepParticles()} is set.
 *
 * <p>Like {@code DoubleMetaphone}, a text has a primary key ({@link #encode(String)}) and an {@link #alternate
 * alternate} one, for the spellings of records: in the alternate, ç is K, like c before a, o, u (the cedilla is
 * often lost: Goncalves), w is V (Wagner = Vagner), and a final unaccented -ez is read as the Spanish surname
 * ending, like -es (Rodriguez = Rodrigues; the primary key reads it as stressed, like rapidez). Two texts match ({@link #isEncodeEqual}) when they share a
 * key, so "Gonçalves" ({@code GONSAUVIS} / {@code GONKAUVIS}) matches both "Gonsalves" and "Goncalves", and
 * "Wilson" ({@code UIUSON} / {@code VIUSON}) both "Uilson" and "Vilson", while "Gonsalves" and "Goncalves" do not
 * match each other. A text with no ç, w or final -ez has one key: its alternate is its primary.
 *
 * <p>Instances are immutable and thread-safe.
 */
public class BrazilianPortuguesePhonetic implements StringEncoder {

    private static final Set<String> PARTICLES = Set.of("de", "da", "do", "das", "dos", "e");

    private final BrazilianPortugueseTranscriber transcriber;
    private final int maxWordLength;
    private final boolean foldUnstressedVowels;
    private final boolean monophthongize;
    private final boolean dropParticles;

    /** The default encoder: monophthongization on, unstressed folding off, particles dropped, no length limit. */
    public BrazilianPortuguesePhonetic() {
        this(builder());
    }

    private BrazilianPortuguesePhonetic(Builder b) {
        this.transcriber = new BrazilianPortugueseTranscriber(b.simplifyEtymologicalSpelling);
        this.maxWordLength = b.maxWordLength;
        this.foldUnstressedVowels = b.foldUnstressedVowels;
        this.monophthongize = b.monophthongize;
        this.dropParticles = b.dropParticles;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Encodes a word or a name.
     *
     * @return the key, {@code null} for {@code null}, an empty string when there is nothing to encode
     */
    @Override
    public String encode(String text) {
        return encode(text, false);
    }

    /**
     * The alternate key: as {@link #encode(String)}, with ç read as K, w as V and a final -ez as -es.
     *
     * @return the key, {@code null} for {@code null}; the primary key when the text has no ç, w or final -ez
     */
    public String alternate(String text) {
        return encode(text, true);
    }

    private String encode(String text, boolean alternate) {
        if (text == null) {
            return null;
        }
        List<List<String>> keys = keys(text, alternate);
        List<String> words = new ArrayList<>(keys.size());
        for (List<String> tokens : keys) {
            String key = String.join("", tokens);
            words.add(maxWordLength > 0 && key.length() > maxWordLength ? key.substring(0, maxWordLength) : key);
        }
        return String.join(" ", words);
    }

    /** Commons Codec contract: encodes a {@link String}, refuses anything else. */
    @Override
    public Object encode(Object source) throws EncoderException {
        if (source == null) {
            return null;
        }
        if (!(source instanceof String s)) {
            throw new EncoderException("Parameter supplied to BrazilianPortuguesePhonetic encode is not of type "
                    + "java.lang.String");
        }
        return encode(s);
    }

    /** Whether the two texts share a key (primary or alternate). */
    public boolean isEncodeEqual(String a, String b) {
        String pa = encode(a);
        String pb = encode(b);
        if (pa == null || pb == null) {
            return false;
        }
        String aa = alternate(a);
        String ab = alternate(b);
        return pa.equals(pb) || pa.equals(ab) || aa.equals(pb) || aa.equals(ab);
    }

    /**
     * The key tokens of each word, primary or alternate (package use: {@link BrazilianPortugueseSoundex}).
     */
    List<List<String>> keys(String text, boolean alternate) {
        Transcription t = transcriber.transcribe(text);
        List<List<Segment>> words = new ArrayList<>(t.words());
        if (dropParticles && words.size() > 1) {
            List<String> raw = BrazilianPortugueseTranscriber.words(text);
            if (raw.size() == words.size()) {
                List<List<Segment>> kept = new ArrayList<>();
                for (int i = 0; i < words.size(); i++) {
                    if (!PARTICLES.contains(raw.get(i))) {
                        kept.add(words.get(i));
                    }
                }
                if (!kept.isEmpty()) {
                    words = kept;
                }
            }
        }
        List<List<String>> keys = new ArrayList<>(words.size());
        for (List<Segment> w : words) {
            keys.add(tokens(Transcription.copyOf(w), alternate));
        }
        return keys;
    }

    private List<String> tokens(List<Segment> w, boolean alternate) {
        int raised = Transcription.finalUnstressedMidVowel(w);
        if (raised >= 0) {
            w.get(raised).phoneme = w.get(raised).phoneme.isFrontMid() ? Phoneme.I : Phoneme.U;
        }
        if (alternate && w.size() >= 3) { // Rodriguez, Fernandez: the unstressed -es of Spanish surnames
            Segment last = w.getLast();
            Segment v = w.get(w.size() - 2);
            if (last.phoneme == Phoneme.ARCH_S && last.underlying == Phoneme.Z && v.phoneme == Phoneme.E_ARCH
                    && !v.accented && w.stream().filter(Segment::isVowel).count() >= 2) {
                v.phoneme = Phoneme.I;
            }
        }
        if (foldUnstressedVowels) {
            // stressed vowels too: without an accent the stress is a guess (Sergio, Keli, Sueli), so folding by
            // stress would part the spellings it should join (Sérgio, Kelly, Suely)
            for (Segment s : w) {
                if (s.phoneme.isFrontMid()) {
                    s.phoneme = Phoneme.I;
                } else if (s.phoneme.isBackMid()) {
                    s.phoneme = Phoneme.U;
                }
            }
        }
        if (monophthongize) {
            monophthongize(w, alternate);
        }

        List<String> tokens = new ArrayList<>(w.size() + 4);
        for (int k = 0; k < w.size(); k++) {
            Segment s = w.get(k);
            Segment next = k + 1 < w.size() ? w.get(k + 1) : null;
            switch (s.phoneme) {
                case ARCH_N -> {
                    if (next != null && next.isGlide()) { // ão, ãe, õe: the glide before the nasality
                        tokens.add(next.phoneme.key());
                        k++;
                    } else if (next == null && k > 0 && (s.underlying == Phoneme.M || s.underlying == Phoneme.N)) {
                        Segment v = w.get(k - 1);
                        if (v.phoneme.isFrontMid()) {
                            tokens.add("I"); // Belém [ẽj]
                        } else if (v.phoneme == Phoneme.A && !v.stressed) {
                            tokens.add("U"); // falam [ɐ̃w], Abraham = Abraão
                        }
                    }
                    tokens.add("N");
                }
                case L -> tokens.add(s.coda ? "U" : "L");
                default -> {
                    if (alternate && s.letter == 'ç') {
                        tokens.add("K"); // Gonçalves = Goncalves: the cedilla is often lost in records
                    } else if (alternate && s.letter == 'w') {
                        tokens.add("V"); // Wagner = Vagner
                    } else {
                        tokens.add(s.phoneme.key());
                    }
                    if (s.isIllicitCoda()) {
                        tokens.add("I");
                    }
                }
            }
        }
        List<String> collapsed = new ArrayList<>(tokens.size());
        for (String tok : tokens) {
            if (collapsed.isEmpty() || !collapsed.getLast().equals(tok)) {
                collapsed.add(tok);
            }
        }
        return collapsed;
    }

    /** ou → o (Moura, Lourdes, Souza); ei and ai before ɾ, ʃ, ʒ → e, a (Ribeiro, peixe, beijo, caixa). */
    private static void monophthongize(List<Segment> w, boolean alternate) {
        for (int k = w.size() - 2; k >= 0; k--) {
            Segment v = w.get(k);
            Segment g = w.get(k + 1);
            if (v.phoneme.isBackMid() && g.phoneme == Phoneme.W_GLIDE && !(alternate && g.letter == 'w')) {
                w.remove(k + 1);
            } else if ((v.phoneme.isFrontMid() || v.phoneme == Phoneme.A) && g.phoneme == Phoneme.J_GLIDE
                    && k + 2 < w.size()) {
                Phoneme after = w.get(k + 2).phoneme;
                if (after == Phoneme.TAP || after == Phoneme.SH || after == Phoneme.ZH) {
                    w.remove(k + 1);
                }
            }
        }
    }

    /** Options of {@link BrazilianPortuguesePhonetic}. */
    public static final class Builder {

        private int maxWordLength;
        private boolean foldUnstressedVowels;
        private boolean monophthongize = true;
        private boolean dropParticles = true;
        private boolean simplifyEtymologicalSpelling = true;

        private Builder() {
        }

        /** Truncates each word's key to this many characters; 0 (the default) keeps it whole. */
        public Builder maxWordLength(int maxWordLength) {
            if (maxWordLength < 0) {
                throw new IllegalArgumentException("maxWordLength must be >= 0: " + maxWordLength);
            }
            this.maxWordLength = maxWordLength;
            return this;
        }

        /**
         * Merges every e/o with i/u, stressed or not: Felipe = Filipe, Geovana = Giovana, and Sergio = Sérgio,
         * Keli = Kelly, whose unaccented spellings put the stress elsewhere. Also merges names that differ only
         * in e/i or o/u (Nelson = Nilson, Rosa = Rusa). Off by default.
         */
        public Builder foldUnstressedVowels() {
            this.foldUnstressedVowels = true;
            return this;
        }

        /** Keeps the diphthongs ou, ei, ai apart from o, e, a (they are merged by default). */
        public Builder keepDiphthongs() {
            this.monophthongize = false;
            return this;
        }

        /** Keeps de, da, do, das, dos, e in names (they are dropped by default). */
        public Builder keepParticles() {
            this.dropParticles = false;
            return this;
        }

        /** Reads the silent c and p of etymological spellings ("Victor", "Baptista") (they are dropped by default). */
        public Builder keepEtymologicalSpelling() {
            this.simplifyEtymologicalSpelling = false;
            return this;
        }

        public BrazilianPortuguesePhonetic build() {
            return new BrazilianPortuguesePhonetic(this);
        }
    }
}
