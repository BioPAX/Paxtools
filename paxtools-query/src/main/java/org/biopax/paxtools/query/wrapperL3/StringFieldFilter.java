package org.biopax.paxtools.query.wrapperL3;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Level3Element;

import java.util.*;

/**
 * This is the base class for filters that are filtering based on values of specific fields of the
 * objects and their related objects.
 *
 * @author Ozgun Babur
 */
public abstract class StringFieldFilter extends Filter
{
	/**
	 * Accessors and their applicable classes.
	 */
	Map<PathAccessor, Class<? extends BioPAXElement>> accessors;

	/**
	 * Set of valid values (case insensitive), for filtering.
	 */
	Set<String> validValues;

	/**
	 * Option to accept objects if the field is empty or null.
	 */
	protected boolean emptyOK;

	/**
	 * Constructor.
	 * 
	 * @param emptyOK whether to always accept empty field when traversing the graph or reject
	 * @param valid filter values (strings' capitalization does not matter)
	 */
	protected StringFieldFilter(boolean emptyOK, String[] valid)
	{
		setEmptyOK(emptyOK);
		accessors = new HashMap<PathAccessor, Class<? extends BioPAXElement>>();
		
		if(valid == null || valid.length == 0) {
			validValues = Collections.EMPTY_SET;
		} else {
			validValues = new HashSet<>();
			//copy all the filter values, making them lower-case (will then match ignoring case)
			for(String val : valid)
				if(val != null && !val.isEmpty())
					validValues.add(val.toLowerCase());
		}
		
		createFieldAccessors();
	}

	/**
	 * Gets the option to accept empty fields.
	 * @return true if empty is ok
	 */
	public boolean isEmptyOK()
	{
		return emptyOK;
	}

	/**
	 * Sets the parameter to accept empty field values.
	 * @param emptyOK parameter to accept empty filed
	 */
	public void setEmptyOK(boolean emptyOK)
	{
		this.emptyOK = emptyOK;
	}

	/**
	 * The child class should populate the list of PathAccessor object using the
	 * <code>addAccessor</code> method.
	 */
	public abstract void createFieldAccessors();

	/**
	 * Adds the given <code>PathAccessor</code> to the list of accessors to use to get field values
	 * of objects and their related objects.
	 * @param acc accessor
	 * @param clazz the type of element that the accessor is applied
	 */
	protected void addAccessor(PathAccessor acc, Class<? extends BioPAXElement> clazz)
	{
		accessors.put(acc, clazz);
	}

	/**
	 * Adds the given valid value to the set of valid values.
	 * @param value a valid value
	 */
	protected void addValidValue(String value)
	{
		if(value!=null && !value.isEmpty())
			validValues.add(value.toLowerCase());
	}

	/**
	 * Checks if the related values of the object are among valid values. Returns true if none of
	 * the accessors is applicable to the given object.
	 * @param ele level 3 element to check
	 * @return true if ok to traverse
	 */
	@Override
	public boolean okToTraverse(Level3Element ele)
	{
		if(validValues.isEmpty())
			return true;
		
		boolean empty = true;
		boolean objectRelevant = false;

		for (PathAccessor acc : accessors.keySet())
		{
			Class clazz = accessors.get(acc);

			if (!clazz.isAssignableFrom(ele.getClass())) continue;

			objectRelevant = true;

			Set values = acc.getValueFromBean(ele);
			if (empty) empty = values.isEmpty();

			for (Object o : values)
				//ignoring capitalization (case)
				if (validValues.contains(o.toString().toLowerCase())) 
					return true;
		}

		return !objectRelevant || (empty && isEmptyOK());
	}
}