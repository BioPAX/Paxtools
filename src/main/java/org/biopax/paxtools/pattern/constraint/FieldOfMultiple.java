package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.pattern.MappedConst;
import org.biopax.paxtools.pattern.Match;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Checks if generated elements has or has not a specific value for a field, or the field value of
 * another set of generated elements.
 *
 * @author Ozgun Babur
 */
public class FieldOfMultiple extends Field
{
	/**
	 * Generative constraint for first group of generated elements, to use on first mapped element.
	 */
	protected MappedConst con1;

	/**
	 * Generative constraint for second group of generated elements, to use on second mapped
	 * element.
	 */
	protected MappedConst con2;

	/**
	 * Constructor with accessor string for the field value of the element and the desired value. If
	 * the desired value is EMPTY, then emptiness is checked. If it is USE_SECOND_ARG, then the
	 * second mapped element is used as the desired value. If a filed of the second element is
	 * desired then the other constructor should be used.
	 *
	 * @param con the generative constraint of size 2 whose generated values will be checked
	 * @param accessorString accessor string for the element
	 * @param value desired value
	 */
	public FieldOfMultiple(MappedConst con, String accessorString, Operation oper, Object value)
	{
		super(accessorString, oper, value);
		con1 = con;
	}

	/**
	 * Constructor with accessor strings for the field value of the element and the desired value
	 * that will be reached from the second element.
	 *
	 * @param con1 the generative constraint of size 2 for the first group of elements
	 * @param accessorString1 accessor string for the first element
	 * @param con2 the generative constraint of size 2 for the second group of elements
	 * @param accessorString2 accessor string for the second element
	 */
	public FieldOfMultiple(MappedConst con1, String accessorString1, MappedConst con2,
		String accessorString2, Operation oper)
	{
		super(accessorString1, accessorString2, oper);
		this.con1 = con1;
		this.con2 = con2;
	}

	/**
	 * Size of this constraint is one less than con1 if con2 is null, otherwise it is two less than
	 * the total of size of con1 and con2.
	 * @return the size based on con1 and con2
	 */
	@Override
	public int getVariableSize()
	{
		return con1.getVariableSize() + (con2 != null ? con2.getVariableSize() - 2 : -1);
	}

	/**
	 * Checks if the generated elements from the first mapped element has either the desired value,
	 * or has some value in common with the elements generated from second mapped element.
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return true if a value match is found
	 */
	@Override
	public boolean satisfies(Match match, int... ind)
	{
		assertIndLength(ind);

		// Collect values of the element group
		Set values = new HashSet();
		for (BioPAXElement gen : con1.generate(match, ind))
		{
			values.addAll(pa1.getValueFromBean(gen));
		}

		// If emptiness is desired, check that
		if (value == EMPTY) return values.isEmpty();

		// If cannot be empty, check it
		if (oper == Operation.NOT_EMPTY_AND_NOT_INTERSECT && values.isEmpty()) return false;

		// If the second element is desired value, check that
		else if (value == USE_SECOND_ARG)
		{
			BioPAXElement q = match.get(ind[1]);
			return oper == Operation.INTERSECT ? values.contains(q) : !values.contains(q);
		}

		// If element group is compared to preset value, but the value is actually a collection,
		// then iterate the collection, see if any of them matches
		else if (value instanceof Collection)
		{
			Collection query = (Collection) value;
			values.retainAll(query);

			if (oper == Operation.INTERSECT) return !values.isEmpty();
			else return values.isEmpty();
		}

		// If two set of elements should share a field value, check that
		else if (pa2 != null)
		{
			// Collect values of the second group
			Set others = new HashSet();
			for (BioPAXElement gen : con2.generate(match, ind))
			{
				others.addAll(pa2.getValueFromBean(gen));
			}

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
}
