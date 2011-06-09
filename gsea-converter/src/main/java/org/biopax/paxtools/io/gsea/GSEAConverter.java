package org.biopax.paxtools.io.gsea;

// imports

import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.controller.Traverser;
import org.biopax.paxtools.controller.Visitor;
import org.biopax.paxtools.converter.OneTwoThree;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.ClassFilterSet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;


/**
 * Converts a BioPAX model to GSEA (GMT format).
 * 
 * Creates GSEA entries from the pathways contained in the model.
 * 
 * Pathway members are derived by finding the xref who's
 * database name matches the database constructor argument and returning
 * the respective database id.  If database id is empty, 
 * the rdf id of the protein is returned.
 * 
 * Note, to properly enforce cross-species violations, bio-sources must
 * be annotated with "taxonomy" database name.
 * 
 * Note this code assumes that the model has successfully been validated
 * using the BioPAX validator.
 */
public class GSEAConverter implements Visitor {
	
	// following vars used during traversal
	String database;
	boolean crossSpeciesCheck;
	boolean visitProtein; // true we visit proteins, false we visit ProteinReference
	Map<String, String> rdfToGenes; // map of member proteins of the pathway rdf id to gene symbol
	Set<BioPAXElement> visited; // helps during traversal
	String taxID;
	Traverser traverser;
	
	/**
	 * Constructor.
	 */
	public GSEAConverter() {
		this("", true);
	}
	
	/**
	 * Constructor.
	 * 
	 * See class declaration for more information.
	 * 
	 * @param database String: the database/xref to use for grabbing participants
	 * @param crossSpeciesCheck - if true, enforces no cross species participants in output
	 * 
	 */
	public GSEAConverter(String database, boolean crossSpeciesCheck) {
		this.database = database;
    	this.crossSpeciesCheck = crossSpeciesCheck;
    	this.traverser = new Traverser(SimpleEditorMap.L3, this);
	}
		
	/**
	 * Converts model to GSEA and writes to out.  See class declaration for more information.
	 * 
	 * @param model Model
	 */
	public void writeToGSEA(final Model model, OutputStream out) throws IOException {
	
		Collection<? extends GSEAEntry> entries = convert(model);
    	if (entries.size() > 0) {
    		Writer writer = new OutputStreamWriter(out);
    		for (GSEAEntry entry : entries) {
    			writer.write(entry.toString() + "\n");
    		}
    		writer.close();
    	}
	}

	/**
     * Creates GSEA entries from the pathways contained in the model.
     *	
     * @param model Model
     * @return a set of GSEA entries
     */
    public Collection<? extends GSEAEntry> convert(final Model model) {
    	
    	// setup some vars
    	Model l3Model = null;
    	
    	Collection<GSEAEntry> toReturn = new HashSet<GSEAEntry>();
    	    	    	
    	// convert to level 3 in necessary
        if (model.getLevel() == BioPAXLevel.L1 ||
        	model.getLevel() == BioPAXLevel.L2) {
        	l3Model = (new OneTwoThree()).filter(model);
        }
        else if (model.getLevel() == BioPAXLevel.L3) {
        	l3Model = model;
        }

        // iterate over all pathways in the model
        for (Pathway aPathway : l3Model.getObjects(Pathway.class)) {
        	toReturn.add(getGSEAEntry(model, aPathway, database));
        }
        
        // outta here
        return toReturn;
    }
    
    
    public void visit(BioPAXElement domain, Object range, Model model, PropertyEditor editor) {
    	
    	boolean checkDatabase = (this.database != null && this.database.length() > 0 && !this.database.equals("NONE"));
    	
    	if (range != null && range instanceof BioPAXElement && !visited.contains(range)) {
    		if (visitProtein) {
    			visitProtein(range, checkDatabase);
    		}
    		else {
    			visitProteinReference(range, checkDatabase);
    		}
    		visited.add((BioPAXElement)range);
			this.traverser.traverse((BioPAXElement)range, model);
    	}
    }
    
