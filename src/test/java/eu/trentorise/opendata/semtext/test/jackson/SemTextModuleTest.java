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
package eu.trentorise.opendata.semtext.test.jackson;

import static eu.trentorise.opendata.commons.test.jackson.TodJacksonTester.changeField;
import static eu.trentorise.opendata.commons.test.jackson.TodJacksonTester.testJsonConv;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import eu.trentorise.opendata.commons.Dict;
import eu.trentorise.opendata.commons.TodConfig;
import eu.trentorise.opendata.commons.jackson.TodCommonsModule;
import eu.trentorise.opendata.commons.test.jackson.TodJacksonTester;
import eu.trentorise.opendata.semtext.Meaning;
import eu.trentorise.opendata.semtext.MeaningKind;
import eu.trentorise.opendata.semtext.MeaningStatus;
import eu.trentorise.opendata.semtext.SemText;
import eu.trentorise.opendata.semtext.Sentence;
import eu.trentorise.opendata.semtext.Term;
import eu.trentorise.opendata.semtext.exceptions.SemTextNotFoundException;
import eu.trentorise.opendata.semtext.jackson.SemTextMetadataException;
import eu.trentorise.opendata.semtext.jackson.SemTextModule;

/**
 *
 * @author David Leoni
 */
public class SemTextModuleTest {

    private static final Logger LOG = Logger.getLogger(SemTextModuleTest.class.getName());

    private ObjectMapper objectMapper;

    @BeforeClass
    public static void beforeClass() {
        TodConfig.init(SemTextModuleTest.class);
    }

    @Before
    public void beforeMethod() {
        objectMapper = new ObjectMapper();
        SemTextModule.registerModulesInto(objectMapper);
    }

    @After
    public void afterMethod() {
        objectMapper = null;
        SemTextModule.clearMetadata();
    }

    @Test
    public void testUnregisteredMetadata() throws IOException {

        Meaning m1 = Meaning.builder()
                .setId("a")
                .setKind(MeaningKind.ENTITY)
                .setProbability(0.2)
                .setName(Dict.of(Locale.ITALIAN, "a"))
                .setDescription(Dict.of(Locale.FRENCH, "b"))
                .setMetadata(ImmutableMap.of("testns", Dict.of("s")))
                .build();
        try {
            TodJacksonTester.testJsonConv(objectMapper, LOG, m1);
            Assert.fail("Should have complained about unregistered namespace!");
        }
        catch (Exception ex) {

        }
        try {
            SemTextModule.getMetadataTypeReference(Sentence.class, "a");
            Assert.fail();
        }
        catch (SemTextNotFoundException ex) {

        }
    }

    @Test
    public void testDeserializingUnregisteredMetadata() throws JsonProcessingException, IOException {        
        
        ObjectNode jo2 = objectMapper.createObjectNode();
        jo2.put("a", "b");        
        String json = changeField(objectMapper, LOG, Meaning.of(),"metadata", jo2);

        try {
            objectMapper.readValue(json, Meaning.class);
            Assert.fail();
        }
        catch (JsonMappingException ex) {

        }
    }

    @Test
    public void testDeserializingWrongMetadata() throws JsonProcessingException, IOException {
        SemTextModule.registerMetadata(Meaning.class, "a", new TypeReference<List<String>>() {
        });
        
        ObjectNode jo2 = objectMapper.createObjectNode();
        jo2.put("a", "b");
        
        String wrongJson = changeField(objectMapper, LOG, Meaning.of(),"metadata", jo2);
        
        try {            
            objectMapper.readValue(wrongJson, Meaning.class);
            Assert.fail();
        }
        catch (JsonMappingException ex) {

        }
    }

    @Test
    public void testDeserializingNullMetadata() throws JsonProcessingException, IOException {
        SemTextModule.registerMetadata(Meaning.class, "a", String.class);
        
        
        ObjectNode jo2 = objectMapper.createObjectNode();
        jo2.put("a", (String) null);
        String json = changeField(objectMapper, LOG, Meaning.of(), "metadata", jo2);

        

        try {
            objectMapper.readValue(json, Meaning.class);
            Assert.fail();
        }
        catch (JsonMappingException ex) {

        }
    }

    /**
     * Registers MyMetadata in objectMapper
     */
    private void registerMyMetadata() {
        objectMapper.registerModule(new SimpleModule() {
            {
                setMixInAnnotation(MyMetadata.class, MyMetadataJackson.class);
            }
        });
    }

