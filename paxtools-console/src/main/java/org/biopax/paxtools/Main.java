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
				" [\"seqDb=db,..\"] [\"chemDb=db,..\"] [-dontMergeInteractions] [-useNameIfNoId]" +
				" [\"mediator\"] [\"pubmed\"] [\"pathway\"] [\"resource\"] [\"source_loc\"] [\"target_loc\"] [\"path/to/a/mediator/field\"]\n" +
        		"\t- exports a BioPAX model to classic SIF (default, has 3 columns) or customizable SIF format;\n" +
				"\t  will use blacklist.txt file in the current directory, if present;\n" +
				"\t  one can list one or more relationship types to include or exclude to/from the analysis\n" +
				"\t  using 'include=' and/or 'exclude=', respectively, e.g., exclude=NEIGHBOR_OF,INTERACTS_WITH\n" +
				"\t  (mind using underscore instead of minus sign in the SIF type names; the default is to use all types);\n" +
				"\t  using 'seqDb=' and 'chemDb=', you can specify standard sequence/gene/chemical ID type(s)\n" +
				"\t  (can be just a unique prefix) to match actual xref.db values in the BioPAX model,\n" +
				"\t  e.g., \"seqDb=uniprot,hgnc,refseq\", and in that order, means: if a UniProt entity ID is found,\n" +
				"\t  other ID types ain't used; otherwise, if an 'hgnc' ID/Symbol is found... and so on;\n" +
				"\t  when not specified, then 'hgnc' (in fact, 'HGNC Symbol') for bio-polymers - \n" +
				"\t  and ChEBI IDs or name (if '-useNameIfNoId' is set) for chemicals - are selected;\n" +
				"\t  if '-extended' is used then the output will be the Pathway Commons' EXTENDED_BINARY_SIF format:\n" +
				"\t  one file - two sections separated with a single blank line - first come inferred SIF-like interactions -\n" +
				"\t  3 classic SIF and 4 extra colums, followed by interactors/nodes description section; comment lines start with #)\n" +
				"\t  if also '-andSif' flag is present (which only makes sense together with -extended), then the second\n" +
				"\t  output file, classic SIF, is also created (with the same name as the output's, plus '.sif' extension)")
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
				"\tthe options' order defines the results output order.")
		        {public void run(String[] argv) throws IOException{summarize(argv);} },
		blacklist("<input> <output>\n" +
		        "\t- creates a blacklist of ubiquitous small molecules, like ATP, \n"
		        + "\tfrom the BioPAX model and writes it to the output file. The blacklist can be used with\n "
		        + "\tpaxtools graph queries or when converting from the SAME BioPAX data to the SIF formats.")
				{public void run(String[] argv) throws IOException{blacklist(argv);} },
		pattern("\n\t- BioPAX pattern search tool (opens a new dialog window)")
				{public void run(String[] argv) throws IOException{pattern(argv);} },
        help("\n\t- prints this screen and exits\n")
		        {public void run(String[] argv) throws IOException{help();} };

        String description;
        int params;

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