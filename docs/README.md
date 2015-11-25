<p class="jedoc-to-strip">
WARNING: WORK IN PROGRESS - THIS IS ONLY A TEMPLATE FOR THE DOCUMENTATION. <br/>
RELEASE DOCS ARE ON THE <a href="http://opendatatrentino.github.io/semtext/" target="_blank">PROJECT WEBSITE</a>
</p>

### UML diagram

A `SemText` object contains a list of `Sentence` objects, which in turn can contain a list of `Term` objects.

Each `Term` has a `MeaningStatus`, and possibly a selected meaning and a list of alternative meanings suggested for disambiguation.

Both sentences and terms are spans, and actual text is only stored in root `SemText` object. Span` offsets are always absolute and calculated with respects to it. Spans can't overlap.

All of semantic text items (sentences, terms, meaning, semtext itself) can hold metadata.

Utility functions are held in `SemTexts` class and to iterate through semtext terms a `TermsView` and a `TermIterator` are provided.

<p align="center">
<img width="650px" alt="semtext uml diagram" src="img/semtext-uml.png">
</p>

### Meaning state machine

SemText data model supports a simple interaction cycle with the user, where some nlp service enriches the text with tags and then a human user validates the tags. There are four possible  `MeaningStatus` associated to a `Term`:


<p align="center">
<img width="650px" alt="semtext meaning state machine" src="img/semtext-state-machine.png">
</p>


### Maven


SemText is available on Maven Central. To use it, put this in the dependencies section of your _pom.xml_: 


```
<dependency>
  <groupId>eu.trentorise.opendata.semtext</groupId>
  <artifactId>semtext</artifactId>
  <version>#{version}</version>
</dependency>
```

In case updates are available, version numbers follows [semantic versioning](http://semver.org/) rules.

### Examples

Objects have no public constructor. To make them use factory methods starting with `of`:

```
	SemText semText1 = SemText.of(Locale.ITALIAN, "ciao");
```

To obtain a modified version of an object, use `with` methods:

```
    SemText semText2 = semText1.with("buongiorno");

    assert semText1.getText().equals("ciao");
    assert semText2.getText().equals("buongiorno");
```

Let's go through the steps to construct a SemText of one sentence and one term with SELECTED meaning.

We will pick this text:
```
        String text = "Welcome to Garda lake.";
```

This builds the`Meaning`:

```

        Meaning meaning = Meaning.of("http://someknowledgebase.org/entities/garda-lake",
        							 MeaningKind.ENTITY,
                                     0.7);
```

We indicate the span where _Garda lake_ occurs (a `Term` can span many words):

```
	Term term = Term.of(11, 21, MeaningStatus.SELECTED, meaning);
```

We finally construct the immutable `SemText`. Notice language can be set only for the whole `SemText`:

```
SemText semText = SemText.of(
                Locale.ENGLISH,
                text,
                Sentence.of(0, 26, term)); // sentence spans the whole text
```

Only `SemText` actually contains the text. To retrieve it from a span use `getText`:

```
	assert "Garda lake".equals(semText.getText(term));
```

`SemTexts` class contains utilities, like converters and checkers:

```
    ImmutableList<SemText> semtexts = SemTexts.dictToSemTexts(Dict.of(Locale.ITALIAN, "Ciao"));
    try {
        SemTexts.checkScore(1.7, "Invalid score!");
    } catch(IllegalArgumentException ex){

    }
```

Other examples usages can be found [in the tests](../src/test/java/eu/trentorise/opendata/semtext/test
)

### Serialization to/from JSON

Serialization to/from JSON is done with Jackson.

#### Registering Jackson Module

For ser/deserilization to work, you need first to register `SemTextModule` and other required modules in your own Jackson `ObjectMapper`:

```
    ObjectMapper om = new ObjectMapper();
    om.registerModule(new GuavaModule());
    om.registerModule(new TodCommonsModule());
    om.registerModule(new SemTextModule());

    String json = om.writeValueAsString(SemText.of(Locale.ITALIAN, "ciao"));
    SemText reconstructedSemText = om.readValue(json, SemText.class);
```

Notice we have also registered the necessary Guava (for immutable collections) and Tod Commons modules (for Dict and LocalizedString).
To register everything in one command just write:

```
    ObjectMapper om = new ObjectMapper();
    SemTextModule.registerModulesInto(om);        
  
```

#### Simple usage example

```
    ObjectMapper om = new ObjectMapper();
    SemTextModule.registerModulesInto(om);

    String json = om.writeValueAsString(SemText.of(Locale.ITALIAN, "ciao"));
    SemText reconstructedSemText = om.readValue(json, SemText.class);
```

#### Metadata deserialization

Any object can be attached as metadata to `SemText`, `Sentence`, `Term` or `Meaning`, with the constraint that it must be non-null and should be immutable. Metadata is accessed by providing a namespace. For example, let's say we want to associate a Java `Date` to `SemText`, under the namespace `testns`. 

To create our object in Java we can write this:

```
    SemText.of("ciao").withMetadata("testns", new Date(123))
```

Once serialized as JSON, it will look like this:

```
{
  "locale":"",
  "text":"ciao",
  "sentences":[],
  "metadata":{
                "testns":123
             }
}
```

In order for metadata objects to be properly deserialized, we need to associate namespaces to object types by registering them in the `SemTextModule`. So, in order to serialize/deserialize the example above, we would do something like the following:

```
    ObjectMapper om = new ObjectMapper();

    // register all required modules into the Jackson Object Mapper
    SemTextModule.registerModulesInto(om);

    // declare that metadata under namespace 'testns' in SemText objects
    // should be deserialized into a Date object
    SemTextModule.registerMetadata(SemText.class, "testns", Date.class);       

    String json = om.writeValueAsString(SemText.of("ciao").withMetadata("testns", new Date(123)));

    SemText reconstructedSemText = om.readValue(json, SemText.class);                                

    Date reconstructedMetadata = (Date) reconstructedSemText.getMetadata("testns");

    assert  new Date(123).equals(reconstructedMetadata);
```

NOTE: namespace register is a static variable, so it's shared among all the object mappers. If you're writing a library with SemText serializer make sure to use a reasonably unique (thus long) namespace. Applications instead may use shorter namespaces.

#### Custom metadata deserialization

A more complex example can be found in <a href="https://github.com/opendatatrentino/semtext-jackson/blob/master/src/test/java/eu/trentorise/opendata/semtext/jackson/test/SemTextModuleTest.java" target="_blank">SemTextModuleTest.metadataSerializationComplex</a>, which shows how to develop and register a custom immutable metadata object. 

