package org.biopax.paxtools.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.util.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * An advanced BioPAX utility class that implements
 * several useful algorithms to extract root or child
 * BioPAX L3 elements, remove dangling, replace elements
 * or identifiers, etc.
 * @author rodche, Arman,..
 */
public final class ModelUtils
{
	private static final Log LOG = LogFactory.getLog(ModelUtils.class);

	//protected constructor
	ModelUtils() {
		throw new AssertionError("Not instantiable");
	}

	
	/**
	 * To ignore 'nextStep' property (in most algorithms),
	 * because it can eventually lead us outside current pathway,
	 * and normally step processes are listed in the pathwayComponent
	 * property as well.
	 */
	public static final Filter<PropertyEditor> nextStepFilter = new Filter<PropertyEditor>()
	{
		public boolean filter(PropertyEditor editor)
		{
			return !editor.getProperty().equals("nextStep") && !editor.getProperty().equals("NEXT-STEP");
		}
	};

	public static final Filter<PropertyEditor> evidenceFilter = new Filter<PropertyEditor>()
	{
		public boolean filter(PropertyEditor editor)
		{
			return !editor.getProperty().equals("evidence") && !editor.getProperty().equals("EVIDENCE");
		}
	};

	public static final Filter<PropertyEditor> pathwayOrderFilter = new Filter<PropertyEditor>()
	{
		public boolean filter(PropertyEditor editor)
		{
			return !editor.getProperty().equals("pathwayOrder");
		}
	};


	public static final MessageDigest MD5_DIGEST; //to calculate the PK from URI

	static
	{
		try
		{
			MD5_DIGEST = MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException("Cannot instantiate MD5 MessageDigest!", e);
		}
	}

	private final static BioPAXFactory factory = BioPAXLevel.L3.getDefaultFactory();

	private final static EditorMap em = SimpleEditorMap.L3;

	private final static BioPAXIOHandler io = new SimpleIOHandler(BioPAXLevel.L3);

	/**
	 * Controlled vocabulary terms for the RelationshipType
	 * CV to be added with auto-generated/inferred comments
	 * and/or relationship xrefs.
	 * <p/>
	 * We do not want to use "official" CV terms and IDs
	 * from the PSI-MI "cross-reference" branch for several reasons:
	 * - it's does not have terms we'd like and have those we'd not use
	 * - to distinguish auto-generated rel. xrefs from the original BioPAX data
	 * - hack: we also want put the related BioPAX element's URI into the xref's 'id' proerty
	 * (this is not what the BioPAX standard officially regarding to use of xrefs)
	 * @author rodche
	 */
	public static enum RelationshipType
	{
		PROCESS, // refers to a parent pathway or interaction
		ORGANISM,
		GENE, // term for, e.g., Entrez Gene rel. xrefs in protein references
		SEQUENCE, // e.g, to relate UniProt to RefSeq identifiers (incl. for splice variants...)
		; //TODO add more on use-case bases...
	}


	/**
	 * A comment (at least - prefix) to add to all generated objects
	 */
	public static final String COMMENT_FOR_GENERATED = "auto-generated";


	/* 
		 * URI prefix for auto-generated utility class objects
		 * (can be useful, for consistency, during, e.g.,
		 * data convertion, normalization, merge, etc.
		 */
	public static final String BIOPAX_URI_PREFIX = "urn:biopax:";


	/**
	 * This is to consistently create URI prefixes for
	 * auto-generated/inferred Xref objects
	 * (except for PublicationXref, where creating of
	 * something like, e.g., 'urn:miriam:pubmed:' is recommended).
	 * @param clazz
	 * @return
	 */
	public static String uriPrefixForGeneratedXref(Class<? extends Xref> clazz)
	{
		String prefix = BIOPAX_URI_PREFIX + clazz.getSimpleName() + ":";

		if (PublicationXref.class.equals(clazz) && LOG.isWarnEnabled())
		{
			LOG.warn("uriPrefixForGeneratedXref: for a PublicationXref, " +
			         "one should probably use a different prefix, " +
			         "e.g., 'urn:miriam:pubmed:', etc. istead of this: " + prefix);
		}

		return prefix;
	}


	/**
	 * Gets a URI for a special (internal, not standard, but useful)
	 * RelationshipTypeVocabulary, which can be used for such objects
	 * auto-generated by this class methods (also by any BioPAX reasoner/tool)
	 * @param relationshipType
	 * @return
	 */
	public static String relationshipTypeVocabularyUri(String relationshipType)
	{
		return BIOPAX_URI_PREFIX + "RelationshipTypeVocabulary:" + relationshipType.trim().toUpperCase();
	}


	static
	{
		((SimpleIOHandler) io).mergeDuplicates(true);
		((SimpleIOHandler) io).normalizeNameSpaces(false);
	}


