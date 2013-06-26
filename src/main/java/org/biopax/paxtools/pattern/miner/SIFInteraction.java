package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.XReferrable;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.paxtools.pattern.util.HGNC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
	public List<String> pubmedIDs;

	public SIFInteraction(BioPAXElement source, BioPAXElement target, SIFType type,
		Set<String> publications, IDFetcher fetcher)
	{
		sourceID = fetcher.fetchID(source);
		targetID = fetcher.fetchID(target);

		if (!type.isDirected())
		{
			if (sourceID != null && targetID != null && sourceID.compareTo(targetID) > 0)
			{
				BioPAXElement temp = source;
				source = target;
				target = temp;
			}
		}

		this.source = source;
		this.target = target;
		this.type = type;

		if (publications != null)
		{
			this.pubmedIDs = new ArrayList<String>(publications);
			Collections.sort(pubmedIDs);
		}
	}

	public boolean hasIDs()
	{
		return sourceID != null && targetID != null;
	}

	@Override
	public int hashCode()
	{
		return source.hashCode() + target.hashCode() + type.hashCode();
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

			return (sourceID + targetID).compareTo(i.sourceID + i.targetID);
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

		if (pubmedIDs == null) pubmedIDs = equivalent.pubmedIDs;
		else if (equivalent.pubmedIDs != null)
		{
			for (String id : equivalent.pubmedIDs)
			{
				if (!pubmedIDs.contains(id)) pubmedIDs.add(id);
			}
		}
	}

	@Override
	public String toString()
	{
		return sourceID + "\t" + type.getTag() + "\t" + targetID;
	}

}
