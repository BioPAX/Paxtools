package org.biopax.paxtools.io.gsea;


import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

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
		this.identifiers = new TreeSet<String>();
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
    
    
    /**
     * Creates GSEA (.gmt) file entry (line):
     */
    public String toString() {
    	StringBuilder toReturn = new StringBuilder();

		String tax = ((taxID.isEmpty()) ? "unspecified" : taxID);
		toReturn
				// the (unique) 'name' column comes first
				.append(tax).append(": ").append(name)
				.append("\t")
				// next, comes the description column
				.append(description)
				.append("; organism: ").append(tax)
				.append("; id type: ").append(idType);
		// finally, - all data (identifiers) columns
		for (String id : identifiers) {
			toReturn.append("\t").append(id);
		}

        return toReturn.toString();
    }
}