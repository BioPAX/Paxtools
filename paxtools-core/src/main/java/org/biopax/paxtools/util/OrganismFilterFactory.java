package org.biopax.paxtools.util;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.Version;
import org.hibernate.search.annotations.Factory;
import org.hibernate.search.annotations.Key;
import org.hibernate.search.filter.FilterKey;
import org.hibernate.search.filter.StandardFilterKey;

public class OrganismFilterFactory {

	private String[] organisms;
	
	
	public String[] getOrganisms() {
		return organisms;
	}

	
	public void setOrganisms(String[] organisms) {
		this.organisms = organisms;
	}
	
	
	@Key
    public FilterKey getKey() {
        StandardFilterKey key = new StandardFilterKey();
        key.addParameter(organisms);
        return key;
    }
	
	
	@Factory
	public Filter getFilter() {
		QueryParser qParser = new QueryParser(Version.LUCENE_31, "organism", 
				new StandardAnalyzer(Version.LUCENE_31));
		String q = StringUtils.join(organisms, " ");
		try {
			Query query = qParser.parse(q);
			return new CachingWrapperFilter( new QueryWrapperFilter(query) );
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

}
