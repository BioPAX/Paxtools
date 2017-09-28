package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.Level3Element;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PublicationXref;

import java.util.*;
import java.util.logging.Level;

/**
 * @author Ozgun Babur
 */
public class SIFInteraction implements Comparable
{
	public Set<BioPAXElement> sourceERs;
	public Set<BioPAXElement> targetERs;
	public Set<BioPAXElement> sourcePEs;
	public Set<BioPAXElement> targetPEs;
	public String sourceID;
	public String targetID;
	public SIFType type;
	public Set<BioPAXElement> mediators;

	public SIFInteraction(String sourceID, String targetID, BioPAXElement sourceER,
		BioPAXElement targetER, SIFType type, Set<BioPAXElement> mediators,
		Set<BioPAXElement> sourcePEs, Set<BioPAXElement> targetPEs)
	{
		this.sourceID = sourceID;
		this.targetID = targetID;
		this.sourceERs = new HashSet<BioPAXElement>();
		this.targetERs = new HashSet<BioPAXElement>();

		this.sourceERs.add(sourceER);
		this.targetERs.add(targetER);
		this.type = type;

		this.mediators = mediators;
		this.sourcePEs = sourcePEs;
		this.targetPEs = targetPEs;
	}

	public boolean hasIDs()
	{
		return sourceID != null && targetID != null;
	}

	@Override
	public int hashCode()
	{
		return sourceID.hashCode() + targetID.hashCode() + type.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof SIFInteraction)
		{
			SIFInteraction i = (SIFInteraction) obj;

			return i.type.equals(type) && i.sourceID.equals(sourceID) && i.targetID.equals(targetID);
		}

