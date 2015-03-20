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

import eu.trentorise.opendata.commons.OdtConfig;
import eu.trentorise.opendata.semtext.Meaning;
import eu.trentorise.opendata.semtext.MeaningKind;
import eu.trentorise.opendata.semtext.SemTexts;
import java.util.logging.Logger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author David Leoni
 */
public class MeaningTest {

    private static final Logger LOG = Logger.getLogger(MeaningTest.class.getName());

    @BeforeClass
    public static void beforeClass() {
        OdtConfig.of(MeaningTest.class).loadLogConfig();
    }

    @Test
    public void testMeaning() {
        assertEquals("b", Meaning.of("a", MeaningKind.ENTITY, 0.3).withMetadata("a", "b").getMetadata("a"));

        double prob = 0.2;
        double p = Meaning.of("a", MeaningKind.ENTITY, 0.1).withProbability(prob).getProbability();
        assertTrue(Math.abs(p - prob) < SemTexts.TOLERANCE);

    }
}
