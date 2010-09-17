package org.biopax.paxtools.io.gsea;

// imports
import java.util.Set;

/**
 * This class represents an entry found in a GSEA (GMT format) file.
 */
public class GSEAEntry {

    private String name;
    private String taxID;
    private String datasource;
    private Set<String> genes;

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

    public Set<String> getGenes() {
        return genes;
    }

    public void setGenes(Set<String> genes) {
        this.genes = genes;
    }

    public String toString() {
        String toReturn = name + "\t" + datasource;
        for (String gene : genes) {
            toReturn += "\t" + gene;
        }
        return toReturn;
    }
}