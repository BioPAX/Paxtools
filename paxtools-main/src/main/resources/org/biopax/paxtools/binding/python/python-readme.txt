#May.19.2009, questions to Gary Bader (gary.bader@utoronto.ca)
# TODO: update this information for the paxtools2!

1. JPype can be used to control Java Paxtools from Python.
Download and install JPype from http://jpype.sourceforge.net/
This code was tested with JPype 0.5.4.1 and Python 2.5.1, Java 1.5 on OS X
and Pythonb 2.6, Java 1.6 on Windows XP, but it seems that JPype is quite
stable and should work with multiple versions of Java, Python and
different OSes

Hello world python script using JPype looks like this:

from jpype import *
startJVM(getDefaultJVMPath(), "-ea")
java.lang.System.out.println("hello world")
shutdownJVM()

2. [old paxtools] Check out and build Paxtools from sourceforge
See instructions at http://sourceforge.net/scm/?type=cvs&group_id=85345
cvs -d:pserver:anonymous@biopax.cvs.sourceforge.net:/cvsroot/biopax login cvs
-z3 -d:pserver:anonymous@biopax.cvs.sourceforge.net:/cvsroot/biopax co -P
Paxtools

Once downloaded, compile using ant (must have ant installed). This was tested
using the Sun Java compiler.  Type "ant" and ensure build was successful.
This creates the paxtools.jar file in the build/jars directory

3. Program in python using paxtools
Set this up by copying all jar files in paxtools/lib and the paxtools.jar file
itself to your own convenient lib directory

Update: with new paxtools2 - you only need one 'fat' JAR (but can also use individual modules):
get either the "paxtools-lite.jar" (smaller, uses simpleIO) or "paxtools-full.jar" (with jenaIO)
from http://downloads.sourceforge.net/project/biopax/paxtools/

(If you get the snapshot JARs without dependencies, set the class path appropriately for your OS 
and lib location - use absolute paths to all jars)

See pythonPaxtoolsExample.py for some example code that creates
a BioPAX Level 3 OWL file with a few simple objects. The resulting
OWL file from this script is included "test.owl"

Notes:
-If JPype gives an error that a class is not callable, it usually means
that you didn't properly set your java classpath. Separator between
jar paths on os x and linux is : and on windows is ;
-If you get an error "No matching overloads found." it means that
JPype couldn't find a method with the type signature matching the classes
that you provided.  This is usually caused because you don't have to set
the type of a variable in Python, so sometimes the variable is of a
different type than you think.  Try "print variableName" to see its type
-This has been verified to work in PyDev Eclipse python IDE