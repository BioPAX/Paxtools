package org.biopax.paxtools.io.gsea;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.biopax.paxtools.controller.*;
import org.biopax.paxtools.converter.LevelUpgrader;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;

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
 * and perhaps normalized (using the BioPAX Validator, Paxtools Normalizer).
 * A BioPAX L1 or L2 model is first converted to the L3 (lossless conversion if there are no BioPAX errors).
 */
public class GSEAConverter
{
	private final static Logger LOG = LoggerFactory.getLogger(GSEAConverter.class);
	
	private final String database;
	private final boolean crossSpeciesCheckEnabled;
	private Set<String> allowedOrganisms;
	private final boolean skipSubPathways;
	private final Set<Provenance> skipSubPathwaysOf;
	private boolean skipOutsidePathways = false;
	private int minNumOfGenesPerEntry = 1;

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
	 * If true, then only GSEA entries that (genes) correspond to a Pathway
	 * are printed to the output.
	 * @return true/false
     */
	public boolean isSkipOutsidePathways() {
		return skipOutsidePathways;
	}

	public void setSkipOutsidePathways(boolean skipOutsidePathways) {
		this.skipOutsidePathways = skipOutsidePathways;
	}

	public Set<String> getAllowedOrganisms() {
		return allowedOrganisms;
	}

	public void setAllowedOrganisms(Set<String> allowedOrganisms) {
		this.allowedOrganisms = allowedOrganisms;
	}

	/**
	 * If this value is greater than 0, and the number of proteins/genes
	 * in a gene set is less than that value, then this gene set is to skip
	 * (no GSEA entry is written).
	 * @return the min. value
     */
	public int getMinNumOfGenesPerEntry() {
		return minNumOfGenesPerEntry;
	}