    @Test
    public void testMeaning() throws IOException {
        SemTextModule.registerMetadata(Meaning.class, "a", Dict.class);
        SemTextModule.registerMetadata(Meaning.class, "b", MyMetadata.class);

        registerMyMetadata();

        Meaning m1 = Meaning.builder()
                .setId("a")
                .setKind(MeaningKind.ENTITY)
                .setProbability(0.2)
                .setName(Dict.of(Locale.ITALIAN, "a"))
                .setDescription(Dict.of(Locale.FRENCH, "b"))
                .setMetadata(ImmutableMap.of("a", Dict.of("s"),
                        "b", MyMetadata.of("hello")))
                .build();
        TodJacksonTester.testJsonConv(objectMapper, LOG, m1);
    }

    @Test
    public void testTerm() throws IOException {
        SemTextModule.registerMetadata(Term.class, "a", Dict.class);
                
        Term t1 = Term.of(0,1, MeaningStatus.NOT_SURE,null)
                .withMetadata("a", Dict.of("s")); 
                
                
        testJsonConv(objectMapper, LOG, t1);
    }

    @Test
    public void testSentence() throws IOException {
        SemTextModule.registerMetadata(Sentence.class, "a", MyMetadata.class);
        registerMyMetadata();

        Sentence sen = Sentence.of(0,
                1,
                ImmutableList.<Term>of(),
                ImmutableMap.of("a", MyMetadata.of("hello")));
        testJsonConv(objectMapper, LOG, sen);
    }

    @Test
    public void testMetadataSentence() throws IOException {
        SemTextModule.registerMetadata(Sentence.class, "a", MyMetadata.class);
        registerMyMetadata();
        Sentence sen = Sentence.of(0,
                1,
                ImmutableList.<Term>of(),
                ImmutableMap.of("a", MyMetadata.of("hello")));
        testJsonConv(objectMapper, LOG, sen);
    }

    @Test
    public void testMetadataSemText() throws IOException {
        SemTextModule.registerMetadata(SemText.class, "a", MyMetadata.class);
        registerMyMetadata();

        testJsonConv(objectMapper,LOG, 
                SemText.ofSentences(Locale.ITALIAN,
                        "abcdefghilmno",
                        ImmutableList.<Sentence>of(),
                        ImmutableMap.of("a", MyMetadata.of())));

    }

    @Test
    public void testMapper() throws IOException {

        SemTextModule.registerMetadata(Meaning.class, "a", Dict.class);
        SemTextModule.registerMetadata(Meaning.class, "b", Dict.class);
        SemTextModule.registerMetadata(Term.class, "c", Integer.class);
        SemTextModule.registerMetadata(Sentence.class, "a", MyMetadata.class);
        SemTextModule.registerMetadata(SemText.class, "a", Integer.class);

        objectMapper.registerModule(new SimpleModule() {
            {
                setMixInAnnotation(MyMetadata.class, MyMetadataJackson.class);
            }
        });

        try {
            objectMapper.readValue("{\"start\":2, \"end\":1}", Term.class);
            Assert.fail("Should have failed because of missing attributes!");
        }
        catch (Exception ex) {

        }
        
        

        Meaning m1 = Meaning.builder()
                .setId("a")
                .setKind(MeaningKind.ENTITY)
                .setProbability(0.2)
                .setName(Dict.of(Locale.ITALIAN, "a"))
                .setDescription(Dict.of(Locale.FRENCH, "b"))
                .setMetadata(ImmutableMap.of("a", Dict.of("s")))
                .build();
        Meaning m2 = Meaning.builder()
                .setId("b")
                .setKind(MeaningKind.ENTITY)
                .setProbability(0.2)
                .setName(Dict.of(Locale.ITALIAN, "a"))
                .setDescription(Dict.of(Locale.FRENCH, "b"))
                .setMetadata(ImmutableMap.of("b", Dict.of("s")))
                .build();

        testJsonConv(objectMapper, LOG, Meaning.of("a", MeaningKind.CONCEPT, 0.2));

        Term term = Term.of(
                0,
                2,
                MeaningStatus.SELECTED,
                m1,
                ImmutableList.of(m1, m2),
                ImmutableMap.of("c", 3));

        testJsonConv(objectMapper,LOG, 
                SemText.ofSentences(Locale.ITALIAN,
                        "abcdefghilmno",
                        ImmutableList.of(Sentence.of(0,
                                        7,
                                        ImmutableList.of(term),
                                        ImmutableMap.of("a", MyMetadata.of("hello")))),
                        ImmutableMap.of("a", 9)));

    }

