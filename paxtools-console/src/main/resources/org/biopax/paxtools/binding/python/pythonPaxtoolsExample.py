###
# Example using BioPAX PaxTools from Python
# 
# PaxTools: http://www.biopax.org/paxtools, 
#           http://sourceforge.net/projects/biopax/files/paxtools/
# JPype:    http://jpype.sourceforge.net/
# BioPAX:   http://www.biopax.org
#
###

from jpype import *

#call this to initialize use of Java
startJVM(getDefaultJVMPath(), "-ea", "-Xmx1g", "-Djava.class.path=paxtools.jar")

#print out using java or python
java.lang.System.out.println("Starting pythonPaxToolsExample")
print java.lang.System.getProperty("java.version")
print java.lang.System.getProperty("java.class.path")

#get the paxtools root package as a shortcut
paxPkg = JPackage("org.biopax.paxtools")

#create a new BioPAX L3 factory
#l3Factory = paxPkg.impl.level3.Level3FactoryImpl()
l3Factory = paxPkg.model.BioPAXLevel.L3.getDefaultFactory()

#create a new empty BioPAX model
model = l3Factory.createModel()

#(highly recommended) use an xml base (URI prefix for elements we create)
xmlBase = "http://biopax.org/examples/pythonPaxtools#"
# set xml base 
# - before paxtools v4.0.0 2011/08:
# model.getNameSpacePrefixMap().put("", xmlBase)
# - since paxtools v4.0.0 2011/08:
model.setXmlBase(xmlBase)

#get BioPAX classes (model interfaces); in this example: Protein, CellularLocationVocabulary, UnificationXref:
proteinClass = java.lang.Class.forName("org.biopax.paxtools.model.level3.Protein", True, java.lang.ClassLoader.getSystemClassLoader())
cellularLocationCvClass = java.lang.Class.forName("org.biopax.paxtools.model.level3.CellularLocationVocabulary", True, java.lang.ClassLoader.getSystemClassLoader())
unificationXrefClass = java.lang.Class.forName("org.biopax.paxtools.model.level3.UnificationXref", True, java.lang.ClassLoader.getSystemClassLoader())

#create/add a couple of elements to the model
#step 1: create a simple protein state using the factory method and unique identifier (URI)
#        (in a real-world scenario, we would also add at least a protein reference with one unification xref)

protein = l3Factory.create(proteinClass, xmlBase + "protein1")
protein.addComment("python: created " + protein.getRDFId())

#NOTE: despite xmlBase was set for the entire model, you still have to use 
#      absolute URIs when creating your BioPAX objects; i.e., use this URI prefix 
#      when creating an Entity class object. However, do not necessarily use your
#      own xml base and id when creating a UtilityClass object (such as ProteinReference 
#      or ControlledVocabulary) for which a better absolute URI might be found (at identifiers.org).

#step 2: add to the model
model.add(protein)

#step 3: set some properties
protein.addAvailability("availability text")
cellLoc = l3Factory.create(cellularLocationCvClass, "http://identifiers.org/obo.go/GO:0005737")
model.add(cellLoc)
cellLoc.addComment("python: created " + cellLoc.getRDFId())
cellLoc.addTerm("cytoplasm")
protein.setCellularLocation(cellLoc)

#alternatively, one can create, set the id (URI), and add the element in one step
protein2 = model.addNew(proteinClass, xmlBase + "protein2")
protein2.addComment("created " + protein2.getRDFId())
# let's add a unification xref to the CV
ux = model.addNew(unificationXrefClass, xmlBase + "XREF_GO_0005737")
ux.setDb("GO")
ux.setId("GO:0005737")
cellLoc.addXref(ux)

#export the model to a BioPAX OWL file
javaIO = JPackage("java.io")
io = paxPkg.io.SimpleIOHandler(paxPkg.model.BioPAXLevel.L3)
fileOS = javaIO.FileOutputStream("test.owl")
io.convertToOWL(model, fileOS)
fileOS.close()

#import a BioPAX model from the file
fileIS = javaIO.FileInputStream("test.owl")
model2 = io.convertFromOWL(fileIS)
fileIS.close()

#output to console
io.convertToOWL(model, java.lang.System.out)

#end use of jpype - docs say you can only do this once, so all java must be run before calling this
shutdownJVM() 
