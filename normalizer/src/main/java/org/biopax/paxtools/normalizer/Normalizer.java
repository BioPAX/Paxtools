package org.biopax.paxtools.normalizer;

import org.apache.commons.lang3.StringUtils;
import org.biopax.paxtools.controller.ModelUtils;
import org.biopax.paxtools.controller.ShallowCopy;
import org.biopax.paxtools.converter.LevelUpgrader;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.BPCollections;
import org.biopax.paxtools.util.ClassFilterSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * BioPAX (Level 3) Normalizer, an advanced BioPAX utility 
 * to help pathway data integrating and linking.
 * 
 * @author rodche
 */
public final class Normalizer {
	private static final Logger log = LoggerFactory.getLogger(Normalizer.class);
	
	private SimpleIOHandler biopaxReader;
	private String description = "";
	private boolean fixDisplayName;
	private String xmlBase;
	
	// Normalizer will generate URIs using a strategy specified by the system property
	// (the default is biopax.normalizer.uri.strategy=md5, to generate 32-byte digest hex string for xrefs's uris)
	public static final String PROPERTY_NORMALIZER_URI_STRATEGY = "biopax.normalizer.uri.strategy";
	public static final String VALUE_NORMALIZER_URI_STRATEGY_SIMPLE = "simple";
	public static final String VALUE_NORMALIZER_URI_STRATEGY_MD5 = "md5"; //default strategy
	
