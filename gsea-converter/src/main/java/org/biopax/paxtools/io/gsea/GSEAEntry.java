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
    private String description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String descr) {
        this.description = descr;
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
    	if (name != null && description != null && rdfToGenes != null) {
    		toReturn = name + "\t" + description;
    		for (String gene : rdfToGenes.values()) {
    			toReturn += "\t" + gene;
    		}
    	}

        return toReturn;
    }
}