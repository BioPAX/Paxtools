/**
 * 
 */
package org.biopax.paxtools.util;

import java.util.Set;

import org.apache.lucene.document.Document;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.Provenance;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

/**
 * @author rodche
 *
 */
public final class DataSourceFieldBridge implements FieldBridge {

	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) 
	{
		if (value instanceof Provenance) {
			setForProvenance(name, (Provenance)value, document, luceneOptions);
		} else if (value instanceof Entity) {
			Entity ent = (Entity) value;
			set(name, ent.getDataSource(), document, luceneOptions);
		} else if (value instanceof EntityReference) {
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
		luceneOptions.addFieldToDocument(name, value.getRDFId().toLowerCase(), document);
		for (String s : value.getName()) {
			luceneOptions.addFieldToDocument(name, s.toLowerCase(), document);
		}
	}
	
}
