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
 * This Hibernate search field bridge implementation is to be applied 
 * to the 'dataSource' object properties of Entity, EntityFeature,
 * EntityReference, PathwayStep classes in order to automatically 
 * build such full-text index field that then allows finding and filtering 
 * these biopax objects by Provenance's names or URI. This is very convenient 
 * and greatly improves biopax full-text search capabilities. For example, 
 * without this field bridge, one could still hit an instance of {@link Provenance}
 * by name, but then would have to look up for its parents, i.e., to get the entities,
 * entity references or features, which is another and expensive 
 * (compared to searching) query. Whereas, having this (as well as similar bridges 
 * for other complex biopax properties), one can find all objects by data source
 * name or URI right away; can add filter by biopax class to get more specific hits, etc.
 * Because, having the declarative search model (i.e., annotations on java getter methods)
 * and custom field bridges like this ine all in place, Lucene indexer will do most of 
 * biopax inference (graph traversal) job for you and stores the results in the index, 
 * if necessary, in advance, and only once.
 * 
 * @author rodche
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

	/**
	 * Creates entries in the full-text index that correspond
	 * to the {@link Provenance} object's URI and names.
	 * 
	 * @param name search field name (e.g., 'datasource')
	 * @param value biopax property 'dataSource' value (Provenance object)
	 * @param document Lucene index document
	 * @param luceneOptions
	 */
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
