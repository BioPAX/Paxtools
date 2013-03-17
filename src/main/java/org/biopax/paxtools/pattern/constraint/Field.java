package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.pattern.Match;

import java.util.Collection;
import java.util.Set;

/**
 * Checks if an element has or has not a specific value for a field, or the field value of another
 * element.
 *
 * @author Ozgun Babur
 */
public class Field extends ConstraintAdapter
{
	/**
	 * Possible parameter value indicating user desire the field to be empty.
	 */
	public static final Object EMPTY = new Object();

	/**
	 * Possible parameter value indicating the desired value will be given as the second element to
	 * the constraint.
	 */
	public static final Object USE_SECOND_ARG = new Object();

	/**
	 * Desired value.
	 */
	Object value;

	/**
	 * Accessor to the field values for first element.
	 */
	PathAccessor pa1;

	/**
	 * Accessor to the field values for second element.
	 */
	PathAccessor pa2;

	/**
	 * Constructor with accessor string for the field value of the element and the desired value. If
	 * the desired value is EMPTY, then emptiness is checked. If it is USE_SECOND_ARG, then the
	 * second mapped element is used as the desired value. If a filed of the second element is
	 * desired then the other constructor should be used.
	 *
	 * @param accessorString accessor string for the element
	 * @param value desired value
	 */
	public Field(String accessorString, Object value)
	{
		this.value = value;
		this.pa1 = new PathAccessor(accessorString);
	}

	/**
	 * Constructor with accessor strings for the field value of the element and the desired value
	 * that will be reached from the second element.
	 *
	 * @param accessorString1 accessor string for the first element
	 * @param accessorString2 accessor string for the second element
	 * @param randomObj this is a random object to avoid the confusion between two constructors. It
	 * is ignored, just pass null.
	 */
	public Field(String accessorString1, String accessorString2, Object randomObj)
	{
		this.pa1 = new PathAccessor(accessorString1);
		this.pa2 = new PathAccessor(accessorString2);
	}

	/**
	 * Size of this constraint is 1 if a value is given to check. It is 2 if the value is
	 * USE_SECOND_ARG or two accessor
	 * @return 1 or 2
	 */
	@Override
	public int getVariableSize()
	{
		return value == USE_SECOND_ARG || pa2 != null ? 2 : 1;
	}

	/**
	 * Checks if the element in the first index has the desired value.
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return true if the filed values contain the desired value, or empty as desired.
	 */
	@Override
	public boolean satisfies(Match match, int... ind)
	{
		assertIndLength(ind);

		BioPAXElement ele = match.get(ind[0]);

		Set values = pa1.getValueFromBean(ele);

		// If emptiness is desired, check that
		if (value == EMPTY) return values.isEmpty();

		// If the second element is desired value, check that
		else if (value == USE_SECOND_ARG)
		{
			BioPAXElement q = match.get(ind[1]);
			return values.contains(q);
		}

		// If two elements should share a field value, check that
		else if (pa2 != null)
		{
			BioPAXElement q = match.get(ind[1]);
			Set others = pa2.getValueFromBean(q);
			others.retainAll(values);
			return !others.isEmpty();
		}

		// If one element is compared to preset value, but the value is actually a collection, then
		// iterate the collection, see if any of them matches
		else if (value instanceof Collection)
		{
			for (Object o : (Collection) value)
			{
				if (values.contains(o)) return true;
			}
			return false;
		}

		// Check if the element field values contain the parameter value
		else return values.contains(value);
	}
}
