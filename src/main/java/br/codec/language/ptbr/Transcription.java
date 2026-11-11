package br.codec.language.ptbr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The result of {@link BrazilianPortugueseTranscriber#transcribe(String)}: the phonemes of each word, rendered
 * phonologically ({@code /ˈkaSka/}) or phonetically for a {@link Dialect} ({@code [ˈkaʃkɐ]}).
 *
 * <p>The phonetic rendering applies the processes of chapter 6 of Seara, Nunes &amp; Lazzarotto-Volcão
 * (UFSC, 2011): raising of final unstressed e/o (Regra II), nasalization of the stressed vowel before a nasal
 * consonant (Regra III), palatalization of t/d before [i] (Regra IV), voicing of coda /S/ and /R/ before a voiced
 * consonant (Regra VI), the realization of the archiphonemes /S/, /R/, /N/ and of coda /l/, the open pretonic vowels
 * of the Northeast, the epenthetic [i] after an illicit coda consonant, and the [z] of a word-final /S/ before a
 * word that begins with a vowel (Quadro 17: lápis amarelo).
 */
public final class Transcription {

    private static final String STRESS = "ˈ";

    private final List<List<Segment>> words;

    Transcription(List<List<Segment>> words) {
        this.words = words;
    }

    /** Package view of the segments; callers must not modify them (use {@link #copyOf(List)}). */
    List<List<Segment>> words() {
        return words;
    }

    static List<Segment> copyOf(List<Segment> word) {
        List<Segment> copy = new ArrayList<>(word.size());
        for (Segment s : word) {
            copy.add(s.copy());
        }
        return copy;
    }

    public boolean isEmpty() {
        return words.isEmpty();
    }

    /** The phonemes of each word, in order (archiphonemes included, prothetic vowels not). */
    public List<List<Phoneme>> phonemes() {
        List<List<Phoneme>> out = new ArrayList<>(words.size());
        for (List<Segment> w : words) {
            out.add(w.stream().filter(s -> !s.inserted).map(s -> s.phoneme).toList());
        }
        return Collections.unmodifiableList(out);
    }

    /** The phonological transcription, e.g. {@code /ˈkaNta/}, {@code /ˈlapiS amaˈɾElO/}. */
    public String phonological() {
        return words.stream().map(Transcription::phonologicalWord).collect(Collectors.joining(" ", "/", "/"));
    }

    /** The phonetic transcription in the {@link Dialect#PAULISTANO} dialect. */
    public String phonetic() {
        return phonetic(Dialect.PAULISTANO);
    }

    /** The phonetic transcription in the given dialect, e.g. {@code [ˈkaʃkɐ]} for "casca" in {@link Dialect#CARIOCA}. */
    public String phonetic(Dialect dialect) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < words.size(); i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(phoneticWord(words.get(i), i + 1 < words.size() ? words.get(i + 1) : null, dialect));
        }
        return sb.append(']').toString();
    }

    @Override
    public String toString() {
        return phonological();
    }

    // ------------------------------------------------------------------------------------------ phonological

    private static String phonologicalWord(List<Segment> w) {
        int stress = stressIndex(w);
        int onset = stress < 0 ? -1 : onsetStart(w, stress);
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < w.size(); k++) {
            if (k == onset) {
                sb.append(STRESS);
            }
            if (!w.get(k).inserted) {
                sb.append(w.get(k).phoneme.symbol());
            }
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------------------------------ phonetic

    private static String phoneticWord(List<Segment> w, List<Segment> nextWord, Dialect d) {
        int stress = stressIndex(w);
        int last = lastNucleus(w);
        int raised = d.raiseFinalVowels() ? finalUnstressedMidVowel(w) : -1;
        String[] parts = new String[w.size()];
        for (int k = 0; k < w.size(); k++) {
            Segment s = w.get(k);
            Segment next = k + 1 < w.size() ? w.get(k + 1) : null;
            parts[k] = switch (s.phoneme.kind()) {
                case VOWEL -> vowel(w, k, stress, last, raised, d);
                case GLIDE -> s.phoneme.symbol();
                case ARCHIPHONEME -> archiphoneme(w, k, next, nextWord, stress, d);
                case CONSONANT -> consonant(w, k, next, raised, d);
            };
        }
        StringBuilder sb = new StringBuilder();
        int onset = stress < 0 ? -1 : onsetStart(w, stress);
        for (int k = 0; k < parts.length; k++) {
            if (k == onset) {
                sb.append(STRESS);
            }
            sb.append(parts[k]);
        }
        return sb.toString();
    }

    private static String vowel(List<Segment> w, int k, int stress, int last, int raised, Dialect d) {
        Segment s = w.get(k);
        Segment next = k + 1 < w.size() ? w.get(k + 1) : null;
        boolean pretonic = stress >= 0 && k < stress;
        String v;
        if (k == raised) {
            v = s.phoneme.isFrontMid() ? "i" : "u";
        } else {
            v = switch (s.phoneme) {
                case A -> k == last && !s.stressed && onlyArchSAfter(w, k) ? "ɐ" : "a";
                case E_OPEN -> "ɛ";
                case E_CLOSED -> "e";
                case E_ARCH -> pretonic && d.openPretonicMidVowels() ? "ɛ" : "e";
                case O_OPEN -> "ɔ";
                case O_CLOSED -> "o";
                case O_ARCH -> pretonic && d.openPretonicMidVowels() ? "ɔ" : "o";
                default -> s.phoneme.symbol();
            };
        }
        boolean nasal = next != null && (next.phoneme == Phoneme.ARCH_N
                || (s.stressed && !next.coda
                && (next.phoneme == Phoneme.M || next.phoneme == Phoneme.N || next.phoneme == Phoneme.NH)));
        return nasal ? nasalize(v) : v;
    }

    private static String nasalize(String v) {
        return switch (v) {
            case "a", "ɐ" -> "ɐ̃";
            case "e", "ɛ" -> "ẽ";
            case "o", "ɔ" -> "õ";
            case "i" -> "ĩ";
            case "u" -> "ũ";
            default -> v;
        };
    }

    private static String archiphoneme(List<Segment> w, int k, Segment next, List<Segment> nextWord, int stress,
                                       Dialect d) {
        Segment s = w.get(k);
        return switch (s.phoneme) {
            case ARCH_N -> {
                // word-final -em, -am written with a consonant: [ẽj] (Belém, hífen), unstressed [ɐ̃w] (falam)
                if (next == null && (s.underlying == Phoneme.M || s.underlying == Phoneme.N) && k > 0) {
                    Segment v = w.get(k - 1);
                    if (v.phoneme.isFrontMid()) {
                        yield "j";
                    }
                    if (v.phoneme == Phoneme.A && !v.stressed) {
                        yield "w";
                    }
                }
                yield "";
            }
            case ARCH_R -> next != null && isVoicedConsonant(next) ? d.codaRVoiced() : d.codaR();
            case ARCH_S -> {
                if (next == null) {
                    if (nextWord != null && !nextWord.isEmpty() && nextWord.getFirst().isVowel()) {
                        yield "z"; // lápis amarelo: [z] in every dialect
                    }
                    boolean voiced = nextWord != null && !nextWord.isEmpty() && isVoicedConsonant(nextWord.getFirst());
                    yield sibilant(voiced, d);
                }
                yield sibilant(isVoicedConsonant(next), d);
            }
            default -> s.phoneme.symbol();
        };
    }

    private static String sibilant(boolean voiced, Dialect d) {
        if (d.chianteCodaS()) {
            return voiced ? "ʒ" : "ʃ";
        }
        return voiced ? "z" : "s";
    }

    private static String consonant(List<Segment> w, int k, Segment next, int raised, Dialect d) {
        Segment s = w.get(k);
        String c = switch (s.phoneme) {
            case T -> d.palatalizeTD() && iFollows(w, k, raised) ? "tʃ" : "t";
            case D -> d.palatalizeTD() && iFollows(w, k, raised) ? "dʒ" : "d";
            case TRILL -> d.strongR();
            case L -> s.coda ? d.codaL() : "l";
            default -> s.phoneme.symbol();
        };
        return s.isIllicitCoda() ? c + "i" : c;
    }

    private static boolean iFollows(List<Segment> w, int k, int raised) {
        if (w.get(k).isIllicitCoda()) {
            return true; // epenthetic [i]: advogado [adʒivoˈgadu]
        }
        if (k + 1 >= w.size()) {
            return false;
        }
        Phoneme next = w.get(k + 1).phoneme;
        return next == Phoneme.I || next == Phoneme.J_GLIDE || (k + 1 == raised && next.isFrontMid());
    }

    private static boolean isVoicedConsonant(Segment s) {
        return !s.isVowel() && !s.isGlide() && s.phoneme.voiced();
    }

    // ------------------------------------------------------------------------------------------ helpers

    static int stressIndex(List<Segment> w) {
        for (int k = 0; k < w.size(); k++) {
            if (w.get(k).stressed) {
                return k;
            }
        }
        return -1;
    }

    static int lastNucleus(List<Segment> w) {
        for (int k = w.size() - 1; k >= 0; k--) {
            if (w.get(k).isVowel()) {
                return k;
            }
        }
        return -1;
    }

    /** The final unstressed oral e/o (Regra II), optionally followed by /S/: leque, bolo, Lopes, Marcos. */
    static int finalUnstressedMidVowel(List<Segment> w) {
        int last = lastNucleus(w);
        if (last < 0) {
            return -1;
        }
        Segment v = w.get(last);
        if (v.stressed || !(v.phoneme.isFrontMid() || v.phoneme.isBackMid())) {
            return -1;
        }
        return onlyArchSAfter(w, last) ? last : -1;
    }

    private static boolean onlyArchSAfter(List<Segment> w, int k) {
        for (int j = k + 1; j < w.size(); j++) {
            if (w.get(j).phoneme != Phoneme.ARCH_S) {
                return false;
            }
        }
        return true;
    }

    /** The first segment of the syllable whose nucleus is at {@code nucleus}: where the stress mark goes. */
    static int onsetStart(List<Segment> w, int nucleus) {
        int k = nucleus;
        while (k > 0) {
            Segment prev = w.get(k - 1);
            if (prev.isVowel() || prev.coda) {
                break;
            }
            if (prev.isGlide() && k >= 2 && (w.get(k - 2).isVowel() || w.get(k - 2).phoneme == Phoneme.ARCH_N)) {
                break; // offglide of the previous syllable: Queiroz /kEjˈɾOS/
            }
            k--;
        }
        return k;
    }
}
