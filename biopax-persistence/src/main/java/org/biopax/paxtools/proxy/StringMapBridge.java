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

import java.util.HashMap;

/**
 * bridge of String Set for Hibernate-search
 */
public class StringMapBridge implements FieldBridge {
	
	/** Creates a new instance of StringSetBridge */
	public StringMapBridge() {
	}
	
	public void set(String name, Object value, Document document, Field.Store store, Field.Index index, Float boost) {
		HashMap<String,String> ss = (HashMap<String,String>)value;
		for (String s : ss.values()) {
			document.add(new Field(name, s, store, index));
		}
	}

	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		HashMap<String,String> ss = (HashMap<String,String>)value;
		for (String s : ss.values()) {
			if (s == null) continue;
			document.add(new Field(name, s, luceneOptions.getStore(), luceneOptions.getIndex()));
		}
	}
}