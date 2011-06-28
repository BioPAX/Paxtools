package org.biopax.paxtools;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.controller.*;
import org.biopax.paxtools.converter.OneTwoThree;
import org.biopax.paxtools.io.*;
import org.biopax.paxtools.io.gsea.GSEAConverter;
import org.biopax.paxtools.io.sif.InteractionRule;
import org.biopax.paxtools.io.sif.SimpleInteractionConverter;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level2.entity;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.query.QueryExecuter;
import org.biopax.paxtools.query.algorithm.Direction;
import org.biopax.validator.BiopaxValidatorClient;
import org.biopax.validator.BiopaxValidatorClient.RetFormat;
import org.biopax.validator.jaxb.Behavior;
import org.mskcc.psibiopax.converter.PSIMIConverter;
import org.mskcc.psibiopax.converter.PSIMIBioPAXConverter;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A command line accessible utility for basic Paxtools operations.
 */
public class PaxtoolsMain {

    public static Log log = LogFactory.getLog(PaxtoolsMain.class);
    private static SimpleIOHandler io = new SimpleIOHandler();

    public static void main(String[] argv) throws IOException, 
    InvocationTargetException, IllegalAccessException 
    {
        io.mergeDuplicates(true);
        if (argv.length == 0) {
            help();
        } else {
            Command.valueOf(argv[0]).run(argv);
        }
    }

    public static void fromPsimi(String[] argv) throws IOException {

        // some utility info
        System.out.println("PSI-MI to BioPAX Conversion Tool v2.0");
        System.out.println("Supports PSI-MI Level 2.5 (compact) model and BioPAX Level 2 or 3.");


        // check args - proper bp level
        Integer bpLevelArg = null;
        try {
            bpLevelArg = Integer.valueOf(argv[1]);
            if (bpLevelArg != 2 && bpLevelArg != 3) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            System.err.println("Incorrect BioPAX level specified: " + argv[1] + " .  Please select level 2 or 3.");
            System.exit(0);
        }

        // set strings vars
        String inputFile = argv[2];
        String outputFile = argv[3];

        // check args - input file exists
        if (!((File) (new File(inputFile))).exists()) {
            System.err.println("input filename: " + inputFile + " does not exist!");
            System.exit(0);
        }

        // create converter and convert file
        try {
            // set bp level
            BioPAXLevel bpLevel = (bpLevelArg == 2) ? BioPAXLevel.L2 : BioPAXLevel.L3;

            // create input/output streams
            FileInputStream fis = new FileInputStream(inputFile);
            FileOutputStream fos = new FileOutputStream(outputFile);

            // create converter
			PSIMIConverter converter = new PSIMIBioPAXConverter(bpLevel);

            // note streams will be closed by converter
            converter.convert(fis, fos);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }


    public static void toGSEA(String[] argv) throws IOException
    {
    	Model model = io.convertFromOWL(new FileInputStream(argv[1]));
        (new GSEAConverter(argv[3], new Boolean(argv[4]))).writeToGSEA(model, new FileOutputStream(argv[2]));
    }

    public static void getNeighbors(String[] argv) throws IOException
    {
        // set strings vars
        String in = argv[1];
        String[] ids = argv[2].split(",");
        String out = argv[3];

        // read BioPAX from the file
        Model model = io.convertFromOWL(new FileInputStream(in));

        // get elements (entities)
        Set<BioPAXElement> elements = new HashSet<BioPAXElement>();
        for (Object id : ids) {
            BioPAXElement e = model.getByID(id.toString());
            if (e != null && (e instanceof Entity || e instanceof entity)) {
                elements.add(e);
            } else {
                log.warn("Source element not found: " + id);
            }
        }

        // execute the 'nearest neighborhood' query
        Set<BioPAXElement> result = QueryExecuter.runNeighborhood(
			elements, model, 1, Direction.BOTHSTREAM);

        // auto-complete/clone the results in a new model
        // (this also cuts some less important edges, right?..)
        Completer c = new Completer(io.getEditorMap());
        result = c.complete(result, model);
        Cloner cln = new Cloner(io.getEditorMap(), io.getFactory());
        model = cln.clone(model, result); // new sub-model

        if (model != null) {
            log.info("Elements in the result model: " + model.getObjects().size());
            // export to OWL
            io.convertToOWL(model, new FileOutputStream(out));
        } else {
            log.error("NULL model returned.");
        }
    }

    public static void fetch(String[] argv) throws IOException {

        // set strings vars
        String in = argv[1];
        String[] ids = argv[2].split(",");
        String out = argv[3];

        Model model = io.convertFromOWL(new FileInputStream(in));
        io.setFactory(model.getLevel().getDefaultFactory());
        // extract and save the sub-model (defined by ids)
        io.convertToOWL(model, new FileOutputStream(out), ids);
    }

    public static void toLevel3(String[] argv) throws IOException {
        Model model = io.convertFromOWL(new FileInputStream(
                argv[1]));
        model = (new OneTwoThree()).filter(model);
        if (model != null) {
            io.setFactory(model.getLevel().getDefaultFactory());
            io.convertToOWL(model, new FileOutputStream(argv[2]));
        }
    }

    
    /**
     * Checks files by the online BioPAX validator 
     * using the validator client.
     * 
     * @see http://www.biopax.org/biopax-validator/ws.html
     * 
     * @param argv
     * @throws IOException
     */
    public static void validate(String[] argv) throws IOException 
    {
        String input = argv[1];
        String output = argv[2];
        // default options
        RetFormat outf = RetFormat.HTML;
        boolean fix = false;
        boolean normalize = false;
        Integer maxErrs = null;
        Behavior level = null; //will report both errors and warnings
        // match optional args
		for (int i = 3; i < argv.length; i++) {
			if ("html".equalsIgnoreCase(argv[i])) {
				outf = RetFormat.HTML;
			} else if ("xml".equalsIgnoreCase(argv[i])) {
				outf = RetFormat.XML;
			} else if ("biopax".equalsIgnoreCase(argv[i])) {
				outf = RetFormat.OWL;
			} else if ("normalize".equalsIgnoreCase(argv[i])) {
				normalize = true;
			} else if ("auto-fix".equalsIgnoreCase(argv[i])) {
				fix = true;
			} else if ("only-errors".equalsIgnoreCase(argv[i])) {
				level = Behavior.ERROR;
			} else if ((argv[i]).toLowerCase().startsWith("maxerrors=")) {
				String num = argv[i].substring(10);
				maxErrs = Integer.valueOf(num);
			}
		}

        Collection<File> files = new HashSet<File>();
        File fileOrDir = new File(input);
        if (!fileOrDir.canRead()) {
            System.out.println("Cannot read " + input);
            System.exit(-1);
        }

        // collect files
        if (fileOrDir.isDirectory()) {
            // validate all the OWL files in the folder
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return (name.endsWith(".owl"));
                }
            };
            for (String s : fileOrDir.list(filter)) {
                files.add(new File(fileOrDir.getCanonicalPath()
                        + File.separator + s));
            }
        } else {
            files.add(fileOrDir);
        }

