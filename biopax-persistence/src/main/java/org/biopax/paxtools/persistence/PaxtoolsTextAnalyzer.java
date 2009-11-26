package org.biopax.paxtools.persistence;

import java.io.Reader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

public final class PaxtoolsTextAnalyzer extends Analyzer {
	public TokenStream tokenStream(String fieldName, Reader reader) {
		if (fieldName.equals(SearchFileName.SEARCH_FIELD_EC_NUMBER))
			return new PaxtoolsTextTokenizerForEcNumber(reader);
		return new PaxtoolsTextTokenizer(reader);
	}
}
