/**
 * 
 */
package org.biopax.paxtools.util;

import java.util.Set;

import org.apache.lucene.document.Document;
import org.biopax.paxtools.model.level3.XReferrable;
import org.biopax.paxtools.model.level3.Xref;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

/**
 * This Hibernate search field bridge implementation is to be applied 
 * to the 'xref' object properties of many biopax {@link XReferrable} classes, 
 * such as EntityReference, Protein, etc., and allows finding these objects 
 * by some identifier (e.g., UniProt, HGNC Symbol, etc.). Without this bridge, 
 * one could only find an instance of {@link Xref} by 'id' or URI.
 * 
 * @author rodche
 * @deprecated Hibernate ORM/Search will be removed in v5
 */
public final class XrefFieldBridge implements FieldBridge {

	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		if (value instanceof Set) { //is getXref() values
			Set<Xref> refs = (Set<Xref>) value;
			for (Xref x : refs) {
				if (x.getId() != null)
					FieldBridgeUtils.addFieldToDocument(luceneOptions, name,
							x.getId(), document); //note: this actually saves value.toLowercase
			}
		}
	}
	
}
