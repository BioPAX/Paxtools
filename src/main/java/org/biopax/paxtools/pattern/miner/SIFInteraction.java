package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PublicationXref;

import java.util.*;

/**
 * @author Ozgun Babur
 */
public class SIFInteraction implements Comparable
{
	public BioPAXElement source;
	public BioPAXElement target;
	public String sourceID;
	public String targetID;
	public SIFType type;
	public Set<BioPAXElement> mediators;

	public SIFInteraction(BioPAXElement source, BioPAXElement target, SIFType type,
		Set<BioPAXElement> mediators, IDFetcher fetcher)
	{
		sourceID = fetcher.fetchID(source);
		targetID = fetcher.fetchID(target);

		if (!type.isDirected())
		{
			if (sourceID != null && targetID != null && sourceID.compareTo(targetID) > 0)
			{
				String tempID = sourceID;
				sourceID = targetID;
				targetID = tempID;

				BioPAXElement temp = source;
				source = target;
				target = temp;
			}
		}

		this.source = source;
		this.target = target;
		this.type = type;

		this.mediators = mediators;
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

		if (mediators == null) mediators = equivalent.mediators;
		else if (equivalent.mediators != null)
		{
			mediators.addAll(equivalent.mediators);
		}
	}

	@Override
	public String toString()
	{
		return toString(true);
	}

	public String toString(boolean withMediators)
	{
		String s = sourceID + "\t" + type.getTag() + "\t" + targetID;

		if (withMediators)
		{
			String m = getMediatorsInString();
			if (!m.isEmpty()) s += "\t" + m;
		}

		return s;
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
			ids.add(ele.getRDFId());
		}
		return ids;
	}

	/**
	 * Gets the mediator IDs in a String with a space between each ID.
	 */
	public String getMediatorsInString()
	{
		String m = "";
		for (String mid : getMediatorIDs())
		{
			m+= " " + mid;
		}
		return m.trim();
	}

	/**
	 * Collects PMIDs from mediators.
	 * @return PMIDs
	 */
	public List<String> getPubmedIDs()
	{
		if (mediators == null) return Collections.emptyList();

		Set<String> set = harvestPMIDs(harvestPublicationXrefs(
			mediators.toArray(new BioPAXElement[mediators.size()])));

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
	 * Collects PubMed IDs fromt the given publication xrefs.
	 * @param xrefs publication xrefs
	 * @return PMIDs
	 */
	private Set<String> harvestPMIDs(Set<PublicationXref> xrefs)
	{
		Set<String> set = new HashSet<String>();

		for (PublicationXref xref : xrefs)
		{
			if (xref.getDb() != null && xref.getDb().equalsIgnoreCase("pubmed"))
				if (xref.getId() != null && !xref.getId().isEmpty())
					set.add(xref.getId());
		}
		return set;
	}

	private static final PathAccessor pathwayAcc1 = new PathAccessor("Interaction/pathwayComponentOf*");
	private static final PathAccessor pathwayAcc2 = new PathAccessor("Interaction/stepProcessOf/pathwayOrderOf");

	/**
	 * Collects Pathway objects that the Interactions among the mediators are members.
	 * @return related pathways
	 */
	public Set<Pathway> getPathways()
	{
		Set<Pathway> set = new HashSet<Pathway>();

		for (Object o : pathwayAcc1.getValueFromBeans(mediators))
		{
			set.add((Pathway) o);
		}
		for (Object o : pathwayAcc2.getValueFromBeans(mediators))
		{
			set.add((Pathway) o);
		}

		return set;
	}
}
