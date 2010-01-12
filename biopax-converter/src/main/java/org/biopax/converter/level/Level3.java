package org.biopax.converter.level;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.PrimitivePropertyEditor;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.io.simpleIO.SimpleEditorMap;
import org.biopax.paxtools.io.simpleIO.SimpleExporter;
import org.biopax.paxtools.io.simpleIO.SimpleReader;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.Level2Element;
import org.biopax.paxtools.model.level2.complex;
import org.biopax.paxtools.model.level2.conversion;
import org.biopax.paxtools.model.level2.dataSource;
import org.biopax.paxtools.model.level2.dna;
import org.biopax.paxtools.model.level2.openControlledVocabulary;
import org.biopax.paxtools.model.level2.pathway;
import org.biopax.paxtools.model.level2.pathwayStep;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.protein;
import org.biopax.paxtools.model.level2.rna;
import org.biopax.paxtools.model.level2.sequenceFeature;
import org.biopax.paxtools.model.level2.sequenceParticipant;
import org.biopax.paxtools.model.level2.smallMolecule;
import org.biopax.paxtools.model.level3.CellularLocationVocabulary;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.ControlledVocabulary;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.DnaRegion;
import org.biopax.paxtools.model.level3.EntityFeature;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.Provenance;
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
		
		final Model newModel = factory.createModel();
		
		// map some objects (without properties, except for the ID),
		// also skipping CVs!
		for(BioPAXElement bpe : model.getObjects()) {
			BioPAXElement l3element = mapClass(bpe);
			if(l3element != null) {
				newModel.add(l3element);
			} else {
				if(log.isDebugEnabled())
					log.debug("skipping " + bpe + " " + bpe.getModelInterface().getSimpleName());
			}
		}
		
		// process all the elements (map properties)
		(new Mapper(newModel)).run(null, model);

		if(log.isTraceEnabled()) {
			log.trace("new model gets " 
					+ newModel.getObjects().size()
					+ " BioPAX individuals.");
		}
		
		return newModel;
	}

	// creates L3 classes and sets IDs
	private BioPAXElement mapClass(BioPAXElement bpe) {
		BioPAXElement newElement = null;
		String type = bpe.getModelInterface().getSimpleName();
		String id = bpe.getRDFId();
		
		if(bpe instanceof physicalEntityParticipant) {
			// dynamic mapping for pEPs
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
				return null;
			}
		} else if(bpe instanceof openControlledVocabulary) {
			return null; // will be treated later (in a specific context)
		}
		else // static mapping (using classesmap.properties) for other types
		{
			String newType = classesmap.getProperty(type);
			if (newType != null && factory.canInstantiate(newType)) {
				newElement = factory.reflectivelyCreate(newType);
			} else {
				if(log.isDebugEnabled()) 
					log.debug("No mapping found for " + type);
				return null;
			}
		}
		newElement.setRDFId(id);
		
		return newElement;
	}

	
	/**
	 * This can be called only after all the mapClass(bpe) and adding new elements is done,
	 * so that all the EntityReference instances have been created for the corresponding physical entities. 
	 * 
	 * parent - interaction, complex, or experimentalForm
	 **/
	private BioPAXElement convertPep(physicalEntityParticipant value, Level2Element parent, Model model3) {	
		Mapper mapper = new Mapper(model3); // is required here in a few special cases
		
		// 1) get old and new physical entities
		PhysicalEntity pe3 = (PhysicalEntity) model3.getByID(value.getRDFId()); // by pEP's ID!
		physicalEntity pe2 = value.getPHYSICAL_ENTITY();
		
		// 2) set those properties that do not map automatically 
		// when converting pE->ER and pEP->PE
		
		// comment
		pe3.setComment(value.getCOMMENT());
		
		// cellular location (also map the CV)
		openControlledVocabulary ocv = value.getCELLULAR_LOCATION();
		if(ocv != null) {
			mapper.visitValue(ocv, value, null, null);
			pe3.setCellularLocation((CellularLocationVocabulary)
					model3.getByID(ocv.getRDFId()));
		}
		
		// set sequence features (if any)
		if(value instanceof sequenceParticipant) {
			for(sequenceFeature sf : ((sequenceParticipant)value).getSEQUENCE_FEATURE_LIST()) {
				EntityFeature f = (EntityFeature) model3.getByID(sf.getRDFId());
				((SimplePhysicalEntity)pe3).addFeature(f);
			}
		}
		
		// availability
		pe3.setAvailability(pe2.getAVAILABILITY());
		
		// dataSource
		for(dataSource ds : pe2.getDATA_SOURCE()) {
			pe3.addDataSource((Provenance) model3.getByID(ds.getRDFId()));
		}
		
		// 3) set the corresponding EtityReference
		if (pe3 instanceof SimplePhysicalEntity) {
			EntityReference eref = (EntityReference) model3.getByID(pe2.getRDFId());
			((SimplePhysicalEntity)pe3).setEntityReference(eref);
		}
		
		if(pe3 instanceof Complex) {
			if(log.isDebugEnabled())
				log.debug("Resetting complex' rdf:ID from pEP's to PE's one: " 
						+ pe2.getRDFId());
			pe3.setRDFId(pe2.getRDFId());
		}
		
		// 4) create Stoichiometry element; set the PE and coefficient; add to the parent interaction
		float coeff = (float) value.getSTOICHIOMETRIC_COEFFICIENT();
		if(coeff != BioPAXElement.UNKNOWN_DOUBLE && parent instanceof conversion) {
			Stoichiometry stoichiometry = factory.reflectivelyCreate(Stoichiometry.class);
			stoichiometry.setRDFId(pe3.getRDFId() + "-stoichiometry");
			stoichiometry.setStoichiometricCoefficient(coeff);
			stoichiometry.setPhysicalEntity(pe3);
			model3.add(stoichiometry);
			Conversion conv = (Conversion) model3.getByID(parent.getRDFId());
			conv.addParticipantStoichiometry(stoichiometry);
		} else if(coeff != BioPAXElement.UNKNOWN_DOUBLE) {
			if(log.isDebugEnabled())
				log.debug("STOICHIOMETRIC_COEFFICIENT is " 
						+ coeff + ", but the pEP parent is not a conversion - "
						+ parent);
		}
				
		return pe3;
	}
	
	
	/* 
	 * Creates a specific ControlledVocabulary subclass 
	 * and adds to the new model
	 */
	private ControlledVocabulary convertCv(openControlledVocabulary value,
			Level2Element parent, Model newModel, PropertyEditor editor) {
		ControlledVocabulary cv = null;
		String cvId = ((BioPAXElement) value).getRDFId();
		
		if (!newModel.containsID(cvId)) {
			BioPAXElement newParent = newModel.getByID(parent.getRDFId());
			String newProp = propsmap.getProperty(editor.getProperty());
			// map oCVs to the Level3 CVs using the property context
			PropertyEditor newEditor = editorMap3.getEditorForProperty(
					newProp, newParent.getModelInterface());
			if (newEditor != null) {
				cv = (ControlledVocabulary) factory.reflectivelyCreate(newEditor.getRange());
				cv.setRDFId(cvId);
				newModel.add(cv);
			} else {
				log.warn("Cannot Convert CV: " + value
					+ " (prop.: " + editor.getProperty()
					+ " => " + newProp + ")");
			}
		}
		
		return cv;
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
			if(editor != null && editor.isUnknown(value)) {
				return;
			}
			
			// pE, pEP and openCV can be only mapped within known context 
			//(i.e. being value of another element)
			if(parent instanceof physicalEntityParticipant
					|| parent instanceof openControlledVocabulary) {
				if(log.isDebugEnabled())
					log.debug("Skipping all properties of "
					+ parent + " (" + parent.getModelInterface().getSimpleName()	
					+ "); special care is required.");
				return;
			}
			
			Object newValue = value;
			
			if(value instanceof physicalEntityParticipant) 
			{
				newValue = convertPep((physicalEntityParticipant)value, 
						(Level2Element) parent, newModel);
			}
			else if(value instanceof openControlledVocabulary) 
			{
				// create the proper type ControlledVocabulary instance	
				newValue = convertCv((openControlledVocabulary)value, 
						(Level2Element)parent, newModel, editor);
			}
			else if(value instanceof BioPAXElement) 
			// other classes were mapped in advance 
			{ 
				BioPAXElement bpe = (BioPAXElement) value;
				String id = bpe.getRDFId();
				// find the L3 element for the value
				newValue = newModel.getByID(id);
			}
			
			String parentType = parent.getModelInterface().getSimpleName();
			String newProp = propsmap.getProperty(editor.getProperty());
			// special case (PATHWAY-COMPONENTS maps to pathwayComponent or pathwayOrder)
			if(parent instanceof pathway && value instanceof pathwayStep
					&& editor.getProperty().equals("PATHWAY-COMPONENTS")) {
				newProp = "pathwayOrder";
			}
			
			BioPAXElement newParent = newModel.getByID(parent.getRDFId());
			if (newParent != null && newProp != null) {
				if(newValue == null) {
					log.warn(value + " becomes NULL: "
						+ " ("+ parentType + "." + editor.getProperty()
						+ " => " + newParent.getRDFId()	+ "." + newProp 
						+ ")");
				}
				PropertyEditor newEditor = 
					editorMap3.getEditorForProperty(newProp, newParent.getModelInterface());
				if(newEditor != null){
					if(newEditor instanceof PrimitivePropertyEditor) {
						newValue = newValue.toString();
					}
					newEditor.setPropertyToBean(newParent, newValue);
				}
			} else if(newProp ==null){
				log.warn("No mapping defined for property: " 
						+ parentType + "."
						+ editor.getProperty());
			} else {
				log.error("Of the value " + value + 
						", parent element is not found in the new model: "
					+ parentType + " : " + parent);
			}
		}

	}
	
}
