/**
 * 
 */
package org.biopax.paxtools.util;

import org.apache.lucene.document.Document;
import org.biopax.paxtools.model.level3.Provenance;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

/**
 * @author rodche
 *
 */
public final class ProvenanceFieldBridge implements FieldBridge {

	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		Provenance ds = (Provenance) value;
		luceneOptions.addFieldToDocument(name, ds.getRDFId().toLowerCase(), document);
		for (String s : ds.getName()) {
			luceneOptions.addFieldToDocument(name, s.toLowerCase(), document);
		}
	}
	
}
