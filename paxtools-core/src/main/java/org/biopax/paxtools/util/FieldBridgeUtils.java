package org.biopax.paxtools.util;

import java.util.Arrays;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.LuceneOptions;

final class FieldBridgeUtils {
	private FieldBridgeUtils() {
		throw new AssertionError();
	}
	
	public static void addFieldToDocument(LuceneOptions luceneOptions, String field, String value, Document document) {
		value = value.toLowerCase(); // toLowerCase() is very important here and there...it's long to explain
		if(!Arrays.asList(document.getValues(field)).contains(value))
			luceneOptions.addFieldToDocument(field, value, document);
	}
}
