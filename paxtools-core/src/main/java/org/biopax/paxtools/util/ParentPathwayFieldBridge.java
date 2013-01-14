/**
 * 
 */
package org.biopax.paxtools.util;

import java.util.Set;

import org.apache.lucene.document.Document;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

/**
 * This is a {@link FieldBridge} implementation 
 * for biopax elements to generate a custom search/filter 
 * index field that contain their parent pathway URIs and names. 
 * 
 * This bridge class can be applied to an index field definition put
 * on either normal or inverse biopax object property (such as 
 * pathwayComponentOf, participantOf, stepProcessOf, etc.)
 * 
 * Not applicable to data type properties.
 * 
 * @author rodche
 */
public final class ParentPathwayFieldBridge implements FieldBridge {
	
	// recursive (more like a crawler or worm) method implementation
	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		/* Once we get here and got the value(s), field name, document, etc.,
		 * we will only look "UP" inverse biopax properties "direction" in order
		 * to reach all "parent" (associated) pathways! We'll never look "down" the
		 * normal biopax properties (if this is desirable, this field bridge can be simply 
		 * applied on the corresponding property in the corresponding biopax entity class implementation)
		 */
		if(value instanceof Pathway) {
			// store!
			setForPathway(name, (Pathway)value, document, luceneOptions);
			// continue looking up to parent pathways
			set(name, ((Process)value).getPathwayComponentOf(), document, luceneOptions);
			set(name, ((Process)value).getControlledOf(), document, luceneOptions); //for pathways, this is eq. to getParticipantOf()
			set(name, ((Process)value).getStepProcessOf(), document, luceneOptions);
		} else if(value instanceof PathwayStep) {
			set(name, ((PathwayStep)value).getPathwayOrderOf(), document, luceneOptions);
		} else if(value instanceof Interaction) { //not Control interactions
			set(name, ((Process)value).getPathwayComponentOf(), document, luceneOptions);
			set(name, ((Interaction)value).getParticipantOf(), document, luceneOptions); //for interactions, same as getControlledOf()
			set(name, ((Process)value).getStepProcessOf(), document, luceneOptions);
		} else if(value instanceof PhysicalEntity ) {
			set(name, ((PhysicalEntity)value).getMemberPhysicalEntityOf(), document, luceneOptions);
			set(name, ((Entity)value).getParticipantOf(), document, luceneOptions);
			set(name, ((PhysicalEntity)value).getComponentOf(), document, luceneOptions);
		} else if(value instanceof EntityReference) {
			set(name, ((EntityReference)value).getMemberEntityReferenceOf(), document, luceneOptions);
			set(name, ((EntityReference)value).getEntityReferenceOf(), document, luceneOptions);
		} else if(value instanceof Set) {
			//process each biopax value
			for(Object e : (Set) value) {
				if(e instanceof BioPAXElement)
					set(name, e, document, luceneOptions);
			}
		}
	}

	/*
	 * Indexes pathway URI, names, and unif. xref identifiers
	 */
	private void setForPathway(String name, Pathway pw,
			Document document, LuceneOptions luceneOptions) 
	{			
		//adding URI as is (do not change the URI to lowercase!)
		FieldBridgeUtils.addFieldToDocumentAsIs(luceneOptions, name, pw.getRDFId(), document);

		for (String s : pw.getName()) {
			FieldBridgeUtils.addFieldToDocument(luceneOptions, name, s, document);
		}

		for (UnificationXref x : new ClassFilterSet<Xref, UnificationXref>(
				pw.getXref(), UnificationXref.class)) {
			if (x.getId() != null)
				FieldBridgeUtils.addFieldToDocument(luceneOptions, name, x.getId(), document);
		}
	}
	
}
