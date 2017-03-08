package org.biopax.paxtools.io.gsea;

import org.apache.commons.lang3.StringUtils;
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
 * An advanced BioPAX to GMT format converter, which can output IDs of
 * both (or either) genetic elements and chemicals
 * (the output file may be run with the GSEA software if gene/protein IDs are there used).
 * 
 *     Each output entry (row) consists of three columns (tab separated):
 * name (URI), description, and the list of identifiers (of the same type).
 * For all ERs not associated with any pathway, "other" is used for name and uri.
 *
 *     The "idtype" is what specified by Constructor parameter 'idType'.
 *
 *     The list may have one or more IDs of the same type per PR,
 * e.g., UniProt IDs or HGNC Symbols; PRs not having an xref of 
 * given db/id type are ignored. If there are less than three protein 
 * references per entry, it will not be printed.
 *
 * Note, this code assumes that the model has successfully been validated
 * and perhaps normalized (using the BioPAX Validator, Paxtools Normalizer).
 * A BioPAX L1 or L2 model is first converted to the L3.
 *
 * TODO: work in progress; add ER sub-class parameter/filter; consider using PE's xrefs as well... make public.
 */
final class GMTConverter
{
	private final static Logger LOG = LoggerFactory.getLogger(GMTConverter.class);

	private final IdFetcher idFetcher;
	private boolean skipSubPathways;
	private boolean skipOutsidePathways;
	private int minNumIdsPerEntry;

	/**
	 * Constructor.
	 */
	public GMTConverter()
	{
		idFetcher = new IdFetcher().chemDbStartsWithOrEquals("chebi")
			.seqDbStartsWithOrEquals("hgnc symbol").seqDbStartsWithOrEquals("hgnc");
		skipSubPathways = true;
		minNumIdsPerEntry = 1;
		skipOutsidePathways = false;
	}

	public boolean isSkipSubPathways() {
		return skipSubPathways;
	}

	public void setSkipSubPathways(boolean skipSubPathways) {
		this.skipSubPathways = skipSubPathways;
	}

	/**
	 * If true, then only GMT entries that (genes) correspond to a Pathway
	 * are printed to the output.
	 * @return true/false
     */
	public boolean isSkipOutsidePathways() {
		return skipOutsidePathways;
	}

	public void setSkipOutsidePathways(boolean skipOutsidePathways) {
		this.skipOutsidePathways = skipOutsidePathways;
	}

	/**
	 * If this value is greater than 0, and the number of proteins/genes
	 * in a gene set is less than that value, then this gene set is to skip
	 * (no GMT entry is written).
	 * @return the min. value
     */
	public int getMinNumIdsPerEntry() {
		return minNumIdsPerEntry;
	}

	public void setMinNumIdsPerEntry(int minNumIdsPerEntry) {
		this.minNumIdsPerEntry = minNumIdsPerEntry;
	}

	/**
	 * Converts model to GMT and writes to out.
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
				if ((minNumIdsPerEntry <= 1 && !entry.identifiers().isEmpty())
						|| entry.identifiers().size() >= minNumIdsPerEntry)
				{
					writer.write(entry.toString() + "\n");
				}
			}
			writer.flush();
		}
	}

	/**
	 * Creates GMT entries from the pathways contained in the model.
	 * @param model Model
	 * @return a set of GMT entries
	 */
	public Collection<GMTEntry> convert(final Model model)
	{
		final Collection<GMTEntry> toReturn = new TreeSet<GMTEntry>(new Comparator<GMTEntry>() {
			@Override
			public int compare(GMTEntry o1, GMTEntry o2) {
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
		//after all, it has all the ERs that do not belong to any pathway
		final Set<EntityReference> entityReferences =
				new HashSet<EntityReference>(l3Model.getObjects(EntityReference.class));

		final Set<Pathway> pathways = l3Model.getObjects(Pathway.class);
		for (Pathway pathway : pathways)
		{
			String name = (pathway.getDisplayName() == null) ? pathway.getStandardName() : pathway.getDisplayName();
			if(name == null || name.isEmpty())
				name = pathway.getUri();

			final Pathway currentPathway = pathway;
			final String currentPathwayName = name;

			LOG.debug("Begin converting " + currentPathwayName + " pathway, uri=" + currentPathway.getUri());
			final Set<EntityReference> ers = new HashSet<EntityReference>();
			final Traverser traverser = new AbstractTraverser(SimpleEditorMap.L3,
					Fetcher.nextStepFilter, Fetcher.objectPropertiesOnlyFilter) {
				@Override
				protected void visit(Object range, BioPAXElement domain, Model model, PropertyEditor editor)
				{
					BioPAXElement bpe = (BioPAXElement) range; //cast is safe (due to objectPropertiesOnlyFilter)
					if(bpe instanceof EntityReference) {
						ers.add((EntityReference) bpe);
					}
					if(bpe instanceof Pathway) {
						if(skipSubPathways)
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

			if(!ers.isEmpty()) {
				LOG.debug("For pathway: " + currentPathwayName + " (" + currentPathway.getUri()
						+ "), got " + ers.size() + " ERs");
				// create GMT entries
				Collection<GMTEntry> entries = createGseaEntries(currentPathway.getUri(),
						currentPathwayName, getDataSource(currentPathway.getDataSource()), ers);
				if(!entries.isEmpty())
					toReturn.addAll(entries);
				entityReferences.removeAll(ers);//keep not processed PRs (a PR can be processed multiple times)
				LOG.debug("- collected " + entries.size() + "entries.");
			}
		}

		//when there're no pathways, only empty pathays, pathways w/o PRs, then use all/rest of PRs -
		//organize PRs by species (GSEA s/w can handle only same species identifiers in a data row)
		if(!entityReferences.isEmpty() && !skipOutsidePathways) {
			LOG.info("Creating entries for the rest of PRs (outside any pathway)...");
			toReturn.addAll(createGseaEntries("other","other", getDataSource(l3Model.getObjects(Provenance.class)),entityReferences));
		}

		return toReturn;
	}

	private Collection<GMTEntry> createGseaEntries(String uri, final String name, final String dataSource,
												   final Set<EntityReference> ers)
	{
		final Collection<GMTEntry> toReturn = new ArrayList<GMTEntry>();
		GMTEntry entry = new GMTEntry(uri, "", "", String.format("name: %s; datasource: %s",name, dataSource));
		for (EntityReference er : ers)
			entry.identifiers().addAll(idFetcher.fetchID(er));
		toReturn.add(entry);
		return toReturn;
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

}
