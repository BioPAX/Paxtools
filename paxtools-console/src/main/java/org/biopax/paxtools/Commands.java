package org.biopax.paxtools;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.biopax.paxtools.client.BiopaxValidatorClient;
import org.biopax.paxtools.client.BiopaxValidatorClient.RetFormat;
import org.biopax.paxtools.controller.*;
import org.biopax.paxtools.converter.LevelUpgrader;
import org.biopax.paxtools.converter.psi.PsiToBiopax3Converter;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.io.gsea.GSEAConverter;
import org.biopax.paxtools.io.sbgn.L3ToSBGNPDConverter;
import org.biopax.paxtools.io.sbgn.ListUbiqueDetector;
import org.biopax.paxtools.io.sbgn.UbiqueDetector;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.entity;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.miner.*;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.biopax.paxtools.query.QueryExecuter;
import org.biopax.paxtools.query.algorithm.Direction;
import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.validator.jaxb.Behavior;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.zip.GZIPInputStream;

/*
 * BioPAX data tools.
 * This is only called from Main class.
 */
final class Commands {
	final static Logger log = LoggerFactory.getLogger(Commands.class);

	private static SimpleIOHandler io;

	private Commands() {
		throw new UnsupportedOperationException("Non-instantiable utility class.");
	}

	static {
		io = new SimpleIOHandler();
		io.mergeDuplicates(true);
	}

	/*
	 * arguments:
	 * <input> <output> <db> [-crossSpecies] [-subPathways] [-notPathway] [organisms=9606,mouse,10090,rat,human,..]
	 */
	static void toGSEA(String[] argv) throws IOException {
		//argv[0] is the command name ('toGsea')
		boolean crossSpecies = false; //cross-check is enabled (i.e., no mixing different species IDs in one row)
		boolean subPathways = false; //no sub-pathways (i.e., going into sub-pathways is not enabled)
		boolean notPathways = false;
		Set<String> organisms = new HashSet<>();

		if (argv.length < 4)
			throw new IllegalArgumentException("Not enough arguments: " + argv);

		if (argv.length > 4) {
			for (int i = 4; i < argv.length; i++) {
				if("-crossSpecies".equalsIgnoreCase(argv[i])) {
					crossSpecies = true;
				}
				else if("-subPathways".equalsIgnoreCase(argv[i])) {
					subPathways = true;
				}
				else if("-notPathway".equalsIgnoreCase(argv[i])) {
					notPathways = true;
				}
				else if(argv[i].startsWith("organisms=")) {
					for(String o : argv[i].substring(10).split(",")) {
						organisms.add(o.trim().toLowerCase());
					}
				}
			}
		}

		// The Constructor args: GSEAConverter(idTypeNameOrPrefix, crossSpeciesCheckEnabled?, skipSubPathways?)
		GSEAConverter gseaConverter = new GSEAConverter(argv[3], !crossSpecies, !subPathways);
		gseaConverter.setSkipOutsidePathways(!notPathways);
		gseaConverter.setAllowedOrganisms(organisms);//if organisms is empty then all species are allowed (no filtering)
		gseaConverter.writeToGSEA(io.convertFromOWL(getInputStream(argv[1])), new FileOutputStream(argv[2]));
	}

	static void getNeighbors(String[] argv) throws IOException
	{
		// set strings vars
		String in = argv[1];
		String[] ids = argv[2].split(",");
		String out = argv[3];

		// read BioPAX from the file
		Model model = io.convertFromOWL(getInputStream(in));

		// get elements (entities)
		Set<BioPAXElement> elements = new HashSet<>();
		for (Object id : ids) {
			BioPAXElement e = model.getByID(id.toString());
			if (e != null && (e instanceof Entity || e instanceof entity)) {
				elements.add(e);
			} else {
				log.warn("Source element not found: " + id);
			}
		}

		// execute the 'nearest neighborhood' query
		Collection<BioPAXElement> result = QueryExecuter
			.runNeighborhood(elements, model, 1, Direction.BOTHSTREAM);

		// auto-complete/clone the results in a new model
		// (this also cuts some less important edges, right?..)
		Completer c = new Completer(io.getEditorMap());
		Cloner cln = new Cloner(io.getEditorMap(), io.getFactory());
		model = cln.clone(c.complete(result)); //a new sub-model

		if (model != null) {
			log.info("Elements in the result model: " + model.getObjects().size());
			// export to OWL
			io.convertToOWL(model, new FileOutputStream(out));
		} else {
			log.error("NULL model returned.");
		}
	}

	static void fetch(String[] argv) throws IOException {
		// set strings vars
		String in = argv[1];
		String out = argv[2];
		String[] uris = new String[]{}; //empty
		boolean absoluteUris = false;
		if(argv.length > 3) {
			for(int i=3; i<argv.length; i++) {
				String param = argv[i];
				if (param.startsWith("uris=")) {
					uris = param.substring(5).split(",");
				}
				else if(param.startsWith("-absolute")) {
					absoluteUris = true;
				}
			}
		}

		// import the model
		log.info("Loading the BioPAX model from " + in);
		Model model = io.convertFromOWL(getInputStream(in));
		log.info("Successfully loaded the BioPAX model. Writing to the output: " + out);

		// extract and save the (sub-)model
		SimpleIOHandler biopaxWriter = new SimpleIOHandler(model.getLevel());
		biopaxWriter.absoluteUris(absoluteUris);
		biopaxWriter.convertToOWL(model, new FileOutputStream(out), uris);
		log.info("Done.");
	}

