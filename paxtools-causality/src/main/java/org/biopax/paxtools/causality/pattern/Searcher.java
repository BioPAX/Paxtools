package org.biopax.paxtools.causality.pattern;

import org.biopax.paxtools.model.BioPAXElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Ozgun Babur
 */
public class Searcher
{
	public List<Match> search(BioPAXElement ele, Pattern pattern)
	{
		Match m = new Match(pattern.getVariableSize());
		m.set(ele, 0);
		
		List<Match> list = new LinkedList<Match>();
		list.add(m);

		for (MappedConst mc : pattern.getConstraints())
		{
			Constraint constr = mc.getConstr();
			int[] ind = mc.getInds();
			int lastInd = ind[ind.length-1];

			for (Match match : new ArrayList<Match>(list))
			{
				if (constr.canGenerate() && match.get(lastInd) == null)
				{
					Collection<BioPAXElement> elements = constr.generate(match, ind);
					
					for (BioPAXElement el : elements)
					{
						try {
							m = (Match) match.clone();
						} catch (CloneNotSupportedException e){e.printStackTrace();}

						m.set(el, lastInd);
						list.add(m);
					}
					list.remove(match);
				}
				else
				{
					if (!constr.satisfies(match, ind))
					{
						list.remove(match);
					}
				}
			}			
		}
		return list;
	}
}
