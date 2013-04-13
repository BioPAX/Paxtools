/**
 * 
 */
package org.biopax.paxtools.util;

import org.apache.lucene.document.Document;
import org.biopax.paxtools.model.level3.Provenance;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

import java.util.Set;

/**
 * 
 * A FieldBridge implementation 
 * for including dataSource (Provenance) names and URIs
 * to the BioPAX element's Lucene index.
 * 
 * @author rodche
 *
 */
public final class DataSourceFieldBridge implements FieldBridge {

	@Override
	public void set(String searchFieldName, Object value, Document document, LuceneOptions luceneOptions) 
	{
		if (value instanceof Set) {
			for (Provenance p : (Set<Provenance>) value) {
				// do not do .toLowerCase() for the URI!
				FieldBridgeUtils.addFieldToDocumentAsIs(luceneOptions, searchFieldName, p.getRDFId(), document);
				// index names as well
				for (String s : p.getName())
					FieldBridgeUtils.addFieldToDocument(luceneOptions, searchFieldName, s, document);
			}
		} else {
			throw new AssertionError("bug!");
		}
	}	
}
