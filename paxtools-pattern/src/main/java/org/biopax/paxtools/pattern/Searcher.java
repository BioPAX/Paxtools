package org.biopax.paxtools.pattern;

import org.biopax.paxtools.controller.Cloner;
import org.biopax.paxtools.controller.Completer;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.pattern.c.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;

/**
 * @author Ozgun Babur
 */
public class Searcher
{
	/**
	 * This method consumes a lot of memory, and it is not practical to use on big graphs. This is
	 * replaced with the recursive alternative. I left this method here in case someday we need it.
	 *
	 * @deprecated
	 * @see Searcher#search(BioPAXElement, Pattern)
	 */
	public static List<Match> search_old(BioPAXElement ele, Pattern pattern)
	{
		assert pattern.getStartingClass().isAssignableFrom(ele.getModelInterface());

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

	public static List<Match> search(Match m, Pattern pattern)
	{
		assert pattern.getStartingClass().isAssignableFrom(m.get(0).getModelInterface());

		try
		{
			return searchRecursive(m, pattern.getConstraints(), 0);
		}
		catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static List<Match> search(BioPAXElement ele, Pattern pattern)
	{
		assert pattern.getStartingClass().isAssignableFrom(ele.getModelInterface());

		Match m = new Match(pattern.getVariableSize());
		m.set(ele, 0);
		return search(m, pattern);
	}

	public static List<Match> searchRecursive(Match match, List<MappedConst> mc, int index) 
		throws CloneNotSupportedException
	{
		List<Match> result = new ArrayList<Match>();

		Constraint con = mc.get(index).getConstr();
		int[] ind = mc.get(index).getInds();
		int lastInd = ind[ind.length-1];

		if (con.canGenerate() && match.get(lastInd) == null)
		{
			Collection<BioPAXElement> elements = con.generate(match, ind);

			for (BioPAXElement ele : elements)
			{
				match.set(ele, lastInd);
				
				if (mc.size() == index + 1)
				{
					result.add((Match) match.clone());
				}
				else
				{
					result.addAll(searchRecursive(match, mc, index + 1));
				}
				
				match.set(null, lastInd);
			}
		}
		else
		{
			if (con.satisfies(match, ind))
			{
				if (mc.size() == index + 1)
				{
					result.add((Match) match.clone());
				}
				else
				{
					result.addAll(searchRecursive(match, mc, index + 1));
				}
			}
		}
		return result;
	}
	
	public static List<Match> searchPlain(Model model, Pattern pattern)
	{
		List<Match> list = new LinkedList<Match>();

		Map<BioPAXElement, List<Match>> map = search(model, pattern);
		for (List<Match> matches : map.values())
		{
			list.addAll(matches);
		}
		return list;
	}

	public static List<Match> searchPlain(Collection<? extends BioPAXElement> eles, Pattern pattern)
	{
		List<Match> list = new LinkedList<Match>();

		Map<BioPAXElement, List<Match>> map = search(eles, pattern);
		for (List<Match> matches : map.values())
		{
			list.addAll(matches);
		}
		return list;
	}

	public static Map<BioPAXElement, List<Match>> search(Model model, Pattern pattern)
	{
		Map<BioPAXElement, List<Match>> map = new HashMap<BioPAXElement, List<Match>>();

		int i = 1;

		for (BioPAXElement ele : model.getObjects(pattern.getStartingClass()))
		{
			System.out.print(".");
			if (i%200==0) System.out.println();

//			System.out.println(((Named) ele).getDisplayName() + "\t" + ele.getRDFId());
			List<Match> matches = search(ele, pattern);
			
			if (!matches.isEmpty())
			{
				map.put(ele, matches);
			}
			i++;
		}
		System.out.println();
		return map;
	}
	
	public static Map<BioPAXElement, List<Match>> search(Collection<? extends BioPAXElement> eles,
		Pattern pattern)
	{
		Map<BioPAXElement, List<Match>> map = new HashMap<BioPAXElement, List<Match>>();

		for (BioPAXElement ele : eles)
		{
			if (!pattern.getStartingClass().isAssignableFrom(ele.getModelInterface())) continue;
			
			List<Match> matches = search(ele, pattern);
			
			if (!matches.isEmpty())
			{
				map.put(ele, matches);
			}
		}
		return map;
	}

	public static <T extends BioPAXElement> Set<T> searchAndCollect(
		Collection<? extends BioPAXElement> eles, Pattern pattern, int index, Class<T> c)
	{
		Set<T> set = new HashSet<T>();

		for (Match match : searchPlain(eles, pattern))
		{
			set.add((T) match.get(index));
		}
		return set;
	}

	public static <T extends BioPAXElement> Set<T> searchAndCollect(
		BioPAXElement ele, Pattern pattern, int index, Class<T> c)
	{
		Set<T> set = new HashSet<T>();

		for (Match match : search(ele, pattern))
		{
			set.add((T) match.get(index));
		}
		return set;
	}

	
	public boolean hasSolution(Pattern p, BioPAXElement ... ele)
	{
		Match m = new Match(p.getVariableSize());
		for (int i = 0; i < ele.length; i++)
		{
			m.set(ele[i], i);
		}

		return !search(m, p).isEmpty();
	}
	
	public static void searchInFile(Pattern p, String inFile, String outFile) throws FileNotFoundException
	{
		searchInFile(p, inFile, outFile, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	public static void searchInFile(Pattern p, String inFile, String outFile, int seedLimit,
		int graphPerSeed) throws FileNotFoundException
	{
		SimpleIOHandler h = new SimpleIOHandler();
		Model model = h.convertFromOWL(new FileInputStream(inFile));

		Map<BioPAXElement,List<Match>> matchMap = Searcher.search(model, p);

		System.out.println("matching groups size = " + matchMap.size());

		List<Set<Interaction>> inters = new LinkedList<Set<Interaction>>();
		Set<Integer> encountered = new HashSet<Integer>();

		Set<BioPAXElement> toExise = new HashSet<BioPAXElement>();

		int seedCounter = 0;
		for (BioPAXElement ele : matchMap.keySet())
		{
			seedCounter++;
			
			if (seedCounter > seedLimit) break;

			int matchCounter = 0;
			
			for (Match match : matchMap.get(ele))
			{
				matchCounter++;
				
				if (matchCounter > graphPerSeed) break;

				Set<Interaction> ints = getInter(match);

				toExise.addAll(Arrays.asList(match.getVariables()));
				toExise.addAll(ints);

				Integer hash = hashSum(ints);
				if (!encountered.contains(hash))
				{
					encountered.add(hash);
					inters.add(ints);
				}
			}
		}

		System.out.println("created pathways = " + inters.size());

		Model clonedModel = excise(model, toExise);

		int i = 0;
		for (Set<Interaction> ints : inters)
		{
			Pathway pathway = clonedModel.addNew(Pathway.class,
				System.currentTimeMillis() + "PaxtoolsPatternGeneratedMatch" + (++i));

			pathway.setDisplayName("Match " + getLeadingZeros(i, inters.size()) + i);

			for (Interaction anInt : ints)
			{
				pathway.addPathwayComponent((Process) clonedModel.getByID(anInt.getRDFId()));
			}
		}

		handler.convertToOWL(clonedModel, new FileOutputStream(outFile));
	}

	public static String getLeadingZeros(int i, int size)
	{
		assert i <= size;
		int w1 = (int) Math.floor(Math.log10(size));
		int w2 = (int) Math.floor(Math.log10(i));
		
		String s = "";

		for (int j = w2; j < w1; j++)
		{
			s += "0";
		}
		return s;
	}
	
	static BioPAXIOHandler handler =  new SimpleIOHandler();
	static final SimpleEditorMap EM = SimpleEditorMap.L3;
	public static Model excise(Model model, Set<BioPAXElement> result)
	{
		Completer c = new Completer(EM);

		result = c.complete(result, model);

		Cloner cln = new Cloner(EM, BioPAXLevel.L3.getDefaultFactory());

		return cln.clone(model, result);
	}

	private static Set<Interaction> getInter(Match match)
	{
		Set<Interaction> set = new HashSet<Interaction>();
		for (BioPAXElement ele : match.getVariables())
		{
			if (ele instanceof Interaction) 
			{
				set.add((Interaction) ele);
				addControlsRecursive((Interaction) ele, set);
			}
		}
		return set;
	}

	private static void addControlsRecursive(Interaction inter, Set<Interaction> set)
	{
		for (Control ctrl : inter.getControlledOf())
		{
			set.add(ctrl);
			addControlsRecursive(ctrl, set);
		}
	}
	
	private static Integer hashSum(Set<Interaction> set)
	{
		int x = 0;
		for (Interaction inter : set)
		{
			x += inter.hashCode();
		}
		return x;
	}
}
