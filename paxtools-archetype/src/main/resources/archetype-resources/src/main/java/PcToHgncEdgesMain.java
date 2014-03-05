#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

/**
 * Created with IntelliJ IDEA.
 * User: cannin
 * Date: 10/7/13
 * Time: 7:53 PM
 * To change this template use File | Settings | File Templates.
 */

import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.controller.Cloner;
import org.biopax.paxtools.controller.Completer;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.sif.InteractionRule;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.io.sif.SimpleInteractionConverter;
import org.biopax.paxtools.io.sif.level3.Group;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.query.QueryExecuter;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.controller.SimpleEditorMap;

import java.io.*;
import java.util.*;

public class PcToHgncEdgesMain {
    private final static Log log = LogFactory.getLog(PcToHgncEdgesMain.class);

    private static SimpleInteractionConverter getDefaultConverter(Map options, Set<String> blacklist) {
        return new SimpleInteractionConverter(options, blacklist, new org.biopax.paxtools.io.sif.level3.ControlRule(),
                new org.biopax.paxtools.io.sif.level3.ParticipatesRule(),
                new org.biopax.paxtools.io.sif.level3.ComponentRule(),
                new org.biopax.paxtools.io.sif.level3.ConsecutiveCatalysisRule(),
                new org.biopax.paxtools.io.sif.level3.ControlsTogetherRule(),
                new org.biopax.paxtools.io.sif.level3.ExpressionRule());
    }

    /*
    public static String convertToURI(String geneSymbol) {
        return "urn:biopax:RelationshipXref:HGNC_HGNC%3A" + hgncUtil.getID(geneSymbol);
    }
    */

