package org.biopax.paxtools.io.sif;
/**
 * User: demir
 * Date: Jan 2, 2008
 * Time: 6:02:44 PM
 */


import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.sif.level2.ControlRule;
import org.biopax.paxtools.io.sif.level2.ParticipatesRule;
import org.biopax.paxtools.io.simpleIO.SimpleReader;

import static org.biopax.paxtools.io.sif.SimpleInteractionConverter.REDUCE_COMPLEXES;

import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.junit.Test;

import java.io.*;
import java.util.Map;
import java.util.HashMap;

public class SimpleInteractionConverterTest
{
	SimpleInteractionConverter simpleInteractionConverter;

	public void testWriteInteractionsInSIF() throws Exception
	{
		SimpleInteractionConverter converter = new SimpleInteractionConverter(
					new ControlRule());

		BioPAXIOHandler handler =  new SimpleReader(null, BioPAXLevel.L2);

		String pathname = "/samples/L2";
		File testDir = new File(getClass().getResource(pathname).toURI());
		FilenameFilter filter = new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				return (name.endsWith("owl"));
			}
		};

		for (String s : testDir.list(filter))

		{
			InputStream in = getClass().getResourceAsStream(pathname + "/" + s);
			Model level2 = handler.convertFromOWL(in);
			FileOutputStream out =
				new FileOutputStream("target/" + s + ".sif");
			converter.writeInteractionsInSIF(level2,out);
			in.close();
			out.close();
		}
	}


     @Test
     public void testWriteInteractionsInSIFNX() throws Exception
	{
        Map options = new HashMap();
        options.put(REDUCE_COMPLEXES, "");

        SimpleInteractionConverter converter = new SimpleInteractionConverter(
                options, new ControlRule(), new ParticipatesRule());

		BioPAXIOHandler handler =  new SimpleReader(null, BioPAXLevel.L2);
		String pathname = "/samples/L2";
		File testDir = new File(getClass().getResource(pathname).toURI());
		FilenameFilter filter = new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				return (name.endsWith("owl"));
			}
		};

		for (String s : testDir.list(filter))

		{
			InputStream in = getClass().getResourceAsStream(pathname + "/" + s);
			Model level2 = handler.convertFromOWL(in);
			FileOutputStream edges =
				new FileOutputStream("target/" + s + ".sifx");
			FileOutputStream nodes =
				new FileOutputStream("target/" +s + ".sifnx");
			converter.writeInteractionsInSIFNX(level2,edges,nodes,true, handler.getEditorMap(),"NAME","XREF");
            //todo
			in.close();
			edges.close();
			nodes.close();
		}
	}
}