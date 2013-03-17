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
	public FieldOfMultiple(MappedConst con, String accessorString, Object value)
	{
		super(accessorString, value);
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
		String accessorString2)
	{
		super(accessorString1, accessorString2, null);
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

		// If the second element is desired value, check that
		else if (value == USE_SECOND_ARG)
		{
			BioPAXElement q = match.get(ind[1]);
			return values.contains(q);
		}

		// If element group is compared to preset value, but the value is actually a collection,
		// then iterate the collection, see if any of them matches
		else if (value instanceof Collection)
		{
			for (Object o : (Collection) value)
			{
				if (values.contains(o)) return true;
			}
			return false;
		}

		// Check if the elements contain the parameter value
		else if (value != null)
		{
			return values.contains(value);
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

			others.retainAll(values);
			return !others.isEmpty();
		}

		throw new RuntimeException("Shouldn't reach here. Please debug.");
	}
}
