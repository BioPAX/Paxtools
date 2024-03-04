package org.biopax.paxtools.pattern;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Named;
import org.biopax.paxtools.pattern.constraint.NonUbique;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

/**
 * @author Ozgun Babur
 */
public class PatternBoxTest
{
	protected final static Model model_P53;
	protected final static Model model_urea;

	protected static SimpleIOHandler h_ = new SimpleIOHandler();

	static {
		model_P53 = h_.convertFromOWL(PatternBoxTest.class.getResourceAsStream("AR-TP53.owl"));
		model_urea = h_.convertFromOWL(PatternBoxTest.class.getResourceAsStream("UreaCycle.owl"));
	}

	@Test
	public void inSameComplex() {
		List<Match> list = Searcher.search(model_P53.getByID("http://pid.nci.nih.gov/biopaxpid_33442"),
			PatternBox.inSameComplex());
		Assertions.assertTrue(list.size() == 1);
	}

	@Test
	public void controlsStateChange()
	{
		Map<BioPAXElement,List<Match>> map = Searcher.search(
			model_P53, PatternBox.controlsStateChange());
		Assertions.assertTrue(map.size() > 0);
	}

	@Test
	public void consecutiveCatalysis()
	{
		Pattern p = PatternBox.catalysisPrecedes(null);

		List<Match> list = Searcher.searchPlain(model_urea, p);

		int size = list.size();
		Assertions.assertTrue(size > 0);

		Blacklist b = new Blacklist();
		b.addEntry("urn:miriam:chebi:29888", 0, null);

		p.insertPointConstraint(new NonUbique(b), p.indexOf("linker PE"));

		list = Searcher.searchPlain(model_urea, p);

		Assertions.assertTrue(!list.isEmpty() && list.size() < size);

		b = new Blacklist(PatternBoxTest.class.getResourceAsStream("blacklist.txt"));
		p = PatternBox.catalysisPrecedes(b);

		Model model_tca = h_.convertFromOWL(PatternBoxTest.class.getResourceAsStream("tca_cycle.owl"));
		list = Searcher.searchPlain(model_tca, p);

		Assertions.assertTrue(list.size() > 15);
	}

	@Test
	@Disabled
	public void consecutiveCatalysis2() throws Exception
	{
		SimpleIOHandler h = new SimpleIOHandler();
		Model model = h.convertFromOWL(new FileInputStream("/home/ozgun/Desktop/NMNAT1-QPRT.owl"));
		Blacklist b = new Blacklist(PatternBoxTest.class.getResourceAsStream("blacklist.txt"));
		Pattern p = PatternBox.catalysisPrecedes(b);
		List<Match> list = Searcher.searchPlain(model, p);
		Assertions.assertFalse(list.isEmpty());
	}

	protected String name(BioPAXElement ele)
	{
		if (ele instanceof Named) return ((Named) ele).getDisplayName();
		return ele.getUri();
	}
}