	static void toLevel3(String[] argv) throws IOException {
		final String input = argv[1];
		final String output = argv[2];

		boolean forcePsiInteractionToComplex = false;
		if(argv.length > 3) {
			for(int i=3; i<argv.length; i++) {
				String param = argv[i];
				if (param.equalsIgnoreCase("-psimiToComplexes")) {
					forcePsiInteractionToComplex = true;
				}
			}
		}

		Type type = detect(input);

		InputStream is = getInputStream(input);
		FileOutputStream os = new FileOutputStream(output);

		try {
			switch (type) {
				case BIOPAX:
					Model model = io.convertFromOWL(is);
					model = (new LevelUpgrader()).filter(model);
					if (model != null) {
						io.setFactory(model.getLevel().getDefaultFactory());
						io.convertToOWL(model, os); //os is closed already
					}
					break;

				case PSIMI:
					PsiToBiopax3Converter psimiConverter = new PsiToBiopax3Converter();
					psimiConverter.convert(is, os, forcePsiInteractionToComplex);
					os.close();
					break;

				default: //MITAB
					psimiConverter = new PsiToBiopax3Converter();
					psimiConverter.convertTab(is, os, forcePsiInteractionToComplex);
					os.close();
					break;
			}

		} catch (Exception e) {
			throw new RuntimeException("Failed to convert " +
				input + "to BioPAX L3", e);
		}
	}

	private enum Type {
		BIOPAX,
		PSIMI,
		PSIMITAB
	}

