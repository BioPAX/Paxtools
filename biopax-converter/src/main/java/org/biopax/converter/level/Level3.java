package org.biopax.converter.level;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.io.simpleIO.SimpleEditorMap;
import org.biopax.paxtools.io.simpleIO.SimpleExporter;
import org.biopax.paxtools.io.simpleIO.SimpleReader;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.complex;
import org.biopax.paxtools.model.level2.dna;
import org.biopax.paxtools.model.level2.openControlledVocabulary;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.protein;
import org.biopax.paxtools.model.level2.rna;
import org.biopax.paxtools.model.level2.sequenceFeature;
import org.biopax.paxtools.model.level2.sequenceParticipant;
import org.biopax.paxtools.model.level2.smallMolecule;
import org.biopax.paxtools.model.level3.CellularLocationVocabulary;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.DnaRegion;
import org.biopax.paxtools.model.level3.EntityFeature;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.RnaRegion;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.model.level3.SmallMolecule;
import org.biopax.paxtools.model.level3.Stoichiometry;

/**
 * This class can be used from console (with arguments) or 
 * other classes to perform BioPAX Level 1,2 to Level 3 conversion.
 */
public final class Level3 {
	private static final Log log = LogFactory.getLog(Level3.class);
	
	public static EditorMap editorMap2;
	public static EditorMap editorMap3;
	private BioPAXFactory factory;
	private Properties classesmap;
	private Properties propsmap;
	
	/**
	 * Constructor (it also loads 'classesmap' and 'propsmap' from property files)
	 * @throws IOException
	 */
	public Level3() throws IOException {
		editorMap2 = new SimpleEditorMap(BioPAXLevel.L2);
		editorMap3 = new SimpleEditorMap(BioPAXLevel.L3);
		factory = BioPAXLevel.L3.getDefaultFactory();
		classesmap = new Properties();
		classesmap.load(getClass().getResourceAsStream("classesmap.properties"));
		propsmap = new Properties();
		propsmap.load(getClass().getResourceAsStream("propsmap.properties"));
	}
	