    /**
     * These ones for some reason don't work....
     */
    @Test(expected = JsonMappingException.class)
    public void testEmptyConstructors() throws IOException {

        assertEquals(Meaning.of(), objectMapper.readValue("{}", Meaning.class));
        assertEquals(SemText.of(), objectMapper.readValue("{}", SemText.class));
    }

    /**
     * Example with manual modules registration
     */
    @Test
    public void example1() throws JsonProcessingException, IOException {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new GuavaModule());
        om.registerModule(new TodCommonsModule());
        om.registerModule(new SemTextModule());

        String json = om.writeValueAsString(SemText.of(Locale.ITALIAN, "ciao"));
        SemText reconstructedSemText = om.readValue(json, SemText.class);
    }

    /**
     * Example with one line module registration
     */    
    @Test
    public void example2() throws JsonProcessingException, IOException {
        ObjectMapper om = new ObjectMapper();
        SemTextModule.registerModulesInto(om);

        String json = om.writeValueAsString(SemText.of(Locale.ITALIAN, "ciao"));
        SemText reconstructedSemText = om.readValue(json, SemText.class);
    }

    @Test
    public void testMetadataSerializationSimple() throws JsonProcessingException, IOException {
        ObjectMapper om = new ObjectMapper();

        // register all required modules into the Jackson Object Mapper
        SemTextModule.registerModulesInto(om);

        // declare that metadata under namespace 'testns' in SemText objects should be deserialized into a Date object
        SemTextModule.registerMetadata(SemText.class, "testns", Date.class);

        // Let's say MyMetadata is tricky to deserialize, so we tell Jackson how to deserialize it with a mixin annotation
        om.registerModule(new SimpleModule() {
            {
                setMixInAnnotation(MyMetadata.class, MyMetadataJackson.class);
            }
        });

        String json = om.writeValueAsString(SemText.of("ciao").withMetadata("testns", new Date(123)));

        LOG.fine("json = " + json);

        SemText reconstructedSemText = om.readValue(json, SemText.class);

        Date reconstructedMetadata = (Date) reconstructedSemText.getMetadata("testns");

        assert new Date(123).equals(reconstructedMetadata);
    }

    @Test
    public void testMetadataSerializationComplex() throws JsonProcessingException, IOException {
        ObjectMapper om = new ObjectMapper();

        // register all required modules into the Jackson Object Mapper
        SemTextModule.registerModulesInto(om);

        // declare that metadata under namespace 'testns' in SemText objects should be deserialized into a MyMetadata object
        SemTextModule.registerMetadata(SemText.class, "testns", MyMetadata.class);

        // Let's say MyMetadata is tricky to deserialize, so we tell Jackson how to deserialize it with a mixin annotation
        om.registerModule(new SimpleModule() {
            {
                setMixInAnnotation(MyMetadata.class, MyMetadataJackson.class);
            }
        });

        String json = om.writeValueAsString(SemText.of(Locale.ITALIAN, "ciao").withMetadata("testns", MyMetadata.of("hello")));

        SemText reconstructedSemText = om.readValue(json, SemText.class);

        MyMetadata reconstructedMetadata = (MyMetadata) reconstructedSemText.getMetadata("testns");

        assert MyMetadata.of("hello").equals(reconstructedMetadata);
    }

    @Test
    public void testSemTextMetadataException() {
        TypeReference typeRef = new TypeReference<String>() {
        };
        SemTextMetadataException ex1 = new SemTextMetadataException("a", Sentence.class, "b", typeRef);
        assertTrue(ex1.getMessage().length() > 1);
        assertEquals("b", ex1.getNamespace());
        assertEquals(typeRef, ex1.getTypeRef());
        assertEquals(Sentence.class, ex1.getHasMetadataClass());

        SemTextMetadataException ex2 = new SemTextMetadataException("a", Sentence.class, "b", null);
        assertTrue(ex2.getMessage().length() > 1);

        @SuppressWarnings({"ThrowableInstanceNotThrown", "ThrowableInstanceNeverThrown"})
        SemTextMetadataException ex3 = new SemTextMetadataException("a", null, "b", typeRef, new RuntimeException());
        assertTrue(ex1.getMessage().length() > 1);
        assertEquals("b", ex1.getNamespace());
        assertEquals(typeRef, ex1.getTypeRef());
        assertEquals(Sentence.class, ex1.getHasMetadataClass());

    }

    /**
     * Tests weird module equality copied from Guava module
     */
    @Test
    public void testEquality() {
        SemTextModule sm = new SemTextModule();
        assertEquals(sm, sm);
        assertEquals(new SemTextModule().hashCode(), new SemTextModule().hashCode());
        assertNotEquals(new SemTextModule(), new SemTextModule());
    }

}
