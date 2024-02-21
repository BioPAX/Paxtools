package org.biopax.paxtools.io.gsea;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.biopax.paxtools.controller.*;
import org.biopax.paxtools.converter.LevelUpgrader;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.normalizer.Resolver;
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
 * It creates GSEA entries from sequence entity reference xrefs
 * in the BioPAX model as follows:
 *
 *   Each entry (row) consists of three columns (tab separated):
 * name (we use pathway URI),
 * description (e.g. "name: Apoptosis; datasource: reactome; organism: 9606 idtype: uniprot"),
 * and the list of identifiers (of the same type). For participants not associated with any pathway,
 * "other" is used for the pathway name and uri.
 *
 *   The list may have one or more IDs of the same type per entity reference,
 * e.g., UniProt IDs or HGNC Symbols; entity references that do not have any xref of
 * given db/id type are ignored. Optionally, if there are less than three protein
 * references per entry, it will not be printed.
 * 
 * Note, to effectively enforce cross-species violation, 
 * 'organism' property and pathways must be set
 * to a BioSource object that has a valid unification xref: 
 * db="Taxonomy" and id= some valid taxonomy id.
 *
 * Note, this code assumes that the model has successfully been validated
 * and perhaps normalized (using the BioPAX Validator, Paxtools Normalizer).
 * A BioPAX L1 or L2 model is first converted to the L3.
 */
public class GSEAConverter
{
	private final static Logger LOG = LoggerFactory.getLogger(GSEAConverter.class);
	
	private final String idType;
	private final boolean crossSpeciesCheckEnabled;
	private Collection<String> allowedOrganisms;
	private final boolean skipSubPathways;
	private Collection<Provenance> skipSubPathwaysOf;
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
	 * @param idType - identifier collection/type name, prefix - either the string value
	 *                   of the most of EntityReference's xref.db property values in the BioPAX data,
	 *                   e.g., "hgnc.symbol", or "HGNC Symbol", "NCBI Gene", "RefSeq", "UniProt", -
	 *                   or the namespace/prefix part of the normalized EntityReference URIs,
	 *                   such as 'chebi' in identifiers.org/chebi:ID or bioregistry.io/chebi:ID
	 *                   (it depends on actual data; so check/verify before using here).
	 * @param crossSpeciesCheckEnabled - if true, enforces no cross species participants in output
	 */
	public GSEAConverter(String idType, boolean crossSpeciesCheckEnabled)
	{
		this(idType, crossSpeciesCheckEnabled, false);
	}

	/**
	 * Constructor.
	 *
	 * See class declaration for more information.
	 * @param idType - identifier collection/type name, prefix - either the string value
	 *                  of the most of EntityReference's xref.db property values in the BioPAX data,
	 *                  e.g., "hgnc.symbol", or "HGNC Symbol", "NCBI Gene", "RefSeq", "UniProt", -
	 *                  or the namespace/prefix part of the normalized EntityReference URIs,
	 *                  such as 'chebi' in identifiers.org/chebi:ID or bioregistry.io/chebi:ID
	 *                  (it depends on actual data; so check/verify before using here).
	 * @param crossSpeciesCheckEnabled - if true, enforces no cross species participants in output
	 * @param skipSubPathways - if true, do not traverse into any sub-pathways to collect entity references
	 *                       (useful when a model, such as converted to BioPAX KEGG data, has lots of sub-pathways, loops.)
	 */
	public GSEAConverter(String idType, boolean crossSpeciesCheckEnabled, boolean skipSubPathways)
	{
		if(Resolver.isKnownNameOrVariant(idType)) {
			this.idType = Resolver.getNamespace(idType).getPrefix().toLowerCase(); //(it must be already lowecase though)
		} else {
			this.idType = (StringUtils.isNotBlank(idType)) ? idType.toLowerCase() : idType;
		}
		this.crossSpeciesCheckEnabled = crossSpeciesCheckEnabled;
		this.skipSubPathways = skipSubPathways;
		this.skipSubPathwaysOf = Collections.emptySet();
	}

