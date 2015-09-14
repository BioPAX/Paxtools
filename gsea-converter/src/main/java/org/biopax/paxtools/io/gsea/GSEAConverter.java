package org.biopax.paxtools.io.gsea;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.controller.*;
import org.biopax.paxtools.converter.LevelUpgrader;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.SetEquivalenceChecker;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Converts a BioPAX model to the GMT format (used by GSEA software).
 * 
 * It creates GSEA entries from the protein reference (PR) xrefs 
 * in the BioPAX model as follows: 
 * <ul>
 * <li>Each entry (row) consists of three columns (tab separated): 
 * name (e.g., "taxonomyID: pathway_name"), 
 * description (e.g. "datasource: pid;reactome; organism: 9606 id type: uniprot"), and
 * the list of identifiers (of the same type). For all PRs not associated with any pathway,
 * "Not pathway" is used instead of the pathway name.</li>
 * <li>The "id type" is what specified by Constructor parameter 'database'. 
 * </li>
 * <li>The list may have one or more IDs of the same type per PR, 
 * e.g., UniProt IDs or HGNC Symbols; PRs not having an xref of 
 * given db/id type are ignored. If there are less than three protein 
 * referencesper entry, it will not be printed.</li>
 * </ul>
 * 
 * Note, to effectively enforce cross-species violation, 
 * 'organism' property of PRs and pathways must be set 
 * to a BioSource object that has a valid unification xref: 
 * db="Taxonomy" and some taxonomy id.
 *
 * Note, this code assumes that the model has successfully been validated
 * and perhaps even normalized (using the BioPAX Validator/Normalizer). 
 * A BioPAX L1 or L2 model is first converted to the L3 
 * (this is a lossless conversion if there are no BioPAX errors).
 */
public class GSEAConverter
{
	private final static Log LOG = LogFactory.getLog(GSEAConverter.class);
	
	private final String database;
	private final boolean crossSpeciesCheckEnabled;
	private final boolean skipSubPathways;
	private final Set<Provenance> skipSubPathwaysOf;

	/**
	 * Constructor.
	 */
	public GSEAConverter()
	{
		this("", true);
	}

	/**
	 * Constructor.
	 *
	 * See class declaration for more information.
	 * @param database - identifier type, name of the resource, either the string value 
	 *                   of the most of EntityReference's xref.db properties in the BioPAX data,
	 *                   e.g., "HGNC Symbol", "NCBI Gene", "RefSeq", "UniProt" or "UniProt knowledgebase",
	 *                   or the &lt;namespace&gt; part in normalized EntityReference URIs 
	 *                   http://identifiers.org/&lt;namespace&gt;/&lt;ID&gt;
	 *                   (it depends on the actual data; so double-check before using in this constructor).
	 * @param crossSpeciesCheckEnabled - if true, enforces no cross species participants in output
	 */
	public GSEAConverter(String database, boolean crossSpeciesCheckEnabled)
	{
		this(database, crossSpeciesCheckEnabled, false);
	}

	/**
	 * Constructor.
	 *
	 * See class declaration for more information.
	 * @param database - identifier type, name of the resource, either the string value
	 *                   of the most of EntityReference's xref.db properties in the BioPAX data,
	 *                   e.g., "HGNC Symbol", "NCBI Gene", "RefSeq", "UniProt" or "UniProt knowledgebase",
	 *                   or the &lt;namespace&gt; part in normalized EntityReference URIs
	 *                   http://identifiers.org/&lt;namespace&gt;/&lt;ID&gt;
	 *                   (it depends on the actual data; so double-check before using in this constructor).
	 * @param crossSpeciesCheckEnabled - if true, enforces no cross species participants in output
	 * @param skipSubPathways - if true, do not traverse into any sub-pathways to collect entity references
	 *                       (useful when a model, such as converted to BioPAX KEGG data, has lots of sub-pathways, loops.)
	 */
	public GSEAConverter(String database, boolean crossSpeciesCheckEnabled, boolean skipSubPathways)
	{
		this.database = database;
		this.crossSpeciesCheckEnabled = crossSpeciesCheckEnabled;
		this.skipSubPathways = skipSubPathways;
		this.skipSubPathwaysOf = Collections.emptySet();
	}

