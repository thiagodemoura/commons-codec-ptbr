package br.codec.language.ptbr;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;

/**
 * A Soundex-style code for Brazilian Portuguese: a letter and three digits ({@code "Gonçalves"} → {@code G524}).
 *
 * <p>Where the classic {@code org.apache.commons.codec.language.Soundex} groups English <i>letters</i>, this code
 * groups PB <i>phonemes</i> (from {@link BrazilianPortuguesePhonetic}), so digraphs, silent letters, c/g before e
 * and i, intervocalic s and the values of x are already resolved. The groups are the classes of
 * <i>sons foneticamente semelhantes</i> of Seara, Nunes &amp; Lazzarotto-Volcão, section 4.3 (after Cristófaro
 * Silva): voiced and voiceless pairs at one place of articulation, fricatives with close places, the nasals among
 * themselves, the laterals, vibrants and tap among themselves.
 *
 * <pre>
 *   1  p b            bilabial stops
 *   2  t d            dental-alveolar stops
 *   3  k g            velar stops
 *   4  f v            labiodental fricatives
 *   5  s z ʃ ʒ /S/    sibilants and chiantes
 *   6  m n ɲ /N/      nasals (and the nasality of a vowel: cata K200 ≠ canta K620)
 *   7  l ʎ ɾ r /R/    laterals and rhotics
 * </pre>
 *
 * Vowels (and coda l, vocalized) are not coded but separate equal digits, as in Soundex. The first character is the
 * first letter of the key. Each word is coded; codes are joined by a space.
 */
public class BrazilianPortugueseSoundex implements StringEncoder {

    private static final int LENGTH = 4;

    private final BrazilianPortuguesePhonetic phonetic;

    public BrazilianPortugueseSoundex() {
        this(new BrazilianPortuguesePhonetic());
    }

    /** Codes the keys of the given phonetic encoder (e.g. one with {@code foldUnstressedVowels}). */
    public BrazilianPortugueseSoundex(BrazilianPortuguesePhonetic phonetic) {
        this.phonetic = phonetic;
    }

    @Override
    public String encode(String text) {
        if (text == null) {
            return null;
        }
        List<String> codes = new ArrayList<>();
        for (List<String> tokens : phonetic.keys(text)) {
            if (!tokens.isEmpty()) {
                codes.add(code(tokens));
            }
        }
        return String.join(" ", codes);
    }

    @Override
    public Object encode(Object source) throws EncoderException {
        if (source == null) {
            return null;
        }
        if (!(source instanceof String s)) {
            throw new EncoderException("Parameter supplied to BrazilianPortugueseSoundex encode is not of type "
                    + "java.lang.String");
        }
        return encode(s);
    }

    /**
     * How many of the code characters of the two texts agree, from 0 (none) to 4 (same code), like
     * {@code Soundex.difference}; multi-word texts are compared on their first word.
     */
    public int difference(String a, String b) {
        String ca = encode(a);
        String cb = encode(b);
        if (ca == null || cb == null || ca.isEmpty() || cb.isEmpty()) {
            return 0;
        }
        int n = 0;
        for (int i = 0; i < LENGTH; i++) {
            if (ca.charAt(i) == cb.charAt(i)) {
                n++;
            }
        }
        return n;
    }

    private static String code(List<String> tokens) {
        StringBuilder sb = new StringBuilder(LENGTH);
        String first = tokens.getFirst();
        sb.append(first.charAt(0));
        char previous = digit(first);
        for (int i = 1; i < tokens.size() && sb.length() < LENGTH; i++) {
            char d = digit(tokens.get(i));
            if (d != '0' && d != previous) {
                sb.append(d);
            }
            previous = d;
        }
        while (sb.length() < LENGTH) {
            sb.append('0');
        }
        return sb.toString();
    }

    /** The group of a key token; '0' for vowels. */
    private static char digit(String token) {
        return switch (token) {
            case "P", "B" -> '1';
            case "T", "D" -> '2';
            case "K", "G" -> '3';
            case "F", "V" -> '4';
            case "S", "Z", "X", "J" -> '5';
            case "M", "N", "Y" -> '6';
            case "L", "LH", "R", "H" -> '7';
            default -> '0';
        };
    }
}