	/**
	 * Replaces BioPAX elements in the model with ones from the map,
	 * updates corresponding BioPAX object references.
	 * <p/>
	 * It does neither remove the old nor add new elements in the model
	 * (if required, one can do this before/after this method, e.g., using
	 * the same 'subs' map)
	 * <p/>
	 * This does visit all object properties of each "explicit" element
	 * in the model, but does traverse deeper into one's sub-properties
	 * to replace something there as well (e.g., nested member entity references
	 * are not replaced unless parent entity reference present in the model)
	 * <p/>
	 * This does not automatically move/migrate old (replaced) object's
	 * children to new objects (the replacement ones are supposed to have
	 * their own properties already set or to be set shortly; otherwise,
	 * consider using of something like {@link #fixDanglingInverseProperties(BioPAXElement, Model)} after.
	 * <p/>
	 * As the result, corresponding object properties will mostly refer to new (replacement) values,
	 * others to old ones (rare, if nested implicit object referred to the replaced value);
	 * therefore, some of the replaced/removed objects will lost their nested children
	 * (where there exists a property, inverse property pair), and such
	 * child objects, in turn, eventually become dangling (if not used by other BioPAX elements).
	 * @param model
	 * @param subs the replacements map (many-to-one, old-to-new)
	 * @exception IllegalBioPAXArgumentException if there is an incompatible type replacement object
	 */
	public static void replace(Model model, final Map<? extends BioPAXElement, ? extends BioPAXElement> subs)
	{
		// update properties

		Visitor visitor = new Visitor()
		{
			@Override
			public void visit(BioPAXElement domain, Object range, Model model, PropertyEditor editor)
			{
				if (editor instanceof ObjectPropertyEditor && range != null && subs.containsKey(range))
				{
					BioPAXElement value = (BioPAXElement) range;
					// 'value' is to be replaced with the 'replacement'
					BioPAXElement replacement = subs.get(range);
					// normal biopax property -
					if (replacement != null && !editor.getRange().isInstance(replacement))
						throw new IllegalBioPAXArgumentException("Incompatible type! Attempted to replace " +
						                                         value.getRDFId() + " (" + value + "; " +
						                                         value.getModelInterface().getSimpleName() + ")" +
						                                         " with " +
						                                         ((replacement != null) ? replacement.getRDFId() :
								                                         "") +
						                                         " (" + replacement + "); for property: " +
						                                         editor.getProperty() + " of bean: " +
						                                         domain.getRDFId() + " (" + domain + "; " +
						                                         domain.getModelInterface().getSimpleName() + ")");

					if (editor.isMultipleCardinality()) editor.removeValueFromBean(value, domain);
					editor.setValueToBean(replacement, domain);
				}
			}
		};

		Traverser traverser = new Traverser(em, visitor);
		for (BioPAXElement bpe : new HashSet<BioPAXElement>(model.getObjects()))
		{
			// update object properties and clear inverse properties using 'subs' map	
			traverser.traverse(bpe, null); //model is not needed
		}
	}


	/**
	 * Finds a subset of "root" BioPAX objects of specific class (incl. sub-classes)
	 * <p/>
	 * Note: however, such "root" elements may or may not be, a property of other
	 * elements, not included in the model.
	 * @param model
	 * @param filterClass
	 * @return
	 */
	public static <T extends BioPAXElement> Set<T> getRootElements(final Model model, final Class<T> filterClass)
	{
		// copy all such elements (initially, we think all are roots...)
		final Set<T> result = Collections.newSetFromMap(new ConcurrentHashMap<T, Boolean>());
		result.addAll(model.getObjects(filterClass));

		ExecutorService exe = Executors.newCachedThreadPool();
		// but we run from every element (all types)
		for (final BioPAXElement e : model.getObjects()) {
			exe.submit(new Runnable() {
				@Override
				public void run() {
					//new "shallow" traverser (visits direct properties, i.e., visitor does not call traverse again) 
					new Traverser(em, new Visitor() {
							@Override
							public void visit(BioPAXElement parent, Object value, Model model,
									PropertyEditor<?, ?> editor) {
								if (filterClass.isInstance(value)) result.remove(value);
							}
						}
					).traverse(e, null);
				}
			});
		}
		
		exe.shutdown();
		try {
			exe.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			throw new RuntimeException("Interrupted!",e1);
		}

		return result;
	}

	/**
	 * Iteratively removes "dangling" elements
	 * of the type, e.g., all utility class objects
	 * not used by others, from current model.
	 * <p/>
	 * This, however, does not change relationships
	 * among objects, particularly, some inverse properties,
	 * such as entityReferenceOf or xrefOf, may still
	 * refer to a removed object.
	 * @param model
	 * @param clazz filter-class
	 */
	public static <T extends BioPAXElement> void removeObjectsIfDangling(Model model, Class<T> clazz)
	{
		Set<T> dangling = getRootElements(model, clazz);
		// get rid of dangling objects
		if (!dangling.isEmpty())
		{
			if (LOG.isInfoEnabled()) LOG.info(dangling.size() + " " + clazz.getSimpleName() +
			                                  " dangling objects will be deleted...");


			for (BioPAXElement thing : dangling)
			{
				model.remove(thing);
				if (LOG.isDebugEnabled()) LOG.debug(
						"removed (dangling) " + thing.getRDFId() + " (" + thing.getModelInterface().getSimpleName() +
						") " + thing);
			}

			// some may have become dangling now, so check again...
			removeObjectsIfDangling(model, clazz);
		}
	}


	/**
	 * For the specified model, this method
	 * iteratively copies given property values
	 * from parent BioPAX elements to children.
	 * If the property is multiple cardinality property, it will add
	 * new values, otherwise - it will set it only if was empty;
	 * in both cases it won't delete/override existing values!
	 * @param model
	 * @param property property name
	 * @param forClasses (optional) infer/set the property for these types only
	 * @see PropertyReasoner
	 */
	public static void inferPropertyFromParent(Model model, final String property,
			final Class<? extends BioPAXElement>... forClasses)
	{
		// for each ROOT element (puts a strict top-down order on the following)
		ExecutorService exec = Executors.newCachedThreadPool();		
		Set<BioPAXElement> roots = getRootElements(model, BioPAXElement.class);
		for (final BioPAXElement bpe : roots) {
			exec.submit(new Runnable() {
						@Override
						public void run() {
							PropertyReasoner reasoner = new PropertyReasoner(property, em);
							reasoner.setDomains(forClasses);
							reasoner.inferPropertyValue(bpe);
						}
					}	
			);
		}
		exec.shutdown();
		try {
			exec.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted!", e);
		}
	}

	
	/**
	 * Iteratively copies given property values
	 * from parent BioPAX elements to children.
	 * 
	 * If the property is multiple cardinality property, it will add
	 * new values, otherwise - it will set it only if it was empty;
	 * in both cases it won't delete/override existing values!
	 * 
	 * @param model
	 * @param properties BioPAX properties (names) to infer values of
	 * @param forClasses (optional) infer/set the property for these types only
	 * @throws InterruptedException 
	 * @see PropertyReasoner
	 */
	public static void inferPropertiesFromParent(Model model, final Set<String> properties,
			final Class<? extends BioPAXElement>... forClasses)
	{
		ExecutorService exec = Executors.newCachedThreadPool();

		Set<BioPAXElement> roots = getRootElements(model, BioPAXElement.class);
		for (final BioPAXElement bpe : roots) {	
			for(String property : properties) {
				final String p = property;
				exec.submit(new Runnable() {
							@Override
							public void run() {
								PropertyReasoner reasoner = new PropertyReasoner(p, em);
								reasoner.setDomains(forClasses);
								reasoner.inferPropertyValue(bpe);
							}
						}	
				);
			}
		}
		
		exec.shutdown();
		try {
			exec.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted!", e);
		}
	}
	
	
	/**
	 * Cuts the BioPAX model off other models and BioPAX objects
	 * by essentially performing write/read to/from OWL.
	 * The resulting model contains new objects with same IDs
	 * and have object properties "fixed", i.e., dangling values
	 * become null/empty, and inverse properties (e.g. xrefOf)
	 * re-calculated. The original model is unchanged.
	 * @param model
	 * @return copy of the model
	 * @exception IOException
	 */
	public static Model writeRead(Model model)
	{
		BioPAXIOHandler io = new SimpleIOHandler(model.getLevel());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		io.convertToOWL(model, baos);
		return io.convertFromOWL(new ByteArrayInputStream(baos.toByteArray()));
	}


