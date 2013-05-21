package org.biopax.paxtools.pattern.miner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class SIFInteraction implements Comparable
{
	public SIFInteraction(String source, String target, String type, boolean directed,
		Set<String> publications)
	{
		if (!directed)
		{
			if (source.compareTo(target) > 0)
			{
				String temp = source;
				source = target;
				target = temp;
			}
		}

		this.source = source;
		this.target = target;
		this.type = type;
		this.directed = directed;
		if (publications != null)
		{
			this.pubmedIDs = new ArrayList<String>(publications);
			Collections.sort(pubmedIDs);
		}
	}

	public String source;
	public String target;
	public String type;
	public boolean directed;
	public List<String> pubmedIDs;

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

			return i.type.equals(type) && i.source.equals(source) && i.target.equals(target);
		}

		return false;
	}


	@Override
	public int compareTo(Object o)
	{
		if (o instanceof SIFInteraction)
		{
			SIFInteraction i = (SIFInteraction) o;

			return (source + target).compareTo(i.source + i.target);
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
		return source + "\t" + type + "\t" + target;
	}
}
