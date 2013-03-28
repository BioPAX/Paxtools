package org.biopax.paxtools.util;


import org.apache.commons.lang.ArrayUtils;
import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.LuceneOptions;

/**
 * @author rodche //TODO annotate
 */
final class FieldBridgeUtils {
	private FieldBridgeUtils() {
		throw new AssertionError();
	}
	
	private static void addFieldToDocument(LuceneOptions luceneOptions, String field, 
			String value, Document document, boolean lowercase) {
		/* merely in all cases, lowercase must be TRUE (though it's hard to explain here why,
		 * but - it's about the @Field(analyze=Analyze.NO,..), MultiFieldQueryParser, 
		 * and StandardAnayzer to work well together... Rabbit's hole...)
		 */
		String v = (lowercase) ?  value.toLowerCase() : value;
		if(!ArrayUtils.contains(document.getValues(field), v))
			luceneOptions.addFieldToDocument(field, v, document);

	}
	
	public static void addFieldToDocument(LuceneOptions luceneOptions, String field, 
			String value, Document document) {
		addFieldToDocument(luceneOptions, field, value, document, true);
	}
	
	public static void addFieldToDocumentAsIs(LuceneOptions luceneOptions, String field, 
			String value, Document document) {
		addFieldToDocument(luceneOptions, field, value, document, false);
	}
}
