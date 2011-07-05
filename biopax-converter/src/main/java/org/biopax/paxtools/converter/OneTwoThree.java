package org.biopax.paxtools.converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.controller.*;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.*;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.Filter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.*;

/**
 * Converts BioPAX L1 and L2 to Level 3.
 * 
 * Notes:
 * - it does not fix existing BioPAX errors
 * - most but not all things are converted (e.g., complex.ORGANISM property cannot...)
 * - phy. entities "clones" - because, during L1 or L2 data read, all re-used pEPs are duplicated... (TODO filter after the conversion)
 * 
 * @author rodch
 *
 */
public final class OneTwoThree extends AbstractTraverser implements ModelFilter {
	private static final Log log = LogFactory.getLog(OneTwoThree.class);
	private static final String l3PackageName = "org.biopax.paxtools.model.level3.";

	private BioPAXFactory factory;
	private Properties classesmap;
	private Properties propsmap;

	/**
	 * For mapping level2 enums to level2.
	 */
	private Map<Object, Object> enumMap;

	/**
	 * Several PEPs in L2 will correspond to a PE in L3. Id of PE will be the ID of one of the
	 * related PEPs. This map will point Ids of PEPs to the ID of PE.
	 */
	private Map<String, String> pep2PE;

	

	/**
	 * Default Constructor 
	 * that also loads 'classesmap' and 'propsmap' 
	 * from the properties files.
	 */
	public OneTwoThree() {
		super(SimpleEditorMap.L2, new Filter<PropertyEditor>() {
			public boolean filter(PropertyEditor editor) {
				return !editor.getProperty().equals("STOICHIOMETRIC-COEFFICIENT"); 
				// will be set manually (pEPs special case)
			}
		  },
		  new Filter<PropertyEditor>() {
			public boolean filter(PropertyEditor editor) {
				return 
					!( 
						editor.getProperty().equals("ORGANISM")
						&& complex.class.isAssignableFrom(editor.getDomain())
					); 
				// L3 Complex has no 'organism' property
			}
		  }
		);
		
		factory = BioPAXLevel.L3.getDefaultFactory();
		// load L2-L3 classes map
		classesmap = new Properties();
		try {
			classesmap.load(getClass().getResourceAsStream("classesmap.properties"));
		// load L2-L3 properties map
		propsmap = new Properties();
		propsmap.load(getClass().getResourceAsStream("propsmap.properties"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		enumMap = new HashMap<Object, Object>();
	}
	
	/**
	 * Constructor
	 * @param factory
	 * @throws IOException
	 */
	public OneTwoThree(BioPAXFactory factory) throws IOException{
		this();
		this.factory = factory;
	}

	/**
	 * Converts a BioPAX Model, Level 1 or 2, to the Level 3.
	 *
	 * @param model
	 * @return
	 */
	public Model filter(Model model) {
		if(model == null || model.getLevel() != BioPAXLevel.L2) 
		{
			if(model != null && log.isInfoEnabled()) 
				log.info("Model is " + model.getLevel());
			return model; // nothing to do
		}

		preparePep2PEIDMap(model);

		final Model newModel = factory.createModel();
		newModel.getNameSpacePrefixMap().putAll(model.getNameSpacePrefixMap());
		
		// facilitate the conversion
		normalize(model);
		
		// First, map classes (and only set ID) if possible (pre-processing)
		for(BioPAXElement bpe : model.getObjects()) {
			Level3Element l3element = mapClass(bpe);
			if(l3element != null) {
				newModel.add(l3element);
			} else {
				if(log.isDebugEnabled())
					log.debug("Skipping " + bpe 
						+ " " + bpe.getModelInterface().getSimpleName());
			}
		}
		
		/* process each L2 element (mapping properties), 
		 * except for pEPs and oCVs that must be processed as values
		 * (anyway, we do not want dangling elements)
		 */
		for(BioPAXElement e : model.getObjects()) {
			if(e instanceof physicalEntityParticipant
				|| e instanceof openControlledVocabulary) {
				continue;
			}
			
			// map properties
			traverse((Level2Element) e, newModel);
		}

		if(log.isInfoEnabled())
			log.info("Done. The new model contains " 
					+ newModel.getObjects().size()
					+ " BioPAX individuals.");
		
		// fix new model (e.g., add PathwayStep's processes to the pathway components ;))
		normalize(newModel);
		
		return newModel;
	}


	/*
	 * Fixes several problems in the BioPAX model
	 */
	private void normalize(Model model) {
		/* If a pE participates via pEP, no problem.
		 * Otherwise, - there is a special case. 
		 * 
		 * physicalEntity PARTICIPANT takes part in interactions
		 * either directly or via physicalEntityParticipant (pEP). 
		 * With pEP, the corresponding L3 PE and ER have been 
		 * already created (above). However, although (if no pEPs used) 
		 * we got Complex and basic PhysicalEntity objects created in L3,
		 * and others (protein, dna, etc.) became ER, we still have to 
		 * create another PEs for all of them and set their ER property (where exists).
		 */
		for (interaction itr : model.getObjects(interaction.class)) {
			if(itr instanceof conversion || itr instanceof control) 
				continue; // cannot have pE participants anyway (only pEPs)
			for (InteractionParticipant ip : itr.getPARTICIPANTS()) {
				if (ip instanceof physicalEntity) { 
					physicalEntity pe = (physicalEntity) ip;
					// create a new pEP
					String newId = itr.getRDFId() + "_"	+ getLocalId(pe);
					physicalEntityParticipant pep = model.addNew(physicalEntityParticipant.class, newId);
					pep.setPHYSICAL_ENTITY(pe);
					// no other properties though
					// reset participant
					itr.removePARTICIPANTS(pe);
					itr.addPARTICIPANTS(pep);
				}
			}
		}
		
		// ...adding step processes to the pathway components (L3) is NOT always required
		
		// TODO remove cloned UtilityClass elements
		
		// set 'name' for simple physical entities (because pEPs didn't have names)
		for(EntityReference er : model.getObjects(EntityReference.class)) {
			for(SimplePhysicalEntity spe : er.getEntityReferenceOf()) {
				// after the conversion, it's always empty.., but let's double-check
				if(spe.getName().isEmpty()) {
					spe.getName().addAll(er.getName());
				}
				
				if(spe.getDisplayName() == null || spe.getDisplayName().trim().length() == 0) {
					spe.setDisplayName(er.getDisplayName());
				}
			}
		}
		
	}

	
	// creates L3 classes and sets IDs
	private Level3Element mapClass(BioPAXElement bpe) {
		Level3Element newElement = null;
		
		if(bpe instanceof physicalEntityParticipant)
		{
			String id = pep2PE.get(bpe.getRDFId());

			if(id == null) {
				log.warn("No mapping possible for " + bpe.getRDFId());
				return null;
			}
			else if (id.equals(bpe.getRDFId()))
			{
				// create a new simplePhysicalEntity
				//(excluding Complex and basic PhysicalEntity that map directly and have no ERs)
				newElement = createSimplePhysicalEntity((physicalEntityParticipant)bpe);
			}
		}
		else if(!(bpe instanceof openControlledVocabulary)) // skip oCVs
		{
			// using classesmap.properties to map other types
			String type = bpe.getModelInterface().getSimpleName();	
			String newType = classesmap.getProperty(type).trim();
			if (newType != null && factory.canInstantiate(factory.getLevel().getInterfaceForName(newType)))
            {
				newElement = (Level3Element) factory.create(newType, bpe.getRDFId());
			} else {
				if(log.isDebugEnabled())
					log.debug("No mapping found for " + type);
				return null;
			}
		}

		return newElement;
	}


	/*
	 * Create L3 simple PE type using the L2 pEP.
	 *
	 * When pEP's PHYSICAL_ENTITY is either complex
	 * or basic physicalEntity, null will be the result.
	 */
	private SimplePhysicalEntity createSimplePhysicalEntity(physicalEntityParticipant pep) {
		physicalEntity pe2 = pep.getPHYSICAL_ENTITY();
		return createSimplePhysicalEntity(pe2, pep.getRDFId());
	}

	private SimplePhysicalEntity createSimplePhysicalEntity(physicalEntity pe2, String id) {
		SimplePhysicalEntity e = null;
		if(pe2 instanceof protein) {
			e = factory.create(Protein.class, id);
		} else if(pe2 instanceof dna) {
			e = factory.create(DnaRegion.class, id);
		} else if (pe2 instanceof rna) {
			e = factory.create(RnaRegion.class, id);
		} else if (pe2 instanceof smallMolecule) {
			e = factory.create(SmallMolecule.class, id);
		}
		return e;
	}


	/*
	 * Creates a specific ControlledVocabulary subclass
	 * and adds to the new model
	 */
	private ControlledVocabulary convertAndAddVocabulary(openControlledVocabulary value,
			Level2Element parent, Model newModel, PropertyEditor newEditor) {
		ControlledVocabulary cv = null;
		String id = ((BioPAXElement) value).getRDFId();

		if (!newModel.containsID(id)) {
			if (newEditor != null) {
				newModel.addNew(newEditor.getRange(), id);
				// copy properties
				traverse(value, newModel);
			} else {
				log.warn("Cannot Convert CV: " + value
					+ " (for prop.: " + newEditor + ")");
			}
		} else {
			cv = (ControlledVocabulary) newModel.getByID(id);
		}

		return cv;
	}

	// parent class's abstract method implementation
	protected void visit(Object value, BioPAXElement parent,
			Model newModel, PropertyEditor editor)
	{
			if(editor != null && editor.isUnknown(value)) {
				return;
			}

			String parentType = parent.getModelInterface().getSimpleName();
			BioPAXElement newParent = null;
			Object newValue = value;
			String newProp = propsmap.getProperty(editor.getProperty());

			// special case (PATHWAY-COMPONENTS maps to pathwayComponent or pathwayOrder)
			if(parent instanceof pathway && value instanceof pathwayStep
					&& editor.getProperty().equals("PATHWAY-COMPONENTS")) {
				newProp = "pathwayOrder";
			}

			// for pEPs, getting the corresponding simple PE or Complex is different
			if(parent instanceof physicalEntityParticipant) {
				newParent = getMappedPE((physicalEntityParticipant) parent, newModel);
			} else {
				newParent = newModel.getByID(parent.getRDFId());
			}

			// bug check!
			if(newParent == null) {
				throw new IllegalAccessError("Of " + value +
					", parent " + parentType + " : " + parent +
					" is not yet in the new model: ");
			}

			PropertyEditor newEditor =
				SimpleEditorMap.L3.getEditorForProperty(newProp, newParent.getModelInterface());

			if(value instanceof Level2Element)
			// not a String, Enum, or primitive type
			{
				// when pEP, create/add stoichiometry!
				if(value instanceof physicalEntityParticipant)
				{
					physicalEntityParticipant pep = (physicalEntityParticipant) value;
					newValue = getMappedPE(pep, newModel);

					float coeff = (float) pep.getSTOICHIOMETRIC_COEFFICIENT();
					if (coeff > 1 ) { //!= BioPAXElement.UNKNOWN_DOUBLE) {
					  if(parent instanceof conversion || parent instanceof complex) {
						PhysicalEntity pe3 = (PhysicalEntity) newValue;
						Stoichiometry stoichiometry = factory
							.create(Stoichiometry.class,
                                    pe3.getRDFId() + "-stoichiometry" + Math.random());
						stoichiometry.setStoichiometricCoefficient(coeff);
						stoichiometry.setPhysicalEntity(pe3);
						//System.out.println("parent=" + parent + "; phy.ent.=" + pep + "; coeff=" + coeff);
						if (parent instanceof conversion) {
							// (pep) value participates in the conversion interaction
							Conversion conv = (Conversion) newModel
								.getByID(parent.getRDFId());
							conv.addParticipantStoichiometry(stoichiometry);	
						} else {
							// this (pep) value is component of the complex
							Complex cplx = (Complex) newModel.getByID(parent.getRDFId());
							cplx.addComponentStoichiometry(stoichiometry);
						} 
						
						newModel.add(stoichiometry);
						
					  } else {
						if (log.isDebugEnabled())
							log.debug(pep + " STOICHIOMETRIC_COEFFICIENT is "
							+ coeff	+ ", but the pEP's parent is not " +
							"a conversion or complex - " + parent);
					  }
					}
					
					traverse(pep, newModel);
				}
				else if(value instanceof openControlledVocabulary) 
				{
					// create the proper type ControlledVocabulary instance	
					newValue = convertAndAddVocabulary((openControlledVocabulary)value, 
						(Level2Element)parent, newModel, newEditor);
				}
				else
				{
					String id = ((Level2Element) value).getRDFId();
					newValue = newModel.getByID(id);
				}
			}
			else if (value.getClass().isEnum())
			{
				newValue = getMatchingEnum(value);
			}
			else
			{
				// relationshipXref.RELATIONSHIP-TYPE range changed (String -> RelationshipTypeVocabulaty)
				if(parent instanceof relationshipXref && editor.getProperty().equals("RELATIONSHIP-TYPE")) {
					String id = URLEncoder.encode(value.toString());
					if(!newModel.containsID(id)) {
						RelationshipTypeVocabulary cv = (RelationshipTypeVocabulary) 
							newModel.addNew(newEditor.getRange(), id);
						cv.addTerm(value.toString().toLowerCase());
						newValue = cv;
					} else {
						newValue = newModel.getByID(id);
					}
				}
			}
			
			if(newValue == null) {
				log.warn("Skipping:  " + parent + "." + editor.getProperty() 
					+ "=" + value + " ==> " + newParent.getRDFId() 
					+ "." + newProp	+ "=NULL");
				return;
			}
			
			if (newProp != null) {
				if (newEditor != null){
					setNewProperty(newParent, newValue, newEditor);
				} else // Special mapping for 'AVAILABILITY' and 'DATA-SOURCE'!
				  if(parent instanceof physicalEntity) {
					// find parent pEP(s)
					Set<physicalEntityParticipant> ppeps = ((physicalEntity)parent).isPHYSICAL_ENTITYof();
					// if several pEPs use the same phy.entity, we get this property/value cloned...
					for(physicalEntityParticipant pep: ppeps) {
						//find proper L3 physical entity
						newParent = getMappedPE(pep, newModel);
						if(newParent != null) {
							newEditor = 
								SimpleEditorMap.L3.getEditorForProperty(
										newProp, newParent.getModelInterface());
							setNewProperty(newParent, newValue, newEditor);
						} else { // bug!
							log.error("Cannot find converted PE to map the property " 
								+ editor.getProperty() 
								+ " of physicalEntity " 
								+ parent + " (" + parentType + ")");
						}
					}
				} else {
					log.info("Skipping property " 
						+ editor.getProperty() 
						+ " in " + parentType + " to " +
						newParent.getModelInterface().getSimpleName() 
						+ " convertion (" + parent + ")");
				}
			} else {
				log.warn("No mapping defined for property: " 
						+ parentType + "."
						+ editor.getProperty());
			} 
		}


	private void setNewProperty(BioPAXElement newParent, Object newValue, PropertyEditor newEditor) {
		if(newEditor != null){
			if(newEditor instanceof PrimitivePropertyEditor) {
				newValue = newValue.toString();
			}
			newEditor.setValueToBean(newValue, newParent);
		}		
	}

	/*
	 * pEP->PE; pE->ER class mapping was done for "simple" entities;
	 * for complex and the "basic" pE, it is pE->PE mapping
	 * (although pEPs were skipped, their properties are to save anyway)
	 */
	private PhysicalEntity getMappedPE(physicalEntityParticipant pep, Model newModel)
	{
		String id = pep2PE.get(pep.getRDFId());
		physicalEntity pe2er = pep.getPHYSICAL_ENTITY();
		
		if(id == null || pe2er == null)
			throw new IllegalAccessError("Illegal pEP (cannot convert): " 
				+ pep.getRDFId()); 
		
		BioPAXElement pe = newModel.getByID(id);
		String inf = "pEP " + pep + " that contains " 
			+ pe2er.getModelInterface().getSimpleName();
		if(!isSimplePhysicalEntity(pe2er)) {
			if(pe == null) {
				if(log.isDebugEnabled())
					log.debug(inf + " gets new ID: " + pe2er.getRDFId());
				pe = newModel.getByID(pe2er.getRDFId());
			} else { // error: complex and basic p.entity's pEP 
				throw new IllegalAccessError("Illegal conversion of pEP: " + inf); 
			}
		} else if(pe == null){
			// for a pEP having a simple PE, a new PE should be created
			throw new IllegalAccessError("No PE for: " + inf
					+ " found in the new Model");
		}
		
		return (PhysicalEntity) pe;
	}

	private boolean isSimplePhysicalEntity(Level2Element pe2) {
		return (pe2 instanceof protein
				|| pe2 instanceof dna
				|| pe2 instanceof rna
				|| pe2 instanceof smallMolecule);
	}
	
    /**
     * Gets the local part of the BioPAX element ID.
     * 
     * @param bpe
     * @return
     */
   	static public String getLocalId(BioPAXElement bpe) {
		String id = bpe.getRDFId();
		return (id != null) ? id.replaceFirst("^.+#", "") : null;
	}

	protected Object getMatchingEnum(Object o)
	{
		assert o.getClass().isEnum();

		if (enumMap.containsKey(o))
			return enumMap.get(o);

		if (!propsmap.containsKey(o.toString()))
		{
			enumMap.put(o, null);
			return null;
		}

		String l2Name = o.getClass().getName();
		l2Name = l2Name.substring(l2Name.lastIndexOf(".") + 1);

		if (!classesmap.containsKey(l2Name))
		{
			log.error("There is no class mapping for enum " + o.getClass() + " in classesmap");
			return null;
		}

		String l3Name = classesmap.getProperty(l2Name);

		assert l3Name != null;

		String l3value = propsmap.getProperty(o.toString());

		assert l3value != null;

		Class<?> cls = null;
		try
		{
			cls = Class.forName(l3PackageName + l3Name);
		}
		catch (ClassNotFoundException e)
		{
			log.error("Cannot find class " + l3PackageName + l3Name);
			//e.printStackTrace();
		}

		assert cls != null;

		Method meth = null;
		try
		{
			meth = cls.getMethod("valueOf", String.class);
		}
		catch (NoSuchMethodException e)
		{
			log.error("No valueOf method here. Is this possible ?!");
			e.printStackTrace();
		}

		assert meth != null;

		Object l3enum = null;
		try
		{
			l3enum = meth.invoke(null, l3value);
		}
		catch (IllegalAccessException e)
		{
			log.error("Cannot invoke method " 
				+ meth + " - " + e);
			//e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			log.error("Cannot invoke method " + meth 
				+ " - " + e);
			//e.printStackTrace();
		}

		enumMap.put(o, l3enum);
		return l3enum;
	}

	private List<Set<physicalEntityParticipant>> getPepsGrouped(physicalEntity pe)
	{
		List<Set<physicalEntityParticipant>> list = new ArrayList<Set<physicalEntityParticipant>>();

		for (physicalEntityParticipant pep : pe.isPHYSICAL_ENTITYof())
		{
			boolean added = false;

			for (Set<physicalEntityParticipant> group : list)
			{
				physicalEntityParticipant first = group.iterator().next();

				if (first.isInEquivalentState(pep))
				{
					group.add(pep);
					added = true;
					break;
				}
			}

			if (!added)
			{
				Set<physicalEntityParticipant> group = new HashSet<physicalEntityParticipant>();
				group.add(pep);
				list.add(group);
			}
		}
		return list;
	}

	private Map<String, String> getPep2StateIDMapping(physicalEntity pe)
	{
		List<Set<physicalEntityParticipant>> sets = getPepsGrouped(pe);

		Map<String, String> map = new HashMap<String, String>();

		for (Set<physicalEntityParticipant> set : sets)
		{
			physicalEntityParticipant first = set.iterator().next();

			for (physicalEntityParticipant pep : set)
			{
				map.put(pep.getRDFId(), first.getRDFId());
			}
		}
		return map;
	}

	protected void preparePep2PEIDMap(Model model)
	{
		assert model.getLevel() == BioPAXLevel.L2;

		pep2PE = new HashMap<String, String>();

		for (physicalEntity pe : model.getObjects(physicalEntity.class))
		{
			Map<String, String> map = getPep2StateIDMapping(pe);

			pep2PE.putAll(map);
		}
	}
}
