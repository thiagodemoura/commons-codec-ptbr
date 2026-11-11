package br.codec.language.ptbr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;
import org.apache.commons.codec.StringEncoderComparator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BrazilianPortuguesePhoneticTest {

    private final BrazilianPortuguesePhonetic encoder = new BrazilianPortuguesePhonetic();

    @ParameterizedTest(name = "{0} = {1} = {2}")
    @CsvSource({
            "Luiz,       Luís,       LUIS",
            "Souza,      Sousa,      SOZA",
            "Thiago,     Tiago,      TIAGU",
            "Ruth,       Rute,       HUTI",
            "Xavier,     Chavier,    XAVIER",
            "Kátia,      Cátia,      KATIA",
            "Mello,      Melo,       MELU",
            "Mattos,     Matos,      MATUS",
            "Gonçalves,  Gonsalves,  GONSAUVIS",
            "Isaac,      Isac,       IZAKI",
            "Abraão,     Abraham,    ABRAUN",
            "Victor,     Vitor,      VITOR",
            "Baptista,   Batista,    BATISTA",
            "Cinthia,    Cíntia,     SINTIA",
            "Raphael,    Rafael,     HAFAEU",
            "Heloiza,    Heloísa,    ELOIZA",
            "Wellington, Welington,  UELINGITON",
            "Jéssica,    Géssica,    JESIKA",
            "Hugo,       Ugo,        UGU",
            "Edson,      Édson,      EDISON",
            "Stefany,    Estefany,   ESTEFANI",
            "Moura,      Mora,       MORA",
            "Ribeiro,    Ribero,     HIBERU",
            "Campos,     Canpos,     KANPUS",
            "Kelly,      Keli,       KELI",
            "Ana,        Anna,       ANA",
            "D'Ávila,    Dávila,     DAVILA",
            "júri,       jure,       JURI",
            "Brasil,     Brasiu,     BRAZIU",
            "Schmidt,    Ximidt,     XIMIDITI",
    })
    void spellingsThatSoundTheSameShareTheKey(String a, String b, String key) {
        assertEquals(key, encoder.encode(a));
        assertEquals(key, encoder.encode(b));
        assertTrue(encoder.isEncodeEqual(a, b));
    }

    @ParameterizedTest(name = "{0} ≠ {1}")
    @CsvSource({
            "caro,  carro", // tepe vs vibrante (section 4.3, pair 6)
            "soco,  choco", // s vs ʃ (section 4.2)
            "pala,  palha", // l vs ʎ (pair 5)
            "lema,  lenha", // m vs ɲ (pair 4)
            "pato,  bato",  // voicing (pair 1)
            "cata,  canta", // oral vs nasal vowel (section 5.2)
            "mão,   Manu",  // nasal diphthong vs n + vowel
            "tora,  torra",
            "casa,  caça",  // z vs s in onset (section 5.2)
    })
    void minimalPairsKeepDistinctKeys(String a, String b) {
        assertNotEquals(encoder.encode(a), encoder.encode(b));
        assertFalse(encoder.isEncodeEqual(a, b));
    }

    @Test
    void foldingUnstressedVowelsMergesPretonicRaising() {
        assertNotEquals(encoder.encode("Felipe"), encoder.encode("Filipe"));
        BrazilianPortuguesePhonetic folding = BrazilianPortuguesePhonetic.builder().foldUnstressedVowels().build();
        assertEquals("FILIPI", folding.encode("Felipe"));
        assertEquals("FILIPI", folding.encode("Filipe"));
        assertEquals(folding.encode("Geovana"), folding.encode("Giovana"));
    }

    @Test
    void namesDropParticles() {
        assertEquals("MARIA SIUVA", encoder.encode("Maria da Silva"));
        assertEquals("JOAUN SANTUS", encoder.encode("João dos Santos"));
        assertEquals("MARIA DA SIUVA",
                BrazilianPortuguesePhonetic.builder().keepParticles().build().encode("Maria da Silva"));
        assertEquals("DE", encoder.encode("de")); // a lone particle is kept (a monosyllable is stressed)
    }

    @Test
    void options() {
        assertEquals("XAV", BrazilianPortuguesePhonetic.builder().maxWordLength(3).build().encode("Xavier"));
        assertEquals("SOUZA", BrazilianPortuguesePhonetic.builder().keepDiphthongs().build().encode("Souza"));
        assertEquals("VIKITOR",
                BrazilianPortuguesePhonetic.builder().keepEtymologicalSpelling().build().encode("Victor"));
        assertThrows(IllegalArgumentException.class, () -> BrazilianPortuguesePhonetic.builder().maxWordLength(-1));
    }

    @Test
    void commonsCodecContract() throws EncoderException {
        assertNull(encoder.encode((String) null));
        assertNull(encoder.encode((Object) null));
        assertEquals("", encoder.encode(""));
        assertEquals("", encoder.encode("  123 - "));
        assertEquals("LUIS", encoder.encode((Object) "Luiz"));
        assertThrows(EncoderException.class, () -> encoder.encode((Object) 42));

        StringEncoder asCommons = encoder;
        assertEquals("SOZA", asCommons.encode("SOUZA"));
    }

    @Test
    void sortsWithStringEncoderComparator() {
        List<String> names = new ArrayList<>(List.of("Xavier", "Abraham", "Kátia"));
        names.sort(new StringEncoderComparator(encoder)::compare);
        assertEquals(List.of("Abraham", "Kátia", "Xavier"), names);
    }
}