	/**
	 * @param args biopax file names to convert
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		for (String filename : args) {
			SimpleReader reader = new SimpleReader();
			Model model = reader.convertFromOWL(new FileInputStream(filename));
			Level3 level3 = new Level3();
			//convert
			model = level3.convert(model);
			if (model != null) {
				SimpleExporter exporter = new SimpleExporter(model.getLevel());
				exporter.convertToOWL(model, System.out);
			}
		}
	}
	
	/**
	 * Converts a BioPAX Model, Level 1 or 2, to the Level 3.
	 *
	 * @param model
	 * @return
	 */
	public Model convert(Model model) {
		if(model == null || model.getLevel() != BioPAXLevel.L2) 
		{
			if(model != null) 
				log.info("Model is " + model.getLevel());
			return model; // nothing to do
		}
		
		// Note: take special care of pEPs (mapping PHYSICAL-ENTITY to entityReference and adding Stoichiometry) 
		// and CVs!
		final Model newModel = factory.createModel();
		
		// map some objects (without properties, except for the ID only),
		// skipping pEPs and CVs!
		for(BioPAXElement bpe : model.getObjects()) {
			BioPAXElement l3element = mapClass(bpe);
			newModel.add(l3element);
		}
		
		// map all values
		(new Mapper(newModel)).run(null, model);

		if(log.isTraceEnabled()) {
			log.trace("new model gets " 
					+ newModel.getObjects().size()
					+ " BioPAX individuals.");
		}
		
		return newModel;
	}

	
	private BioPAXElement mapClass(BioPAXElement bpe) {
		BioPAXElement newElement = null;
		String type = bpe.getModelInterface().getSimpleName();
		String id = bpe.getRDFId();
		
		if(bpe instanceof physicalEntityParticipant) {
			// dynamic mapping
			physicalEntity pe2 = ((physicalEntityParticipant)bpe).getPHYSICAL_ENTITY();
			if(pe2 instanceof protein) {
				newElement = factory.reflectivelyCreate(Protein.class);	
			} else if(pe2 instanceof dna) {
				newElement = factory.reflectivelyCreate(DnaRegion.class);
			} else if (pe2 instanceof rna) {
				newElement = factory.reflectivelyCreate(RnaRegion.class);
			} else if (pe2 instanceof smallMolecule) {
				newElement = factory.reflectivelyCreate(SmallMolecule.class);
			} else if (pe2 instanceof complex) {
				newElement = factory.reflectivelyCreate(Complex.class);
			} else {
				log.warn("Cannot map pEP (" + id + ") to L3 "
					+ pe2.getModelInterface());
				return null;
			}
		} 
		else // static mapping (using the table)
		{
			String newType = classesmap.getProperty(type);
			if (newType != null && factory.canInstantiate(newType)) {
				newElement = factory.reflectivelyCreate(newType);
			} else {
				log.warn("Cannot convert " + type + " (" + id + ")" + " to "
						+ newType);
				return null;
			}
		}
		newElement.setRDFId(id);
		
		return newElement;
	}

	
	/**
	 * This should be called after all the mapClass(bpe) and adding to the new model have been called,
	 * so that all the entity references have been created for the corresponding L2 physical entities. 
	 **/
	private BioPAXElement convertPep(physicalEntityParticipant value, BioPAXElement parent, Model model3) {		
		// 1) get old and new physical entities
		PhysicalEntity pe3 = (PhysicalEntity) model3.getByID(value.getRDFId()); // by pEP's ID!
		physicalEntity pe2 = value.getPHYSICAL_ENTITY();
		
		// 2) match pEP's properties:
		
		// cellular location
		openControlledVocabulary ocv = value.getCELLULAR_LOCATION();
		(new Mapper(model3)).visitValue(ocv, value, null, null);
		pe3.setCellularLocation((CellularLocationVocabulary) model3.getByID(ocv.getRDFId()));
		
		// set comment
		pe3.setComment(value.getCOMMENT());
		
		// set sequence features (if any)
		if(value instanceof sequenceParticipant) {
			for(sequenceFeature sf : ((sequenceParticipant)value).getSEQUENCE_FEATURE_LIST()) {
				EntityFeature f = (EntityFeature) model3.getByID(sf.getRDFId());
				((SimplePhysicalEntity)pe3).addFeature(f);
			}
		}
		
		// 3) set the corresponding EtityReference
		if (pe3 instanceof SimplePhysicalEntity) {
			EntityReference eref = (EntityReference) model3.getByID(pe2.getRDFId());
			((SimplePhysicalEntity)pe3).setEntityReference(eref);
		}
		
		// 4) create Stoichiometry element; set the PE and coefficient; add to the parent interaction
		double coeff = value.getSTOICHIOMETRIC_COEFFICIENT();
		if(coeff != BioPAXElement.UNKNOWN_DOUBLE) {
			Stoichiometry stoichiometry = factory.reflectivelyCreate(Stoichiometry.class);
			stoichiometry.setRDFId(pe3.getRDFId() + "-stoichiometry");
			
			// TODO find and assign to the interaction...
		}
		
		if(log.isTraceEnabled()) {
			log.trace("Cnverting pEP: " + value + " property "
					+ " of " +	parent);
		}
		
		return pe3;
	}
	
	 
	class Mapper extends AbstractTraverser {	
		Model newModel;
		
		public Mapper(Model mapTo) {
			super(editorMap2);
			this.newModel = mapTo;
		}

		@Override
		public void visitValue(Object value, BioPAXElement parent, Model model, PropertyEditor editor) 
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
			else if(value instanceof openControlledVocabulary) 
			{
				// map oCVs to the Level3 CVs using the property context
				PropertyEditor newEditor = editorMap3.getEditorForProperty(newProp, newParent.getModelInterface());
				if(newEditor != null) {
					BioPAXElement cv = factory.reflectivelyCreate(newEditor.getRange());
					cv.setRDFId(((BioPAXElement) value).getRDFId());
					newModel.add(cv);
					newValue = cv;
				} else {
					log.warn("Cannot Convert CV: " + value
							+ " (prop.: " 
							+ editor.getProperty() + 
							" => " + newProp + ")");
				}
			}
			else if(value instanceof BioPAXElement) 
			// other classes were mapped in advance 
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

	}
	
}
