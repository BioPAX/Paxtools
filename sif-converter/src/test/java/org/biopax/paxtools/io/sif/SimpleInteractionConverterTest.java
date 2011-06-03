package org.biopax.paxtools.io.sif;
/**
 * User: demir
 * Date: Jan 2, 2008
 * Time: 6:02:44 PM
 */


import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.sif.level2.ControlRule;
import org.biopax.paxtools.io.sif.level2.ParticipatesRule;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO write tests that actually check the SIF output is correct (not just that no exceptions happend...)
 * 
 * @author rodche
 *
 */
public class SimpleInteractionConverterTest
{
	SimpleInteractionConverter simpleInteractionConverter;
	static BioPAXIOHandler handler =  new SimpleIOHandler();
	// test out - to target/test-classes dir:
	static final String outFile = SimpleInteractionConverterTest.class.getResource("/").getPath()
		+ File.separator + "simpleInteractionConverterTest.out.txt";
	PrintStream out = null;
	
	@Before
	public void setupTest() throws IOException {
		FileOutputStream out1 = new FileOutputStream(outFile, true);
		FileDescriptor fd = out1.getFD();
		out = new PrintStream(out1);
	}
	
	@After
	public void finishTest() throws IOException {
		out.flush();
		out.close();
	}
	
//	@Test
	public void testWriteInteractionsInSIF() throws Exception
	{
		SimpleInteractionConverter converter = new SimpleInteractionConverter(
					new ControlRule());
		File testDir = new File(getClass().getResource("/L2").getFile());
		FilenameFilter filter = new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				return (name.endsWith("owl"));
			}
		};

		out.println("testWriteInteractionsInSIF (L2)");
		for (String s : testDir.list(filter))
		{
			InputStream in = getClass().getResourceAsStream("/L2/"+s); // this is classpath - no need to use a "separator"
			Model level2 = handler.convertFromOWL(in);
			converter.writeInteractionsInSIF(level2, out);
			in.close();
		}
	}


//    @Test
    public void testWriteInteractionsInSIFNX() throws Exception
	{
        Map options = new HashMap();
        options.put(SimpleInteractionConverter.REDUCE_COMPLEXES, "");

        SimpleInteractionConverter converter = new SimpleInteractionConverter(
                options, new ControlRule(), new ParticipatesRule());

		File testDir = new File(getClass().getResource("/L2").getFile());
		FilenameFilter filter = new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				return (name.endsWith("owl"));
			}
		};

		out.println("testWriteInteractionsInSIFNX (L2) ");
		for (String s : testDir.list(filter))
		{
			InputStream in = getClass().getResourceAsStream("/L2/" + s);
			Model level2 = handler.convertFromOWL(in);
			converter.writeInteractionsInSIFNX(level2, out, out,
			null,
			Arrays.asList("entity/NAME","entity/XREF","entity/ORGANISM"));
			in.close();
		}
	}
    
//	@Test
	public void testWriteInteractionsInSIFl3() throws Exception
	{
		SimpleInteractionConverter converter = new SimpleInteractionConverter(
					new org.biopax.paxtools.io.sif.level3.ControlRule(),
					new org.biopax.paxtools.io.sif.level3.ParticipatesRule(),
					new org.biopax.paxtools.io.sif.level3.ComponentRule(),
					new org.biopax.paxtools.io.sif.level3.ConsecutiveCatalysisRule(),
					new org.biopax.paxtools.io.sif.level3.ControlsTogetherRule());

		File testDir = new File(getClass().getResource("/L3").getFile());
		FilenameFilter filter = new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				return (name.endsWith("owl"));
			}
		};

		out.println("testWriteInteractionsInSIF (L3)");
		for (String s : testDir.list(filter))
		{
			InputStream in = getClass().getResourceAsStream("/L3/" + s);
			Model model = handler.convertFromOWL(in);
			converter.writeInteractionsInSIF(model,out);
			in.close();
		}
	}


    @Test
    public void testWriteInteractionsInSIFNXl3() throws Exception
	{
        Map options = new HashMap();
        options.put(SimpleInteractionConverter.REDUCE_COMPLEXES, "");

        SimpleInteractionConverter converter = new SimpleInteractionConverter(
                options, new org.biopax.paxtools.io.sif.level3.ControlRule(),
				new org.biopax.paxtools.io.sif.level3.ParticipatesRule(),
				new org.biopax.paxtools.io.sif.level3.ComponentRule(),
				new org.biopax.paxtools.io.sif.level3.ConsecutiveCatalysisRule(),
				new org.biopax.paxtools.io.sif.level3.ControlsTogetherRule());

		File testDir = new File(getClass().getResource("/L3").getFile());
		FilenameFilter filter = new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				return (name.endsWith("p.owl"));
			}
		};

		out.println("testWriteInteractionsInSIFNX (L3)");
		for (String s : testDir.list(filter))
		{
			InputStream in = getClass().getResourceAsStream("/L3/" + s);
			Model m = handler.convertFromOWL(in);
			converter.writeInteractionsInSIFNX(m,
					out,out, Arrays.asList("Entity/name","Entity/xref"), Arrays.asList("Entity/xref"));
			in.close();
		}
	}
}