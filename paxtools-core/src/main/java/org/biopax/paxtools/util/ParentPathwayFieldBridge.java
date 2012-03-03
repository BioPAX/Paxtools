/**
 * 
 */
package org.biopax.paxtools.util;

import java.util.Set;

import org.apache.lucene.document.Document;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

/**
 * This is a special search field bridge implementation that allows user define
 * custom index fields for some biopax elements to be found or filtered by 
 * their parent pathway URIs and names. This bridge class can be used 
 * also with inverse biopax properties (part of the paxtools public api),  
 * such as pathwayComponentOf, participantOf, etc.
 * (hope, there are no infinite loops ;))
 * 
 * This one, specifically, helps to infer and index parent pathways 
 * (starting from various BioPAX properties, elements) and then use it
 * to find a physical entity or process by pathway identifier or name.
 * 
 * @author rodche
 */
public final class ParentPathwayFieldBridge implements FieldBridge {

	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		if(value instanceof Pathway) {
			setForPathway(name, (Pathway)value, document, luceneOptions);
			set(name, ((Pathway)value).getPathwayComponentOf(), document, luceneOptions);
			set(name, ((Process)value).getControlledOf(), document, luceneOptions);
// TODO does participation of a Pathway in a Control interaction mean being sub-pathway? so - stop!
//			set(name, ((Entity)value).getParticipantOf(), document, luceneOptions);
		} else if(value instanceof Interaction) {
			set(name, ((Interaction)value).getPathwayComponentOf(), document, luceneOptions);
			set(name, ((Process)value).getControlledOf(), document, luceneOptions);
// TODO getParticipantOf() usually leads to the same parent pathways as getPathwayComponentOf(), but can also cross into a peer pathway; so - stop!  
//			set(name, ((Entity)value).getParticipantOf(), document, luceneOptions);
		} else if(value instanceof PhysicalEntity ) {
			set(name, ((PhysicalEntity)value).getMemberPhysicalEntityOf(), document, luceneOptions);
			set(name, ((Entity)value).getParticipantOf(), document, luceneOptions);
			set(name, ((PhysicalEntity)value).getComponentOf(), document, luceneOptions);
		} else if(value instanceof EntityReference) {
			set(name, ((EntityReference)value).getMemberEntityReference(), document, luceneOptions);
			set(name, ((EntityReference)value).getEntityReferenceOf(), document, luceneOptions);
// this seems was a mistake... (never/shouldn't call)
//		} else if(value instanceof UnificationXref) {
//			set(name, ((UnificationXref)value).getXrefOf(), document, luceneOptions);
		} else if(value instanceof Set) {
			for(Object e : (Set) value) {
				set(name, e, document, luceneOptions);
			}
		}
	}

	/*
	 * Indexes pathway URI, names, identifiers
	 */
	private void setForPathway(String name, Pathway pw,
			Document document, LuceneOptions luceneOptions) 
	{	
		//adding URI as is (do not change the URI to lowercase!)
		luceneOptions.addFieldToDocument(name, pw.getRDFId(), document);

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
