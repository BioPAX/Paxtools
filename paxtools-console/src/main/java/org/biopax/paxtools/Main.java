package org.biopax.paxtools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static org.biopax.paxtools.PaxtoolsMain.*;

/**
 * PaxtoolsMain console application
 * (very useful BioPAX utilities).
 */
public final class Main {
    final static Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] argv) throws Exception
	{
        if (argv.length == 0) {
            help();
        } else {
	        String command = argv[0];
	        Command.valueOf(command).run(argv);
        }
    }
	
    enum Command {
        merge("<file1> <file2> <output>\n" +
					"\t- merges file2 into file1 and writes it into output")
		        {public void run(String[] argv) throws IOException{merge(argv);} },
        toSIF("<input> <output> [-extended] [-andSif] [\"include=SIFType,..\"] [\"exclude=SIFType,..\"]" +
					" [\"seqDb=db,..\"] [\"chemDb=db,..\"] [-dontMergeInteractions] [-useNameIfNoId] [<property_accessor> ...]\n" +
					"\t- converts a BioPAX model to SIF (default) or custom SIF-like text format;\n" +
					"\t  will use blacklist.txt (recommended) file in the current directory, if present.\n" +
					"\t- Include or exclude to/from the analysis one or more relationship types by \n" +
					"\t  using 'include=' and/or 'exclude=', respectively, e.g., exclude=NEIGHBOR_OF,INTERACTS_WITH\n" +
					"\t  (mind using underscore instead of minus sign in the SIF type names; the default is to use all types).\n" +
					"\t- With 'seqDb=' and 'chemDb=', you can specify standard sequence/gene/chemical ID type(s)\n" +
					"\t  (can be just a unique prefix) to match actual xref.db values in the BioPAX model,\n" +
					"\t  e.g., \"seqDb=uniprot,hgnc,refseq\", and in that order, means: if a UniProt entity ID is found,\n" +
					"\t  other ID types ain't used; otherwise, if an 'hgnc' ID/Symbol is found... and so on;\n" +
					"\t  when not specified, then 'hgnc' (in fact, 'HGNC Symbol') for bio-polymers - \n" +
					"\t  and ChEBI IDs or name (if '-useNameIfNoId' is set) for chemicals - are selected.\n" +
					"\t- With '-extended' flag, the output will be the Pathway Commons TXT (Extended SIF) format:\n" +
					"\t  two sections separated with one blank line - first come inferred SIF interactions -\n" +
					"\t  'A\trelationship-type\tB' plus RESOURCE, PUBMED, PATHWAY, MEDIATOR extra columns, \n" +
					"\t  followed by interaction participants description section).\n" +
					"\t- If '-andSif' flag is present (only makes sense together with '-extended'), then the \n" +
					"\t  classic SIF output file is also created (will have '.sif' extension).\n" +
					"\t- Finally, <property_accessor>... list is to specify 4th, 5th etc. custom output columns;\n" +
					"\t  use pre-defined column names (accessors): \n" +
						"\t\tMEDIATOR,\n" +
						"\t\tPUBMED,\n" +
						"\t\tPMC,\n" +
						"\t\tCOMMENTS,\n" +
						"\t\tPATHWAY,\n" +
						"\t\tPATHWAY_URI,\n" +
						"\t\tRESOURCE,\n" +
						"\t\tSOURCE_LOC,\n" +
						"\t\tTARGET_LOC\n" +
					"\t  or custom biopax property path accessors (XPath-like expressions to apply to each mediator entity; \n" +
					"\t  see https://github.com/BioPAX/Paxtools/wiki/PatternBinaryInteractionFramework)")
		        {public void run(String[] argv) throws IOException{toSifnx(argv);} },
        toSBGN("<biopax.owl> <output.sbgn> [-nolayout]\n" +
        		"\t- converts model to the SBGN format and applies COSE layout unless optional -nolayout flag is set.")
                {public void run(String[] argv) throws IOException { toSbgn(argv); } },
        validate("<path> <out> [xml|html|biopax] [auto-fix] [only-errors] [maxerrors=n] [notstrict]\n" +
        		"\t- validate BioPAX file/directory (up to ~25MB in total size, -\n" +
        		"\totherwise download and run the stand-alone validator)\n" +
        		"\tin the directory using the online validator service\n" +
        		"\t(generates html or xml report, or gets the processed biopax\n" +
        		"\t(cannot be perfect though) see http://www.biopax.org/validator)")
		        {public void run(String[] argv) throws IOException{validate(argv);} },
        integrate("<file1> <file2> <output>\n" +
        		"\t- integrates file2 into file1 and writes it into output (experimental)")
		        {public void run(String[] argv) throws IOException{integrate(argv);} },
        toLevel3("<input> <output> [-psimiToComplexes]\n" +
        		"\t- converts BioPAX level 1 or 2, PSI-MI 2.5 and PSI-MITAB to the level 3 file;\n" +
        		"\t-psimiToComplexes forces PSI-MI Interactions become BioPAX Complexes instead MolecularInteractions.")
		        {public void run(String[] argv) throws IOException{toLevel3(argv);} },
        toGSEA("<input> <output> <db> [-crossSpecies] [-subPathways] [-notPathway] [organisms=9606,human,rat,..]\n" +
        		"\t- converts BioPAX data to the GSEA software format (GMT); options/flags:\n"
                + "\t<db> - gene/protein ID type; values: uniprot, hgnc, refseq, etc. (a name or prefix to match\n"
				+ "\t  ProteinReference/xref/db property values in the input BioPAX model).\n"
                + "\t-crossSpecies - allows printing on the same line gene/protein IDs from different species;\n"
				+ "\t-subPathways - traverse into sub-pathways to collect all protein IDs for a pathway.\n"
				+ "\t-notPathway - also list those protein/gene IDs that cannot be reached from pathways.\n"
				+ "\torganisms - optional filter; a comma-separated list of taxonomy IDs and/or names\n")
		        {public void run(String[] argv) throws IOException{toGSEA(argv);} },
        fetch("<input> <output> [uris=URI1,..] [-absolute] \n" +
        		"\t- extracts a self-integral BioPAX sub-model from file1 and writes to the output; options:\n" +
				"\turi=... - an optional list of existing in the model BioPAX elements' full URIs;\n" +
				"\t-absolute - set this flag to write full/absolute URIs to the output (i.e., 'rdf:about' instead 'rdf:ID').")
		        {public void run(String[] argv) throws IOException{fetch(argv);} },
        getNeighbors("<input> <id1,id2,..> <output>\n" +
        		"\t- nearest neighborhood graph query (id1,id2 - of Entity sub-class only)")
		        {public void run(String[] argv) throws IOException{getNeighbors(argv);} },
        summarize("<input> <output> [--model] [--pathways] [--hgnc-ids] [--uniprot-ids] [--chebi-ids]\n" +
        		"\t- (experimental) summary of the input BioPAX model;\n " +
				"\truns one or several analyses and writes to the output file;\n " +
				"\t'--model' - (default) BioPAX classes, properties and values summary;\n " +
				"\t'--pathways' - pathways and sub-pathways hierarchy;\n " +
				"\t'--hgnc-ids' - HGNC IDs/Symbols that occur in sequence entity references;\n " +
				"\t'--uniprot-ids' - UniProt IDs in protein references;\n " +
				"\t'--chebi-ids' - ChEBI IDs in small molecule references;\n " +
				"\t'--uri-ids' - URI,type,name(s) and standard identifiers (in JSON format) for each physical entity;\n " +
				"\tthe options' order defines the results output order.")
		        {public void run(String[] argv) throws IOException{summarize(argv);} },
		blacklist("<input> <output>\n" +
		        "\t- creates a blacklist of ubiquitous small molecules, like ATP, \n"
		        + "\tfrom the BioPAX model and writes it to the output file. The blacklist can be used with\n "
		        + "\tpaxtools graph queries or when converting from the SAME BioPAX data to the SIF formats.")
				{public void run(String[] argv) throws IOException{blacklist(argv);} },
		pattern("\n\t- BioPAX pattern search tool (opens a new dialog window)")
				{public void run(String[] argv){pattern(argv);} },
        help("\n\t- prints this screen and exits\n")
				{public void run(String[] argv){ help();} };

        String description;

        Command(String description) {
            this.description = description;
        }

        public abstract void run(String[] argv) throws IOException;
    }

	private static void help() {
		System.out.println("(PaxtoolsMain Console) Available Operations:\n");
		for (Command cmd : Command.values()) {
			System.out.println(cmd.name() + " " + cmd.description);
		}
		System.out.println("Commands can also use compressed input files (only '.gz').\n");
	}
}
