package org.biopax.paxtools.persistence;

import java.io.Reader;
import org.apache.lucene.analysis.CharTokenizer;


public final class PaxtoolsTextTokenizer extends CharTokenizer {
	public PaxtoolsTextTokenizer(Reader in) {
		super(in);
	}

	protected boolean isTokenChar(char c) {
		if (Character.isLetterOrDigit(c) || c == '-' || c == '_') {
			return true;
		}
		return false;
	}

	protected char normalize(char c) {
		return Character.toLowerCase(c);
	}
}