	/**
	 * Constructor.
	 *
	 * See class declaration for more information.
	 * @param idType identifier collection/type name, prefix - either the string value
	 *                of the most of EntityReference's xref.db property values in the BioPAX data,
	 *                e.g., "hgnc.symbol", or "HGNC Symbol", "NCBI Gene", "RefSeq", "UniProt", -
	 *                or the namespace/prefix part of the normalized EntityReference URIs,
	 *                such as 'chebi' in identifiers.org/chebi:ID or bioregistry.io/chebi:ID
	 *                (it depends on actual data; so check/verify before using here).
	 * @param crossSpeciesCheckEnabled - if true, enforces no cross species participants in output
	 * @param skipSubPathwaysOf - do not look inside sub-pathways of pathways of given data sources to collect entity references
	 *                       (useful when a model, such as converted to BioPAX KEGG data, has lots of sub-pathways, loops.)
	 */
	public GSEAConverter(String idType, boolean crossSpeciesCheckEnabled, Collection<Provenance> skipSubPathwaysOf)
	{
		this(idType, crossSpeciesCheckEnabled, false);
		this.skipSubPathwaysOf = (skipSubPathwaysOf == null) ? Collections.emptySet() : skipSubPathwaysOf;
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

	public Collection<String> getAllowedOrganisms() {
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
		Collection<GMTEntry> entries = convert(model);
		if (entries.size() > 0)
		{
			Writer writer = new OutputStreamWriter(out);
			for (GMTEntry entry : entries) {
				if ((minNumOfGenesPerEntry <= 1 && !entry.identifiers().isEmpty())
						|| entry.identifiers().size() >= minNumOfGenesPerEntry)
				{
					writer.write(entry + "\n");
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
	public Collection<GMTEntry> convert(final Model model)
	{
		final Collection<GMTEntry> toReturn = new TreeSet<>(Comparator.comparing(GMTEntry::toString));

		Model l3Model;
		// convert to level 3 in necessary
		if (model.getLevel() == BioPAXLevel.L2)
			l3Model = (new LevelUpgrader()).filter(model);
		else
			l3Model = model;
		
		//a modifiable copy of the set of all PRs in the model - 
		//after all, it has all the PRs that do not belong to any pathway
		final Collection<SequenceEntityReference> sequenceEntityReferences =
				new ArrayList<>(l3Model.getObjects(SequenceEntityReference.class));

		final Collection<Pathway> pathways = l3Model.getObjects(Pathway.class);
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

			// collect sequence entity references from current pathway
			Fetcher fetcher = new Fetcher(SimpleEditorMap.L3, Fetcher.nextStepFilter);
			fetcher.setSkipSubPathways(ignoreSubPathways);
			Set<SequenceEntityReference> pathwaySers = fetcher.fetch(currentPathway, SequenceEntityReference.class);

			if(!pathwaySers.isEmpty()) {
				LOG.debug("For pathway: " + currentPathwayName + " (" + currentPathway.getUri()
						+ "), got " + pathwaySers.size() + " sERs; now - grouping by organism...");
				Map<String, Collection<SequenceEntityReference>> orgToPrsMap = organismToProteinRefsMap(pathwaySers);
				// create GSEA/GMT entries - one entry per organism (null organism also makes one)
				String dataSource = getDataSource(currentPathway.getDataSource());
				Collection<GMTEntry> entries = createGseaEntries(currentPathway.getUri(),
						currentPathwayName, dataSource, orgToPrsMap);
				if(!entries.isEmpty())
					toReturn.addAll(entries);
				sequenceEntityReferences.removeAll(pathwaySers);//keep not processed PRs (can be processed multiple times)
				LOG.debug("- collected " + entries.size() + " entries.");
			}
		}
		
		//when there are no pathways, only empty pathays, pathways w/o PRs, then use all/rest of PRs -
		//organize PRs by species (GSEA s/w can handle only same species identifiers in a data row)
		if(!sequenceEntityReferences.isEmpty() && !skipOutsidePathways) {
			LOG.info("Creating entries for the rest of PRs (outside any pathway)...");
			Map<String, Collection<SequenceEntityReference>> orgToPrsMap =
				organismToProteinRefsMap(sequenceEntityReferences);
			if(!orgToPrsMap.isEmpty()) {
				// create GSEA/GMT entries - one entry per organism (null organism also makes one)
				if(model.getUri()==null) {
					toReturn.addAll(createGseaEntries(
						"other", "other", getDataSource(l3Model.getObjects(Provenance.class)), orgToPrsMap));
				} else {
					toReturn.addAll(createGseaEntries(model.getUri(), model.getName(),
							getDataSource(l3Model.getObjects(Provenance.class)), orgToPrsMap));
				}
			}
		}
					
		return toReturn;
	}

	private Collection<GMTEntry> createGseaEntries(
		String uri, String name, String dataSource,
		Map<String, Collection<SequenceEntityReference>> orgToPrsMap)
	{
		// generate GSEA entries for each species
		final Collection<GMTEntry> toReturn = new ArrayList<>();
		for (final String org : orgToPrsMap.keySet()) {
			if(orgToPrsMap.get(org).size() > 0) {
				GMTEntry GMTEntry = new GMTEntry(uri, org, idType,
						String.format("name: %s; datasource: %s", name, dataSource));
				processEntityReferences(orgToPrsMap.get(org), GMTEntry);
				toReturn.add(GMTEntry);
			}
		}
		return toReturn;
	}

	
	//warn: there can be many equivalent BioSource objects (same taxonomy id, different URIs)
	private Map<String, Collection<SequenceEntityReference>> organismToProteinRefsMap(
		Collection<SequenceEntityReference> seqErs)
	{
		Map<String, Collection<SequenceEntityReference>> map = new HashMap<>();

		if(seqErs.isEmpty())
			throw new IllegalArgumentException("Empty set");
		
		if (crossSpeciesCheckEnabled) {
			for (SequenceEntityReference r : seqErs) {
				String key = getOrganismKey(r.getOrganism()); // null org. is ok (key == "")
				//collect PRs only from allowed organisms
				if(allowedOrganisms==null || allowedOrganisms.isEmpty() || allowedOrganisms.contains(key))
				{
					Collection<SequenceEntityReference> sers = map.get(key);
					if (sers == null) {
						sers = new HashSet<>();
						map.put(key, sers);
					}
					sers.add(r);
				}
			}
		} else {
			final Set<SequenceEntityReference> sers = new HashSet<>();
			for (SequenceEntityReference r : seqErs) {
				String key = getOrganismKey(r.getOrganism());
				//collect PRs only from allowed organisms
				if(allowedOrganisms==null || allowedOrganisms.isEmpty() || allowedOrganisms.contains(key))
				{
					sers.add(r);
				}
			}
			map.put("", sers);
		}
				
		return map;
	}


	void processEntityReferences(Collection<SequenceEntityReference> sers, GMTEntry targetEntry)
	{
		for (SequenceEntityReference ser : sers)
		{
			// process PRs that belong to the same species (as targetEntry's) if crossSpeciesCheckEnabled==true
			if (crossSpeciesCheckEnabled && !targetEntry.taxID().equals(getOrganismKey(ser.getOrganism()))) {
				continue;
			}

			if (StringUtils.isNotBlank(idType))
			{
				int added = 0;
				for (Xref aXref : ser.getXref())
				{
					if(aXref instanceof UnificationXref
							&& aXref.getId() != null && aXref.getDb() != null
							&& Resolver.isKnownNameOrVariant(aXref.getDb())
							&& StringUtils.equalsIgnoreCase(idType, Resolver.getNamespace(aXref.getDb(), true).getPrefix()))
					{
						targetEntry.identifiers().add(aXref.getId());
						++added;
					}
				}
				if(added == 0) {
					for (Xref aXref : ser.getXref())
					{
						if(aXref instanceof RelationshipXref
								&& aXref.getId() != null && aXref.getDb() != null
								&& Resolver.isKnownNameOrVariant(aXref.getDb())
								&& StringUtils.equalsIgnoreCase(idType, Resolver.getNamespace(aXref.getDb(), true).getPrefix()))
						{
							targetEntry.identifiers().add(aXref.getId());
							++added;
						}
					}
				}
				if(added > 12) {
					LOG.info("In GSEA entry: " + targetEntry.taxID() + " " + targetEntry.name() +
							", sER " + ser.getUri() + " got " + added + " '" + idType + "' identifiers...");
				}
			} else {
				// fallback - use URI (not really useful for the GSEA software)
				targetEntry.identifiers().add(ser.getUri());
			}
		}
	}

	/*
	 * Gets datasource names, if any, in a consistent way/order, excl. duplicates
	 */
	private String getDataSource(Collection<Provenance> provenances)
	{
		if(provenances.isEmpty()) return "N/A";
		
		Set<String> dsNames = new TreeSet<>();
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

	private boolean shareSomeObjects(Collection<?> setA, Collection<?> setB) {
		return (!setA.isEmpty() && !setB.isEmpty())	? !CollectionUtils.intersection(setA, setB).isEmpty() : false;
	}
}
