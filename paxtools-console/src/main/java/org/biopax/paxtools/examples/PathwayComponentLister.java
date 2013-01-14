package org.biopax.paxtools.examples;

import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;


public class PathwayComponentLister 
{
    public static void printPathwayComponents(Model model)
    {
        String prefix = "";
        for (Pathway pathway : model.getObjects(Pathway.class))
        {
            printPathway(pathway, prefix);
        }
    }

    private static void printPathway(Pathway pathway, String prefix)
    {
        System.out.println(prefix+"Pathway:"+pathway);
        prefix=prefix+"\t";
        for (Process process : pathway.getPathwayComponent())
        {
            if(process instanceof Pathway)
            {
                printPathway((Pathway) process,prefix);
            }
            else System.out.println(prefix + process);
        }
    }
}
