package br.codec.language.ptbr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.codec.EncoderException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BrazilianPortugueseSoundexTest {

    private final BrazilianPortugueseSoundex soundex = new BrazilianPortugueseSoundex();

    @ParameterizedTest(name = "{0} = {1}")
    @CsvSource({
            "Gonçalves, G654",
            "Gonsalves, G654",
            "Xavier,    X470",
            "Chavier,   X470",
            "Luiz,      L500",
            "Luís,      L500",
            "Ana,       A600",
            "Thiago,    T300",
            "cata,      K200",
            "canta,     K620",
            "Pereira,   P770",
            "Ferreira,  F770",
    })
    void codes(String word, String code) {
        assertEquals(code, soundex.encode(word));
    }

    @Test
    void similarSoundsShareTheirDigit() {
        // voicing pairs and the rhotics fall in one group (section 4.3): only the first letter tells them apart
        assertEquals(soundex.encode("Bato").substring(1), soundex.encode("Pato").substring(1));
        assertEquals(soundex.encode("caro"), soundex.encode("carro"));
        assertNotEquals(soundex.encode("cata"), soundex.encode("canta"));
    }

    @Test
    void difference() {
        assertEquals(4, soundex.difference("Souza", "Sousa"));
        assertEquals(4, soundex.difference("Pereira", "Pereyra"));
        assertEquals(3, soundex.difference("Pereira", "Ferreira"));
        assertEquals(0, soundex.difference("", "Ana"));
    }

    @Test
    void wordsAndContract() throws EncoderException {
        assertEquals("M700 S400", soundex.encode("Maria da Silva"));
        assertNull(soundex.encode((String) null));
        assertEquals("", soundex.encode(""));
        assertThrows(EncoderException.class, () -> soundex.encode((Object) 1L));
    }
}
