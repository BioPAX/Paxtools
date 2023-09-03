package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Adapter class for all property accessors.
 */
public abstract class PropertyAccessorAdapter<D extends BioPAXElement, R> implements PropertyAccessor<D,R>
{

	/**
	 * This is the Class representing the domain of the property.
	 */
	protected Class<D> domain;

	/**
	 * This is the Class representing the range of the property. It is by default an object.
	 */
	protected Class<R> range;

	/**
	 * This is false if there is a cardinality restriction of one on the property.
	 */
	protected boolean multipleCardinality;


	protected PropertyAccessorAdapter(Class<D> domain, Class<R> range, boolean multipleCardinality)
	{
		this.domain = domain;
		this.range = range;
		this.multipleCardinality = multipleCardinality;
	}

	/**
	 * Returns the domain of the property.
	 * @return the domain of the editor
	 */
	public Class<D> getDomain()
	{
		return domain;
	}

	/**
	 * Returns the range of the editor.
	 * @return a class
	 */
	public Class<R> getRange()
	{
		return range;
	}

	/**
	 * Checks if the property to which editor is assigned has multiple cardinality.
	 * @return true if editor belongs to a multiple cardinality property.
	 */
	public boolean isMultipleCardinality()
	{
		return multipleCardinality;
	}

	@Override public Set<? extends R> getValueFromBeans(Collection<? extends D> beans) throws
	                                                                               IllegalBioPAXArgumentException
	{
	    Set<R> aggregate = new HashSet<>();
		for (D bean : beans)
		{
			aggregate.addAll(this.getValueFromBean(bean));
		}
		return aggregate;
	}

}
