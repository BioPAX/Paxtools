# Paxtools

The [BioPAX](http://www.biopax.org) Object Model, API and utilities implemented in Java.
Paxtools is developed at [Computational Biology Centre at MSKCC](http://cbio.mskcc.org/) and [Bader Lab, Donnelly Centre, University of Toronto](http://baderlab.org/).

## Features
###A complete and consistent implementation of BioPAX specification 
BioPAX elements in Paxtools are plain Java beans which provide methods to access the properties described in BioPAX, and a model, acting as a container for all BioPAX elements, provides querying facilities for them. Users can either read a BioPAX model from a file or create an empty one from the scratch. Methods to add new elements to a model and to remove elements from a model are also provided.

###Support for OWL properties and additional inverse links
Owl properties can be symmetric, transient or subtyped into other properties. These semantics can not be represented directly in an object oriented programming language. Paxtools implements these additional semantics and automatically update the fields of objects. For example, since property _standardName_ is a subproperty of_name_, updating the _standardName_ of a protein will also update its list of names. Similarly since _component_ is a transient property, a query for the components of a complex will not only list its immediate components but also the components of the subcomplexes inside this complex. In the BioPAX specification, properties are unidirectional for brevity. For example, _participant_ property links interactions to physical entities. Paxtools provides additional "inverse" links that allows efficient navigation from a physical entity to the all interactions that it participates (e.g., xrefOf, entityReferenceOf).

### Syntactic validation
Each operation that modifies the model is internally validated by Paxtools to comply with BioPAX syntax, including RDF well-formedness, domain and range restrictions, bidirectional links, and redundancies. ([BioPAX Validator project](http://github.com/BioPAX/validator) provides a much more detailed [validation](http://www.biopax.org/validator) and also checks for best practices.)

## Seamless handling of different BioPAX levels
BioPAX Level 3 introduced significant improvements to the naming and structure of the BioPAX at some cost of backwards compatibility. Paxtools supports all three BioPAX levels and provides facilities for upgrading older BioPAX models to Level 3, reducing the burden of working with different BioPAX levels for developers. (__Note__: BioPAX Level1 is not supported starting from Paxtools version 5.0.0-SNAPSHOT.)

### Converting to and from different formats
Paxtools can convert PSI-MI models to BioPAX Level 3. In addition, BioPAX models can be exported back to OWL and several other useful formats, including SIF (Simple Interaction Format), SBGN-ML, and GSEA (GMT) gene sets.

### Efficient traversal and editing via reflection
Paxtools implements the Property Editor design pattern to allow tools to manipulate BioPAX models without actually hard coding property and class names. This pattern considerably simplifies development of BioPAX exporters and other tools and makes it easier to extend and update them to support future changes in the BioPAX specification.

### Modular and lightweight structure
Paxtools is currently distributed as a Maven project in a modular structure which allows developers to easily select just the parts of Paxtools they need in their application.

### A platform for development of BioPAX software infrastructure
Several projects are built on top of Core Paxtools: a persistence system using Java Persistence API integrated with the querying facilities, an advanced validator that allows checking complex rules and the best practices using an extensible framework, an integrator that detects and merges interactions that are equivalent based on their participants, and a graph theoretic query engine and pattern search for finding biologically relevant connections and sub-networks. These software tools are available as a part of Pathway Commons project. Software that uses Paxtools can natively interact with these tools.

## Use
Paxtools provides, beyond the core and converters API, a console application that can execute several useful commands. If you have downloaded the "fat" JAR (with built-in dependencies), then you can access to the console interface description with the following command:

`java -jar paxtools.jar` (add -Xmx option when processing large data files).

If you have [homebrew](http://brew.sh/) installed on your system (Mac OS X), you can install the latest release of Paxtools via the following brew command (there might be old version):

```bash
$ brew install homebrew/science/paxtools
$ paxtools help
```

## Availability
* The latest stable Paxtools modules are available in Maven Central
* [OSSRH public repository](https://oss.sonatype.org/content/groups/public/) (snapshots, since 4.3.1-SNAPSHOT, and releases)
* Older BioPAX [snapshots](http://www.biopax.org/m2repo/snapshots/) and [releases](http://www.biopax.org/m2repo/releases/) Maven2 repository
* [Downloads](http://www.biopax.org/downloads/paxtools/)

More information about Paxtools can be found in [the publication](http://dx.plos.org/10.1371/journal.pcbi.1003194),  [wiki archive](http://www.biopax.org/mediawiki/index.php/Paxtools), and [BioPAX forum](https://groups.google.com/d/msg/biopax-discuss/zwtwDG23T1E/Vu1OK7iXBQAJ).

(_TODO_: move the Paxtools description here and deprecate that wiki soon.)

## Build

To build or use with a modern JDK (>11), e.g., OpenJDK-19, additional java options are required:
```bash
export _JAVA_OPTIONS="--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED"
```
(can also add to the java command args)

To compile, run unit tests, and build JARs:
```bash
mvn clean package
```
