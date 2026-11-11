package br.codec.language.ptbr;

/**
 * The segments of Brazilian Portuguese (PB) used by the transcriber: the consonant and vowel phonemes of
 * Seara, Nunes &amp; Lazzarotto-Volcão, <i>Fonética e Fonologia do Português Brasileiro</i> (UFSC, 2011), Quadros 7
 * and 8, the two glides (semivogais) and the archiphonemes of section 4.4 / 5.2.
 *
 * <p>Each value carries its symbol in a phonological transcription (between slashes, IPA where the phoneme is
 * a phoneme, a capital letter where it is an archiphoneme) and its token in the matching key built by
 * {@link BrazilianPortuguesePhonetic}.
 */
public enum Phoneme {

    // oclusivas
    P("p", "P", Kind.CONSONANT, false),
    B("b", "B", Kind.CONSONANT, true),
    T("t", "T", Kind.CONSONANT, false),
    D("d", "D", Kind.CONSONANT, true),
    K("k", "K", Kind.CONSONANT, false),
    G("g", "G", Kind.CONSONANT, true),
    // fricativas
    F("f", "F", Kind.CONSONANT, false),
    V("v", "V", Kind.CONSONANT, true),
    S("s", "S", Kind.CONSONANT, false),
    Z("z", "Z", Kind.CONSONANT, true),
    SH("ʃ", "X", Kind.CONSONANT, false),
    ZH("ʒ", "J", Kind.CONSONANT, true),
    // nasais
    M("m", "M", Kind.CONSONANT, true),
    N("n", "N", Kind.CONSONANT, true),
    NH("ɲ", "Y", Kind.CONSONANT, true),
    // laterais
    L("l", "L", Kind.CONSONANT, true),
    LH("ʎ", "LH", Kind.CONSONANT, true),
    // róticos: "r" fraco (tepe) e "r" forte (vibrante múltipla, fricativa na maioria dos dialetos)
    TAP("ɾ", "R", Kind.CONSONANT, true),
    TRILL("r", "H", Kind.CONSONANT, true),

    // arquifonemas de coda silábica (seções 4.4 e 5.2)
    /** /S/: neutralização de s, z, ʃ, ʒ em final de sílaba. */
    ARCH_S("S", "S", Kind.ARCHIPHONEME, false),
    /** /R/: neutralização do "r" fraco e do "r" forte em final de sílaba. */
    ARCH_R("R", "R", Kind.ARCHIPHONEME, true),
    /** /N/: consoante nasal de coda (vogal nasal bifonêmica, Câmara Jr.). */
    ARCH_N("N", "N", Kind.ARCHIPHONEME, true),

    // semivogais
    J_GLIDE("j", "I", Kind.GLIDE, true),
    W_GLIDE("w", "U", Kind.GLIDE, true),

    // vogais
    A("a", "A", Kind.VOWEL, true),
    E_OPEN("ɛ", "E", Kind.VOWEL, true),
    E_CLOSED("e", "E", Kind.VOWEL, true),
    /** /E/: média anterior cuja altura (e ou ɛ) a ortografia não revela. */
    E_ARCH("E", "E", Kind.VOWEL, true),
    I("i", "I", Kind.VOWEL, true),
    O_OPEN("ɔ", "O", Kind.VOWEL, true),
    O_CLOSED("o", "O", Kind.VOWEL, true),
    /** /O/: média posterior cuja altura (o ou ɔ) a ortografia não revela. */
    O_ARCH("O", "O", Kind.VOWEL, true),
    U("u", "U", Kind.VOWEL, true);

    /** Broad class of a segment. */
    public enum Kind { CONSONANT, ARCHIPHONEME, GLIDE, VOWEL }

    private final String symbol;
    private final String key;
    private final Kind kind;
    private final boolean voiced;

    Phoneme(String symbol, String key, Kind kind, boolean voiced) {
        this.symbol = symbol;
        this.key = key;
        this.kind = kind;
        this.voiced = voiced;
    }

    /** Symbol used in the phonological transcription. */
    public String symbol() {
        return symbol;
    }

    /** Token used in the phonetic matching key. */
    public String key() {
        return key;
    }

    public Kind kind() {
        return kind;
    }

    /** Whether the segment is voiced (vozeado / sonoro). */
    public boolean voiced() {
        return voiced;
    }

    public boolean isVowel() {
        return kind == Kind.VOWEL;
    }

    public boolean isGlide() {
        return kind == Kind.GLIDE;
    }

    public boolean isConsonant() {
        return kind == Kind.CONSONANT;
    }

    public boolean isFrontMid() {
        return this == E_OPEN || this == E_CLOSED || this == E_ARCH;
    }

    public boolean isBackMid() {
        return this == O_OPEN || this == O_CLOSED || this == O_ARCH;
    }

    /** Stops and labiodental fricatives: the first consonant of a complex onset (C + l/ɾ), section 5.1. */
    boolean isClusterHead() {
        return switch (this) {
            case P, B, T, D, K, G, F, V -> true;
            default -> false;
        };
    }
}
