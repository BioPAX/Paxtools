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
	 * The kind of check.
	 */
	Operation oper;

	/**
	 * Constructor with accessor string for the field value of the element and the desired value. If
	 * the desired value is EMPTY, then emptiness is checked. If it is USE_SECOND_ARG, then the
	 * second mapped element is used as the desired value. If a filed of the second element is
	 * desired then the other constructor should be used.
	 *
	 * @param accessorString accessor string for the element
	 * @param oper type of check
	 * @param value desired value
	 */
	public Field(String accessorString, Operation oper, Object value)
	{
		super(value == USE_SECOND_ARG ? 2 : 1);
		this.value = value;
		this.pa1 = new PathAccessor(accessorString);
		this.oper = oper;

		if (value instanceof Collection && ((Collection) value).isEmpty())
			throw new IllegalArgumentException("The queried collection cannot be empty.");
	}

	/**
	 * Constructor with accessor strings for the field value of the element and the desired value
	 * that will be reached from the second element.
	 *
	 * @param accessorString1 accessor string for the first element
	 * @param accessorString2 accessor string for the second element
	 * @param oper type of check
	 */
	public Field(String accessorString1, String accessorString2, Operation oper)
	{
		super(2);
		this.pa1 = new PathAccessor(accessorString1);
		this.pa2 = new PathAccessor(accessorString2);
		this.oper = oper;
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

		// If being empty is a failure, check it
		if (oper == Operation.NOT_EMPTY_AND_NOT_INTERSECT && values.isEmpty()) return false;

		// If emptiness is desired, check that
		if (value == EMPTY) return values.isEmpty();

		// If the second element is desired value, check that
		else if (value == USE_SECOND_ARG)
		{
			BioPAXElement q = match.get(ind[1]);
			return oper == Operation.INTERSECT ? values.contains(q) : !values.contains(q);
		}

		// If one element is compared to preset value, but the value is actually a collection, then
		// iterate the collection, see if any of them matches
		else if (value instanceof Collection)
		{
			Collection query = (Collection) value;
			values.retainAll(query);

			if (oper == Operation.INTERSECT) return !values.isEmpty();
			else return values.isEmpty();
		}

		// Check if fields of second element is to be used
		else if (pa2 != null)
		{
			BioPAXElement q = match.get(ind[1]);
			Set others = pa2.getValueFromBean(q);

			switch (oper)
			{
				case INTERSECT:
					others.retainAll(values);
					return !others.isEmpty();
				case NOT_INTERSECT:
					others.retainAll(values);
					return others.isEmpty();
				case NOT_EMPTY_AND_NOT_INTERSECT:
					if (others.isEmpty()) return false;
					others.retainAll(values);
					return others.isEmpty();
				default: throw new RuntimeException("Unhandled operation: " + oper);
			}
		}

		// Check if the element field values contain the parameter value
		else if (oper == Operation.INTERSECT) return values.contains(value);
		else return !values.contains(value);
	}

	public enum Operation
	{
		INTERSECT,
		NOT_INTERSECT,
		NOT_EMPTY_AND_NOT_INTERSECT
	}
}
