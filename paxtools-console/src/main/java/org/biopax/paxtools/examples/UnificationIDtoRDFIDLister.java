package org.biopax.paxtools.examples;

import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.unificationXref;
import org.biopax.paxtools.util.ClassFilterSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

/**
 * A basic example that shows how to list all unification xrefs.
 *
 * NOTE: This method is now outdated as it is easier to do this now with the new PathAccessors. I did not remove this
 * example, however, as it demonstrates many "low-level" operations of Paxtools.

 */
public class UnificationIDtoRDFIDLister
{
	private static Logger log = LoggerFactory.getLogger(UnificationIDtoRDFIDLister.class);

	static BioPAXIOHandler handler = new SimpleIOHandler();

	public static void main(String[] args)
		throws IllegalAccessException, InvocationTargetException
	{

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

	private static void processXrefs(String arg) throws
		FileNotFoundException,
		IllegalAccessException,
		InvocationTargetException
	{
		FileInputStream in =
			new FileInputStream(new File(arg));
		Model level2 = handler.convertFromOWL(in);

		Collection<unificationXref> unis = level2.getObjects(unificationXref.class);
		for (unificationXref uni : unis)
		{
			ClassFilterSet referrables = new ClassFilterSet(uni.isXREFof(),
				physicalEntity.class);
			for (Object referrable : referrables)
			{
				System.out
					.print(uni.getDB() + " : " + uni.getID() + " refers to " +
						((physicalEntity) referrable).getUri());
			}
		}
	}
}