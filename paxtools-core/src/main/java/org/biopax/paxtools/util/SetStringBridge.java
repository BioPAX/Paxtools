package org.biopax.paxtools.util;


import org.hibernate.search.bridge.StringBridge;

import java.util.Set;

/**
 * Hibernate - Lucene bridge between Set<String> and String.
 */
public class SetStringBridge implements StringBridge {

	public String objectToString(Object object) {

		// string to return
		StringBuilder toReturn = new StringBuilder();

		// no way to do instanceof on generics.
		// trust this bridge is used properly
		Set<String> items = (Set<String>)object;

		// interate over all strings and append (' ' delimit) to return string
		for (String item : items) {
			toReturn.append(item).append(" ");
		}

		return toReturn.toString();
	}
}

