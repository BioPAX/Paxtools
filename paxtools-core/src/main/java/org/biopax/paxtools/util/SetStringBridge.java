package org.biopax.paxtools.util;

// imports

import org.hibernate.search.bridge.StringBridge;

import java.util.Set;

/**
 * Hibernate - Lucene bridge between Set<String> and String.
 */
public class SetStringBridge implements StringBridge {

	public String objectToString(Object object) {

		// string to return
		StringBuffer toReturn = new StringBuffer();

		// no way to do instanceof on generics.
		// trust this bridge is used properly
		Set<String> comments = (Set<String>)object;

		// interate over all comments and append (' ' delimit) to return string
		for (String comment : comments) {
			toReturn.append(comment + " ");
		}

		return toReturn.toString();
	}
}

