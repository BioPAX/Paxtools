package org.biopax.paxtools.io.gsea;


import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * This package-private class represents an entry found in a GMT format file.
 * 
 * Thread-safe.
 */
final class GMTEntry {

    final private String name;
    final private String taxID;
    final private String idType;
    final private String description;
    private final Set<String> identifiers;

    public GMTEntry(String name, String taxID, String idType, String description) {
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
   
    public Collection<String> identifiers() {
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

		toReturn
				// the (unique) 'name' column comes first
				.append(name)
				.append("\t")
				// next, comes the description column
				.append(description);

		if(!taxID.isEmpty())
			toReturn.append("; organism: ").append(taxID);

		if(!idType.isEmpty())
			toReturn.append("; idtype: ").append(idType);

		// finally, - all data (identifiers) columns
		for (String id : identifiers) {
			toReturn.append("\t").append(id);
		}

        return toReturn.toString();
    }
}