    public static void main(String args[]) throws IOException {

        System.out.println("Timestamp: 3:30PM");

        String OUTPUT_FILE = "prior_network.tsv";
        Integer GRAPH_LIMIT = 1;

        //String in = "C:${symbol_escape}${symbol_escape}Users${symbol_escape}${symbol_escape}cannin${symbol_escape}${symbol_escape}Downloads${symbol_escape}${symbol_escape}pc4_hs_bp_owl${symbol_escape}${symbol_escape}Pathway Commons.4.homo sapiens.BIOPAX.owl";
        //String in = "C:${symbol_escape}${symbol_escape}Users${symbol_escape}${symbol_escape}cannin${symbol_escape}${symbol_escape}Downloads${symbol_escape}${symbol_escape}pc4_hs_bp_owl${symbol_escape}${symbol_escape}pc4_hs_bp.owl";

        String in = "pc4_hs_bp.owl";
        //String in = "C:${symbol_escape}${symbol_escape}Users${symbol_escape}${symbol_escape}cannin${symbol_escape}${symbol_escape}Downloads${symbol_escape}${symbol_escape}dna_replication.owl";

        String blacklistIn = "blacklist.txt";

        InputStream f = new FileInputStream(in);

        BioPAXIOHandler handler = new SimpleIOHandler();
        Model rawModel = handler.convertFromOWL(f);

        f.close();

        Set<String> symbols = new HashSet<String>();
        symbols.add("POLA1");
        symbols.add("POLE2");
        symbols.add("MCM2");
        symbols.add("E2F1");
        symbols.add("PCNA");

        Set<String> types = new HashSet<String>();
        types.add("INTERACTS_WITH");
        types.add("IN_SAME_COMPONENT");
        types.add("STATE_CHANGE");
        types.add("REACTS_WITH");
        types.add("METABOLIC_CATALYSIS");
        types.add("CO_CONTROL");
        types.add("SEQUENTIAL_CATALYSIS");
        types.add("UPREGULATE_EXPRESSION");

        Set<String> notTypes = new HashSet<String>();
        //notTypes.add("IN_SAME_COMPONENT");
        notTypes.add("GENERIC_OF");

        //Set<BioPAXElement> rawModelObjects = rawModel.getObjects();

        /*
        Set<EntityReference> rawModelObjects = rawModel.getObjects(EntityReference.class);

        HashSet<BioPAXElement> allXrefs = new HashSet<BioPAXElement>();

        //for (BioPAXElement rawModelObject : rawModelObjects)
        for (EntityReference rawModelObject : rawModelObjects)
        {
            Set<Xref> xrefs = rawModelObject.getXref();

            for(Xref xref : xrefs)
            {
                if(xref instanceof RelationshipXref && xref.getDb().equals("HGNC Symbol") && symbols.contains(xref.getId()))
                {
                    System.out.println("RDFId: " + rawModelObject.getRDFId());
                    System.out.println(rawModelObject.getName() + ": " + rawModelObject.getDisplayName());

                    System.out.println("DB: " + xref.getDb() + ": " + xref.getId());
                    allXrefs.add(rawModelObject);
                }
            }
        }
        */

        /*
        log.info("Check if the model has all the genes?");
        for(String symbol: symbols) {
            BioPAXElement bpe = rawModel.getByID(PathwayCommons2Util.convertToURI(symbol));
            if(bpe == null) {
                symbolToNodes.remove(symbol);
                log.warn("Could not find " + symbol + " on PC2.");
            }
        }
        log.info("Found " + symbolToNodes.size() + "/" + symbols.size() + " genes on PC2.");
        */

        // EXTRACTOR
        /*
        HashSet<BioPAXElement> allXrefs = new HashSet<BioPAXElement>();
        for (String symbol : symbols) {
            RelationshipXref xref = (RelationshipXref) rawModel.getByID(PathwayCommons2Util.convertToURI(symbol));
            allXrefs.add(xref);
        }
        */

        // QUERY FULL MODEL FOR NODES OF INTEREST
        //Set<BioPAXElement> result = QueryExecuter.runPathsBetween(allXrefs, rawModel, GRAPH_LIMIT);

        // COMPLETE MODEL
        //Completer c = new Completer(handler.getEditorMap());
        //result = c.complete(result, rawModel);

        //Cloner cln = new Cloner(handler.getEditorMap(), handler.getLevel().getDefaultFactory());
        //Model model = cln.clone(null, result);

        // LOAD BLACKLIST
        Set<String> blacklist = new HashSet<String>();

        FileReader fileReader = new FileReader(new File(blacklistIn));
        BufferedReader br = new BufferedReader(fileReader);

        String line = null;

        while ((line = br.readLine()) != null) {
            blacklist.add(line);
        }

        System.out.println("BLACKLIST SIZE: " + blacklist.size());

        // CONVERTER
        Map options = new HashMap();
        options.put(SimpleInteractionConverter.REDUCE_COMPLEXES, "");

        SimpleInteractionConverter converter = getDefaultConverter(options, blacklist);

        //List<InteractionRule> rules = SimpleInteractionConverter.getRules(BioPAXLevel.L3);
        //for(InteractionRule rule : rules) {
        //    System.out.println("Rule: " + rule.toString());
        //}

        String outFile = "test_result.txt";

        PrintStream out = null;

        FileOutputStream out1 = new FileOutputStream(outFile, true);
        System.out.println(outFile);
        out = new PrintStream(out1);

        Set<SimpleInteraction> sis = converter.inferInteractions(rawModel);

        String gene1 = "";
        String gene2 = "";

        for (SimpleInteraction si : sis) {


            //if(!notTypes.contains(si.getType().toString())) {
                gene1 = printXrefs(symbols, si.getSource(), "source");
                gene2 = printXrefs(symbols, si.getTarget(), "target");

                //if(!gene1.equals("") && !gene2.equals("") && symbols.contains(gene1) && symbols.contains(gene2)) {
                if(!gene1.equals("") && !gene2.equals("")) {
                    out.println(gene1 + " " + si.getType() + " " + gene2);
                }
            //}
        }

        //converter.writeInteractionsInSIFNX(model, out, out, Arrays.asList("EntityReference/name",
        //        "EntityReference/xref"),
        //       Arrays.asList("Interaction/dataSource/displayName"), true);

        out.flush();
        out.close();

        log.info("Done! Please see " + OUTPUT_FILE + " for results");
        System.out.println("DONE");
    }

    private static String printXrefs(Set<String> symbols, BioPAXElement e, String sourceTarget) {

        Class clsSource = e.getClass();
        //System.out.print("The type of the object is: " + clsSource.getName() + " ");

        String gene = "";

        if(e instanceof EntityReference) {
            if(sourceTarget.equals("source")) {
                EntityReference er = (EntityReference) e;
                gene = printXrefs(symbols, er);
            } else {
                EntityReference er = (EntityReference) e;
                gene = printXrefs(symbols, er);
            }
        }
        /*else if(e instanceof Group) {
            Group source = (Group) e;
            for (EntityReference er : source.getMemberEntityReference()) {
                gene = printXrefs(symbols, er);
            }
        }*/

        return gene;
    }

    private static String printXrefs(Set<String> symbols, EntityReference er) {
        Set<Xref> xrefs = er.getXref();

        //System.out.print(er.getRDFId() + " ");

        String gene = "";

        for(Xref xref : xrefs) {
            //if(xref instanceof RelationshipXref && xref.getDb().equals("HGNC Symbol") && symbols.contains(xref.getId()))
            if(xref instanceof RelationshipXref && xref.getDb().equals("HGNC Symbol"))
            //if(xref instanceof RelationshipXref)
            {
                //System.out.print(er.getRDFId() + " " + xref.getId() + " ");
                //System.out.print(xref.getId() + " ");

                gene = xref.getId();
            }
        }

        return gene;
    }
}
