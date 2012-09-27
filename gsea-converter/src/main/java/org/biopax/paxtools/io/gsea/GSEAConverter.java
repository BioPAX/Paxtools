package org.biopax.paxtools.io.gsea;

import org.apache.commons.lang.StringUtils;
import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.converter.OneTwoThree;
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

	static PathAccessor complexComponentPath = new PathAccessor("Complex/component*");

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
			for (Pathway pathway : pathways) {
				// collect all physical entities only; also, - need a modifiable collection here
				Set<PhysicalEntity> participants = new HashSet<PhysicalEntity>(
					new ClassFilterSet<Entity, PhysicalEntity>(participantPath.getValueFromBean(pathway), PhysicalEntity.class));
				//Component/memberEntity cycle can't be handled by PathAccessors although there should not be any deep nestings...
				iterateComponentMemberPECycle(participants);
				
				// collect all PRs from proteins;
				// (using pathwayProteins tmp set - is a work around a bug in PathAccessor (as on 2012/09/26 paxtools), 
				// which for when 'Protein/entityReference' path applied to a mixed set of PEs also returns also SMRs...)
				Set<Protein> pathwayProteins = new ClassFilterSet<PhysicalEntity, Protein>(participants, Protein.class);
				Set<ProteinReference> pathwayProteinRefs = PRPath.getValueFromBeans(pathwayProteins);
				pathwayProteinRefs.addAll(memberERPath.getValueFromBeans(pathwayProteinRefs));
				
				// define gsea entry name
				String name = pathway.getDisplayName();
				name = (name == null) ? pathway.getStandardName() : name;
				name = (name == null) ? "NO NAME Pathway" : name;
				// data source
				String dataSource = getDataSource(pathway.getDataSource());
				dataSource = (dataSource == null) ? "N/A" : dataSource;
				// taxonomy id
				String pathwayTaxonomyID = "";
				if(pathway.getOrganism() != null)
					pathwayTaxonomyID = getTaxID(pathway.getOrganism().getXref());

				// when pathwayTaxonomyID is empty, split all PRs by species, if crossSpeciesCheck==true
				if(crossSpeciesCheck && pathwayTaxonomyID.isEmpty()) 
				{
					Map<BioSource,Set<ProteinReference>> orgToPrsMap = bioSourceToPrsMap(pathwayProteinRefs);
					// create one GSEA/GMT entry per organism (null organism also makes one) 
					for (BioSource org : orgToPrsMap.keySet()) {
						GSEAEntry gseaEntry = new GSEAEntry();
						gseaEntry.setName(name);
						String taxid = (org != null) ? getTaxID(org.getXref()) : "";
						gseaEntry.setTaxID(taxid);
						gseaEntry.setDescription("datasource: " + dataSource + "; taxonomy: " 
								+ ((taxid.isEmpty()) ? "N/A" : taxid));
						gseaEntry.setRDFToGeneMap(processProteinReferences(orgToPrsMap.get(org), checkDatabase, taxid));
						toReturn.add(gseaEntry);
					}
				} else {
					GSEAEntry gseaEntry = new GSEAEntry();
					gseaEntry.setName(name);
					gseaEntry.setTaxID(pathwayTaxonomyID);
					gseaEntry.setDescription("datasource: " + dataSource + "; taxonomy: " 
							+ ((pathwayTaxonomyID.isEmpty()) ? "N/A" : pathwayTaxonomyID));
					gseaEntry.setRDFToGeneMap(processProteinReferences(pathwayProteinRefs, checkDatabase, pathwayTaxonomyID));
					toReturn.add(gseaEntry);
				}
			}
		} else {
			Set<ProteinReference> allProteinRefs = l3Model.getObjects(ProteinReference.class);
			//organize PRs by species (GSEA s/w can handle only same species identifiers in a data row)
			Map<BioSource,Set<ProteinReference>> orgToPrsMap = bioSourceToPrsMap(allProteinRefs);
			// create one GSEA/GMT entry per organism (null organism also makes one) 
			for (BioSource org : orgToPrsMap.keySet()) {
				GSEAEntry gseaEntry = new GSEAEntry();
				gseaEntry.setName("A set of protein references");
				final String taxid = (org != null) ? getTaxID(org.getXref()) : "";
				gseaEntry.setTaxID(taxid);
				gseaEntry.setDescription("a BioPAX sub-model; datasources: "
						+ getDataSource(l3Model.getObjects(Provenance.class))
						+ "; taxonomy: " + ((taxid.isEmpty()) ? "N/A" : taxid)
				);
				gseaEntry.setRDFToGeneMap(
					processProteinReferences(orgToPrsMap.get(org), checkDatabase, taxid)
				);
				toReturn.add(gseaEntry);
			}			
		}
		
		 //TODO what about Gene objects?
				
		return toReturn;
	}

	private Map<BioSource, Set<ProteinReference>> bioSourceToPrsMap(
			Set<ProteinReference> proteinRefs) {
		Map<BioSource,Set<ProteinReference>> map = new HashMap<BioSource, Set<ProteinReference>>();
		for(ProteinReference r : proteinRefs) {
			BioSource org = r.getOrganism(); //'null' is perfectly legal key
			Set<ProteinReference> prs = map.get(org);
			if(prs == null)  {
				prs = new HashSet<ProteinReference>();
				map.put(org, prs);
			}
			prs.add(r);
		}
		return map;
	}

	private void iterateComponentMemberPECycle(Set<PhysicalEntity> participants)
	{
		Set<PhysicalEntity> newPes = complexComponentPath.getValueFromBeans(participants);
		newPes.addAll(memberPEPath.getValueFromBeans(participants));
		if (!newPes.isEmpty())
		{
			iterateComponentMemberPECycle(newPes);
		}
		participants.addAll(newPes);
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

	/*
	 * Gets datasource names, if any, in a consistent way/order, excl. duplicates
	 */
	private String getDataSource(Set<Provenance> provenances)
	{
		Set<String> dsNames = new TreeSet<String>();
		for (Provenance provenance : provenances)
		{
			String name = provenance.getDisplayName();
			if(name == null) 
				name = provenance.getStandardName();
			if(name == null && !provenance.getName().isEmpty()) 
				name = provenance.getName().iterator().next();
			if (name != null && name.length() > 0)
				dsNames.add(name.toLowerCase());
		}
		
		return StringUtils.join(dsNames, ";");
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
