package org.biopax.paxtools.converter;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.ModelFilter;
import org.biopax.paxtools.controller.PrimitivePropertyEditor;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.controller.PropertyFilter;
import org.biopax.paxtools.io.simpleIO.SimpleEditorMap;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.InteractionParticipant;
import org.biopax.paxtools.model.level2.Level2Element;
import org.biopax.paxtools.model.level2.complex;
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
import org.biopax.paxtools.model.level2.relationshipXref;
import org.biopax.paxtools.model.level2.rna;
import org.biopax.paxtools.model.level2.smallMolecule;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.ControlledVocabulary;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.DnaRegion;
import org.biopax.paxtools.model.level3.Level3Element;
import org.biopax.paxtools.model.level3.Level3Factory;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.RelationshipTypeVocabulary;
import org.biopax.paxtools.model.level3.RnaRegion;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.model.level3.SmallMolecule;
import org.biopax.paxtools.model.level3.Stoichiometry;

/**
 * Converts BioPAX L1 and L2 to Level 3.
 * 
 * Notes:
 * - it does not fix existing BioPAX errors (TODO add validation/normalization in the future, maybe)
 * - most but not all things are converted (e.g., complex.ORGANISM property cannot...)
 * - phy. entities "clones" - because, during L1 or L2 data read, all re-used pEPs are duplicated... (TODO filter after the conversion)
 * 
 * @author rodch
 *
 */
public final class OneTwoThree extends AbstractTraverser implements ModelFilter {
	private static final Log log = LogFactory.getLog(OneTwoThree.class);
	private Level3Factory factory;
	private Properties classesmap;
	private Properties propsmap;
	
	public static EditorMap editorMap2 = new SimpleEditorMap(BioPAXLevel.L2);
	public static EditorMap editorMap3 = new SimpleEditorMap(BioPAXLevel.L3);
	
	/**
	 * Default Constructor 
	 * that also loads 'classesmap' and 'propsmap' 
	 * from the properties files.
	 */
	public OneTwoThree() {
		super(editorMap2, new PropertyFilter() {
			public boolean filter(PropertyEditor editor) {
				return !editor.getProperty().equals("STOICHIOMETRIC-COEFFICIENT"); 
				// will be set manually (pEPs special case)
			}
		  },
		  new PropertyFilter() {
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
		
		// TODO add step processes to the pathway components (L3)
		
		// TODO remove clones
		
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
				newParent = getMappedPep((physicalEntityParticipant) parent, newModel);
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
				editorMap3.getEditorForProperty(newProp, newParent.getModelInterface());
			
			if(value instanceof Level2Element) 
			// not a String, Enum, or primitive type
			{ 
				// when pEP, create/add stoichiometry! 
				if(value instanceof physicalEntityParticipant) 
				{
					physicalEntityParticipant pep = (physicalEntityParticipant) value;
					newValue = getMappedPep(pep, newModel);
					
					float coeff = (float) pep.getSTOICHIOMETRIC_COEFFICIENT();
					if (coeff > 1 ) { //!= BioPAXElement.UNKNOWN_DOUBLE) {
					  if(parent instanceof conversion || parent instanceof complex) { 
						PhysicalEntity pe3 = (PhysicalEntity) newValue;
						Stoichiometry stoichiometry = factory.reflectivelyCreate(Stoichiometry.class);
						stoichiometry.setRDFId(pe3.getRDFId() + "-stoichiometry");
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
						(Level2Element)parent, newModel, newProp);
				}
				else
				{
					String id = ((Level2Element) value).getRDFId();
					newValue = newModel.getByID(id);
				}
			} else {
				// relationshipXref.RELATIONSHIP-TYPE range changed (String -> RelationshipTypeVocabulaty)
				if(parent instanceof relationshipXref && editor.getProperty().equals("RELATIONSHIP-TYPE")) {
					String id = URLEncoder.encode(value.toString());
					if(!newModel.containsID(id)) {
						RelationshipTypeVocabulary cv = (RelationshipTypeVocabulary) 
							factory.reflectivelyCreate(newEditor.getRange());
						cv.setRDFId(id);
						cv.addTerm(value.toString().toLowerCase()); // TODO later, normalize, add xref, check term...
						newModel.add(cv);
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
						newParent = getMappedPep(pep, newModel);
						if(newParent != null) {
							newEditor = 
								editorMap3.getEditorForProperty(
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
			newEditor.setPropertyToBean(newParent, newValue);
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
		physicalEntity pe2er = pep.getPHYSICAL_ENTITY();
		
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

}
