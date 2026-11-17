# commons-codec-ptbr

Brazilian Portuguese (PB) phonetic encoders for [Apache Commons Codec](https://commons.apache.org/proper/commons-codec/).
Commons Codec ships phonetic encoders for English and Germanic names: Soundex, Metaphone, NYSIIS, Cologne. Its
Beider-Morse encoder has a generic Portuguese rule set. None of them models the phonology of *Brazilian* Portuguese:
the coda archiphonemes, final vowel raising, the "r" forte, lh/nh. This library does.

| Class | What it gives |
|---|---|
| `BrazilianPortugueseTranscriber` | Spelling → phonemes → `/ˈkaSka/` (phonological) or `[ˈkaʃkɐ]` (phonetic, per dialect) |
| `BrazilianPortuguesePhonetic` | `StringEncoder`: phonetic matching key. `Luiz` = `Luís` → `LUIS` |
| `BrazilianPortugueseSoundex` | `StringEncoder`: letter + 3 digits over PB phonemes. `Gonçalves` → `G654` |
| `Dialect` | Presets for carioca, paulistano, florianopolitano, belo-horizontino, porto-alegrense, caipira, nordestino |

Requires Java 25 and commons-codec 1.21.0.

### Getting it

Releases are published to GitHub Packages. Each release also has a
[GitHub Release](https://github.com/thiagodemoura/commons-codec-ptbr/releases) with the jars attached.

```xml
<repositories>
    <repository>
        <id>github-commons-codec-ptbr</id>
        <url>https://maven.pkg.github.com/thiagodemoura/commons-codec-ptbr</url>
    </repository>
</repositories>

<dependency>
    <groupId>br.codec</groupId>
    <artifactId>commons-codec-ptbr</artifactId>
    <version>1.0.0</version>
</dependency>
```

GitHub Packages requires a login even for public packages. Add a `<server>` with the same id to
`~/.m2/settings.xml`. Use your GitHub user name and a token with the `read:packages` scope:

```xml
<server>
    <id>github-commons-codec-ptbr</id>
    <username>YOUR_GITHUB_USER</username>
    <password>YOUR_TOKEN</password>
</server>
```

To build it yourself instead: `mvn install`.

### Releases

[.github/workflows/release.yml](.github/workflows/release.yml) builds every pull request. On every push to `main`
it builds, deploys a version to GitHub Packages and creates a GitHub Release:

- if the pushed commit has a `vX.Y.Z` tag, that is the version. Push the tag before `main` to choose a minor or
  major bump.
- otherwise the version is the latest tag with its patch number plus one (1.0.0 → 1.0.1), and the workflow tags
  the commit.

The `<version>` in `pom.xml` is only used for local builds. The workflow sets the released version.

## Usage

```java
StringEncoder key = new BrazilianPortuguesePhonetic();
key.encode("Luiz");              // LUIS     (= Luís)
key.encode("Souza");             // SOZA     (= Sousa)
key.encode("Thiago");            // TIAGU    (= Tiago)
key.encode("Maria da Silva");    // MARIA SIUVA   (particles dropped)
key.encode("caro");              // KARU  ≠  carro KAHU

var pb = new BrazilianPortuguesePhonetic();
pb.encode("Gonçalves");          // GONSAUVIS (= Gonsalves)
pb.alternate("Gonçalves");       // GONKAUVIS (= Goncalves: ç as K, w as V, final -ez as -es)
pb.isEncodeEqual("Gonçalves", "Goncalves");   // true: they share a key
pb.isEncodeEqual("Wagner", "Vagner");         // true

var folding = BrazilianPortuguesePhonetic.builder().foldUnstressedVowels().build();
folding.encode("Felipe");        // FILIPI   (= Filipe)

new BrazilianPortugueseSoundex().encode("Xavier");   // X470 (= Chavier)

Transcription t = new BrazilianPortugueseTranscriber().transcribe("casca");
t.phonological();                    // /ˈkaSka/
t.phonetic(Dialect.CARIOCA);         // [ˈkaʃkɐ]
t.phonetic(Dialect.PAULISTANO);      // [ˈkaskɐ]
```

The encoders follow the Commons Codec contract. `encode(null)` is `null`, `encode(Object)` throws
`EncoderException` for non-strings, and they work with `StringEncoderComparator`. They are immutable and thread-safe.

## How the rules map to the reference

The phonology follows Seara, Nunes & Lazzarotto-Volcão, *Fonética e Fonologia do Português Brasileiro*
(UFSC, 2011). The letter-to-sound rules also use the Toda Matéria page
[Fonema e letra](https://www.todamateria.com.br/fonema-e-letra/).

| Rule | Source | Example |
|---|---|---|
| Digraphs ch, lh, nh, rr, ss, sc, sç, xc, qu, gu; initial/silent h | Toda Matéria; book §3.2 | *chuva* /ˈʃuva/, *hoje* /ˈOʒE/ |
| x = /ʃ/, /z/, /s/, /ks/ | Toda Matéria | *xícara* /ʃ/, *exalar* /z/, *sexto* /S/, *fixo* /ks/ |
| "r" fraco (tepe) vs "r" forte | §3.2, Quadro 18 | *caro* /ˈkaɾO/, *carro* /ˈkarO/, *honra*, *Israel* |
| Coda archiphonemes /S/, /R/, /N/ | §4.4, §5.2 | *casca* /ˈkaSka/, *mar* /ˈmaR/, *canta* /ˈkaNta/ |
| Mid-vowel archiphonemes /E/, /O/ (when the spelling has no accent) | §4.4 | *avó* /aˈvɔ/, *avô* /aˈvo/, *bolo* /ˈbOlO/ |
| Complex onsets (C + l/ɾ), illicit codas → epenthetic [i], prothesis before s + C | §5.1, §5.2, §6.1 | *prato*, *advogado* [adʒivoˈgadu], *Stefany* |
| Glide vs hiatus | §5.4 | *pais* /ˈpajS/, *raiz* /raˈiS/, *Luiz* = *Luís* |
| Stress from accents and word endings | §5.5 | *sábia*, *sabia*, *sabiá* |
| Final unstressed e, o → i, u (Regra II) | §6.2.1 | *júri* = *jure* [ˈʒuɾi] |
| Nasalization before a nasal consonant (Regra III) | §6.2.2 | *cama* [ˈkɐ̃mɐ] |
| Palatalization t, d → tʃ, dʒ before [i] (Regra IV) | §6.2.2 | *tia* [ˈtʃiɐ] (carioca) |
| Coda /S/ voicing (Regra VI) and sandhi | §6.2.3, Quadro 17 | *mesmo* [ˈmezmu], *lápis amarelo* [ˈlapiz ...] |
| Coda l: vocalized [w] or velarized [ɫ] | §3.2.4, Quadro 19 | *mal* [ˈmaw] / [ˈmaɫ] (Porto Alegre) |
| Open pretonic vowels | §4.4 | *menino* [mɛˈnĩnu] (nordestino) |
| Soundex groups = "sons foneticamente semelhantes" | §4.3 | p/b, t/d, k/g, f/v, s/z/ʃ/ʒ, m/n/ɲ, l/ʎ/ɾ/r |

Each `Dialect` preset documents which of its settings come from the book. Where the book says nothing, the preset
keeps the majority PB value, and the Javadoc marks that setting as "assumed".

## The matching key

The key keeps only what tells words apart in PB and drops the rest:

- **Merged.** The archiphonemes /S/, /R/, /N/, /E/, /O/. Final unstressed e/o, written I/U. Coda l (U). Glides (I/U).
  By default also ou → O and ei/ai before r, x, j → E/A (*Moura* = *Mora*, *Ribeiro* = *Ribero*).
- **Dropped.** Allophones: palatalization, nasalization before a nasal consonant, how each dialect realizes r, s and l.
- **Simplified etymological spellings.** ph, th, y, double consonants, cq, silent ct/pt, initial igna/igne (*Victor* =
  *Vitor*, *Baptista* = *Batista*, *Assumpção* = *Assunção*, *Jacqueline* = *Jaqueline*, *Ignez* = *Inês*).
- **Alternate key** for the spellings of records: ç as K (*Goncalves*), w as V (*Vagner*), final -ez as -es
  (*Rodriguez* = *Rodrigues*). Texts match when they share a key.

Tokens: `A E I O U`, `P B T D K G F V S Z M N L`, `X` = ʃ, `J` = ʒ, `Y` = ɲ (nh), `LH` = ʎ, `R` = ɾ or /R/,
`H` = "r" forte. A nasal vowel is its vowel followed by `N`, placed after its glide if it has one: *canta* `KANTA`,
*mão* `MAUN`, *Belém* `BELEIN`. An `N` followed by a vowel is the consonant n; any other `N` marks nasality.
Consecutive equal tokens are collapsed.

Builder options: `maxWordLength(n)`, `foldUnstressedVowels()`, `keepDiphthongs()`, `keepParticles()`,
`keepEtymologicalSpelling()`.

## Known limits

- **Stress comes from the spelling alone.** Unaccented words follow the accentuation rules, so names whose accent
  was lost are stressed elsewhere (*Sergio*, *Keli*). The primary key reads Spanish-style surnames in -ez as oxytones,
  like *talvez*; only the alternate key reads them as -es. `foldUnstressedVowels()` ignores stress for this reason.
- **The open and closed mid vowels are lost** in words without an accent (/E/, /O/). The spelling does not record them.
- **Intervocalic x defaults to /ʃ/.** /ks/ and /s/ are recognized only for a short list of stems (*fixo*, *táxi*,
  *sexo*, *próximo*, *máximo*, ...).
- **Not modeled:** stressed-vowel diphthongization (*três* [ˈtɾejs]) and vowel deletion across word boundaries.
- **Dropping silent ct/pt also drops** the pronounced c of common words (*pacto*). Use `keepEtymologicalSpelling()`
  for general vocabulary.

## Migrating

Keys are not stable across versions: re-encode the keys you store when you upgrade. Encoding is cheap, so recompute
all of them rather than guessing which changed.

| | 1.0.0 → 1.0.1 | 1.0.1 → 1.0.2 |
|---|---|---|
| `encode()` | unchanged | changed for cq (*Jacqueline*) and initial igna/igne (*Ignez*, *Ignácio*) |
| `encode()` with `foldUnstressedVowels()` | unchanged | changed for almost every word: stressed e/o fold too |
| `alternate()` | new: ç as K, w as V | also final -ez as -es (*Rodriguez*) |
| `isEncodeEqual()` | true when any key is shared (more pairs match) | unchanged |
| Soundex `encode()` | unchanged; `alternate()` and `isEncodeEqual()` new | changed for cq and initial igna/igne |
| Transcriptions | unchanged | changed for cq and initial igna/igne |

The igna/igne rule does not apply with `keepEtymologicalSpelling()`.

1. **Bump the dependency** to the new `<version>`.
2. **Re-encode stored keys** with the same builder options as before:
   ```java
   var pb = new BrazilianPortuguesePhonetic();
   for (Person p : repository.findAll()) {
       p.setNameKey(pb.encode(p.getName()));
       p.setNameAltKey(pb.alternate(p.getName()));
   }
   ```
3. **Store and search the alternate key** to match the spellings of records (*Goncalves*, *Vagner*, *Rodriguez*).
   Searching both columns for both keys is what `isEncodeEqual` does:
   ```sql
   ALTER TABLE person ADD name_alt_key VARCHAR(100);
   CREATE INDEX person_name_alt_key ON person (name_alt_key);

   SELECT * FROM person
   WHERE name_key IN (:key, :altKey) OR name_alt_key IN (:key, :altKey);
   ```
   Most names have no ç, w or final -ez; their alternate equals their key, and you may store `NULL` instead.
4. **Review code that relies on `isEncodeEqual`** (automatic merging of duplicates, for instance): since 1.0.1 it
   also matches through the alternate key (*Gonçalves* = *Goncalves*, *Wagner* = *Vagner*).
5. **Reconsider `foldUnstressedVowels()`.** Since 1.0.2 it also merges names that differ only in e/i or o/u
   (*Nelson* = *Nilson*, *Rosa* = *Rusa*). If that is too loose, use the default with the alternate key.
6. **Soundex** follows the same steps. `difference()` still compares the primary codes only.
