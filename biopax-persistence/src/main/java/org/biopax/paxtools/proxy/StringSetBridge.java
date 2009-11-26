/*
 * StringSetBridge.java
 *
 * 2007.05.11 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

import java.util.Set;

/**
 * bridge of String Set for Hibernate-search
 */
public class StringSetBridge implements FieldBridge {
	
	/** Creates a new instance of StringSetBridge */
	public StringSetBridge() {
	}
	
	public void set(String name, Object value, Document document, Field.Store store, Field.Index index, Float boost) {
		Set<String> ss = (Set<String>)value;
		for (String s : ss) {
			document.add(new Field(name, s, store, index));
		}
	}

	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		Set<String> ss = (Set<String>)value;
		for (String s : ss) {
			if (s == null) continue;
			document.add(new Field(name, s, luceneOptions.getStore(), luceneOptions.getIndex()));
		}
	}
}