from jpype import *
#call this to initialize use of Java
startJVM(getDefaultJVMPath(), "-ea","-Djava.class.path=/Users/admin/pythonPaxtools/lib/collections-generic-4.01.jar:/Users/admin/pythonPaxtools/lib/commons-logging.jar:/Users/admin/pythonPaxtools/lib/concurrent.jar:/Users/admin/pythonPaxtools/lib/icu4j_3_4.jar:/Users/admin/pythonPaxtools/lib/iri.jar:/Users/admin/pythonPaxtools/lib/jakarta-oro.jar:/Users/admin/pythonPaxtools/lib/jena.jar:/Users/admin/pythonPaxtools/lib/junit-4.1.jar:/Users/admin/pythonPaxtools/lib/log4j-1.2.12.jar:/Users/admin/pythonPaxtools/lib/paxtools.jar:/Users/admin/pythonPaxtools/lib/xercesImpl.jar:/Users/admin/pythonPaxtools/lib/xml-apis.jar")
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

#add elements to the model
#step 1: create an object using the factory
protein = l3Factory.createProtein()
#step 2: must set unique RDF ID and add to model for each object created
protein.setRDFId("protein1")
model.add(protein)
#step 3: add data to your object
protein.addAvailability("availability text")
cellLoc = l3Factory.createCellularLocationVocabulary()
cellLoc.setRDFId("cellularLocationVocabulary1")
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
exporter = paxPkg.io.simpleIO.SimpleExporter(paxPkg.model.BioPAXLevel.L3)
fileOS = javaIO.FileOutputStream("test.owl")
#output to stdout
exporter.convertToOWL(model, java.lang.System.out)
#output to a file
exporter.convertToOWL(model, fileOS)
#end use of jpype - docs say you can only do this once, so all java must be run before calling this
shutdownJVM() 