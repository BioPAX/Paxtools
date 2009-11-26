package org.biopax.paxtools.io.simpleIO;
/**
 * Created by IntelliJ IDEA.
 * User: Emek
 * Date: Feb 25, 2008
 * Time: 12:11:27 PM
 */

import junit.framework.TestCase;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

public class SimpleExporterTest extends TestCase
{

    @Test
    public void testExportL2() throws InvocationTargetException, IOException,
                                      IllegalAccessException
    {
    	SimpleExporter simpleExporter = new SimpleExporter(BioPAXLevel.L2);
        Model model = BioPAXLevel.L2.getDefaultFactory().createModel();
        FileOutputStream out = new FileOutputStream(
        		getClass().getResource("").getFile() + "/simple.owl"
        	);
        simpleExporter.convertToOWL(model, out);
        out.close();

    }
    
	@Test
	public void testReadWriteL2()
	{
		String s = "/samples/L2/biopax_id_557861_mTor_signaling.owl";
		SimpleReader simpleReader = new SimpleReader();
		
		System.out.println("file = " + s);
		    try
		    {
			    System.out.println("starting "+s);
			    InputStream in = getClass().getResourceAsStream(s);
			    assertNotNull(in);
				Model model =   simpleReader.convertFromOWL(in);
				assertNotNull(model);
				assertFalse(model.getObjects().isEmpty());
			    System.out.println("Model has "+model.getObjects().size()+" objects)");
			    FileOutputStream out =
	                new FileOutputStream(
	                	getClass().getResource("").getFile() + "/simpleReadWrite.owl"
	                	);
				SimpleExporter simpleExporter = new SimpleExporter(BioPAXLevel.L2);
				simpleExporter.convertToOWL(model, out);
				out.close();
		    }
		    catch (Exception e)
		    {
		        e.printStackTrace();
		        System.exit(1);
		    }
	}
	
	@Test
	public void testReadWriteL3()
	{
		String s = "/samples/L3/biopax3-short-metabolic-pathway.owl";
		SimpleReader simpleReader = new SimpleReader(BioPAXLevel.L3);
		
		System.out.println("file = " + s);
		    try
		    {
			    System.out.println("starting "+s);
			    InputStream in =  getClass().getResourceAsStream(s);
				Model model =   simpleReader.convertFromOWL(in);
				assertNotNull(model);
				assertFalse(model.getObjects().isEmpty());
			    System.out.println("Model has "+model.getObjects().size()+" objects)");
			    FileOutputStream out =
	                new FileOutputStream(
	                	getClass().getResource("").getFile() + "/simpleReadWrite.owl"
	                	);
				SimpleExporter simpleExporter = new SimpleExporter(BioPAXLevel.L3);
				simpleExporter.convertToOWL(model, out);
				out.close();
		    }
		    catch (Exception e)
		    {
		        e.printStackTrace();
		        System.exit(1);
		    }
	}
}