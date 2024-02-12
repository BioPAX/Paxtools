package org.biopax.paxtools.controller;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Several useful algorithms and examples, e.g., to extract root or child
 * BioPAX L3 elements, remove dangling, replace elements
 * or URIs, fix/infer property values, etc.
 * 
 * NOTE: despite it is public class and has public methods,
 * this class can be (and has been already) modified (sometimes considerably) 
 * in every minor revision; it was not designed to be Paxtools' public API...
 * So, we encourage users copy some methods to their own apps rather than 
 * depend on this unstable utility class in long term.
 * 
 * @author rodche, Arman, Emek
 */
public final class ModelUtils
{
	private static final Logger LOG = LoggerFactory.getLogger(ModelUtils.class);

	/**
	 * Protected Constructor
	 * 
	 * @throws AssertionError always (i.e, if called via java reflection)
	 */
	ModelUtils() {
		throw new AssertionError("Not instantiable");
	}

	static final MessageDigest MD5_DIGEST;

	private final static BioPAXFactory factory = BioPAXLevel.L3.getDefaultFactory();
	private final static EditorMap em = SimpleEditorMap.L3;
	private final static SimpleIOHandler io = new SimpleIOHandler(BioPAXLevel.L3);


	/**
	 * Initializer.
	 */
	static {
		try {
			MD5_DIGEST = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Cannot instantiate MD5 MessageDigest!", e);
		}
		io.mergeDuplicates(true);
		io.normalizeNameSpaces(false);
	}


	/**
	 * Replaces BioPAX elements in the model with ones from the map,
	 * updates corresponding BioPAX object references.
	 * 
	 * It does not neither remove the old nor add new elements in the model
	 * (if required, one can do this before/after this method, e.g., using
	 * the same 'subs' map)
	 * 
	 * This does visit all object properties of each "explicit" element
	 * in the model, but does not traverse deeper into one's sub-properties
	 * to replace something there as well (e.g., nested member entity references
	 * are not replaced unless parent entity reference present in the model)
	 * 
	 * This does not automatically move/migrate old (replaced) object's
	 * children to new objects (the replacement ones are supposed to have
	 * their own properties already set or to be set shortly; otherwise,
	 * consider using of something like {@link #fixDanglingInverseProperties(BioPAXElement, Model)} after.
	 * 
	 * @param model biopax model where the objects are to be replaced
	 * @param subs the replacements map (many-to-one, old-to-new)
	 * @exception IllegalBioPAXArgumentException if there is an incompatible type replacement object
	 */
	public static void replace(Model model, final Map<? extends BioPAXElement, ? extends BioPAXElement> subs)
	{
		if(subs == null || subs.isEmpty()) {
			return;
		}

		// update properties
		Visitor visitor = (domain, range, bpModel, propertyEditor) -> {
			if (propertyEditor instanceof ObjectPropertyEditor && range != null && subs.containsKey(range))
			{
				ObjectPropertyEditor editor = (ObjectPropertyEditor) propertyEditor;
				BioPAXElement value = (BioPAXElement) range;
				// 'value' is to be replaced with the 'replacement'
				BioPAXElement replacement = subs.get(range); //can get null (ok)

				// normal biopax property -
				if (replacement != null && !editor.getRange().isInstance(replacement))
				{
					throw new IllegalBioPAXArgumentException(
						"Incompatible type! Attempted to replace "
						+ value.getUri() + " (" + value.getModelInterface().getSimpleName()
						+ ") with " + replacement.getUri() + " ("
						+ replacement.getModelInterface().getSimpleName() + "); "
						+ "property: " + editor.getProperty()
						+ " of bean: " + domain.getUri() + " ("
						+ domain.getModelInterface().getSimpleName() + ")");
				}

				if (replacement != value)
				{
					editor.removeValueFromBean(value, domain);
					editor.setValueToBean(replacement, domain);
				} else {
					LOG.debug("replace: skipped the identical: " + replacement.getUri());
				}
			}
		};

		Traverser traverser = new Traverser(em, visitor);
		model.getObjects().stream().forEach(bpe -> traverser.traverse(bpe, null));//model is not used
	}


