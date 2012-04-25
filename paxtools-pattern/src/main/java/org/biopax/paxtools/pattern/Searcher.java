package org.biopax.paxtools.pattern;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;

import java.util.*;

/**
 * @author Ozgun Babur
 */
public class Searcher
{
	public static List<Match> search(BioPAXElement ele, Pattern pattern)
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
	
	public static Map<BioPAXElement, List<Match>> search(Model model, 
		Class<? extends BioPAXElement> clazz, Pattern pattern)
	{
		Map<BioPAXElement, List<Match>> map = new HashMap<BioPAXElement, List<Match>>();

		for (BioPAXElement ele : model.getObjects(clazz))
		{
			List<Match> matches = search(ele, pattern);
			
			if (!matches.isEmpty())
			{
				map.put(ele, matches);
			}
		}
		return map;
	}
}
