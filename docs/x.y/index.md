<p class="jadoc-to-strip">
WARNING: WORK IN PROGRESS - THIS IS ONLY A TEMPLATE FOR THE DOCUMENTATION. <br/>
RELEASE DOCS ARE ON THE PROJECT WEBSITE
</p>


### Introduction

SemText is a lightweight model for semantically annotated text, designed for reliable exchange among applications rather than for efficiency. 

### UML diagram

<p align="center">
<img alt="semtext uml diagram" src="img/semtext-uml.png">
</p>

### Meaning state machine

<p align="center">
<img width="700px" alt="semtext meaning state machine" src="img/semtext-state-machine.png">
</p>


### Usage

#### Maven dependency

SemText Jackson is available on Maven Central. To use it, put this in the dependencies section of your _pom.xml_: 


```
<dependency>
  <groupId>eu.trentorise.opendata.semtext</groupId>
  <artifactId>semtext</artifactId>
  <version>#{version}</version>            
</dependency>
```

In case updates are available, version numbers follows [semantic versioning](http://semver.org/) rules.

#### Examples

Crude examples usages can be found [in the tests](../../src/test/java/eu/trentorise/opendata/semantics/nlp/test/model/SemTextTest.java
)

```
todo put better examples
```