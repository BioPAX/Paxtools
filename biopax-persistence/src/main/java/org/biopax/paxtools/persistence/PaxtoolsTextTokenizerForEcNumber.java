package org.biopax.paxtools.persistence;

import java.io.Reader;
import org.apache.lucene.analysis.CharTokenizer;

// SimpleAnalyzerではisLetterなのでEC-NUMBERの文字列中に含まれる'.'が検索対象にならない。
// WhitespaceAnalyzerではisWhitespaceなので'[hoge]'というような記述のとき'hoge'が検索対象にならない。
// また、toLowerCaseも行わない。
// そこでLetterと数字と一部の非Letterを検索対象とし、また大文字小文字を区別しないAnalyzerを独自に用意する。
// 2007.09.11 Takeshi Yoneki

public final class PaxtoolsTextTokenizerForEcNumber extends CharTokenizer {
	public PaxtoolsTextTokenizerForEcNumber(Reader in) {
		super(in);
	}

	protected boolean isTokenChar(char c) {
		if (Character.isLetterOrDigit(c) || c == '.' || c == '-' || c == '_') {
			return true;
		}
		return false;
	}

	protected char normalize(char c) {
		return Character.toLowerCase(c);
	}
}
