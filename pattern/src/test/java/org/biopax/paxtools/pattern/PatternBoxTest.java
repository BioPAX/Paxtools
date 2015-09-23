package org.biopax.paxtools.pattern;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Named;
import org.biopax.paxtools.pattern.constraint.NonUbique;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.*;

/**
 * @author Ozgun Babur
 */
public class PatternBoxTest
{
	protected Model model_P53;
	protected Model model_urea;
	protected Model model_tca;

	@Before
	public void setUp() throws Exception
	{
		SimpleIOHandler h = new SimpleIOHandler();
		model_P53 = h.convertFromOWL(PatternBoxTest.class.getResourceAsStream("AR-TP53.owl"));
		model_urea = h.convertFromOWL(PatternBoxTest.class.getResourceAsStream("UreaCycle.owl"));
		model_tca = h.convertFromOWL(PatternBoxTest.class.getResourceAsStream("tca_cycle.owl"));
	}

	@Test
	public void testInSameComplex() throws Exception
	{
		List<Match> list = Searcher.search(model_P53.getByID("http://pid.nci.nih.gov/biopaxpid_33442"),
			PatternBox.inSameComplex());
		
		Assert.assertTrue(list.size() == 1);


		Map<BioPAXElement,List<Match>> map = Searcher.search(model_P53, PatternBox.inSameComplex());

		for (BioPAXElement ele : map.keySet())
		{
//			printMatches(map.get(ele), 0, 4);
		}
	}

	@Test
	public void testControlsStateChange() throws Exception
	{
		Map<BioPAXElement,List<Match>> map = Searcher.search(
			model_P53, PatternBox.controlsStateChange());

		Assert.assertTrue(map.size() > 0);
	}

	@Test
	public void testConsecutiveCatalysis() throws Exception
	{
		Pattern p = PatternBox.catalysisPrecedes(null);

		List<Match> list = Searcher.searchPlain(model_urea, p);

		int size = list.size();
		Assert.assertTrue(size > 0);

		printMatches(list, 0, 5, 10);

		Blacklist b = new Blacklist();
		b.addEntry("urn:miriam:chebi:29888", 0, null);

		p.insertPointConstraint(new NonUbique(b), p.indexOf("linker PE"));

		list = Searcher.searchPlain(model_urea, p);

		Assert.assertTrue(!list.isEmpty() && list.size() < size);

		b = new Blacklist(PatternBoxTest.class.getResourceAsStream("blacklist.txt"));
		p = PatternBox.catalysisPrecedes(b);
		list = Searcher.searchPlain(model_tca, p);

		Assert.assertTrue(list.size() > 15);

		System.out.println();
		printMatches(list, 0, 5, 10);

	}

	@Test
	@Ignore
	public void testConsecutiveCatalysis2() throws Exception
	{
		SimpleIOHandler h = new SimpleIOHandler();
		model_P53 = h.convertFromOWL(new FileInputStream("/home/ozgun/Desktop/NMNAT1-QPRT.owl"));

		Blacklist b = new Blacklist(PatternBoxTest.class.getResourceAsStream("blacklist.txt"));
		Pattern p = PatternBox.catalysisPrecedes(b);

		List<Match> list = Searcher.searchPlain(model_P53, p);

		printMatches(list, 0, 5, 10);

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
		return ele.getUri();
	}
}
