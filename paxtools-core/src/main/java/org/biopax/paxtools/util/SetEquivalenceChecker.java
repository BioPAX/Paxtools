package org.biopax.paxtools.util;

import org.biopax.paxtools.model.BioPAXElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class performs set operations based on equivalence.
 */
public class SetEquivalenceChecker {

	private static final Logger LOG = LoggerFactory.getLogger(SetEquivalenceChecker.class);

	/**
	 * @param set1 First set to be checked.
	 * @param set2 Second set to be checked
	 * @param <T> Both sets should be of type that extends from T.
	 * @return true iff both sets are of equal size and all objects in set1 has an equivalent object in set2.
	 */
	public static <T extends BioPAXElement> boolean isEquivalent(Collection<? extends T> set1, Collection<? extends T> set2)
	{
		if (set1 != null && !set1.isEmpty() && set2 != null && !set2.isEmpty())
		{
			int size = set1.size();
			if (size == set2.size())
			{
				EquivalenceGrouper<T> grouper = new EquivalenceGrouper<>();
				grouper.addAll(set1);
				if (grouper.getBuckets().size() == size)
				{
					grouper.addAll(set2);
					return (grouper.getBuckets().size() == size);
				}
			}
		} 
		
		//now, only if both null or both empty - return true 
		if( set1 == null && set2 == null //both null
			|| // or both not null but empty
			set1 != null && set1.isEmpty() && set2 != null && set2.isEmpty()) 
		{ 
			return true;
		}
		
		return false;	
	}

	/**
	 * @param set Set to test if it contains an element equivalent to query
	 * @param query BPE to look for equivalents in set
	 * @return true iff there is an element of set that is equivalent to query.
	 */
	public static boolean containsEquivalent(Collection<? extends BioPAXElement> set, BioPAXElement query)
	{
		if (set != null && query != null)
		{
			for (BioPAXElement element2 : set)
			{
				if (query.isEquivalent(element2))
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @param set1 First set to be checked.
	 * @param set2 Second set to be checked
	 * @param <T> Both sets should be of type that extends from T.
	 * @return elements of set1 that has an equivalent element in set2
	 */
	public static <T extends BioPAXElement> Set<T> findEquivalentIntersection(Collection<? extends T> set1,
			Collection<? extends T> set2)
	{

		Set<T> value = new HashSet<>();

		if (set1 != null && !set1.isEmpty() && set2 != null && !set2.isEmpty())
		{
			EquivalenceGrouper<T> grouper = new EquivalenceGrouper<T>();
			grouper.addAll(set1);
			if (grouper.getBuckets().size() < set1.size())
			{
				throw new IllegalArgumentException("There should not be equivalent elements in a set");
			}
			grouper.addAll(set2);
			for (List<T> ts : grouper.getBuckets())
			{
				if (ts.size() > 1)
				{
					for (T t : ts)
					{
						if (set1.contains(t))
						{
							value.add(t);
						}
					}
				}
			}
		}
		return value;
	}

	/**
	 * @param set1 First set to be checked.
	 * @param set2 Second set to be checked
	 * @param <T> Both sets should be of type that extends from T.
	 * @return true iff there are at least one equivalent element between set1 and set2, or both sets are empty.
	 */
	public static <T extends BioPAXElement> boolean hasEquivalentIntersection(Collection<? extends T> set1,
			Collection<? extends T> set2)
	{
		if (!set1.isEmpty() && !set2.isEmpty()) {
			EquivalenceGrouper<T> grouper1 = new EquivalenceGrouper<>(set1);
			int size1 = grouper1.getBuckets().size();
			if(set1.size() > size1)
				LOG.warn("hasEquivalentIntersection: the first set already contains equivalent objects");
			EquivalenceGrouper<T> grouper2 = new EquivalenceGrouper<>(set2);
			int size2 = grouper2.getBuckets().size();
			if(set2.size() > size2)
				LOG.warn("hasEquivalentIntersection: the second set already contains equivalent objects");
			//now add both sets into one grouper -
			EquivalenceGrouper<T> grouper = new EquivalenceGrouper<>();
			grouper.addAll(set1);
			grouper.addAll(set2);
			return (grouper.getBuckets().size() < size1 + size2);
		} else if(set1.isEmpty() ^ set2.isEmpty()) {
			//only one of sets is empty
			return false;
		} else { //both empty
			return true;
		}
	}

}
