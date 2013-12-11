package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.Searcher;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

/**
 * @author Ozgun Babur
 */
public class ConBoxTest extends TestParent
{
	@Test
	public void testErToPE() throws Exception
	{
		List<Match> list = Searcher.search(model.getByID("urn:miriam:uniprot:P04637"), //TP53
			new Pattern(EntityReference.class, ConBox.erToPE(), "ER", "PE"));

		Assert.assertTrue(list.size() == 5);
	}

	@Test
	public void testPeToER() throws Exception
	{
		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_17220"), //p53
			new Pattern(PhysicalEntity.class, ConBox.peToER(), "PE", "ER"));

		Assert.assertTrue(list.size() == 1);
	}

	@Test
	public void testDownControl() throws Exception
	{
		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35449"), //T-DHT/AR complex
			new Pattern(PhysicalEntity.class, ConBox.peToControl(), "PE", "Control"));

		Assert.assertTrue(list.size() == 1);
	}

	@Test
	public void testControlled() throws Exception
	{
		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35543"),
			new Pattern(Control.class, ConBox.controlled(), "Control", "Inter"));

		Assert.assertTrue(list.size() == 1);
		Assert.assertTrue(list.get(0).get(1) == model.getByID("http://pid.nci.nih.gov/biopaxpid_35537"));
	}

	@Test
	public void testControlToConv() throws Exception
	{
		// todo Test for nested controls

		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35543"),
			new Pattern(Control.class, ConBox.controlToConv(), "Control", "Conversion"));

		Assert.assertTrue(list.size() == 1);
		Assert.assertTrue(list.get(0).get(1) == model.getByID("http://pid.nci.nih.gov/biopaxpid_35537"));
	}

	@Test
	public void testControlsConv() throws Exception
	{
		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35449"),
			new Pattern(PhysicalEntity.class, ConBox.controlsConv(), "PE", "Conv"));

		Assert.assertTrue(list.size() == 1);
		Assert.assertTrue(list.get(0).get(1) == model.getByID("http://pid.nci.nih.gov/biopaxpid_35537"));
	}

	@Test
	public void testGenericEquiv() throws Exception
	{
		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_637"), // GNAO1
			new Pattern(PhysicalEntity.class, ConBox.genericEquiv(), "PE", "EQ"));

		Assert.assertTrue(list.size() == 2);
		Assert.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_623")));

		list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_623"), // Gi Family
			new Pattern(PhysicalEntity.class, ConBox.genericEquiv(), "PE", "EQ"));

		Assert.assertTrue(list.size() == 7);
	}

	@Test
	public void testComplexMembers() throws Exception
	{
		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35409"), // Gi family/GNB1/GNG2/GDP
			new Pattern(Complex.class, ConBox.complexMembers(), "Complex", "member"));

		Assert.assertTrue(list.size() == 4);
	}

	@Test
	public void testSimpleMembers() throws Exception
	{
		// todo Test for a nested complex

		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35409"), // Gi family/GNB1/GNG2/GDP
			new Pattern(Complex.class, ConBox.simpleMembers(), "Complex", "SPE"));

		Assert.assertTrue(list.size() == 4);
	}

	@Test
	public void testWithComplexMembers() throws Exception
	{
		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35409"), // Gi family/GNB1/GNG2/GDP
			new Pattern(PhysicalEntity.class, ConBox.withComplexMembers(), "PE", "member"));

		Assert.assertTrue(list.size() == 5);
	}

	@Test
	public void testWithSimpleMembers() throws Exception
	{
		// todo Test for a nested complex

		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35409"), // Gi family/GNB1/GNG2/GDP
			new Pattern(PhysicalEntity.class, ConBox.withSimpleMembers(), "PE", "SPE"));

		Assert.assertTrue(list.size() == 5);
	}

	@Test
	public void testComplexes() throws Exception
	{
		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_5511"), // GNB1
			new Pattern(PhysicalEntity.class, ConBox.complexes(), "PE", "Complex"));

		Assert.assertTrue(list.size() == 2);
	}

	@Test
	public void testWithComplexes() throws Exception
	{
		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_5511"), // GNB1
			new Pattern(PhysicalEntity.class, ConBox.withComplexes(), "PE", "Complex"));

		Assert.assertTrue(list.size() == 3);
	}

	@Test
	public void testLeft() throws Exception
	{
		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35537"),
			new Pattern(Conversion.class, ConBox.left(), "Conversion", "left"));

		Assert.assertTrue(list.size() == 2);
		Assert.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_685")));
		Assert.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_35409")));
	}

	@Test
	public void testRight() throws Exception
	{
		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35537"),
			new Pattern(Conversion.class, ConBox.right(), "Conversion", "right"));

		Assert.assertTrue(list.size() == 3);
		Assert.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_678")));
		Assert.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_21741")));
		Assert.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_31826")));
	}

	@Test
	public void testParticipatesInConv() throws Exception
	{
		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_31826"), // Gi family/GTP
			new Pattern(PhysicalEntity.class, ConBox.participatesInConv(), "PE", "Conversion"));

		Assert.assertTrue(list.size() == 1);
		Assert.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_35537")));
	}

	@Test
	public void testNameEquals() throws Exception
	{
		Pattern p = new Pattern(Complex.class, "Complex");
		p.add(ConBox.complexMembers(), "Complex", "member");
		p.add(ConBox.nameEquals("GDP"), "member");

		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35409"), // Gi family/GNB1/GNG2/GDP
			p);

		Assert.assertTrue(list.size() == 1);
		Assert.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_678")));
	}

	@Test
	public void testNotUbique() throws Exception
	{
		Pattern p = new Pattern(Complex.class, "Complex");
		p.add(ConBox.complexMembers(), "Complex", "member");
		Blacklist blacklist = new Blacklist();
		blacklist.addEntry("http://pid.nci.nih.gov/biopaxpid_679", 0, null); // GDP ER ID
		p.add(new NonUbique(blacklist), "member"); // not GDP

		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35409"), // Gi family/GNB1/GNG2/GDP
			p);

		Assert.assertTrue(list.size() == 3);
		Assert.assertFalse(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_678"))); //GDP PE ID
	}
}
