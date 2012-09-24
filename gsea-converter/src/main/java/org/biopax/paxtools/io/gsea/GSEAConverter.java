package org.biopax.paxtools.io.gsea;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.util.StrUtils;
import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.converter.OneTwoThree;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;


/**
 * Converts a BioPAX model to GSEA (GMT format).
 * <p/>
 * Creates GSEA entries from the pathways contained in the model.
 * <p/>
 * Pathway members are derived by finding the xref who's
 * database name matches the database constructor argument and returning
 * the respective database id.  If database id is empty,
 * the rdf id of the protein is returned.
 * <p/>
 * Note, to properly enforce cross-species violations, bio-sources must
 * be annotated with "taxonomy" database name.
 * <p/>
 * Note this code assumes that the model has successfully been validated
 * (e.g., using the BioPAX validator for Level3 data). L1 and L2 models
 * are first converted to L3 (this however does not fix BioPAX errors
 * if any present in the L1/L2 data but rather adds new; so - double-check)
 */
public class GSEAConverter
{

	// following vars used during traversal
	final String database;

	boolean crossSpeciesCheck;

	boolean checkDatabase;



	static PathAccessor participantPath = new PathAccessor("Pathway/pathwayComponent*/participant*");

	static PathAccessor componentPath = new PathAccessor("Complex/component*");

	static PathAccessor memberPEPath = new PathAccessor("PhysicalEntity/memberPhysicalEntity");

	static PathAccessor PRPath = new PathAccessor("Protein/entityReference");

	static PathAccessor memberERPath = new PathAccessor("ProteinReference/memberEntityReference*");

	/**
	 * Constructor.
	 */
	public GSEAConverter()
	{
		this("", true);
	}

	/**
	 * Constructor.
	 * <p/>
	 * See class declaration for more information.
	 * @param database String: the database/xref to use for grabbing participants
	 * @param crossSpeciesCheck - if true, enforces no cross species participants in output
	 */
	public GSEAConverter(final String database, boolean crossSpeciesCheck)
	{
		this.database = database;
		this.crossSpeciesCheck = crossSpeciesCheck;
		checkDatabase = (database != null && database.length() > 0 && !database.equals("NONE"));
	}

	/**
	 * Converts model to GSEA and writes to out.  See class declaration for more information.
	 * @param model Model
	 */
	public void writeToGSEA(final Model model, OutputStream out) throws IOException
	{

		Collection<? extends GSEAEntry> entries = convert(model);
		if (entries.size() > 0)
		{
			Writer writer = new OutputStreamWriter(out);
			for (GSEAEntry entry : entries)
			{
				writer.write(entry.toString() + "\n");
			}
			writer.close();
		}
	}

	/**
	 * Creates GSEA entries from the pathways contained in the model.
	 * @param model Model
	 * @return a set of GSEA entries
	 */
	public Collection<? extends GSEAEntry> convert(final Model model)
	{

		// setup some vars
		Model l3Model = null;

		Collection<GSEAEntry> toReturn = new HashSet<GSEAEntry>();

		// convert to level 3 in necessary
		if (model.getLevel() == BioPAXLevel.L1 || model.getLevel() == BioPAXLevel.L2)
		{
			l3Model = (new OneTwoThree()).filter(model);
		} else
		{
			l3Model = model;
		}

		Set<Pathway> pathways = l3Model.getObjects(Pathway.class);
		if(!pathways.isEmpty()) {	
			for (Pathway pathway : pathways)
				toReturn.add(getGSEAEntry(l3Model, pathway, database));
		} else {
			Set<ProteinReference> ers = l3Model.getObjects(ProteinReference.class);
			GSEAEntry gseaEntry = new GSEAEntry();
			gseaEntry.setName("A set of protein references");
			gseaEntry.setTaxID("");			
			Set<String> dsNames = new TreeSet<String>();
			for(Provenance ds: l3Model.getObjects(Provenance.class))
				dsNames.add(ds.getDisplayName().toLowerCase());
			gseaEntry.setDataSource("a BioPAX sub-model; datasources: " 
				+ StringUtils.join(dsNames, ", "));
			gseaEntry.setRDFToGeneMap(processProteinReferences(ers, checkDatabase, ""));
			toReturn.add(gseaEntry);
		}
		
		 //TODO what about Gene objects?
				
		// outta here
		return toReturn;
	}

	private void iterateComponentMemberPECycle(Set participants)
	{
		Set newPes = componentPath.getValueFromBeans(participants);
		newPes.addAll(memberPEPath.getValueFromBeans(participants));
		if (!newPes.isEmpty())
		{
			iterateComponentMemberPECycle(newPes);
		}
		participants.addAll(newPes);
	}


