/**
 * 
 */
package org.biopax.paxtools.util;

import java.util.Set;

import org.apache.lucene.document.Document;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.Xref;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

/**
 * This is a {@link FieldBridge} implementation 
 * for biopax elements to generate a custom search/filter 
 * index field that contain their parent pathway URIs and names. 
 * 
 * @author rodche
 * @deprecated Hibernate ORM/Search will be removed in v5
 */
public final class ParentPathwayFieldBridge implements FieldBridge {

	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		if(value instanceof Set) {
			for(Pathway pw : (Set<Pathway>) value) {
				//adding URI as is (do not change the URI to lowercase!)
				FieldBridgeUtils.addFieldToDocumentAsIs(luceneOptions, name, pw.getRDFId(), document);
				// add names to the index as well
				for (String s : pw.getName()) {
					FieldBridgeUtils.addFieldToDocument(luceneOptions, name, s, document);
				}
				// add unification xref IDs too
				for (UnificationXref x : new ClassFilterSet<Xref, UnificationXref>(
						pw.getXref(), UnificationXref.class)) {
					if (x.getId() != null)
						FieldBridgeUtils.addFieldToDocument(luceneOptions, name, x.getId(), document);
				}
			}
		} else {
			throw new AssertionError("Bug!");
		}
	}
}
