package org.biopax.paxtools.util;

import org.biopax.paxtools.model.BioPAXElement;

import java.util.HashSet;
import java.util.Set;

/**
 * User: demir Date: Mar 26, 2007 Time: 10:35:26 PM
 */
public class SetEquivalanceChecker
{
	public static <T extends BioPAXElement> boolean isEquivalent(Set<? extends T> set1,
	                                                             Set<? extends T> set2)
	{
		boolean equals = false;
		if (set1 == null)
		{
			equals = set2 == null;
		}
		else if (set1.size() == set2.size())
		{
			equals = true;
			for (BioPAXElement element1 : set1)
			{
				if (!containsEquivalent(set2, element1))
				{
					equals = false;
					break;
				}
			}
		}
		return equals;
	}

	public static boolean containsEquivalent(Set<? extends BioPAXElement> set,
	                                         BioPAXElement element)
	{
		for (BioPAXElement element2 : set)
		{
			if (element.isEquivalent(element2))
			{
				return true;
			}
		}
		return false;
	}

	public static <T extends BioPAXElement> Set<T> findEquivalentIntersection(
			Set<? extends T> set1, Set<? extends T> set2)
	{
		HashSet<T> intersection = new HashSet<T>();
		for (T element : set1)
		{
			if (containsEquivalent(set2, element))
			{
				intersection.add(element);
			}
		}
		return intersection;
	}


	public static <T extends BioPAXElement> boolean isEquivalentIntersection(Set<? extends T> set1,
	                                                                         Set<? extends T> set2)
	{
		boolean value = false;
		if (set1 == null)
		{
			value = set2 == null;
		}
		else if (set2 != null)
		{
			value = (set1.isEmpty() && set2.isEmpty())
			        || !findEquivalentIntersection(set1, set2).isEmpty();
		}
		return value;

	}

}