	/**
	 * Finds "root" BioPAX objects that belong to a particular class (incl. sub-classes)
	 * in the model.
	 * 
	 * Note: however, such "root" elements may or may not be, a property of other
	 * elements, not included in the model.
	 * @param model biopax model to work with
	 * @param filterClass filter class (including subclasses)
	 * @param <T> biopax type
	 * @return set of the root biopax objects of given type
	 */
	public static <T extends BioPAXElement> Set<T> getRootElements(final Model model, final Class<T> filterClass)
	{
		// copy all such elements (initially, we think all are roots...)
		final Set<T> result = new HashSet<>(model.getObjects(filterClass));

		//define a "shallow" (non-recursive) object property traverser
		Traverser traverser = new Traverser(
			em, // editor map
			(bpe, value, m, e) -> { // visitor.visit
				if (filterClass.isInstance(value)) result.remove(value);
			},
			pe -> (pe instanceof ObjectPropertyEditor) // filter
		);

		model.getObjects().stream().forEach(e -> traverser.traverse(e, null));

		return result;
	}

	
	/**
	 * Iteratively removes "dangling" elements of given type and its subtypes,
	 * e.g. Xref.class objects, from the BioPAX model. 
	 * 
	 * If the "model" does not contain any root Entity class objects,
	 * and the second parameter is basic UtilityClass.class (i.e., not its sub-class), 
	 * then it simply logs a warning and quits shortly (otherwise, it would 
	 * remove everything from the model). Do not use basic Entity.class either
	 * (but a subclass is OK) for the same reason (it would delete everything).
	 * 
	 * This, however, does not change relationships
	 * among objects, particularly, some inverse properties,
	 * such as entityReferenceOf or xrefOf, may still
	 * refer to a removed object.
	 * @param model to modify
	 * @param clazz filter-class (filter by this type and subclasses)
	 * @param <T> biopax type
	 * @return removed objects
	 */
	public static <T extends BioPAXElement> Set<BioPAXElement> removeObjectsIfDangling(Model model, Class<T> clazz)
	{
		final Set<BioPAXElement> removed = new HashSet<>();
		
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
				LOG.debug("removed (dangling) " + thing.getUri() + " ("
					+ thing.getModelInterface().getSimpleName() + ") " + thing);
			}

