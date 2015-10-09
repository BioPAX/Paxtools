#May.19.2009, questions to Gary Bader (gary.bader@utoronto.ca)
#Updated in September 2011

JPype can be used to control Java Paxtools from Python.

The example script worked on JPype 0.5.4.1 and Python 2.5.1, Java 1.5 on OS X; 
Python 2.6, Java 1.6 on Windows XP; JPype 0.5.4.2, Python 2.6, Java 1.6 on 
OS X 10.6; but it seems that JPype is quite stable and should work with multiple
versions of Java, Python and different OSes

1. Download and install JPype from http://jpype.sourceforge.net/
Hello world python script using JPype looks like this:

from jpype import *
startJVM(getDefaultJVMPath(), "-ea")
java.lang.System.out.println("hello world")
shutdownJVM()

2. Download Paxtools
Get the paxtools*.jar
'fat' JAR from http://www.biopax.org/downloads/paxtools/

3. Program in python using paxtools
See pythonPaxtoolsExample.py example script that creates
a simple BioPAX Level 3 OWL file ("test.owl").

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