package org.biopax.paxtools.util;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.util.Version;
import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.hibernate.search.annotations.Factory;
import org.hibernate.search.annotations.Key;
import org.hibernate.search.filter.FilterKey;
import org.hibernate.search.filter.StandardFilterKey;

public class DataSourceFilterFactory {

	private String[] datasources;
	

	public String[] getDatasources() {
		return datasources;
	}


	public void setDatasources(String[] datasources) {
		this.datasources = datasources;
	}

	
	@Key
    public FilterKey getKey() {
        StandardFilterKey key = new StandardFilterKey();
        key.addParameter(datasources);
        return key;
    }
	

	@Factory
	public Filter getFilter() {
		QueryParser qParser = new QueryParser(Version.LUCENE_31, 
			BioPAXElementImpl.FIELD_DATASOURCE, 
				new StandardAnalyzer(Version.LUCENE_31));
		String q = StringUtils.join(datasources, " ");
		try {
			Query query = qParser.parse(q);
			return new CachingWrapperFilter( new QueryWrapperFilter(query) );
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
}