	private GSEAEntry getGSEAEntry(final Model model, final Pathway aPathway, final String database)
	{
		Set participants = participantPath.getValueFromBean(aPathway);

		//Component/memberEntity cycle can't be handled by PathAccessors although there should not be any deep nestings...
		iterateComponentMemberPECycle(participants);

		Set ers = PRPath.getValueFromBeans(participants);
		ers.addAll(memberERPath.getValueFromBeans(ers));


		// the GSEAEntry to return
		final GSEAEntry toReturn = new GSEAEntry();

		// set name
		String name = aPathway.getDisplayName();
		name = (name == null) ? aPathway.getStandardName() : name;
		name = (name == null) ? "NAME" : name;
		toReturn.setName(name);
		// tax id
		String taxID = null;
		if(aPathway.getOrganism() != null)
			taxID = getTaxID(aPathway.getOrganism().getXref());
		if(taxID == null) 
			taxID = "";
		
		toReturn.setTaxID(taxID);
		// data source
		String dataSource = getDataSource(aPathway.getDataSource());
		dataSource = (dataSource == null) ? "N/A" : dataSource;
		toReturn.setDataSource(dataSource);

		toReturn.setRDFToGeneMap(processProteinReferences(ers, checkDatabase, taxID));

		return toReturn;
	}
	
	
	Map<String, String> processProteinReferences(Set prs, boolean checkDatabase, String taxID)
	{

		Map<String, String> rdfToGenes = new HashMap<String, String>();
		for (Object er : prs)
		{
			if (er instanceof ProteinReference)
			{
				ProteinReference aProteinRef = (ProteinReference) er;
				// we only process protein refs that are same species as pathway
				if (
						!crossSpeciesCheck 
						|| taxID.isEmpty() 
						|| (
								aProteinRef.getOrganism() != null 
								&& 
								getTaxID(aProteinRef.getOrganism().getXref()).equals(taxID)
							)
				) {
					if (checkDatabase)
					{
						// short circuit if we are converting new Pathway Commons or another normalized data;
						// we get back the primary accession number, which is built into the URI of the
						// ProteinReference.
						final String lowcaseUri = aProteinRef.getRDFId().toLowerCase();
						if (lowcaseUri.startsWith("urn:miriam:" + database.toLowerCase()))
						{
							String accession = aProteinRef.getRDFId();
							accession = accession.substring(accession.lastIndexOf(":") + 1);
							rdfToGenes.put(aProteinRef.getRDFId(), accession);
						} 
						else if (lowcaseUri.startsWith("http://identifiers.org/")
							&& lowcaseUri.contains(database.toLowerCase()))
						{
							String accession = aProteinRef.getRDFId();
							accession = accession.substring(accession.lastIndexOf("/") + 1);
							rdfToGenes.put(aProteinRef.getRDFId(), accession);
						} 
						else {
							TreeSet<Xref> orderedXrefs = new TreeSet<Xref>(new Comparator<Xref>() {
								@Override
								public int compare(Xref o1, Xref o2) {
									return o1.toString().compareTo(o2.toString());
								}
							});
							
							orderedXrefs.addAll(aProteinRef.getXref());
							for (Xref aXref : orderedXrefs)
							{
								if (aXref.getDb() != null && aXref.getDb().equalsIgnoreCase(database))
								{
									rdfToGenes.put(aProteinRef.getRDFId(), aXref.getId());
									break;
								}
							}
						}
					} else
					{
						rdfToGenes.put(aProteinRef.getRDFId(), aProteinRef.getRDFId());
					}
				}
			}
		}
		return rdfToGenes;
	}

	private String getDataSource(Set<Provenance> provenances)
	{
		StringBuilder s = new StringBuilder();
		
		for (Provenance provenance : provenances)
		{
			String name = provenance.getDisplayName();
			if(name == null) 
				name = provenance.getStandardName();
			if(name == null && !provenance.getName().isEmpty()) 
				name = provenance.getName().iterator().next();
			if (name != null && name.length() > 0)
				s.append(name).append(";");
		}
		
		if(s.length() > 0)
			s.deleteCharAt(s.length()-1);

		return s.toString();
	}

	private boolean sameSpecies(Protein aProtein, String taxID)
	{

		ProteinReference pRef = (ProteinReference) aProtein.getEntityReference();
		if (pRef != null && pRef.getOrganism() != null)
		{
			BioSource bs = pRef.getOrganism();
			if (bs.getXref() != null)
				return (getTaxID(bs.getXref()).equals(taxID));
		}

		// outta here
		return false;
	}

	private String getTaxID(Set<Xref> xrefs)
	{

		for (Xref xref : xrefs)
		{
			if (xref.getDb().equalsIgnoreCase("taxonomy"))
			{
				return xref.getId();
			}
		}

		// outta here
		return "";
	}
}
