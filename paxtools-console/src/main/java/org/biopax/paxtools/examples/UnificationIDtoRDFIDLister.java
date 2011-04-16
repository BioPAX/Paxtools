package org.biopax.paxtools.examples;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.unificationXref;
import org.biopax.paxtools.util.ClassFilterSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

/**
 * User: root Date: Jan 18, 2007 Time: 4:56:53 PM
 */
public class UnificationIDtoRDFIDLister
{
	private static Log log = LogFactory.getLog(
		UnificationIDtoRDFIDLister.class);

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

		Set<unificationXref> unis =
			level2.getObjects(unificationXref.class);
		for (unificationXref uni : unis)
		{
			ClassFilterSet referrables = new ClassFilterSet(uni.isXREFof(),
				physicalEntity.class);
			for (Object referrable : referrables)
			{
				System.out
					.print(uni.getDB() + " : " + uni.getID() + " refers to " +
						((physicalEntity) referrable).getRDFId());
			}
		}
	}
}