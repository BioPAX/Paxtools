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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * An advanced BioPAX utility class that implements
 * several useful algorithms to extract root or child
 * BioPAX L3 elements, remove dangling, replace elements
 * or identifiers, etc.
 * @author rodche, Arman 
 * //TODO consider breaking it down to several classes.
 */
public final class ModelUtils
{
	private static final Log LOG = LogFactory.getLog(ModelUtils.class);

	/**
	 * Protected Constructor
	 * 
	 * @throws AssertionError always (i.e, if called via java reflection)
	 */
	ModelUtils() {
		throw new AssertionError("Not instantiable");
	}


	public static final MessageDigest MD5_DIGEST; 

	/**
	 * Initializer.
	 */
	static {
		try {
			MD5_DIGEST = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
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


	/** 
	 * URI prefix for auto-generated utility class objects
	 * (can be useful, for consistency, during, e.g.,
	 * data convertion, normalization, merge, etc.
	 */
	public static final String BIOPAX_URI_PREFIX = "urn:biopax:";


	static
	{
		((SimpleIOHandler) io).mergeDuplicates(true);
		((SimpleIOHandler) io).normalizeNameSpaces(false);
	}


	/**
	 * Replaces BioPAX elements in the model with ones from the map,
	 * updates corresponding BioPAX object references.
	 * <p/>
	 * It does not neither remove the old nor add new elements in the model
	 * (if required, one can do this before/after this method, e.g., using
	 * the same 'subs' map)
	 * <p/>
	 * This does visit all object properties of each "explicit" element
	 * in the model, but does not traverse deeper into one's sub-properties
	 * to replace something there as well (e.g., nested member entity references
	 * are not replaced unless parent entity reference present in the model)
	 * <p/>
	 * This does not automatically move/migrate old (replaced) object's
	 * children to new objects (the replacement ones are supposed to have
	 * their own properties already set or to be set shortly; otherwise,
	 * consider using of something like {@link #fixDanglingInverseProperties(BioPAXElement, Model)} after.
	 * 
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
					BioPAXElement replacement = subs.get(range); //can get null (ok)
					
					// normal biopax property -
					if (replacement != null && !editor.getRange().isInstance(replacement))
					{
						throw new IllegalBioPAXArgumentException(
							"Incompatible type! Attempted to replace " 
							+ value.getRDFId() + " (" + value + "; "
							+ value.getModelInterface().getSimpleName() + ") with "
							+ ((replacement != null) ? replacement.getRDFId() :
							"") + " (" + replacement + "); for property: " 
							+ editor.getProperty() + " of bean: " 
							+ domain.getRDFId() + " (" + domain + "; " 
							+ domain.getModelInterface().getSimpleName() + ")");
					}

					if (replacement != value) 
					{
						editor.removeValueFromBean(value, domain);
						editor.setValueToBean(replacement, domain);
					} else {
						LOG.debug("replace: skipped the identical: " + replacement.getRDFId());
					}
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
	 * Finds "root" BioPAX objects that belong to a particular class (incl. sub-classes)
	 * in the model.
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
		final Set<T> result = new HashSet<T>(model.getObjects(filterClass));

		//"shallow" traverser (direct object properties only - Visitor.visit does not call traverse again) 
		@SuppressWarnings("unchecked")
		Traverser traverser = new Traverser(em, 
			new Visitor() {
				@Override
				public void visit(BioPAXElement parent, Object value, Model model,
					PropertyEditor<?, ?> editor) 
				{
					if (filterClass.isInstance(value)) result.remove(value);
				}
			}, 
			new Filter<PropertyEditor>() {
				@Override
				public boolean filter(PropertyEditor pe) {
					return (pe instanceof ObjectPropertyEditor);
				}
		});
		
		// but we run from every element (all types)
		for(BioPAXElement e : model.getObjects())
			traverser.traverse(e, null);

		return result;
	}

	
	/**
	 * Iteratively removes "dangling" elements of given type and its sub-types,
	 * e.g. Xref.class objects, from the BioPAX model. 
	 * 
	 * If the "model" does not contain any root Entity class objects,
	 * and the second parameter is basic UtilityClass.class (i.e., not its sub-class), 
	 * then it simply logs a warning and quits shortly (otherwise, it would 
	 * remove everything from the model). Do not use basic Entity.class either
	 * (but a sub-class is OK) for the same reason (it would delete everything).
	 * 
	 * <p/>
	 * This, however, does not change relationships
	 * among objects, particularly, some inverse properties,
	 * such as entityReferenceOf or xrefOf, may still
	 * refer to a removed object.
	 * @param model
	 * @param clazz filter-class
	 * @return removed objects
	 */
	public static <T extends BioPAXElement> Set<BioPAXElement> removeObjectsIfDangling(Model model, Class<T> clazz)
	{
		final Set<BioPAXElement> removed = new HashSet<BioPAXElement>();
		
		// 'equals' below is used intentionally (isAssignableFrom() would be incorrect)
		if(Entity.class.equals(clazz)) {
			LOG.warn("Ignored removeObjectsIfDangling call for: " +
					"Entity.class (it would delete all)");
			return removed;
		}
		if(UtilityClass.class.equals(clazz) 
				&& getRootElements(model, Entity.class).isEmpty()) 
		{
			LOG.warn("Ignored removeObjectsIfDangling call: " +
					"no root entities model; UtilityClass.class");
			return removed;
		}
		
		Set<T> dangling = getRootElements(model, clazz);	
		
		// get rid of dangling objects
		if (!dangling.isEmpty())
		{
			LOG.info(dangling.size() + " " + clazz.getSimpleName() +
				" dangling objects will be deleted...");

			for (BioPAXElement thing : dangling)
			{
				model.remove(thing);
				removed.add(thing);
				LOG.debug("removed (dangling) " + thing.getRDFId() + " (" 
					+ thing.getModelInterface().getSimpleName() + ") " + thing);
			}

			// some may have become dangling now, so check again...
			removed.addAll(removeObjectsIfDangling(model, clazz));
		}
		
		return removed;
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
			exec.execute(new Runnable() {
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
				exec.execute(new Runnable() {
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

		@SuppressWarnings("unchecked")
		final AbstractTraverser traverser = new AbstractTraverser(em)
		{
			@Override
			protected void visit(Object range, BioPAXElement domain, Model model, PropertyEditor<?,?> editor)
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
	public static Model getAllChildren(BioPAXElement bpe, 
		@SuppressWarnings("rawtypes") Filter<PropertyEditor>... filters)
	{

		Model m = factory.createModel();
		if (filters.length == 0)
		{
			new Fetcher(em, Fetcher.nextStepFilter).fetch(bpe, m);
		} else
		{
			new Fetcher(em, filters).fetch(bpe, m);
		}
		m.remove(bpe); // remove the parent

		return m;
	}

	/**
	 * Collects direct children of a given BioPAX element.
	 * 
	 * @param model
	 * @param bpe
	 * @return
	 */
	public static Set<BioPAXElement> getDirectChildrenAsSet(Model model, BioPAXElement bpe)
	{
		final Set<BioPAXElement> toReturn = new HashSet<BioPAXElement>();

		@SuppressWarnings("unchecked")
		final AbstractTraverser traverser = new AbstractTraverser(em)
		{
			@Override
			protected void visit(Object range, BioPAXElement domain, Model model, PropertyEditor<?,?> editor)
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
	 * Generates simple counts of different elements in the model.
	 * 
	 * 
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
	 * A more strict, type-safe way to ask for a biopax object
	 * from the model, unlike {@link Model#getByID(String)}.
	 * 
	 * @param model
	 * @param uri
	 * @param clazz
	 * @return the biopax object or null (if no such element, or element with this URI is of incompatible type)
	 */
	public static <T extends BioPAXElement> T getObject(Model model, String uri, Class<T> clazz)
	{
		BioPAXElement bpe = model.getByID(uri);
		if (clazz.isInstance(bpe))
		{
			return (T) bpe;
		} else
		{
			return null;
		}
	}


	/**
	 * Calculates MD5 hash code (as 32-byte hex. string).
	 * 
	 * This method is not BioPAX specific. Can be
	 * used for many purposes, such as generating 
	 * new unique URIs, database primary keys, etc.
	 * 
	 * 
	 * @param id
	 * @return the 32-byte digest string
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
					if (value != null && !model.containsID(value.getRDFId())) 
						editor.removeValueFromBean(value, domain);
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
	 * TODO annotate
	 * 
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
	 * TODO annotate
	 * 
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
	 * TODO annotate
	 * 
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
	 * TODO annotate
	 * 
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
	 * TODO annotate
	 * 
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


	/**
	 * Converts generic simple physical entities, 
	 * i.e., physical entities except Complexes 
	 * that have not empty memberPhysicalEntity property,
	 * into equivalent physical entities
	 * with generic entity references (which have members);
	 * this is a better and less error prone way to model
	 * generic molecules in BioPAX L3. 
	 * 
	 * Notes:
	 * Generic Complexes could be normalized in a similar way,
	 * but they do not have entityReference property and might
	 * contain generic (incl. not yet normalized) components, which
	 * makes it complicated.
	 * 
	 * Please avoid using 'memberPhysicalEntity' in your BioPAX L3 models
	 * unless absolutely sure/required, for there is an alternative way 
	 * (using PhysicalEntity/entityReference/memberEntityReference), and 
	 * this will probably be deprecated in the future BioPAX releases.
	 * 
	 * @param model
	 */
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
		Set<EntityReference> members = pe.getGenericEntityReferences();
		EntityReference er = memberMap.get(members);
		if (er == null)
		{
			EntityReference firstEntityReference = first.getEntityReference();
			if (firstEntityReference != null)
			{
				//generate a new URI in the same namespace (xml:base)
				String syntheticId = model.getXmlBase() + md5hex(pe.getRDFId()); 
				// create and add a new EntityReference
				er = (EntityReference) model.addNew(firstEntityReference.getModelInterface(), syntheticId);
				// copy names and xrefs (making orig. unif.xrefs become relat.xrefs)
				copySimplePointers(model, pe, er);
				
				er.addComment("auto-generated by Paxtools from generic " 
						+ pe.getModelInterface().getSimpleName()
						+ ", uri=" + pe.getRDFId() + "");

				for (EntityReference member : members)
				{
					er.addMemberEntityReference(member);
				}
				
				memberMap.put(members, er);
			}
		}
		pe.setEntityReference(er);
	}

	
	/**
	 * Copies names and xrefs from source to target 
	 * biopax object; it does not copy unification xrefs 
	 * but instead adds relationship xrefs using the same 
	 * db and id values as source's unification xrefs.
	 * 
	 * @param model
	 * @param source
	 * @param target
	 */
	public static void copySimplePointers(Model model, Named source, Named target)
	{
		target.setDisplayName(source.getDisplayName());
		target.setStandardName(source.getStandardName());
		for (String name : source.getName())
		{
			target.addName(name);
		}
		for (Xref xref : source.getXref())
		{
			if ((xref instanceof UnificationXref))
			{
				// generate URI using model's xml:base and xref's properties
				String id = model.getXmlBase() + md5hex(xref.getDb()+xref.getRDFId());
				Xref byID = (Xref) model.getByID(id);
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
					xref = byID;
				}
			}
			
			target.addXref(xref);
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
				if (!SetEquivalenceChecker.hasEquivalentIntersection(complex.getComponent(), featureOf))
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

		EquivalenceGrouper<EntityFeature> equivalents = new EquivalenceGrouper<EntityFeature>();
		HashMap<EntityFeature, EntityFeature> mapped = new HashMap<EntityFeature, EntityFeature>();
		HashSet<EntityFeature> scheduled = new HashSet<EntityFeature>();

		for (EntityFeature ef : model.getObjects(EntityFeature.class))
		{
			if (ef.getEntityFeatureOf() == null)
			{
				inferEntityFromPE(ef, ef.getFeatureOf());
				if (ef.getEntityFeatureOf() == null) inferEntityFromPE(ef, ef.getNotFeatureOf());
			}
			equivalents.add(ef);
		}
		for (List<EntityFeature> bucket : equivalents.getBuckets())
		{
			for (int i = 1; i < bucket.size(); i++)
			{
				EntityFeature ef = bucket.get(i);
				if (LOG.isWarnEnabled())
				{
					LOG.warn("removing: "+ ef.getRDFId()+ " since it is equivalent to: "+ bucket.get(0));
				}
				scheduled.add(ef);
			}
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
					LOG.debug(" replacing " + feature +
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
					LOG.debug("Inferred the ER of " + ef.getRDFId() + " as " + er.getRDFId());
					return;
				}
			}
		}
	}


	/**
	 * Collects all data type (not object) property
	 * values (can be then used for full-text indexing).
	 * 
	 * @param biopaxElement
	 * @param depth 0 means use this objects' 
	 *        data properties only, 1 - add child's data properties, etc.
	 * @return
	 */
	public static Set<String> getKeywords(BioPAXElement biopaxElement, int depth) {
		
		LOG.debug("getKeywords called: " + biopaxElement.getRDFId());
		
		EditorMap em = SimpleEditorMap.L3;
		Set<String> ss = new HashSet<String>();
		
		Set<BioPAXElement> elms = new Fetcher(em, Fetcher.nextStepFilter)
			.fetch(biopaxElement, depth);
		//add this one too
		elms.add(biopaxElement);
		
		for (BioPAXElement bpe : elms) {
			Set<PropertyEditor> props = em.getEditorsOf(bpe);
			for (PropertyEditor pe : props) {
				if (pe instanceof ObjectPropertyEditor)
					continue; //skip object props
				
				Set values = pe.getValueFromBean(bpe);
				for (Object v : values) {
					if (!pe.isUnknown(v)) {
						ss.add(v.toString());
					}
				}
			}
		}
		
		return ss;
	}
	
	
	/**
	 * Collects BioSource objects from this or
	 * related elements (where it makes sense;
	 * though the biopax element might have no 
	 * or empty 'organism' property at all.
	 * 
	 * The idea is to additionally associate with 
	 * existing BioSource objects, and thus make 
	 * filtering by organism possible, for at least 
	 * Interaction, Protein, Complex, Dna, etc. 
	 * biopax entities.
 	 * 
	 * 
	 * @param biopaxElement
	 * @return
	 */
	public static Set<BioSource> getOrganisms(BioPAXElement biopaxElement) {		
		final Set<BioSource> biosources = new HashSet<BioSource>();
		//shortcut
		if(biopaxElement == null)
			return biosources;
		
		LOG.debug("getOrganisms called: " + biopaxElement.getRDFId());	
				
		if(biopaxElement instanceof BioSource) {
			biosources.add((BioSource) biopaxElement);			
		} else if (biopaxElement instanceof Pathway) {			
			if(((Pathway)biopaxElement).getOrganism() != null)
				biosources.add(((Pathway)biopaxElement).getOrganism());
//			else 
//				//if not set, - infer from children (expensive)
//				biosources.addAll((new Fetcher(em, Fetcher.nextStepFilter))
//					.fetch(biopaxElement, BioSource.class));
			
		} else if (biopaxElement instanceof Gene) {	
			if(((Gene)biopaxElement).getOrganism() != null)
				biosources.add(((Gene) biopaxElement).getOrganism());
		} else if (biopaxElement instanceof PathwayStep) {
			Pathway pw = ((PathwayStep) biopaxElement).getPathwayOrderOf();
			if(pw != null && pw.getOrganism() != null)
				biosources.add(pw.getOrganism());
		} else if (biopaxElement instanceof Interaction 
				|| biopaxElement instanceof EntityReference
				|| biopaxElement instanceof PhysicalEntity) {
			
			if (biopaxElement instanceof SequenceEntityReference)
				biosources.add(((SequenceEntityReference) biopaxElement).getOrganism());
			
			//get from children (members, participants, components, etc.)
			biosources.addAll((new Fetcher(em, Fetcher.nextStepFilter))
				.fetch(biopaxElement, BioSource.class));			
		} 
		
		return biosources;
	}


	/**
	 * Collects all Provenance objects 
	 * associated with this one as follows:
	 * - if the element is Entity (has 'dataSource' property) 
	 *   or is Provenence itself, get the values and quit;
	 * - if the biopax element is PathwayStep or EntityReference, 
	 *   traverse into some of its object/inverse properties to collect 
	 *   dataSource values from associated entities.
	 * - return empty set for all other BioPAX types (it is less important 
	 *   to associate common self-descriptive biopax utility classes with 
	 *   particular pathway data sources)
	 * 
	 * @param biopaxElement
	 * @return
	 */
	public static Set<Provenance> getDatasources(BioPAXElement biopaxElement) {
		
		final Set<Provenance> datasources = new HashSet<Provenance>();
		
		//shortcut
		if(biopaxElement == null)
			return datasources;

		LOG.debug("getDatasources called: " + biopaxElement.getRDFId());
		
		if (biopaxElement instanceof Provenance) {			
			datasources.add((Provenance) biopaxElement);			
		} else if (biopaxElement instanceof Entity) {			
			datasources.addAll(((Entity) biopaxElement).getDataSource());			
		} else if (biopaxElement instanceof EntityReference) {
			// Let ERs inherit its dataSource from parent PEs or ERs:			
			for(SimplePhysicalEntity spe : ((EntityReference) biopaxElement).getEntityReferenceOf())
				datasources.addAll(getDatasources(spe));			
			for(EntityReference er : ((EntityReference) biopaxElement).getMemberEntityReferenceOf())
				datasources.addAll(getDatasources(er));			
		} else if (biopaxElement instanceof PathwayStep) {			
			datasources.addAll(getDatasources(((PathwayStep) biopaxElement).getPathwayOrderOf()));				
		} else {
			// ignore
		}
				
		return datasources;
	}

	
	/**
	 * Collects parent Pathway objects recursively
	 * traversing inverse object properties of the
	 * biopax element.
	 * 
	 * @param biopaxElement
	 * @return
	 */
	public static Set<Pathway> getParentPathways(BioPAXElement biopaxElement) {
		
		final Set<Pathway> pathways = new HashSet<Pathway>();
		
		//shortcut
		if(biopaxElement == null)
			return pathways;
		
		LOG.debug("getParentPathways called: " + biopaxElement.getRDFId());
				
		if(biopaxElement instanceof Process) {
			if(biopaxElement instanceof Pathway) // add itself
				pathways.add((Pathway) biopaxElement);
			// continue looking up to parent pathways (until all top ones reached)
			for(Pathway pw : ((Process)biopaxElement).getPathwayComponentOf())
				pathways.addAll(getParentPathways(pw));
			for(Interaction it : ((Process)biopaxElement).getParticipantOf())
				pathways.addAll(getParentPathways(it));
			for(PathwayStep pt : ((Process)biopaxElement).getStepProcessOf())
				pathways.addAll(getParentPathways(pt));
		} else if(biopaxElement instanceof PathwayStep) {
			pathways.addAll(getParentPathways(((PathwayStep)biopaxElement).getPathwayOrderOf()));
		} else if(biopaxElement instanceof PhysicalEntity ) {
			for(PhysicalEntity pe : ((PhysicalEntity)biopaxElement).getMemberPhysicalEntityOf())
				pathways.addAll(getParentPathways(pe));
			for(Interaction it : ((Entity)biopaxElement).getParticipantOf())
				pathways.addAll(getParentPathways(it));
			for(Complex c : ((PhysicalEntity)biopaxElement).getComponentOf())
				pathways.addAll(getParentPathways(c));
		} else if(biopaxElement instanceof EntityReference) {
			for(EntityReference er : ((EntityReference) biopaxElement).getMemberEntityReferenceOf())
				pathways.addAll(getParentPathways(er));
			for(SimplePhysicalEntity spe : ((EntityReference) biopaxElement).getEntityReferenceOf())
				pathways.addAll(getParentPathways(spe));
		} else if (biopaxElement instanceof Gene ) { 
			for(Interaction it : ((Entity) biopaxElement).getParticipantOf())
				pathways.addAll(getParentPathways(it));
		} else {
			// ignore
		}
		
		return pathways;
	}

}



