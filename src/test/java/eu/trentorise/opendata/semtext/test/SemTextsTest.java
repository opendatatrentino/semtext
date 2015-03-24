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
    public void spansTest() {
        assertFalse(SemTexts.spanEqual(null, Term.of(0, 1, MeaningStatus.NOT_SURE, null)));
        assertFalse(SemTexts.spanEqual(Term.of(0, 1, MeaningStatus.NOT_SURE, null), null));
        assertTrue(SemTexts.spanEqual(null, null));
        
        assertTrue(SemTexts.spanEqual(Sentence.of(0, 1), Term.of(0, 1, MeaningStatus.NOT_SURE, null)));
        assertFalse(SemTexts.spanEqual(Sentence.of(0, 2), Term.of(0, 1, MeaningStatus.NOT_SURE, null)));
        
    }

    @Test
    public void disambiguateTest() {
        Meaning meaningA = Meaning.of("a", MeaningKind.ENTITY, 0.8, Dict.of());

        assertEquals(meaningA, SemTexts.disambiguate(ImmutableList.of(
                meaningA,
                Meaning.of("b", MeaningKind.ENTITY, 0.2, Dict.of()))));

        assertEquals(null, SemTexts.disambiguate(ImmutableList.of(Meaning.of("a", MeaningKind.ENTITY, 0.2, Dict.of()),
                Meaning.of("b", MeaningKind.ENTITY, 0.2, Dict.of()))));
    }
    
    
    @Test
    public void testChecks(){        
         SemTexts.checkPositiveScore(- 0.5 * SemTexts.TOLERANCE, this);
        try {
            SemTexts.checkPositiveScore(- 2 * SemTexts.TOLERANCE, this);
        } catch (IllegalArgumentException ex){
            
        }
    }

    @Test
    public void testConversions(){
        List<SemText> semtexts = ImmutableList.of(SemText.of(Locale.ITALIAN, "a"), SemText.of(Locale.ENGLISH, "b"));
        assertEquals(Dict.of(Locale.ITALIAN, "a").with(Locale.ENGLISH, "b"), SemTexts.semTextsToDict(semtexts));
    }
        
}
