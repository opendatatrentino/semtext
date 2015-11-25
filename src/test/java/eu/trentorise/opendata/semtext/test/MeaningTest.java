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

import eu.trentorise.opendata.commons.Dict;
import eu.trentorise.opendata.commons.TodConfig;
import eu.trentorise.opendata.semtext.Meaning;
import eu.trentorise.opendata.semtext.MeaningKind;
import eu.trentorise.opendata.semtext.SemTexts;
import eu.trentorise.opendata.semtext.exceptions.SemTextNotFoundException;

import java.util.logging.Logger;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author David Leoni
 */
public class MeaningTest {

    private static final Logger LOG = Logger.getLogger(MeaningTest.class.getName());

    @BeforeClass
    public static void beforeClass() {
        TodConfig.init(MeaningTest.class);
    }

    /**
     * This fails apparently because of this:
     * https://github.com/immutables/immutables/issues/196
     */
    @Test
    @Ignore
    public void testWith() {
        assertTrue(Meaning.of()
                          .withMetadata("a", "b")
                          .hasMetadata("a"));
    }

    @Test
    public void testMeaning() {

        double prob = 0.2;
        double p = Meaning.of("a", MeaningKind.ENTITY, 0.1)
                          .withProbability(prob)
                          .getProbability();
        assertTrue(Math.abs(p - prob) < SemTexts.TOLERANCE);

        Meaning ma = Meaning.builder()
                            .setId("a")
                            .setKind(MeaningKind.ENTITY)
                            .setProbability(0.3)
                            .setName(Dict.of("a"))
                            .setDescription(Dict.of("b"))
                            .build();
        assertEquals("a", ma.getId());
        assertEquals(MeaningKind.ENTITY, ma.getKind());
        assertTrue(ma.getProbability() > 0.29);
        assertTrue(ma.getProbability() < 0.31);
        assertEquals(Dict.of("a"), ma.getName());
        assertEquals(Dict.of("b"), ma.getDescription());

        assertEquals("b", Meaning.of("a", MeaningKind.ENTITY, 0.3)
                                 .withMetadata("a", "b")
                                 .getMetadata("a"));
        assertFalse(Meaning.of()
                           .hasMetadata("a"));

        assertTrue(Meaning.of("a", MeaningKind.ENTITY, 0.3)
                          .compareTo(Meaning.of("b", MeaningKind.CONCEPT, 0.2)) > 0);

        assertTrue(Meaning.of()
                          .toString()
                          .length() > 0);



        try {
            Meaning.of()
                   .withProbability(-2 * SemTexts.TOLERANCE);
            Assert.fail();
        } catch (IllegalArgumentException ex) {

        }

        try {
            Meaning.of()
                   .getMetadata("blabla");
            Assert.fail();
        } catch (SemTextNotFoundException ex) {

        }

    }

    @Test
    @SuppressWarnings({ "IncompatibleEquals", "ObjectEqualsNull" })
    public void testEquality() {
        Meaning maEntity01 = Meaning.of("a", MeaningKind.ENTITY, 0.1);
        Meaning maConcept01 = Meaning.of("a", MeaningKind.CONCEPT, 0.1);
        Meaning mbConcept01 = Meaning.of("b", MeaningKind.CONCEPT, 0.1);
        Meaning maConcept09 = Meaning.of("a", MeaningKind.CONCEPT, 0.9);

        assertNotEquals(maEntity01, maConcept01);
        assertNotEquals(maConcept01, mbConcept01);

        assertEquals(maConcept01, maConcept09);
        assertEquals(maConcept01.hashCode(), maConcept09.hashCode());

        assertFalse(maConcept01.equals(null));
        assertFalse(maConcept01.equals(""));
    }
}
