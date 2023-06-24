package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.Searcher;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author Ozgun Babur
 */
public class ConBoxTest extends TestParent
{
	@Test
	public void erToPE() throws Exception
	{
		List<Match> list = Searcher.search(model.getByID("urn:miriam:uniprot:P04637"), //TP53
			new Pattern(EntityReference.class, ConBox.erToPE(), "ER", "PE"));

		Assertions.assertTrue(list.size() == 5);
	}

	@Test
	public void peToER() throws Exception
	{
		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_17220"), //p53
			new Pattern(PhysicalEntity.class, ConBox.peToER(), "PE", "ER"));

		Assertions.assertTrue(list.size() == 1);
	}

	@Test
	public void downControl() throws Exception
	{
		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35449"), //T-DHT/AR complex
			new Pattern(PhysicalEntity.class, ConBox.peToControl(), "PE", "Control"));

		Assertions.assertTrue(list.size() == 1);
	}

	@Test
	public void controlled() throws Exception
	{
		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35543"),
			new Pattern(Control.class, ConBox.controlled(), "Control", "Inter"));

		Assertions.assertTrue(list.size() == 1);
		Assertions.assertTrue(list.get(0).get(1) == model.getByID("http://pid.nci.nih.gov/biopaxpid_35537"));
	}

	@Test
	public void controlToConv() throws Exception
	{
		// todo Test for nested controls

		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35543"),
			new Pattern(Control.class, ConBox.controlToConv(), "Control", "Conversion"));

		Assertions.assertTrue(list.size() == 1);
		Assertions.assertTrue(list.get(0).get(1) == model.getByID("http://pid.nci.nih.gov/biopaxpid_35537"));
	}

	@Test
	public void controlsConv() throws Exception
	{
		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35449"),
			new Pattern(PhysicalEntity.class, ConBox.controlsConv(), "PE", "Conv"));

		Assertions.assertTrue(list.size() == 1);
		Assertions.assertTrue(list.get(0).get(1) == model.getByID("http://pid.nci.nih.gov/biopaxpid_35537"));
	}

	@Test
	public void genericEquiv() throws Exception
	{
		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_637"), // GNAO1
			new Pattern(PhysicalEntity.class, ConBox.genericEquiv(), "PE", "EQ"));

		Assertions.assertTrue(list.size() == 2);
		Assertions.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_623")));

		list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_623"), // Gi Family
			new Pattern(PhysicalEntity.class, ConBox.genericEquiv(), "PE", "EQ"));

		Assertions.assertTrue(list.size() == 7);
	}

	@Test
	public void complexMembers() throws Exception
	{
		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35409"), // Gi family/GNB1/GNG2/GDP
			new Pattern(Complex.class, ConBox.complexMembers(), "Complex", "member"));

		Assertions.assertTrue(list.size() == 4);
	}

	@Test
	public void simpleMembers() throws Exception
	{
		// todo Test for a nested complex

		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35409"), // Gi family/GNB1/GNG2/GDP
			new Pattern(Complex.class, ConBox.simpleMembers(), "Complex", "SPE"));

		Assertions.assertTrue(list.size() == 4);
	}

	@Test
	public void withComplexMembers() throws Exception
	{
		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35409"), // Gi family/GNB1/GNG2/GDP
			new Pattern(PhysicalEntity.class, ConBox.withComplexMembers(), "PE", "member"));

		Assertions.assertTrue(list.size() == 5);
	}

	@Test
	public void withSimpleMembers() throws Exception
	{
		// todo Test for a nested complex

		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35409"), // Gi family/GNB1/GNG2/GDP
			new Pattern(PhysicalEntity.class, ConBox.withSimpleMembers(), "PE", "SPE"));

		Assertions.assertTrue(list.size() == 5);
	}

	@Test
	public void complexes() throws Exception
	{
		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_5511"), // GNB1
			new Pattern(PhysicalEntity.class, ConBox.complexes(), "PE", "Complex"));

		Assertions.assertTrue(list.size() == 2);
	}

	@Test
	public void withComplexes() throws Exception
	{
		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_5511"), // GNB1
			new Pattern(PhysicalEntity.class, ConBox.withComplexes(), "PE", "Complex"));

		Assertions.assertTrue(list.size() == 3);
	}

	@Test
	public void left() throws Exception
	{
		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35537"),
			new Pattern(Conversion.class, ConBox.left(), "Conversion", "left"));

		Assertions.assertTrue(list.size() == 2);
		Assertions.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_685")));
		Assertions.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_35409")));
	}

	@Test
	public void right() throws Exception
	{
		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35537"),
			new Pattern(Conversion.class, ConBox.right(), "Conversion", "right"));

		Assertions.assertTrue(list.size() == 3);
		Assertions.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_678")));
		Assertions.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_21741")));
		Assertions.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_31826")));
	}

	@Test
	public void participatesInConv() throws Exception
	{
		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_31826"), // Gi family/GTP
			new Pattern(PhysicalEntity.class, ConBox.participatesInConv(), "PE", "Conversion"));

		Assertions.assertTrue(list.size() == 1);
		Assertions.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_35537")));
	}

	@Test
	public void nameEquals() throws Exception
	{
		Pattern p = new Pattern(Complex.class, "Complex");
		p.add(ConBox.complexMembers(), "Complex", "member");
		p.add(ConBox.nameEquals("GDP"), "member");

		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35409"), // Gi family/GNB1/GNG2/GDP
			p);

		Assertions.assertTrue(list.size() == 1);
		Assertions.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_678")));
	}

	@Test
	public void notUbique() throws Exception
	{
		Pattern p = new Pattern(Complex.class, "Complex");
		p.add(ConBox.complexMembers(), "Complex", "member");
		Blacklist blacklist = new Blacklist();
		blacklist.addEntry("http://pid.nci.nih.gov/biopaxpid_679", 0, null); // GDP ER ID
		p.add(new NonUbique(blacklist), "member"); // not GDP

		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35409"), // Gi family/GNB1/GNG2/GDP
			p);

		Assertions.assertTrue(list.size() == 3);
		Assertions.assertFalse(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_678"))); //GDP PE ID
	}
}
