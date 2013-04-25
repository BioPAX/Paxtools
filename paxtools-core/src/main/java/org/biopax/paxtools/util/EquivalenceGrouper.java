package org.biopax.paxtools.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Utility class for equivalence based comparison of a set of BioPAXElements.
 * <p/>
 * BioPAXElement by default uses equals and hash code methods inherited from Object.
 * 
 * NOTE: BioPAXElement (and sub-classes) does not override these basic methods anymore 
 * (since paxtools v2). In general and in some situations, such as during models merge or in 
 * the middle of a model creation, collections of biopax objects can contain different versions 
 * of a biopax element with the same URI.
 * 
 * On the other hand for many elements it is possible to determine semantic equivalence
 * among elements. For example two entityFeatures with exactly the same type and location
 * are equivalent. This logic is implemented in isEquivalent() and equivalenceCode()
 * methods.
 * <p/>
 * For most Java collections that uses hashCode and equals there is no easy way to plug-in a
 * comparator to switch to different comparison behavior. This is a simple Set implementation that uses
 * equivalence codes when possible. It uses HashMap as the underlying implementation and will use a special bucket in
 * the case of clashes.
 * <p/>
 */
public class EquivalenceGrouper<T extends BioPAXElement>
{


	HashSet<EquivalanceBucket<T>> buckets;

	Log log = LogFactory.getLog(EquivalenceGrouper.class);

	public EquivalenceGrouper(Set<? extends T> bpes)
	{
		this();
		addAll(bpes);
	}

	public HashSet<? extends List<T>> getBuckets()
	{
		return buckets;
	}

	void addAll(Set<? extends T> bpes)
	{
		for (T bpe : bpes)
		{
			add(bpe);
		}
	}

	public EquivalanceBucket<T> access(final T element)
	{
		EquivalanceBucket<T> value =null;
		if (element != null)
		{
			value = access(element, EquivalanceBucket.EQUALITY);
			if (value==null) //now try with equivalence
			{
				value=access(element,EquivalanceBucket.EQUIVALENCE);
			}
		}
		return value;

	}

	private EquivalanceBucket<T> access(final T element, final boolean parity)
	{
		final Object[] trap = new Object[]{null};
		if (this.buckets.contains(new Object()
		{
			public int hashCode()
			{
				return parity?element.equivalenceCode():element.hashCode();
			}

			public boolean equals(Object other)
			{
				if(other != null && other.equals(element))
				{
					trap[0] = other;
					return true;
				}
				else return false;
			}
		}))
		{
			return (EquivalanceBucket<T>) trap[0];
		}
		return null;
	}


	public EquivalenceGrouper()
	{
		this.buckets = new HashSet<EquivalanceBucket<T>>();
	}

	public void add(T bpe)
	{

		// If this is the case then we will simply return false when
		// we have something that matches the evcode
		// AND if that something is a bucket contains something that is  equivalent to bpe
		// AND if that something is not a bucket and it is equivalent to bpe
		EquivalanceBucket<T> bucket = access(bpe);
		if (bucket == null)
		{
			bucket = new EquivalanceBucket(bpe);
			this.buckets.add(bucket);
		} else
		{
			for (T t : bucket)
			{
				if(t==bpe) return;
			}
			bucket.add(bpe);
		}
	}


	private class EquivalanceBucket<T extends BioPAXElement> extends LinkedList<T>
	{

		static final boolean EQUALITY = false;

		static final boolean EQUIVALENCE = true;

		private final int code;

		private boolean parity;

		private EquivalanceBucket(T first)
		{
			this.add(first);
			if (first.equivalenceCode() == 0 || first.equivalenceCode() == first.hashCode())
			{
				parity = EQUALITY;
				this.code = first.hashCode();
			} else
			{
				parity = EQUIVALENCE;
				this.code = first.equivalenceCode();
			}
		}

		@Override public int hashCode()
		{
			return code;
		}

		@Override public boolean equals(Object o)
		{
			if (o instanceof EquivalanceBucket)
			{
				return super.equals(o);
			} else
			{
				T t = this.get(0);
				if (parity) //EQUALITY
				{
					return t.isEquivalent((BioPAXElement) o);
				} else //EQUIVALENCE
				{
					return t.equals(o);
				}
			}
		}

	}
}