	/**
	 * Gets direct children of a given BioPAX element
	 * and adds them to a new model.
	 * @param bpe
	 * @return new model
	 */
	public static Model getDirectChildren(BioPAXElement bpe)
	{
		Model m = factory.createModel();

		final AbstractTraverser traverser = new AbstractTraverser(em)
		{
			@Override
			protected void visit(Object range, BioPAXElement domain, Model model, PropertyEditor editor)
			{
				if (range instanceof BioPAXElement && !model.containsID(((BioPAXElement) range).getRDFId()))
					model.add((BioPAXElement) range);
			}
		};

		traverser.traverse(bpe, m);

		return m;
	}


	/**
	 * Gets all the child BioPAX elements of a given BioPAX element
	 * (using the "tuned" {@link Fetcher}) and adds them to a
	 * new model.
	 * @param bpe
	 * @param filters property filters (e.g., for Fetcher to skip some properties). Default is to skip 'nextStep'.
	 * @return
	 */
	public static Model getAllChildren(BioPAXElement bpe, Filter<PropertyEditor>... filters)
	{

		Model m = factory.createModel();
		if (filters.length == 0)
		{
			new Fetcher(em, nextStepFilter).fetch(bpe, m);
		} else
		{
			new Fetcher(em, filters).fetch(bpe, m);
		}
		m.remove(bpe); // remove the parent

		return m;
	}


	/**
	 * Collects all child BioPAX elements of a given BioPAX element
	 * (using the "tuned" {@link Fetcher})
	 * @param model
	 * @param bpe
	 * @param filters property filters (e.g., for Fetcher to skip some properties). Default is to skip 'nextStep'.
	 * @return
	 */
	public static Set<BioPAXElement> getAllChildrenAsSet(Model model, BioPAXElement bpe,
			Filter<PropertyEditor>... filters)
	{

		Set<BioPAXElement> toReturn = null;
		if (filters.length == 0)
		{
			toReturn = new Fetcher(em, nextStepFilter).fetch(bpe);
		} else
		{
			toReturn = new Fetcher(em, filters).fetch(bpe);
		}

		toReturn.remove(bpe); // remove the parent

		return toReturn;
	}


	/**
	 * Collects direct children of a given BioPAX element
	 * @param model
	 * @param bpe
	 * @return
	 */
	public static Set<BioPAXElement> getDirectChildrenAsSet(Model model, BioPAXElement bpe)
	{
		final Set<BioPAXElement> toReturn = new HashSet<BioPAXElement>();

		final AbstractTraverser traverser = new AbstractTraverser(em)
		{
			@Override
			protected void visit(Object range, BioPAXElement domain, Model model, PropertyEditor editor)
			{
				if (range instanceof BioPAXElement)
				{
					toReturn.add((BioPAXElement) range);
				}
			}
		};

		traverser.traverse(bpe, null);
		return toReturn;
	}


	/**
	 * Creates "process" (membership) relationship xrefs
	 * for each child {@link Entity} if possible.
	 * <p/>
	 * For each child {@link Entity} of every process
	 * (of the type given by the second argument), creates a
	 * relationship xref with the following properties:
	 * - db = provider (a name given by the second parameter)
	 * - id = the rdfId (URI) of the parent process
	 * - relationshipType = controlled vocabulary: "process" (MI:0359), urn:miriam:obo.mi:MI%3A0359
	 * - comment = "Auto-generated by Paxtools" (also added to the CV and its unification xref)
	 * <p/>
	 * Note: ok for Pathway, but confusing, expensive / wrong with Interaction types
	 * @param model
	 * @param <T>
	 * @param processClass to relate entities with an interaction/pathway of this type
	 */
	public static <T extends Process> void generateEntityProcessXrefs(Model model, Class<T> processClass)
	{
		// use a special relationship CV;
		RelationshipTypeVocabulary cv = getTheRelatioshipTypeCV(model, RelationshipType.PROCESS);

		Set<T> processes = new HashSet<T>(model.getObjects(processClass)); //to avoid concurr. mod. ex.
		for (T ownerProc : processes)
		{
			// prepare the xref to use in children
			String relXrefId = generateURIForXref(COMMENT_FOR_GENERATED, ownerProc.getRDFId(),
			                                      RelationshipXref.class);
			RelationshipXref rx = (RelationshipXref) model.getByID(relXrefId);
			if (rx == null)
			{
				rx = model.addNew(RelationshipXref.class, relXrefId);
				rx.addComment(COMMENT_FOR_GENERATED);
				rx.setDb(COMMENT_FOR_GENERATED);
				rx.setId(ownerProc.getRDFId());
				rx.setRelationshipType(cv);
			}

			// add the xref to all (biologically) child entities
			Model childModel = getAllChildren(ownerProc, pathwayOrderFilter, evidenceFilter);
			saveRelationship(model, childModel.getObjects(Entity.class), rx, true, false);
		}
	}


