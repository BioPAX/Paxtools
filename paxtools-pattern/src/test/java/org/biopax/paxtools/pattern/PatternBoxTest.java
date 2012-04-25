package org.biopax.paxtools.pattern;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.Named;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Ozgun Babur
 */
public class PatternBoxTest
{
	Model model;

	@Before
	public void setUp() throws Exception
	{
		SimpleIOHandler h = new SimpleIOHandler();
		model = h.convertFromOWL(getClass().getResourceAsStream("AR-TP53.owl"));
	}

	@Test
	public void testInSameComplex() throws Exception
	{
		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_33442"), 
			PatternBox.inSameComplex());
		
		Assert.assertTrue(list.size() == 1);



		Map<BioPAXElement,List<Match>> map = Searcher.search(
			model, EntityReference.class, PatternBox.inSameComplex());

		for (BioPAXElement ele : map.keySet())
		{
//			printMatches(map.get(ele), 0, 4);
		}
	}

	@Test
	public void testControlsStateChange() throws Exception
	{
		Map<BioPAXElement,List<Match>> map = Searcher.search(
			model, EntityReference.class, PatternBox.controlsStateChange(false));

		Assert.assertTrue(map.size() > 0);
		
		for (BioPAXElement ele : map.keySet())
		{
//			printMatches(map.get(ele));
		}

//		System.out.println("-------");

		map = Searcher.search(model, EntityReference.class, PatternBox.controlsStateChange(true));

		Assert.assertTrue(map.size() > 0);

		for (BioPAXElement ele : map.keySet())
		{
//			printMatches(map.get(ele));
		}
	}


	protected void printMatches(Collection<Match> matches)
	{
		for (Match match : matches)
		{
			System.out.println(name(match.get(0)) + " ---> " + name(match.getLast()));
		}
	}
	
	protected void printMatches(Collection<Match> matches, int i, int j)
	{
		for (Match match : matches)
		{
			System.out.println(name(match.get(i)) + " ---> " + name(match.get(j)));
		}
	}

	protected String name(BioPAXElement ele)
	{
		if (ele instanceof Named) return ((Named) ele).getDisplayName();
		return ele.getRDFId();
	}
}
