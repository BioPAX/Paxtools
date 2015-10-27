package org.biopax.paxtools.normalizer;


import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
	private static final Log log = LogFactory.getLog(Normalizer.class);
	
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
	 * outside the BioPAX Validator framework.
	 * 
	 * @param biopaxOwlData
	 * @return
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
	 * @param model
	 */
	private void normalizeXrefs(Model model) {
		
		NormalizerMap map = new NormalizerMap(model);
		
		final String xmlBase = getXmlBase(model); //current base, the default or model's one, if set.
		
		// use a copy of the xrefs set (to avoid concurrent modif. exception)
		Set<? extends Xref> xrefs = new HashSet<Xref>(model.getObjects(Xref.class));
		for(Xref ref : xrefs) {
			String idPart = null;
			if(ref.getId() != null) { 
				idPart = ref.getId();
				if(ref.getIdVersion()!=null)
					idPart += "_" + ref.getIdVersion();
			}
			
			if(ref instanceof PublicationXref)
			{
				//skip if db or id is null
				if(ref.getDb() == null || ref.getId() == null)
					continue;	
			}		
			else if(ref instanceof RelationshipXref)
			{
				//only do RXs that potentially clash with normalized CVs or ERs
				if(ref.getUri().startsWith("http://identifiers.org/")) {
					//RXs might have the same db,id but different type.	
					RelationshipTypeVocabulary cv = ((RelationshipXref) ref).getRelationshipType();
					if(cv != null && !cv.getTerm().isEmpty()) {
						if(idPart!=null)
							idPart += "_" + StringUtils.join(cv.getTerm(), '_').toLowerCase();
						else 
							idPart = StringUtils.join(cv.getTerm(), '_').toLowerCase();
					}
				}
				else continue;
			}
			else if(ref instanceof UnificationXref)
			{
				if(ref.getDb() == null || ref.getId() == null)
					continue; //skip not well-defined ones			
				
				// try to normalize db name (if known to MIRIAM EBI registry)
				String db = ref.getDb();
				try {
					db = MiriamLink.getName(ref.getDb());
					if(db != null) 
						ref.setDb(db);
				} catch (IllegalArgumentException e) {}
			
				// a hack for uniprot isoform xrefs that were previously cut off the isoform number part...
				if (db.toUpperCase().startsWith("UNIPROT")) {
					//fix for possibly incorrect db name
					if (Normalizer.uri(xmlBase, "UniProt Isoform", ref.getId(), ProteinReference.class)
							.startsWith("http://identifiers.org/uniprot.isoform/")) 
					{
						ref.setDb("UniProt Isoform");
						idPart = ref.getId();
					} 
					else if (ref.getIdVersion() != null) {
						final String isoformId = ref.getId() + "-" + ref.getIdVersion();
						if(Normalizer.uri(xmlBase, "UniProt Isoform", isoformId, ProteinReference.class)
								.startsWith("http://identifiers.org/uniprot.isoform/")) 
						{
							ref.setDb("UniProt Isoform");
							ref.setId(isoformId);
							ref.setIdVersion(null);
							idPart = isoformId;
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
		String dbName, final String idPart, Class<? extends BioPAXElement> type)
	{
		if(type == null || (dbName == null && idPart == null))
			throw new IllegalArgumentException("'Either type' is null, " +
					"or both dbName and idPart are nulls.");		
			
		// try to find a standard URI, if exists, for a publication xref, 
		// or at least a standard name:
		if (dbName != null) {
			try {
				// try to get the preferred/standard name
				// for any type, for consistency
				dbName = MiriamLink.getName(dbName);
				
				// a shortcut: a standard and resolvable URI exists for some BioPAX types
				if ((type.equals(PublicationXref.class) && "pubmed".equalsIgnoreCase(dbName))
					|| type.equals(RelationshipTypeVocabulary.class)
					|| ProteinReference.class.isAssignableFrom(type)
					|| SmallMoleculeReference.class.isAssignableFrom(type))
				{	//get the standard URI and quit (success), or fail and continue making a new URI below...
					return MiriamLink.getIdentifiersOrgURI(dbName, idPart);
				} 
				
			} catch (IllegalArgumentException e) {
				log.debug("uri: not a standard db name or synonym: " + dbName, e);
			}
		}

		// If not returned above this point yet -
		// no standard URI (Identifiers.org) was found for this object, class -
		// then let's consistently build a new URI from args, anyway, the other way around:
		
		StringBuilder sb = new StringBuilder();		
		if (dbName != null) //lowercase for consistency
			sb.append(dbName.toLowerCase()); 	
		
		if (idPart != null) {
			if (dbName != null) sb.append("_");
			sb.append(idPart);
		}
		
		String localPart = sb.toString();
		String strategy = System
			.getProperty(PROPERTY_NORMALIZER_URI_STRATEGY, VALUE_NORMALIZER_URI_STRATEGY_MD5);
		if(VALUE_NORMALIZER_URI_STRATEGY_SIMPLE.equals(strategy) 
				//for xrefs, always use the simple URI strategy (makes them human-readable)
				|| Xref.class.isAssignableFrom(type))
		{
			//simply replace "unsafe" symbols with underscore (some uri clashes might be possible but rare...)
			localPart = localPart.replaceAll("[^-\\w]", "_");
		} else {
			//replace the local part with its md5 sum string (32-byte)
			localPart = ModelUtils.md5hex(localPart);
		}
		
		// create URI using the xml:base and digest of other values:
		return ((xmlBase!=null)?xmlBase:"") + type.getSimpleName() + "_" + localPart;		
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

		Collection<UnificationXref> urefs = new TreeSet<UnificationXref>(
				new Comparator<UnificationXref>() {
					public int compare(UnificationXref o1, UnificationXref o2) {
						String s1 = o1.getDb() + o1.getId();
						String s2 = o2.getDb() + o2.getId();
						return s1.compareTo(s2);
					}
				}
		);

		for(UnificationXref ux : new ClassFilterSet<Xref,UnificationXref>(r.getXref(), UnificationXref.class))
		{
			if(ux.getDb() != null && ux.getId() != null) {
				urefs.add(ux);
			} 
		}
		
		return urefs;
	}

	
	/**
	 * Finds preferred unification xref, if possible.
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
				if(ret == null) {
					ret = (UnificationXref)x;
				} else {
					//another same kind xref is found;
					//can't test here if several IDs map to the same thing;
					//so, give up (for safety) - return null.
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
				// so let's generate a safe URI instead of standard identifiers.org
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
				String 	idPart = uref.getId(); //tissue/cellType terms can be added below:
				if(bs.getTissue()!=null && !bs.getTissue().getTerm().isEmpty()) 
					idPart += "_" + bs.getTissue().getTerm().iterator().next();
				if(bs.getCellType()!=null && !bs.getCellType().getTerm().isEmpty()) 
					idPart += "_" + bs.getCellType().getTerm().iterator().next();
				
				String uri = uri(xmlBase, uref.getDb(), idPart, BioSource.class);
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
							+ ". " + e + ". ");
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
	 * @param pro
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
	 * @param biopaxData String
	 * @return
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
	 * @return
	 */
	private String getXmlBase(Model modelToNormalize) {
		if(xmlBase != null && !xmlBase.isEmpty())
			return xmlBase; //this one is preferred
		else
			return 
				(modelToNormalize.getXmlBase() != null) 
					? modelToNormalize.getXmlBase() : "";
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
