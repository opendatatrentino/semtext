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
import eu.trentorise.opendata.commons.Dict;
import eu.trentorise.opendata.commons.OdtConfig;
import eu.trentorise.opendata.semtext.Meaning;
import eu.trentorise.opendata.semtext.MeaningKind;
import eu.trentorise.opendata.semtext.MeaningStatus;
import eu.trentorise.opendata.semtext.SemText;
import eu.trentorise.opendata.semtext.SemTexts;
import eu.trentorise.opendata.semtext.Sentence;
import eu.trentorise.opendata.semtext.Term;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author David Leoni
 */
public class SemTextsTest {

    private static final Logger LOG = Logger.getLogger(SemTextsTest.class.getName());

    @BeforeClass
    public static void beforeClass() {
        OdtConfig.of(SemTextsTest.class).loadLogConfig();
    }

    @Test
    public void testSpans() {
        assertFalse(SemTexts.spanEqual(null, Term.of(0, 1, MeaningStatus.NOT_SURE, null)));
        assertFalse(SemTexts.spanEqual(Term.of(0, 1, MeaningStatus.NOT_SURE, null), null));
        assertTrue(SemTexts.spanEqual(null, null));

        assertTrue(SemTexts.spanEqual(Sentence.of(0, 1), Term.of(0, 1, MeaningStatus.NOT_SURE, null)));
        assertFalse(SemTexts.spanEqual(Sentence.of(0, 2), Term.of(0, 1, MeaningStatus.NOT_SURE, null)));

    }

    @Test
    public void testCheckSpan() {

        SemTexts.checkSpan(0, 0, "a");
        SemTexts.checkSpan(1, 1, "a");

        try {
            SemTexts.checkSpan(-1, 1, "a");
            Assert.fail();
        }
        catch (IllegalArgumentException ex) {

        }

        try {
            SemTexts.checkSpan(-2, -1, "a");
            Assert.fail();
        }
        catch (IllegalArgumentException ex) {

        }

        try {
            SemTexts.checkSpan(2, 1, "a");
            Assert.fail();
        }
        catch (IllegalArgumentException ex) {

        }

    }

    @Test
    public void testDisambiguate() {
        Meaning meaningA = Meaning.of("a", MeaningKind.ENTITY, 0.8, Dict.of());

        assertEquals(meaningA, SemTexts.disambiguate(ImmutableList.of(
                meaningA,
                Meaning.of("b", MeaningKind.ENTITY, 0.2, Dict.of()))));

        assertEquals(null, SemTexts.disambiguate(ImmutableList.of(Meaning.of("a", MeaningKind.ENTITY, 0.2, Dict.of()),
                Meaning.of("b", MeaningKind.ENTITY, 0.2, Dict.of()))));

        assertEquals(meaningA, SemTexts.disambiguate(ImmutableList.of(meaningA)));
        assertEquals(null, SemTexts.disambiguate(ImmutableList.<Meaning>of()));
    }

    @Test
    public void testCheckMeaningStatus() {
        SemTexts.checkMeaningStatus(MeaningStatus.NOT_SURE, null, "a");

        try {
            SemTexts.checkMeaningStatus(MeaningStatus.NOT_SURE, Meaning.of("b", MeaningKind.ENTITY, 0.2, Dict.of()), "a");
            Assert.fail();
        }
        catch (IllegalArgumentException ex) {

        }

        SemTexts.checkMeaningStatus(MeaningStatus.TO_DISAMBIGUATE, null, "a");
        try {
            SemTexts.checkMeaningStatus(MeaningStatus.TO_DISAMBIGUATE, Meaning.of("b", MeaningKind.ENTITY, 0.2, Dict.of()), "a");
            Assert.fail();
        }
        catch (IllegalArgumentException ex) {

        }

        SemTexts.checkMeaningStatus(MeaningStatus.REVIEWED, Meaning.of("b", MeaningKind.ENTITY, 0.2, Dict.of()), "a");
        try {
            SemTexts.checkMeaningStatus(MeaningStatus.REVIEWED, null, "a");
            Assert.fail();
        }
        catch (IllegalArgumentException ex) {

        }

        SemTexts.checkMeaningStatus(MeaningStatus.SELECTED, Meaning.of("b", MeaningKind.ENTITY, 0.2, Dict.of()), "a");

        try {
            SemTexts.checkMeaningStatus(MeaningStatus.SELECTED, null, "a");
            Assert.fail();
        }
        catch (IllegalArgumentException ex) {

        }

    }

