package br.codec.language.ptbr;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Examples of Seara, Nunes &amp; Lazzarotto-Volcão, <i>Fonética e Fonologia do Português Brasileiro</i>
 * (UFSC, 2011), and of the Toda Matéria page "Fonema e letra" (the values of x, the digraphs, silent h).
 */
class TranscriptionTest {

    private final BrazilianPortugueseTranscriber transcriber = new BrazilianPortugueseTranscriber();

    private String phonological(String text) {
        return transcriber.transcribe(text).phonological();
    }

    private String phonetic(String text, Dialect dialect) {
        return transcriber.transcribe(text).phonetic(dialect);
    }

    @ParameterizedTest(name = "{0} = {1}")
    @CsvSource({
            // archiphonemes of coda (sections 4.4 and 5.2, Quadro 18)
            "casca,     /ˈkaSka/",
            "mar,       /ˈmaR/",
            "carta,     /ˈkaRta/",
            "canta,     /ˈkaNta/",
            "campo,     /ˈkaNpO/",
            // "r" fraco and "r" forte (Quadro 18)
            "caro,      /ˈkaɾO/",
            "carro,     /ˈkarO/",
            "rato,      /ˈratO/",
            "prato,     /ˈpɾatO/",
            "honra,     /ˈONra/",
            "Israel,    /iSraˈEl/",
            // the mid vowels the spelling cannot tell apart are /E/ and /O/; accents tell them
            "chatice,   /ʃaˈtisE/",
            "avó,       /aˈvɔ/",
            "avô,       /aˈvo/",
            // digraphs and silent h (Toda Matéria)
            "chuva,     /ˈʃuva/",
            "arranhar,  /araˈɲaR/",
            "quindim,   /kiNˈdiN/",
            "aborrecer, /abOrEˈsER/",
            "nascer,    /naˈsER/",
            "hoje,      /ˈOʒE/",
            "harpa,     /ˈaRpa/",
            "palha,     /ˈpaʎa/",
            // the values of x (Toda Matéria): /s/, /z/, /ks/, /ʃ/
            "sexto,     /ˈsEStO/",
            "exalar,    /EzaˈlaR/",
            "exílio,    /EˈziliO/",
            "fixo,      /ˈfiksO/",
            "xícara,    /ˈʃikaɾa/",
            "caixa,     /ˈkajʃa/",
            "próximo,   /ˈpɾɔsimO/",
            "exceção,   /EsEˈsaNw/",
            // g and j (Toda Matéria): ge = je, gi = ji
            "refúgio,   /rEˈfuʒiO/",
            "gema,      /ˈʒEma/",
            "guerra,    /ˈgEra/",
            "quando,    /ˈkwaNdO/",
    })
    void phonologicalTranscription(String word, String expected) {
        assertEquals(expected, phonological(word));
    }

    @Test
    void hiatusAndDiphthongs() {
        assertEquals("/raˈiS/", phonological("raiz"));    // hiatus before z
        assertEquals("/ˈpajS/", phonological("pais"));    // diphthong before s
        assertEquals("/paˈiS/", phonological("país"));
        assertEquals("/luˈiS/", phonological("Luiz"));
        assertEquals("/luˈiS/", phonological("Luís"));
        assertEquals("/raˈul/", phonological("Raul"));
        assertEquals("/ˈlOwRdES/", phonological("Lourdes")); // ou stays a diphthong
        assertEquals("/kEjˈɾOS/", phonological("Queiroz"));
        assertEquals("/ˈmaNw/", phonological("mão"));
        assertEquals("/ˈmaNj/", phonological("mãe"));
    }

    @Test
    void finalUnstressedVowelsAreRaisedRegraII() {
        assertEquals("[ˈʒuɾi]", phonetic("júri", Dialect.PAULISTANO));
        assertEquals("[ˈʒuɾi]", phonetic("jure", Dialect.PAULISTANO)); // júri and jure sound the same
        assertEquals("[ˈleki]", phonetic("leque", Dialect.PAULISTANO));
        assertEquals("[ˈbolu]", phonetic("bolo", Dialect.PAULISTANO));
        assertEquals("[ˈsoku]", phonetic("soco", Dialect.PAULISTANO));
    }

