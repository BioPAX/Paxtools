package org.biopax.paxtools.io.gsea;

import org.apache.commons.lang.StringUtils;
import org.biopax.paxtools.controller.Fetcher;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.converter.LevelUpgrader;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * Converts a BioPAX model to GSEA (GMT format).
 * <p/>
 * Creates GSEA entries from the protein references's xrefs contained in the BioPAX model. 
 * One entry (id-list) per pathway per organism. If there are no pathways,
 * then simply - per organism (i.e., all available protein types are considered).
 * - One identifier per protein reference (not guaranteed to be the primary one).
 * All identifiers can only be of the same type, e.g., UniProt,
 * and the converter does not do any id-mapping; so a protein without 
 * the required identifier type will not be listed.
 * <p/>
 * Note, to effectively enforce cross-species violation, bio-sources must
 * be annotated (have a unification xref) with "taxonomy" database name 
 * and id, and pathways's, protein references's "organism" property - not empty. 
 * <p/>
 * Note, this code assumes that the model has successfully been validated
 * and normalized (e.g., using the BioPAX Validator for Level3 data). 
 * L1 and L2 models are first converted to L3 (this however does not 
 * fix BioPAX errors, if any present, but possibly adds new)
 */
public class GSEAConverter
{
	private final String database;
	private final boolean crossSpeciesCheckEnabled;

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
	 * @param crossSpeciesCheckEnabled - if true, enforces no cross species participants in output
	 */
	public GSEAConverter(final String database, boolean crossSpeciesCheckEnabled)
	{
		this.database = database;
		this.crossSpeciesCheckEnabled = crossSpeciesCheckEnabled;
	}

	/**
	 * Converts model to GSEA and writes to out.  See class declaration for more information.
	 * @param model Model
	 */
	public void writeToGSEA(final Model model, OutputStream out) throws IOException
	{

		Collection<GSEAEntry> entries = convert(model);
		if (entries.size() > 0)
		{
			Writer writer = new OutputStreamWriter(out);
			for (GSEAEntry entry : entries)
			{
				writer.write(entry.toString() + "\n");
			}
			writer.flush();
		}
	}

