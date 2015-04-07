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
package eu.trentorise.opendata.semtext;

import com.google.common.collect.ImmutableList;
import eu.trentorise.opendata.commons.OdtConfig;
import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * For these tests to work we need to stay in same SemText package as TermIterator and TermView are not public
 * @author David Leoni
 */
public class TermIteratorTest {

    private static final Logger LOG = Logger.getLogger(TermIteratorTest.class.getName());

    @BeforeClass
    public static void beforeClass() {
        OdtConfig.of(TermIteratorTest.class);
    }
        
    @Test
    @SuppressWarnings({"ObjectEqualsNull", "IncompatibleEquals"})
    public void testTermViewEquality(){
        SemText st = SemText.ofTerms(Locale.ITALIAN, "blabla", Term.of(0,2,MeaningStatus.NOT_SURE, null));
        assertTrue(TermsView.of(st).equals(TermsView.of(st)));
        assertTrue(TermsView.of(st).hashCode() == TermsView.of(st).hashCode());
        assertFalse(TermsView.of(st).equals(null));
        assertFalse(TermsView.of(st).equals("ciao"));
    }    

      @Test
    public void testTermIteratorOneSentenceZeroTerms() {

        assertFalse(SemText.ofSentences(Locale.FRENCH, "abcde", Sentence.of(0, 1))
                .terms().iterator().hasNext());
        try {
            SemText.ofSentences(Locale.FRENCH, "abcde", Sentence.of(0, 1))
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

        SemText semText = SemText.of(Locale.FRENCH, "abcde", MeaningStatus.REVIEWED, Meaning.of("a", MeaningKind.ENTITY, 0.2));

        Iterator<Term> termIter = semText.terms().iterator();
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

        
        TermIterator titer1 = ((TermIterator)(SemText.of().terms().iterator()));
        assertFalse(titer1.hasCurrentTerm());
        assertFalse(titer1.hasCurrentSentence());
        try {
            titer1.term();
            Assert.fail();
        }
        catch (NoSuchElementException ex) {

        }

        try {
            titer1.sentence();
            Assert.fail();
        }
        catch (NoSuchElementException ex) {

        }
        
        assertEquals(Sentence.of(1,2), 
                     ((TermIterator) SemText.ofSentences(Locale.ITALIAN, "abcd", Sentence.of(1,2)).terms().iterator()).sentence());
        
        
        SemText st = SemText.ofSentences(Locale.FRENCH, "abcde", Sentence.of(0, 7,
                ImmutableList.of(Term.of(0, 1, MeaningStatus.SELECTED, Meaning.of("a", MeaningKind.CONCEPT, 0.2)),
                        Term.of(2, 3, MeaningStatus.SELECTED, Meaning.of("b", MeaningKind.CONCEPT, 0.2)))));
                                    
        TermIterator termIter
                = (TermIterator) st.terms().iterator();

        Term term1 = termIter.next();
        assertEquals("a", term1.getSelectedMeaning().getId());
        assertTrue(termIter.hasCurrentTerm());
        assertTrue(termIter.hasCurrentSentence());

        assertEquals("a", termIter.term().getSelectedMeaning().getId());
        assertEquals(termIter.sentence(), st.getSentences().get(0));
        assertTrue(termIter.toString().length() > 0);

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

        assertTrue(SemText.of().terms().isEmpty());

        assertEquals(4, st.terms().toArray().length);
        assertEquals("a", st.terms().get(0).getSelectedMeaning().getId());

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


}