	/**
	 * Constructor.
	 *
	 * See class declaration for more information.
	 * @param database - identifier type, name of the resource, either the string value
	 *                   of the most of EntityReference's xref.db properties in the BioPAX data,
	 *                   e.g., "HGNC Symbol", "NCBI Gene", "RefSeq", "UniProt" or "UniProt knowledgebase",
	 *                   or the &lt;namespace&gt; part in normalized EntityReference URIs
	 *                   http://identifiers.org/&lt;namespace&gt;/&lt;ID&gt;, such as 'hgnc.symbol', 'uniprot'
	 *                   (it depends on the actual data; so double-check before using in this constructor).
	 * @param crossSpeciesCheckEnabled - if true, enforces no cross species participants in output
	 * @param skipSubPathwaysOf - do not look inside sub-pathways of pathways of given data sources to collect entity references
	 *                       (useful when a model, such as converted to BioPAX KEGG data, has lots of sub-pathways, loops.)
	 */
	public GSEAConverter(String database, boolean crossSpeciesCheckEnabled, Set<Provenance> skipSubPathwaysOf)
	{
		this.database = database;
		this.crossSpeciesCheckEnabled = crossSpeciesCheckEnabled;

		if(skipSubPathwaysOf == null)
			skipSubPathwaysOf = Collections.emptySet();
		this.skipSubPathwaysOf = skipSubPathwaysOf;

		this.skipSubPathways = false;
	}


