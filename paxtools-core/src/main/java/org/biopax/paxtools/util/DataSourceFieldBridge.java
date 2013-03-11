/**
 * 
 */
package org.biopax.paxtools.util;

import org.apache.lucene.document.Document;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.Provenance;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

import java.util.Set;

/**
 * @author rodche //TODO annotate
 *
 */
public final class DataSourceFieldBridge implements FieldBridge {

	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) 
	{
		if (value instanceof Provenance) {
			//save to index 
			setForProvenance(name, (Provenance)value, document, luceneOptions);
		} else if (value instanceof Entity) {
			//Entities have dataSource property, use it
			Entity ent = (Entity) value;
			set(name, ent.getDataSource(), document, luceneOptions);
		} else if (value instanceof EntityReference) {
			// Let ERs inherit its dataSource from parent PEs or ERs:
			EntityReference er = (EntityReference) value;
			set(name, er.getEntityReferenceOf(), document, luceneOptions);
			set(name, er.getMemberEntityReferenceOf(), document, luceneOptions);
		} else if (value instanceof Set) {
			for (Object o : (Set) value) {
				set(name, o, document, luceneOptions);
			}
		} else if (value != null) {
			throw new IllegalArgumentException("Not applicable to: "
					+ value.getClass());
		}
	}

	private void setForProvenance(String name, Provenance value,
			Document document, LuceneOptions luceneOptions) 
	{
		// do not do .toLowerCase() for the URI!
		FieldBridgeUtils.addFieldToDocumentAsIs(luceneOptions, name, value.getRDFId(), document);
		
		for (String s : value.getName()) {
			FieldBridgeUtils.addFieldToDocument(luceneOptions, name, s, document);
		}
	}
	
}
