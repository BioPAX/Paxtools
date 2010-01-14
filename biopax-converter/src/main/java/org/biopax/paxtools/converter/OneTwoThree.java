package org.biopax.paxtools.converter;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.ModelFilter;
import org.biopax.paxtools.controller.PrimitivePropertyEditor;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.io.simpleIO.SimpleEditorMap;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.InteractionParticipant;
import org.biopax.paxtools.model.level2.Level2Element;
import org.biopax.paxtools.model.level2.control;
import org.biopax.paxtools.model.level2.conversion;
import org.biopax.paxtools.model.level2.dna;
import org.biopax.paxtools.model.level2.interaction;
import org.biopax.paxtools.model.level2.openControlledVocabulary;
import org.biopax.paxtools.model.level2.pathway;
import org.biopax.paxtools.model.level2.pathwayStep;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.protein;
import org.biopax.paxtools.model.level2.rna;
import org.biopax.paxtools.model.level2.smallMolecule;
import org.biopax.paxtools.model.level3.ControlledVocabulary;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.DnaRegion;
import org.biopax.paxtools.model.level3.Level3Element;
import org.biopax.paxtools.model.level3.Level3Factory;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.RnaRegion;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.model.level3.SmallMolecule;
import org.biopax.paxtools.model.level3.Stoichiometry;

/**
 * Converts BioPAX L1 and L2 to Level 3.
 * 
 * @author rodch
 *
 */
public final class OneTwoThree implements ModelFilter {
	private static final Log log = LogFactory.getLog(OneTwoThree.class);
	private Level3Factory factory;
	private Properties classesmap;
	private Properties propsmap;
	
	public static EditorMap editorMap2;
	public static EditorMap editorMap3;
	
	static {
		editorMap2 = new SimpleEditorMap(BioPAXLevel.L2);
		editorMap3 = new SimpleEditorMap(BioPAXLevel.L3);
	}
	
	
	
	/**
	 * Default Constructor 
	 * (it also loads 'classesmap' and 'propsmap' from property files)
	 */
	public OneTwoThree() {
		factory = (Level3Factory) BioPAXLevel.L3.getDefaultFactory();
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
	}
	
