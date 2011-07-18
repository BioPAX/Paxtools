hg clone http://biopax.hg.sourceforge.net:8000/hgroot/biopax/paxtools

--merge <file1> <file2> <output>
--to-sif <file1> <output>
--to-sifnx <file1> <outEdges> <outNodes> prop1,prop2,etc...
--validate <path> <out> [xml|html]
--integrate <file1> <file2> <output> (experimental)
--to-level3 <file1> <output>
--psimi-to <level> <file1> <output>
--to-GSEA <file1> <output> <database> <crossSpeciesCheck>" converts level 1 or 2 or 3 to GSEA output. Searches database for participant id or uses biopax rdf id if database is NONE. Cross species check ensures participant protein is from same species as pathway (set to true or false).
--fetch <file1> <id1,id2,..> <output> extracts a sub-model (id1,id2, etc. - new “root” elements)
--get-neighbors <file1> <id1,id2,..> <output> nearest neighborhood graph query

java -jar paxtools.jar --merge file1.owl file2.owl output.owl

java -Xmx2048M -jar paxtools.jar --merge file1.owl file2.owl output.owl

java -cp paxtools.jar org.biopax.paxtools.PaxtoolsMain --validate file.owl