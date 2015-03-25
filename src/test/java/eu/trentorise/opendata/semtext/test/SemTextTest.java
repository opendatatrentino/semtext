/* 
 * Copyright 2015 TrentoRISE  (trentorise.eu) .
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.trentorise.opendata.semtext.test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import eu.trentorise.opendata.commons.Dict;
import eu.trentorise.opendata.commons.NotFoundException;
import eu.trentorise.opendata.commons.OdtConfig;
import eu.trentorise.opendata.semtext.MeaningKind;
import eu.trentorise.opendata.semtext.Meaning;
import eu.trentorise.opendata.semtext.MeaningStatus;
import eu.trentorise.opendata.semtext.SemText;
import eu.trentorise.opendata.semtext.SemTexts;
import eu.trentorise.opendata.semtext.Sentence;
import eu.trentorise.opendata.semtext.Term;
import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author David Leoni
 */
public class SemTextTest {

    private static final Logger LOG = Logger.getLogger(SemTextTest.class.getName());

    @BeforeClass
    public static void beforeClass() {
        OdtConfig.of(SemTextTest.class).loadLogConfig();
    }

    @Test
    @SuppressWarnings({"IncompatibleEquals", "ObjectEqualsNull"})
    public void testEquality() {

        assertEquals(SemText.of(), SemText.of());
        assertEquals(SemText.of("a"), SemText.of("a"));

        assertNotEquals(SemText.of("a"), SemText.of("b"));
        assertEquals(SemText.of(Locale.ITALIAN, "a"), SemText.of(Locale.ITALIAN, "a"));

        Sentence s1 = Sentence.of(0, 2);
        Sentence s2 = Sentence.of(0, 2);

        SemText st1 = SemText.of(Locale.ITALIAN, "ab", s1);
        SemText st2 = SemText.of(Locale.ITALIAN, "ab", s2);

        assertEquals(st1, st2);
        assertEquals(st1.hashCode(), st2.hashCode());

        assertNotEquals(SemText.of(Locale.ITALIAN, "ab", s1), SemText.of(Locale.ITALIAN, "ab"));

        assertFalse(s1.equals(null));
        assertFalse(s1.equals(""));

    }

    @Test
    public void testSemText() {

        assertEquals("a",
                Term.of(0, 1, MeaningStatus.NOT_SURE, null)
                .with(MeaningStatus.SELECTED, Meaning.of("a", MeaningKind.CONCEPT, 0.2))
                .getSelectedMeaning()
                .getId());

        assertEquals("b", SemText.of("a").with("b").getText());

        assertEquals(Locale.ITALIAN, SemText.of("a").with(Locale.ITALIAN).getLocale());
        assertEquals(Locale.ROOT, SemText.of(null, "a").getLocale()); // for nasty Locale json deserializers...

        assertFalse(SemText.of().hasMetadata("a"));
        assertEquals("b", SemText.of("").withMetadata("a", "b").getMetadata("a"));
        assertEquals("c", SemText.of("").withMetadata("a", "b").withMetadata("a", "c").getMetadata("a"));
        assertTrue("b", SemText.of("").withMetadata("a", "b").hasMetadata("a"));

        assertEquals("c", SemText.of("").withMetadata("a", "b")
                .withMetadata("a", "c").getMetadata("a"));

        try {
            SemText.of().getMetadata("blabla");
            Assert.fail();
        }
        catch (NotFoundException ex) {

        }

        try {
            SemText.of().withSentences(ImmutableList.of(Sentence.of(0, 1)));
            Assert.fail();
        }
        catch (IllegalArgumentException ex) {

        }

        Term t2 = Term.of(0, 1, MeaningStatus.NOT_SURE, null);
        SemText newSemText = SemText.ofTerms(Locale.ITALIAN, "abc", Term.of(0, 2, MeaningStatus.NOT_SURE, null))
                .withTerms(ImmutableList.of(t2));
        assertEquals(1, newSemText.terms().size());
        assertEquals(t2, newSemText.terms().get(0));

        try {
            SemText.of(Locale.ITALIAN, "abc", Sentence.of(0, 3)).with("ab");
            Assert.fail();
        }
        catch (IllegalArgumentException ex) {

        }

        assertEquals(SemText.of(Locale.ITALIAN, "ab", Sentence.of(0, 1)),
                SemText.of(Locale.ITALIAN, "abc", Sentence.of(0, 1)).with("ab"));

        assertEquals(SemText.ofSentences(Locale.ITALIAN, "a", ImmutableList.of(Sentence.of(0, 1, Term.of(0, 1, MeaningStatus.NOT_SURE, null)))),
                SemText.of(Locale.ITALIAN, "a", MeaningStatus.NOT_SURE, null, ImmutableList.<Meaning>of()));

        assertTrue(SemText.of().toString().length() > 0);

        assertEquals(Dict.of(Locale.ITALIAN, "a"), SemText.of(Locale.ITALIAN, "a").asDict());
        assertEquals(SemText.of(Locale.ITALIAN, "a"), SemText.of(SemText.of(Locale.ITALIAN, "a").asLocalizedString()));
        assertEquals(SemText.of(Locale.ITALIAN, "a"), SemText.of(Dict.of(Locale.ITALIAN, "a"), Locale.CANADA));
        assertEquals(SemText.of(Locale.ITALIAN, "a"), SemText.of(Dict.of(Locale.ITALIAN, "a"), ImmutableList.of(Locale.CANADA)));
        assertEquals(Dict.of(Locale.ITALIAN, "a"), SemText.of(Locale.ITALIAN, "a").asDict());
    }

