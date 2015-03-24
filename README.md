# pattern
## Pattern search software for BioPAX models ##
[BioPAX](http://www.biopax.org) is a language standard for describing biological networks. This software helps to define graph patterns with BioPAX objects, and searches them on BioPAX models.

This tool is developed on top of [Paxtools](http://www.biopax.org/paxtools), and effective usage requires knowledge of BioPAX structure and some familiarity with Paxtools.

### A pattern sample ###

A pattern is composed of fixed number of BioPAX elements and a list of constraints between. For instance the below illustrated pattern captures pairs of proteins that control two consecutive reactions (an output of first reaction is input to the second).

![https://docs.google.com/drawings/d/1gYgIWKb45Iw2fRnjb3vfYkyi8VHOcWho7JgcA9qNqZ4/pub?w=400&type=.png](https://docs.google.com/drawings/d/1gYgIWKb45Iw2fRnjb3vfYkyi8VHOcWho7JgcA9qNqZ4/pub?w=400&type=.png)

The Java code equivalent of this pattern is below.

```
Pattern p = new Pattern(ProteinReference.class, "PR1");
p.add(ConBox.erToPE(), "PR1", "P1");
p.add(ConBox.peToControlledConv(), "P1", "Conv1");
p.add(new Participant(RelType.OUTPUT),  "Conv1", "linker");
p.add(ConBox.equal(false), "linker", "P1");
p.add(new ParticipatesInConv(RelType.INPUT), "linker", "Conv2");
p.add(ConBox.equal(false), "Conv1", "Conv2");
p.add(ConBox.convToController(), "Conv2", "P2");
p.add(ConBox.equal(false), "linker", "P2");
p.add(ConBox.peToER(), "P2", "PR2");
p.add(ConBox.equal(false), "PR2", "PR1");
```

To learn how to use, please start with the QuickStartGuide.

A [Javadoc](http://biopax.github.io/pattern/index.html) is also available.

### Searching for binary interactions ###

As an example use of this pattern search framework, we defined a set of patterns that capture useful binary interactions between proteins and small molecules in a BioPAX model. Binary interaction types include upstream-to-downstream signal transmissions, controlling expression changes, sequential catalysis by metabolic enzymes, controlling transportation, enzymes consuming or producing chemicals, etc. [[Read more|UsingBinaryInteractionFramework]].

### Using the software ###

You can find a "fat" (with dependencies) biopax-pattern jar file in [BioPAX Downloads](http://www.biopax.org/downloads/paxtools/).

[Maven](http://maven.apache.org) users can add the following lines to their project pom files to use biopax-pattern framework.

```
<repositories>
	<repository>
		<id>biopax.release.repo</id>
		<name>BioPAX releases</name>
		<url>http://www.biopax.org/m2repo/releases</url>
		<snapshots>
			<enabled>false</enabled>
		</snapshots>
	</repository>
	<repository>
		<id>biopax.snapshot.repo</id>
		<name>BioPAX snapshots</name>
		<url>http://www.biopax.org/m2repo/snapshots</url>
		<releases>
			<enabled>false</enabled>
		</releases>
	</repository>
</repositories>

<dependencies>
	<dependency>
		<groupId>org.biopax.paxtools</groupId>
		<artifactId>pattern</artifactId>
		<version>LATEST</version>
	</dependency>
</dependencies>
```

### Searching for pre-defined patterns ###

This software is developed as a library, however it also supports searching BioPAX models using some pre-defined patterns and output formats, without any programming. To do this, run the biopax-pattern jar with the following command.

```
java -Xmx5G -jar biopax-pattern-{version}.jar
```

A dialog will appear and let you to select a model, a pattern, and output file name.

### How to cite ###
If biopax-pattern framework was useful for your research, please cite the below article in your publications.

Ö Babur, BA Aksoy, I Rodchenkov, SO Sümer, C Sander, E Demir [Pattern search in BioPAX models](http://bioinformatics.oxfordjournals.org/content/30/1/139.full.html) Bioinformatics (2014) 30 (1): 139-140.
doi: 10.1093/bioinformatics/btt539

### Contact ###
patternsearch@cbio.mskcc.org
