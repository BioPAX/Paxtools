package org.biopax.converter.upgrader;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.io.simpleIO.SimpleEditorMap;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level3.CellularLocationVocabulary;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.DnaRegion;
import org.biopax.paxtools.model.level3.DnaRegionReference;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.model.level3.RnaRegion;
import org.biopax.paxtools.model.level3.RnaRegionReference;
import org.biopax.paxtools.model.level3.SmallMolecule;
import org.biopax.paxtools.model.level3.SmallMoleculeReference;
import org.biopax.paxtools.model.level3.Stoichiometry;

public final class Upgrader {
	private static final Log log = LogFactory.getLog(Upgrader.class);
	
	public EditorMap editorMap2;
	public EditorMap editorMap3;
	private BioPAXFactory factory;
	private Properties classesmap;
	private Properties propsmap;
	
	public Upgrader() throws IOException {
		editorMap2 = new SimpleEditorMap(BioPAXLevel.L2);
		editorMap3 = new SimpleEditorMap(BioPAXLevel.L3);
		factory = BioPAXLevel.L3.getDefaultFactory();
		classesmap = new Properties();
		classesmap.load(getClass().getResourceAsStream("classesmap.properties"));
		propsmap = new Properties();
		propsmap.load(getClass().getResourceAsStream("propsmap.properties"));
	}
	
	/**
	 * Converts a BioPAX Level 2 Model to the Level 3.
	 *
	 * @param model2
	 * @return
	 */
	public Model convert(Model model) {
		if(model == null || model.getLevel() == BioPAXLevel.L3) 
		{
			return model; // nothing to do
		}
		
		// do convert
		// Note: take special care of pEPs (mapping PHYSICAL-ENTITY to entityReference and adding Stoichiometry) 
		// and CVs!
		final Model newModel = factory.createModel();
		
		// map objects (without properties, except for the ID only),
		// also skipping pEPs
		for(BioPAXElement bpe : model.getObjects()) {
			mapClass(bpe, newModel);
		}
		
		if(log.isTraceEnabled()) {
			log.trace("new model gets " 
					+ newModel.getObjects().size()
					+ " BioPAX individuals.");
		}
		
		// implement a special Model Traverser
		AbstractTraverser mapper = new AbstractTraverser(editorMap2)
		{
			@Override
			protected void visitValue(Object value, 
					BioPAXElement parent, Model model, PropertyEditor editor) 
			{
				String parentType = parent.getModelInterface().getSimpleName();
				String newProp = propsmap.getProperty(editor.getProperty());
				BioPAXElement newParent = newModel.getByID(parent.getRDFId());
				Object newValue = null;
				
				if(value instanceof physicalEntityParticipant) 
				{
					// becomes a PhysicalEntity with the EntityReference 
					// and links to the Stoichiometry (and the interaction)
					newValue = convertPep((physicalEntityParticipant)value, parent, newModel);
				}
				else if(value instanceof BioPAXElement) 
				{ 
					BioPAXElement bpe = (BioPAXElement) value;
					String id = bpe.getRDFId();
					// find the L3 element for the value
					newValue = newModel.getByID(id);
				}
				else 
				{ // set "simple" parent property (Primitive or Enum value)
					newValue = value;
				}
				
				if (newParent != null) {
					
					if(newValue == null) {
						log.warn("Converted to NULL: " + value
								+ " (using " + parentType
								+ "." + editor.getProperty() + 
								" => " + newParent 
								+ "." + newProp + ")");
					}
					
					for(PropertyEditor newEditor : editorMap3.getEditorsOf(newParent))
					{
						if(newEditor.getProperty().equals(newProp)) {
							newEditor.setPropertyToBean(newParent, newValue);
						}
					}
				} else {
					log.error("Of the value " + value + 
							", parent element is not found in the new model: "
						+ parentType + " : " + parent);
				}
			}

		};
		
		mapper.run(null, model);
		
		return newModel;
	}

	
	private BioPAXElement mapClass(BioPAXElement bpe, Model model3) {
		BioPAXElement newElement = null;
		String id = bpe.getRDFId();
		String type = bpe.getModelInterface().getSimpleName();
		String newType = classesmap.getProperty(type);
		
		if (newType!=null && factory.canInstantiate(newType)) {
			newElement = factory.reflectivelyCreate(newType);
			newElement.setRDFId(id);
			model3.add(newElement);
		} else {
			log.warn("Cannot convert " + type 
					+ " (" + id + ")"
					+ " to " + newType);
		}
		
		return newElement;
	}

	private BioPAXElement convertPep(physicalEntityParticipant value, BioPAXElement parent, Model model3) {
		PhysicalEntity pe3 = null;
		
		// 1. get PhysicalEntity (now can be found by ID because it's mapped earlier); 
		physicalEntity pe2 = value.getPHYSICAL_ENTITY();
		pe3 = (PhysicalEntity) model3.getByID(pe2.getRDFId());
		if(pe3 == null) {
			log.warn("Cannot find converted " + pe2.getModelInterface()
					+ " (" + pe2.getRDFId() + ")");
			return null;
		}
		
		//set location and other props
		String cvId = value.getCELLULAR_LOCATION().getRDFId();
		// the CellularLocationVocabulary is already mapped and has the same ID:
		pe3.setCellularLocation((CellularLocationVocabulary) model3.getByID(cvId));
		pe3.setComment(value.getCOMMENT());
		
		//traverse(bpe, model); // continue with properties
			
		// 1. create the corresponding EtityReference and set (if any) sequence, xrefs, etc.
		if(pe3 instanceof Protein) {
			ProteinReference pref = factory.reflectivelyCreate(ProteinReference.class);
			
		} else if(pe3 instanceof DnaRegion) {
			DnaRegionReference dref = factory.reflectivelyCreate(DnaRegionReference.class);
		} else if (pe3 instanceof RnaRegion) {
			RnaRegionReference rref = factory.reflectivelyCreate(RnaRegionReference.class);
		} else if (pe3 instanceof SmallMolecule) {
			SmallMoleculeReference smolref = factory.reflectivelyCreate(SmallMoleculeReference.class);
		} else if (pe3 instanceof Complex) {
			// no complex references exist
		} else {
			//oops... 
		}
		
		
		// 3. create Stoichiometry element; populate with the PE and coefficient, and add to the parent (interaction)
		double coeff = value.getSTOICHIOMETRIC_COEFFICIENT();
		if(coeff != BioPAXElement.UNKNOWN_DOUBLE) {
			Stoichiometry stoichiometry = factory.reflectivelyCreate(Stoichiometry.class);
			stoichiometry.setRDFId(pe3.getRDFId() + "-stoichiometry");
		}
		
		if(log.isTraceEnabled()) {
			log.trace("Cnverting pEP: " + value + " property "
					+ " of " +	parent);
		}
		
		return pe3;
	}
	
}
