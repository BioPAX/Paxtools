package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.SequenceEntityReference;
import org.biopax.paxtools.pattern.Match;

import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class Field extends ConstraintAdapter
{
	public static final Object EMPTY = new Object();
	
	Object value;
	PathAccessor pa;

	public Field(PathAccessor pa, Object value)
	{
		this.value = value;
		this.pa = pa;
	}

	@Override
	public int getVariableSize()
	{
		return 1;
	}

	@Override
	public boolean satisfies(Match match, int... ind)
	{
		assertIndLength(ind);

		BioPAXElement ele = match.get(ind[0]);

		Set values = pa.getValueFromBean(ele);
		
		if (value == EMPTY) return values.isEmpty();
		else return values.contains(value);
	}
}
