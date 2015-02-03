package eu.trentorise.opendata.semantics.nlp.test.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Range;
import eu.trentorise.opendata.commons.OdtConfig;
import eu.trentorise.opendata.semantics.nlp.model.MeaningKind;

import eu.trentorise.opendata.semantics.nlp.model.Meaning;
import eu.trentorise.opendata.semantics.nlp.model.MeaningStatus;
import eu.trentorise.opendata.semantics.nlp.model.SemText;
import eu.trentorise.opendata.semantics.nlp.model.SemTexts;
import eu.trentorise.opendata.semantics.nlp.model.Sentence;
import eu.trentorise.opendata.semantics.nlp.model.Term;
import eu.trentorise.opendata.semantics.nlp.model.TermIterator;
import java.util.Locale;
import java.util.NoSuchElementException;
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
    
    @BeforeClass
    public void beforeClass(){
        OdtConfig.of(this.getClass()).loadLogConfig();        
    }

    @Test
    public void testTermIteratorOneSentenceZeroTerms() {

        assertFalse(SemText.of("abcde", Locale.FRENCH, Sentence.of(0, 1))
                .terms().hasNext());
        try {
            SemText.of("abcde", Locale.FRENCH, Sentence.of(0, 1))
                    .terms().next();
            Assert.fail("Should have found no term!");
        }
        catch (NoSuchElementException ex) {

        }
    }

    @Test
    public void testTermIteratorOneSentenceOneTerm() {
        assertTrue(SemText.of("abcde", Locale.FRENCH, MeaningStatus.REVIEWED, Meaning.of("a", MeaningKind.ENTITY, 0.2))
                .terms().hasNext());

        TermIterator wi = SemText.of("abcde", Locale.FRENCH, MeaningStatus.REVIEWED, Meaning.of("a", MeaningKind.ENTITY, 0.2))
                .terms();
        Term w = wi.next();
        assertEquals("a", w.getSelectedMeaning().getId());
        try {
            assertFalse(wi.hasNext());
            wi.next();
            Assert.fail("Should have found no term!");
        }
        catch (NoSuchElementException ex) {

        }
    }

    @Test
    public void testTermIteratorOneSentenceTwoTerms() {

        SemText st = SemText.of("abcde", Locale.FRENCH,
                Sentence.of(0, 7,
                        ImmutableList.of(Term.of(0, 1, MeaningStatus.SELECTED, Meaning.of("a", MeaningKind.CONCEPT, 0.2)),
                                Term.of(2, 3, MeaningStatus.SELECTED, Meaning.of("b", MeaningKind.CONCEPT, 0.2)))));

        TermIterator wi
                = st.terms();
        Term w1 = wi.next();
        assertEquals("a", w1.getSelectedMeaning().getId());
        Term w2 = wi.next();
        assertEquals("b", w2.getSelectedMeaning().getId());

        try {
            assertFalse(wi.hasNext());
            wi.next();
            Assert.fail("Should have found no term!");
        }
        catch (NoSuchElementException ex) {

        }
    }

    @Test
    public void testTermIteratorTwoSentencesFourTerms() {

        Sentence s1 = Sentence.of(0, 5,
                ImmutableList.of(Term.of(0, 1, MeaningStatus.SELECTED, Meaning.of("a", MeaningKind.CONCEPT, 0.2)),
                        Term.of(2, 3, MeaningStatus.SELECTED, Meaning.of("b", MeaningKind.CONCEPT, 0.2))));

        Sentence s2 = Sentence.of(6, 10,
                ImmutableList.of(Term.of(6, 7, MeaningStatus.SELECTED, Meaning.of("c", MeaningKind.CONCEPT, 0.2)),
                        Term.of(8, 9, MeaningStatus.SELECTED, Meaning.of("d", MeaningKind.CONCEPT, 0.2))));

        SemText st = SemText.ofSentences("abcdefghilmnopqrs", Locale.FRENCH, ImmutableList.of(s1, s2));

        TermIterator wi
                = st.terms();
        Term w1 = wi.next();
        assertEquals("a", w1.getSelectedMeaning().getId());
        Term w2 = wi.next();
        assertEquals("b", w2.getSelectedMeaning().getId());
        Term w3 = wi.next();
        assertEquals("c", w3.getSelectedMeaning().getId());
        Term w4 = wi.next();
        assertEquals("d", w4.getSelectedMeaning().getId());

        try {
            assertFalse(wi.hasNext());
            wi.next();
            Assert.fail("Should have found no term!");
        }
        catch (NoSuchElementException ex) {

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
        assertEquals(SemText.of("a", Locale.ITALY), SemText.of("a", Locale.ITALY));

        Sentence s1 = Sentence.of(0, 2);
        Sentence s2 = Sentence.of(0, 2);

        assertEquals(SemText.of("ab", Locale.ITALY, s1), SemText.of("ab", Locale.ITALY, s2));
        assertNotEquals(SemText.of("ab", Locale.ITALY, s1), SemText.of("ab", Locale.ITALY));

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

        SemText semText = SemText.ofTerms("t", Locale.ITALIAN, terms);
        SemText updated = semText.merge(newTerm);
        assertEquals(1, Iterators.size(updated.terms()));
        assertEquals(mb, updated.terms().next().getSelectedMeaning());

        // meanings should be merged
        assertEquals(2, updated.terms().next().getMeanings().size());
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

        SemText semText = SemText.ofTerms("abcd", Locale.ITALIAN, terms);
        SemText updatedSemText = semText.merge(newTerm);
        assertEquals(1, Iterators.size(updatedSemText.terms()));
        assertEquals(newTerm, updatedSemText.terms().next());

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

        SemText semText = SemText.ofTerms("abcd", Locale.ITALIAN, origTerms);
        SemText updatedSemText = semText.merge(newTerm);
        assertEquals(2, Iterators.size(updatedSemText.terms()));
        TermIterator wi = updatedSemText.terms();
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

        SemText semText = SemText.ofTerms("abcd", Locale.ITALIAN, terms);
        SemText updatedSemText = semText.merge(newTerm);
        assertEquals(2, Iterators.size(updatedSemText.terms()));
        TermIterator wi = updatedSemText.terms();
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
        SemText st = SemText.ofTerms("ab", Locale.FRENCH, ImmutableList.<Term>of(
                Term.of(0, 1, MeaningStatus.NOT_SURE, null),
                t2
        ));

        SemText newText = st.delete(ImmutableList.of(Range.closedOpen(0, 1)));
        assertEquals(1, Iterators.size(newText.terms()));
        assertEquals(t2, newText.terms().next());
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
        SemText st = SemText.ofTerms("ab", Locale.FRENCH,
                Term.of(0, 1, MeaningStatus.NOT_SURE, null));

        assertEquals(1, Iterators.size(st.terms()));
        SemText newText = st.delete(ImmutableList.of(Range.closed(0, 0)));
        assertEquals(0, Iterators.size(newText.terms()));
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
        SemText st = SemText.ofTerms("ab", Locale.FRENCH,
                Term.of(0, 1, MeaningStatus.NOT_SURE, null));

        assertEquals(1, Iterators.size(st.terms()));
        SemText newText = st.delete(ImmutableList.of(Range.closedOpen(0, 0)));
        assertEquals(1, Iterators.size(newText.terms()));
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

        SemText st = SemText.ofTerms("ab", Locale.FRENCH, ImmutableList.<Term>of(
                Term.of(0, 1, MeaningStatus.NOT_SURE, null),
                Term.of(1, 2, MeaningStatus.NOT_SURE, null)
        ));

        SemText newText = st.delete(ImmutableList.of(Range.closedOpen(0, 2)));
        assertEquals(0, Iterators.size(newText.terms()));
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

        SemText st = SemText.ofTerms("ab", Locale.FRENCH, ImmutableList.<Term>of(
                Term.of(0, 1, MeaningStatus.NOT_SURE, null),
                Term.of(1, 2, MeaningStatus.NOT_SURE, null)
        ));

        SemText newText = st.delete(ImmutableList.of(Range.closed(0, 1)));
        assertEquals(0, Iterators.size(newText.terms()));
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
    public void testProbNormalization(){        
        Term t = Term.of(0,1,MeaningStatus.NOT_SURE, null,ImmutableList.of(Meaning.of("a", MeaningKind.ENTITY, 0.2)));
        double prob = t.getMeanings().get(0).getProbability();
        assertTrue("prob should be near 1.0, found instead: " + prob, 1.0 - SemTexts.TOLERANCE <= prob && prob <= 1.0 + SemTexts.TOLERANCE);        
    }

}
