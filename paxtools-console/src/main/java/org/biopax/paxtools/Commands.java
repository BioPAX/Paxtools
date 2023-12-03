package org.biopax.paxtools;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
				} else if("-subPathways".equalsIgnoreCase(argv[i])) {
					subPathways = true;
				} else if("-notPathway".equalsIgnoreCase(argv[i])) {
					notPathways = true;
				} else if(argv[i].startsWith("organisms=")) {
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

	static void getNeighbors(String[] argv) throws IOException {
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
		long start = System.currentTimeMillis();
		String in = argv[1];
		String out = argv[2];
		String[] uris = new String[]{}; //empty
		boolean absoluteUris = false;
		if(argv.length > 3) {
			for(int i=3; i<argv.length; i++) {
				String param = argv[i];
				if (param.startsWith("uris=")) {
					uris = param.substring(5).split(",");
				} else if(param.startsWith("-absolute")) {
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
		long stop = System.currentTimeMillis();
		log.info(String.format("Done (in %d sec)", TimeUnit.MILLISECONDS.toSeconds(stop-start)));
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
		} else if (buf.contains("<entrySet") && buf.contains("/psidev")) {
			return Type.PSIMI;
		} else
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
					//remove the 'seqDb=' and split comma-sep. values (a single val. no comma is fine too)
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
					if ((type != null && type != OutputColumn.Type.CUSTOM) || param.contains("/")) {
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
			if(line==null || line.isEmpty()) {
				break;
			}
			//keep only the first three columns (otherwise, it's not SIF format)
			writer.write(StringUtils.join(Arrays.copyOfRange(line.split("\t", 4),
					0, 3), '\t') + '\n');
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
		Set<String> elements = new TreeSet<>();

		//write one by one to insert EOLs and make potentially a very large file human-readable -
		for(PhysicalEntity pe : model.getObjects(PhysicalEntity.class)) {
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
	 * Recursively collects bio identifiers of given type (an xref.db value, bio id collection name)
	 * associated with the physical entity or generic, complex entity.
	 *
	 * @param entity a process participant (simple or generic)
	 * @param xrefdb identifier type, such as 'HGNC Symbol' or 'ChEBI' (match Xref.db values in the source BioPAX model)
	 * @param isPrefix whether the xrefdb value is a prefix rather than complete name.
	 * @param includeEvidence whether to traverse into property:evidence to collect ids.
	 */
	private static Set<String> identifiers(final PhysicalEntity entity, final String xrefdb,
																				 boolean isPrefix, boolean includeEvidence) {
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
							: xrefdb.equalsIgnoreCase(x.getDb())) {
						ids.add(x.getId());
					}
		}
		return ids;
	}


	private static void summarizePathways(Model model, PrintStream out) {
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
			out.println(sb);
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
			for(String name : pathway.getName()) {
				sb.append('"').append(name).append('"').append(";");
			}
			//"size"
			sb.append('\t').append(size);
			out.println(sb);
		}
	}

	private static void summarizeHgncIds(Model model, PrintStream out) {
		//Analyse each Protein, Dna*, Rna*, entity reference (except generic and SMR).
		//Get the number of non-generic ERs having >1 different HGNC symbols and IDs.
		//Note that the input biopax data/model may be not perfect, not normalized ("hgnc" can mean either symbol or id, etc.)
		PathAccessor pa = new PathAccessor("EntityReference/entityReferenceOf/dataSource", model.getLevel());
		Map<Provenance,MutableInt> numErs = new HashMap<>();
		Map<Provenance,MutableInt> numProblematicErs = new HashMap<>();
		Set<SequenceEntityReference> haveMultipleHgnc = new HashSet<>();
		Set<String> problemErs = new TreeSet<>();

		for(SequenceEntityReference ser : model.getObjects(SequenceEntityReference.class)) {
			//skip a generic
			if(!ser.getMemberEntityReference().isEmpty()) {
				continue;
			}

			//count hgnc ids, symbols if any
			Set<String> hgncSymbols = new HashSet<>();
			Set<String> hgncIds = new HashSet<>();
			//there are two kinds of HGNC id: hgnc (number, can be prefixed with 'HGNC:' banana) and hgnc.symbol (gene name)
			for(Xref x : ser.getXref()) {
				if(x instanceof PublicationXref || StringUtils.isBlank(x.getDb()) || StringUtils.isBlank(x.getId())) {
					continue; //skip PX, or when db or id is undefined/blank
				}
				if(StringUtils.startsWithIgnoreCase(x.getDb(),"hgnc")) {
					String id = x.getId().toLowerCase();
					if(StringUtils.startsWithIgnoreCase(id,"hgnc:") || StringUtils.isNumeric(id))
						hgncIds.add(id);
					else
						hgncSymbols.add(id);
				}
			}
			//save SER in the map if there are multiple ids or symbols (not unique)
			if(hgncIds.size()>1 || hgncSymbols.size()>1) {
				haveMultipleHgnc.add(ser);
			}

			//increment the counts by data source
			final String uri = ser.getUri();
			for(Object provenance : pa.getValueFromBean(ser)) {
				if (!StringUtils.startsWithIgnoreCase(uri, "identifiers.org/hgnc")
						&& !StringUtils.startsWithIgnoreCase(uri, "bioregistry.io/hgnc")
						&& !StringUtils.containsIgnoreCase(ser.getXref().toString(), "hgnc")
				) {
					problemErs.add(String.format("%s\t%s\t%s",
							((Provenance)provenance).getDisplayName(), ser.getDisplayName(), uri));
					MutableInt n = numProblematicErs.get(provenance);
					if (n == null) {
						numProblematicErs.put((Provenance) provenance, new MutableInt(1));
					} else {
						n.increment();
					}
				}
				MutableInt tot = numErs.get(provenance);
				if (tot == null) {
					numErs.put((Provenance) provenance, new MutableInt(1));
				} else {
					tot.increment();
				}
			}
		}
		//print the summary
		out.println("\nNumber of SequenceEntityReferences (non-generic) without any HGNC Symbol: " + problemErs.size());
		for(String line : problemErs) {
			out.println(line);
		}
		out.println("Number of SequenceEntityReferences (non-generic) having multiple HGNC Symbols: " + haveMultipleHgnc.size());
		for(SequenceEntityReference r : haveMultipleHgnc) {
			out.println(r.getUri());
		}
		out.println("Number of SequenceEntityReferences (non-generic) without any HGNC ID/Symbol, by data source:" );
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
		//Analyse PRs...
		Map<Provenance,MutableInt> numErs = new HashMap<>();
		Map<Provenance,MutableInt> numProblematicErs = new HashMap<>();
		PathAccessor pa = new PathAccessor("EntityReference/entityReferenceOf:Protein/dataSource", model.getLevel());
		Set<String> problemErs = new TreeSet<>();
		for(ProteinReference pr : model.getObjects(ProteinReference.class)) {
			//skip a generic one
			if(!pr.getMemberEntityReference().isEmpty()) {
				continue;
			}
			final String uri = pr.getUri();
			for(Object provenance : pa.getValueFromBean(pr)) {
				//when the protein reference does not have any uniprot AC/ID -
				if(!StringUtils.startsWithIgnoreCase(uri, "identifiers.org/uniprot")
						&& !StringUtils.startsWithIgnoreCase(uri, "bioregistry.io/uniprot")
						&& !StringUtils.containsIgnoreCase(pr.getXref().toString(), "uniprot")) {
					problemErs.add(String.format("%s\t%s\t%s",
							((Provenance) provenance).getDisplayName(), pr.getDisplayName(), uri));
					MutableInt n = numProblematicErs.get(provenance);
					if (n == null) {
						numProblematicErs.put((Provenance) provenance, new MutableInt(1));
					} else {
						n.increment();
					}
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
		out.println("\nNumber of ProteinReferences (non-generic) without any Uniprot AC:" + problemErs.size());
		for(String line : problemErs) {
			out.println(line);
		}
		out.println("Number of ProteinReferences (non-generic) without any Uniprot AC, by data source:");
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
		//Analyse SMRs...
		Map<Provenance,MutableInt> numErs = new HashMap<>();
		Map<Provenance,MutableInt> numProblematicErs = new HashMap<>();
		PathAccessor pa = new PathAccessor("EntityReference/entityReferenceOf:SmallMolecule/dataSource", model.getLevel());
		Set<String> problemErs = new TreeSet<>();
		for(SmallMoleculeReference smr : model.getObjects(SmallMoleculeReference.class)) {
			//skip a generic SMR
			if(!smr.getMemberEntityReference().isEmpty()) {
				continue;
			}
			final String uri = smr.getUri();
			for(Object provenance : pa.getValueFromBean(smr)) {
				if(!StringUtils.startsWithIgnoreCase(uri, "identifiers.org/chebi")
						&& !StringUtils.startsWithIgnoreCase(uri,"bioregistry.io/chebi")
						&& !StringUtils.containsIgnoreCase(smr.getXref().toString(),"chebi")) {
					problemErs.add(String.format("%s\t%s\t%s",
							((Provenance) provenance).getDisplayName(), smr.getDisplayName(), uri));
					MutableInt n = numProblematicErs.get(provenance);
					if (n == null) {
						numProblematicErs.put((Provenance) provenance, new MutableInt(1));
					} else {
						n.increment();
					}
				}
				//increment total SMRs per datasource
				MutableInt tot = numErs.get(provenance);
				if(tot == null) {
					numErs.put((Provenance) provenance, new MutableInt(1));
				} else {
					tot.increment();
				}
			}
		}
		//print results
		out.println("\nNumber of SmallMoleculeReferences (non-generic) without any ChEBI ID:" + problemErs.size());
		for(String line : problemErs) {
			out.println(line);
		}
		out.println("Number of SmallMoleculeReferences (non-generic) without any ChEBI ID, by data source:");
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

	/**
	 * Prints a summary of a BioPAX model.
	 * @param model
	 * @param out
	 */
	static void summarize(Model model, PrintStream out) throws IOException {
		BioPAXLevel level = model.getLevel();
		JSONObject summary = new JSONObject();
		summary.put("xml:base", model.getXmlBase());
		summary.put("level", level.name());
		JSONArray types = new JSONArray();
		summary.put("types", types);

		final SimpleEditorMap em = SimpleEditorMap.get(level);
		for (Class<? extends BioPAXElement> clazz : sortToName(em.getKnownSubClassesOf(BioPAXElement.class))) {
			if(!level.getDefaultFactory().canInstantiate(clazz)) {
				continue;
			}
			Collection<? extends BioPAXElement> allInstancesOfClass = model.getObjects(clazz);
			final int numInstances = allInstancesOfClass.size();
			if(numInstances > 0) {
				JSONObject type = new JSONObject();
				type.put("type", clazz.getSimpleName());
				types.add(type);
				Collection<? extends BioPAXElement> directInstances = filterToExactClass(allInstancesOfClass, clazz);
				int numDirectInstances = directInstances.size();
				type.put("instances", numInstances);
				type.put("direct_instances", numDirectInstances);
				JSONArray props = new JSONArray();
				//summarize some properties (enum, CVs)
				for (PropertyEditor editor : em.getEditorsOf(clazz)) {
					Method getMethod = editor.getGetMethod();
					Class<?> returnType = getMethod.getReturnType();
					Map<Object, Integer> cnt = new HashMap<>();
					if (returnType.isEnum() || implementsInterface(returnType, ControlledVocabulary.class)
							|| implementsInterface(editor.getRange(), ControlledVocabulary.class)) {
						for (BioPAXElement ele : directInstances) {
							Set<?> values = editor.getValueFromBean(ele);
							if (!values.isEmpty()) {
								for (Object val : values) {
									increaseCnt(cnt, val);
								}
							}
						}
					}
					if (!cnt.isEmpty()) {
						JSONObject p = new JSONObject();
						props.add(p);
						p.put("prop", editor.getProperty());
						JSONObject vals = new JSONObject();
						p.put("values_to_string", vals);
						String name = (returnType.equals(Set.class) ? editor.getRange().getSimpleName() : returnType.getSimpleName());
						p.put("range", name);
						for (Object key : cnt.keySet()) {
							vals.put(key.toString(), cnt.get(key));
						}
						type.put("properties", props);
					}
				}
			}
		}

		//Other property counts
		JSONArray properties = new JSONArray();
		summary.put("properties", properties);
		String[] propPaths = (model.getLevel() == BioPAXLevel.L3)
				? new String[]{"UnificationXref/db","RelationshipXref/db"}
				: new String[]{"unificationXref/DB","relationshipXref/DB"};
		for (String pPath : propPaths) {
			Set<String> cnt = new TreeSet<>();
			PathAccessor acc = new PathAccessor(pPath, model.getLevel());
			for (Object o : acc.getValueFromModel(model)) {
				cnt.add(o.toString());
			}
			//distinct values
			JSONObject p = new JSONObject();
			properties.add(p);
			p.put("path", pPath);
			p.put("unique_values", cnt.size());
			JSONArray v = new JSONArray();
			p.put("values", v);
			for (Object key : cnt) {
				v.add(key);
			}
		}

		//Count simple PEs that do not have any entityReference
		int speLackingEr = 0;
		int genericSpeLackingEr = 0;
		int speLackingErAndId = 0;
		int protLackingErAndId = 0;
		int molLackingErAndId = 0;
		int naLackingErAndId = 0; //rem: na - nucleic acid
		//a map that contains the no. SPEs that have null entityReference by data source
		Map<String,Integer> numSpeLackErByProvider = new TreeMap<>();
		for(SimplePhysicalEntity spe : model.getObjects(SimplePhysicalEntity.class)) {
			if(spe.getEntityReference()==null) {
				speLackingEr++;
				if(!spe.getMemberPhysicalEntity().isEmpty()) {
					genericSpeLackingEr++;
				}

				String providers = spe.getDataSource().toString();
				Integer n = numSpeLackErByProvider.get(providers);
				n = (n==null) ? 1 : n + 1;
				numSpeLackErByProvider.put(providers, n);

				if(spe.getXref().isEmpty() ||
						new ClassFilterSet<>(spe.getXref(), PublicationXref.class)
								.size() == spe.getXref().size()) {
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
		JSONObject speSummary = new JSONObject();
		summary.put("spe_without_er", speSummary);
		speSummary.put("description", "SimplePEs (not complexes) that do not have any entityReference");
		// speLackingEr simple physical entities have NULL 'entityReference'
		speSummary.put("total", speLackingEr);
		// genericSpeLackingEr of which have member physical entities (are generic)
		speSummary.put("generic", genericSpeLackingEr);
		//by data source
		JSONObject speByDs = new JSONObject();
		speSummary.put("by_source", speByDs);
		for(String key : numSpeLackErByProvider.keySet()) {
			speByDs.put(key, numSpeLackErByProvider.get(key));
		}
		//speLackingErAndId neither have 'entityReference' nor xref/id (except publications)
		if(speLackingErAndId > 0) {
			JSONObject speNoErNoId = new JSONObject();
			speSummary.put("also_without_id", speNoErNoId);
			speNoErNoId.put("total", speLackingErAndId);
			speNoErNoId.put("proteins", protLackingErAndId);
			speNoErNoId.put("small_molecules", molLackingErAndId);
			speNoErNoId.put("nucl_acids", naLackingErAndId);
		}

		int erLackingXref = 0;
		for(EntityReference er : model.getObjects(EntityReference.class)) {
			if(er.getMemberEntityReference().isEmpty() &&
					(er.getXref().isEmpty() || new ClassFilterSet<>(er.getXref(), PublicationXref.class)
							.size() == er.getXref().size())) {
				erLackingXref++;
			}
		}
		//erLackingId non-generic entity references have no xref/id
		summary.put("nongeneric_er_without_xref", erLackingXref);

		//The number of SequenceERs (non-generic), Genes, Pathways, where 'organism' property is empty
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
		JSONObject noOrg = new JSONObject();
		summary.put("without_organism", noOrg);
		noOrg.put("genes", genesLackingOrganism);
		noOrg.put("pathways", pwLackingOrganism);
		noOrg.put("seq_er", serLackingOrganism);
		noOrg.put("nucleic_acid_refs", narLackingOrganism);
		noOrg.put("protein_refs", serLackingOrganism-narLackingOrganism);

		int badUXrefs = 0;
		for(Xref x : model.getObjects(Xref.class)) {
			if(x instanceof PublicationXref)
				continue;
			if(StringUtils.isBlank(x.getId()) || StringUtils.isBlank(x.getDb())) {
				badUXrefs++;
			}
		}
		summary.put("uxrx_without_dbid", badUXrefs);

		out.println(summary.toJSONString());
	}

	private static List<Class<? extends BioPAXElement>> sortToName(
			Set<? extends Class<? extends BioPAXElement>> classes) {
		List<Class<? extends BioPAXElement>> list = new ArrayList<>(classes);
		Collections.sort(list, Comparator.comparing(
				clazz -> clazz.getName().substring(clazz.getName().lastIndexOf(".") + 1)));
		return list;
	}

	private static Collection<BioPAXElement> filterToExactClass(Collection<? extends BioPAXElement> classSet, Class<?> clazz) {
		Collection<BioPAXElement> exact = new HashSet<>();
		for (BioPAXElement ele : classSet) {
			if (ele.getModelInterface().equals(clazz)) exact.add(ele);
		}
		return exact;
	}

	private static boolean implementsInterface(Class clazz, Class inter) {
		for (Class anInter : clazz.getInterfaces()) {
			if (anInter.equals(inter)) {
				return true;
			}
		}
		return false;
	}

	private static void increaseCnt(Map<Object, Integer> cnt, Object key) {
		if (!cnt.containsKey(key)) {
			cnt.put(key, 0);
		}
		cnt.put(key, cnt.get(key) + 1);
	}

	// gets new IS - either FIS or GzipIS if .gz extension present
	private static InputStream getInputStream(String path) throws IOException {
		InputStream is = new FileInputStream(path);
		return (path.endsWith(".gz")) ? new GZIPInputStream(is) : is ;
	}

}
