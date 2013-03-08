package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.pattern.Match;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class TestParent
{
	Model model;

	@Before
	public void setUp() throws Exception
	{
		SimpleIOHandler h = new SimpleIOHandler();
		model = h.convertFromOWL(getClass().getResourceAsStream("../AR-TP53.owl"));
	}

	@Test
	public void dummyMethod()
	{
		// I needed to add this dummy method because maven was complaining that this class has no
		// test methods.
	}


	protected Set<BioPAXElement> collect(List<Match> list, int index)
	{
		Set<BioPAXElement> set = new HashSet<BioPAXElement>();

		for (Match match : list)
		{
			set.add(match.get(index));
		}
		return set;
	}

}