	/**
	 * Creates GSEA entries from the pathways contained in the model.
	 * @param model Model
	 * @return a set of GSEA entries
	 */
	public Collection<GSEAEntry> convert(final Model model)
	{
		Collection<GSEAEntry> toReturn = new TreeSet<GSEAEntry>(new Comparator<GSEAEntry>() {
			@Override
			public int compare(GSEAEntry o1, GSEAEntry o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});

		Model l3Model = null;
		// convert to level 3 in necessary
		if (model.getLevel() == BioPAXLevel.L1 || model.getLevel() == BioPAXLevel.L2)
			l3Model = (new LevelUpgrader()).filter(model);
		else
			l3Model = model;

		Set<Pathway> pathways = l3Model.getObjects(Pathway.class);
		if(!pathways.isEmpty()) {	
			for (Pathway pathway : pathways) 
			{
				String name = pathway.getDisplayName();
				name = (name == null) ? pathway.getStandardName() : name;
				name = (name == null) ? pathway.getRDFId() : name;
				
				String dataSource = getDataSource(pathway.getDataSource());
				
				Set<ProteinReference> pathwayProteinRefs = 
					(new Fetcher(SimpleEditorMap.L3, Fetcher.nextStepFilter))
						.fetch(pathway, ProteinReference.class);
				
				if(!pathwayProteinRefs.isEmpty()) {
					Map<String,Set<ProteinReference>> orgToPrsMap = organismToProteinRefsMap(pathwayProteinRefs);			
					// create GSEA/GMT entries - one entry per organism (null organism also makes one) 
					Collection<GSEAEntry> entries = createGseaEntries(name, dataSource, orgToPrsMap);
					toReturn.addAll(entries);
				}
			}
		} else {
			//organize PRs by species (GSEA s/w can handle only same species identifiers in a data row)
			Set<ProteinReference> allProteinRefs = l3Model.getObjects(ProteinReference.class);
			if(!allProteinRefs.isEmpty()) {
				Map<String,Set<ProteinReference>> orgToPrsMap = organismToProteinRefsMap(allProteinRefs);
				if(!orgToPrsMap.isEmpty()) {
					// create GSEA/GMT entries - one entry per organism (null organism also makes one) 
					toReturn.addAll(createGseaEntries("From a BioPAX sub-model (protein references, no pathways)", 
						getDataSource(l3Model.getObjects(Provenance.class)), orgToPrsMap));	
				}
			}
		}
					
		return toReturn;
	}

	
	private Collection<GSEAEntry> createGseaEntries(final String name, final String dataSource, 
			final Map<String, Set<ProteinReference>> orgToPrsMap) 
	{
		// generate GSEA entries for each taxId in parallel threads; await till all done (before returning)
		final Collection<GSEAEntry> toReturn = Collections.synchronizedList(new ArrayList<GSEAEntry>());
		ExecutorService exe = Executors.newFixedThreadPool(orgToPrsMap.keySet().size());
		for (final String org : orgToPrsMap.keySet()) {
			exe.submit(new Runnable() {
				@Override
				public void run() {
					GSEAEntry gseaEntry = new GSEAEntry(name, org, database, "datasource: " + dataSource);
					processProteinReferences(orgToPrsMap.get(org), gseaEntry);
					toReturn.add(gseaEntry);
				}
			});
		}
		exe.shutdown();
		try {
			exe.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted unexpectedly!");
		}
		
		return toReturn;
	}

	
	//warn: there can be many equivalent BioSource objects (same taxonomy id, different URIs)
	private Map<String, Set<ProteinReference>> organismToProteinRefsMap(
			Set<ProteinReference> proteinRefs) 
	{
		Map<String,Set<ProteinReference>> map = new HashMap<String, Set<ProteinReference>>();

		if(proteinRefs.isEmpty())
			throw new IllegalArgumentException("Empty set");
		
		if (crossSpeciesCheckEnabled) {
			for (ProteinReference r : proteinRefs) {
				String key = getTaxID(r.getOrganism()); // null also works (key == "")
				Set<ProteinReference> prs = map.get(key);
				if (prs == null) {
					prs = new HashSet<ProteinReference>();
					map.put(key, prs);
				}
				prs.add(r);
			}
		} else {
			map.put("", proteinRefs); //all PRs
		}
				
		return map;
	}


	void processProteinReferences(Set<ProteinReference> prs, GSEAEntry targetEntry)
	{

		for (ProteinReference aProteinRef : prs)
		{
			// we only process PRs that belong to the same species (as for targetEntry) if crossSpeciesCheckEnabled==true
			if (crossSpeciesCheckEnabled && !targetEntry.taxID().equals(getTaxID(aProteinRef.getOrganism())))
				continue;
				
			if (database != null && !database.isEmpty())
			{
				// short circuit if we are converting new Pathway Commons or another normalized data;
				// we get back the primary accession number, which is built into the URI of the
				// ProteinReference.
				final String lowcaseUri = aProteinRef.getRDFId().toLowerCase();
				if (lowcaseUri.startsWith("urn:miriam:" + database.toLowerCase()))
				{
					String accession = aProteinRef.getRDFId();
					accession = accession.substring(accession.lastIndexOf(":") + 1);
					targetEntry.getIdentifiers().add(accession);
				} 
				else if (lowcaseUri.startsWith("http://identifiers.org/")
					&& lowcaseUri.contains(database.toLowerCase()))
				{
					String accession = aProteinRef.getRDFId();
					accession = accession.substring(accession.lastIndexOf("/") + 1);
					targetEntry.getIdentifiers().add(accession);
				} 
				else { // simply pick one xref with matching db value (any one)
					TreeSet<Xref> orderedXrefs = new TreeSet<Xref>(new Comparator<Xref>() {
						@Override
						public int compare(Xref o1, Xref o2) {
							return o1.toString().compareTo(o2.toString());
						}
					});
					
					orderedXrefs.addAll(aProteinRef.getXref());
					for (Xref aXref : orderedXrefs)
					{
						if (aXref.getId() != null && aXref.getDb() != null && 
							(aXref.getDb().equalsIgnoreCase(database) 
									|| aXref.getId().toLowerCase().startsWith(database.toLowerCase()+":")
									|| aXref.getId().toLowerCase().startsWith(database.toLowerCase()+"_"))
						) {
							targetEntry.getIdentifiers().add(aXref.getId());
							break;
						}
					}
				}
			} else // use URI (not really useful for GSEA software, but good for testing/hacking...)
			{
				targetEntry.getIdentifiers().add(aProteinRef.getRDFId());
			}
		}
		
	}

	/*
	 * Gets datasource names, if any, in a consistent way/order, excl. duplicates
	 */
	private String getDataSource(Set<Provenance> provenances)
	{
		if(provenances.isEmpty()) return "N/A";
		
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

	
	private String getTaxID(BioSource org) {
		if (org != null) {
			for (Xref xref : org.getXref()) 
				if (xref.getDb().equalsIgnoreCase("taxonomy"))
					return xref.getId();
		}

		return ""; //unspecified/all species
	}
}