	/**
	 * Converts model to GSEA (GMT) and writes to out.
	 * See class declaration for more information.
	 *
	 * @param model Model
	 * @param out output stream to write the result to
	 * @throws IOException when there's an output stream error
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
		final Collection<GSEAEntry> toReturn = new TreeSet<GSEAEntry>(new Comparator<GSEAEntry>() {
			@Override
			public int compare(GSEAEntry o1, GSEAEntry o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});

		Model l3Model;
		// convert to level 3 in necessary
		if (model.getLevel() == BioPAXLevel.L1 || model.getLevel() == BioPAXLevel.L2)
			l3Model = (new LevelUpgrader()).filter(model);
		else
			l3Model = model;
		
		//a modifiable copy of the set of all PRs in the model - 
		//simply to keep, after all, all the PRs that do not belong to any pathway
		final Set<ProteinReference> prs = Collections.synchronizedSet(
				new HashSet<ProteinReference>(l3Model.getObjects(ProteinReference.class))
		);

		ExecutorService exe = Executors.newFixedThreadPool(10);
		final Set<Pathway> pathways = l3Model.getObjects(Pathway.class);
		for (Pathway pathway : pathways) 
		{
			String name = (pathway.getDisplayName() == null)
					? pathway.getStandardName() : pathway.getDisplayName();
			
			if(name == null || name.isEmpty()) 
				name = pathway.getRDFId();

			final Pathway currentPathway = pathway;
			final String currentPathwayName = name;
			final boolean ignoreSubPathways = skipSubPathways ||
				(!skipSubPathwaysOf.isEmpty() && shareSomeObjects(currentPathway.getDataSource(), skipSubPathwaysOf));

			exe.submit(new Runnable() {
				@Override
				public void run() {
					LOG.info("Begin converting " + currentPathwayName + " pathway, uri=" + currentPathway.getRDFId());

					final Set<ProteinReference> pathwayProteinRefs = new HashSet<ProteinReference>();

					Traverser traverser = new AbstractTraverser(SimpleEditorMap.L3,
							Fetcher.nextStepFilter, Fetcher.objectPropertiesOnlyFilter)
					{
						protected void visit(Object range, BioPAXElement domain, Model model, PropertyEditor editor)
						{
							//by design (- objectPropertiesOnlyFilter is used), it'll visit only object properties.
							BioPAXElement bpe = (BioPAXElement) range;

							if(bpe instanceof ProteinReference) {
								pathwayProteinRefs.add((ProteinReference) bpe);
							}

							if(bpe instanceof Pathway) {
								Pathway subPathway = (Pathway) bpe;
								if(ignoreSubPathways)
								{	//do not traverse into the sub-pathway; log
									LOG.info("Skipping sub-pathway: " + subPathway.getRDFId());
								} else {
									traverse(subPathway, null);
								}
							} else {
								traverse(bpe, null);
							}
						}
					};
					//run it - collect all PRs from the pathway
					traverser.traverse(currentPathway, null);


					LOG.info("- fetched PRs: " + pathwayProteinRefs.size());
					if(!pathwayProteinRefs.isEmpty()) {
						LOG.info("- grouping the PRs by organism...");
						Map<String,Set<ProteinReference>> orgToPrsMap = organismToProteinRefsMap(pathwayProteinRefs);
						// create GSEA/GMT entries - one entry per organism (null organism also makes one)
						String dataSource = getDataSource(currentPathway.getDataSource());
						LOG.info("- creating GSEA/GMT entries...");
						Collection<GSEAEntry> entries = createGseaEntries(currentPathwayName, dataSource, orgToPrsMap);
						if(!entries.isEmpty())
							toReturn.addAll(entries);
						prs.removeAll(pathwayProteinRefs);//there left not yet processed PRs (PR can be processed multiple times anyway)
						LOG.info("- collected " + entries.size() + "entries.");
					}
				}
			});
		}

		exe.shutdown();
		try {
			exe.awaitTermination(48, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted unexpectedly!");
		}
		
		//when there're no pathways, only empty pathays, pathways w/o PRs, then use all/rest of PRs -
		//organize PRs by species (GSEA s/w can handle only same species identifiers in a data row)
		LOG.info("Creating entries for the rest fo (unused) PRs...");
		if(!prs.isEmpty()) { //all or not processed above
			Map<String,Set<ProteinReference>> orgToPrsMap = organismToProteinRefsMap(prs);
			if(!orgToPrsMap.isEmpty()) {
				// create GSEA/GMT entries - one entry per organism (null organism also makes one) 
				toReturn.addAll(createGseaEntries("Not pathway", 
					getDataSource(l3Model.getObjects(Provenance.class)), orgToPrsMap));	
			}
		}
					
		return toReturn;
	}

	
	private Collection<GSEAEntry> createGseaEntries(final String name, final String dataSource, 
			final Map<String, Set<ProteinReference>> orgToPrsMap) 
	{
		// generate GSEA entries for each taxId in parallel threads; await till all done (before returning)
		final Collection<GSEAEntry> toReturn = Collections.synchronizedList(new ArrayList<GSEAEntry>());	
		ExecutorService exe = Executors.newFixedThreadPool(5);
		for (final String org : orgToPrsMap.keySet()) {
			if(orgToPrsMap.get(org).size() > 0) {
				exe.submit(new Runnable() {
					@Override
					public void run() {
						LOG.info("adding " + database + " IDs of " + org + 
							" proteins (PRs) from '" + name + "', " + 
							dataSource + " pathway...");
						GSEAEntry gseaEntry = new GSEAEntry(name, org, database, "datasource: " + dataSource);
						processProteinReferences(orgToPrsMap.get(org), gseaEntry);
						toReturn.add(gseaEntry);
					}
				});
			}
		}
		exe.shutdown();
		try {
			exe.awaitTermination(4, TimeUnit.HOURS);
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
				String key = getTaxID(r.getOrganism()); // null org. is ok (key == "")
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
				String lowercaseDb = database.toLowerCase();
				// a shortcut if we are converting validated normalized BioPAX model:
				// get the primary ID from the URI of the ProteinReference
				final String lowcaseUri = aProteinRef.getRDFId().toLowerCase();
				if (lowcaseUri.startsWith("http://identifiers.org/")
					&& lowcaseUri.contains(lowercaseDb))
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
							(aXref.getDb().toLowerCase().startsWith(lowercaseDb) 
							|| aXref.getId().toLowerCase().startsWith(lowercaseDb + ":")
							|| aXref.getId().toLowerCase().startsWith(lowercaseDb + "_"))
						) {
							targetEntry.getIdentifiers().add(aXref.getId());
						}
					}
				}
			} else {
				// use URI (not really useful for GSEA software, but good for testing/hacking)
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

	private boolean shareSomeObjects(Set<?> setA, Set<?> setB) {
		return (!setA.isEmpty() && !setB.isEmpty())	? !CollectionUtils.intersection(setA, setB).isEmpty() : false;
	}
}