    /**
     * One term replaced with another
     *
     * <pre>
     *
     *  N
     *  E
     *  0
     *
     * </pre>
     */
    @Test
    public void testUpdateSemText_1() {
        Meaning ma = Meaning.of("a", MeaningKind.ENTITY, 0.3);

        ImmutableList<Term> terms = ImmutableList.of(Term.of(0,
                1,
                MeaningStatus.SELECTED,
                ma,
                ImmutableList.of(ma)));

        Meaning mb = Meaning.of("b", MeaningKind.ENTITY, 0.3);

        Term newTerm = Term.of(0, 1,
                MeaningStatus.SELECTED,
                mb,
                ImmutableList.of(Meaning.of("c", MeaningKind.ENTITY, 0.3)));

        SemText semText = SemText.ofTerms(Locale.ITALIAN, "t", terms);
        SemText updated = semText.merge(newTerm);
        assertEquals(1, updated.terms().size());
        assertEquals(mb, updated.terms().get(0).getSelectedMeaning());

        // meanings should be merged
        assertEquals(2, updated.terms().get(0).getMeanings().size());
    }

    /**
     * <pre>
     *
     * One new term, two existing terms, deletes both
     *
     *   N1N1
     * E1E1E2E2
     * 0 1 2 3
     *
     * </pre>
     */
    @Test
    public void testUpdateSemText_2() {

        ImmutableList<Term> terms = ImmutableList.of(Term.of(0, 2, MeaningStatus.TO_DISAMBIGUATE, null),
                Term.of(2, 4, MeaningStatus.TO_DISAMBIGUATE, null));

        Term newTerm = Term.of(1, 3, MeaningStatus.TO_DISAMBIGUATE, null);

        SemText semText = SemText.ofTerms(Locale.ITALIAN, "abcd", terms);
        SemText updatedSemText = semText.merge(newTerm);
        assertEquals(1, updatedSemText.terms().size());
        assertEquals(newTerm, updatedSemText.terms().get(0));

    }

    /**
     * <pre>
     *
     * One new term, two existing terms, deletes first
     *
     * N1
     * E1E1E2E2
     * 0 1 2 3
     *
     * </pre>
     */
    @Test
    public void testUpdateSemText_3() {
        ImmutableList<Term> origTerms = ImmutableList.of(Term.of(0, 2, MeaningStatus.SELECTED, Meaning.of("a", MeaningKind.CONCEPT, 0.4)),
                Term.of(2, 4, MeaningStatus.SELECTED, Meaning.of("b", MeaningKind.ENTITY, 0.4)));
        Term newTerm = Term.of(0, 1, MeaningStatus.SELECTED, Meaning.of("c", MeaningKind.CONCEPT, 0.4));

        SemText semText = SemText.ofTerms(Locale.ITALIAN, "abcd", origTerms);
        SemText updatedSemText = semText.merge(newTerm);
        assertEquals(2, updatedSemText.terms().size());
        Iterator<Term> wi = updatedSemText.terms().iterator();
        assertEquals(newTerm, wi.next());
        assertEquals(origTerms.get(1), wi.next());
    }