	/**
	 * Creates pathway membership relationship xrefs and
	 * annotations for each child element if possible.
	 * This is Level3 specific.
	 * @param model
	 * @param forBiopaxType
	 * @param addRelationshipXrefs
	 * @param addPathwaysToAnnotations
	 * @see {@link BioPAXElement#getAnnotations()}
	 *      <p/>
	 *      For each child {@link XReferrable} of every pathway it creates a
	 *      relationship xref with the following properties:
	 *      - db = provider (a name given by the second parameter)
	 *      - id = the rdfId (URI) of the parent process
	 *      - relationshipType = controlled vocabulary: "process" (MI:0359), urn:miriam:obo.mi:MI%3A0359
	 *      - comment = "Auto-generated by Paxtools" (also added to the CV and its unification xref)
	 *      And also adds parent pathways to child elements's annotation map
	 *      using {@link AnnotationMapKey} "PARENT_PATHWAYS" as the key.
	 */
	public static void calculatePathwayMembership(Model model, Class<? extends BioPAXElement> forBiopaxType,
			boolean addRelationshipXrefs, boolean addPathwaysToAnnotations)
	{
		Set<Pathway> processes = new HashSet<Pathway>(model.getObjects(Pathway.class)); //to avoid concurr. mod. ex.
		for (Pathway ownerProc : processes)
		{
			// use a special relationship CV;
			RelationshipTypeVocabulary cv = getTheRelatioshipTypeCV(model, RelationshipType.PROCESS);

			// prepare the xref to use in children
			String relXrefId = generateURIForXref(COMMENT_FOR_GENERATED, ownerProc.getRDFId(),
			                                      RelationshipXref.class);
			RelationshipXref rx = (RelationshipXref) model.getByID(relXrefId);
			if (rx == null)
			{
				rx = model.addNew(RelationshipXref.class, relXrefId);
				rx.addComment(COMMENT_FOR_GENERATED);
				rx.setDb(COMMENT_FOR_GENERATED);
				rx.setId(ownerProc.getRDFId());
				rx.setRelationshipType(cv);
			}

			// add the xref to all (biologically) child entities
			Model childModel = getAllChildren(ownerProc, pathwayOrderFilter, evidenceFilter);
			saveRelationship(model, childModel.getObjects(forBiopaxType), rx, addRelationshipXrefs,
			                 addPathwaysToAnnotations);
		}
	}


	/**
	 * Adds the relationship xref to every xreferrable entity in the set
	 * (and optionally - the corresponding pathway to the annotations map)
	 * @param model
	 * @param objects
	 * @param rx
	 * @param addRelationshipXrefs
	 * @param addPathwaysToAnnotations
	 */
	private static void saveRelationship(Model model, Set<? extends BioPAXElement> elements, RelationshipXref rx,
			boolean addRelationshipXrefs, boolean addPathwaysToAnnotations)
	{
		if (addPathwaysToAnnotations || addRelationshipXrefs)
		{
			for (BioPAXElement ent : elements)
			{
				if (addRelationshipXrefs && ent instanceof XReferrable) ((XReferrable) ent).addXref(rx);

				if (addPathwaysToAnnotations)
				{
					final String key = AnnotationMapKey.PARENT_PATHWAYS.toString();
					Set<Pathway> ppw = (Set<Pathway>) ent.getAnnotations().get(key);
					if (ppw == null)
					{
						ppw = new HashSet<Pathway>();
						ent.getAnnotations().put(key, ppw);
					}
					ppw.add((Pathway) model.getByID(rx.getId()));
				}
			}
		} else
		{
			throw new IllegalArgumentException("Useless call or a bug: " + "both boolean parameters are 'false'!");
		}
	}


	/**
	 * Auto-generates organism relationship xrefs -
	 * for BioPAX entities that do not have such property (but their children do),
	 * such as of Interaction, Protein, Complex, DNA, Protein, etc. classes.
	 * <p/>
	 * Infers organisms in two steps:
	 * <p/>
	 * 1. add organisms as relationship xrefs
	 * of {@link SimplePhysicalEntity} objects (from EntityReference objects),
	 * except for smallmolecules.
	 * 2. infer organism information recursively via all children,
	 * but only when children are also Entity objects (not utility classes)
	 * (equivalently, this can be achieved by collecting all the children first,
	 * though not visiting properties who's range is a sub-class of UtilityClass)
	 * @param model
	 */
	public static void generateEntityOrganismXrefs(Model model)
	{
		// The first pass (physical entities)
		Set<SimplePhysicalEntity> simplePEs = // prevents concurrent mod. exceptions
				new HashSet<SimplePhysicalEntity>(model.getObjects(SimplePhysicalEntity.class));
		for (SimplePhysicalEntity spe : simplePEs)
		{
			//get the organism value (BioSource) from the ER; create/add rel. xrefs to the spe
			EntityReference er = spe.getEntityReference();
			// er can be generic (member ers), so -
			Set<BioSource> organisms = getOrganismsFromER(er);
			addOrganismXrefs(model, spe, organisms);
		}

		// use a special filter for the (child elements) fetcher
		Filter<PropertyEditor> entityRangeFilter = new Filter<PropertyEditor>()
		{
			@Override
			public boolean filter(PropertyEditor editor)
			{
				// values are of Entity sub-class -
				return Entity.class.isAssignableFrom(editor.getRange());
			}
		};

		/* 
				 * The second pass (all entities, particularly -
				 * Pathway, Gene, Interaction, Complex and generic physical entities
				 */
		Set<Entity> entities = new HashSet<Entity>(model.getObjects(Entity.class));
		for (Entity entity : entities)
		{
			Set<BioSource> organisms = new HashSet<BioSource>();

			//If the entity has "true" organism property (it's Gene or Pathway), collect it
			addOrganism(entity, organisms);

			/* collect its children (- of Entity class only, 
						 * i.e., won't traverse into UtilityClass elements's properties)
						 *
						 * Note: although Stoichiometry.physicalEntity,
						 *       ExperimentalForm.experimentalFormEntity,
						 *       and PathwayStep.stepProcess are (only) examples
						 *       when an Entity can be value of a UtilityClass
						 *       object's property, we have to skip these
						 *       utility classes (with their properties) anyway.
						 */
			Model submodel = getAllChildren(entity, entityRangeFilter);
			// now, collect organism values from the children entities 
			// (using both property 'organism' and rel.xrefs created above!)
			for (Entity e : submodel.getObjects(Entity.class))
			{
				//skip SM
				if (e instanceof SmallMolecule) continue;

				//has "true" organism property? (a Gene or Pathway?) Collect it.
				addOrganism(e, organisms);
				// check in rel. xrefs
				for (Xref x : e.getXref())
				{
					if (x instanceof RelationshipXref)
					{
						if (isOrganismRelationshipXref((RelationshipXref) x))
						{
							//previously, xref.id was set to a BioSource' ID!
							assert (x.getId() != null);
							BioSource bs = (BioSource) model.getByID(x.getId());
							assert (bs != null);
							organisms.add(bs);
						}
					}
				}
			}

			// add all the organisms (xrefs) to this entity
			addOrganismXrefs(model, entity, organisms);
		}
	}


