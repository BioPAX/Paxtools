package org.biopax.paxtools.util;

import java.util.Arrays;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.LuceneOptions;

final class FieldBridgeUtils {
	private FieldBridgeUtils() {
		throw new AssertionError();
	}
	
	public static void addFieldToDocument(LuceneOptions luceneOptions, String field, String value, Document document) {
		/* toLowerCase() is important here and there; it's long to explain in full,
		 * but - it's about the @Field(index=Index.UN_TOKENIZED,..), MultiFieldQueryParser, 
		 * and StandardAnayzer to work well together... Rabbit's hole...
		 */
		String v = value.toLowerCase();
		if(!Arrays.asList(document.getValues(field)).contains(v))
			luceneOptions.addFieldToDocument(field, v, document);
	}
}