	/**
	 * Constructor
	 */
	public Normalizer() {
		biopaxReader = new SimpleIOHandler(BioPAXLevel.L3);
		biopaxReader.mergeDuplicates(true);
		fixDisplayName = true;
		xmlBase = "";
	}
	
	
	/**
	 * Normalizes BioPAX OWL data and returns
	 * the result as BioPAX OWL (string).
	 * 
	 * This public method is actually intended to use 
	 * outside the BioPAX Validator framework, with not too large models.
	 * 
	 * @param biopaxOwlData RDF/XML BioPAX content string
	 * @return normalized BioPAX RDF/XML
	 * @deprecated this method will fail if the data exceeds ~1Gb (max UTF8 java String length)
	 */
	public String normalize(String biopaxOwlData) {
		
		if(biopaxOwlData == null || biopaxOwlData.length() == 0) 
			throw new IllegalArgumentException("no data. " + description);
		
		// quick-fix for older BioPAX L3 version (v0.9x) property 'taxonXref' (range: BioSource)
		biopaxOwlData = biopaxOwlData.replaceAll("taxonXref","xref");
		
		// build the model
		Model model = null;
		try {
			model = biopaxReader.convertFromOWL(
				new ByteArrayInputStream(biopaxOwlData.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Failed! " + description, e);
		}
		
		if(model == null) {
			throw new IllegalArgumentException("Failed to create Model! " 
					+ description);
		}
		
		// auto-convert to Level3 model
		if (model.getLevel() != BioPAXLevel.L3) {
			log.info("Converting model to BioPAX Level3...");
			model = (new LevelUpgrader()).filter(model);
		}
		
		normalize(model); // L3 only!
		
		// return as BioPAX OWL
		return convertToOWL(model);
	}
	

	/**
	 * Normalizes all xrefs (not unification xrefs only) to help normalizing/merging other objects, 
	 * and also because some of the original xref URIs ("normalized")
	 * are in fact to be used for other biopax types (e.g., CV or ProteinReference); therefore
	 * this method will replace URI also for "bad" xrefs, i.e., those with empty/illegal 'db' or 'id' values.
	 * 
	 * @param model biopax model to update
	 */
	private void normalizeXrefs(Model model) {
		
		final NormalizerMap map = new NormalizerMap(model);
		final String xmlBase = getXmlBase(model); //current base, the default or model's one, if set.
		
		// use a copy of the xrefs set (to avoid concurrent modif. exception)
		Set<? extends Xref> xrefs = new HashSet<Xref>(model.getObjects(Xref.class));
		for(Xref ref : xrefs)
		{
			//skip not well-defined ones (incl. PublicationXrefs w/o db/id - won't normalize)
			if(ref.getDb() == null || ref.getId() == null)
				continue;

			ref.setDb(ref.getDb().toLowerCase()); //set lowercase
			String idPart = ref.getId();

			if(ref instanceof RelationshipXref) {
				// only normalize (replace URI of) RXs that could potentially clash with other RX, CVs, ERs;
				// skip, won't bother, for all other RXs...
				if(!ref.getUri().startsWith("http://identifiers.org/"))
					continue;

				//RXs might have the same db, id but different rel. type.
				RelationshipTypeVocabulary cv = ((RelationshipXref) ref).getRelationshipType();
				if(ref.getIdVersion()!=null) {
					idPart += "_" + ref.getIdVersion();
				}
				if(cv != null && !cv.getTerm().isEmpty()) {
					idPart += "_" + StringUtils.join(cv.getTerm(), '_').toLowerCase();
				}
			}
			else if(ref instanceof UnificationXref) {
				// first, try to normalize the db name (using MIRIAM EBI registry)
				try {
					ref.setDb(MiriamLink.getName(ref.getDb()).toLowerCase());
				} catch (IllegalArgumentException e) {
					// - unknown/unmatched db name (normalize using defaults)
					if(ref.getIdVersion()!=null) {
						idPart += "_" + ref.getIdVersion();
					}
					map.put(ref, Normalizer.uri(xmlBase, ref.getDb(), idPart, ref.getModelInterface()));
					continue; //shortcut (non-standard db name)
				}
			
				// a hack for uniprot/isoform xrefs
				if (ref.getDb().startsWith("uniprot")) {
					//auto-fix (guess) for possibly incorrect db/id (can be 'uniprot isoform' with/no idVersion, etc..)
					if (isValidDbId("uniprot isoform", ref.getId())
							&& ref.getId().contains("-")) //the second condition is important
					{	//then it's certainly an isoform id; so - fix the db name
						ref.setDb("uniprot isoform"); //fix the db
					}
					else {
						//id does not end with "-\\d+", i.e., not a isoform id
						//(idVersion is a different thing, but id db was "uniprot isoform" they probably misused idVersion)
						if(ref.getDb().equals("uniprot isoform"))
						{
							if(ref.getIdVersion() != null && ref.getIdVersion().matches("^\\d+$"))
								idPart = ref.getId()+"-"+ref.getIdVersion(); //guess, by idVersion, they actually meant isoform
							if(isValidDbId(ref.getDb(), idPart)) {
								ref.setId(idPart); //moved the isoform # to the ID
								ref.setIdVersion(null);
							}
							else if(!isValidDbId(ref.getDb(), ref.getId())) {
								//certainly not isoform (might not even uniprot, but try...)
								ref.setDb("uniprot knowledgebase"); //guess, fix
							}
							idPart = ref.getId();
						}
					}
				}
			}

			// shelve it for URI replace
			map.put(ref, Normalizer.uri(xmlBase, ref.getDb(), idPart, ref.getModelInterface()));
		}
		
		// execute replace xrefs
		map.doSubs();
	}

	/*
	 * @param db must be valid MIRIAM db name or sinonym
	 * @trows IllegalArgumentException when db is unknown name.
	 */
	private boolean isValidDbId(String db, String id) {
		return MiriamLink.checkRegExp(id, db);
	}

	/**
	 * Consistently generates a new BioPAX element URI 
	 * using given URI namespace (xml:base), BioPAX class, 
	 * and two different identifiers (at least one is required).
	 * Miriam registry is used to get the standard db name and 
	 * identifiers.org URI, if possible, only for relationship type vocabulary, 
	 * publication xref, and entity reference types.
	 * 
	 * @param xmlBase xml:base (common URI prefix for a BioPAX model), case-sensitive
	 * @param dbName a bio data collection name or synonym, case-insensitive
	 * @param idPart optional (can be null), e.g., xref.id, case-sensitive
	 * @param type BioPAX class
	 * @return URI
	 * @throws IllegalArgumentException if either type is null or both 'dbName' and 'idPart' are all nulls.
	 */
	public static String uri(final String xmlBase, 
		String dbName, String idPart, Class<? extends BioPAXElement> type)
	{
		if(type == null || (dbName == null && idPart == null))
			throw new IllegalArgumentException("'Either type' is null, or both dbName and idPart are nulls.");

		if (idPart != null) idPart = idPart.trim();
		if (dbName != null) dbName = dbName.trim();

		// try to find a standard URI, if exists, for a publication xref, or at least a standard name:
		if (dbName != null)
		{
			try {
				// try to get the preferred/standard name
				// for any type, for consistency
				dbName = MiriamLink.getName(dbName);
				// a shortcut: a standard and resolvable URI exists for some BioPAX types
				if ((type.equals(PublicationXref.class) && "pubmed".equalsIgnoreCase(dbName))
					|| type.equals(RelationshipTypeVocabulary.class)
					|| ProteinReference.class.isAssignableFrom(type)
					|| SmallMoleculeReference.class.isAssignableFrom(type)
					|| (type.equals(BioSource.class) && "taxonomy".equalsIgnoreCase(dbName)
						&& idPart!=null && idPart.matches("^\\d+$"))
				)
				{	//get the standard URI and quit (success), or fail and continue making a new URI below...
					return MiriamLink.getIdentifiersOrgURI(dbName, idPart);
				}
			} catch (IllegalArgumentException e) {
				log.info(String.format("uri(for a %s): db:%s, id:%s are not standard; %s)",
						type.getSimpleName(), dbName, idPart, e.getMessage()));
			}
		}

		// If not returned above this point - no standard URI (Identifiers.org) was found -
		// then let's consistently build a new URI from args, anyway, the other way around:
		
		StringBuilder sb = new StringBuilder();		
		if (dbName != null) //lowercase for consistency
			sb.append(dbName.toLowerCase()); 	
		
		if (idPart != null) {
			if (dbName != null) sb.append("_");
			sb.append(idPart);
		}
		
		String localPart = sb.toString();
		String strategy = System.getProperty(PROPERTY_NORMALIZER_URI_STRATEGY, VALUE_NORMALIZER_URI_STRATEGY_MD5);
		if(VALUE_NORMALIZER_URI_STRATEGY_SIMPLE.equals(strategy) || Xref.class.isAssignableFrom(type))
		//i.e., for xrefs, always use the simple URI strategy (makes them human-readable)
		{
			//simply replace "unsafe" symbols with underscore (some uri clashes might be possible but rare...)
			localPart = localPart.replaceAll("[^-\\w]", "_");
		}
		else
		{
			//replace the local part with its md5 sum string (32-byte)
			localPart = ModelUtils.md5hex(localPart);
		}
		
		// create URI using the xml:base and digest of other values:
		return ((xmlBase != null) ? xmlBase : "") + type.getSimpleName() + "_" + localPart;
	}

	
	/**
	 * Description of the model to normalize.
	 * 
	 * @return info about the BioPAX model
	 */
	public String getDescription() {
		return description;
	}
	
	
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	
	private void fixDisplayName(Model model) {
		log.info("Trying to auto-fix 'null' displayName...");
		// where it's null, set to the shortest name if possible
		for (Named e : model.getObjects(Named.class)) {
			if (e.getDisplayName() == null) {
				if (e.getStandardName() != null) {
					e.setDisplayName(e.getStandardName());
					log.info(e + " displayName auto-fix: "
							+ e.getDisplayName() + ". " + description);
				} else if (!e.getName().isEmpty()) {
					String dsp = e.getName().iterator().next();
					for (String name : e.getName()) {
						if (name.length() < dsp.length())
							dsp = name;
					}
					e.setDisplayName(dsp);
					log.info(e + " displayName auto-fix: " + dsp
						+ ". " + description);
				}
			}
		}
		// if required, set PE name to (already fixed) ER's name...
		for(EntityReference er : model.getObjects(EntityReference.class)) {
			for(SimplePhysicalEntity spe : er.getEntityReferenceOf()) {
				if(spe.getDisplayName() == null || spe.getDisplayName().trim().length() == 0) {
					if(er.getDisplayName() != null && er.getDisplayName().trim().length() > 0) {
						spe.setDisplayName(er.getDisplayName());
					}
				}
			}
		}
	}


	private String convertToOWL(Model model) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		(new SimpleIOHandler(model.getLevel())).convertToOWL(model, out);
		return out.toString();
	}


	private Collection<UnificationXref> getUnificationXrefsSorted(XReferrable r) {

		List<UnificationXref> urefs = new ArrayList<UnificationXref>();
		for(UnificationXref ux : new ClassFilterSet<Xref,UnificationXref>(r.getXref(), UnificationXref.class))
		{
			if(ux.getDb() != null && ux.getId() != null) {
				urefs.add(ux);
			} 
		}

		Collections.sort(urefs, new Comparator<UnificationXref>() {
			public int compare(UnificationXref o1, UnificationXref o2) {
				String s1 = o1.getDb() + o1.getId();
				String s2 = o2.getDb() + o2.getId();
				return s1.compareTo(s2);
			}
		});

		return urefs;
	}

	
	/**
	 * Finds one preferred unification xref, if possible.
	 * Preferred db values are:
	 * "ncbi gene" - for NucleicAcidReference and the sub-classes;
	 * "uniprot" or "refseq" for ProteinReference;
	 * "chebi" - for SmallMoleculeReference;
	 * 
	 * @param bpe BioPAX object that can have xrefs
	 * @return the "best" first unification xref 
	 */
	private UnificationXref findPreferredUnificationXref(XReferrable bpe)
	{
		UnificationXref toReturn = null;

		Collection<UnificationXref> orderedUrefs = getUnificationXrefsSorted(bpe);

		//use preferred db prefix for different type of ER
		if(bpe instanceof ProteinReference) {
			// prefer xref having db starts with "uniprot" (preferred) or "refseq";
			toReturn = findSingleUnificationXref(orderedUrefs, "uniprot");
			// if null, next try refseq.
			if(toReturn==null)
				toReturn = findSingleUnificationXref(orderedUrefs, "refseq");
		} else if(bpe instanceof SmallMoleculeReference) {
			// - "chebi", then  "pubchem";
			toReturn = findSingleUnificationXref(orderedUrefs, "chebi");
			if(toReturn==null)
				toReturn = findSingleUnificationXref(orderedUrefs, "pubchem");
		} else if(bpe instanceof NucleicAcidReference) {
			//that includes NucleicAcidRegionReference, etc. sub-classes;
			toReturn = findSingleUnificationXref(orderedUrefs, "ncbi gene");
			if(toReturn==null)
				toReturn = findSingleUnificationXref(orderedUrefs, "entrez");
		} else {
			//for other XReferrable types (BioSource or ControlledVocabulary)
			//use if there's only one xref (return null if many)
			if(orderedUrefs.size()==1)
				toReturn = orderedUrefs.iterator().next();
		}
		
		return toReturn;
	}

	private UnificationXref findSingleUnificationXref(Collection<UnificationXref> uXrefs, String dbStartsWith) {
		UnificationXref ret = null;

		for(Xref x : uXrefs) {
			if(x instanceof UnificationXref && x.getId() != null && x.getDb() != null
					&& x.getDb().toLowerCase().startsWith(dbStartsWith))
			{
				if(ret == null)
				{
					ret = (UnificationXref)x;
				}
				else if (ret.getDb().equalsIgnoreCase(x.getDb()) && !ret.getId().equals(x.getId())) {
					//exactly the same kind (same 'db') xref is found;
					//we can't test here if several IDs map to the same thing;
					//so, give up - for safety - and return null.
					ret = null;
					break;
				}
			}
		}

		return ret;
	}

	
	/**
	 * BioPAX normalization 
	 * (modifies the original Model)
	 * 
	 * @param model BioPAX model to normalize
	 * @throws NullPointerException if model is null
	 * @throws IllegalArgumentException if model is not Level3 BioPAX
	 */
	public void normalize(Model model) {
		
		if(model.getLevel() != BioPAXLevel.L3)
			throw new IllegalArgumentException("Not Level3 model. " +
				"Consider converting it first (e.g., with the PaxTools).");
		
		//if set, update the xml:base
		if(xmlBase != null && !xmlBase.isEmpty())
			model.setXmlBase(xmlBase);

		// Normalize/merge xrefs, first, and then CVs
		// (also because some of original xrefs might have "normalized" URIs 
		// that, in fact, must be used for other biopax types, such as CV or ProteinReference)
		log.info("Normalizing xrefs..." + description);
		normalizeXrefs(model);
		
		// fix displayName where possible
		if(fixDisplayName) {
			log.info("Normalizing display names..." + description);
			fixDisplayName(model);
		}
			
		log.info("Normalizing CVs..." + description);
		normalizeCVs(model);
		
		//normalize BioSource objects (better, as it is here, go after Xrefs and CVs)
		log.info("Normalizing organisms..." + description);
		normalizeBioSources(model);

		// auto-generate missing entity references:
		for(SimplePhysicalEntity spe : new HashSet<SimplePhysicalEntity>(model.getObjects(SimplePhysicalEntity.class))) {
			//it skips if spe has entityReference or memberPE already
			ModelUtils.addMissingEntityReference(model, spe);
		}

		log.info("Normalizing entity references..." + description);
		normalizeERs(model);
		
		// find/add lost (in replace) children
		log.info("Repairing..." + description);
		model.repair(); // it does not remove dangling utility class objects (can be done separately, later, if needed)
		
		log.info("Optional tasks (reasoning)..." + description);
	}

	
	private void normalizeCVs(Model model) {
		
		NormalizerMap map = new NormalizerMap(model);
		
		// process ControlledVocabulary objects (all sub-classes)
		for(ControlledVocabulary cv : model.getObjects(ControlledVocabulary.class))
		{
			//it does not check/fix the CV terms though (but a validation rule can do if run before the normalizer)...
			UnificationXref uref = findPreferredUnificationXref(cv); //usually, there's only one such xref
			if (uref != null) {
				// so let's generate a consistent URI
				map.put(cv, uri(xmlBase, uref.getDb(), uref.getId(), cv.getModelInterface()));
			} else if(!cv.getTerm().isEmpty()) {
				map.put(cv, uri(xmlBase, null, cv.getTerm().iterator().next(), cv.getModelInterface()));
			} else log.info("Cannot normalize " + cv.getModelInterface().getSimpleName() 
				+ " : no unification xrefs nor terms found in " + cv.getUri()
				+ ". " + description);
		} 
		
		// replace/update elements in the model
		map.doSubs();
	}
	
	
	private void normalizeBioSources(Model model) {
		
		NormalizerMap map = new NormalizerMap(model);
		
		for(BioSource bs : model.getObjects(BioSource.class))
		{
			UnificationXref uref = findPreferredUnificationXref(bs);
			//normally, the xref db is 'Taxonomy' (or a valid synonym)
			if (uref != null
				&& (uref.getDb().toLowerCase().contains("taxonomy") || uref.getDb().equalsIgnoreCase("newt")))
			{	
				String 	idPart = uref.getId();

				//tissue/cellType terms can be added below:
				if(bs.getTissue()!=null && !bs.getTissue().getTerm().isEmpty()) 
					idPart += "_" + bs.getTissue().getTerm().iterator().next();
				if(bs.getCellType()!=null && !bs.getCellType().getTerm().isEmpty()) 
					idPart += "_" + bs.getCellType().getTerm().iterator().next();
				
				String uri = (idPart.equals(uref.getId()) //- no tissue or celltype were attached
						&& idPart.matches("^\\d+$")) //- is positive integer id
					? uri(xmlBase, uref.getDb(), idPart, BioSource.class)
						: "http://identifiers.org/taxonomy/" + idPart;
					// the latter is intentionally invalid identifiers.org/taxonomy URI - good (and important) for merging
				map.put(bs, uri);

			} else 
				log.debug("Won't normalize BioSource" 
					+ " : no taxonomy unification xref found in " + bs.getUri()
					+ ". " + description);
		} 
		
		map.doSubs();
	}

	private void normalizeERs(Model model) {
		
		NormalizerMap map = new NormalizerMap(model);
		
		// process the rest of utility classes (selectively though)
		for (EntityReference bpe : model.getObjects(EntityReference.class)) {
			
			//skip those with already normalized URIs
			if(bpe.getUri().startsWith("http://identifiers.org/")) {
				log.info("Skip already normalized: " + bpe.getUri());
				continue;
			}			
			
			UnificationXref uref = findPreferredUnificationXref(bpe);
			if (uref != null) {
				// Create (with a new URI made from a unif. xref) 
				// and save the replacement object, if possible, 
				// but do not replace yet (will call doSubs later, for all).
				final String db = uref.getDb();
				final String id = uref.getId();
				// get the standard ID
				String uri = null;
				try { // make a new ID for the element
					uri = MiriamLink.getIdentifiersOrgURI(db, id);
				} catch (Exception e) {
					log.error("Cannot get a Miriam standard ID for " + bpe 
							+ " (" + bpe.getModelInterface().getSimpleName()
							+ ") " + ", using " + db + ":" + id 
							+ ". " + e.getMessage());
					return;
				}

				if(uri != null) {
					map.put(bpe, uri);
				}	
			} else
				log.info("Cannot normalize EntityReference: "
					+ "no unification xrefs found in " + bpe.getUri()
					+ ". " + description);
		}
		
		// replace/update elements in the model
		map.doSubs();
	}
	
	
	/**
	 * Auto-generates standard and other names for the datasource
	 * from either its ID (if URN) or one of its existing names (preferably - standard name)
	 * 
	 * @param pro data source (BioPAX Provenance)
	 */
	public static void autoName(Provenance pro) {
		if(!(pro.getUri().startsWith("urn:miriam:") || pro.getUri().startsWith("http://identifiers.org/"))
				&& pro.getName().isEmpty()) {
			log.info("Skipping: cannot normalize Provenance: " + pro.getUri());
		}
		else { // i.e., 'name' is not empty or ID is the URN
			final SortedSet<String> names = new TreeSet<String>();
			
			String key = null;
			if(pro.getUri().startsWith("urn:miriam:") || pro.getUri().startsWith("http://identifiers.org/")) {
				key = pro.getUri();
			} else if (pro.getStandardName() != null) {
				key = pro.getStandardName();
			} else {
				key = pro.getDisplayName(); // can be null
			}
			
			if (key != null) {
				try {
					names.addAll(Arrays.asList(MiriamLink.getNames(key)));
					pro.setStandardName(MiriamLink.getName(key));
					// get the datasource description
					String description = MiriamLink.getDataTypeDef(pro.getStandardName());
					pro.addComment(description);
				} catch (IllegalArgumentException e) {
					// ignore (then, names is still empty...)
				}
			} 
			
			// when the above failed (no match in Miriam), or key was null -
			if(names.isEmpty()) {
				// finally, trying to find all valid names for each existing one
					for (String name : pro.getName()) {
						try {
							names.addAll(Arrays.asList(MiriamLink.getNames(name)));
						} catch (IllegalArgumentException e) {
							// ignore
						}
					}
					// pick up the first name, get the standard name
					if(!names.isEmpty())
						pro.setStandardName(MiriamLink
							.getName(names.iterator().next()));
			}
			
			// and add all the synonyms if any
			for(String name : names)
				pro.addName(name);
			
			//set display name if not set (standard name is set already)
			if(pro.getDisplayName() == null)
				pro.setDisplayName(pro.getStandardName());			
		}
	}
	
	/**
	 * Converts BioPAX L1 or L2 RDF/XML string data to BioPAX L3 string.
	 *
	 * WARN: this is not for huge (larger than 1GB) BioPAX RDF/XML data
	 * due to use of (UTF-8) String and Byte Array internally.
	 * This can be and is used by online web apps, such as the
	 * BioPAX Validator.
	 *
	 * @param biopaxData String
	 * @return BioPAX Level3 RDF/XML string
	 */
	public static String convertToLevel3(final String biopaxData) {
		String toReturn = "";
		
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			InputStream is = new ByteArrayInputStream(biopaxData.getBytes());
			SimpleIOHandler io = new SimpleIOHandler();
			io.mergeDuplicates(true);
			Model model = io.convertFromOWL(is);
			if (model.getLevel() != BioPAXLevel.L3) {
				log.info("Converting to BioPAX Level3... " + model.getXmlBase());
				model = (new LevelUpgrader()).filter(model);
				if (model != null) {
					io.setFactory(model.getLevel().getDefaultFactory());
					io.convertToOWL(model, os);
					toReturn = os.toString();
				}
			} else {
				toReturn = biopaxData;
			}
		} catch(Exception e) {
			throw new RuntimeException(
				"Cannot convert to BioPAX Level3", e);
		}

		return toReturn;
	}
	
	
	/**
	 * Gets the xml:base to use with newly created BioPAX elements.
	 * 
	 * @param modelToNormalize
	 * @return BioPAX model's xml:base or empty string if it's null
	 */
	private String getXmlBase(Model modelToNormalize) {
		if(xmlBase != null)
			return xmlBase;
		else
			return "";
	}
	
	
	public boolean isFixDisplayName() {
		return fixDisplayName;
	}
	public void setFixDisplayName(boolean fixDisplayName) {
		this.fixDisplayName = fixDisplayName;
	}