	/**
	 * Returns a set of organism of the entity reference.
	 * If it is a generic entity reference (has members),
	 * - will recursively collect all the organisms from
	 * its members.
	 * @param er
	 * @return
	 */
	private static Set<BioSource> getOrganismsFromER(EntityReference er)
	{
		Set<BioSource> organisms = new HashSet<BioSource>();
		if (er instanceof SequenceEntityReference)
		{
			BioSource organism = ((SequenceEntityReference) er).getOrganism();
			if (organism != null)
			{
				organisms.add(organism);
			}

			if (!er.getMemberEntityReference().isEmpty())
			{
				for (EntityReference mer : er.getMemberEntityReference())
				{
					organisms.addAll(getOrganismsFromER(mer));
				}
			}
		}
		return organisms;
	}


	/**
	 * Adds entity's organism value (if applicable and it has any) to the set.
	 * @param entity
	 * @param organisms
	 */
	private static void addOrganism(BioPAXElement entity, Set<BioSource> organisms)
	{
		PropertyEditor editor = em.getEditorForProperty("organism", entity.getModelInterface());
		if (editor != null)
		{
			Object o = editor.getValueFromBean(entity);
			if (o != null)
			{
				Set<BioSource> seto = (Set<BioSource>) o;
				organisms.addAll(seto);
			}
		}
	}


	/**
	 * Generates a relationship xref for each
	 * organism (BioSource) in the list and adds them to the entity.
	 * @param model
	 * @param entity
	 * @param organisms
	 */
	private static void addOrganismXrefs(Model model, Entity entity, Set<BioSource> organisms)
	{
		// create/find a RelationshipTypeVocabulary with term="ORGANISM"
		RelationshipTypeVocabulary cv = getTheRelatioshipTypeCV(model, RelationshipType.ORGANISM);
		// add xref(s) to the entity
		for (BioSource organism : organisms)
		{
			String db = COMMENT_FOR_GENERATED;
			String id = organism.getRDFId();
			String relXrefId = generateURIForXref(db, id, RelationshipXref.class);
			RelationshipXref rx = (RelationshipXref) model.getByID(relXrefId);
			if (rx == null)
			{
				rx = model.addNew(RelationshipXref.class, relXrefId);
				rx.setRelationshipType(cv);
				rx.addComment(COMMENT_FOR_GENERATED);
				rx.setDb(db);
				rx.setId(id);
			}
			entity.addXref(rx);
		}
	}


	/**
	 * Finds in the model or creates a new special RelationshipTypeVocabulary
	 * controlled vocabulary with the term value defined by the argument (enum).
	 * @param model
	 * @param relationshipType
	 * @return
	 */
	private static RelationshipTypeVocabulary getTheRelatioshipTypeCV(Model model, RelationshipType relationshipType)
	{
		String cvId = relationshipTypeVocabularyUri(relationshipType.name());
		// try to get from the model first
		RelationshipTypeVocabulary cv = (RelationshipTypeVocabulary) model.getByID(cvId);
		if (cv == null)
		{ // one instance per model to be created
			cv = model.addNew(RelationshipTypeVocabulary.class, cvId);
			cv.addTerm(relationshipType.name());
			cv.addComment(COMMENT_FOR_GENERATED);

			/* disabled: in favor of custom terms from RelationshipType -
			//String uxid = "urn:biopax:UnificationXref:MI_MI%3A0359";
			String uxid = generateURIForXref("MI", "MI:0359", UnificationXref.class);
			UnificationXref ux = (UnificationXref) model.getByID(uxid);
			if(ux == null) {
				ux = model.addNew(UnificationXref.class, uxid);
				ux.addComment(COMMENT_FOR_GENERATED);
				ux.setDb("MI");
				ux.setId("MI:0359");
			}
			cv.addXref(ux);
			*/
		}

		return cv;
	}


	/**
	 * Builds a "normalized" RelationshipXref URI.
	 * @param db
	 * @param id
	 * @param type TODO
	 * @return new ID (URI); not null (unless it's a bug :))
	 */
	public static String generateURIForXref(String db, String id, Class<? extends Xref> type)
	{
		String rdfid;
		String prefix = uriPrefixForGeneratedXref(type);

		// add the local part of the URI encoded -
		try
		{
			rdfid = prefix + URLEncoder.encode(db.trim() + "_" + id.trim(), "UTF-8").toUpperCase();
		}
		catch (UnsupportedEncodingException e)
		{
			if (LOG.isWarnEnabled())
				LOG.warn("ID UTF-8 encoding failed! " + "Using the platform default (deprecated method).", e);
			rdfid = prefix + URLEncoder.encode(db.trim() + "_" + id.trim()).toUpperCase();
		}

		return rdfid;
	}


	/**
	 * TODO
	 * @param model
	 * @return
	 */
	public static Map<Class<? extends BioPAXElement>, Integer> generateClassMetrics(Model model)
	{
		Map<Class<? extends BioPAXElement>, Integer> metrics = new HashMap<Class<? extends BioPAXElement>, Integer>();
		for (BioPAXElement bpe : model.getObjects())
		{
			Integer count = metrics.get(bpe.getModelInterface());
			if (count == null)
			{
				count = 1;
			} else
			{
				count = count + 1;
			}
			metrics.put(bpe.getModelInterface(), count);
		}
		return metrics;
	}


	/**
	 * TODO
	 * @param model
	 * @param urn
	 * @param clazz
	 * @return
	 */
	public static <T extends BioPAXElement> T getObject(Model model, String urn, Class<T> clazz)
	{
		BioPAXElement bpe = model.getByID(urn);
		if (clazz.isInstance(bpe))
		{
			return (T) bpe;
		} else
		{
			return null;
		}
	}


