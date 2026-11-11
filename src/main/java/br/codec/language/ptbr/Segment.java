package br.codec.language.ptbr;

/** One segment of a transcribed word. Mutable while the transcriber builds it; never exposed. */
final class Segment {

    Phoneme phoneme;
    /** The phoneme before coda neutralization (e.g. {@link Phoneme#Z} for the /S/ of "raiz"). */
    final Phoneme underlying;
    /** Written with an acute or circumflex accent: marks stress. */
    boolean accented;
    /** Written with a tilde (ã, õ). */
    boolean tilde;
    /** A consonant in syllable coda (always true for archiphonemes). */
    boolean coda;
    /** The stressed vowel of the word. */
    boolean stressed;
    /** Inserted by the transcriber, not written (prothetic e of "Stefany"). */
    boolean inserted;

    Segment(Phoneme phoneme) {
        this.phoneme = phoneme;
        this.underlying = phoneme;
    }

    Segment copy() {
        Segment s = new Segment(underlying);
        s.phoneme = phoneme;
        s.accented = accented;
        s.tilde = tilde;
        s.coda = coda;
        s.stressed = stressed;
        s.inserted = inserted;
        return s;
    }

    boolean isVowel() {
        return phoneme.isVowel();
    }

    boolean isGlide() {
        return phoneme.isGlide();
    }

    /**
     * A consonant left in coda that PB does not allow there (only /S/, /R/, /N/ and /l/ are licit codas,
     * section 5.2): it is followed by an epenthetic [i] in speech ("advogado" [adʒivoˈgadu], "Edson").
     */
    boolean isIllicitCoda() {
        return coda && phoneme.isConsonant() && phoneme != Phoneme.L;
    }

    @Override
    public String toString() {
        return phoneme.symbol();
    }
}
