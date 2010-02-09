package org.biopax.paxtools.io.jena;

import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.InvocationTargetException;


public class JenaIOHandlerTest
{
    @Test
    public void testL2File() throws Exception
    {
        long l = System.currentTimeMillis();
        JenaIOHandler jenaIOHandler = new JenaIOHandler(null, BioPAXLevel.L2);
        jenaIOHandler.setConvertingFromLevel1ToLevel2(true);
	    jenaIOHandler.treatNilAsNull(true);
        readWriteReadTest("biopax_id_557861_mTor_signaling.owl", jenaIOHandler);
        l= System.currentTimeMillis()-l;
        System.out.println(l);
    }

    @Test
     public void testL3File() throws Exception
     {
         long l = System.currentTimeMillis();
         JenaIOHandler jenaIOHandler = new JenaIOHandler(null, BioPAXLevel.L3);
         jenaIOHandler.setConvertingFromLevel1ToLevel2(true);
 	     jenaIOHandler.treatNilAsNull(true);
         readWriteReadTest("biopax3-short-metabolic-pathway.owl", jenaIOHandler);
         l= System.currentTimeMillis()-l;
         System.out.println(l);
     }
  
    private void readWriteReadTest(String s, JenaIOHandler jenaIOHandler)
            throws IllegalAccessException, InvocationTargetException,
                   IOException {
        InputStream in = getClass().getClassLoader().getResourceAsStream(s);
        //jenaIOHandler.setStrict(false);
        Model model = jenaIOHandler.convertFromOWL(in);
        FileOutputStream out = new FileOutputStream( // to 'target' tests dir...
        		getClass().getResource("").getFile() 
        		+ File.separator + "out-" + s);
        jenaIOHandler.convertToOWL(model,out);
        in.close();
        out.close();
        in = new FileInputStream(new File(
        		getClass().getResource("").getFile() 
        		+ File.separator + "out-" + s));
	    Model level21 =
                jenaIOHandler.convertFromOWL(in);
        assert model.getObjects().size() == level21.getObjects().size();
	    in.close();
    }

    @Test
    public void scratchTest() throws Exception
    {
        Model model = BioPAXLevel.L2.getDefaultFactory().createModel();
        JenaIOHandler jenaIOHandler = new JenaIOHandler(null, BioPAXLevel.L2);
        FileOutputStream fileOutputStream =
                new FileOutputStream(getClass().getResource("").getFile() 
                		+ File.separator + "scratch.owl");
        jenaIOHandler.convertToOWL(model, fileOutputStream);
        fileOutputStream.close();
    }
}
