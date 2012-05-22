package org.biopax.paxtools.pattern;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.Named;
import org.biopax.paxtools.pattern.c.ConBox;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Ozgun Babur
 */
public class PatternBoxTest
{
	Model model;
	Model model_urea;

	@Before
	public void setUp() throws Exception
	{
		SimpleIOHandler h = new SimpleIOHandler();
		model = h.convertFromOWL(getClass().getResourceAsStream("AR-TP53.owl"));
		model_urea = h.convertFromOWL(getClass().getResourceAsStream("UreaCycle.owl"));
	}

	@Test
	public void testInSameComplex() throws Exception
	{
		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_33442"), 
			PatternBox.inSameComplex());
		
		Assert.assertTrue(list.size() == 1);


		Map<BioPAXElement,List<Match>> map = Searcher.search(model, PatternBox.inSameComplex());

		for (BioPAXElement ele : map.keySet())
		{
//			printMatches(map.get(ele), 0, 4);
		}
	}

	@Test
	public void testControlsStateChange() throws Exception
	{
		Map<BioPAXElement,List<Match>> map = Searcher.search(model,
			PatternBox.controlsStateChange(false));

		Assert.assertTrue(map.size() > 0);
		
		for (BioPAXElement ele : map.keySet())
		{
//			printMatches(map.get(ele));
		}

//		System.out.println("-------");

		map = Searcher.search(model, PatternBox.controlsStateChange(true));

		Assert.assertTrue(map.size() > 0);

		for (BioPAXElement ele : map.keySet())
		{
//			printMatches(map.get(ele));
		}
	}

	@Test
	public void testConsecutiveCatalysis() throws Exception
	{
		Pattern p = PatternBox.consecutiveCatalysis(null);
		p.insertPointConstraint(ConBox.notUbique(Collections.singleton("http://www.reactome.org/biopax/68322SmallMolecule19")), 5);

		Map<BioPAXElement,List<Match>> map = Searcher.search(model_urea, p);

		Assert.assertTrue(map.size() > 0);

		for (BioPAXElement ele : map.keySet())
		{
			printMatches(map.get(ele), 0, 5, 10);
		}
	}


	protected void printMatches(Collection<Match> matches)
	{
		if (matches.isEmpty()) return;
		printMatches(matches, 0, matches.iterator().next().varSize()-1);
	}
	
	protected void printMatches(Collection<Match> matches, int ... ind)
	{
		for (Match match : matches)
		{
			System.out.print(name(match.get(ind[0])));
			for (int i = 1; i < ind.length; i++)
			{
				System.out.print(" --- " + name(match.get(ind[i])));
			}
			System.out.println();
		}
	}

	protected String name(BioPAXElement ele)
	{
		if (ele instanceof Named) return ((Named) ele).getDisplayName();
		return ele.getRDFId();
	}
}
