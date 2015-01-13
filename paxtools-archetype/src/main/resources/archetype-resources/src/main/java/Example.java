import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;

import java.io.*;
import java.util.*;

/**
 *
 * Basic example that shows reading in BioPAX OWL files and simple iteration over content
 *
 * @author cannin
 *
 */
public class Example {
    public static void main(String args[]) throws IOException {
        String inFileName = "dna_replication.owl";
        String outFileName = "test_result.txt";

        ClassLoader classLoader = Example.class.getClassLoader();
        File inFile = new File(classLoader.getResource(inFileName).getFile());

        InputStream f = new FileInputStream(inFile);

        BioPAXIOHandler handler = new SimpleIOHandler();
        Model rawModel = handler.convertFromOWL(f);

        f.close();

        PrintStream out = null;
        out = new PrintStream(new FileOutputStream(outFileName, true));

        // Get all BiochemicalReactions
        Set<BiochemicalReaction> rawModelObjects = rawModel.getObjects(BiochemicalReaction.class);

        // Print participants in BiochemicalReactions
        for (BiochemicalReaction rawModelObject : rawModelObjects) {
            String tmp = "BiochemicalReaction: " + rawModelObject.getRDFId();

            System.out.println(tmp);
            out.println(tmp);

            // BiochemicalReactions have "right" and "left" PhysicalEntity participants
            Set<PhysicalEntity> bpeSet = rawModelObject.getLeft();
            bpeSet.addAll(rawModelObject.getRight());

            for(PhysicalEntity bpe : bpeSet) {
                EntityReference er = null;

                // Check what type of entity
                if(bpe instanceof Protein) {
                    Protein p = (Protein) bpe;
                    er = p.getEntityReference();
                }

                if(bpe instanceof SmallMolecule) {
                    SmallMolecule p = (SmallMolecule) bpe;
                    er = p.getEntityReference();
                }

                if(er instanceof EntityReference) {
                    // Xrefs (or cross-references) include identifier information in BioPAX
                    Set<Xref> xrefs = er.getXref();

                    for(Xref xref : xrefs) {
                        tmp = "\tID: " + bpe.getRDFId() +
                                " NAME: " + bpe.getDisplayName() +
                                " DB: " + xref.getDb() + " DB_ID: " + xref.getId();

                        System.out.println(tmp);
                        out.println(tmp);
                    }
                } else {
                    tmp = "\tNONE";

                    System.out.println(tmp);
                    out.println(tmp);
                }
            }
        }

        out.flush();
        out.close();

        System.out.println("DONE. Results in " + outFileName);
    }
}
