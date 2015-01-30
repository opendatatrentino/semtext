package eu.trentorise.opendata.semantics.nlp.test.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import eu.trentorise.opendata.semantics.nlp.model.MeaningKind;

import eu.trentorise.opendata.semantics.nlp.model.Meaning;
import eu.trentorise.opendata.semantics.nlp.model.MeaningStatus;
import eu.trentorise.opendata.semantics.nlp.model.SemText;
import eu.trentorise.opendata.semantics.nlp.model.Sentence;
import eu.trentorise.opendata.semantics.nlp.model.Term;
import eu.trentorise.opendata.semantics.nlp.model.TermIterator;
import java.util.ArrayList;
import java.util.Locale;
import java.util.NoSuchElementException;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author David Leoni
 */
public class SemTextTest {

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
        assertTrue(SemText.of("abcde", Locale.FRENCH, MeaningStatus.REVIEWED, Meaning.of("a", 0.2, MeaningKind.ENTITY))
                .terms().hasNext());

        TermIterator wi = SemText.of("abcde", Locale.FRENCH, MeaningStatus.REVIEWED, Meaning.of("a", 0.2, MeaningKind.ENTITY))
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
                        ImmutableList.of(Term.of(0, 1, MeaningStatus.SELECTED, Meaning.of("a",0.2,MeaningKind.CONCEPT)),
                                Term.of(2, 3, MeaningStatus.SELECTED, Meaning.of("b",0.2,MeaningKind.CONCEPT)))));

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
                        ImmutableList.of(Term.of(0, 1, MeaningStatus.SELECTED, Meaning.of("a",0.2,MeaningKind.CONCEPT)),
                                Term.of(2, 3, MeaningStatus.SELECTED, Meaning.of("b",0.2,MeaningKind.CONCEPT))));

        Sentence s2 = Sentence.of(6, 10,
                        ImmutableList.of(Term.of(6, 7, MeaningStatus.SELECTED, Meaning.of("c",0.2,MeaningKind.CONCEPT)),
                                Term.of(8, 9,  MeaningStatus.SELECTED, Meaning.of("d",0.2, MeaningKind.CONCEPT))));        
        
        SemText st = SemText.ofSentences("abcdefghilmnopqrs", Locale.FRENCH,ImmutableList.of(s1, s2));

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

        assertNotEquals(Meaning.of("a", 0.1, MeaningKind.CONCEPT), Meaning.of("a", 0.1, MeaningKind.ENTITY));
        assertNotEquals(Meaning.of("a", 0.1, MeaningKind.CONCEPT), Meaning.of("b", 0.1, MeaningKind.CONCEPT));
        assertEquals(Meaning.of("a", 0.1, MeaningKind.CONCEPT), Meaning.of("a", 0.9, MeaningKind.CONCEPT));

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
                .with(MeaningStatus.SELECTED, Meaning.of("a", 0.2, MeaningKind.CONCEPT))
                .getSelectedMeaning()
                .getId());

        assertEquals("a",
                SemText.of()
                .with(MeaningStatus.SELECTED, Meaning.of("a", 0.0, MeaningKind.ENTITY))
                .getTerm()                
                .getSelectedMeaning()
                .getId());

        assertEquals("b", SemText.of("a").with("b").text());
        assertEquals(Locale.ITALIAN, SemText.of("a").with(Locale.ITALIAN).getLocale());

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
        Meaning ma = Meaning.of("a", 0.3, MeaningKind.ENTITY);

        ImmutableList<Term> terms = ImmutableList.of(Term.of(0,
                        1,
                        ma,
                        ImmutableList.of(ma)));

        Meaning mb = Meaning.of("b", 0.3, MeaningKind.ENTITY);

        Term newTerm = Term.of(0, 1, Meaning.of());

        SemText semText = SemText.of("t", Locale.ITALIAN, terms);
        SemText updated = semText.update(newTerm);
        assertEquals(1, Iterators.size(updated.terms()));
        assertEquals(mb, updated.terms().next().getSelectedMeaning());

        // meanings should be merged
        assertEquals(2, updated.terms().next().getMeanings().size());
    }

    /**
     * <pre>

 One new term, two existing terms, deletes both

   N1N1
 E1E1E2E2
 0 1 2 3

 </pre>
     */
    @Test
    public void testUpdateSemText_2() {

        ImmutableList<Term> terms = ImmutableList.of(Term.of(0, 2, MeaningStatus.TO_DISAMBIGUATE, null),
                Term.of(2, 4, Meaning.of()));

        Term newTerm = Term.of(1, 3, Meaning.of());

        SemText semText = SemText.of("abcd", Locale.ITALIAN, terms);
        SemText updatedSemText = semText.update(newTerm);
        assertEquals(1, Iterators.size(updatedSemText.terms()));
        assertEquals(newTerm, updatedSemText.terms().next());

    }

    /**
     * <pre>

 One new term, two existing terms, deletes first

   N1
   E1E1E2E2
   0 1 2 3

 </pre>
     */
    @Test
    public void testUpdateSemText_3() {
        ImmutableList<Term> origTerms = ImmutableList.of(Term.of(0, 2, Meaning.of("a", 0.4, MeaningKind.CONCEPT)),
                Term.of(2, 4, Meaning.of("b", 0.4, MeaningKind.ENTITY)));
        Term newTerm = Term.of(0, 1, Meaning.of("a", 0.4, MeaningKind.CONCEPT));

        SemText semText = SemText.of("abcd", Locale.ITALIAN, origTerms);
        SemText updatedSemText = semText.update(newTerm);
        assertEquals(2, Iterators.size(updatedSemText.terms()));
        TermIterator wi = updatedSemText.terms();
        assertEquals(newTerm, wi.next());
        assertEquals(origTerms.get(1), wi.next());
    }

    /**
     * <pre>

 One new term, two existing terms, deletes second

       N1
   E1E1E2E2
   0 1 2 3

 </pre>
     */
    @Test
    public void testUpdateSemText_4() {
        ImmutableList<Term> terms = ImmutableList.of(Term.of(0, 2, Meaning.of("a", 0.4, MeaningKind.CONCEPT)),
                Term.of(2, 4, Meaning.of("b", 0.4, MeaningKind.ENTITY)));
        Term newTerm = Term.of(2, 3, Meaning.of("c", 0.4, MeaningKind.CONCEPT));

        SemText semText = SemText.of("abcd", Locale.ITALIAN, terms);
        SemText updatedSemText = semText.update(newTerm);
        assertEquals(2, Iterators.size(updatedSemText.terms()));
        TermIterator wi = updatedSemText.terms();
        assertEquals("a", wi.next().getSelectedMeaning().getId());
        assertEquals("c", wi.next().getSelectedMeaning().getId());
    }

}