			// some may have become dangling now, so check again...
			removed.addAll(removeObjectsIfDangling(model, clazz));
		}
		
		return removed;
	}


	/**
	 * Cuts the BioPAX model off other models and BioPAX objects
	 * by essentially performing write/read to/from OWL.
	 * The resulting model contains new objects with same IDs
	 * and have object properties "fixed", i.e., dangling values
	 * become null/empty, and inverse properties (e.g. xrefOf)
	 * re-calculated. The original model is unchanged.
	 * 
	 * Note: this method will fail for very large models 
	 * (if resulting RDF/XML utf8 string is longer than approx. 1Gb)
	 * 
	 * @param model biopax model to process
	 * @return copy of the model
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
	 * @param bpe biopax element/object
	 * @return new model
	 */
	public static Model getDirectChildren(BioPAXElement bpe)
	{
		Model m = factory.createModel();

		Traverser traverser = new Traverser(em, (domain, range, model, editor) -> {
			if (range instanceof BioPAXElement && !model.containsID(((BioPAXElement) range).getUri()))
				model.add((BioPAXElement) range);
		});

		traverser.traverse(bpe, m);

		return m;
	}


	/**
	 * Gets all the child BioPAX elements of a given BioPAX element
	 * (using the "tuned" {@link Fetcher}) and adds them to a
	 * new model.
	 * @param bpe biopax object
	 * @param filters property filters (e.g., for Fetcher to skip some properties). Default is to skip 'nextStep'.
	 * @return new biopax Model that contain all the child objects
	 *
	 * @deprecated use {@link Fetcher#fetch(BioPAXElement, Model)} instead (with Fetcher.nextStepFilter or without)
	 */
	@Deprecated
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
	 * @param bpe biopax object (parent)
	 * @return set of child biopax objects
	 */
	public static Set<BioPAXElement> getDirectChildrenAsSet(BioPAXElement bpe)
	{
		final Set<BioPAXElement> toReturn = new HashSet<>();

		Traverser traverser = new Traverser(em,
			(domain, range, model, editor) -> { // Visitor impl.
				if (range instanceof BioPAXElement) {
					toReturn.add((BioPAXElement) range);
				}
			}
		);

		traverser.traverse(bpe, null);

		return toReturn;
	}
	
	
	/**
	 * Generates simple counts of different elements in the model.
	 * 
	 * @param model biopax model to analyze
	 * @return a biopax types - to counts of objects of each type map
	 */
	public static Map<Class<? extends BioPAXElement>, Integer> generateClassMetrics(Model model)
	{
		Map<Class<? extends BioPAXElement>, Integer> metrics = new HashMap<>();
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
	 * @param model biopax model to query
	 * @param uri absolute URI of a biopax element
	 * @param clazz class-filter (to filter by the biopax type and its sub-types)
	 * @param <T> biopax type
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
	 * @param id some identifier, e.g., URI
	 * @return the 32-byte digest string
	 */
	public static String md5hex(String id)
	{
		byte[] digest = MD5_DIGEST.digest(id.getBytes());
		StringBuffer sb = new StringBuffer();
		for (byte b : digest)
		{
			sb.append(Integer.toHexString((b & 0xff) | 0x100), 1, 3);
		}
		String hex = sb.toString();
		return hex;
	}


	/**
	 * Unlinks <em>object properties</em> of the BioPAX object
	 * from values the model does not have.
	 * 
	 * @param bpe a biopax object
	 * @param model the model to look for objects in
	 */
	public static void fixDanglingObjectProperties(BioPAXElement bpe, Model model)
	{
		Visitor visitor = (domain, range, m, editor) -> {
			if (editor instanceof ObjectPropertyEditor)
			{
				BioPAXElement value = (BioPAXElement) range;
				if (value != null && !m.containsID(value.getUri()))
					((ObjectPropertyEditor)editor).removeValueFromBean(value, domain);
			}
		};
		Traverser traverser = new Traverser(em, visitor);
		traverser.traverse(bpe, model);
	}


	/**
	 * Unlinks <em>inverse properties</em> of the BioPAX object
	 * from values the model does not have.
	 * @param bpe BioPAX object
	 * @param model where to look for other objects
	 */
	public static void fixDanglingInverseProperties(BioPAXElement bpe, Model model)
	{
		final Visitor visitor = (domain, range, m, editor) -> {
			BioPAXElement value = (BioPAXElement) range;
			if (value != null && !m.containsID(value.getUri()))
				((ObjectPropertyEditor)editor).removeValueFromBean(domain, value); //in this order!
		};

		TraverserBilinked traverser = new TraverserBilinked(em, visitor);
		traverser.setInverseOnly(true); //do only inverse properties (e.g. "entityReferenceOf", object property editors)
		traverser.traverse(bpe, model);
	}


	// Moved from FeatureUtils; provides operations for comparing features of physical entities.

	enum FeatureType
	{
		FEATURE,
		NOT_FEATURE,
		UNKNOWN_FEATURE;
	}

	/**
	 * Gets the entity features that both entities have in common, taking the feature type into account.
	 * @param physicalEntity1 first entity
	 * @param featureType1 first feature type (feature/not-feature/unknown)
	 * @param physicalEntity2 second entity
	 * @param featureType2 second feature type
	 * @return the set of entity features
	 */
	public static Set<EntityFeature> getFeatureIntersection(PhysicalEntity physicalEntity1, FeatureType featureType1,
			PhysicalEntity physicalEntity2, FeatureType featureType2)
	{
		Set<EntityFeature> intersection = getFeatureSetByType(physicalEntity1, featureType1);
		intersection.removeAll(getFeatureSetByType(physicalEntity2, featureType2));
		return intersection;
	}

	/**
	 * Get entity's features by type.
	 *
	 * @param pe
	 * @param type
	 * @return
	 */
	public static Set<EntityFeature> getFeatureSetByType(PhysicalEntity pe, FeatureType type)
	{
		Set<EntityFeature> modifiableSet = new HashSet<>();
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
				if (pe instanceof SimplePhysicalEntity) {
					EntityReference er = ((SimplePhysicalEntity) pe).getEntityReference();
					if(er != null) {
						modifiableSet.addAll(er.getEntityFeature());
						modifiableSet.removeAll(pe.getFeature());
						modifiableSet.removeAll(pe.getNotFeature());
					}
				}
				break;
			}
		}
		return modifiableSet;
	}


	/**
	 * Finds and adds all (missing) entity features 
	 * to given entity reference from all its owner 
	 * simple physical entities ('feature' and 'notFeature' 
	 * properties).
	 * 
	 * Though, it neither checks for nor resolves any violations 
	 * of the 'entityFeature' property's inverse functional constraint
	 * (i.e., an EntityFeature instance can only belong to one and only one
	 * EntityReference object).  
	 * 
	 * @param er entity reference object
	 * @param fix flag
	 * @return true or false
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

	private static boolean scanAndAddToFeatureSet(EntityReference er, boolean fix, boolean check, EntityFeature ef)
	{
		if (!er.getEntityFeature().contains(ef))
		{
			check = false;
			if (fix)
			{
				er.addEntityFeature(ef);
				//TODO: fix inverse functional constraint violation: copy the "ef" if entityFeatureOf is not null!
			}
		}
		return check;
	}

	// TODO annotate
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
			LOG.warn("These two PhysicalEntities do not share an EntityReference. They can not be compared! " +
					"Skipping");
			return false;
		}

	}

	/**
	 * Property "controlled" (OWL Functional prop!) should not accept multiple values, but it unfortunately does.
	 *
	 * Let's fix/copy Controls to make sure every one has at most one controlled process, not many.
	 *
	 * @param model biopax model
	 * @param control to be cloned to set one controlled per control
	 */
	public static void fixControlled(Model model, Control control) {
		//TODO: implement
	}

	/**
	 * Converts each generic simple (except a Complex) physical entity having memberPhysicalEntity property set
	 * into equivalent physical entity with a generic entity reference (have memberEntityReference values).
	 *
	 * Complexes cannot be normalized in the same way, for they do not have entityReference property and might also
	 * contain generic components.
	 * 
	 * In general, avoid using 'memberPhysicalEntity' (made exclusively for Reactome) in BioPAX models,
	 * for there is a better alternative  - using entityReference/memberEntityReference.
	 *
	 * @param model biopax model to fix
	 */
	public static void normalizeGenerics(Model model)
	{
		final Set<SimplePhysicalEntity> simplePEsToDo = new HashSet<>();

		for (SimplePhysicalEntity spe : model.getObjects(SimplePhysicalEntity.class)) {
			if (spe.getEntityReference() == null && !spe.getMemberPhysicalEntity().isEmpty()) {
				simplePEsToDo.add(spe);
			}
		}

		final Map<Set<EntityReference>,EntityReference> memberMap = new HashMap<>();

		for (SimplePhysicalEntity pe : simplePEsToDo)
		{
			try {
				createGenericEntityRef(model, pe, memberMap);
			}
			catch (Exception e) {
				LOG.error("createGenericEntityRef failed at " + pe.getUri(), e);
			}
		}
		//TODO: unlink and remove all pe.memberPhysicalEntity for each pe in simplePEs?
	}
	
	private static void createGenericEntityRef(Model model, SimplePhysicalEntity spe,
			final Map<Set<EntityReference>, EntityReference> memberMap)
	{
		Set<EntityReference> members = spe.getGenericEntityReferences();
		EntityReference er = memberMap.get(members);
		if (er == null)
		{
			//find an ER, if any...
			EntityReference firstEntityReference = spe.getEntityReference(); //null, but worth double-checking
			if(firstEntityReference == null) {
				for (PhysicalEntity pe : spe.getMemberPhysicalEntity()) {
					//safe to cast to SimplePhysicalEntity, thanks to the memberPE property range restriction
					firstEntityReference = ((SimplePhysicalEntity) pe).getEntityReference();
					if (firstEntityReference != null)
						break; //got one!
				}
			} else {
				return; //spe already has ER
				//TODO: shall we add members (spe.getGenericEntityReferences()) to firstEntityReference?
			}

			if (firstEntityReference != null)
			{
				//generate a new URI in the same namespace (xml:base) and create and add a new EntityReference
				String syntheticId = model.getXmlBase() +
						firstEntityReference.getModelInterface().getSimpleName() + "_" + md5hex(spe.getUri());
				er = (EntityReference) model.addNew(firstEntityReference.getModelInterface(), syntheticId);
				er.addComment("auto-generated generic entity reference");

				// copy names and xrefs (making orig. unif.xrefs become relat.xrefs)
				copySimplePointers(model, spe, er);

				//remove unification xrefs from the pe
				for(UnificationXref ux : new HashSet<>(new ClassFilterSet<>(spe.getXref(), UnificationXref.class)))
				{
					spe.removeXref(ux);
				}

				for (EntityReference member : members) {
					er.addMemberEntityReference(member);
				}
				
				memberMap.put(members, er);
			} else {
				LOG.warn(String.format("Cannot generate a generic ER for generic SPE %s, " +
								"for none of member PEs has got an ER.", spe.getUri()));
			}
		}

		spe.setEntityReference(er);
	}

	/**
	 * For a non-generic simple physical entity (memberPhysicalEntity property is empty)
	 * that does not have entityReference property defined, this method generates and adds
	 * a new entity reference of proper type to both this entity and the model,
	 * and also copies names and xrefs from the source physical entity to the generated entity reference
	 * (UnificationXrefs are converted to RelationshipXref and then also deleted from the original entity.)
	 *
	 * @param model the BioPAX model
	 * @param pe a simple physical entity (that has neither entityReference nor memberPEs set)
	 */
	public static void addMissingEntityReference(Model model, SimplePhysicalEntity pe)
	{
		if (!(pe.getEntityReference()==null && pe.getMemberPhysicalEntity().isEmpty()))
			return; //nothing to do.
		//continue for a simple PE

		// use a specific EntityReference subclass depending on the PE class:
		Class<? extends EntityReference> type = null;
		if(pe instanceof Protein)
			type = ProteinReference.class;
		else if(pe instanceof SmallMolecule)
			type = SmallMoleculeReference.class;
		else if (pe instanceof Dna)
			type = DnaReference.class;
		else if (pe instanceof DnaRegion)
			type = DnaRegionReference.class;
		else if (pe instanceof Rna)
			type = RnaReference.class;
		else if (pe instanceof RnaRegion)
			type = RnaRegionReference.class;
		else {} //impossible SimplePhysicalEntity subtype

		//generate a new URI in the same namespace (xml:base)
		String syntheticId = model.getXmlBase() + type.getSimpleName() + "_"+ md5hex(pe.getUri());
		//create a new ER object using the auto-detected above type:
		EntityReference er = model.addNew(type, syntheticId);

		// copy names and xrefs (making orig. UnificationXrefs become RelationshipXrefs)
		copySimplePointers(model, pe, er);

		//remove unification xrefs from the original pe
		for(UnificationXref ux : new HashSet<>(new ClassFilterSet<>(pe.getXref(),UnificationXref.class)))
			pe.removeXref(ux);

		pe.setEntityReference(er);
	}

	
	/**
	 * Copies names, xrefs, comments, evidence, data sources from source to target biopax object;
	 * it does not copy unification xrefs but instead adds relationship xrefs using the same
	 * db and id values as source's unification xrefs.
	 * 
	 * @param model the biopax model where the source and target objects belong
	 * @param source from
	 * @param target to
	 */
	private static void copySimplePointers(Model model, Named source, Named target)
	{
		//copy names
		if(source.getDisplayName()!=null)
			target.setDisplayName(source.getDisplayName());
		if(source.getStandardName()!=null)
			target.setStandardName(source.getStandardName());
		for (String name : source.getName()) {
			target.addName(name);
		}

		// copy xrefs, converting UXs to RXs on the go
		for (Xref xref : source.getXref())
		{
			if (xref instanceof UnificationXref)
			{
				// generate URI using model's xml:base and xref's properties
				String uri = model.getXmlBase() + "RelationshipXref_"+ md5hex(xref.getDb()+xref.getId());
				Xref byID = (Xref) model.getByID(uri);
				if (byID == null)
				{
					RelationshipXref rref = model.addNew(RelationshipXref.class, uri);
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

		//copy evidence and dataSource if (Named) source and target are same subtype - either Entity or ER only:
		if(source instanceof Entity && target instanceof Entity) {
			Entity src = (Entity) source;
			for (Evidence ev : src.getEvidence()) {
				((Entity) target).addEvidence(ev);
			}
			for (Provenance prov : src.getDataSource()) {
				((Entity) target).addDataSource(prov);
			}
			// copy comments
			for(String comm : source.getComment()) {
				target.addComment(comm);
			}
		} else if(source instanceof EntityReference && target instanceof EntityReference) {
			EntityReference src = (EntityReference) source;
			for (Evidence ev : src.getEvidence()) {
				((EntityReference) target).addEvidence(ev);
			}
			// copy comments
			for(String comm : source.getComment()) {
				target.addComment(comm);
			}
		}
	}


	/**
	 * This method iterates over the features in a model and tries to find equivalent objects and merges them.
	 * @param model to be fixed
	 */
	public static void replaceEquivalentFeatures(Model model)
	{

		EquivalenceGrouper<EntityFeature> equivalents = new EquivalenceGrouper<>();
		HashMap<EntityFeature, EntityFeature> mapped = new HashMap<>();
		HashSet<EntityFeature> scheduled = new HashSet<>();

		for (EntityFeature ef : model.getObjects(EntityFeature.class))
		{
			if (ef.getEntityFeatureOf() == null)
				inferEntityReference(ef);

			equivalents.add(ef);
		}
		for (List<EntityFeature> bucket : equivalents.getBuckets())
		{
			for (int i = 1; i < bucket.size(); i++)
			{
				EntityFeature ef = bucket.get(i);
				if (LOG.isWarnEnabled())
				{
					LOG.warn("removing: "+ ef.getUri()+ " since it is equivalent to: "+ bucket.get(0));
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
			Set<EntityFeature> features = new HashSet<>(physicalEntity.getFeature());
			for (EntityFeature feature : features)
			{
				EntityFeature that = mapped.get(feature);
				if (that != null && !that.equals(feature))
				{
					LOG.debug(" replacing " + feature +
					                                  "{" + feature.getUri() + "} with " +
					                                  that + "{" + that.getUri() + "}");
					physicalEntity.removeFeature(feature);
					physicalEntity.addFeature(that);
				}
			}
		}
	}


	private static void inferEntityReference(EntityFeature ef)
	{
		if(ef.getEntityFeatureOf() != null)
			return; //shortcut

		for (PhysicalEntity physicalEntity : ef.getFeatureOf()) {
			if (physicalEntity instanceof SimplePhysicalEntity) {
				EntityReference er = ((SimplePhysicalEntity) physicalEntity).getEntityReference();
				if (er != null) {
					er.addEntityFeature(ef);
					break;
				}
			}
		}

		// when no ER is found, try notFeatureOf
		if (ef.getEntityFeatureOf() == null) {
			for (PhysicalEntity physicalEntity : ef.getNotFeatureOf()) {
				if (physicalEntity instanceof SimplePhysicalEntity) {
					EntityReference er = ((SimplePhysicalEntity) physicalEntity).getEntityReference();
					if (er != null) {
						er.addEntityFeature(ef);
						break;
					}
				}
			}
		}
	}


	/**
	 * Collects data type (not object) property
	 * values (can be then used for full-text indexing).
	 * 
	 * @param biopaxElement biopax object
	 * @param depth greater or equals 0: 0 means use this object's
	 *        data properties only; 1 - add child's data properties, etc.;
	 *        (the meaning is slightly different from that of Fetcher.fetch(..) method)
	 * @param dataPropertyFilters - biopax data property filters to optionally
	 *                        either skip e.g. properties 'sequence', 'temperature',
	 *                        or only accept 'term', 'comment', 'name', etc.
	 * @return set of keywords
	 */
	public static Set<String> getKeywords(BioPAXElement biopaxElement, int depth,
										  Filter<DataPropertyEditor>... dataPropertyFilters)
	{
		LOG.debug("getKeywords called: " + biopaxElement.getUri());
		
		EditorMap em = SimpleEditorMap.L3;
		Set<String> ss = new HashSet<>();

		//if depth>0, fetch child biopax objects (ignoring PathwayStep.nextStep property)
		Set<BioPAXElement> elms = (depth > 0)
			? new Fetcher(em, Fetcher.nextStepFilter).fetch(biopaxElement, depth)
				: new HashSet<>();

		//add this one
		elms.add(biopaxElement);
		
		for (BioPAXElement bpe : elms) {
			Set<PropertyEditor> props = em.getEditorsOf(bpe);
			for (PropertyEditor pe : props) {
				//skip for object prop. or one that fails to pass a filter
				if (pe instanceof ObjectPropertyEditor
						|| !filter((DataPropertyEditor)pe, dataPropertyFilters))
					continue;

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


	private static <T extends PropertyEditor> boolean filter(T pe, Filter<T>... propertyFilters) {
		if(propertyFilters.length==0)
			return true;

		for(Filter<T> pf : propertyFilters) {
			if (!pf.filter(pe)) {
				return false;
			}
		}

		return true;
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
	 * @param biopaxElement biopax object
	 * @return organism names
	 */
	public static Set<BioSource> getOrganisms(BioPAXElement biopaxElement) {		
		final Set<BioSource> biosources = new HashSet<>();
		//shortcut
		if(biopaxElement == null)
			return biosources;
		
		LOG.debug("getOrganisms called: " + biopaxElement.getUri());
				
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
			
			if (biopaxElement instanceof SequenceEntityReference) {
				if(((SequenceEntityReference) biopaxElement).getOrganism() != null)
					biosources.add(((SequenceEntityReference) biopaxElement).getOrganism());
			}
			
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
	 * @param biopaxElement a biopax object
	 * @return Provenance objects set
	 */
	public static Set<Provenance> getDatasources(BioPAXElement biopaxElement) {
		
		final Set<Provenance> datasources = new HashSet<>();
		
		//shortcut
		if(biopaxElement == null)
			return datasources;

		LOG.debug("getDatasources called: " + biopaxElement.getUri());
		
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
	 * Collects all parent Pathway objects recursively
	 * traversing the inverse object properties of the
	 * biopax element. It ignores all BioPAX types except (incl. sub-classes of):
	 * Pathway, Interaction, PathwayStep, PhysicalEntity, EntityReference, and Gene.
	 * 
	 * @param biopaxElement biopax object
	 * @return inferred parent pathways
	 */
	public static Set<Pathway> getParentPathways(BioPAXElement biopaxElement) {
		final Set<BioPAXElement> visited = new HashSet<>();
		return getParentPathwaysRecursively(biopaxElement, visited);
	}

	// recursively finds all the parent pathways of the object, while escaping infinite loops
	private static Set<Pathway> getParentPathwaysRecursively(
			final BioPAXElement biopaxElement, final Set<BioPAXElement> visited) {

		final Set<Pathway> pathways = new HashSet<>();
		
		//shortcut, when bpe is null or already processed
		if(biopaxElement == null || !visited.add(biopaxElement)) {
			LOG.debug("Ignored null or previously visited object:" + biopaxElement);
			return pathways;
		}
		
		LOG.debug("getParentPathways called: " + biopaxElement.getUri());

		if(biopaxElement instanceof Process) {
			if(biopaxElement instanceof Pathway) // add itself
				pathways.add((Pathway) biopaxElement);
			// continue looking up to parent pathways (until all top ones reached)
			//TODO: inf. loop happened here (StackOverFlow; e.g., using PC8 KEGG model)
			for(Pathway pw : ((Process)biopaxElement).getPathwayComponentOf())
				pathways.addAll(getParentPathwaysRecursively(pw, visited));
			for(Interaction it : ((Process)biopaxElement).getParticipantOf())
				pathways.addAll(getParentPathwaysRecursively(it, visited));
			for(PathwayStep pt : ((Process)biopaxElement).getStepProcessOf())
				pathways.addAll(getParentPathwaysRecursively(pt, visited));
		} else if(biopaxElement instanceof PathwayStep) {
			pathways.addAll(getParentPathwaysRecursively(((PathwayStep) biopaxElement).getPathwayOrderOf(), visited));
		} else if(biopaxElement instanceof PhysicalEntity ) {
			for(PhysicalEntity pe : ((PhysicalEntity)biopaxElement).getMemberPhysicalEntityOf())
				pathways.addAll(getParentPathwaysRecursively(pe, visited));
			for(Interaction it : ((Entity)biopaxElement).getParticipantOf())
				pathways.addAll(getParentPathwaysRecursively(it, visited));
			for(Complex c : ((PhysicalEntity)biopaxElement).getComponentOf())
				pathways.addAll(getParentPathwaysRecursively(c, visited));
		} else if(biopaxElement instanceof EntityReference) {
			for(EntityReference er : ((EntityReference) biopaxElement).getMemberEntityReferenceOf())
				pathways.addAll(getParentPathwaysRecursively(er, visited));
			for(SimplePhysicalEntity spe : ((EntityReference) biopaxElement).getEntityReferenceOf())
				pathways.addAll(getParentPathwaysRecursively(spe, visited));
		} else if (biopaxElement instanceof Gene ) { 
			for(Interaction it : ((Entity) biopaxElement).getParticipantOf())
				pathways.addAll(getParentPathwaysRecursively(it, visited));
		} else {
			// ignore
		}
		
		return pathways;
	}

	/**
	 * Merges equivalent interactions (currently - Conversions only).
	 *
	 * Warning: experimental; - check if the result is desirable;
	 * 			the result very much depends on actual pathway data quality...
	 * 
	 * @param model to edit/update
	 */
	public static void mergeEquivalentInteractions(Model model)
	{
		EquivalenceGrouper<Conversion> groups = new EquivalenceGrouper(model.getObjects(Conversion.class));

		for (List<Conversion> group : groups.getBuckets())
		{
			if (group.size() > 1)
			{
				HashMap<Conversion,Conversion> subs = new HashMap<Conversion, Conversion>();//to replace in the model
				Conversion primus = null;
				for (Conversion conversion : group)
				{
					if (primus == null)
					{
						primus = conversion;
					} else
					{
						copySimplePointers(model, conversion, primus);
						subs.put(conversion, primus);
					}
				}

				ModelUtils.replace(model, subs);

				for (Conversion conversion : subs.keySet())
				{
					model.remove(conversion);
				}
			}
		}
	}

	/**
	 * Merges equivalent physical entities.
	 *
	 * This can greatly decrease model's size and improve some visualizations, but
	 * can also introduce (or uncover hidden) semantic problems, such as when a physical entity is both
	 * component of a complex and independently participates in an interaction
	 * (this can happen when location and mod. features of a protein are not defined -
	 * only names, xrefs and perhaps entity reference - are there).
	 *
	 * Note (warning): please check if the result is desirable;
	 * the result of the merging very much depends on actual pathway data quality
	 * (in fact, such merging is better if decided and done by a data provider before releasing the data)...
	 *
	 * @param model to edit/update
	 */
	public static void mergeEquivalentPhysicalEntities(Model model) {
		HashMap<BioPAXElement,BioPAXElement> subs = new HashMap<>();
		EquivalenceGrouper<PhysicalEntity> groups = new EquivalenceGrouper(model.getObjects(PhysicalEntity.class));
		for (List<PhysicalEntity> group : groups.getBuckets()) {
			if (group.size() > 1) {
				PhysicalEntity primus = null;
				for (PhysicalEntity pe : group) {
					if (primus == null) {
						primus = pe;
					} else {
						copySimplePointers(model, pe, primus);
						//put in the map to replace pe with primus later on
						if(!subs.containsKey(pe))
							subs.put(pe,primus);
						else // this must not ever happen unless there's a bug in EquivalenceGrouper...
							throw new AssertionError("mergeEquivalentPhysicalEntities: equivalence groups do intersect; "
								+ pe.getUri() + " was in the other group as well");
					}
				}
			}
		}

		//do replace equivalent objects in the model
		replace(model, subs);

		//remove replaced ones
		for (BioPAXElement pe : subs.keySet()) {
			model.remove(pe);
		}
	}

	// base62 encoding/decoding for (not too long) URI shortening
	private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private static final int BASE = ALPHABET.length();

	public static String encodeBase62(String str) {
		// encode to base10 (long) first -
		char[] chars = new StringBuilder(str).reverse().toString().toCharArray();
		long n = 0;
		for (int i = chars.length - 1; i >= 0; i--) {
			n += ALPHABET.indexOf(chars[i]) * Math.pow(BASE, i);
		}

		// convert the long number to a base62 short string
		StringBuilder sb = new StringBuilder("");
		while (n > 0) {
			int rem = (int)(n % BASE);
			sb.append(ALPHABET.charAt(rem));
			n = n / BASE;
		}

		return sb.reverse().toString();
	}

	/**
	 * Creates a short URI from the URI,
	 * given the xml:base. One have to
	 * check the new URI is unique before
	 * using in a model (if not - e.g., add some
	 * suffix to the xmlBase parameter and try again).
	 *
	 * @param xmlbase
	 * @param uri
     * @return a short URI
     */
	public static String shortenUri(String xmlbase, String uri) {
		return xmlbase + encodeBase62(uri);
	}


	/**
	 * Replaces the URI of a BioPAX object in the Model using java reflection.
	 * If the element also belongs to other BioPAX models, those will become inconsistent
	 * unless this method is called for each such model.
	 *
	 * Warnings:
	 *  - one should not normally use this method at all;
	 *  - but if you do, then don't use a URI of another object from the same model.
	 *
	 * @param model model (can be null; if the object in fact belongs to a model, the model will be inconsistent)
	 * @param el biopax object
	 * @param newUri URI - not null/empty URI
	 */
	public static  void updateUri(Model model, BioPAXElement el, String newUri) {
		if(newUri == null || newUri.isEmpty())
			throw new IllegalArgumentException("New URI is null or empty.");

		if(model != null)
			model.remove(el);

		try {
			Method m = BioPAXElementImpl.class.getDeclaredMethod("setUri", String.class);
			m.setAccessible(true);
			m.invoke(el, newUri);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		if(model != null)
			model.add(el);
	}


	/**
	 * Removes cyclic pathway inclusions, non-trivial infinite loops, in 'pathwayComponent' biopax property.
	 * Such loops usually do not make much sense and only can cause trouble in pathway data analysis.
	 * This tool recursively removes parent pathways from sub pathways' pathwayComponent set.
	 *
	 * @param model a model that contains Pathways; will be modified as the result
	 */
	public static void breakPathwayComponentCycle(final Model model) {
		for(Pathway pathway : model.getObjects(Pathway.class))
			breakPathwayComponentCycle(new HashSet<>(), pathway, pathway);
	}

	//Recursively, though, avoiding infinite loops (KEGG pathways can cause it),
	// removes cyclic pathway inclusions like pathwayA/pathwayA or pathwayA/pathwayB/pathwayA, etc.
	private static void breakPathwayComponentCycle(final Set<Pathway> visited, final Pathway rootPathway,
											final Pathway currentPathway)
	{
		if(!visited.add(currentPathway))
			return; // already processed

		if(currentPathway.getPathwayComponent().contains(rootPathway)) {
			currentPathway.removePathwayComponent(rootPathway);
		}

		for(Process proc : currentPathway.getPathwayComponent())
			if(proc instanceof Pathway)
				breakPathwayComponentCycle(visited, rootPathway, (Pathway) proc);
	}

	/**
	 * Checks whether the BioPAX element is generic physical entity or entity reference.
	 *
	 * @param e biopax object
	 * @return true when the object is generic physical entity or entity reference
	 */
	public static boolean isGeneric(BioPAXElement e) {
		return (
			(e instanceof EntityReference && !((EntityReference)e).getMemberEntityReference().isEmpty())
			|| (e instanceof PhysicalEntity && !((PhysicalEntity)e).getMemberPhysicalEntity().isEmpty())
			|| (e instanceof SimplePhysicalEntity && isGeneric(((SimplePhysicalEntity) e).getEntityReference()))
		);
		//false when e==null
	}
}
