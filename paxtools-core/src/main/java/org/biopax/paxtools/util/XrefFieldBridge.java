/**
 * 
 */
package org.biopax.paxtools.util;

import java.util.Set;

import org.apache.lucene.document.Document;
import org.biopax.paxtools.model.level3.Xref;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

/**
 * @author rodche
 *
 */
public final class XrefFieldBridge implements FieldBridge {

	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		Set<Xref> refs = (Set<Xref>) value;	
		for (Xref x : refs) {
			if(x.getId() != null)
				FieldBridgeUtils.addFieldToDocument(luceneOptions, name, x.getId(), document);
		}
	}
	
}