	/**
	 * This is a special (not always applicable) utility method.
	 * It finds the list of IDs of objects in the model
	 * that have a NORMALIZED xref equivalent (same db,id,type)
	 * to at least one of the specified xrefs.
	 * @param model
	 * @param xrefs
	 * @param clazz
	 * @return
	 */
	public static Set<String> getByXref(Model model, Set<? extends Xref> xrefs, Class<? extends XReferrable> clazz)
	{
		Set<String> toReturn = new HashSet<String>();

		for (Xref xref : xrefs)
		{
			// map to a normalized RDFId for this type of xref:
			if (xref.getDb() == null || xref.getId() == null)
			{
				continue;
			}

			String xurn =
					generateURIForXref(xref.getDb(), xref.getId(), (Class<? extends Xref>) xref.getModelInterface());

			Xref x = (Xref) model.getByID(xurn);
			if (x != null)
			{
				// collect owners's ids (of requested type only)
				for (XReferrable xr : x.getXrefOf())
				{
					if (clazz.isInstance(xr))
					{
						toReturn.add(xr.getRDFId());
					}
				}
			}
		}

		return toReturn;
	}


	private static boolean isOrganismRelationshipXref(RelationshipXref rx)
	{
		RelationshipTypeVocabulary cv = rx.getRelationshipType();
		return cv != null &&
		       cv.getRDFId().equalsIgnoreCase(relationshipTypeVocabularyUri(RelationshipType.ORGANISM.name()));
	}

	private static boolean isProcessRelationshipXref(RelationshipXref rx)
	{
		RelationshipTypeVocabulary cv = rx.getRelationshipType();
		return cv != null &&
		       cv.getRDFId().equalsIgnoreCase(relationshipTypeVocabularyUri(RelationshipType.PROCESS.name()));
	}


	/**
	 * Calculates MD5 hash code (as 32-byte hex. string).
	 * @param id
	 * @return
	 */
	public static String md5hex(String id)
	{
		byte[] digest = MD5_DIGEST.digest(id.getBytes());
		StringBuffer sb = new StringBuffer();
		for (byte b : digest)
		{
			sb.append(Integer.toHexString((int) (b & 0xff) | 0x100).substring(1, 3));
		}
		String hex = sb.toString();
		return hex;
	}


	/**
	 * Unlinks <em>object properties</em> of the BioPAX object
	 * from values the model does not have.
	 * @param bpe a biopax object
	 * @param model the model to look for objects in
	 */
	public static void fixDanglingObjectProperties(BioPAXElement bpe, Model model)
	{
		final Visitor visitor = new Visitor()
		{
			@Override
			public void visit(BioPAXElement domain, Object range, Model model, PropertyEditor editor)
			{
				if (editor instanceof ObjectPropertyEditor)
				{
					BioPAXElement value = (BioPAXElement) range;
					if (value != null && !model.containsID(value.getRDFId())) editor.removeValueFromBean(value,
					                                                                                     domain);
				}
			}
		};

		Traverser traverser = new Traverser(em, visitor);
		traverser.traverse(bpe, model);
	}


	/**
	 * Unlinks <em>inverse properties</em> of the BioPAX object
	 * from values the other model does not have.
	 * @param bpe BioPAX object
	 * @param model where to look for other objects
	 */
	public static void fixDanglingInverseProperties(BioPAXElement bpe, Model model)
	{
		final Visitor visitor = new Visitor()
		{
			@Override
			public void visit(BioPAXElement domain, Object range, Model model, PropertyEditor editor)
			{
				BioPAXElement value = (BioPAXElement) range;
				if (value != null && !model.containsID(value.getRDFId()))
					editor.removeValueFromBean(domain, value); //right order!
			}
		};

		TraverserBilinked traverser = new TraverserBilinked(em, visitor);
		traverser.setInverseOnly(true);
		traverser.traverse(bpe, model);
	}









	// moved from FeatureUtils; provides operations for comparing features of physical entities.

	static enum FeatureType
	{
		FEATURE,
		NOT_FEATURE,
		UNKNOWN_FEATURE;
	}


	/**
	 * TODO
	 * @param first
	 * @param firstClass
	 * @param second
	 * @param secondClass
	 * @return
	 */
	public static Set<EntityFeature> getFeatureIntersection(PhysicalEntity first, FeatureType firstClass,
			PhysicalEntity second, FeatureType secondClass)
	{
		Set<EntityFeature> intersection = getFeatureSetByType(first, firstClass);
		intersection.removeAll(getFeatureSetByType(second, secondClass));
		return intersection;
	}

	/**
	 * TODO
	 * @param pe
	 * @param type
	 * @return
	 */
	public static Set<EntityFeature> getFeatureSetByType(PhysicalEntity pe, FeatureType type)
	{

		Set<EntityFeature> modifiableSet = new HashSet<EntityFeature>();

		switch (type)
		{
			case FEATURE:
				modifiableSet.addAll(pe.getFeature());
				break;
			case NOT_FEATURE:
				modifiableSet.addAll(pe.getNotFeature());
				break;
			case UNKNOWN_FEATURE:
			{
				if (pe instanceof SimplePhysicalEntity)
				{
					modifiableSet.addAll(((SimplePhysicalEntity) pe).getEntityReference().getEntityFeature());
					modifiableSet.removeAll(pe.getFeature());
					modifiableSet.removeAll(pe.getNotFeature());
				}
			}
		}
		return modifiableSet;
	}


	/**
	 * TODO
	 * @param er
	 * @param fix
	 * @return
	 */
	public static boolean checkERFeatureSet(EntityReference er, boolean fix)
	{
		boolean check = true;
		for (SimplePhysicalEntity spe : er.getEntityReferenceOf())
		{
			for (EntityFeature ef : spe.getFeature())
			{
				check = scanAndAddToFeatureSet(er, fix, check, ef);
				// if not fixing return at first fail, otherwise go on;
				if (!fix && !check) return check;
			}
			for (EntityFeature ef : spe.getNotFeature())
			{
				check = scanAndAddToFeatureSet(er, fix, check, ef);
				// if not fixing return at first fail, otherwise go on;
				if (!fix && !check) return check;
			}
		}
		return check;
	}


