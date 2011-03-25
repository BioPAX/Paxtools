package org.biopax.paxtools.io.gsea;

// imports

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * This class represents an entry found in a GSEA (GMT format) file.
 */
public class GSEAEntry {

    private String name;
    private String taxID;
    private String datasource;
    private Map<String,String> rdfToGenes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getTaxID() {
    	return taxID;
    }
    
    public void setTaxID(String taxID) {
    	this.taxID = taxID;
    }

    public String getDataSource() {
        return datasource;
    }

    public void setDataSource(String datasource) {
        this.datasource = datasource;
    }

    public Map<String, String> getRDFToGeneMap() {
        return rdfToGenes;
    }
    
    public void setRDFToGeneMap(Map<String, String> rdfToGenes) {
    	this.rdfToGenes = rdfToGenes;
    }
    
    public Collection<String> getGenes() {
    	return (rdfToGenes != null) ? rdfToGenes.values() : new HashSet<String>(); 
    }

    public String toString() {
 
    	String toReturn = "";
    	if (name != null && datasource != null && rdfToGenes != null) {
    		toReturn = name + "\t" + datasource;
    		for (String gene : rdfToGenes.values()) {
    			toReturn += "\t" + gene;
    		}
    	}

    	// outta here
        return toReturn;
    }
}