    @Test
    public void checkSpans() {
        SemTexts.checkSpans(ImmutableList.of(Sentence.of(0, 1)), 0, 1, this);
        SemTexts.checkSpans(ImmutableList.of(Sentence.of(0, 0)), 0, 0, this);

        try {
            SemTexts.checkSpans(ImmutableList.of(Sentence.of(0, 1)), -1, 1, this);
            Assert.fail();
        }
        catch (IllegalArgumentException ex) {

        }

        try {
            SemTexts.checkSpans(ImmutableList.of(Sentence.of(1, 3)), 2, 4, this);
            Assert.fail();
        }
        catch (IllegalArgumentException ex) {

        }

        try {
            SemTexts.checkSpans(ImmutableList.of(Sentence.of(3, 5)), 2, 4, this);
            Assert.fail();
        }
        catch (IllegalArgumentException ex) {

        }

        try {
            SemTexts.checkSpans(ImmutableList.of(Sentence.of(1, 2), Sentence.of(1, 3)), 0, 6, this);
            Assert.fail();
        }
        catch (IllegalArgumentException ex) {

        }

    }

    @Test
    public void testCheckScore() {
        SemTexts.checkScore(-0.5 * SemTexts.TOLERANCE, this);
        SemTexts.checkScore(1 + 0.5 * SemTexts.TOLERANCE, this);

        try {
            SemTexts.checkScore(0 - 2 * SemTexts.TOLERANCE, this);
            Assert.fail();
        }
        catch (IllegalArgumentException ex) {

        }

        try {
            SemTexts.checkScore(1 + 2 * SemTexts.TOLERANCE, this);
            Assert.fail();
        }
        catch (IllegalArgumentException ex) {

        }

    }

    @Test
    public void testCheckPositiveScore() {
        SemTexts.checkPositiveScore(-0.5 * SemTexts.TOLERANCE, this);
        try {
            SemTexts.checkPositiveScore(- 2 * SemTexts.TOLERANCE, this);
            Assert.fail();
        }
        catch (IllegalArgumentException ex) {

        }
    }

    @Test
    public void checkMergeMeanings() {
        Meaning m1 = Meaning.of("a", MeaningKind.ENTITY, 0.3);
        Meaning m2 = Meaning.of("a", MeaningKind.ENTITY, 0.4);
        ImmutableList<Meaning> ms = SemTexts.mergeMeanings(ImmutableList.of(m1),
                ImmutableList.of(m2));
        assertEquals(1, ms.size());
        assertTrue( ms.get(0).getProbability() < 1 + SemTexts.TOLERANCE);
        assertTrue( ms.get(0).getProbability() > 1 - SemTexts.TOLERANCE);
        assertEquals("a", ms.get(0).getId());
        assertEquals(MeaningKind.ENTITY, ms.get(0).getKind());
                
        SemTexts.mergeMeanings(ImmutableList.<Meaning>of(), ImmutableList.<Meaning>of());
    }

    @Test
    public void testConversions() {
        List<SemText> semtexts = ImmutableList.of(SemText.of(Locale.ITALIAN, "a"), SemText.of(Locale.ENGLISH, "b"));
        assertEquals(Dict.of(Locale.ITALIAN, "a").with(Locale.ENGLISH, "b"), SemTexts.semTextsToDict(semtexts));
    }
    
}