	public String getXmlBase() {
		return xmlBase;
	}
	public void setXmlBase(String xmlBase) {
		this.xmlBase = xmlBase;
	}

	
	/**
	 * Helper class, to associate original 
	 * and new (either copy or existing) objects 
	 * within some biopax model and then execute 
	 * batch replace.
	 * 
	 * @author rodche
	 */
	private static class NormalizerMap {

		//a model to modify by replacing biopax objects
		final Model model;
		
		// this is the biopax elements substitution map (old->new)
		final Map<BioPAXElement,BioPAXElement> subs;
		
		//the next map is to make sure the subs.values()  all have different URIs
		final Map<String,BioPAXElement> uriToSub;
		
		final ShallowCopy copier;
				
		NormalizerMap(Model model) {
			subs = BPCollections.I.createMap();
			uriToSub = BPCollections.I.createMap();
			this.model = model;
			copier = new ShallowCopy();
		}


		/**
		 * Creates (by URI) and saves the replacement object,
		 * but does not replace yet (call doSubs to replace).
		 * 
		 * @param bpe
		 * @param newUri
		 */
		void put(BioPAXElement bpe, String newUri)
		{
			if(model.containsID(newUri)) {
				// will use existing original (model) object that has the new Uri
				map(bpe, model.getByID(newUri));
			} else if(uriToSub.containsKey(newUri)) {
				// re-use the new object that's already added to replace another original
				map(bpe, uriToSub.get(newUri));
			} else {
				// will use the object's shallow copy that gets new Uri
				BioPAXElement copy = copier.copy(bpe, newUri);
				map(bpe, copy);
			}
		}		
		
		/**
		 * Executes the batch replace - migrating  
		 * to the normalized equivalent objects.
		 */
		void doSubs() {
			for(BioPAXElement e : subs.keySet()) {
				model.remove(e);
			}
			
			try {
				ModelUtils.replace(model, subs);
			} catch (Exception e) {
				log.error("Failed to replace BioPAX elements.", e);
				return;
			}
			
			for(BioPAXElement e : subs.values()) {
				if(!model.contains(e))
					model.add(e);
			}
		
			for(BioPAXElement e : model.getObjects()) {
				ModelUtils.fixDanglingInverseProperties(e, model);
			}
		}

		private void map(BioPAXElement bpe, BioPAXElement newBpe) {
			subs.put(bpe, newBpe);
			uriToSub.put(newBpe.getUri(), newBpe);
		}
	}
	
}
