package org.biopax.paxtools.scripting;


import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;





public class Shell
{
    public static Model load(String filename) throws FileNotFoundException
    {
            return new SimpleIOHandler().convertFromOWL(new FileInputStream(filename));
    }
}
