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
				/* toLowerCase() is important here and there; it's long to explain in full,
				 * but - it's about the @Field(index=Index.UN_TOKENIZED,..), MultiFieldQueryParser, 
				 * and StandardAnayzer to work well together... Rabbit's hole...
				 */
				luceneOptions.addFieldToDocument(name, x.getId().toLowerCase(), document);
		}
	}
	
}
