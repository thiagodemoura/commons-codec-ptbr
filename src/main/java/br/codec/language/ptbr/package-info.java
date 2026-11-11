/**
 * Brazilian Portuguese phonetics for Apache Commons Codec.
 *
 * <ul>
 *   <li>{@link br.codec.language.ptbr.BrazilianPortugueseTranscriber}: spelling to phonemes, rendered as a
 *       phonological transcription ({@code /ˈkaSka/}) or, per {@link br.codec.language.ptbr.Dialect}, a phonetic one
 *       ({@code [ˈkaʃkɐ]});</li>
 *   <li>{@link br.codec.language.ptbr.BrazilianPortuguesePhonetic}: a {@code StringEncoder} producing a phonetic
 *       matching key ("Luiz" = "Luís" = {@code LUIS});</li>
 *   <li>{@link br.codec.language.ptbr.BrazilianPortugueseSoundex}: a {@code StringEncoder} producing a Soundex-style
 *       code over PB phonemes ("Gonçalves" = {@code G654}).</li>
 * </ul>
 *
 * <p>References: Izabel Christine Seara, Vanessa Gonzaga Nunes, Cristiane Lazzarotto-Volcão, <i>Fonética e
 * Fonologia do Português Brasileiro</i>, Florianópolis, LLV/CCE/UFSC, 2011
 * (https://ppglin.posgrad.ufsc.br/files/2013/04/Livro_Fonetica_e_Fonologia.pdf); "Fonema e letra", Toda Matéria
 * (https://www.todamateria.com.br/fonema-e-letra/), for the values of x and the digraphs.
 */
package br.codec.language.ptbr;
