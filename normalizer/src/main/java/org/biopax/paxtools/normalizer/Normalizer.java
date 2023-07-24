package org.biopax.paxtools.normalizer;

import org.apache.commons.lang3.StringUtils;
import org.biopax.paxtools.controller.ModelUtils;
import org.biopax.paxtools.converter.LevelUpgrader;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.ClassFilterSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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
	// (the default is biopax.normalizer.uri.strategy=md5 to generate 32-byte digest hex string)
	public static final String PROPERTY_NORMALIZER_URI_STRATEGY = "biopax.normalizer.uri.strategy";
	public static final String VALUE_NORMALIZER_URI_STRATEGY_SIMPLE = "simple";
	public static final String VALUE_NORMALIZER_URI_STRATEGY_MD5 = "md5"; //default strategy

	//short "codes" to use e.g. in a generated URI for some of instantiable BioPAX types:
	public static final Map<String,String> typeCodes = Map.ofEntries(
			//Entity class (lower-case values):
			Map.entry("BiochemicalReaction","br"),
			Map.entry("TransportWithBiochemicalReaction","tbr"),
			Map.entry("TemplateReaction","tr"),
			Map.entry("TemplateReactionRegulation","trr"),
			Map.entry("DnaRegion","dg"),
			Map.entry("RnaRegion","rg"),
			Map.entry("Pathway","pw"),
			Map.entry("PhysicalEntity","pe"),
			Map.entry("SmallMolecule","sm"),
			Map.entry("Protein","p"),
			Map.entry("Gene","g"),
			Map.entry("Complex","c"),
			Map.entry("Catalysis","cat"),
			Map.entry("ChemicalStructure","cs"),
			Map.entry("ComplexAssembly","ca"),
			Map.entry("Control","ct"),
			Map.entry("Conversion","r"),
			Map.entry("Transport","t"),
			Map.entry("GeneticInteraction","gi"),
			Map.entry("Interaction","i"),
			Map.entry("Modulation","m"),
			Map.entry("MolecularInteraction","mi"),
			Map.entry("Degradation","deg"),
			//UtilityClass (upper-case values):
			Map.entry("BioSource","BIO"),
			Map.entry("Provenance","PRO"),
			Map.entry("RelationshipXref","RX"),
			Map.entry("PublicationXref","PX"),
			Map.entry("UnificationXref","UX"),
			Map.entry("CellularLocationVocabulary","LV"),
			Map.entry("ControlledVocabulary","V"),
			Map.entry("CellVocabulary","CV"),
			Map.entry("TissueVocabulary","TV"),
			Map.entry("PhenotypeVocabulary","PV"),
			Map.entry("EvidenceCodeVocabulary","EV"),
			Map.entry("EntityReferenceTypeVocabulary","ERV"),
			Map.entry("ExperimentalFormVocabulary","XFV"),
			Map.entry("InteractionVocabulary","IV"),
			Map.entry("RelationshipTypeVocabulary","RTV"),
			Map.entry("SequenceModificationVocabulary","SMV"),
			Map.entry("SequenceRegionVocabulary","SRV"),
			Map.entry("SmallMoleculeReference","SMR"),
			Map.entry("BiochemicalPathwayStep","BPS"),
			Map.entry("PathwayStep","PS"),
			Map.entry("DeltaG","DG"),
			Map.entry("KPrime","KP"),
			Map.entry("DnaReference","DR"),
			Map.entry("DnaRegionReference","DGR"),
			Map.entry("RnaReference","RR"),
			Map.entry("RnaRegionReference","RGR"),
			Map.entry("EntityFeature","EF"),
			Map.entry("BindingFeature","BF"),
			Map.entry("CovalentBindingFeature","CF"),
			Map.entry("FragmentFeature","FF"),
			Map.entry("ModificationFeature","MF"),
			Map.entry("ExperimentalForm","XF"),
			Map.entry("Evidence","E"),
			Map.entry("ProteinReference","PR"),
			Map.entry("SequenceInterval","SI"),
			Map.entry("SequenceLocation","SL"),
			Map.entry("SequenceSite","SS"),
			Map.entry("Stoichiometry","S")
	);
	
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
	 * Normalizes xrefs to ease normalizing or merging other BioPAX objects later on,
	 * and also because some original xref URIs should in fact be used for different BioPAX types
	 * (for CV or ProteinReference instead); so, this will also replace URIs of xrefs having bad 'db' or 'id'.
	 * 
	 * @param model biopax model to update
	 * @param usePrefixAsDbName if possible, use CURIE prefix as xref.db instead of preferred name
	 */
	public void normalizeXrefs(Model model, boolean usePrefixAsDbName) {
		final NormalizerMap map = new NormalizerMap(model);
		final String xmlBase = getXmlBase(model); //current base, the default or model's one, if set.

		// use a copy of the xrefs set (to avoid concurrent exceptions)
		Set<? extends Xref> xrefs = new HashSet<>(model.getObjects(Xref.class));
		for(Xref ref : xrefs) {
			//won't normalize xrefs missing db or id property value (e.g., some of PublicationXrefs)
			if(ref.getDb() == null || ref.getId() == null) {
				continue;
			}

			//normalize name first
			Namespace ns = Resolver.getNamespace(ref.getDb()); //resolve a prefix, name, synonym or known spelling variants
			if(ns != null) {
				if (usePrefixAsDbName) {
					ref.setDb(ns.getPrefix()); //use bioregistry collection prefix (already lowercase)
				} else {
					ref.setDb(ns.getName().toLowerCase()); //use the standard name
				}
			}
			final String isoformName = (usePrefixAsDbName) ? "uniprot.isoform" : "uniprot isoform";

			String idPart = ref.getId();

			if(ref instanceof RelationshipXref) {
				//RXs might have the same db, id but different rel. type.
				RelationshipTypeVocabulary cv = ((RelationshipXref) ref).getRelationshipType();
				if(ref.getIdVersion() != null) {
					idPart += "_" + ref.getIdVersion();
				}
				if(cv != null && !cv.getTerm().isEmpty()) {
					idPart += "_" + StringUtils.join(cv.getTerm(), '_').toLowerCase();
				}
			} else if(ref instanceof UnificationXref) {
				// fix 'uniprot' instead 'uniprot isoform' and vice versa mistakes
				if (ref.getDb().startsWith("uniprot")) {
					//auto-fix (guess) for possibly incorrect db/id (can be 'uniprot isoform' with/no idVersion, etc..)
					if (isValidDbId("uniprot.isoform", ref.getId())
							&& ref.getId().contains("-")) //the second condition is important
					{	//then it's certainly an isoform id; so - fix the db name
						ref.setDb(isoformName); //fix the db
					} else {
						//id does not end with "-\\d+", i.e., not a isoform id
						//(idVersion is a different thing, but if db was "uniprot isoform" they probably misused idVersion)
						if(ref.getDb().equalsIgnoreCase(isoformName)) {
							if(ref.getIdVersion() != null && ref.getIdVersion().matches("^\\d+$")) {
								idPart = ref.getId() + "-" + ref.getIdVersion(); //guess idVersion is isoform number
							}
							if(isValidDbId(ref.getDb(), idPart)) {
								ref.setId(idPart); //moved the isoform # to the ID
								ref.setIdVersion(null);
							}
							else if(!isValidDbId(ref.getDb(), ref.getId())) {
								//certainly not isoform
								ref.setDb("uniprot"); //guess, fix
							}
							idPart = ref.getId();
						}
					}
				} else {//not any uniprot...
					//if not standard and has idVersion, add that
					if(ns == null && ref.getIdVersion() != null) {
							idPart += "_" + ref.getIdVersion();
					}
				}
			}

			// make a new URI and save in the URI replacement map
			String newUri = Normalizer.uri(xmlBase, ref.getDb(), idPart, ref.getModelInterface());
			map.put(ref, newUri);
		}
		
		// execute replace xrefs
		map.doSubs();
	}

	/*
	 * @param db must be valid MIRIAM db name or synonym
	 * @trows IllegalArgumentException when db is unknown name.
	 */
	private boolean isValidDbId(String db, String id) {
		return Resolver.checkRegExp(id, db);
	}

	/**
	 * Generates a new BioPAX element URI
	 * using given URI namespace (xml:base), BioPAX class, 
	 * and two different identifiers (at least one is required).
	 *
	 * ID registry data is used to get the standard db name and
	 * URI, if possible, for relationship type vocabulary,
	 * publication xref, entity reference, and bio source types.
	 * 
	 * @param xmlBase xml:base (common URI prefix for a BioPAX model), case-sensitive
	 * @param dbName a bio data collection name or synonym, case-insensitive
	 * @param idPart optional (can be null), e.g., xref.id, case-sensitive
	 * @param type BioPAX class
	 * @return URI
	 * @throws IllegalArgumentException if either type is null or both 'dbName' and 'idPart' are all nulls.
	 */
	public static String uri(final String xmlBase, String dbName, String idPart, Class<? extends BioPAXElement> type) {
		return uri(xmlBase, dbName, idPart, type, true);
	}

	/**
	 * Generates a new BioPAX element URI
	 * using given URI namespace (xml:base), BioPAX class,
	 * and two different identifiers (at least one is required).
	 *
	 * Registry is optionally used to get the standard db name and URI, if possible.
	 *
	 * @param xmlBase xml:base (common URI prefix for a BioPAX model), case-sensitive
	 * @param dbName a bio data collection name or synonym, case-insensitive
	 * @param idPart optional (can be null), e.g., xref.id, case-sensitive
	 * @param type BioPAX class
	 * @param useRegistry use or not the ID types registry data
	 * @return URI
	 * @throws IllegalArgumentException if either type is null or both 'dbName' and 'idPart' are all nulls.
	 */
	public static String uri(final String xmlBase, String dbName, String idPart,
							 Class<? extends BioPAXElement> type, boolean useRegistry) {

		if(type == null || (dbName == null && idPart == null)) {
			throw new IllegalArgumentException("Type is null or both dbName and idPart are null");
		}

		String uri = null;
		if (idPart != null) {
			idPart = idPart.trim();
		}
		if (dbName != null) {
			dbName = dbName.trim().toLowerCase();
		}

		// for some types, try to find a standard URI or at least a standard name:
		if (dbName != null && useRegistry) {
			Namespace ns = Resolver.getNamespace(dbName); //a synonym or (mis)spelling can match as well
			if (ns != null) {
				String prefix = ns.getPrefix();
				// make a bioregistry.io URI or CURIE for some of the BioPAX types
				if (type.equals(RelationshipTypeVocabulary.class)
						|| ProteinReference.class.isAssignableFrom(type)
						|| SmallMoleculeReference.class.isAssignableFrom(type)
						|| (type.equals(BioSource.class) && "ncbitaxon".equalsIgnoreCase(prefix) && idPart != null && idPart.matches("^\\d+$"))
				) {
					// makes URL
					uri = Resolver.getURI(prefix, idPart); //can be null when there's id-pattern mismatch
				} else if (type.equals(UnificationXref.class) && !"pubmed".equalsIgnoreCase(prefix) ||
						(type.equals(PublicationXref.class) && "pubmed".equalsIgnoreCase(prefix))) {
					//use CURIE for these xref types
					uri = Resolver.getCURIE(prefix, idPart);
				}
			}
		}

		if(StringUtils.isNotEmpty(uri)) {
			return uri; //done
		}

		// No standard URI was found; let's consistently build a hash-based URI:
		StringBuilder sb = new StringBuilder();
		if (dbName != null) {
			sb.append(dbName);
		}
		if (idPart != null) {
			if (dbName != null) {
				sb.append("_");
			}
			sb.append(idPart);
		}
		String localPart = sb.toString();
		String strategy = System.getProperty(PROPERTY_NORMALIZER_URI_STRATEGY, VALUE_NORMALIZER_URI_STRATEGY_MD5);
		if(VALUE_NORMALIZER_URI_STRATEGY_SIMPLE.equals(strategy)
				|| Xref.class.isAssignableFrom(type)
				|| ControlledVocabulary.class.isAssignableFrom(type)
				|| BioSource.class.isAssignableFrom(type))
		{
			//for xrefs, always use the simple URI strategy (human-readable)
			//replace unsafe symbols with underscore
			localPart = localPart.replaceAll("[^-\\w]", "_");
		} else {
			//replace the local part with its md5 hash string (32-byte)
			localPart = ModelUtils.md5hex(localPart);
		}

		// create URI using the xml:base and digest of other values:
		String prefix = typeCodes.get(type.getSimpleName());
		if(prefix == null) {
			prefix = type.getSimpleName();
		}
		uri = ((xmlBase != null) ? xmlBase : "") + prefix + "_" + localPart;
		return uri;
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
		List<UnificationXref> urefs = new ArrayList<>();
		for(UnificationXref ux : new ClassFilterSet<>(r.getXref(), UnificationXref.class))
		{
			if(ux.getDb() != null && ux.getId() != null) {
				urefs.add(ux);
			} 
		}
		Collections.sort(urefs, (o1, o2) -> {
			String s1 = o1.getDb() + o1.getId();
			String s2 = o2.getDb() + o2.getId();
			return s1.compareTo(s2);
		});
		return urefs;
	}

	
	/**
	 * Finds one preferred unification xref, if possible.
	 * Preferred db values are:
	 * "entrez gene" - for NucleicAcidReference and the subclasses;
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
			toReturn = findSingleUnificationXref(orderedUrefs, "entrez");
			//when xrefs were normalized to use either 'entrez gene' (the preferred name) or 'ncbigene' (the prefix)
			if(toReturn==null)
				toReturn = findSingleUnificationXref(orderedUrefs, "ncbigene");
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
		normalize(model, false);
	}

	
	/**
	 * BioPAX normalization 
	 * (modifies the original Model)
	 * 
	 * @param model BioPAX model to normalize
	 * @param usePrefixAsDbName if possible, use CURIE prefix as xref.db instead of preferred name
	 * @throws NullPointerException if model is null
	 * @throws IllegalArgumentException if model is not Level3 BioPAX
	 */
	public void normalize(Model model, boolean usePrefixAsDbName) {
		
		if(model.getLevel() != BioPAXLevel.L3)
			throw new IllegalArgumentException("Not Level3 model. " +
				"Consider converting it first (e.g., with the PaxTools).");
		
		//if set, update the xml:base
		if(xmlBase != null && !xmlBase.isEmpty())
			model.setXmlBase(xmlBase);

		// Normalize/merge xrefs first and then - CVs
		// (xrefs could have URIs that should be instead used for CV, PR, SMR or BS biopax types)
		log.info("Normalizing xrefs..." + description);
		normalizeXrefs(model, usePrefixAsDbName);
		
		// fix displayName where possible
		if(fixDisplayName) {
			log.info("Normalizing display names..." + description);
			fixDisplayName(model);
		}
			
		log.info("Normalizing CVs..." + description);
		normalizeCVs(model);
		
		//normalize BioSource objects (after Xrefs and CVs!)
		log.info("Normalizing organisms..." + description);
		normalizeBioSources(model);

		// auto-generate missing entity references:
		for(SimplePhysicalEntity spe : new HashSet<>(model.getObjects(SimplePhysicalEntity.class))) {
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
			//it does not check/fix the CV terms (but Validator can do if run before the Normalizer)
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
		//it's called after all the xrefs and CVs were normalized
		NormalizerMap map = new NormalizerMap(model);
		for(BioSource bs : model.getObjects(BioSource.class)) {
			UnificationXref uref = findPreferredUnificationXref(bs);
			//normally, the xref db is 'Taxonomy' (or a valid synonym)
			if (uref != null
				&& ( uref.getDb().equalsIgnoreCase("ncbitaxon") //should be (the preferred prefix in bioregistry.io)
					|| uref.getDb().toLowerCase().contains("taxonomy") //just in case..
					|| uref.getDb().equalsIgnoreCase("newt"))) {
				String 	idPart = uref.getId();

				//tissue/cellType terms can be added below:
				if(bs.getTissue()!=null && !bs.getTissue().getTerm().isEmpty()) 
					idPart += "_" + bs.getTissue().getTerm().iterator().next();
				if(bs.getCellType()!=null && !bs.getCellType().getTerm().isEmpty()) 
					idPart += "_" + bs.getCellType().getTerm().iterator().next();
				
				String uri = uri(xmlBase, uref.getDb(), idPart, BioSource.class); //for standard db, id it makes bioregistry.io/ncbitaxon:id URI
				map.put(bs, uri);
			} else {
				log.debug("Won't normalize BioSource"
						+ " : no taxonomy unification xref found in " + bs.getUri()
						+ ". " + description);
			}
		}
		map.doSubs();
	}

	private void normalizeERs(Model model) {
		
		NormalizerMap map = new NormalizerMap(model);
		
		// process the rest of utility classes (selectively though)
		for (EntityReference bpe : model.getObjects(EntityReference.class)) {
			
			//skip those with already normalized URIs
			if(bpe.getUri().contains("identifiers.org/")) {
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
				String uri;
				try { // make a new ID for the element
					uri = Resolver.getURI(db, id);
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
	 * from either its URI or one of the names (preferably - standard name)
	 * 
	 * @param pro data source (BioPAX Provenance)
	 */
	public static void autoName(Provenance pro) {
		String uri = pro.getUri();
		String key;
		if(StringUtils.startsWithIgnoreCase(uri, "urn:miriam:")
				|| StringUtils.containsIgnoreCase(uri,"identifiers.org/")
				|| StringUtils.containsIgnoreCase(uri, "bioregistry.io/")) {
			key = StringUtils.removeEnd(uri,"/"); //removes ending '/' if present
		} else if (pro.getStandardName() != null) {
			key = pro.getStandardName();
		} else {
			key = pro.getDisplayName();
		}

		if (key != null) {
			Namespace ns = Resolver.getNamespace(key);
			if(ns != null) {
				pro.setStandardName(ns.getName());
				pro.addName(ns.getPrefix());
			}
		}

		if(StringUtils.isBlank(pro.getStandardName())) {
			// find a standard name in names
			for (String name : pro.getName()) {
				Namespace ns = Resolver.getNamespace(name);
				if(ns != null) {
					String stdName = ns.getName();
					pro.setStandardName(stdName);
					pro.addName(ns.getPrefix());
					break;
				}
			}
		}

		//set display name if not set (standard name is set already)
		if(pro.getDisplayName() == null) {
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

}
