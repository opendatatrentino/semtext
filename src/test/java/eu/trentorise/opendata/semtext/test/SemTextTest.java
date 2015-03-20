package eu.trentorise.opendata.semtext.test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import eu.trentorise.opendata.commons.Dict;
import eu.trentorise.opendata.commons.OdtConfig;
import eu.trentorise.opendata.semtext.MeaningKind;
import eu.trentorise.opendata.semtext.Meaning;
import eu.trentorise.opendata.semtext.MeaningStatus;
import eu.trentorise.opendata.semtext.SemText;
import eu.trentorise.opendata.semtext.SemTexts;
import eu.trentorise.opendata.semtext.Sentence;
import eu.trentorise.opendata.semtext.Term;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    public void testTermIteratorOneSentenceZeroTerms() {

        assertFalse(SemText.of(Locale.FRENCH, "abcde", Sentence.of(0, 1))
                .terms().iterator().hasNext());
        try {
            SemText.of(Locale.FRENCH, "abcde", Sentence.of(0, 1))
                    .terms().iterator().next();
            Assert.fail("Should have found no term!");
        }
        catch (NoSuchElementException ex) {

        }
    }

    @Test
    public void testTermIteratorOneSentenceOneTerm() {
        assertTrue(SemText.of(Locale.FRENCH, "abcde", MeaningStatus.REVIEWED, Meaning.of("a", MeaningKind.ENTITY, 0.2))
                .terms().iterator().hasNext());

        Iterator<Term> termIter = SemText.of(Locale.FRENCH, "abcde", MeaningStatus.REVIEWED, Meaning.of("a", MeaningKind.ENTITY, 0.2))
                .terms().iterator();
        Term term = termIter.next();
        assertEquals("a", term.getSelectedMeaning().getId());
        try {
            assertFalse(termIter.hasNext());
            termIter.next();
            Assert.fail("Should have found no term!");
        }
        catch (NoSuchElementException ex) {

        }
    }

    @Test
    public void testTermIteratorOneSentenceTwoTerms() {

        SemText st = SemText.of(Locale.FRENCH, "abcde", Sentence.of(0, 7,
                ImmutableList.of(Term.of(0, 1, MeaningStatus.SELECTED, Meaning.of("a", MeaningKind.CONCEPT, 0.2)),
                        Term.of(2, 3, MeaningStatus.SELECTED, Meaning.of("b", MeaningKind.CONCEPT, 0.2)))));

        Iterator<Term> termIter
                = st.terms().iterator();
        Term term1 = termIter.next();
        assertEquals("a", term1.getSelectedMeaning().getId());
        Term term2 = termIter.next();
        assertEquals("b", term2.getSelectedMeaning().getId());

        try {
            assertFalse(termIter.hasNext());
            termIter.next();
            Assert.fail("Should have found no term!");
        }
        catch (NoSuchElementException ex) {

        }
    }

    @Test
    public void testTermIteratorTwoSentencesFourTerms() {

        assertTrue(SemText.of().terms().isEmpty());

        Sentence s1 = Sentence.of(0, 5,
                ImmutableList.of(Term.of(0, 1, MeaningStatus.SELECTED, Meaning.of("a", MeaningKind.CONCEPT, 0.2)),
                        Term.of(2, 3, MeaningStatus.SELECTED, Meaning.of("b", MeaningKind.CONCEPT, 0.2))));

        Sentence s2 = Sentence.of(6, 10,
                ImmutableList.of(Term.of(6, 7, MeaningStatus.SELECTED, Meaning.of("c", MeaningKind.CONCEPT, 0.2)),
                        Term.of(8, 9, MeaningStatus.SELECTED, Meaning.of("d", MeaningKind.CONCEPT, 0.2))));

        SemText st = SemText.ofSentences(Locale.FRENCH, "abcdefghilmnopqrs", ImmutableList.of(s1, s2));

        Iterator<Term> termIter
                = st.terms().iterator();
        Term term1 = termIter.next();
        assertEquals("a", term1.getSelectedMeaning().getId());
        Term term2 = termIter.next();
        assertEquals("b", term2.getSelectedMeaning().getId());
        Term term3 = termIter.next();
        assertEquals("c", term3.getSelectedMeaning().getId());
        Term term4 = termIter.next();
        assertEquals("d", term4.getSelectedMeaning().getId());

        try {
            assertFalse(termIter.hasNext());
            termIter.next();
            Assert.fail("Should have found no term!");
        }
        catch (NoSuchElementException ex) {

        }

        assertEquals("a", st.terms().get(0).getSelectedMeaning().getId());

        assertEquals("b", st.terms().get(1).getSelectedMeaning().getId());
        assertEquals("c", st.terms().get(2).getSelectedMeaning().getId());
        assertEquals("d", st.terms().get(3).getSelectedMeaning().getId());

        assertTrue(st.terms().contains(term3));
        Term outsideTerm = Term.of(55, 66, MeaningStatus.NOT_SURE, null);
        assertFalse(st.terms().contains(outsideTerm));

        assertTrue(st.terms().containsAll(ImmutableList.of(term2, term3)));
        assertFalse(st.terms().containsAll(ImmutableList.of(outsideTerm)));

        assertEquals(4, st.terms().size());

        try {
            st.terms().get(4);
            Assert.fail("Should had exceeded bounds!");
        }
        catch (IndexOutOfBoundsException ex) {

        }

        try {
            st.terms().get(-1);
            Assert.fail("Should have exceeded bounds!");
        }
        catch (IndexOutOfBoundsException ex) {

        }

    }

    @Test
    public void testEquality() {

        assertNotEquals(Meaning.of("a", MeaningKind.CONCEPT, 0.1), Meaning.of("a", MeaningKind.ENTITY, 0.1));
        assertNotEquals(Meaning.of("a", MeaningKind.CONCEPT, 0.1), Meaning.of("b", MeaningKind.CONCEPT, 0.1));
        assertEquals(Meaning.of("a", MeaningKind.CONCEPT, 0.1), Meaning.of("a", MeaningKind.CONCEPT, 0.9));

        assertEquals(SemText.of(), SemText.of());
        assertEquals(SemText.of("a"), SemText.of("a"));

        assertNotEquals(SemText.of("a"), SemText.of("b"));
        assertEquals(SemText.of(Locale.ITALIAN, "a"), SemText.of(Locale.ITALIAN, "a"));

        Sentence s1 = Sentence.of(0, 2);
        Sentence s2 = Sentence.of(0, 2);

        assertEquals(SemText.of(Locale.ITALIAN, "ab", s1), SemText.of(Locale.ITALIAN, "ab", s2));
        assertNotEquals(SemText.of(Locale.ITALIAN, "ab", s1), SemText.of(Locale.ITALIAN, "ab"));

        assertEquals(Term.of(0, 2, MeaningStatus.NOT_SURE, null, ImmutableList.<Meaning>of()),
                Term.of(0, 2, MeaningStatus.NOT_SURE, null, ImmutableList.<Meaning>of()));
        assertNotEquals(Term.of(0, 2, MeaningStatus.NOT_SURE, null, ImmutableList.<Meaning>of()),
                Term.of(0, 3, MeaningStatus.NOT_SURE, null, ImmutableList.<Meaning>of()));

    }

    @Test
    public void testWith() {

        assertEquals("a",
                Term.of(0, 1, MeaningStatus.NOT_SURE, null)
                .with(MeaningStatus.SELECTED, Meaning.of("a", MeaningKind.CONCEPT, 0.2))
                .getSelectedMeaning()
                .getId());

        assertEquals("b", SemText.of("a").with("b").getText());
        assertEquals(Locale.ITALIAN, SemText.of("a").with(Locale.ITALIAN).getLocale());

        assertEquals("b", SemText.of("").withMetadata("a", "b").getMetadata("a"));

        assertEquals("c", SemText.of("").withMetadata("a", "b")
                .withMetadata("a", "c").getMetadata("a"));

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

        SemText newText = st.delete(ImmutableList.of(Range.closedOpen(0, 1)));
        assertEquals(1, newText.terms().size());
        assertEquals(t2, newText.terms().get(0));
    }

    /**
     * <pre>
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
        SemText newText = st.delete(ImmutableList.of(Range.closed(0, 0)));
        assertEquals(0, newText.terms().size());
    }

    /**
     * This won't delete anything!
     * <pre>
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
        SemText newText = st.delete(ImmutableList.of(Range.closedOpen(0, 0)));
        assertEquals(1, newText.terms().size());
    }

    /**
     * <pre>
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

        SemText newText = st.delete(ImmutableList.of(Range.closedOpen(0, 2)));
        assertEquals(0, newText.terms().size());
    }

    /**
     * <pre>
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

        SemText newText = st.delete(ImmutableList.of(Range.closed(0, 1)));
        assertEquals(0, newText.terms().size());
    }

    @Test
    public void testTerms() {
        try {
            Term.of(-1, 1, MeaningStatus.NOT_SURE, null);
            Assert.fail("Terms can't have negative start");
        }
        catch (Exception ex) {

        }

        try {
            Term.of(0, 1, MeaningStatus.SELECTED, null);
            Assert.fail("Terms can't have SELECTED meaning with null meaning");
        }
        catch (Exception ex) {

        }

        try {
            Term.of(0, 1, MeaningStatus.NOT_SURE, null, null);
            Assert.fail("Terms can't have null meanings");
        }
        catch (Exception ex) {

        }

    }

    @Test
    public void testProbNormalization() {
        Term t = Term.of(0, 1, MeaningStatus.NOT_SURE, null, ImmutableList.of(Meaning.of("a", MeaningKind.ENTITY, 0.2)));
        double prob = t.getMeanings().get(0).getProbability();
        assertTrue("prob should be near 1.0, found instead: " + prob, 1.0 - SemTexts.TOLERANCE <= prob && prob <= 1.0 + SemTexts.TOLERANCE);
    }

    /**
     * Usage example for the docs
     */
    @Test
    public void example() {

        // Objects only support factory methods starting with 'of':
        SemText.of(Locale.ITALIAN, "ciao");

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
        } catch(IllegalArgumentException ex){
            
        }
    }

}
