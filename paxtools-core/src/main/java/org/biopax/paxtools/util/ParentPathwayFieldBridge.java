/**
 * 
 */
package org.biopax.paxtools.util;

import java.util.Set;

import org.apache.lucene.document.Document;
import org.biopax.paxtools.model.level3.*;
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
 * @author rodche
 */
public final class ParentPathwayFieldBridge implements FieldBridge {

	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		if(value instanceof Pathway) {
			setForPathway(name, (Pathway)value, document, luceneOptions);
			set(name, ((Pathway)value).getPathwayComponentOf(), document, luceneOptions);
// TODO does participation of a Pathway in a Control interaction mean being sub-pathway? so - stop!
//			set(name, ((Pathway)value).getParticipantOf(), document, luceneOptions);
		} else if(value instanceof Interaction) {
			set(name, ((Interaction)value).getPathwayComponentOf(), document, luceneOptions);
// TODO getParticipantOf() usually leads to the same parent pathways as getPathwayComponentOf(), but can also cross into a peer pathway; so - stop!  
//			set(name, ((Interaction)value).getParticipantOf(), document, luceneOptions);
		} else if(value instanceof PhysicalEntity ) {
			set(name, ((PhysicalEntity)value).getMemberPhysicalEntityOf(), document, luceneOptions);
			set(name, ((PhysicalEntity)value).getParticipantOf(), document, luceneOptions);
			set(name, ((PhysicalEntity)value).getComponentOf(), document, luceneOptions);
		} else if(value instanceof EntityReference) {
			set(name, ((EntityReference)value).getMemberEntityReference(), document, luceneOptions);
			set(name, ((EntityReference)value).getEntityReferenceOf(), document, luceneOptions);
		} else if(value instanceof UnificationXref) {
			set(name, ((UnificationXref)value).getXrefOf(), document, luceneOptions);
		} else if(value instanceof Set) {
			for(Object e : (Set) value) {
				set(name, e, document, luceneOptions);
			}
		} else if(value != null) {
			// ignore (it's ok not to throw IAE here, because of the xrefOfs...)
			//throw new IllegalArgumentException("Cannot use with: " + value.getClass());
		}
	}
	
	private void setForPathway(String name, Pathway pw,
			Document document, LuceneOptions luceneOptions) 
	{	
		luceneOptions.addFieldToDocument(name, pw.getRDFId().toLowerCase(),
				document);

		for (String s : pw.getName()) {
			luceneOptions.addFieldToDocument(name, s.toLowerCase(), document);
		}

		for (UnificationXref x : new ClassFilterSet<Xref, UnificationXref>(
				pw.getXref(), UnificationXref.class)) {
			if (x.getId() != null)
				luceneOptions.addFieldToDocument(name, x.getId().toLowerCase(),
						document);
		}
	}
	
}
