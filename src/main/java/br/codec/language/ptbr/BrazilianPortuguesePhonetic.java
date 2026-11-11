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
 *       unstressed e/o merge with i/u (Felipe = Filipe, the pretonic raising of Regra VIII).</li>
 * </ul>
 *
 * <p>A nasal vowel is written as its vowel followed by N, after its glide if it has one ("canta" {@code KANTA},
 * "mão" {@code MAUN}, "Belém" {@code BELEIN}): an N followed by a vowel is the consonant n, any other N is nasality.
 * Other tokens: X = ʃ, J = ʒ, Y = ɲ (nh), LH = ʎ, R = ɾ or /R/, H = "r" forte. Consecutive equal tokens are
 * collapsed (Isaac = Isac). Each word gets its key; keys are joined by a space. Prepositions and the conjunction
 * "e" of names (de, da, do, das, dos, e) are dropped unless {@link Builder#keepParticles()} is set.
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
        if (text == null) {
            return null;
        }
        List<List<String>> keys = keys(text);
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

    /** Whether the two texts have the same key. */
    public boolean isEncodeEqual(String a, String b) {
        String ka = encode(a);
        return ka != null && ka.equals(encode(b));
    }

    /** The key tokens of each word (package use: {@link BrazilianPortugueseSoundex}). */
    List<List<String>> keys(String text) {
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
            keys.add(tokens(Transcription.copyOf(w)));
        }
        return keys;
    }

    private List<String> tokens(List<Segment> w) {
        int raised = Transcription.finalUnstressedMidVowel(w);
        if (raised >= 0) {
            w.get(raised).phoneme = w.get(raised).phoneme.isFrontMid() ? Phoneme.I : Phoneme.U;
        }
        if (foldUnstressedVowels) {
            for (Segment s : w) {
                if (!s.stressed && s.phoneme.isFrontMid()) {
                    s.phoneme = Phoneme.I;
                } else if (!s.stressed && s.phoneme.isBackMid()) {
                    s.phoneme = Phoneme.U;
                }
            }
        }
        if (monophthongize) {
            monophthongize(w);
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
                    tokens.add(s.phoneme.key());
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
    private static void monophthongize(List<Segment> w) {
        for (int k = w.size() - 2; k >= 0; k--) {
            Segment v = w.get(k);
            Segment g = w.get(k + 1);
            if (v.phoneme.isBackMid() && g.phoneme == Phoneme.W_GLIDE) {
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

        /** Merges every unstressed e/o with i/u: Felipe = Filipe, Geovana = Giovana (off by default). */
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
