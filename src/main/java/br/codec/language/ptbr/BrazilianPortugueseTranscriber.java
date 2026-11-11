package br.codec.language.ptbr;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Transcribes Brazilian Portuguese spelling into phonemes.
 *
 * <p>The result is the <i>transcrição fonológica</i> of Seara, Nunes &amp; Lazzarotto-Volcão, <i>Fonética e
 * Fonologia do Português Brasileiro</i> (UFSC, 2011): the underlying representation, free of regional
 * variation, with the coda archiphonemes /S/, /R/, /N/ (sections 4.4 and 5.2) and the mid-vowel archiphonemes
 * /E/, /O/ where the spelling does not tell e from ɛ or o from ɔ. From it, {@link Transcription#phonetic(Dialect)}
 * derives the <i>transcrição fonética</i> of a dialect, and the encoders of this package derive their keys.
 *
 * <p>The steps, in order:
 * <ol>
 *   <li>Graphemes to phonemes: the digraphs ch, lh, nh, rr, ss, sc, sç, xc, qu, gu (and the foreign ph, th, sh,
 *       sch), silent h, c/g before e and i, intervocalic s, the values of x, the "r" fraco and "r" forte.</li>
 *   <li>Prothesis: an initial s + consonant gets a vowel in front ("Stefany" = "Estefany", section 5.1).</li>
 *   <li>Syllable coda: a consonant not followed by a vowel (and not the first consonant of a complex onset such as
 *       pr, bl) is in coda; s and z become /S/, r becomes /R/, m and n become /N/, l stays /l/. Any other
 *       consonant in coda is not licit in PB and gets an epenthetic [i] in speech.</li>
 *   <li>Glides: an unaccented i or u after a vowel is a semivowel (pai, pau) unless it closes a hiatus with
 *       z, l, r, m, n in the same syllable or nh after it (raiz, Raul, ruim, rainha) — the same condition as the
 *       accentuation rule of país and saída.</li>
 *   <li>Stress: the vowel with an acute or circumflex accent, else with a tilde, else the penultimate vowel of a word
 *       ending in a, e, o, as, es, os, am, em, ens — or in y, ys: foreign names such as Kelly, Stefany — and the
 *       last vowel of any other word (section 5.5). Stress comes from the spelling only: Spanish patronymics written
 *       with -ez (Gonsalvez, Rodriguez) are read as oxytones, like talvez.</li>
 * </ol>
 */
public final class BrazilianPortugueseTranscriber {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final Pattern APOSTROPHES = Pattern.compile("['’`´ʼ]");
    private static final Pattern NON_LETTERS = Pattern.compile("[^\\p{L}]+");
    private static final Pattern PAROXYTONE_ENDING = Pattern.compile("(?:a|e|o|as|es|os|am|em|ens|y|ys)$");
    private static final Pattern SILENT_C_OR_P = Pattern.compile("(?<=[aeiouáéíóúâêô])[cp](?=t)");

    /** Word beginnings where x is /ks/ ("fixo", "táxi", "tóxico", "sexo", "anexo", "reflexo"). */
    private static final String[] X_AS_KS = {"fix", "tax", "táx", "tox", "tóx", "sex", "nex", "anex", "reflex",
            "complex", "flux", "box", "oxig", "axiom", "léxic", "lexic"};
    /** Word beginnings where x is /s/ ("próximo", "máximo", "auxílio", "trouxe"). */
    private static final String[] X_AS_S = {"próxim", "proxim", "máxim", "maxim", "auxíl", "auxil", "trouxe",
            "sintax", "sintáx"};

    private final boolean simplifyEtymologicalSpelling;

    /** A transcriber that simplifies etymological spellings ("Victor" = "Vitor", "Baptista" = "Batista"). */
    public BrazilianPortugueseTranscriber() {
        this(true);
    }

    /**
     * @param simplifyEtymologicalSpelling drop the silent c and p of the pre-1943 etymological spellings still
     *                                     common in names (ct → t, pt → t, mpç → nç, mpt → nt); this also drops the
     *                                     c of words where it is pronounced ("pacto")
     */
    public BrazilianPortugueseTranscriber(boolean simplifyEtymologicalSpelling) {
        this.simplifyEtymologicalSpelling = simplifyEtymologicalSpelling;
    }

    /**
     * Transcribes a text: each word on its own (words are split at anything that is not a letter; apostrophes are
     * removed, so "D'Ávila" is "Dávila" and "Sant'Anna" is "Santanna").
     */
    public Transcription transcribe(String text) {
        Objects.requireNonNull(text, "text");
        List<List<Segment>> words = new ArrayList<>();
        for (String word : words(text)) {
            List<Segment> segments = transcribeWord(word);
            if (!segments.isEmpty()) {
                words.add(segments);
            }
        }
        return new Transcription(words);
    }

    /** Lower-cases, folds letters foreign to Portuguese spelling and splits into words. */
    static List<String> words(String text) {
        String clean = APOSTROPHES.matcher(Normalizer.normalize(text, Normalizer.Form.NFC)).replaceAll("");
        List<String> words = new ArrayList<>();
        for (String raw : NON_LETTERS.split(clean.toLowerCase(PT_BR))) {
            StringBuilder w = new StringBuilder(raw.length());
            for (int i = 0; i < raw.length(); i++) {
                char ch = raw.charAt(i);
                switch (ch) {
                    case 'è', 'ë' -> w.append('e');
                    case 'ì', 'ï', 'î' -> w.append('i');
                    case 'ò', 'ö' -> w.append('o');
                    case 'ù', 'û' -> w.append('u');
                    case 'ä' -> w.append('a');
                    case 'ÿ', 'ý' -> w.append('y');
                    case 'ñ' -> w.append("nh");
                    default -> {
                        if ((ch >= 'a' && ch <= 'z') || "áàâãéêíóôõúüç".indexOf(ch) >= 0) {
                            w.append(ch);
                        }
                    }
                }
            }
            if (!w.isEmpty()) {
                words.add(w.toString());
            }
        }
        return words;
    }

    List<Segment> transcribeWord(String word) {
        String w = simplifyEtymologicalSpelling ? simplify(word) : word;
        List<Segment> segments = graphemesToPhonemes(w);
        prothesis(segments);
        markCodas(segments);
        resolveGlides(segments);
        assignStress(segments, w);
        return segments;
    }

    private static String simplify(String w) {
        String s = w.replace("mpç", "nç").replace("mpt", "nt");
        return SILENT_C_OR_P.matcher(s).replaceAll("");
    }

    // ---------------------------------------------------------------------------------------------- graphemes

    private static boolean isVowelLetter(char ch) {
        return ch != 0 && "aáàâãeéêiíoóôõuúüy".indexOf(ch) >= 0;
    }

    private static boolean isFrontLetter(char ch) {
        return ch != 0 && "eéêiíy".indexOf(ch) >= 0;
    }

    private static List<Segment> graphemesToPhonemes(String w) {
        List<Segment> out = new ArrayList<>(w.length() + 2);
        int n = w.length();
        for (int i = 0; i < n; i++) {
            char ch = w.charAt(i);
            char n1 = i + 1 < n ? w.charAt(i + 1) : 0;
            char n2 = i + 2 < n ? w.charAt(i + 2) : 0;
            char p = i > 0 ? w.charAt(i - 1) : 0;

            // orthographic geminates of names (Mello, Mattos, Anna, Rizzo) are one consonant; rr, ss and cc are not
            if (!isVowelLetter(ch) && ch == n1 && ch != 'r' && ch != 's' && ch != 'c') {
                continue;
            }
            switch (ch) {
                case 'a', 'à' -> add(out, Phoneme.A);
                case 'á', 'â' -> add(out, Phoneme.A).accented = true;
                case 'ã' -> {
                    add(out, Phoneme.A).tilde = true;
                    add(out, Phoneme.ARCH_N);
                    if (n1 == 'o') { // ão
                        add(out, Phoneme.W_GLIDE);
                        i++;
                    } else if (n1 == 'e') { // ãe
                        add(out, Phoneme.J_GLIDE);
                        i++;
                    }
                }
                case 'e' -> add(out, Phoneme.E_ARCH);
                case 'é' -> add(out, Phoneme.E_OPEN).accented = true;
                case 'ê' -> add(out, Phoneme.E_CLOSED).accented = true;
                case 'i', 'y' -> add(out, Phoneme.I);
                case 'í' -> add(out, Phoneme.I).accented = true;
                case 'o' -> add(out, Phoneme.O_ARCH);
                case 'ó' -> add(out, Phoneme.O_OPEN).accented = true;
                case 'ô' -> add(out, Phoneme.O_CLOSED).accented = true;
                case 'õ' -> {
                    add(out, Phoneme.O_CLOSED).tilde = true;
                    add(out, Phoneme.ARCH_N);
                    if (n1 == 'e') { // õe
                        add(out, Phoneme.J_GLIDE);
                        i++;
                    }
                }
                case 'u', 'ü' -> add(out, Phoneme.U);
                case 'ú' -> add(out, Phoneme.U).accented = true;
                case 'b' -> add(out, Phoneme.B);
                case 'd' -> add(out, Phoneme.D);
                case 'f' -> add(out, Phoneme.F);
                case 'k' -> add(out, Phoneme.K);
                case 'v' -> add(out, Phoneme.V);
                case 'j' -> add(out, Phoneme.ZH);
                case 'm' -> add(out, Phoneme.M);
                case 'z' -> add(out, Phoneme.Z);
                case 'ç' -> add(out, Phoneme.S);
                case 'w' -> add(out, Phoneme.W_GLIDE); // Wellington, Wilson: [w]
                case 'h' -> { /* silent: hoje, Hugo, Thiago; the digraphs are read with the letter before */ }
                case 'p' -> {
                    if (n1 == 'h') { // Raphael, Philippe
                        add(out, Phoneme.F);
                        i++;
                    } else {
                        add(out, Phoneme.P);
                    }
                }
                case 't' -> {
                    if (n1 == 'h') { // Thiago, Ruth, Cinthia
                        i++;
                    }
                    add(out, Phoneme.T);
                }
                case 'c' -> {
                    if (n1 == 'h') {
                        add(out, n2 == 'r' || n2 == 'l' ? Phoneme.K : Phoneme.SH); // Christiano, Chloé : chuva
                        i++;
                    } else {
                        add(out, isFrontLetter(n1) ? Phoneme.S : Phoneme.K);
                    }
                }
                case 'g' -> {
                    if (n1 == 'ü' && isVowelLetter(n2)) { // agüentar
                        add(out, Phoneme.G);
                        add(out, Phoneme.W_GLIDE);
                        i++;
                    } else if (n1 == 'u' && isFrontLetter(n2)) { // Guilherme, Miguel: silent u
                        add(out, Phoneme.G);
                        i++;
                    } else if (n1 == 'u' && isVowelLetter(n2)) { // Guarani, Iguaçu
                        add(out, Phoneme.G);
                        add(out, Phoneme.W_GLIDE);
                        i++;
                    } else if (isFrontLetter(n1)) { // gema, Gisele
                        add(out, Phoneme.ZH);
                    } else {
                        if (n1 == 'h') {
                            i++;
                        }
                        add(out, Phoneme.G);
                    }
                }
                case 'q' -> {
                    add(out, Phoneme.K);
                    if ((n1 == 'u' || n1 == 'ü') && isVowelLetter(n2)) {
                        if (n1 == 'ü' || !isFrontLetter(n2)) { // quando, cinqüenta : queijo, quindim
                            add(out, Phoneme.W_GLIDE);
                        }
                        i++;
                    }
                }
                case 'l' -> {
                    if (n1 == 'h') {
                        add(out, Phoneme.LH);
                        i++;
                    } else {
                        add(out, Phoneme.L);
                    }
                }
                case 'n' -> {
                    if (n1 == 'h') {
                        add(out, Phoneme.NH);
                        i++;
                    } else {
                        add(out, Phoneme.N);
                    }
                }
                case 'r' -> {
                    if (n1 == 'r') { // carro
                        add(out, Phoneme.TRILL);
                        i++;
                    } else if (i == 0 || p == 'n' || p == 'l' || p == 's') { // rato, honra, guelra, Israel
                        add(out, Phoneme.TRILL);
                    } else { // caro, prato, mar
                        add(out, Phoneme.TAP);
                    }
                }
                case 's' -> {
                    if (n1 == 's' || n1 == 'ç') { // assa, desça
                        add(out, Phoneme.S);
                        i++;
                    } else if (n1 == 'c' && n2 == 'h') { // Schmidt, Schneider
                        add(out, Phoneme.SH);
                        i += 2;
                    } else if (n1 == 'c' && isFrontLetter(n2)) { // nascer
                        add(out, Phoneme.S);
                        i++;
                    } else if (n1 == 'h') { // Shirley, Washington
                        add(out, Phoneme.SH);
                        i++;
                    } else if (isVowelLetter(p) && isVowelLetter(n1)) { // casa, Isabel
                        add(out, Phoneme.Z);
                    } else {
                        add(out, Phoneme.S);
                    }
                }
                case 'x' -> {
                    i += x(out, w, i, n1, n2, p);
                }
                default -> { /* not a letter of Portuguese spelling */ }
            }
        }
        return out;
    }

    /** The letter x: /ʃ/, /z/, /s/ or /ks/. Returns how many following letters it consumed. */
    private static int x(List<Segment> out, String w, int i, char n1, char n2, char p) {
        if (isVowelLetter(n1) && startsWithXAt(w, i, X_AS_KS)) { // sexo, but not sexto
            add(out, Phoneme.K);
            add(out, Phoneme.S);
            return 0;
        }
        if (isVowelLetter(n1) && startsWithXAt(w, i, X_AS_S)) {
            add(out, Phoneme.S);
            return 0;
        }
        if (i == 0) { // Xavier, xícara
            add(out, Phoneme.SH);
            return 0;
        }
        if (n1 == 'c' && isFrontLetter(n2)) { // exceção, excelente: xc = /s/
            add(out, Phoneme.S);
            return 1;
        }
        boolean exPrefix = (p == 'e' || p == 'ê') && (i == 1 || w.startsWith("hex") || w.startsWith("inex"));
        if (exPrefix && isVowelLetter(n1)) { // exalar, exílio, Exupério, inexato
            add(out, Phoneme.Z);
            return 0;
        }
        if (n1 == 0) { // Félix, Max, tórax
            add(out, Phoneme.K);
            add(out, Phoneme.S);
            return 0;
        }
        if (!isVowelLetter(n1)) { // sexto, texto, Expedito: coda, becomes /S/
            add(out, Phoneme.S);
            return 0;
        }
        add(out, Phoneme.SH); // caixa, peixe, enxame, Alexandre, lixo
        return 0;
    }

    private static boolean startsWithXAt(String w, int i, String[] stems) {
        for (String stem : stems) {
            if (w.startsWith(stem) && stem.indexOf('x') == i) {
                return true;
            }
        }
        return false;
    }

    private static Segment add(List<Segment> out, Phoneme phoneme) {
        Segment s = new Segment(phoneme);
        out.add(s);
        return s;
    }

    // ---------------------------------------------------------------------------------------------- syllables

    /** Word-initial s + consonant is not a licit onset: speakers insert a vowel ("Stefany" [isteˈfɐ̃ni]). */
    private static void prothesis(List<Segment> s) {
        if (s.size() >= 2 && s.get(0).phoneme == Phoneme.S && s.get(1).phoneme.isConsonant()) {
            Segment e = new Segment(Phoneme.E_ARCH);
            e.inserted = true;
            s.addFirst(e);
        }
    }

    private static void markCodas(List<Segment> s) {
        for (int k = 0; k < s.size(); k++) {
            Segment seg = s.get(k);
            Phoneme ph = seg.phoneme;
            if (ph.kind() == Phoneme.Kind.ARCHIPHONEME) {
                seg.coda = true;
                continue;
            }
            if (!ph.isConsonant()) {
                continue;
            }
            Segment next = k + 1 < s.size() ? s.get(k + 1) : null;
            boolean onset = next != null
                    && (next.isVowel() || next.isGlide()
                    || (ph.isClusterHead() && (next.phoneme == Phoneme.L || next.phoneme == Phoneme.TAP)
                    && k + 2 < s.size() && !s.get(k + 2).phoneme.isConsonant()));
            if (onset) {
                continue;
            }
            seg.coda = true;
            boolean afterNucleus = k > 0 && (s.get(k - 1).isVowel() || s.get(k - 1).isGlide());
            switch (ph) {
                case S, Z, SH, ZH -> {
                    if (k > 0) {
                        seg.phoneme = Phoneme.ARCH_S;
                    }
                }
                case TAP, TRILL -> {
                    if (afterNucleus) {
                        seg.phoneme = Phoneme.ARCH_R;
                    }
                }
                case M, N -> {
                    if (afterNucleus) {
                        seg.phoneme = Phoneme.ARCH_N;
                    }
                }
                default -> { /* l stays /l/; any other consonant is an illicit coda */ }
            }
        }
    }

    private static void resolveGlides(List<Segment> s) {
        for (int k = 1; k < s.size(); k++) {
            Segment seg = s.get(k);
            if (seg.accented || seg.tilde || (seg.phoneme != Phoneme.I && seg.phoneme != Phoneme.U)) {
                continue;
            }
            Segment prev = s.get(k - 1);
            if (!prev.isVowel()) {
                continue;
            }
            // ou and ei are diphthongs before any coda (Lourdes, Leiria); the hiatus is after a, i, u
            boolean openOrHigh = prev.phoneme == Phoneme.A || prev.phoneme == Phoneme.I || prev.phoneme == Phoneme.U;
            Segment next = k + 1 < s.size() ? s.get(k + 1) : null;
            boolean hiatus = openOrHigh && next != null
                    && (next.phoneme == Phoneme.NH || (next.coda && closesHiatus(next)));
            if (!hiatus) {
                seg.phoneme = seg.phoneme == Phoneme.I ? Phoneme.J_GLIDE : Phoneme.W_GLIDE;
            }
        }
    }

    /** z, l, r, m, n in the same syllable after i/u keep the hiatus: raiz, Luiz, Raul, sair, ruim. Not s: pais. */
    private static boolean closesHiatus(Segment coda) {
        return switch (coda.underlying) {
            case Z, L, TAP, TRILL, M, N -> true;
            default -> false;
        };
    }

    private static void assignStress(List<Segment> s, String word) {
        List<Segment> nuclei = new ArrayList<>();
        for (Segment seg : s) {
            if (seg.isVowel()) {
                nuclei.add(seg);
            }
        }
        if (nuclei.isEmpty()) {
            return;
        }
        Segment stressed = null;
        for (Segment v : nuclei) {
            if (v.accented) {
                stressed = v;
                break;
            }
        }
        if (stressed == null) {
            for (Segment v : nuclei) {
                if (v.tilde) {
                    stressed = v;
                    break;
                }
            }
        }
        if (stressed == null) {
            boolean paroxytone = nuclei.size() >= 2 && PAROXYTONE_ENDING.matcher(word).find();
            stressed = nuclei.get(nuclei.size() - (paroxytone ? 2 : 1));
        }
        stressed.stressed = true;
    }
}