	//read a few lines to detect it's a BioPAX vs. PSI-MI vs. PSI-MITAB data.
	private static Type detect(String input) {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(getInputStream(input)));
			int linesToCheck = 20;
			while (linesToCheck-- > 0) {
				sb.append(reader.readLine()).append('\n');
			}
			reader.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		String buf = sb.toString();
		if (buf.contains("<rdf:RDF") && buf.contains("http://www.biopax.org/release/biopax")) {
			return Type.BIOPAX;
		}
		else if (buf.contains("<entrySet") && buf.contains("/psidev")) {
			return Type.PSIMI;
		}
		else
			return Type.PSIMITAB; //default/guess
	}

	static void toSbgn(String[] argv) throws IOException {
		String input = argv[1];
		String output = argv[2];
		Model model = io.convertFromOWL(getInputStream(input));
		boolean doLayout = true;
		if(argv.length > 3) {
			for(int i=3; i<argv.length; i++) {
				String param = argv[i];
				if (param.equalsIgnoreCase("-nolayout")) {
					doLayout = false;
					break;
				}
			}
		}

		//use blacklist.txt file if exists
		Blacklist blackList = null;
		File blacklistFile = new File("blacklist.txt");
		if(blacklistFile.exists()) {
			log.info("toSBGN: using blacklist.txt from current directory");
			blackList = new Blacklist(new FileInputStream(blacklistFile));
		} else {
			log.info("toSBGN: not blacklisting any ubiquitous molecules (no blacklist.txt found)");
		}

		final UbiqueDetector ubd = (blackList != null) ? new ListUbiqueDetector(blackList.getListed()) : null;
		L3ToSBGNPDConverter l3ToSBGNPDConverter = new L3ToSBGNPDConverter(ubd, null, doLayout);
		l3ToSBGNPDConverter.writeSBGN(model, output);
	}

	static void validate(String[] argv) throws IOException
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

		Collection<File> files = new HashSet<>();
		File fileOrDir = new File(input);
		if (!fileOrDir.canRead()) {
			System.out.println("Cannot read " + input);
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
			String msg = "Unable to check with the biopax-validator web service: \n " +
				ex.toString() +
				"\n Fall-back: trying to parse the file(s) with paxtools " +
				"(up to the first syntax error in each file)...\n";
			log.error(msg, ex);
			os.write(msg.getBytes());

			for (File f : files) {
				try {
					Model m = io.convertFromOWL(getInputStream(f.getPath()));
					msg = "Model that contains "
						+ m.getObjects().size()
						+ " elements is successfully created from "
						+ f.getPath()
						+ " (check the console output for warnings).\n";
					os.write(msg.getBytes());
				} catch (Exception e) {
					msg = "Error: " + e +
						" in building a BioPAX Model from: " +
						f.getPath() + "\n";
					os.write(msg.getBytes());
					e.printStackTrace();
					log.error(msg);
				}
				os.flush();
			}
		}
	}

	static void toSifnx(String[] argv) throws IOException {
		boolean extended = false; //if it stays 'false', then andSif==true will be in effect automatically
		boolean andSif = false; //if extended==false, SIF will be generated as it would be andSif==true
		boolean mergeInteractions = true;
		boolean useNameIfNoId = false;
		final Collection<SIFEnum> include = new HashSet<>();
		final Collection<SIFEnum> exclude = new HashSet<>();
		final ConfigurableIDFetcher idFetcher = new ConfigurableIDFetcher();
		final List<String> customFieldList = new ArrayList<>(); //there may be custom field names (SIF mediators)
		//process arguments
		if(argv.length > 3) {
			for(int i=3; i<argv.length; i++) {
				String param = argv[i];
				if (param.startsWith("seqDb=")) {
					//remove the 'seqDb=' and split comma-sep. values (a single val. no comma is gonna be fine too)
					for (String db : param.substring(6).split(","))
						idFetcher.seqDbStartsWithOrEquals(db);
				} else if (param.startsWith("chemDb=")) {
					for (String db : param.substring(7).split(","))
						idFetcher.chemDbStartsWithOrEquals(db);
				} else if (param.equalsIgnoreCase("-andSif")) {
					andSif = true;
				} else if (param.equalsIgnoreCase("-extended")) {
					extended = true;
				} else if (param.equalsIgnoreCase("-dontMergeInteractions")) {
					mergeInteractions = false;
				} else if (param.equalsIgnoreCase("-useNameIfNoId")) {
					useNameIfNoId = true;
				} else if (param.startsWith("include=")) {
					for (String t : param.substring(8).split(","))
						include.add(SIFEnum.valueOf(t.toUpperCase()));
				} else if (param.startsWith("exclude=")) {
					for (String t : param.substring(8).split(","))
						exclude.add(SIFEnum.valueOf(t.toUpperCase())); //e.g. NEIGHBOR_OF
				} else {
					OutputColumn.Type type = OutputColumn.Type.getType(param.toUpperCase());
					if ((type != null && type != OutputColumn.Type.CUSTOM) || param.contains("/"))
					{
						if(!param.contains("/"))
							customFieldList.add(param.toUpperCase());
						else
							customFieldList.add(param);
					}
				}
			}
		}

		//fall back to defaults when no ID types were provided
		if(idFetcher.getChemDbStartsWithOrEquals().isEmpty()) {
			idFetcher.chemDbStartsWithOrEquals("chebi");
		}
		if(idFetcher.getSeqDbStartsWithOrEquals().isEmpty()) {
			idFetcher.chemDbStartsWithOrEquals("hgnc");
		}
		idFetcher.useNameWhenNoDbMatch(useNameIfNoId);

		Model model = getModel(io, argv[1]);

		if(mergeInteractions)
			ModelUtils.mergeEquivalentInteractions(model);

		//Create a new SIF searcher:
		//set SIF miners to use (default is to use all types, given no include/exclude args provided)
		final Collection<SIFEnum> sifTypes = (include.isEmpty())
			? new HashSet<>(Arrays.asList(SIFEnum.values())) : include;
		for(SIFType t : exclude) {
			sifTypes.remove(t); //remove if exists, otherwise - ignore
		}
		SIFSearcher searcher = new SIFSearcher(idFetcher, sifTypes.toArray(new SIFEnum[]{}));
		log.info("toSIF: using SIFTypes: " + sifTypes);
		//load and set blacklist.txt file if exists
		File blacklistFile = new File("blacklist.txt");
		if(blacklistFile.exists()) {
			log.info("toSIF: using blacklist.txt from current directory");
			searcher.setBlacklist(new Blacklist(new FileInputStream(blacklistFile)));
		} else {
			log.info("toSIF: not blacklisting ubiquitous molecules (no blacklist.txt found)");
		}

		File outputFile = new File(argv[2]);
		OutputStream outputStream = new FileOutputStream(outputFile);

		if (extended) {
			//using built-in PC EXTENDED_BINARY_SIF format (customFieldList parameter is ignored)
			Set<SIFInteraction> binaryInts = searcher.searchSIF(model);
			ExtendedSIFWriter.write(binaryInts, outputStream);
		} else if (customFieldList.isEmpty()) {
			searcher.searchSIF(model, outputStream); //classic SIF
		} else {
			// not really SIF format (this is useful but extra columns sometimes cannot be parsed correctly, by e.g. Cytoscape)
			log.info("toSIF: using custom fields (extra columns): " + customFieldList);
			searcher.searchSIF(model, outputStream, new CustomFormat(customFieldList.toArray(new String[]{})));
		}
		//outputStream is closed at this point (inside searchSIF(..) or write(..) method)

		if(extended && andSif) {
			//convert the file into classic thee-column SIF format
			sifnxToSif(outputFile.getPath(), outputFile.getPath()+".sif");
		}

		log.info("toSIF: done.");
	}

	static void sifnxToSif(String inputFile, String outputFile) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(getInputStream(inputFile)));
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFile));
		//skip the first line (headers)
		if(reader.ready()) reader.readLine();
		while(reader.ready()) {
			String line = reader.readLine();
			//stop at the first blank line (because next come nodes with attributes)
			if(line==null || line.isEmpty())
				break;
			//keep only the first three columns (otherwise, it's not gonna be SIF format)
			writer.write(StringUtils.join(Arrays.copyOfRange(line.split("\t", 4), 0, 3), '\t') + '\n');
		}
		reader.close();
		writer.close();
	}

	static void integrate(String[] argv) throws IOException {
		Model model1 = getModel(io, argv[1]);
		Model model2 = getModel(io, argv[2]);

		Integrator integrator = new Integrator(SimpleEditorMap.get(model1.getLevel()), model1, model2);
		integrator.integrate();

		io.setFactory(model1.getLevel().getDefaultFactory());
		io.convertToOWL(model1, new FileOutputStream(argv[3]));
	}

	static void merge(String[] argv) throws IOException {
		Model model1 = getModel(io, argv[1]);
		Model model2 = getModel(io, argv[2]);

		Merger merger = new Merger(SimpleEditorMap.get(model1.getLevel()));
		merger.merge(model1, model2);

		io.setFactory(model1.getLevel().getDefaultFactory());
		io.convertToOWL(model1, new FileOutputStream(argv[3]));
	}

	static void blacklist(String[] argv) throws IOException {
		Model model = getModel(io, argv[1]);
		BlacklistGenerator3 gen = new BlacklistGenerator3();
		Blacklist blacklist = gen.generateBlacklist(model);
		blacklist.write(new FileOutputStream(argv[2]));
	}

	static void pattern(String[] argv) {
		Dialog.main(argv);
	}

	private static Model getModel(BioPAXIOHandler io, String fName) throws IOException {
		return io.convertFromOWL(getInputStream(fName));
	}

	static void summarize(String[] argv) throws IOException {
		log.debug("Importing the input model from " + argv[1] + "...");
		final Model model = getModel(io, argv[1]);
		final PrintStream out = new PrintStream(argv[2]);
		//run a specific or default analysis
		if(argv.length>3) {
			for(int i=3; i < argv.length; i++) {
				if(argv[i].equals("--model")) {
					summarize(model, out);
				} else if(argv[i].equals("--pathways")) {
					summarizePathways(model, out);
				} else if(argv[i].equals("--hgnc-ids")) {
					summarizeHgncIds(model, out);
				} else if(argv[i].equals("--uniprot-ids")) {
					summarizeUniprotIds(model, out);
				} else if(argv[i].equals("--chebi-ids")) {
					summarizeChebiIds(model, out);
				} else if(argv[i].equals("--uri-ids")) {
					mapUriToIds(model, out);
				}
			}
		} else {
			summarize(model, out);
		}
		out.close();
	}


	/*
	 * For each physical entity participant in the BioPAX model,
	 * output uri, type, names, standard identifiers (in JSON format).
	 */
	private static void mapUriToIds(Model model, PrintStream out) {
		Set<String> elements = new TreeSet<String>();

		//write one by one to insert EOLs and make potentially a very large file human-readable -
		for(PhysicalEntity pe : model.getObjects(PhysicalEntity.class))
		{
			JSONObject jo = new JSONObject();
			jo.put("uri", pe.getUri());
			jo.put("type", pe.getModelInterface().getSimpleName());
			jo.put("generic", ModelUtils.isGeneric(pe));
			jo.put("label", pe.getDisplayName());

			JSONArray ja = new JSONArray();
			ja.addAll(pe.getName());
			jo.put("name", ja);

			if(!(pe instanceof SmallMolecule)) {
				ja = new JSONArray();
				ja.addAll(identifiers(pe, "hgnc symbol", false, false));
				jo.put("HGNC Symbol", ja);

				ja = new JSONArray();
				ja.addAll(identifiers(pe, "uniprot", true, false));
				jo.put("UniProt", ja);
			}

			if(pe instanceof SmallMolecule || PhysicalEntity.class.equals(pe.getModelInterface())) {
				ja = new JSONArray();
				ja.addAll(identifiers(pe, "chebi", false, false));
				jo.put("ChEBI", ja);
			}

			ja = new JSONArray();
			for(BioSource bs : ModelUtils.getOrganisms(pe)) ja.add(bs.getDisplayName());
			jo.put("organism", ja);

			ja = new JSONArray();
			for(Provenance ds : pe.getDataSource()) ja.add(ds.getDisplayName());
			jo.put("datasource", ja);

			elements.add(jo.toJSONString());
		}

		// Write as JSON array to the output
		out.print("[\n" + StringUtils.join(elements,"\n,") + "\n]");
	}


	/**
	 * Recursively collects bio identifiers of given type (xref.db name)
	 * associated with the physical entity or generic, complex entity.
	 *
	 * TODO (options): process Gene and Interaction; traverse into 'evidence' property.
	 * @param entity a process participant (simple or generic)
	 * @param xrefdb identifier type, such as 'HGNC Symbol' or 'ChEBI' (matches Xref.db values in the BioPAX model)
	 * @param isPrefix whether the xrefdb value is a prefix rather than complete name.
	 * @param includeEvidence whether to traverse into property:evidence to collect ids.
	 */
	private static Set<String> identifiers(final PhysicalEntity entity, final String xrefdb,
																				 boolean isPrefix, boolean includeEvidence)
	{
		final Set<String> ids = new HashSet<>();
		final Fetcher fetcher = (includeEvidence)
			? new Fetcher(SimpleEditorMap.L3, Fetcher.nextStepFilter)
			: new Fetcher(SimpleEditorMap.L3, Fetcher.nextStepFilter, Fetcher.evidenceFilter);
		fetcher.setSkipSubPathways(true); //makes no difference now  but good to have/know...
		Set<XReferrable> children = fetcher.fetch(entity, XReferrable.class);
		children.add(entity); //include itself
		for(XReferrable child : children) {//ignore some classes, such as controlled vocabularies, interactions, etc.
			if (child instanceof PhysicalEntity || child instanceof EntityReference || child instanceof Gene)
				for (Xref x : child.getXref())
					if ((x.getId()!=null && x.getDb()!=null) && (isPrefix)
						? x.getDb().toLowerCase().startsWith(xrefdb.toLowerCase())
						: xrefdb.equalsIgnoreCase(x.getDb()))
					{
						ids.add(x.getId());
					}
		}
		return ids;
	}


	private static void summarizePathways(Model model, PrintStream out) throws IOException {
		final PathAccessor directChildPathwaysAccessor = new PathAccessor("Pathway/pathwayComponent:Pathway");
		final PathAccessor pathwayComponentAccessor = new PathAccessor("Pathway/pathwayComponent");
		final PathAccessor pathwayOrderStepProcessAccessor = new PathAccessor("Pathway/pathwayOrder/stepProcess");

		Collection<Pathway> pathways = model.getObjects(Pathway.class);
		//print column titles
		out.println("PATHWAY_URI\tDISPLAY_NAME\tDIRECT_SUB_PATHWAY_URIS\tALL_SUB_PATHWAY_URIS");
		for(Pathway pathway : pathways) {
			StringBuilder sb = new StringBuilder();
			//write URI and name
			sb.append(pathway.getUri()).append('\t').append(pathway.getDisplayName()).append('\t');
			//add direct sub-pathways
			for(Object o : directChildPathwaysAccessor.getValueFromBean(pathway)) {
				Pathway p = (Pathway) o;
				sb.append(p.getUri()).append(";");
			}
			sb.append("\t");
			//add all sub-pathways
			Fetcher fetcher = new Fetcher(SimpleEditorMap.L3, Fetcher.nextStepFilter);
			for(Pathway p : fetcher.fetch(pathway, Pathway.class)) {
				sb.append(p.getUri()).append(";");
			}
			out.println(sb.toString());
		}
		// print pathway names, etc. after a blank line and title line
		out.println("\nPATHWAY_URI\tDATASOURCE\tDISPLAY_NAME\tALL_NAMES" +
			"\tNUM_DIRECT_COMPONENT_OR_STEP_PROCESSES");
		for(Pathway pathway : pathways) {
			final int size = pathwayComponentAccessor.getValueFromBean(pathway).size()
				+ pathwayOrderStepProcessAccessor.getValueFromBean(pathway).size();
			//pathways in PC2 normally has only one dataSource (Provenance)
			String datasource = pathway.getDataSource().iterator().next().getDisplayName();
			StringBuilder sb = new StringBuilder();
			sb.append(pathway.getUri()).append('\t')
				.append(datasource).append('\t')
				.append(pathway.getDisplayName()).append('\t');
			//all names
			for(String name : pathway.getName())
				sb.append('"').append(name).append('"').append(";");
			//"size"
			sb.append('\t').append(size);
			out.println(sb.toString());
		}
	}

	private static void summarizeHgncIds(Model model, PrintStream out) {
		boolean verbose = true; //TODO use another parameter here

		//Analyse SERs (Protein-, Dna* and Rna* references) - HGNC usage, coverage,..
		//Calc. the no. non-generic ERs having >1 different HGNC symbols and IDs, or none, etc.
		Set<SequenceEntityReference> haveMultipleHgnc = new HashSet<>();
		Map<Provenance,MutableInt> numErs = new HashMap<>();
		Map<Provenance,MutableInt> numProblematicErs = new HashMap<>();
		PathAccessor pa = new PathAccessor("EntityReference/entityReferenceOf/dataSource", model.getLevel());
		Set<String> problemErs = new TreeSet<String>();
		for(EntityReference ser : model.getObjects(EntityReference.class)) {
			//skip if it's a SMR or generic
			if(ser instanceof SmallMoleculeReference || !ser.getMemberEntityReference().isEmpty())
				continue;

			Set<String> hgncSymbols = new HashSet<>();
			Set<String> hgncIds = new HashSet<>();

			if(ser.getUri().startsWith("http://identifiers.org/hgnc")) {
				String s = ser.getUri().substring(ser.getUri().lastIndexOf("/")+1);
				if(s.startsWith("HGNC:"))
					hgncIds.add(s);
				else
					hgncSymbols.add(s);
			}

			for(Xref x : ser.getXref()) {
				if(x instanceof PublicationXref || x.getDb()==null || x.getId()==null)
					continue; //skip

				if(x.getDb().toLowerCase().startsWith("hgnc") && !x.getId().toLowerCase().startsWith("hgnc:")) {
					hgncSymbols.add(x.getId().toLowerCase());
				}
				else if(x.getDb().toLowerCase().startsWith("hgnc") && x.getId().toLowerCase().startsWith("hgnc:")) {
					hgncIds.add(x.getId().toLowerCase());
				}
			}

			if(hgncIds.size()>1 || hgncSymbols.size()>1)
				haveMultipleHgnc.add((SequenceEntityReference) ser);

			//increment "no hgnc" and "total" counts by data source
			for(Object provenance : pa.getValueFromBean(ser)) {
				if (hgncSymbols.isEmpty() && hgncIds.isEmpty()) {
					if (verbose) {
						problemErs.add(String.format("%s\t%s\t%s",
							((Provenance) provenance).getDisplayName(), ser.getDisplayName(), ser.getUri()));
					}

					MutableInt n = numProblematicErs.get(provenance);
					if (n == null)
						numProblematicErs.put((Provenance) provenance, new MutableInt(1));
					else
						n.increment();
				}

				MutableInt tot = numErs.get(provenance);
				if (tot == null)
					numErs.put((Provenance) provenance, new MutableInt(1));
				else
					tot.increment();
			}
		}
		//print results
		if(verbose) {
			out.println("SequenceEntityReferences (not generics) without any HGNC Symbol:");
			for(String line : problemErs) out.println(line);
		}
		out.println("The number of SERs (not generic) having more than one HGNC Symbols: " + haveMultipleHgnc.size());
		out.println("\nNumber of SequenceEntityReferences (not generics) without any HGNC ID, by data source:");
		int totalPrs = 0;
		int numPrsNoHgnc = 0;
		for(Provenance ds : numProblematicErs.keySet()) {
			int n = numProblematicErs.get(ds).intValue();
			numPrsNoHgnc += n;
			int t = numErs.get(ds).intValue();
			totalPrs += t;
			out.println(String.format("%s\t\t%d\t(%3.1f%%)", ds.getUri(), n, ((float)n)/t*100));
		}
		out.println(String.format("Total\t\t%d\t(%3.1f%%)", numPrsNoHgnc, ((float)numPrsNoHgnc)/totalPrs*100));
	}

	private static void summarizeUniprotIds(Model model, PrintStream out) {
		boolean verbose = true; //TODO use another parameter here

		//Analyse PRs - UniProt ID coverage,..
		Map<Provenance,MutableInt> numErs = new HashMap<>();
		Map<Provenance,MutableInt> numProblematicErs = new HashMap<>();
		PathAccessor pa = new PathAccessor("EntityReference/entityReferenceOf:Protein/dataSource", model.getLevel());
		Set<String> problemErs = new TreeSet<String>();
		for(ProteinReference pr : model.getObjects(ProteinReference.class)) {
			//skip a generic one
			if(!pr.getMemberEntityReference().isEmpty())
				continue;

			for(Object provenance : pa.getValueFromBean(pr)) {
				if(!pr.getUri().startsWith("http://identifiers.org/uniprot")
					&& !pr.getXref().toString().toLowerCase().contains("uniprot")) {

					if (verbose) {
						problemErs.add(String.format("%s\t%s\t%s",
							((Provenance) provenance).getDisplayName(), pr.getDisplayName(), pr.getUri()));
					}

					MutableInt n = numProblematicErs.get(provenance);
					if (n == null)
						numProblematicErs.put((Provenance) provenance, new MutableInt(1));
					else
						n.increment();
				}

				//increment total PRs per datasource
				MutableInt tot = numErs.get(provenance);
				if(tot == null)
					numErs.put((Provenance)provenance, new MutableInt(1));
				else
					tot.increment();
			}
		}

		//print results
		if(verbose) {
			out.println("\nProteinReferences (not generics) without any UniProt AC:");
			for(String line : problemErs) out.println(line);
		}
		out.println("\nNumber of ProteinReferences (not generics) without any UniProt AC, by data source:");
		int totalErs = 0;
		int problematicErs = 0;
		for(Provenance ds : numProblematicErs.keySet()) {
			int n = numProblematicErs.get(ds).intValue();
			problematicErs += n;
			int t = numErs.get(ds).intValue();
			totalErs += t;
			out.println(String.format("%s\t\t%d\t(%3.1f%%)", ds.getUri(), n, ((float)n)/t*100));
		}
		out.println(String.format("Total\t\t%d\t(%3.1f%%)", problematicErs, ((float)problematicErs)/totalErs*100));
	}

	private static void summarizeChebiIds(Model model, PrintStream out) {
		boolean verbose = true; //TODO use another parameter here
		//Analyse SMRs - ChEBI usage, coverage,..
		Map<Provenance,MutableInt> numErs = new HashMap<>();
		Map<Provenance,MutableInt> numProblematicErs = new HashMap<>();
		PathAccessor pa = new PathAccessor("EntityReference/entityReferenceOf:SmallMolecule/dataSource", model.getLevel());
		Set<String> problemErs = new TreeSet<String>();
		for(SmallMoleculeReference smr : model.getObjects(SmallMoleculeReference.class)) {
			//skip a generic SMR
			if(!smr.getMemberEntityReference().isEmpty())
				continue;

			for(Object provenance : pa.getValueFromBean(smr)) {
				if(!smr.getUri().startsWith("http://identifiers.org/chebi/CHEBI:")
					&& !smr.getXref().toString().contains("CHEBI:")) {

					if (verbose) {
						problemErs.add(String.format("%s\t%s\t%s",
							((Provenance) provenance).getDisplayName(), smr.getDisplayName(), smr.getUri()));
					}

					MutableInt n = numProblematicErs.get(provenance);
					if (n == null)
						numProblematicErs.put((Provenance) provenance, new MutableInt(1));
					else
						n.increment();
				}

				//increment total SMRs per datasource
				MutableInt tot = numErs.get(provenance);
				if(tot == null)
					numErs.put((Provenance)provenance, new MutableInt(1));
				else
					tot.increment();
			}
		}

		//print results
		if(verbose) {
			out.println("\nSmallMoleculeReferences (not generics) without any ChEBI ID:");
			for(String line : problemErs) out.println(line);
		}
		out.println("\nNumber of SmallMoleculeReferences (not generics) without any ChEBI ID, by data source:");
		int totalSmrs = 0;
		int numSmrsNoChebi = 0;
		for(Provenance ds : numProblematicErs.keySet()) {
			int n = numProblematicErs.get(ds).intValue();
			numSmrsNoChebi += n;
			int t = numErs.get(ds).intValue();
			totalSmrs += t;
			out.println(String.format("%s\t\t%d\t(%3.1f%%)", ds.getUri(), n, ((float)n)/t*100));
		}
		out.println(String.format("Total\t\t%d\t(%3.1f%%)", numSmrsNoChebi, ((float)numSmrsNoChebi)/totalSmrs*100));
	}

	static void summarize(Model model, PrintStream out) throws IOException {
		HashMap<String, Integer> hm = new HashMap<String, Integer>();

		final SimpleEditorMap em = SimpleEditorMap.get(model.getLevel());

		for (Class<? extends BioPAXElement> clazz : sortToName(em.getKnownSubClassesOf(BioPAXElement.class)))
		{
			Collection<? extends BioPAXElement> set = model.getObjects(clazz);
			int initialSize = set.size();
			set = filterToExactClass(set, clazz);
			String s = clazz.getSimpleName() + " = " + set.size();
			if (initialSize != set.size())
				s += " (and " + (initialSize - set.size()) + " children)";
			out.println(s);

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
						Set<?> values = editor.getValueFromBean(ele);
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
						Set<?> values = editor.getValueFromBean(ele);
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

					out.print("\t" + name + ":");
					for (Object key : getOrdering(cnt))
					{
						out.print("\t" + key + " = " + cnt.get(key));
					}
					out.println();
				}
			}
		}

		out.println("\nOther property counts\n");
		String[] props = (model.getLevel() == BioPAXLevel.L3)
			? new String[]{"UnificationXref/db","RelationshipXref/db"}
			: new String[]{"unificationXref/DB","relationshipXref/DB"};
		for (String prop : props)
		{
			Map<Object, Integer> cnt = new HashMap<Object, Integer>();
			List<String> valList = new ArrayList<>();
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

			out.println(prop + "\t(" + cnt.size() + " distinct values):");
			hm.put(prop, cnt.size());

			// If the object is String, then all counts are 1, no need to print counts.
			if (isString)
			{
				Collections.sort(valList);
				for (String s : valList)
				{
					out.print("\t" + s);
				}
			}
			else
			{
				for (Object key : getOrdering(cnt))
				{
					out.print("\t" + key + " = " + cnt.get(key));
				}
			}
			out.println();
		}

		//Count simple PEs that have null entityReference
		int speLackingEr = 0;
		int genericSpeLackingEr = 0;
		int speLackingErAndId = 0;
		int protLackingErAndId = 0;
		int molLackingErAndId = 0;
		int naLackingErAndId = 0; //rem: na - nucleic acid
		//a map that contains the no. SPEs that have null entityReference by data source
		Map<String,Integer> numSpeLackErByProvider = new TreeMap<String, Integer>();
		for(SimplePhysicalEntity spe : model.getObjects(SimplePhysicalEntity.class)) {
			if(spe.getEntityReference()==null) {
				speLackingEr++;
				if(!spe.getMemberPhysicalEntity().isEmpty())
					genericSpeLackingEr++;

				String providers = spe.getDataSource().toString();
				Integer n = numSpeLackErByProvider.get(providers);
				n = (n==null) ? 1 : n + 1;
				numSpeLackErByProvider.put(providers, n);

				if(spe.getXref().isEmpty() ||
					new ClassFilterSet<Xref,PublicationXref>(spe.getXref(), PublicationXref.class)
						.size() == spe.getXref().size())
				{
					speLackingErAndId++;
					if(spe instanceof Protein)
						protLackingErAndId++;
					else if(spe instanceof SmallMolecule)
						molLackingErAndId++;
					else if(spe instanceof NucleicAcid)
						naLackingErAndId++;
				}
			}
		}

		out.println("\n" + speLackingEr + " simple physical entities have NULL 'entityReference';\n");
		out.println("\n\t-" + genericSpeLackingEr + " of which have member physical entities (are generic).\n");
		out.println("\n\t- by data source:\n");
		for(String key : numSpeLackErByProvider.keySet()) {
			out.println(String.format("\n\t\t-- %s -> %d\n", key, numSpeLackErByProvider.get(key)));
		}
		out.println("\n\t- " + speLackingErAndId + " neither have 'entityReference' nor xref/id (except publications):\n");
		if(speLackingErAndId > 0) {
			out.println("\n\t\t-- proteins: " + protLackingErAndId + "\n");
			out.println("\n\t\t-- small molecules: " + molLackingErAndId + "\n");
			out.println("\n\t\t-- nucl. acids: " + naLackingErAndId + "\n");
		}

		int erLackingId = 0;
		for(EntityReference er : model.getObjects(EntityReference.class)) {
			if(er.getMemberEntityReference().isEmpty() &&
				(er.getXref().isEmpty() || new ClassFilterSet<Xref,PublicationXref>(er.getXref(), PublicationXref.class)
					.size() == er.getXref().size()))
			{
				erLackingId++;
			}
		}
		out.println("\n" + erLackingId + " non-generic entity references have no xref/id.\n");

		//The number of sequence ERs (not generic), Genes, Pathways, where 'organism' property is empty -
		int genesLackingOrganism = 0;
		int pwLackingOrganism = 0;
		int serLackingOrganism = 0;
		int narLackingOrganism = 0;
		for(BioPAXElement bpe : model.getObjects()) {
			if(bpe instanceof Gene && ((Gene) bpe).getOrganism()==null)
				++genesLackingOrganism;
			else if(bpe instanceof Pathway && ((Pathway) bpe).getOrganism()==null)
				++pwLackingOrganism;
			else if(bpe instanceof SequenceEntityReference && ((SequenceEntityReference)bpe).getOrganism() == null) {
				++serLackingOrganism;
				if(bpe instanceof NucleicAcidReference)
					++narLackingOrganism;
			}
		}
		out.println(
			String.format(
				"\n%d Genes, %d Pathways, %d SequenceEntityReferences " +
					"(%d in NucleicAcidRef. and %d in PRs) have NULL 'organism'.\n",
				genesLackingOrganism, pwLackingOrganism, serLackingOrganism,
				narLackingOrganism, serLackingOrganism-narLackingOrganism
			)
		);
	}

	static void toSer(String[] argv) throws IOException {
		String in = argv[1];
		String out = argv[2];
		log.info("Loading BioPAX data from " + in);
		Model model = io.convertFromOWL(getInputStream(in));
		log.info("Writing Paxtools Model to (binary file) " + out);
		ModelUtils.serialize(model, new FileOutputStream(out));
		log.info("Done.");
	}

	private static List<Class<? extends BioPAXElement>> sortToName(
		Set<? extends Class<? extends BioPAXElement>> classes)
	{
		List<Class<? extends BioPAXElement>> list = new ArrayList<Class<? extends BioPAXElement>>(classes);
		Collections.sort(list, new Comparator<Class<? extends  BioPAXElement>>() {
			public int compare(Class<? extends BioPAXElement> clazz1, Class<? extends BioPAXElement> clazz2)
			{
				return clazz1.getName().substring(clazz1.getName().lastIndexOf(".")+1).compareTo(
					clazz2.getName().substring(clazz2.getName().lastIndexOf(".")+1));
			}
		});

		return list;
	}

	private static List<Object> getOrdering(final Map<Object, Integer> map) {
		List<Object> list = new ArrayList<>(map.keySet());
		Collections.sort(list, (key1, key2) -> {
			int cnt1 = map.get(key1);
			int cnt2 = map.get(key2);
			if (cnt1 == cnt2)
				return key1.toString().compareTo(key2.toString());
			else
				return cnt2 - cnt1;
		});

		return list;
	}

	private static Collection<BioPAXElement> filterToExactClass(Collection<? extends BioPAXElement> classSet, Class<?> clazz)
	{
		Collection<BioPAXElement> exact = new HashSet<>();
		for (BioPAXElement ele : classSet) {
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

	private static boolean implementsInterface(Class clazz, Class inter) {
		for (Class anInter : clazz.getInterfaces()) {
			if (anInter.equals(inter))
				return true;
		}

		return false;
	}

	private static void increaseCnt(Map<Object, Integer> cnt, Object key) {
		if (!cnt.containsKey(key))
			cnt.put(key, 0);
		cnt.put(key, cnt.get(key) + 1);
	}

	// gets new IS - either FIS or GzipIS if .gz extension present
	private static InputStream getInputStream(String path) throws IOException {
		InputStream is = new FileInputStream(path);
		return (path.endsWith(".gz")) ? new GZIPInputStream(is) : is ;
	}

}
