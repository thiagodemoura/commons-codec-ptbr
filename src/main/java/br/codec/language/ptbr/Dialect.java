package br.codec.language.ptbr;

import java.util.Objects;

/**
 * The regional choices made when a phonological transcription is realized phonetically.
 *
 * <p>The presets follow what Seara, Nunes &amp; Lazzarotto-Volcão, <i>Fonética e Fonologia do Português
 * Brasileiro</i> (UFSC, 2011) states about each variety; where the book says nothing, the preset keeps the value
 * of the majority of PB and the field is marked "assumed" below.
 *
 * @param name                  a label
 * @param palatalizeTD          /t/ and /d/ become [tʃ] and [dʒ] before [i] (Regra IV: tia, dia)
 * @param chianteCodaS          coda /S/ is the chiante [ʃ]/[ʒ] instead of the sibilant [s]/[z] (section 4.4: casca)
 * @param strongR               realization of the "r" forte in onset (rato, carro, honra)
 * @param codaR                 realization of /R/ before a voiceless consonant and at the end of the word (corta, mar)
 * @param codaRVoiced           realization of /R/ before a voiced consonant (corda)
 * @param codaL                 realization of /l/ in coda: vocalized [w] or velarized [ɫ] (mal, balde)
 * @param openPretonicMidVowels unstressed /E/ and /O/ before the stress are open [ɛ] [ɔ] (menino, corado)
 * @param raiseFinalVowels      final unstressed e and o are [i] and [u] (Regra II: leque, bolo, júri = jure)
 */
public record Dialect(String name,
                      boolean palatalizeTD,
                      boolean chianteCodaS,
                      String strongR,
                      String codaR,
                      String codaRVoiced,
                      String codaL,
                      boolean openPretonicMidVowels,
                      boolean raiseFinalVowels) {

    /** São Paulo (capital): sibilant coda s (book: tosco [ˈtosku]); tap in coda, glottal strong r (assumed). */
    public static final Dialect PAULISTANO =
            new Dialect("paulistano", true, false, "h", "ɾ", "ɾ", "w", false, true);

    /** Rio de Janeiro: chiante coda s, velar fricative r, palatalized tia/dia (book, sections 3.2 and 4.4). */
    public static final Dialect CARIOCA =
            new Dialect("carioca", true, true, "x", "x", "ɣ", "w", false, true);

    /**
     * Florianópolis: chiante coda s (book: tosco [ˈtoʃku]), velar fricative r in onset and coda (book);
     * palatalization assumed.
     */
    public static final Dialect FLORIANOPOLITANO =
            new Dialect("florianopolitano", true, true, "x", "x", "ɣ", "w", false, true);

    /** Belo Horizonte: glottal r, voiceless [h] and voiced [ɦ] (book: corta, corda); the rest assumed. */
    public static final Dialect BELO_HORIZONTE =
            new Dialect("belo-horizontino", true, false, "h", "h", "ɦ", "w", false, true);

    /** Porto Alegre: velarized coda l [ɫ] (book: mal, balde); alveolar trill and tap assumed. */
    public static final Dialect PORTO_ALEGRE =
            new Dialect("porto-alegrense", true, false, "r", "ɾ", "ɾ", "ɫ", false, true);

    /** Caipira: retroflex coda r (book: mar, porca); the rest assumed. */
    public static final Dialect CAIPIRA =
            new Dialect("caipira", true, false, "h", "ɹ", "ɹ", "w", false, true);

    /** Northeast: open pretonic mid vowels (book: [mɛˈninu], [kɔˈɾadu]); no palatalization and glottal r assumed. */
    public static final Dialect NORDESTINO =
            new Dialect("nordestino", false, false, "h", "h", "ɦ", "w", true, true);

    public Dialect {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(strongR, "strongR");
        Objects.requireNonNull(codaR, "codaR");
        Objects.requireNonNull(codaRVoiced, "codaRVoiced");
        Objects.requireNonNull(codaL, "codaL");
    }
}
