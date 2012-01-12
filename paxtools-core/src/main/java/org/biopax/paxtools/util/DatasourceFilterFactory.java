package org.biopax.paxtools.util;

import java.util.Set;

import org.hibernate.search.annotations.Factory;
import org.hibernate.search.annotations.Key;
import org.hibernate.search.filter.FilterKey;
import org.hibernate.search.filter.StandardFilterKey;

public class DataSourceFilterFactory {

	private Set<String> datasources;
	

	public Set<String> getDatasources() {
		return datasources;
	}


	public void setDatasources(Set<String> datasources) {
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
		return null;
	}
}
