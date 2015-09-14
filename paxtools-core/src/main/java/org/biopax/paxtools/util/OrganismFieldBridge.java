/**
 * 
 */
package org.biopax.paxtools.util;

import java.util.Set;

import org.apache.lucene.document.Document;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.Xref;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

/**
 * A FieldBridge implementation 
 * for including organism (BioSource) names, ids, terms
 * to parent BioPAX element's index.
 * 
 * @author rodche
 * @deprecated Hibernate ORM/Search will be removed in v5
 */
public final class OrganismFieldBridge implements FieldBridge {

	@Override
	public void set(String searchFieldName, Object value, Document document, LuceneOptions luceneOptions) {
		if (value instanceof Set) {
			for (BioSource o : (Set<BioSource>) value) {
				if(o != null)
					setOrganism(searchFieldName, o, document, luceneOptions);
			}
		} else {
			throw new AssertionError("bug!");
		}
	}

	
	private void setOrganism(String searchFieldName, BioSource bs,
			Document document, LuceneOptions luceneOptions) 
	{
		// put id (e.g., urn:miriam:taxonomy:9606, if normalized...)
		// do not do .toLowerCase()!
		FieldBridgeUtils.addFieldToDocumentAsIs(luceneOptions, searchFieldName, bs.getRDFId(), document);
		
		// add organism names
		for(String s : bs.getName()) {
			FieldBridgeUtils.addFieldToDocument(luceneOptions, searchFieldName, s, document);
		}
		// add taxonomy
		for(UnificationXref x : 
			new ClassFilterSet<Xref,UnificationXref>(bs.getXref(), UnificationXref.class)) {
			if(x.getId() != null)
				FieldBridgeUtils.addFieldToDocument(luceneOptions, searchFieldName, x.getId(), document);
		}
		// include tissue type terms
		if (bs.getTissue() != null) {
			for (String s : bs.getTissue().getTerm()) {
				FieldBridgeUtils.addFieldToDocument(luceneOptions, searchFieldName, s, document);
			}
		}
		// include cell type terms
		if (bs.getCellType() != null) {
			for (String s : bs.getCellType().getTerm()) {
				FieldBridgeUtils.addFieldToDocument(luceneOptions, searchFieldName, s, document);
			}
		}
	}
	
}
