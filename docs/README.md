<p class="jedoc-to-strip">
WARNING: WORK IN PROGRESS - THIS IS ONLY A TEMPLATE FOR THE DOCUMENTATION. <br/>
RELEASE DOCS ARE ON THE <a href="http://opendatatrentino.github.io/semtext/" target="_blank">PROJECT WEBSITE</a>
</p>


SemText is a lightweight model for semantically annotated text, designed for reliable exchange among applications rather than for efficiency.

  * provides a simple hierarchy of sentences and terms
  * has immutable data structures
  * allows attaching metadata to spans

The purpose of this release is to provide a first model sufficiently stable to be reusable in different projects.

### UML diagram

SemText contains sentences, which in turn can contain terms. Each term has a meaning status, and possibly a selected meaning and a list of alternative meanings suggested for disambiguation.  Both sentences and terms are spans, and actual text is only stored in root SemText object. Span offsets are always absolute and calculated with respects to it. Spans can't overlap. All of semantic text items (sentences, terms, meaning, semtext itself) can hold metadata.

Utility functions are held in `SemTexts` class and to iterate through terms a list view and an iterator are provided.

<p align="center">
<img alt="semtext uml diagram" src="img/semtext-uml.png">
</p>

### Meaning state machine

SemText data model supports a four state simple interaction cycle with the user where some nlp service enriches the text with tags and then a human user validates the tags.


<p align="center">
<img width="700px" alt="semtext meaning state machine" src="img/semtext-state-machine.png">
</p>


### Usage

#### Maven dependency

SemText is available on Maven Central. To use it, put this in the dependencies section of your _pom.xml_: 


```
<dependency>
  <groupId>eu.trentorise.opendata.semtext</groupId>
  <artifactId>semtext</artifactId>
  <version>#{version}</version>            
</dependency>
```

In case updates are available, version numbers follows [semantic versioning](http://semver.org/) rules.

#### Examples

Crude examples usages can be found [in the tests](../src/test/java/eu/trentorise/opendata/semtext/test/SemTextTest.java
)

```
todo put better examples
```