    /**
     * <pre>
     *
     * One new term, two existing terms, deletes second
     *
     *     N1
     * E1E1E2E2
     * 0 1 2 3
     *
     * </pre>
     */
    @Test
    public void testUpdateSemText_4() {
        ImmutableList<Term> terms = ImmutableList.of(Term.of(0, 2, MeaningStatus.SELECTED, Meaning.of("a", MeaningKind.CONCEPT, 0.4)),
                Term.of(2, 4, MeaningStatus.SELECTED, Meaning.of("b", MeaningKind.ENTITY, 0.4)));
        Term newTerm = Term.of(2, 3, MeaningStatus.SELECTED, Meaning.of("c", MeaningKind.CONCEPT, 0.4));

        SemText semText = SemText.ofTerms(Locale.ITALIAN, "abcd", terms);
        SemText updatedSemText = semText.merge(newTerm);
        assertEquals(2, updatedSemText.terms().size());
        Iterator<Term> wi = updatedSemText.terms().iterator();
        assertEquals("a", wi.next().getSelectedMeaning().getId());
        assertEquals("c", wi.next().getSelectedMeaning().getId());
    }

    /**
     * <pre>
     *
     * two sentences, no existing term, two new terms. first term exceeds sentence thus is discarded
     *
     * S1  S2
     * N1N1N2
     * --------
     * 0 1 2 3
     *
     * </pre>
     */
    @Test
    public void testUpdateSemTextTwoSentences() {
        Term newTerm1 = Term.of(0, 2, MeaningStatus.SELECTED, Meaning.of("a", MeaningKind.CONCEPT, 0.4));
        Term newTerm2 = Term.of(2, 3, MeaningStatus.SELECTED, Meaning.of("a", MeaningKind.CONCEPT, 0.4));

        Sentence sentence1 = Sentence.of(0, 1);
        Sentence sentence2 = Sentence.of(2, 3);

        SemText semText = SemText.of(Locale.ITALIAN, "abcd", sentence1, sentence2);
        SemText updatedSemText = semText.merge(newTerm1, newTerm2);
        assertEquals(1, updatedSemText.terms().size());
        assertEquals(sentence1, updatedSemText.getSentences().get(0));
        ImmutableList<Term> termsSen2 = updatedSemText.getSentences().get(1).getTerms();
        assertEquals(1, termsSen2.size());
        assertEquals(newTerm2, termsSen2.get(0));
    }

    /**
     * <pre>
     * 012
     * ab
     * [)   t1
     *  [)  t2
     * [)   del
     * </pre>
     */
    @Test
    public void deleteFirstTermSemText_1() {

        Term t2 = Term.of(1, 2, MeaningStatus.NOT_SURE, null);
        SemText st = SemText.ofTerms(Locale.FRENCH, "ab", ImmutableList.<Term>of(
                Term.of(0, 1, MeaningStatus.NOT_SURE, null),
                t2
        ));

        SemText newText = st.deleteTerms(ImmutableList.of(Range.closedOpen(0, 1)));
        assertEquals(1, newText.terms().size());
        assertEquals(t2, newText.terms().get(0));

        assertEquals(newText, st.deleteTerms(Pattern.compile("a")));
    }

    /**
     * <pre>
     * 012
     * ab
     * [)   t1
     * [  del Note it is closed on the same point at 0
     * </pre>
     */
    @Test
    public void deletePartialRangeFromSemText_1() {
        SemText st = SemText.ofTerms(Locale.FRENCH, "ab",
                Term.of(0, 1, MeaningStatus.NOT_SURE, null));

        assertEquals(1, st.terms().size());
        SemText newText = st.deleteTerms(ImmutableList.of(Range.closed(0, 0)));
        assertEquals(0, newText.terms().size());

    }

    /**
     * This won't deleteTerms anything!
     * <pre>
     * 012
     * ab
     * [)   t1
     * )  del Note it is closed and open on the same point at 0. Seems like the 'open' side wins!
     * </pre>
     */
    @Test
    public void deletePartialRangeFromSemText_2() {
        SemText st = SemText.ofTerms(Locale.FRENCH, "ab",
                Term.of(0, 1, MeaningStatus.NOT_SURE, null));

        assertEquals(1, st.terms().size());
        SemText newText = st.deleteTerms(ImmutableList.of(Range.closedOpen(0, 0)));
        assertEquals(1, newText.terms().size());

        Pattern emptyPattern = Pattern.compile("");
        try {
            st.deleteTerms(emptyPattern);
        }
        catch (IllegalArgumentException ex) {

        }

    }

