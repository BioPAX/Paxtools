package org.biopax.paxtools.examples;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.XReferrable;
import org.biopax.paxtools.model.level2.relationshipXref;
import org.biopax.paxtools.model.level2.unificationXref;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

/**
 * User: Emek Demir Date: Jan 18, 2007 Time: 4:56:53 PM
 *
 * In this example we get all the unification xrefs in the model
 * and check if they point to the Gene Ontology. If this is the case
 * we convert them to relationship xrefs.
 */
public class GOUnificationXREFtoRelationshipXREFConverter
{
	private static Log log = LogFactory.getLog(
		GOUnificationXREFtoRelationshipXREFConverter.class);
   
	static BioPAXIOHandler reader = new SimpleIOHandler();

    /**
     *
     * @param args a space seperated list of owl files to be processed
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static void main(String[] args)
		throws IllegalAccessException, InvocationTargetException
	{
        // Process all the args
        for (String arg : args)
        {
            log.info(arg);
            if (arg.toLowerCase().endsWith("owl"))
            {
                try
                {
                    processXrefs(arg);
                }
                catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                }
            }

        }
	}

    /**
     * Main conversion method. Demonstrates how to read and write a BioPAX
     * model and accessing its objects.
     * @param arg file name to be processed
     * @throws FileNotFoundException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private static void processXrefs(String arg) throws
		FileNotFoundException,
		IllegalAccessException,
		InvocationTargetException
	{
		//Read in the model
        FileInputStream in =
			new FileInputStream(new File(arg));
		Model level2 =
			reader.convertFromOWL(in);

        //Get all unification xrefs.
        Set<unificationXref> unis =
			level2.getObjects(unificationXref.class);
		//Create another set for avoiding concurrent modifications
        Set<unificationXref> gos = new HashSet<unificationXref>();

        //Process all uni. refs
        for (unificationXref uni : unis)
		{
			log.trace(uni.getDB());
			//Assuming DB is represented as "GO"
            if (uni.getDB().equalsIgnoreCase("GO"))
			{
				//this it to avoid concurrent modification.
				log.info("scheduling " + uni.getRDFId());
				gos.add(uni);

			}
		}
        //Now we have a list of xrefs to be converted. Let's do it.
        for (unificationXref go : gos)
		{
			convert(go, level2);
		}
		//And finally write out the file. We are done !
        reader.convertToOWL(level2, new FileOutputStream(
			arg.substring(0, arg.lastIndexOf('.')) +
				"-converted.owl"));
	}

    /**
     * This method converts the given unification xref to a relationship xref
     * @param uni xref to be converted
     * @param level2 model containing the xref
     */
    private static void convert(unificationXref uni, Model level2)
	{
		//We can not simply convert a class, so we need to remove the
        //uni and insert a new relationship xref

        //First get all the objects that refers to this uni
        Set<XReferrable> referrables =
			new HashSet<XReferrable>(uni.isXREFof());

        //Create the new relationship xref in the model.
        relationshipXref relationshipXref =
        	level2.addNew(relationshipXref.class, uni.getRDFId());

        //Copy the fields from uni
		relationshipXref.setCOMMENT(uni.getCOMMENT());
		relationshipXref.setDB(uni.getDB());
		relationshipXref.setDB_VERSION(uni.getDB_VERSION());
		relationshipXref.setID(uni.getID());
		relationshipXref.setID_VERSION(uni.getID_VERSION());
		relationshipXref.setRELATIONSHIP_TYPE(
			"http://www.biopax.org/paxtools/convertedGOUnificationXREF");

        //Create a link to the new xref from all the owners.
        for (XReferrable referrable : referrables)
		{
			referrable.addXREF(relationshipXref);
		}

        //Remove the references to the old uni
        for (XReferrable referrable : referrables)
        {
            referrable.removeXREF(uni);
        }
        //Now remove it from the model.
        level2.remove(uni);

        //We are done!
    }
}

