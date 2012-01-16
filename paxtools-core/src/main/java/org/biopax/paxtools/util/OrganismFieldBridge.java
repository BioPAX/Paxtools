/**
 * 
 */
package org.biopax.paxtools.util;

import java.util.Set;

import org.apache.lucene.document.Document;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.Gene;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.SequenceEntityReference;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.Xref;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

/**
 * A muli-domain FieldBridge implementation 
 * for extracting and indexing organism names/ids
 * from other BioPAX properties (incl. some inverse properties),
 * such as 'participant', 'component', etc. E.g., when applied to
 * interaction.participant, it follows one of biopax property paths to get organisms:
 * - participant:Complex/component:SimplePhysicalEntity/entityReference/organism
 * - participant:SimplePhysicalEntity/entityReference/organism
 * - participant:Gene/organism
 * - participant:Pathway/organism
 * - participant:Interaction/participant...
 * etc.
 * 
 * (hope, there are no infinite loops :))
 * 
 * @author rodche
 *
 */
public final class OrganismFieldBridge implements FieldBridge {

	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		if (value instanceof BioSource) {
			setOrganism(name, (BioSource)value, document, luceneOptions);
		} else if (value instanceof Pathway) {
			setOrganism(name, ((Pathway) value).getOrganism(), document, luceneOptions);
		} else if (value instanceof Interaction) {
			set(name, ((Interaction) value).getPathwayComponentOf(), document, luceneOptions); //go to Pathways
			set(name, ((Interaction) value).getParticipant(), document, luceneOptions); // to controlled interactions
		} else if (value instanceof SequenceEntityReference) {
			setOrganism(name, ((SequenceEntityReference) value).getOrganism(), document, luceneOptions);
		} else if (value instanceof SimplePhysicalEntity) {
			set(name, ((SimplePhysicalEntity) value).getEntityReference(), document, luceneOptions);
		} else if (value instanceof Gene) {
			setOrganism(name, ((Gene) value).getOrganism(), document, luceneOptions);
		} else if (value instanceof Set) {
			for (Object o : (Set) value) {
				set(name, o, document, luceneOptions);
			}
		} else if (value != null) {
			//Not applicable to value.getClass(); it's ok to ignore
		}
	}

	
	private void setOrganism(String name, BioSource bs,
			Document document, LuceneOptions luceneOptions) 
	{
		// put id (e.g., urn:miriam:taxonomy:9606, if normalized...)
		// (toLowerCase() is very important here and there...it's long to explain)
		luceneOptions.addFieldToDocument(name, bs.getRDFId().toLowerCase(), document);
		
		// add organism names
		for(String s : bs.getName()) {
			luceneOptions.addFieldToDocument(name, s.toLowerCase(), document);
		}
		// add taxonomy
		for(UnificationXref x : 
			new ClassFilterSet<Xref,UnificationXref>(bs.getXref(), UnificationXref.class)) {
			if(x.getId() != null)
				luceneOptions.addFieldToDocument(name, x.getId().toLowerCase(), document);
		}
		// include tissue type terms
		if (bs.getTissue() != null) {
			for (String s : bs.getTissue().getTerm()) {
				luceneOptions.addFieldToDocument(name, s.toLowerCase(), document);
			}
		}
		// include cell type terms
		if (bs.getCellType() != null) {
			for (String s : bs.getCellType().getTerm()) {
				luceneOptions.addFieldToDocument(name, s.toLowerCase(), document);
			}
		}
	}

}