    /**
     * <pre>
     * 012
     * ab
     * [)   t1
     *  [)  t2
     * [-)  del
     * </pre>
     */
    @Test
    public void deleteEverythingFromSemText_1() {

        SemText st = SemText.ofTerms(Locale.FRENCH, "ab", ImmutableList.<Term>of(
                Term.of(0, 1, MeaningStatus.NOT_SURE, null),
                Term.of(1, 2, MeaningStatus.NOT_SURE, null)
        ));

        SemText newText = st.deleteTerms(ImmutableList.of(Range.closedOpen(0, 2)));
        assertEquals(0, newText.terms().size());

        assertEquals(newText, st.deleteTerms(Pattern.compile("ab")));
    }

    /**
     * <pre>
     * 012
     * ab
     * [)   t1
     *  [)  t2
     * []   del
     * </pre>
     */
    @Test
    public void deleteEverythingFromSemText_2() {

        SemText st = SemText.ofTerms(Locale.FRENCH, "ab", ImmutableList.<Term>of(
                Term.of(0, 1, MeaningStatus.NOT_SURE, null),
                Term.of(1, 2, MeaningStatus.NOT_SURE, null)
        ));

        SemText newText = st.deleteTerms(ImmutableList.of(Range.closed(0, 1)));
        assertEquals(0, newText.terms().size());
    }

    /**
     * Two sentences with one term per sentence. Deletes first term from first
     * sentence
     *
     * <pre>
     * 0123
     * abc
     * [)    t1
     *   [)  t2
     * [)    s1
     *  [ )  s2
     * []   del
     * </pre>
     */
    @Test
    public void testDeleteWith2Sentences() {
        Term t1 = Term.of(0, 1, MeaningStatus.NOT_SURE, null);
        Term t2 = Term.of(2, 3, MeaningStatus.NOT_SURE, null);

        Sentence s1 = Sentence.of(0, 1, t1);
        Sentence s2 = Sentence.of(1, 3, t2);

        SemText st = SemText.of(Locale.FRENCH, "abc", s1, s2);
        SemText newText = st.deleteTerms(ImmutableList.of(Range.closed(0, 1)));
        assertEquals(2, newText.getSentences().size());
        Sentence newSentence1 = newText.getSentences().get(0);
        assertEquals(0, newSentence1.getTerms().size());
        assertEquals(s2, newText.getSentences().get(1));
    }

    /**
     * Will delete pattern 'a', so term 3 should remain
     * <pre>
     * 012345
     * abaad
     * [)     t1
     *   [)   t2
     *     [) t3
     * </pre>
     */
    @Test
    public void testDeletePattern() {
        Term t3 = Term.of(4, 5, MeaningStatus.NOT_SURE, null);
        SemText st = SemText.ofTerms(Locale.FRENCH, "abaad", ImmutableList.<Term>of(
                Term.of(0, 1, MeaningStatus.NOT_SURE, null),
                Term.of(2, 3, MeaningStatus.NOT_SURE, null),
                t3
        ));
        SemText newST = st.deleteTerms(Pattern.compile("a"));
        assertEquals(1, newST.terms().size());
        assertEquals(t3, newST.terms().get(0));
    }

    /**
     * Usage example for the docs
     */
    @Test
    public void example() {

        // Objects only support factory methods starting with 'of':
        SemText semText1 = SemText.of(Locale.ITALIAN, "ciao");

        // New objects can be created using 'with' methods: 
        SemText semText2 = semText1.with("buongiorno");

        assert semText1.getText().equals("ciao");
        assert semText2.getText().equals("buongiorno");

        // Let's construct a SemText of one sentence and one term with SELECTED meaning.
        String text = "Welcome to Garda lake.";

        Meaning meaning = Meaning.of("http://someknowledgebase.org/entities/garda-lake", MeaningKind.ENTITY, 0.7);

        // we indicate the span where 'Garda lake' occurs
        Term term = Term.of(11, 21, MeaningStatus.SELECTED, meaning);

        // Language can be set only for the whole SemText:
        SemText semText = SemText.of(
                Locale.ENGLISH,
                text,
                Sentence.of(0, 26, term)); // sentence spans the whole text

        // only semText actually contains the text and terms can span many words
        assert "Garda lake".equals(semText.getText(term));

        // SemTexts class contains utilities like converters and checkers:
        ImmutableList<SemText> semtexts = SemTexts.dictToSemTexts(Dict.of(Locale.ITALIAN, "Ciao"));
        try {
            SemTexts.checkScore(1.7, "Invalid score!");
        }
        catch (IllegalArgumentException ex) {

        }
    }

}
