package org.biopax.paxtools.io.sif.level3;

import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

/**

 */
public class ProteinStateNetworkAnalyzerTest {

        static BioPAXIOHandler handler = new SimpleIOHandler();

        // test out - to target/test-classes dir:
        static final String outFile =
                StateNetworkAnalyzer.class.getResource("/").getPath() + "PSNAnalyzer.out" +
                        ".txt";

        PrintStream out = null;

        @Before
        public void setupTest() throws IOException
        {
            FileOutputStream out1 = new FileOutputStream(outFile, true);
            FileDescriptor fd = out1.getFD();
            System.out.println(outFile);
            out = new PrintStream(out1);
        }

        @After
        public void finishTest() throws IOException
        {
            out.flush();
            out.close();
        }

        @Test
        public void testProteinStateAnalysis() throws Exception
        {
            File testDir = new File(getClass().getResource("/L3").getFile());
            StateNetworkAnalyzer analyzer = new StateNetworkAnalyzer();


            for (String s : testDir.list(getFilter()))
            {
                InputStream in = getClass().getResourceAsStream("/L3/" + s);
                Model model = handler.convertFromOWL(in);
                analyzer.analyzeStates(model);
                analyzer.writeStateNetworkAnalysis(out);
                in.close();
            }
        }

        private FilenameFilter getFilter()
        {
            return new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return (name.endsWith(".owl"));
                }
            };
        }



}
