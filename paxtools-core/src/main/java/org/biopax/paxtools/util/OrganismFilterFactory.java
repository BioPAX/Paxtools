package org.biopax.paxtools.util;

import java.util.Set;

import org.hibernate.search.annotations.Factory;
import org.hibernate.search.annotations.Key;
import org.hibernate.search.filter.FilterKey;
import org.hibernate.search.filter.StandardFilterKey;

public class OrganismFilterFactory {

	private Set<String> organisms;
	
	
	public Set<String> getOrganisms() {
		return organisms;
	}

	
	public void setOrganisms(Set<String> organisms) {
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
		return null;
	}

}
