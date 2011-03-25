package org.biopax.paxtools.examples;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.controller.*;
import org.biopax.paxtools.io.simpleIO.SimpleReader;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.pathway;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.protein;
import org.biopax.paxtools.model.level2.unificationXref;
import org.biopax.paxtools.util.ClassFilterSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Set;


/**
 * This example class processes all the Level2 BioPAX OWL   
 * files in the input directory to find all the protein names
 * 
 * Notes:
 * 
 * - recent fix: it doesn't traverse into the NEXT-STEP property,
 * as it may lead beyond the boundaries of the pathway of interest!
 * 
 * - one may prefer using the Paxtools' jenaIO instead of the simpleIO:
 * 
 * import org.biopax.paxtools.io.jena.JenaIOHandler;
 * JenaIOHandler reader = new JenaIOHandler(null, BioPAXLevel.L2);
 * 
 */
public class ProteinNameLister
{
// ------------------------------ FIELDS ------------------------------

    private static Log log = LogFactory.getLog(ProteinNameLister.class);
    private static Fetcher fetcher;

// --------------------------- main() method ---------------------------

    public static void main(String[] args)
    {
		if(args.length != 1) {
			System.out.println("\nUse Parameter: path (to biopax OWL files)\n");
			System.exit(-1);
		}
    	
    	SimpleReader reader = new SimpleReader(BioPAXLevel.L2);
        final String pathname = args[0];
        File testDir = new File(pathname);
        
        /*
         * Customized Fetcher is to fix the issue with Level2 
         * - when NEXT-STEP leads out of the pathway...
         * (do not worry - those pathway steps that are part of 
         * the pathway must be in the PATHWAY-COMPONENTS set)
         */
        PropertyFilter nextStepPropertyFilter = new PropertyFilter() {
			public boolean filter(PropertyEditor editor) {
				return !editor.getProperty().equals("NEXT-STEP");
			}
		};
		fetcher = new Fetcher(
			new SimpleEditorMap(BioPAXLevel.L2), nextStepPropertyFilter);
        
        FilenameFilter filter = new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return (name.endsWith("owl"));     
            }
        };

        for (String s : testDir.list(filter))
        {
            try
            {
                process(pathname, s, reader);
            }
            catch (Exception e)
            {
                log.error("Failed at testing " + s, e);
            }
        }
    }

    private static void process(String pathname, String name,
                                SimpleReader reader)
            throws FileNotFoundException
    {
        System.out.println("--------------" + name + "---------");
        Model model =
        	reader.convertFromOWL(new FileInputStream(pathname + "/" + name));
        listProteinUnificationXrefsPerPathway(model);
    }


    public static void listProteinUnificationXrefsPerPathway(Model model)
    {
        Set<pathway> pathways = model.getObjects(pathway.class);
        for (pathway aPathway : pathways)
        {
            //printout aPathway's name
            System.out.println(aPathway.getNAME());

            //Use new fetcher to get all dependents in a new level2
            Model onePathwayModel =
                    BioPAXLevel.L2.getDefaultFactory().createModel();
            fetcher.fetch(aPathway, onePathwayModel);
            //get all proteins in the new level2
            Set<protein> proteins =
                    onePathwayModel.getObjects(protein.class);

            //iterate and print names
            for (protein aProtein : proteins)
            {
                System.out.println("\t" + aProtein.getNAME());
                //now list xrefs and print if uni
                Set<unificationXref> xrefs =
                        new ClassFilterSet<unificationXref>(
                                aProtein.getXREF(), unificationXref.class);
                for (unificationXref x : xrefs)
                {
                    System.out.println("\t\t" + x.getDB() + ":" + x.getID());
                }
            }
        }

    }

    /**
     * Here is a more elegant way of doing the previous method!
     * 
     * @param model
     */
    public static void listUnificationXrefsPerPathway(Model model)
    {
        // This is a visitor for elements in a pathway - direct and indirect
        Visitor visitor = new Visitor()
        {
            public void visit(BioPAXElement domain, Object range, Model model,
                              PropertyEditor editor)
            {
                if (range instanceof physicalEntity)
                {
                    // Do whatever you want with the pe and xref here
                    physicalEntity pe = (physicalEntity) range;
                    ClassFilterSet<unificationXref> unis=
                    new ClassFilterSet<unificationXref>(pe.getXREF(),
                                                        unificationXref.class);
                    for (unificationXref uni : unis)
                    {
                        System.out.println("pe.getNAME() = " + pe.getNAME());
                        System.out.println("uni = " + uni.getID());
                    }
                } 
            }
        };
        
        Traverser traverser = new Traverser(new SimpleEditorMap(BioPAXLevel.L2), visitor);
        
        Set<pathway> pathways = model.getObjects(pathway.class);
        for (pathway pathway : pathways)
        {
            traverser.traverse(pathway,model);
        }
    }
}

