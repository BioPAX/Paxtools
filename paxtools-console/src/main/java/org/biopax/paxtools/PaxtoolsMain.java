package org.biopax.paxtools;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.controller.*;
import org.biopax.paxtools.converter.LevelUpgrader;
import org.biopax.paxtools.io.*;
import org.biopax.paxtools.io.gsea.GSEAConverter;
import org.biopax.paxtools.io.sbgn.L3ToSBGNPDConverter;
import org.biopax.paxtools.io.sif.InteractionRule;
import org.biopax.paxtools.io.sif.SimpleInteractionConverter;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level2.entity;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.query.QueryExecuter;
import org.biopax.paxtools.query.algorithm.Direction;
import org.biopax.paxtools.client.BiopaxValidatorClient;
import org.biopax.paxtools.client.BiopaxValidatorClient.RetFormat;
import org.biopax.validator.jaxb.Behavior;
import org.mskcc.psibiopax.converter.PSIMIConverter;
import org.mskcc.psibiopax.converter.PSIMIBioPAXConverter;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

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
	        String command = argv[0];
	        if(command.startsWith("--"))  //accept both w and w/out --
		        command = command.substring(2);
	        Command.valueOf(command).run(argv);
        }
    }

    public static void fromPsimi(String[] argv) throws IOException {

        // some utility info
        System.out.println("PSI-MI to BioPAX Conversion Tool");
        System.out.println("Supports PSI-MI Level 2.5 (compact) model and BioPAX Level 2 or 3.");


        // check args - proper bp level
        Integer bpLevelArg = null;
        try {
            bpLevelArg = Integer.valueOf(argv[1]);
            if (bpLevelArg != 2 && bpLevelArg != 3) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            System.err.println("Incorrect BioPAX level specified: " + argv[1] 
            	+ " .  Please select level 2 or 3.");
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
			elements, model, 1, Direction.BOTHSTREAM, null);

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
        model = (new LevelUpgrader()).filter(model);
        if (model != null) {
            io.setFactory(model.getLevel().getDefaultFactory());
            io.convertToOWL(model, new FileOutputStream(argv[2]));
        }
    }

    /**
     *  Converts a BioPAX file to SBGN and saves it in a file.
     */
    public static void toSBGN(String[] argv) throws IOException
    {
        String input = argv[1];
        String output = argv[2];

        Model model = io.convertFromOWL(new FileInputStream(input));
        L3ToSBGNPDConverter l3ToSBGNPDConverter = new L3ToSBGNPDConverter();
        l3ToSBGNPDConverter.writeSBGN(model, output);
    }

    
    /**
     * Checks files by the online BioPAX validator 
     * using the validator client.
     * 
     * @see <a href="http://www.biopax.org/validator">BioPAX Validator Webservice</a>
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
        Integer maxErrs = null;
        Behavior level = null; //will report both errors and warnings
        String profile = null;
        
        // match optional args
		for (int i = 3; i < argv.length; i++) {
			if ("html".equalsIgnoreCase(argv[i])) {
				outf = RetFormat.HTML;
			} else if ("xml".equalsIgnoreCase(argv[i])) {
				outf = RetFormat.XML;
			} else if ("biopax".equalsIgnoreCase(argv[i])) {
				outf = RetFormat.OWL;
			} else if ("auto-fix".equalsIgnoreCase(argv[i])) {
				fix = true;
			} else if ("only-errors".equalsIgnoreCase(argv[i])) {
				level = Behavior.ERROR;
			} else if ((argv[i]).toLowerCase().startsWith("maxerrors=")) {
				String num = argv[i].substring(10);
				maxErrs = Integer.valueOf(num);
			} else if ("notstrict".equalsIgnoreCase(argv[i])) {
				profile = "notstrict";
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
                BiopaxValidatorClient val = new BiopaxValidatorClient();
                val.validate(fix, profile, outf, level, maxErrs, null, files.toArray(new File[]{}), os);
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
                                     Arrays.asList(argv[4].split(",")), Arrays.asList(argv[5].split(",")),false);
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

        System.out.println("(Paxtools Console) Available Operations:\n");
        for (Command cmd : Command.values()) {
            System.out.println(cmd.name() + " " + cmd.description);
        }

    }

    private static Model getModel(BioPAXIOHandler io,
                                  String fName) throws FileNotFoundException {
        FileInputStream file = new FileInputStream(fName);
        return io.convertFromOWL(file);
    }

	//----- Section: Printing summary -------------------------------------------------------------|
	
	public static void summarize(String[] argv) throws IOException {

		Model model = getModel(io, argv[1]);


//---- Debug code
//		for (Complex c : model.getObjects(Complex.class))
//		{
//			if (c.getComponent().size() == 1 &&
//				c.getComponent().iterator().next() instanceof SimplePhysicalEntity &&
//				c.getComponentStoichiometry().size() == 1 &&
//				c.getComponentStoichiometry().iterator().next().getStoichiometricCoefficient() > 1)
//			{
//				for (PhysicalEntity pe : c.getComponent())
//				{
//					if (pe instanceof SimplePhysicalEntity)
//					{
//						for (Xref xref : ((SimplePhysicalEntity) pe).getEntityReference().getXref())
//						{
//							if (xref.getDb().startsWith("HGNC"))
//							{
//								System.out.println(xref.getId());
//							}
//						}
//					}
//				}
//			}
//		}
//		BioPAXElement byID = model.getByID("http://biocyc.org/biopax/biopax-level3ComplexAssembly175733");
//---- Debug code

		final SimpleEditorMap em = (model.getLevel() == BioPAXLevel.L3) 
				? SimpleEditorMap.L3 : SimpleEditorMap.L2;

		for (Class<? extends BioPAXElement> clazz : sortToName(em.getKnownSubClassesOf(BioPAXElement.class)))
		{
			Set<? extends BioPAXElement> set = model.getObjects(clazz);
			int initialSize = set.size();
			set = filterToExactClass(set, clazz);
			
			String s = clazz.getSimpleName() + " = " + set.size();
			if (initialSize != set.size()) s += " (and " + (initialSize - set.size()) + " children)";
			System.out.println(s);

			Set<PropertyEditor> editors = em.getEditorsOf(clazz);
			for (PropertyEditor editor : editors)
			{
				Method getMethod = editor.getGetMethod();
				Class<?> returnType = getMethod.getReturnType();

				Map<Object, Integer> cnt = new HashMap<Object, Integer>();

				if (returnType.isEnum() ||
					implementsInterface(returnType, ControlledVocabulary.class))
				{
					for (BioPAXElement ele : set)
					{
						Set values = editor.getValueFromBean(ele);
						if (values.isEmpty())
						{
							increaseCnt(cnt, NULL);
						}
						else
						{
							increaseCnt(cnt, values.iterator().next());
						}
					}
				}
				else if (returnType.equals(Set.class) &&
					implementsInterface(editor.getRange(), ControlledVocabulary.class))
				{
					for (BioPAXElement ele : set)
					{
						Set values = editor.getValueFromBean(ele);
						if (values.isEmpty())
						{
							increaseCnt(cnt, EMPTY);
						}
						for (Object val : values)
						{
							increaseCnt(cnt, val);
						}
					}
				}

				if (!cnt.isEmpty())
				{
					String name = "-" 
						+ (returnType.equals(Set.class) ? editor.getRange().getSimpleName() : returnType.getSimpleName());

					System.out.print("\t" + name + ":");
					for (Object key : getOrdering(cnt))
					{
						System.out.print("\t" + key + " = " + cnt.get(key));
					}
					System.out.println();
				}
			}
		}

		String[] props = (model.getLevel() == BioPAXLevel.L3) 
			? new String[]{"UnificationXref/db","RelationshipXref/db"}
			: new String[]{"unificationXref/DB","relationshipXref/DB"};

		System.out.println("\nOther property counts\n");

		for (String prop : props)
		{
			Map<Object, Integer> cnt = new HashMap<Object, Integer>();
			List<String> valList = new ArrayList<String>();
			PathAccessor acc = new PathAccessor(prop, model.getLevel());
			
			boolean isString = false;
			
			for (Object o : acc.getValueFromModel(model))
			{
				if (o instanceof String) isString = true;
				
				String s = o.toString();
				valList.add(s);
				if (!cnt.containsKey(s)) cnt.put(s, 1);
				else cnt.put(s, cnt.get(s) + 1);
			}

			System.out.println(prop + "\t(" + cnt.size() + " distinct values):");

			// If the object is String, then all counts are 1, no need to print counts.
			if (isString)
			{
				Collections.sort(valList);
				for (String s : valList)
				{
					System.out.print("\t" + s);
				}
			}
			else
			{
				for (Object key : getOrdering(cnt))
				{
					System.out.print("\t" + key + " = " + cnt.get(key));
				}
			}
			System.out.println();
		}
	}

	private static List<Class<? extends BioPAXElement>> sortToName(Set<? extends Class<? extends BioPAXElement>>
			classes)
	{
		List<Class<? extends BioPAXElement>> list = new ArrayList<Class<? extends BioPAXElement>>(classes);

		Collections.sort(
				list, new Comparator<Class<? extends  BioPAXElement>>()
		{
			public int compare(Class<? extends BioPAXElement> clazz1, Class<? extends BioPAXElement> clazz2)
			{
				return clazz1.getName().substring(clazz1.getName().lastIndexOf(".")+1).compareTo(
					clazz2.getName().substring(clazz2.getName().lastIndexOf(".")+1));
			}
		});
		return list;
	}

	private static List<Object> getOrdering(final Map<Object, Integer> map)
	{
		List<Object> list = new ArrayList<Object>(map.keySet());
		Collections.sort(list, new Comparator<Object>()
		{
			public int compare(Object key1, Object key2)
			{
				int cnt1 = map.get(key1);
				int cnt2 = map.get(key2);

				if (cnt1 == cnt2) return key1.toString().compareTo(key2.toString());
				else return cnt2 - cnt1;
			}
		});
		return list;
	}
	
	private static Set<BioPAXElement> filterToExactClass(Set<? extends BioPAXElement> classSet, Class clazz)
	{
		Set<BioPAXElement> exact = new HashSet<BioPAXElement>();
		for (BioPAXElement ele : classSet)
		{
			if (ele.getModelInterface().equals(clazz)) exact.add(ele);
		}
		return exact;
	}

	private static final Object NULL = new Object(){
		@Override
		public String toString()
		{
			return "NULL";
		}
	};

	private static final Object EMPTY = new Object(){
		@Override
		public String toString()
		{
			return "EMPTY";
		}
	};

	private static boolean implementsInterface(Class clazz, Class inter)
	{
		for (Class anInter : clazz.getInterfaces())
		{
			if (anInter.equals(inter)) return true;
		}
		return false;
	}

	private static void increaseCnt(Map<Object, Integer> cnt, Object key)
	{
		if (!cnt.containsKey(key)) cnt.put(key, 0);
		cnt.put(key, cnt.get(key) + 1);
	}

	//-- End of Section; Printing summary ---------------------------------------------------------|
	
    enum Command {
        merge("<file1> <file2> <output>\n" +
        		"\t- merges file2 into file1 and writes it into output", 3)
		        {public void run(String[] argv) throws IOException{merge(argv);} },
        toSif("<file1> <output>\n" +
        		"\t- converts model to the simple interaction format", 2)
		        {public void run(String[] argv) throws IOException{toSif(argv);} },
        toSifnx("<file1> <outEdges> <outNodes> <node-prop1,node-prop2,..> <edge-prop1,edge-prop2,...>\n" +
        		"\t- converts model to the extendent simple interaction format", 4)
		        {public void run(String[] argv) throws IOException{toSifnx(argv);} },
        toSbgn("<biopax.owl> <output.sbgn>\n" +
        		"\t- converts model to the SBGN format.", 2)
                {public void run(String[] argv) throws IOException { toSBGN(argv); } },
        validate("<path> <out> [xml|html|biopax] [auto-fix] [only-errors] [maxerrors=n] [notstrict]\n" +
        		"\t- validate BioPAX file/directory (up to ~25MB in total size, -\n" +
        		"\totherwise download and run the stand-alone validator)\n" +
        		"\tin the directory using the online validator service\n" +
        		"\t(generates html or xml report, or gets the processed biopax\n" +
        		"\t(cannot be perfect though) see http://www.biopax.org/validator)", 3)
		        {public void run(String[] argv) throws IOException{validate(argv);} },
        integrate("<file1> <file2> <output>\n" +
        		"\t- integrates file2 into file1 and writes it into output (experimental)", 3)
		        {public void run(String[] argv) throws IOException{integrate(argv);} },
        toLevel3("<file1> <output>\n" +
        		"\t- converts level 1 or 2 to the level 3 file", 2)
		        {public void run(String[] argv) throws IOException{toLevel3(argv);} },
        fromPsimi("<level> <file1> <output>\n" +
        		"\t- converts PSI-MI Level 2.5 to biopax level 2 or 3 file", 3)
		        {public void run(String[] argv) throws IOException{fromPsimi(argv);} },
        toGSEA("<file1> <output> <database> [crossSpeciesCheck]\n" +
        		"\t- converts Level 1 or 2 or 3 to GSEA output.\n"
                + "\tUses that database identifier or the biopax URI if database is \"NONE\".\n"
                + "\tCross species check ensures participant protein is from same species\n" +
                "\tas pathway (set to true or false).", 4)
		        {public void run(String[] argv) throws IOException{toGSEA(argv);} },
        fetch("<file1> <id1,id2,..> <output>\n" +
        		"\t- extracts a sub-model from file1 and writes BioPAX to output", 3)
		        {public void run(String[] argv) throws IOException{fetch(argv);} },
        getNeighbors("<file1> <id1,id2,..> <output>\n" +
        		"\t- nearest neighborhood graph query (id1,id2 - of Entity sub-class only)", 3)
		        {public void run(String[] argv) throws IOException{getNeighbors(argv);} },
        summarize("<file>\n" +
        		"\t- prints a summary of the contents of the model", 1)
		        {public void run(String[] argv) throws IOException{summarize(argv);} },
        help("\t- prints this screen and exits", Integer.MAX_VALUE)
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