        // upload and validate using the default URL:
        // http://www.biopax.org/biopax-validator/check.html
        OutputStream os = new FileOutputStream(output);
        try {
            if (!files.isEmpty()) {
                BiopaxValidatorClient val =
                        new BiopaxValidatorClient();
                // do not auto-fix, nor normalize, nor filter errors, etc..
                val.validate(fix, normalize, outf, level, maxErrs, null, files.toArray(new File[]{}), os);
            }
        } catch (Exception ex) {
            // fall-back: not using the remote validator; trying to read files
            String msg = "Faild to Validate Using the Remote Service.\n " +
                    "Now Trying To Read Each File and Build The Model\n" +
                    "Watch Log Messages...\n";
            System.err.println(msg);
            os.write(msg.getBytes());

            for (File f : files) {
                Model m = null;
                msg = "";
                try {
                    m = io.convertFromOWL(new FileInputStream(f));
                    msg = "Model that contains "
                            + m.getObjects().size()
                            + " elements is created (check the log messages)\n";
                    os.write(msg.getBytes());
                } catch (Exception e) {
                    msg = "Error during validation" + e + "\n";
                    os.write(msg.getBytes());
                    e.printStackTrace();
                    log.error(msg);
                }
                os.flush();
            }
        }
    }

    public static void toSifnx(String[] argv) throws IOException {

        Model model = getModel(io, argv[1]);

        SimpleInteractionConverter sic =
                new SimpleInteractionConverter(SimpleInteractionConverter
                        .getRules(model.getLevel()).toArray(new InteractionRule[]{}));

        sic.writeInteractionsInSIFNX(model, new FileOutputStream(argv[2]), new FileOutputStream(argv[3]),
                                     Arrays.asList(argv[4].split(",")), Arrays.asList(argv[5].split(",")));
    }

    public static void toSif(String[] argv) throws IOException {

        Model model = getModel(io, argv[1]);

        SimpleInteractionConverter sic =
                new SimpleInteractionConverter(SimpleInteractionConverter
                        .getRules(model.getLevel()).toArray(new InteractionRule[]{}));

        sic.writeInteractionsInSIF(model, new FileOutputStream(argv[2]));
    }

    public static void integrate(String[] argv) throws IOException {

        Model model1 = getModel(io, argv[1]);
        Model model2 = getModel(io, argv[2]);

        Integrator integrator =
                new Integrator(SimpleEditorMap.get(model1.getLevel()), model1, model2);
        integrator.integrate();

        io.setFactory(model1.getLevel().getDefaultFactory());
        io.convertToOWL(model1, new FileOutputStream(argv[3]));
    }

    public static void merge(String[] argv) throws IOException {

        Model model1 = getModel(io, argv[1]);
        Model model2 = getModel(io, argv[2]);

        Merger merger = new Merger(SimpleEditorMap.get(model1.getLevel()));
        merger.merge(model1, model2);

        io.setFactory(model1.getLevel().getDefaultFactory());
        io.convertToOWL(model1, new FileOutputStream(argv[3]));
    }


    static void help() {

        System.out.println("Available operations:");
        for (Command cmd : Command.values()) {
            System.out.println(cmd.name() + " : " + cmd.description);
        }

    }

    private static Model getModel(BioPAXIOHandler io,
                                  String fName) throws FileNotFoundException {
        FileInputStream file = new FileInputStream(fName);
        return io.convertFromOWL(file);
    }

    enum Command {
        merge("file1 file2 output\t\tmerges file2 into file1 and writes it into output", 3)
		        {public void run(String[] argv) throws IOException{merge(argv);} },
        toSif("file1 output\t\t\tconverts model to the simple interaction format", 2)
		        {public void run(String[] argv) throws IOException{toSif(argv);} },
        toSifnx("file1 outEdges outNodes writePublications prop1,prop2,..\tconverts model " +
        		"to the extendent simple interaction format", 4)
		        {public void run(String[] argv) throws IOException{toSifnx(argv);} },
        validate("path out [xml|html|biopax] [auto-fix] [normalize] [only-errors] [maxerrors=n]\t\t" +
        		"validates the BioPAX file (or all the files in the directory); " +
        		"writes the html report, xml report (including fixed xml-escaped biopax), " +
        		"or the biopax (fixed/normalized) only; see also: http://www.biopax.org/biopax-validator/ws.html", 3)
		        {public void run(String[] argv) throws IOException{validate(argv);} },
        integrate("file1 file2 output\t\tintegrates file2 into file1 and writes it into output (experimental)", 3)
		        {public void run(String[] argv) throws IOException{integrate(argv);} },
        toLevel3("file1 output\t\tconverts level 1 or 2 to the level 3 file", 2)
		        {public void run(String[] argv) throws IOException{toLevel3(argv);} },
        fromPsimi("level file1 output\t\tconverts PSI-MI Level 2.5 to biopax level 2 or 3 file", 3)
		        {public void run(String[] argv) throws IOException{fromPsimi(argv);} },
        toGSEA("file1 output database crossSpeciesCheck\t\tconverts level 1 or 2 or 3 to GSEA output."
                + "\t\tSearches database for participant id or uses biopax rdf id if database is \"NONE\"."
                + "\t\tCross species check ensures participant protein is from same species as pathway (set to true or false).", 4)
		        {public void run(String[] argv) throws IOException{toGSEA(argv);} },
        fetch("file1 id1,id2,.. output\t\textracts a sub-model from file1 and writes BioPAX to output", 3)
		        {public void run(String[] argv) throws IOException{fetch(argv);} },
        getNeighbors("file1 id1,id2,.. output\t\tnearest neighborhood graph query (id1,id2 - of Entity sub-class only)", 3)
		        {public void run(String[] argv) throws IOException{getNeighbors(argv);} },
        help("\t\t\t\t\t\tprints this screen and exits", Integer.MAX_VALUE)
		        {public void run(String[] argv) throws IOException{help();} };

        String description;
        int params;

        Command(String description, int params) {
            this.description = description;
            this.params = params;
        }

        public abstract void run(String[] argv) throws IOException;
    }
}
