package org.biopax.paxtools.io.gsea;


import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * This package-private class represents an entry found in a GSEA (GMT format) file.
 * 
 * Thread-safe.
 */
class GSEAEntry {

    final private String name;
    final private String taxID;
    final private String idType;
    final private String description;
    private final Set<String> identifiers;

    public GSEAEntry(String name, String taxID, String idType, String description) {
    	if(name == null || taxID == null || idType == null || description == null) 
    		throw new IllegalArgumentException("Null paraneter (not allowed)");
    	
		this.name = name;
		this.taxID = taxID;
		this.idType = idType;
		this.description = description;
		
		this.identifiers = new ConcurrentSkipListSet<String>();
	}
    
    
    public String name() {
        return name;
    }

    public String taxID() {
    	return taxID;
    }   

    public String description() {
        return description;
    }
   
    Collection<String> getIdentifiers() {
    	return identifiers; 
    }

    public String idType() {
		return idType;
	}
    
    
    public String toString() {
    	StringBuilder toReturn = new StringBuilder();
    	
    	if (!identifiers.isEmpty()) {
    		toReturn.append(name).append("\t").append(description)
    			.append("; taxonomy: ").append(((taxID.isEmpty()) ? "N/A" : taxID))
    			.append("; id type: ").append(idType);
    		for (String id : identifiers) {
    			toReturn.append("\t").append(id);
    		}
    	}

        return toReturn.toString();
    }
}