	/**
	 * TODO
	 * @param pe
	 * @return
	 */
	public static boolean checkMutuallyExclusiveSets(PhysicalEntity pe)
	{
		if (pe.getFeature().isEmpty() || pe.getNotFeature().isEmpty()) return true;
		else
		{
			Set<EntityFeature> test = new HashSet<EntityFeature>(pe.getFeature());
			test.retainAll(pe.getNotFeature());
			return test.size() == 0;
		}
	}

	private static boolean scanAndAddToFeatureSet(EntityReference er, boolean fix, boolean check, EntityFeature ef)
	{
		if (!er.getEntityFeature().contains(ef))
		{
			check = false;
			if (fix)
			{
				er.addEntityFeature(ef);
			}
		}
		return check;
	}

	/**
	 * TODO
	 * @param first
	 * @param second
	 * @param fix
	 * @return
	 */
	public static Set<EntityFeature> findFeaturesAddedToSecond(PhysicalEntity first, PhysicalEntity second,
			boolean fix)
	{

		if (checkCommonEntityReferenceForTwoPEs(first, second, fix)) return null;
		Set<EntityFeature> explicit =
				getFeatureIntersection(first, FeatureType.NOT_FEATURE, second, FeatureType.FEATURE);
		Set<EntityFeature> implicit =
				getFeatureIntersection(first, FeatureType.UNKNOWN_FEATURE, second, FeatureType.FEATURE);
		Set<EntityFeature> negativeImplicit =
				getFeatureIntersection(first, FeatureType.NOT_FEATURE, second, FeatureType.UNKNOWN_FEATURE);

		if (fix)
		{
			for (EntityFeature implied : implicit)
			{
				LOG.info("The feature " + implied + "implied as a not-feature of " + first + ". " +
				         "Adding it to the not-feature list");
				first.addNotFeature(implied);
			}

			for (EntityFeature implied : negativeImplicit)
			{
				LOG.info("The feature " + implied + "implied as a feature of " + second + ". " +
				         "Adding it to the feature list");
				second.addFeature(implied);
			}

		}
		explicit.retainAll(implicit);
		explicit.retainAll(negativeImplicit);
		return explicit;
	}

	private static boolean checkCommonEntityReferenceForTwoPEs(PhysicalEntity first, PhysicalEntity second,
			boolean fix)
	{
		if (first instanceof SimplePhysicalEntity)
		{
			EntityReference er = ((SimplePhysicalEntity) first).getEntityReference();
			if (!er.getEntityReferenceOf().contains(second))
			{
				LOG.warn("These two physicalEntities do not share an EntityReference. They can not be compared! " +
				         "Skipping");
				return false;
			} else if (!checkERFeatureSet(er, fix))
			{
				LOG.warn("ER feature set is incomplete!");
				if (!fix)
				{
					LOG.warn("fixing...");
				} else
				{
					LOG.warn("skipping");
					return false;
				}
			}
			return true;
		} else
		{
			LOG.warn("These two physicalEntities do not share an EntityReference. They can not be compared! " +
			         "Skipping");
			return false;
		}

	}


	public static void normalizeGenerics(Model model)
	{

		HashMap<Set<EntityReference>, EntityReference> memberMap = new HashMap<Set<EntityReference>,
				EntityReference>();
		Set<SimplePhysicalEntity> pes = model.getObjects(SimplePhysicalEntity.class);
		Set<SimplePhysicalEntity> pesToBeNormalized = new HashSet<SimplePhysicalEntity>();
		for (SimplePhysicalEntity pe : pes)
		{
			if (pe.getEntityReference() == null)
			{
				if (!pe.getMemberPhysicalEntity().isEmpty())
				{
					pesToBeNormalized.add(pe);
				}
			}
		}
		for (SimplePhysicalEntity pe : pesToBeNormalized)
		{
			try
			{
				createNewERandAddMembers(model, pe, memberMap);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}


		}
	}

	private static void createNewERandAddMembers(Model model, SimplePhysicalEntity pe,
			HashMap<Set<EntityReference>, EntityReference> memberMap)
	{

		SimplePhysicalEntity first = (SimplePhysicalEntity) pe.getMemberPhysicalEntity().iterator().next();
		String syntheticId = "http://biopax.org/generated/fixer/normalizeGenerics/" + pe.getRDFId();
		Set<EntityReference> members = pe.getGenericEntityReferences();
		EntityReference er = memberMap.get(members);
		if (er == null)
		{
			EntityReference firstEntityReference = first.getEntityReference();
			if (firstEntityReference != null)
			{
				er = (EntityReference) model.addNew(firstEntityReference.getModelInterface(), syntheticId);
				copySimplePointers(model, pe, er);

				for (EntityReference member : members)
				{
					er.addMemberEntityReference(member);
				}
				memberMap.put(members, er);
			}
		}
		pe.setEntityReference(er);

	}

	public static void copySimplePointers(Model model, Named pe, Named generic)
	{
		generic.setDisplayName(pe.getDisplayName());
		generic.setStandardName(pe.getStandardName());
		for (String name : pe.getName())
		{
			generic.addName(name);
		}
		for (Xref xref : pe.getXref())
		{
			if ((xref instanceof UnificationXref))
			{
				String id = "http://biopax.org/generated/fixer/copySimplePointers/" + xref.getRDFId();
				BioPAXElement byID = model.getByID(id);
				if (byID == null)
				{
					RelationshipXref rref = model.addNew(RelationshipXref.class, id);
					rref.setDb(xref.getDb());
					rref.setId(xref.getId());
					rref.setDbVersion(xref.getDbVersion());
					rref.setIdVersion(xref.getDbVersion());
					xref = rref;
				} else
				{
					xref = (Xref) byID;
				}
			}
			generic.addXref(xref);
		}
	}

