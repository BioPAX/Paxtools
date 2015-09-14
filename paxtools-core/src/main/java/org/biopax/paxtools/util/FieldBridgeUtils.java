package org.biopax.paxtools.util;


import org.apache.commons.lang.ArrayUtils;
import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

/**
 * This is a package-private static factory utility 
 * class to be called from all the custom {@link FieldBridge}
 * implementations in this package in order to consistently 
 * build and use biopax full-text search model.
 * 
 * @author rodche
 * @deprecated Hibernate ORM/Search will be removed in v5
 */
final class FieldBridgeUtils {
	private FieldBridgeUtils() {
		throw new AssertionError();
	}
	
	/**
	 * Adds the value (of some biopax property) to 
	 * given search field in the index document.
	 * 
	 * Private method.
	 * 
	 * @param luceneOptions
	 * @param field
	 * @param value
	 * @param document
	 * @param lowercase
	 */
	private static void addFieldToDocument(LuceneOptions luceneOptions, String field, 
			String value, Document document, boolean lowercase) {
		/* merely in all cases, must be lowercase=true (though it's hard to explain here why,
		 * but - it's about the @Field(analyze=Analyze.NO), i.e. UN_TOKENIZED, 
		 * MultiFieldQueryParser and StandardAnayzer to work well together...)
		 */
		String v = (lowercase) ?  value.toLowerCase() : value;
		if(!ArrayUtils.contains(document.getValues(field), v))
			luceneOptions.addFieldToDocument(field, v, document);

	}
	
	/**
	 * Adds the (biopax property) value, turning it to lower case, 
	 * to given search field in the Lucene index document.
	 * 
	 * Delegates to the following method, using lowercase=true:
	 * @see FieldBridgeUtils#addFieldToDocument(LuceneOptions, String, String, Document, boolean)
	 * 
	 * @param luceneOptions
	 * @param field search field name
	 * @param value string value to be indexed
	 * @param document Lucene document
	 */
	public static void addFieldToDocument(LuceneOptions luceneOptions, String field, 
			String value, Document document) {
		addFieldToDocument(luceneOptions, field, value, document, true);
	}
	
	/**
	 * Adds a value "as is" to the given search field in the index document.
	 * This method is mostly used to save URIs.
	 * 
	 * Delegates to the following method, using lowercase=false:
	 * @see FieldBridgeUtils#addFieldToDocument(LuceneOptions, String, String, Document, boolean)
	 * 
	 * 
	 * @param luceneOptions
	 * @param field
	 * @param value
	 * @param document
	 */
	public static void addFieldToDocumentAsIs(LuceneOptions luceneOptions, String field, 
			String value, Document document) {
		addFieldToDocument(luceneOptions, field, value, document, false);
	}
}