    @Test
    void palatalizationRegraIV() {
        assertEquals("[ˈtʃiɐ]", phonetic("tia", Dialect.CARIOCA));
        assertEquals("[ˈdʒiɐ]", phonetic("dia", Dialect.CARIOCA));
        assertEquals("[ˈtiɐ]", phonetic("tia", Dialect.NORDESTINO));
        assertEquals("[ʃaˈtʃisi]", phonetic("chatice", Dialect.CARIOCA));
        assertEquals("[ˈpẽtʃi]", phonetic("pente", Dialect.PAULISTANO)); // from the raised final e
        assertEquals("[ˈtõmi]", phonetic("tome", Dialect.NORDESTINO)); // also Regra III
    }

    @Test
    void codaSIsSibilantOrChianteAndAssimilatesVoicing() {
        assertEquals("[ˈtosku]", phonetic("tosco", Dialect.PAULISTANO));
        assertEquals("[ˈtoʃku]", phonetic("tosco", Dialect.FLORIANOPOLITANO));
        assertEquals("[ˈkaʃkɐ]", phonetic("casca", Dialect.CARIOCA));
        assertEquals("[ˈmezmu]", phonetic("mesmo", Dialect.PAULISTANO)); // Regra VI
        assertEquals("[ˈmeʒmu]", phonetic("mesmo", Dialect.CARIOCA));
    }

    @Test
    void codaSAcrossWordsQuadro17() {
        assertEquals("[ˈlapiz amaˈɾelu]", phonetic("lápis amarelo", Dialect.CARIOCA)); // [z] in every dialect
        assertEquals("[ˈlapiz amaˈɾelu]", phonetic("lápis amarelo", Dialect.PAULISTANO));
        assertEquals("[ˈlapis koloˈɾidu]", phonetic("lápis colorido", Dialect.PAULISTANO));
        assertEquals("[ˈlapiʃ koloˈɾidu]", phonetic("lápis colorido", Dialect.CARIOCA));
        assertEquals("[ˈlapiz ˈnovu]", phonetic("lápis novo", Dialect.PAULISTANO));
        assertEquals("[ˈlapiʒ ˈnovu]", phonetic("lápis novo", Dialect.CARIOCA));
    }

    @Test
    void rhoticsByDialect() {
        assertEquals("[ˈkoxtɐ]", phonetic("corta", Dialect.CARIOCA));
        assertEquals("[ˈkoɣdɐ]", phonetic("corda", Dialect.CARIOCA));
        assertEquals("[ˈkohtɐ]", phonetic("corta", Dialect.BELO_HORIZONTE));
        assertEquals("[ˈkoɦdɐ]", phonetic("corda", Dialect.BELO_HORIZONTE));
        assertEquals("[ˈpoɹkɐ]", phonetic("porca", Dialect.CAIPIRA));
        assertEquals("[ˈxatu]", phonetic("rato", Dialect.CARIOCA));
        assertEquals("[ˈkaɾu]", phonetic("caro", Dialect.CARIOCA));
        assertEquals("[ˈkaxu]", phonetic("carro", Dialect.CARIOCA));
    }

    @Test
    void codaL() {
        assertEquals("[ˈmaw]", phonetic("mal", Dialect.PAULISTANO));
        assertEquals("[ˈmaɫ]", phonetic("mal", Dialect.PORTO_ALEGRE));
        assertEquals("[ˈbaɫdʒi]", phonetic("balde", Dialect.PORTO_ALEGRE));
    }

    @Test
    void nasality() {
        assertEquals("[ˈkɐ̃mɐ]", phonetic("cama", Dialect.PAULISTANO)); // Regra III
        assertEquals("[ˈkɐ̃tɐ]", phonetic("canta", Dialect.PAULISTANO));
        assertEquals("[ˈmɐ̃w]", phonetic("mão", Dialect.PAULISTANO));
        assertEquals("[beˈlẽj]", phonetic("Belém", Dialect.PAULISTANO));
        assertEquals("[ˈfalɐ̃w]", phonetic("falam", Dialect.PAULISTANO));
    }

    @Test
    void openPretonicVowelsOfTheNortheast() {
        assertEquals("[mɛˈnĩnu]", phonetic("menino", Dialect.NORDESTINO));
        assertEquals("[kɔˈɾadu]", phonetic("corado", Dialect.NORDESTINO));
        assertEquals("[meˈnĩnu]", phonetic("menino", Dialect.PAULISTANO));
    }

    @Test
    void epenthesisAfterIllicitCoda() {
        assertEquals("[adʒivoˈgadu]", phonetic("advogado", Dialect.PAULISTANO));
        assertEquals("[ˈfɛlikis]", phonetic("Félix", Dialect.PAULISTANO));
        assertEquals("[esteˈfɐ̃ni]", phonetic("Stefany", Dialect.PAULISTANO));
    }
}
