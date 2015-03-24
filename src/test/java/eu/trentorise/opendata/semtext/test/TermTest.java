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
import eu.trentorise.opendata.semtext.Meaning;
import eu.trentorise.opendata.semtext.MeaningKind;
import eu.trentorise.opendata.semtext.MeaningStatus;
import eu.trentorise.opendata.semtext.SemTexts;
import eu.trentorise.opendata.semtext.Sentence;
import eu.trentorise.opendata.semtext.Term;
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
public class TermTest {

    private static final Logger LOG = Logger.getLogger(TermTest.class.getName());

    @BeforeClass
    public static void beforeClass() {
        OdtConfig.of(TermTest.class).loadLogConfig();
    }

    @Test
    public void testTerm() {
        Meaning m = Meaning.of("a", MeaningKind.ENTITY, 0.3);

        Term termWithMeaning = Term.of(1, 2, MeaningStatus.NOT_SURE, null).with(MeaningStatus.SELECTED, m);

        assertTrue(termWithMeaning.getMeanings().isEmpty());
        assertEquals(m, termWithMeaning.getSelectedMeaning());

        assertFalse(Term.of(1, 2, MeaningStatus.NOT_SURE, null).hasMetadata("a"));
        assertTrue(Term.of(1, 2, MeaningStatus.NOT_SURE, null).withMetadata("a", "b").hasMetadata("a"));
        assertEquals("b", Term.of(1, 2, MeaningStatus.NOT_SURE, null).withMetadata("a", "b").getMetadata("a"));

        try {
            Term.of(0, 1, MeaningStatus.NOT_SURE, null).getMetadata("blabla");
            Assert.fail();
        }
        catch (NotFoundException ex) {

        }

    }

    @Test
    public void testEquality() {
        assertEquals(Term.of(0, 2, MeaningStatus.NOT_SURE, null, ImmutableList.<Meaning>of()),
                Term.of(0, 2, MeaningStatus.NOT_SURE, null, ImmutableList.<Meaning>of()));
        assertNotEquals(Term.of(0, 2, MeaningStatus.NOT_SURE, null, ImmutableList.<Meaning>of()),
                Term.of(0, 3, MeaningStatus.NOT_SURE, null, ImmutableList.<Meaning>of()));
    }

    @Test
    public void testWrongMeaningStatus() {
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
}
