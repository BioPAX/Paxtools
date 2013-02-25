package org.biopax.paxtools.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;

import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Utility class for switching to the equivalence based comparison of  BioPAXElement.
 * <p/>
 * BioPAXElement by default implements equals and hash code based on its RDF-ID.
 * On the other hand for many elements it is possible to determine semantic equivalence
 * among elements. For example two entityFeatures with exactly the same type and location
 * are equivalent. This logic is implemented in isEquivalent() and equivalenceCode()
 * methods.
 * <p/>
 * For most Java collections that uses hashCode and equals there is no easy way to plug-in a
 * comparator to switch to different comparison behaviour. This is a simple Set implementation that uses
 * equivalance codes when possible.
 * <p/>
 */
public class EquivalenceSet extends AbstractSet<BioPAXElement>
{

	private HashMap<Integer, BioPAXElement> elementMap;

	private boolean mergeEquivalents = false;

	Log log = LogFactory.getLog(EquivalenceSet.class);

	public EquivalenceSet(Set<? extends BioPAXElement> bpes)
	{
		this();
		this.addAll(bpes);
	}

	public EquivalenceSet()
	{
		this.elementMap= new HashMap<Integer, BioPAXElement>();
	}


	@Override public Iterator<BioPAXElement> iterator()
	{
		return this.elementMap.values().iterator();
	}

	@Override public int size()
	{
		return this.elementMap.size();
	}

	@Override public boolean contains(Object o)
	{
		if (o instanceof BioPAXElement) return this.getEquivalent((BioPAXElement) o) != null;
		else return false;
	}

	@Override public boolean add(BioPAXElement bpe)
	{
		int evcode = bpe.equivalenceCode();
		BioPAXElement equalO;
		if (mergeEquivalents)
		{
			equalO = this.elementMap.get(evcode);
			if (equalO == null)
			{
				this.elementMap.put(bpe.equivalenceCode(), bpe);
				return true;
			} else return false;

		} else
		{
			equalO = this.elementMap.get(bpe.hashCode());
			if (equalO != null)
			{
				if (equalO.equals(bpe)) return false;
				else
				{
					log.error("Source: "+ bpe.getModelInterface()+" "+bpe.getRDFId()+ " " +bpe.hashCode());
					log.error("Target: "+ equalO.getModelInterface()+" "+equalO.getRDFId()+ " " +equalO.hashCode());
					throw new RuntimeException("Unexpected hash code");
				}
			}
			else
			{
				if (evcode != bpe.hashCode()) equalO = this.elementMap.get(evcode);
				if (equalO == null)
				{
					this.elementMap.put(bpe.equivalenceCode(), bpe);
					return true;
				} else
				{

					//Unequal elements returning same equivalence code, probably because of unknown values
					//In this context then we need to trace them by equality not equivalence
					this.elementMap.remove(evcode);
					this.elementMap.put(equalO.hashCode(), equalO);
					this.elementMap.put(bpe.hashCode(), bpe);
					return true;
				}
			}
		}
	}

	public boolean isMergeEquivalents()
	{
		return mergeEquivalents;
	}

	public void setMergeEquivalents(boolean mergeEquivalents)
	{
		this.mergeEquivalents = mergeEquivalents;
	}

	@Override public void clear()
	{
		this.elementMap.clear();
	}

	@Override public Object[] toArray()
	{
		return this.elementMap.values().toArray();
	}

	@Override public boolean isEmpty()
	{
		return this.elementMap.isEmpty();
	}

	@Override public <T> T[] toArray(T[] a)
	{
		return elementMap.values().toArray(a);
	}

	@Override public boolean remove(Object o)
	{
		if (o instanceof BioPAXElement)
		{
			BioPAXElement bpe = (BioPAXElement) o;
			BioPAXElement equalO = elementMap.get(bpe.hashCode());
			if (equalO != null)
			{
				if (o.equals(equalO))
				{
					elementMap.remove(bpe.hashCode());
					return true;
				} else
				{
					throw new IllegalBioPAXArgumentException("");
				}
			} else
			{
				equalO = elementMap.get(bpe.hashCode());
				if (bpe.isEquivalent(equalO))
				{
					elementMap.remove(bpe.hashCode());
					return true;
				} else
				{
					throw new IllegalBioPAXArgumentException("");
				}
			}
		}
		return false;
	}


	public BioPAXElement getEquivalent(BioPAXElement bpe)
	{
		BioPAXElement equalO = this.elementMap.get(bpe.hashCode());
		if (equalO != null && equalO.equals(bpe))
		{
			return equalO;
		}
		int eql = bpe.equivalenceCode();
		if (eql != bpe.hashCode()) equalO = elementMap.get(bpe.equivalenceCode());
		if (equalO != null && equalO.isEquivalent(bpe))
		{
			return equalO;
		} else return null;
	}
}