	public void setMinNumOfGenesPerEntry(int minNumOfGenesPerEntry) {
		this.minNumOfGenesPerEntry = minNumOfGenesPerEntry;
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
			for (GSEAEntry entry : entries) {
				if ((minNumOfGenesPerEntry <= 1 && !entry.getIdentifiers().isEmpty())
						|| entry.getIdentifiers().size() >= minNumOfGenesPerEntry)
				{
					writer.write(entry.toString() + "\n");
				}
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
		if (model.getLevel() == BioPAXLevel.L2)
			l3Model = (new LevelUpgrader()).filter(model);
		else
			l3Model = model;
		
		//a modifiable copy of the set of all PRs in the model - 
		//after all, it has all the PRs that do not belong to any pathway
		final Set<SequenceEntityReference> sequenceEntityReferences =
				new HashSet<SequenceEntityReference>(l3Model.getObjects(SequenceEntityReference.class));

		final Set<Pathway> pathways = l3Model.getObjects(Pathway.class);
		for (Pathway pathway : pathways) 
		{
			String name = (pathway.getDisplayName() == null) ? pathway.getStandardName() : pathway.getDisplayName();
			if(name == null || name.isEmpty()) 
				name = pathway.getUri();

			final Pathway currentPathway = pathway;
			final String currentPathwayName = name;

			final boolean ignoreSubPathways =
				(!skipSubPathwaysOf.isEmpty() && shareSomeObjects(currentPathway.getDataSource(), skipSubPathwaysOf))
					|| skipSubPathways;

			LOG.debug("Begin converting " + currentPathwayName + " pathway, uri=" + currentPathway.getUri());
			final Set<SequenceEntityReference> pathwaySers = new HashSet<SequenceEntityReference>();
			final Traverser traverser = new AbstractTraverser(SimpleEditorMap.L3,
					Fetcher.nextStepFilter, Fetcher.objectPropertiesOnlyFilter) {
				@Override
				protected void visit(Object range, BioPAXElement domain, Model model, PropertyEditor editor)
				{
					//by design (objectPropertiesOnlyFilter is used), it'll visit only object properties.
					BioPAXElement bpe = (BioPAXElement) range;
					if(bpe instanceof SequenceEntityReference) {
						pathwaySers.add((SequenceEntityReference) bpe);
					}
					if(bpe instanceof Pathway) {
						if(ignoreSubPathways)
						{	//do not traverse into the sub-pathway; log
							LOG.debug("Skipping sub-pathway: " + bpe.getUri());
						} else {
							traverse(bpe, model);
						}
					} else {
						traverse(bpe, model);
					}
				}
			};
			//run it - collect all PRs from the pathway
			traverser.traverse(currentPathway, null);

			if(!pathwaySers.isEmpty()) {
				if(pathwaySers.size() > 199) {
					LOG.debug("Pathway " + currentPathwayName + " (" + currentPathway.getUri()
							+ ") has lots of PRs: " + pathwaySers.size());
				}
				LOG.debug("- fetched PRs: " + pathwaySers.size() + "; now grouping by organism...");
				Map<String,Set<SequenceEntityReference>> orgToPrsMap = organismToProteinRefsMap(pathwaySers);
				// create GSEA/GMT entries - one entry per organism (null organism also makes one)
				String dataSource = getDataSource(currentPathway.getDataSource());
				LOG.debug("- creating GSEA/GMT entries...");
				Collection<GSEAEntry> entries = createGseaEntries(currentPathwayName, dataSource, orgToPrsMap);
				if(!entries.isEmpty())
					toReturn.addAll(entries);
				sequenceEntityReferences.removeAll(pathwaySers);//keep not processed PRs (a PR can be processed multiple times)
				LOG.debug("- collected " + entries.size() + "entries.");
			}
		}
		
		//when there're no pathways, only empty pathays, pathways w/o PRs, then use all/rest of PRs -
		//organize PRs by species (GSEA s/w can handle only same species identifiers in a data row)
		if(!sequenceEntityReferences.isEmpty() && !skipOutsidePathways) {
			LOG.info("Creating entries for the rest of PRs (outside any pathway)...");
			Map<String,Set<SequenceEntityReference>> orgToPrsMap = organismToProteinRefsMap(sequenceEntityReferences);
			if(!orgToPrsMap.isEmpty()) {
				// create GSEA/GMT entries - one entry per organism (null organism also makes one) 
				toReturn.addAll(createGseaEntries("Not pathway",
						getDataSource(l3Model.getObjects(Provenance.class)), orgToPrsMap));
			}
		}
					
		return toReturn;
	}

	private Collection<GSEAEntry> createGseaEntries(final String name, final String dataSource, 
			final Map<String, Set<SequenceEntityReference>> orgToPrsMap)
	{
		// generate GSEA entries for each taxId
		final Collection<GSEAEntry> toReturn = new ArrayList<GSEAEntry>();
		for (final String org : orgToPrsMap.keySet()) {
			if(orgToPrsMap.get(org).size() > 0) {
				LOG.debug("adding " + database + " IDs of " + org +
						" proteins (PRs) from '" + name + "', " + dataSource + " pathway...");
				GSEAEntry gseaEntry = new GSEAEntry(name, org, database, "datasource: " + dataSource);
				processEntityReferences(orgToPrsMap.get(org), gseaEntry);
				toReturn.add(gseaEntry);
			}
		}
		return toReturn;
	}

	
	//warn: there can be many equivalent BioSource objects (same taxonomy id, different URIs)
	private Map<String, Set<SequenceEntityReference>> organismToProteinRefsMap(Set<SequenceEntityReference> seqErs)
	{
		Map<String,Set<SequenceEntityReference>> map = new HashMap<String, Set<SequenceEntityReference>>();

		if(seqErs.isEmpty())
			throw new IllegalArgumentException("Empty set");
		
		if (crossSpeciesCheckEnabled) {
			for (SequenceEntityReference r : seqErs) {
				String key = getOrganismKey(r.getOrganism()); // null org. is ok (key == "")
				//collect PRs only from allowed organisms
				if(allowedOrganisms==null || allowedOrganisms.isEmpty() || allowedOrganisms.contains(key)) {
					Set<SequenceEntityReference> sers = map.get(key);
					if (sers == null) {
						sers = new HashSet<SequenceEntityReference>();
						map.put(key, sers);
					}
					sers.add(r);
				}
			}
		} else {
			final Set<SequenceEntityReference> sers = new HashSet<SequenceEntityReference>();
			for (SequenceEntityReference r : seqErs) {
				String key = getOrganismKey(r.getOrganism());
				//collect PRs only from allowed organisms
				if(allowedOrganisms==null || allowedOrganisms.isEmpty() || allowedOrganisms.contains(key)) {
					sers.add(r);
				}
			}
			map.put("", sers);
		}
				
		return map;
	}


	void processEntityReferences(Set<SequenceEntityReference> sers, GSEAEntry targetEntry)
	{
		prs_loop: for (SequenceEntityReference ser : sers)
		{
			// process PRs that belong to the same species (as targetEntry's) if crossSpeciesCheckEnabled==true
			if (crossSpeciesCheckEnabled && !targetEntry.taxID().equals(getOrganismKey(ser.getOrganism())))
				continue;
				
			if (database != null && !database.isEmpty())
			{
				final String db = database.toLowerCase();
				// a shortcut if we are converting validated normalized BioPAX model:
				// get the primary ID from the URI of the ProteinReference
				final String uri = ser.getUri();
				if (uri.startsWith("http://identifiers.org/") && uri.contains(db))
				{
					String accession = ser.getUri();
					accession = accession.substring(accession.lastIndexOf("/") + 1);
					targetEntry.getIdentifiers().add(accession);
				}
				else {
					int added = 0;
					for (Xref aXref : ser.getXref())
					{
						if(aXref instanceof UnificationXref
								&& aXref.getId() != null && aXref.getDb() != null
								&& aXref.getDb().toLowerCase().startsWith(db))
						{
							targetEntry.getIdentifiers().add(aXref.getId());
							++added;
						}
					}
					if(added == 0) for (Xref aXref : ser.getXref())
					{
						if(aXref instanceof RelationshipXref
								&& aXref.getId() != null && aXref.getDb() != null
								&& aXref.getDb().toLowerCase().startsWith(db))
						{
							targetEntry.getIdentifiers().add(aXref.getId());
							++added;
						}
					}

					if(added > 12)
						LOG.info("In GSEA entry: " + targetEntry.taxID() + " " + targetEntry.name() +
								", sER " + ser.getUri() + " got " + added + " '" + db + "' identifiers...");
				}
			} else {
				// fallback - use URI (not really useful for the GSEA software)
				targetEntry.getIdentifiers().add(ser.getUri());
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

	
	private String getOrganismKey(BioSource org) {
		String key = ""; //default value: unspecified/all species

		if (org != null) {
			Set<Xref> xrefs = org.getXref();

			if(!xrefs.isEmpty()) {
				for (Xref xref : xrefs) {
					if (xref instanceof UnificationXref
						&& xref.getDb().equalsIgnoreCase("taxonomy")) {
							if(key.isEmpty())
								key = xref.getId();
							else
								LOG.warn("BioSource " + org + " has multiple taxonomy unification xrefs; " +
										"I will use " + key);
					}
				}
			}

			//when there're no Taxonomy xrefs - use a name
			if(key.isEmpty()) {
				if (org.getStandardName()!=null) {
					key = org.getStandardName().toLowerCase();
				} else if(org.getDisplayName()!=null) {
					key = org.getDisplayName().toLowerCase();
				} else if(!org.getName().isEmpty()) {
					key = org.getName().iterator().next().toLowerCase();
				}
			}
		}

		return key;
	}

	private boolean shareSomeObjects(Set<?> setA, Set<?> setB) {
		return (!setA.isEmpty() && !setB.isEmpty())	? !CollectionUtils.intersection(setA, setB).isEmpty() : false;
	}
}
