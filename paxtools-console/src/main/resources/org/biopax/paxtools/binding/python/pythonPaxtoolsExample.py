from jpype import *
#call this to initialize use of Java
#(with using a 'fat' paxtools.jar (with all dependencies built-in), setting long java.class.path is not required anymore - )
#startJVM(getDefaultJVMPath(), "-ea","-Djava.class.path=$PAXTOOLS_HOME/lib/collections-generic-4.01.jar:$PAXTOOLS_HOME/lib/commons-logging.jar:$PAXTOOLS_HOME/lib/concurrent.jar:$PAXTOOLS_HOME/lib/icu4j_3_4.jar:$PAXTOOLS_HOME/lib/iri.jar:$PAXTOOLS_HOME/lib/jakarta-oro.jar:$PAXTOOLS_HOME/lib/jena.jar:$PAXTOOLS_HOME/lib/junit-4.1.jar:$PAXTOOLS_HOME/lib/log4j-1.2.12.jar:$PAXTOOLS_HOME/lib/paxtools.jar:$PAXTOOLS_HOME/lib/xercesImpl.jar:$PAXTOOLS_HOME/lib/xml-apis.jar")
startJVM(getDefaultJVMPath(), "-ea","-Djava.class.path=paxtools.jar")
#print out using java or python
java.lang.System.out.println("Starting pythonPaxToolsExample")
print java.lang.System.getProperty("java.version")
print java.lang.System.getProperty("java.class.path")
#get the paxtools package as a shortcut
paxPkg = JPackage("org.biopax.paxtools")
#short form e.g. Document = JPackage('org').w3c.dom.Document
#create a new BioPAX model to add data to
l3Factory = paxPkg.impl.level3.Level3FactoryImpl()
model = l3Factory.createModel()

#will be using the following BioPAX classes (model interfaces):
proteinClass = java.lang.Class.forName("org.biopax.paxtools.model.level3.Protein", True, java.lang.ClassLoader.getSystemClassLoader())
cellularLocationCvClass = java.lang.Class.forName("org.biopax.paxtools.model.level3.CellularLocationVocabulary", True, java.lang.ClassLoader.getSystemClassLoader())

#add elements to the model
#step 1: create an object using the factory
protein = l3Factory.create(proteinClass, "protein1")
#step 2: must set unique RDF ID and add to model for each object created
model.add(protein)
#step 3: add data to your object
protein.addAvailability("availability text")
cellLoc = l3Factory.create(cellularLocationCvClass, "cellularLocationVocabulary1")
model.add(cellLoc)
cellLoc.addComment("comment")
cellLoc.addTerm("cytoplasm")
protein.setCellularLocation(cellLoc)

#or, do the creation, setting RDF ID and adding to model in one step
proteinClass = java.lang.Class.forName("org.biopax.paxtools.model.level3.Protein", True, java.lang.ClassLoader.getSystemClassLoader())
#once you get a reference to a Class object of a given type, you can reuse it across multiple calls to model.addNew method
protein2 = model.addNew(proteinClass, "protein2")
protein2.addComment("created protein2")

#export to BioPAX OWL
javaIO = JPackage("java.io")
io = paxPkg.io.SimpleIOHandler(paxPkg.model.BioPAXLevel.L3)
fileOS = javaIO.FileOutputStream("test.owl")
#output to stdout
io.convertToOWL(model, java.lang.System.out)
#output to a file
io.convertToOWL(model, fileOS)
fileOS.close()

#read from file
fileIS = javaIO.FileInputStream("test.owl")
model2 = io.convertFromOWL(fileIS)
io.convertToOWL(model, java.lang.System.out)

#end use of jpype - docs say you can only do this once, so all java must be run before calling this
shutdownJVM() 
