package org.biopax.paxtools.io.jena;

import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

/**
 */
public class JenaIOHandlerTest
{
   
    @Test
     public void testAllL2FilesInTestDirectory() throws URISyntaxException
     {
         long l = System.currentTimeMillis();
         testAllFilesInTestDirectory(BioPAXLevel.L2);
         l= System.currentTimeMillis()-l;
         System.out.println(l);
     }

    @Test
     public void testAllL3FilesInTestDirectory() throws URISyntaxException
     {
         long l = System.currentTimeMillis();
         testAllFilesInTestDirectory(BioPAXLevel.L3);
         l= System.currentTimeMillis()-l;
         System.out.println(l);
     }


    public void testAllFilesInTestDirectory(BioPAXLevel level) throws URISyntaxException
    {
        JenaIOHandler jenaIOHandler = new JenaIOHandler(null, level);
        jenaIOHandler.setConvertingFromLevel1ToLevel2(true);
	    jenaIOHandler.treatNilAsNull(true);
        String pathname = "/samples/"+level.name();
        System.out.println("pathname = " + pathname);
        File testDir = new File(getClass().getResource(pathname).toURI());
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
                readWriteReadTest(pathname, s, jenaIOHandler);
            }
            catch (Exception e)
            {
                System.out.println("Failed at testing " + s + "\n");
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
  
    private void readWriteReadTest(String pathname, String s,
                                   JenaIOHandler jenaIOHandler)
            throws IllegalAccessException, InvocationTargetException,
                   IOException {
        InputStream in = getClass().getResourceAsStream(pathname + "/" + s);
        //jenaIOHandler.setStrict(false);
        Model model = jenaIOHandler.convertFromOWL(in);
        FileOutputStream out = new FileOutputStream("target/out-" + s);
        jenaIOHandler.convertToOWL(model,out);
        in.close();
        out.close();
        in = new FileInputStream(new File("target/out-" + s));
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
                new FileOutputStream("target/scratch.owl");
        jenaIOHandler.convertToOWL(model, fileOutputStream);
        fileOutputStream.close();
    }
}
