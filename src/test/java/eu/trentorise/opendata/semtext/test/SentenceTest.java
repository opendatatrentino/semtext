/*
 * Copyright 2015 Trento Rise.
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
import eu.trentorise.opendata.commons.NotFoundException;
import eu.trentorise.opendata.commons.OdtConfig;
import eu.trentorise.opendata.semtext.MeaningStatus;
import eu.trentorise.opendata.semtext.Sentence;
import eu.trentorise.opendata.semtext.Term;
import java.util.List;
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
public class SentenceTest {

    private static final Logger LOG = Logger.getLogger(SentenceTest.class.getName());

    @BeforeClass
    public static void beforeClass() {
         OdtConfig.init(SentenceTest.class);
    }

    @Test
    public void testSentence() {

        assertEquals("b", Sentence.of(0, 1).withMetadata("a", "b").getMetadata("a"));

        assertFalse(Sentence.of(1, 2).hasMetadata("a"));
        assertTrue(Sentence.of(1, 2).withMetadata("a", "1").hasMetadata("a"));

        Term myTerm = Term.of(2, 3, MeaningStatus.TO_DISAMBIGUATE, null);
        List<Term> terms = Sentence.of(0, 3, Term.of(0, 1, MeaningStatus.NOT_SURE, null)).withTerms(myTerm).getTerms();
        assertEquals(1, terms.size());
        assertEquals(myTerm, terms.get(0));
        assertEquals(MeaningStatus.TO_DISAMBIGUATE, myTerm.getMeaningStatus());

        assertTrue(Sentence.of(0, 1).toString().length() > 0);

        try {
            Sentence.of(0, 1).getMetadata("blabla");
            Assert.fail();
        }
        catch (NotFoundException ex) {

        }

    }

    @Test
    @SuppressWarnings({"IncompatibleEquals", "ObjectEqualsNull"})
    public void testEquality() {
        Sentence s1 = Sentence.of(0, 5, ImmutableList.of(Term.of(1, 3, MeaningStatus.NOT_SURE, null))).withMetadata("a", 2);
        Sentence s2 = Sentence.of(0, 5, ImmutableList.of(Term.of(1, 3, MeaningStatus.NOT_SURE, null))).withMetadata("a", 2);
        Sentence s3 = Sentence.of(0, 5);

        assertEquals(s1, s2);
        assertEquals(s1.hashCode(),s2.hashCode());
        assertNotEquals(s1, s3);

        assertFalse(s1.equals(null));
        assertFalse(s2.equals(""));
    }
}