		return false;
	}

	@Override
	public int compareTo(Object o)
	{
		if (o instanceof SIFInteraction)
		{
			SIFInteraction i = (SIFInteraction) o;

			return (sourceID + targetID + type.getTag()).compareTo(
				i.sourceID + i.targetID + i.type.getTag());
		}

		return 0;
	}

	/**
	 * Merges publications of the parameter equivalent sif with this one.
	 * @param equivalent the equivalent sif interaction to get its publications.
	 */
	public void mergeWith(SIFInteraction equivalent)
	{
		if (!this.equals(equivalent))
			throw new IllegalArgumentException("SIF interactions are not equivalent.");

		sourceERs.addAll(equivalent.sourceERs);
		targetERs.addAll(equivalent.targetERs);

		sourcePEs.addAll(equivalent.sourcePEs);
		targetPEs.addAll(equivalent.targetPEs);

		if (mediators == null) mediators = equivalent.mediators;
		else if (equivalent.mediators != null)
		{
			mediators.addAll(equivalent.mediators);
		}
	}

	@Override
	public String toString()
	{
		return sourceID + "\t" + type.getTag() + "\t" + targetID;
	}

	/**
	 * Collects IDs of mediators.
	 * @return mediator IDs
	 */
	public List<String> getMediatorIDs()
	{
		List<String> ids = new ArrayList<String>(mediators.size());

		for (BioPAXElement ele : mediators)
		{
			ids.add(ele.getUri());
		}
		return ids;
	}

	/**
	 * Gets the mediator IDs in a String with a space between each ID.
	 *
	 * @return mediator IDs joined with spaces.
	 */
	public String getMediatorsInString()
	{
		String m = "";
		for (String mid : getMediatorIDs())
		{
			m+= " " + mid;
		}
		return m.trim().replaceAll(" ", ";");
	}

	/**
	 * Collects Pubmed or PMC ids
	 * @return publication IDs
	 */
	public List<String> getPublicationIDs(boolean pubmed)
	{
		if (mediators == null) return Collections.emptyList();

		Set<PublicationXref> xrefs = harvestPublicationXrefs(mediators.toArray(new BioPAXElement[mediators.size()]));
		Set<String> set = pubmed ? harvestPMIDs(xrefs) : harvestPMCIDs(xrefs);

		List<String> list = new ArrayList<String>(set);
		Collections.sort(list);

		return list;
	}

	private static final PathAccessor xrefAcc = new PathAccessor("XReferrable/xref:PublicationXref");
	private static final PathAccessor evidAcc = new PathAccessor("Observable/evidence/xref:PublicationXref");

	/**
	 * Collects publication xrefs of the given elements.
	 * @param ele element array
	 * @return publication xrefs
	 */
	private Set<PublicationXref> harvestPublicationXrefs(BioPAXElement... ele)
	{
		Set<PublicationXref> set = new HashSet<PublicationXref>();

		for (Object o : xrefAcc.getValueFromBeans(Arrays.asList(ele)))
		{
			set.add((PublicationXref) o);
		}
		for (Object o : evidAcc.getValueFromBeans(Arrays.asList(ele)))
		{
			set.add((PublicationXref) o);
		}
		return set;
	}

	/**
	 * Collects PubMed IDs from the given publication xrefs.
	 * @param xrefs publication xrefs
	 * @return PMIDs
	 */
	private Set<String> harvestPMIDs(Set<PublicationXref> xrefs)
	{
		return harvestXrefs(xrefs, "pubmed");
	}

	/**
	 * Collects PubMed Central IDs from the given publication xrefs.
	 * @param xrefs publication xrefs
	 * @return PMIDs
	 */
	private Set<String> harvestPMCIDs(Set<PublicationXref> xrefs)
	{
		return harvestXrefs(xrefs, "PMC International");
	}

	/**
	 * Collects things from given xrefs.
	 * @param xrefs
	 * @return whatever collected
	 */
	private Set<String> harvestXrefs(Set<PublicationXref> xrefs, String dbID)
	{
		Set<String> set = new HashSet<String>();

		for (PublicationXref xref : xrefs)
		{
			if (xref.getDb() != null && xref.getDb().equalsIgnoreCase(dbID))
				if (xref.getId() != null && !xref.getId().isEmpty())
					set.add(xref.getId());
		}
		return set;
	}

	private static final PathAccessor[] pathwayAcc = new PathAccessor[]{
		new PathAccessor("Interaction/pathwayComponentOf"),
		new PathAccessor("Complex/participantOf/pathwayComponentOf"),
		new PathAccessor("Interaction/stepProcessOf/pathwayOrderOf"),
		new PathAccessor("Complex/participantOf/stepProcessOf/pathwayOrderOf"),
	};

	/**
	 * Collects Pathway objects that the Interactions among the mediators are members.
	 * @return related pathways
	 */
	public Set<Pathway> getPathways()
	{
		Set<Pathway> set = new HashSet<Pathway>();

		for (PathAccessor pAcc : pathwayAcc)
		{
			for (Object o : pAcc.getValueFromBeans(mediators))
			{
				set.add((Pathway) o);
			}
		}

		return set;
	}

	/**
	 * Collects the names of the related pathways.
	 * @return pathway names
	 */
	public List<String> getPathwayNames()
	{
		Set<Pathway> set = getPathways();
		List<String> names = new ArrayList<String>();

		for (Pathway p : set)
		{
			String name = p.getDisplayName();
			if (!names.contains(name)) names.add(name);
		}

		Collections.sort(names);
		return names;
	}

	private static final PathAccessor dataSourceAcc = new PathAccessor("Entity/dataSource/displayName");

	/**
	 * Collects data source names (Provenance display names) of mediators.
	 * @return related data sources
	 */
	public Set<String> getDataSources()
	{
		Set<String> set = new HashSet<String>();

		for (Object o : dataSourceAcc.getValueFromBeans(mediators))
		{
			set.add((String) o);
		}

		return set;
	}

	private static final PathAccessor locAcc = new PathAccessor("PhysicalEntity/cellularLocation/term");

	/**
	 * Collects cellular location terms of target objects.
	 * @return cellular locations
	 */
	public Set<String> getCellularLocationsOfTarget()
	{
		return getCellularLocations(targetPEs);
	}

	/**
	 * Collects cellular location terms of source objects.
	 * @return cellular locations
	 */
	public Set<String> getCellularLocationsOfSource()
	{
		return getCellularLocations(sourcePEs);
	}

	/**
	 * Collects cellular location terms of related objects.
	 * @return cellular locations
	 */
	private Set<String> getCellularLocations(Set<BioPAXElement> eles)
	{
		Set<String> set = new HashSet<String>();

		for (Object o : locAcc.getValueFromBeans(eles))
		{
			set.add((String) o);
		}

		return set;
	}

	/**
	 * Collects comments strings of mediator objects.
	 * @return comments
	 */
	public Set<String> getMediatorComments()
	{
		Set<String> comments = new HashSet<String>();
		for (BioPAXElement med : mediators)
		{
			if (med instanceof Level3Element) comments.addAll(((Level3Element) med).getComment());
		}
		return comments;
	}

	//---- Section: Static methods ----------------------------------------------------------------|

	/**
	 * Collects and sorts sourceID and targetID of the given collection of sif interactions.
	 * @param sifInts interactions to consider
	 * @param type types of interest, all types accepted if empty
	 * @return sorted source and target IDS
	 */
	public static List<String> getSortedGeneNames(Collection<SIFInteraction> sifInts,
		SIFType... type)
	{
		Set<String> genes = new HashSet<String>();
		Set<SIFType> types = new HashSet<SIFType>(Arrays.asList(type));

		// collect gene names

		for (SIFInteraction sifInt : sifInts)
		{
			if (!types.isEmpty() && !types.contains(sifInt.type)) continue;

			genes.add(sifInt.sourceID);
			genes.add(sifInt.targetID);
		}

		// sort all gene names

		List<String> names = new ArrayList<String>(genes);
		Collections.sort(names);

		return names;
	}

	/**
	 * Converts the given collection of interactions into an adjacency matrix.
	 * @param sifInts interactions to consider
	 * @param type types of interest, all types accepted if empty
	 * @return the sif network as adjacency matrix
	 */
	public static boolean[][] convertToAdjacencyMatrix(Collection<SIFInteraction> sifInts,
		SIFType... type)
	{
		Set<SIFType> types = new HashSet<SIFType>(Arrays.asList(type));

		// collect gene names
		List<String> names = getSortedGeneNames(sifInts, type);

		// record name indexes for efficient lookup

		Map<String, Integer> name2ind = new HashMap<String, Integer>();
		int i = 0;
		for (String name : names)
		{
			name2ind.put(name, i++);
		}

		// generate adjacency matrix

		boolean[][] matrix = new boolean[names.size()][names.size()];
		for (boolean[] m : matrix) Arrays.fill(m, false);

		for (SIFInteraction sifInt : sifInts)
		{
			if (!types.isEmpty() && !types.contains(sifInt.type)) continue;

			matrix[name2ind.get(sifInt.sourceID)][name2ind.get(sifInt.targetID)] = true;

			if (!sifInt.type.isDirected())
			{
				matrix[name2ind.get(sifInt.targetID)][name2ind.get(sifInt.sourceID)] = true;
			}
		}

		return matrix;
	}
}