	public void resolveFeatures(Model model)
	{
		if (!model.getLevel().equals(BioPAXLevel.L3))
		{
			//TODO Log error
		} else
		{
			resolveBindingFeatures(model);

			//For each entity reference:
			for (EntityReference er : model.getObjects(EntityReference.class))
			{
				for (SimplePhysicalEntity spe : er.getEntityReferenceOf())
				{
					for (Interaction interaction : spe.getParticipantOf())
					{
						//we will do this left to right
						if (interaction instanceof Conversion)
						{
							Conversion cnv = (Conversion) (interaction);
							if (cnv.getLeft().contains(spe))
							{
								for (PhysicalEntity physicalEntity : cnv.getRight())
								{
									if (physicalEntity instanceof SimplePhysicalEntity)
									{
										SimplePhysicalEntity otherSPE = (SimplePhysicalEntity) (physicalEntity);
										if (otherSPE.getEntityReference().equals(spe.getEntityReference()))
										{
											Set<EntityFeature> added =
													findFeaturesAddedToSecond(physicalEntity, otherSPE, true);
											Set<EntityFeature> removed =
													findFeaturesAddedToSecond(otherSPE, physicalEntity, true);
										}
									}
								}
								//TODO HANDLE complexes?
							}
						}
					}
				}
			}
		}
	}


	private void resolveBindingFeatures(Model model)
	{
		ShallowCopy copier = new ShallowCopy(BioPAXLevel.L3);

		//For each Complex
		Set<Complex> complexes = model.getObjects(Complex.class);
		for (Complex complex : complexes)
		{
			resolveBindingFeatures(model, complex, copier);


		}
	}

	private void resolveBindingFeatures(Model model, Complex complex, ShallowCopy copier)
	{
		Set<PhysicalEntity> components = complex.getComponent();
		for (PhysicalEntity component : components)
		{
			resolveFeaturesOfComponent(model, complex, component, copier);
		}
	}

	private void resolveFeaturesOfComponent(Model model, Complex complex, PhysicalEntity component,
			ShallowCopy copier)
	{
		boolean connected = false;
		Set<EntityFeature> feature = component.getFeature();
		for (EntityFeature ef : feature)
		{
			if (ef instanceof BindingFeature)
			{
				BindingFeature bindsTo = ((BindingFeature) ef).getBindsTo();
				Set<PhysicalEntity> featureOf = bindsTo.getFeatureOf();
				if (!SetEquivalanceChecker.isEquivalentIntersection(complex.getComponent(), featureOf))
				{
					System.err.println(
							"The Complex" + complex.getName() + "(" + complex.getRDFId() + ") has  component" +
							component.getDisplayName() + "(" + component.getRDFId() + ") which has" +
							"a binding feature (" + ef.getRDFId() + "), but none of the bound " +
							"participants are in this complex");
					//TODO This is an error - fail.
					return;
				} else
				{
					connected = true;

				}
			}
		}
		if (!connected)
		{
			Set<Interaction> participantOf = component.getParticipantOf();
			for (Interaction interaction : participantOf)
			{
				//It is ok for complex members to control a participant
				if (!(interaction instanceof Control))
				{
					component = createCopy(model, complex, component, copier);
					break;
				}
			}

			BindingFeature bf = model.addNew(BindingFeature.class,
			                                 component.getRDFId() + "bond" + "in_Complex_" + complex.getRDFId());
			component.addFeature(bf);
			if (component instanceof SimplePhysicalEntity)
			{
				((SimplePhysicalEntity) component).getEntityReference().addEntityFeature(bf);
			}
		}
	}

	private PhysicalEntity createCopy(Model model, Complex complex, PhysicalEntity component, ShallowCopy copier)
	{
		//This is an aggressive fix - if a complex member is present in both an interaction that is not a control
		// and a complex, we are creating clone, adding it a binding feature to mark it  and put it  into the
		// complex and remove the old one.
		complex.removeComponent(component);
		component = copier.copy(model, component, component.getRDFId() + "in_Complex_" + complex.getRDFId());
		complex.addComponent(component);
		return component;
	}


	/**
	 * This method iterates over the features in a model and tries to find equivalent objects and merges them.
	 * @param model to be fixed
	 */
	public static void replaceEquivalentFeatures(Model model)
	{

		HashSet<EquivalenceWrapper> equivalents = new HashSet<EquivalenceWrapper>();
		HashMap<EntityFeature, EntityFeature> mapped = new HashMap<EntityFeature, EntityFeature>();
		HashSet<EntityFeature> scheduled = new HashSet<EntityFeature>();

		for (EntityFeature ef : model.getObjects(EntityFeature.class))
		{
			if (ef.getEntityFeatureOf() == null)
			{
				inferEntityFromPE(ef, ef.getFeatureOf());
				if (ef.getEntityFeatureOf() == null) inferEntityFromPE(ef, ef.getNotFeatureOf());
			}
			EquivalenceWrapper wrapper = new EquivalenceWrapper(ef);
			if (equivalents.contains(wrapper))
			{
				if (LOG.isWarnEnabled())
					LOG.warn("removing: " + wrapper.getEqBpe() + "{" + wrapper.getEqBpe().getRDFId() + "}");
				scheduled.add(ef);
				mapped.put(ef, (EntityFeature) wrapper.getEqBpe());
			} else equivalents.add(wrapper);
		}
		for (EntityFeature entityFeature : scheduled)
		{
			model.remove(entityFeature);
		}
		for (PhysicalEntity physicalEntity : model.getObjects(PhysicalEntity.class))
		{
			Set<EntityFeature> features = new HashSet<EntityFeature>(physicalEntity.getFeature());
			for (EntityFeature feature : features)
			{
				EntityFeature that = mapped.get(feature);
				if (that != null && !that.equals(feature))
				{
					if (LOG.isWarnEnabled()) LOG.warn(" replacing " + feature +
					                                  "{" + feature.getRDFId() + "} with " +
					                                  that + "{" + that.getRDFId() + "}");
					physicalEntity.removeFeature(feature);
					physicalEntity.addFeature(that);
				}
			}
		}
	}


	private static void inferEntityFromPE(EntityFeature ef, Set<PhysicalEntity> pes)
	{

		for (PhysicalEntity physicalEntity : pes)
		{
			if (physicalEntity instanceof SimplePhysicalEntity)
			{
				EntityReference er = ((SimplePhysicalEntity) physicalEntity).getEntityReference();
				if (er != null)
				{
					er.addEntityFeature(ef);
					if (LOG.isWarnEnabled()) LOG.warn("Inferred the ER of " + ef.getRDFId() + " as " + er.getRDFId());
					return;
				}
			}
		}
	}


}



