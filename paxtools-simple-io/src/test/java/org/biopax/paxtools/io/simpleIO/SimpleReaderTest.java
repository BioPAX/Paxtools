package org.biopax.paxtools.io.simpleIO;

import junit.framework.TestCase;
import org.junit.Test;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.xref;
import org.biopax.paxtools.model.level3.Xref;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URISyntaxException;

public class SimpleReaderTest extends TestCase
{
	@Test
	public void testSimpleReaderL2() throws URISyntaxException
	{
		testDirectory(new SimpleReader(), BioPAXLevel.L2); //resources dir
	}		

	@Test
	public void testSimpleReaderL3() throws URISyntaxException
	{
		testDirectory(new SimpleReader(), BioPAXLevel.L3); 
	}
	
	private void testDirectory(SimpleReader simpleReader, BioPAXLevel level) throws URISyntaxException
	{
		String pathname = level.name();
		System.out.println("pathname = " + pathname);
		File testDir = new File(getClass()
				.getClassLoader().getResource(pathname).toURI()); 
		FilenameFilter filter = new FilenameFilter()
		{
		    public boolean accept(File dir, String name)
		    {
		        return (name.endsWith(".owl"));
		    }
		};
		System.out.println("testDir) = " + testDir);
		System.out.println(testDir.listFiles());
		for (String s : testDir.list(filter))
		{
		    try
		    {
			    System.out.println("starting "+s);
			     InputStream in = getClass().getClassLoader() // - at the root classpath
			     	.getResourceAsStream(pathname + File.separator + s);
				Model model =   simpleReader.convertFromOWL(in);
				assertNotNull(model);
				assertFalse(model.getObjects().isEmpty());
			    System.out.println("done (model has "+model.getObjects().size()+" objects)");
			    
			    if(model.getLevel()==BioPAXLevel.L3) {
			    	Xref x = (Xref) model.getObjects(Xref.class).toArray()[0];
			    	assertNotNull(x.getDb());
			    } else {
			    	xref x = (xref) model.getObjects(xref.class).toArray()[0];
			    	assertNotNull(x.getDB());
			    }
		    }
		    catch (Exception e)
		    {
		        System.out.println("Failed at testing " + s + "\n");
		        e.printStackTrace();
		        System.exit(1);
		    }
		}
	}
}
