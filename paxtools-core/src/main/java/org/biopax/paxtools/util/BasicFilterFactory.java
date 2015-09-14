package org.biopax.paxtools.util;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.TermQuery;
import org.hibernate.search.annotations.Factory;
import org.hibernate.search.annotations.FullTextFilterDef;
import org.hibernate.search.annotations.Key;
import org.hibernate.search.filter.FilterKey;
import org.hibernate.search.filter.StandardFilterKey;

/**
 * Basic filter factory implementation class.
 * (See Hibernate Search documentation for more details)
 * 
 * @see Factory
 * @see Key
 * @see FullTextFilterDef
 * 
 * @author rodche
 * @deprecated Hibernate ORM/Search will be removed in v5
 */
public abstract class BasicFilterFactory {
	
	private String[] values;
	private final String searchField;

	/**
	 * @param searchField search/index field name
	 */
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
		/* a simpler, stricter, less confusing way filter search results:
		 * e.g., only full standard biopax names and IDs will work as filter values,
		 * while others will return empty result set (and the searchField is UN_TOKENIZED)
		 */
		BooleanQuery query = new BooleanQuery();
		for(String fv : values) {
			String term = fv.trim();
			query.add(new TermQuery(new Term(searchField, term)),
					Occur.SHOULD); // SHOULD here means "OR"
			query.add(new TermQuery(new Term(searchField, term.toLowerCase())),
				Occur.SHOULD);
		}
		
		return new CachingWrapperFilter( new QueryWrapperFilter(query) );
	}
}