	private GSEAEntry getGSEAEntry(final Model model, final Pathway aPathway, final String database) {
		
		// the GSEAEntry to return
		final GSEAEntry toReturn = new GSEAEntry();
		
		// set name
		String name = aPathway.getDisplayName();
		name = (name == null) ? aPathway.getStandardName() : name;
		name = (name == null) ? "NAME" : name;
		toReturn.setName(name);
		// tax id
		String taxID = getTaxID(aPathway.getOrganism().getXref());
		taxID = (taxID == null) ? "TAX-ID" : taxID;
		toReturn.setTaxID(taxID);
		// data source
		String dataSource = getDataSource(aPathway.getDataSource());
		dataSource = (dataSource == null) ? "N/A" : dataSource;
		toReturn.setDataSource(dataSource);
		// genes
		this.taxID = taxID;
		this.visitProtein = true;
		this.rdfToGenes = new HashMap<String, String>();
		this.visited = new HashSet<BioPAXElement>();
		this.traverser.traverse(aPathway, model);
		if (this.rdfToGenes.size() == 0) {
			this.visitProtein = false;
			this.visited = new HashSet<BioPAXElement>();
			this.traverser.traverse(aPathway, model);
		}
		toReturn.setRDFToGeneMap(this.rdfToGenes);
	 	
		// outta here
		return toReturn;
	}
	
	private void visitProtein(Object range, boolean checkDatabase) {
    	
    	if (range instanceof Protein) {
    		Protein aProtein = (Protein)range;
    		// we only process proteins that are same species as pathway
    		if (crossSpeciesCheck && this.taxID.length() > 0 && !sameSpecies(aProtein, this.taxID)) {
    			return;
    		}
    		// if we are not checking database, just return rdf id
    		if (checkDatabase) {
			    for (Xref aXref : aProtein.getXref())
			    {
    				if (aXref.getDb() != null && aXref.getDb().equalsIgnoreCase(this.database)) {
    					this.rdfToGenes.put(aProtein.getRDFId(), aXref.getId());
    					break;
    				}
    			}
    		}
    		else {
    			this.rdfToGenes.put(aProtein.getRDFId(), aProtein.getRDFId());
    		}
    	}
	}
    
    private void visitProteinReference(Object range, boolean checkDatabase) {
    	
    	if (range instanceof ProteinReference) {
    		ProteinReference aProteinRef = (ProteinReference)range;
    		// we only process protein refs that are same species as pathway
    		if (crossSpeciesCheck && this.taxID.length() > 0 && !getTaxID(aProteinRef.getOrganism().getXref()).equals(this.taxID)) {
    			return;
    		}
    		if (checkDatabase) {
				// short circuit if we are converting for pathway commons
				// Also ensure we get back primary accession - which is built into the rdf id of the protein  ref
				if (database.equalsIgnoreCase("uniprot") && aProteinRef.getRDFId().startsWith("urn:miriam:uniprot:")) {
					String accession = aProteinRef.getRDFId();
					accession = accession.substring(accession.lastIndexOf(":")+1);
					this.rdfToGenes.put(aProteinRef.getRDFId(), accession);
				}
				else {
					for (Xref aXref: aProteinRef.getXref()) {
						if (aXref.getDb() != null && aXref.getDb().equalsIgnoreCase(database)) {
							this.rdfToGenes.put(aProteinRef.getRDFId(), aXref.getId());
							break;
						}
					}
				}
    		}
    		else {
    			this.rdfToGenes.put(aProteinRef.getRDFId(), aProteinRef.getRDFId());
    		}
    	}
	}

    private String getDataSource(Set<Provenance> provenances) {

		for (Provenance provenance : provenances) {
			String name = provenance.getDisplayName();
			name = (name == null) ? provenance.getStandardName() : name;
			if (name != null && name.length() > 0) return name;
		}
		
		// outta here
		return "";
	}
    
	private boolean sameSpecies(Protein aProtein, String taxID) {

		ProteinReference pRef = (ProteinReference)aProtein.getEntityReference();
		if (pRef != null && pRef.getOrganism() != null) {
			BioSource bs = pRef.getOrganism();
			if (bs.getXref() != null) {
				return (getTaxID(bs.getXref()).equals(taxID));
			}
		}

		// outta here
		return false;
	}
	
	private String getTaxID(Set<Xref> xrefs) {

		for (Xref xref : xrefs) {
			if (xref.getDb().equalsIgnoreCase("taxonomy")) {
				return xref.getId();
			}
		}
		
		// outta here
		return "";
	}
}
