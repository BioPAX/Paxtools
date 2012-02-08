package org.biopax.paxtools.util;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.TermQuery;
import org.hibernate.search.annotations.Factory;
import org.hibernate.search.annotations.Key;
import org.hibernate.search.filter.FilterKey;
import org.hibernate.search.filter.StandardFilterKey;

/**
 * 
 * @author rodche
 */
public abstract class BasicFilterFactory {

//	private static final Log LOG = LogFactory.getLog(BasicFilterFactory.class);
	
	private String[] values;
//	private final QueryParser queryParser;
	private final String searchField;
	
//	public BasicFilterFactory(String searchField, QueryParser queryParser) {
//		this.searchField = searchField;
//		this.queryParser = queryParser;
//	}
//	
//	public BasicFilterFactory(String searchField) {
//		this(searchField, new QueryParser(Version.LUCENE_31, 
//			searchField, new StandardAnalyzer(Version.LUCENE_31)));		
//	}
	
	public BasicFilterFactory(String searchField) {
		this.searchField = searchField;
	}
	
	
	public String[] getValues() {
		return values;
	}

	public void setValues(String[] values) {
		this.values = values;
	}

	
	@Key
    public FilterKey getKey() {
        StandardFilterKey key = new StandardFilterKey();
        key.addParameter(values);
        return key;
    }
	

	@Factory
	public Filter getFilter() {
// was a pretty cool solution: lucene syntax (operators, wildcard, fussy) filter strings were possible; 
// i.e, it's more like normal full-text searching; had to take care of query parser issues (escaping)
// 
//		Query query = null;		
//		String q = StringUtils.join(values, " ");
//		
//		try {
//			query = queryParser.parse(q); 
//		} catch (ParseException e) {
//			LOG.warn("Query queryParser error; " +
//				"cannot build '" + searchField + "' filter using: " 
//				+ q + " (try escaping Lucene reserved symbols: " +
//				" + - && || ! ( ) { } [ ] ^ \" ~ * ? : \\ " +
//				" with '\', unless they are actually used as such);" +
//				" Guess what, I will try to 'escape' ':' sysmbols and retry..."
//				+ e.toString());
//			q = QueryParser.escape(q); // here escaping would effectively disable Lucene query syntax!
////			q = q.replaceAll(":","\\\\:"); // try to deal with ":" only...
//			try {
//				query = queryParser.parse(q);
//				LOG.warn("Query queryParser - recovered from error using '" 
//						+ q + "' filter string instead!");
//			} catch (ParseException e1) {
//				throw new RuntimeException("Query queryParser - " +
//					"failed to recovered from previous parsing error using '" 
//						+ q + "' replacement filter string!");
//			}
//			
//		}
		
		/* a simpler, stricter, less confusing way filter search results:
		 * e.g., only full standard biopax names and IDs will work as filter values,
		 * while others will return empty result set (and the searchField is UN_TOKENIZED)
		 */
		BooleanQuery query = new BooleanQuery();
		for(String fv : values) {
			query.add(new TermQuery(new Term(searchField, fv.trim().toLowerCase())),
				Occur.SHOULD); // SHOULD here means "OR"
		}
		
		return new CachingWrapperFilter( new QueryWrapperFilter(query) );
	}
}
