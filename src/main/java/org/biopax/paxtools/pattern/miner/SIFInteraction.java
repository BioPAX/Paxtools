package org.biopax.paxtools.pattern.miner;

/**
 * @author Ozgun Babur
 */
public class SIFInteraction implements Comparable
{
	public SIFInteraction(String source, String target, String type, boolean directed)
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
	}

	public String source;
	public String target;
	public String type;
	public boolean directed;

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
}