	/**
	 * Constructor
	 * @param factory
	 * @throws IOException
	 */
	public OneTwoThree(Level3Factory factory) throws IOException{
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
		
		final Model newModel = factory.createModel();
		
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
					physicalEntityParticipant pep = BioPAXLevel.L2
							.getDefaultFactory().reflectivelyCreate(
									physicalEntityParticipant.class);
					String newId = itr.getRDFId() + "_"	+ getLocalId(pe);
					pep.setRDFId(newId);
					pep.setPHYSICAL_ENTITY(pe);
					model.add(pep);
					// no other properties though
					
					// reset participant
					itr.removePARTICIPANTS(pe);
					itr.addPARTICIPANTS(pep);
				}
			}
		}
		
		//TODO add step processes to the pathway components (L3)
		
		
	}

	
	// creates L3 classes and sets IDs
	private Level3Element mapClass(BioPAXElement bpe) {
		Level3Element newElement = null;
		
		if(bpe instanceof physicalEntityParticipant) {
			// create a new simplePhysicalEntity 
			//(excluding Complex and basic PhysicalEntity that map directly and have no ERs)
			newElement = createSimplePhysicalEntity((physicalEntityParticipant)bpe);
		}
		else if(!(bpe instanceof openControlledVocabulary)) // skip oCVs
		{
			// using classesmap.properties to map other types
			String type = bpe.getModelInterface().getSimpleName();	
			String newType = classesmap.getProperty(type).trim();
			if (newType != null && factory.canInstantiate(newType)) {
				newElement = (Level3Element) factory.reflectivelyCreate(newType);
			} else {
				if(log.isDebugEnabled()) 
					log.debug("No mapping found for " + type);
				return null;
			}
		}
		
		if(newElement != null)
			newElement.setRDFId(bpe.getRDFId());
		
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
		return createSimplePhysicalEntity(pe2);
	}
	
	private SimplePhysicalEntity createSimplePhysicalEntity(physicalEntity pe2) {
		SimplePhysicalEntity e = null;
		if(pe2 instanceof protein) {
			e = factory.reflectivelyCreate(Protein.class);	
		} else if(pe2 instanceof dna) {
			e = factory.reflectivelyCreate(DnaRegion.class);
		} else if (pe2 instanceof rna) {
			e = factory.reflectivelyCreate(RnaRegion.class);
		} else if (pe2 instanceof smallMolecule) {
			e = factory.reflectivelyCreate(SmallMolecule.class);
		} 
		return e;
	}

	
	/* 
	 * Creates a specific ControlledVocabulary subclass 
	 * and adds to the new model
	 */
	private ControlledVocabulary convertAndAddVocabulary(openControlledVocabulary value,
			Level2Element parent, Model newModel, String newProp) {
		ControlledVocabulary cv = null;
		String id = ((BioPAXElement) value).getRDFId();
		
		if (!newModel.containsID(id)) {
			BioPAXElement newParent = newModel.getByID(parent.getRDFId());
			// map oCVs to the OneTwoThree CVs using the property context
			PropertyEditor newEditor = editorMap3.getEditorForProperty(
					newProp, newParent.getModelInterface());
			if (newEditor != null) {
				cv = (ControlledVocabulary) 
					factory.reflectivelyCreate(newEditor.getRange());
				cv.setRDFId(id);
				newModel.add(cv);
				// copy properties
				traverse(value, newModel);
			} else {
				log.warn("Cannot Convert CV: " + value
					+ " (for prop.: " + newProp + ")");
			}
		} else {
			cv = (ControlledVocabulary) newModel.getByID(id);
		}
		
		return cv;
	}

	private void traverse(Level2Element element, Model newModel) {
		if (element == null) {
			return;
		}
		
		Set<PropertyEditor> editors = editorMap2.getEditorsOf(element);
		if(editors == null || editors.isEmpty()) {
			if(log.isWarnEnabled())
				log.warn("No editors for : " + element.getModelInterface());
			return;
		}
			
		for (PropertyEditor editor : editors) {
			if (editor.isMultipleCardinality()) {
				for (Object value : (Collection) editor.getValueFromBean(element)) {
					processValue(value, element, newModel, editor);
				}
			} else {
				Object value = editor.getValueFromBean(element);
				processValue(value, element, newModel, editor);
			}
		}
	}
	
	private void processValue(Object value, BioPAXElement parent, Model newModel, PropertyEditor editor) 
	{
			if(editor != null && editor.isUnknown(value)) {
				return;
			}

			BioPAXElement newParent = null;
			Object newValue = value;
			
			String newProp = propsmap.getProperty(editor.getProperty());
			// special case (PATHWAY-COMPONENTS maps to pathwayComponent or pathwayOrder)
			if(parent instanceof pathway && value instanceof pathwayStep
					&& editor.getProperty().equals("PATHWAY-COMPONENTS")) {
				newProp = "pathwayOrder";
			}
			
			if(parent instanceof physicalEntityParticipant) {
				newParent = getMappedPep((physicalEntityParticipant) parent, newModel);
			} else {
				newParent = newModel.getByID(parent.getRDFId());
			}
					
			if(value instanceof Level2Element) // not a String, Enum, or primitive type
			{ 
				// when pEP - add its stoichiometry
				if(value instanceof physicalEntityParticipant) 
				{
					physicalEntityParticipant pep = (physicalEntityParticipant) value;
					newValue = getMappedPep(pep, newModel);
					PhysicalEntity pe3 = (PhysicalEntity) newValue;
					float coeff = (float) pep.getSTOICHIOMETRIC_COEFFICIENT();
					if (coeff != BioPAXElement.UNKNOWN_DOUBLE
						&& parent instanceof conversion) {
						Stoichiometry stoichiometry = factory.reflectivelyCreate(Stoichiometry.class);
						stoichiometry.setRDFId(pe3.getRDFId() + "-stoichiometry");
						stoichiometry.setStoichiometricCoefficient(coeff);
						stoichiometry.setPhysicalEntity(pe3);
						newModel.add(stoichiometry);
						Conversion conv = (Conversion) newModel.getByID(parent.getRDFId());
						conv.addParticipantStoichiometry(stoichiometry);
					} else if (coeff != BioPAXElement.UNKNOWN_FLOAT) {
						if (log.isDebugEnabled())
							log.debug(pep + " STOICHIOMETRIC_COEFFICIENT is "
								+ coeff	+ ", but the pEP's parent is not a conversion - "
								+ parent);
					}
					
					traverse(pep, newModel);
					
				}
				else if(value instanceof openControlledVocabulary) 
				{
					// create the proper type ControlledVocabulary instance	
					newValue = convertAndAddVocabulary((openControlledVocabulary)value, 
						(Level2Element)parent, newModel, newProp);
				}
				else
				{
					String id = ((Level2Element) value).getRDFId();
					newValue = newModel.getByID(id);
				}
			}
			
			if(newValue == null) {
				log.warn("Skipping:  " + parent + "." + editor.getProperty() 
					+ "=" + value + " ==> " + newParent.getRDFId() 
					+ "." + newProp	+ "=NULL");
				return;
			}
			
			String parentType = parent.getModelInterface().getSimpleName();
			
			if (newParent != null && newProp != null) {
				PropertyEditor newEditor = 
					editorMap3.getEditorForProperty(newProp, newParent.getModelInterface());
				if(newEditor != null){
					if(newEditor instanceof PrimitivePropertyEditor) {
						newValue = newValue.toString();
					}
					newEditor.setPropertyToBean(newParent, newValue);
				}
			} else if(newProp == null){
				log.warn("No mapping defined for property: " 
						+ parentType + "."
						+ editor.getProperty());
			} else {
				throw new IllegalAccessError("Of " + value + 
						", parent " + parentType + 
						" : " + parent +
						" is not yet in the new model: ");
			}
		}


	/*
	 * pEP->PE; pE->ER class mapping was done for "simple" entities;
	 * for complex and the "basic" pE, it is pE->PE mapping
	 * (although pEPs were skipped, their properties are to save anyway)
	 */
	private PhysicalEntity getMappedPep(physicalEntityParticipant pep, Model newModel) {
		String id = pep.getRDFId();
		BioPAXElement pe = newModel.getByID(id);
		physicalEntity pe2 = pep.getPHYSICAL_ENTITY();
		
		String inf = "pEP " + pep + " that contains " 
		+ pe2.getModelInterface().getSimpleName();
		if(!isSimplePhysicalEntity(pe2)) {
			if(pe == null) {
				if(log.isDebugEnabled())
					log.debug(inf + " gets new ID: " + pe2.getRDFId());
				pe = newModel.getByID(pe2.getRDFId());
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
}
