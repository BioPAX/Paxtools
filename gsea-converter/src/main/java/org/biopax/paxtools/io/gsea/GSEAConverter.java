package org.biopax.paxtools.io.gsea;

// imports
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.BioPAXElement;

import org.biopax.paxtools.controller.Visitor;
import org.biopax.paxtools.controller.Traverser;
import org.biopax.paxtools.controller.PropertyEditor;

import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.paxtools.converter.OneTwoThree;
import org.biopax.paxtools.io.simpleIO.SimpleEditorMap;

import java.io.*;
import java.util.HashSet;
import java.util.Collection;
import java.util.Set;


/**
 * Converts a BioPAX model to GSEA (GMT format).
 */
public class GSEAConverter implements Visitor {
	
	// following vars used during traversal
	String database;
	boolean crossSpeciesCheck;
	boolean visitProtein; // true we visit proteins, false we visit ProteinReference
	Set<String> genes; // member genes of the pathway
	Set<BioPAXElement> visited; // helps during traversal
	String taxID;
	Traverser traverser;
	
	/**
	 * Constructor.
	 */
	public GSEAConverter() {
		this.traverser = new Traverser(new SimpleEditorMap(BioPAXLevel.L3), this);
	}
		
	/**
	 * Converts model to GSEA and writes to out.  See convert for more information.
	 * 
	 * @param model Model
	 * @param database String: the database/xref to use for grabbing participants (can be null)
	 * @param crossSpeciesCheck - if true, enforces no cross species participants in output
	 */
	public void writeToGSEA(final Model model, final String database, final boolean crossSpeciesCheck, OutputStream out) throws IOException {
	
		Collection<? extends GSEAEntry> entries = convert(model, database, crossSpeciesCheck);
    	if (entries.size() > 0) {
    		Writer writer = new OutputStreamWriter(out);
    		for (GSEAEntry entry : entries) {
    			String dataSource = entry.getDataSource();
    			writer.write(entry.getName() + "\t" + dataSource);
    			for (String gene : entry.getGenes()) {
    				writer.write("\t" + gene);
    			}
    			writer.write("\n");
    		}
    		writer.close();
    	}
	}

	/**
     * Creates GSEA entries from the pathways contained in the model.
     * 
     * Pathway members are derived by finding the xref who's
     * database name matches the database argument and returning
     * the respective database id.  If database id is empty (or null), 
     * the rdf id of the protein is returned.
     * 
     * Note, to properly enforce cross-species violations, bio-sources must
     * be annotated with either "urn.miriam.taxonomy" or "NCBI_TAXONOMY"
     * database names.
     *	
     * @param model Model
     * @param database String
     * @param crossSpeciesCheck
     * @return a set of GSEA entries
     */
    public Collection<? extends GSEAEntry> convert(final Model model, final String database, final boolean crossSpeciesCheck) {
    	
    	// setup some vars
    	Model l3Model = null;
    	this.database = database;
    	this.crossSpeciesCheck = crossSpeciesCheck;
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
		this.genes = new HashSet<String>();
		this.visited = new HashSet<BioPAXElement>();
		this.traverser.traverse(aPathway, model);
		if (this.genes.size() == 0) {
			this.visitProtein = false;
			this.visited = new HashSet<BioPAXElement>();
			this.traverser.traverse(aPathway, model);
		}
		toReturn.setGenes(this.genes);
		
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
    			ClassFilterSet<Xref> xrefs= new ClassFilterSet<Xref>(aProtein.getXref(), Xref.class);
    			for (Xref aXref: xrefs) {
    				if (aXref.getDb().equals(this.database)) {
    					this.genes.add(aXref.getId());
    					break;
    				}
    			}
    		}
    		else {
    			this.genes.add(aProtein.getRDFId());
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
    			ClassFilterSet<Xref> xrefs= new ClassFilterSet<Xref>(aProteinRef.getXref(), Xref.class);
    			for (Xref aXref: xrefs) {
    				if (aXref.getDb().equals(database)) {
    					this.genes.add(aXref.getId());
    					break;
    				}
    			}
    		}
    		else {
    			this.genes.add(aProteinRef.getRDFId());
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
		return (getTaxID(pRef.getOrganism().getXref()).equals(taxID));
	}
	
	private String getTaxID(Set<Xref> xrefs) {

		for (Xref xref : xrefs) {
			if (xref.getDb().equals("urn.miriam.taxonomy") ||
				xref.getDb().equals("NCBI_TAXONOMY")) {
				return xref.getId();
			}
		}
		
		// outta here
		return "";
	}
}
