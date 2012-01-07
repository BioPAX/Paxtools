/**
 * 
 */
package org.biopax.paxtools.util;

import org.apache.lucene.document.Document;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.Xref;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

/**
 * @author rodche
 *
 */
public final class BioSourceFieldBridge implements FieldBridge {

	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		BioSource bs = (BioSource) value;
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
