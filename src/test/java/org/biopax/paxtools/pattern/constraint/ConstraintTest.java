package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.model.level3.SmallMolecule;
import org.biopax.paxtools.pattern.*;
import org.biopax.paxtools.pattern.util.RelType;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author Ozgun Babur
 */
public class ConstraintTest extends TestParent
{
	@Test
	public void testAND() throws Exception
	{
		Pattern p = new Pattern(PhysicalEntity.class, "PE");
		p.add(new AND(
			new MappedConst(ConBox.withComplexMembers(), 0, 1),
			new MappedConst(new PathConstraint("PhysicalEntity/participantOf/right/entityReference/entityReferenceOf"), 0, 1)),
			"PE", "second");

		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35409"), p); // Gi family/GNB1/GNG2/GDP

		Assert.assertTrue(list.size() == 1);
		Assert.assertTrue(list.get(0).get(1) == model.getByID("http://pid.nci.nih.gov/biopaxpid_678"));
	}

	@Test
	public void testType() throws Exception
	{
		Pattern p = new Pattern(Complex.class, "Complex");
		p.add(ConBox.complexMembers(), "Complex", "member");
		p.add(new Type(SmallMolecule.class), "member");

		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35409"), p); // Gi family/GNB1/GNG2/GDP

		Assert.assertTrue(list.size() == 1);
		Assert.assertTrue(list.get(0).get(1) == model.getByID("http://pid.nci.nih.gov/biopaxpid_678"));

		p = new Pattern(Complex.class, "Complex");
		p.add(ConBox.complexMembers(), "Complex", "member");
		p.add(new Type(SimplePhysicalEntity.class), "member");

		list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35409"), p); // Gi family/GNB1/GNG2/GDP

		Assert.assertTrue(list.size() == 4);
		Assert.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_678")));
	}

	@Test
	public void testEquality() throws Exception
	{
		Pattern p = new Pattern(PhysicalEntity.class, "PE");
		p.add(ConBox.genericEquiv(), "PE", "eq1");
		p.add(ConBox.genericEquiv(), "PE", "eq2");
		p.add(new Equality(false), "eq1", "eq2");
		p.add(ConBox.peToER(), "eq1", "ER1");
		p.add(ConBox.peToER(), "eq2", "ER2");
		p.add(new Equality(true), "ER1", "ER2");

		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_623"), p); // Gi family

		Assert.assertTrue(list.size() == 2);
		Assert.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_637")));
		Assert.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_638")));
	}

	@Test
	public void testNOT() throws Exception
	{
		Pattern p = new Pattern(Complex.class, "Comp");
		p.add(ConBox.complexMembers(), "Comp", "member");
		p.add(new NOT(new Type(SmallMolecule.class)), "member");

		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35409"), p); // Gi family/GNB1/GNG2/GDP

		Assert.assertTrue(list.size() == 3);
		Assert.assertFalse(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_678")));
	}

	@Test
	public void testMultiPath() throws Exception
	{
		Pattern p = new Pattern(PhysicalEntity.class, "PE");
		p.add(new MultiPathConstraint("PhysicalEntity/controllerOf/controlled",
			"PhysicalEntity/participantOf:Conversion"), "PE", "process");

		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_21741"), p); // GNB1/GNG2

		Assert.assertTrue(list.size() == 2);
		Assert.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_50156")));
		Assert.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_35537")));
	}

	@Test
	public void testConversionSide() throws Exception
	{
		Pattern p = new Pattern(PhysicalEntity.class, "PE");
		p.add(ConBox.participatesInConv(), "PE", "Conv");
		p.add(new ConversionSide(ConversionSide.Type.OTHER_SIDE), "PE", "Conv", "PE2");

		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_21151"), p); // p38alpha-beta

		Assert.assertTrue(list.size() == 1);
		Assert.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_50156")));
		Assert.assertTrue(collect(list, 2).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_17089")));
	}

	@Test
	public void testParticipatesInConv() throws Exception
	{
		Pattern p = new Pattern(PhysicalEntity.class, "PE");
		p.add(new ParticipatesInConv(RelType.INPUT), "PE", "Conv");

		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_21151"), p); // p38alpha-beta

		Assert.assertTrue(list.size() == 1);
		Assert.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_50156")));

		p = new Pattern(PhysicalEntity.class, "PE");
		p.add(new ParticipatesInConv(RelType.OUTPUT), "PE", "Conv");

		list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_21151"), p); // p38alpha-beta

		Assert.assertTrue(list.isEmpty());
	}

	@Test
	public void testSelfOrThis() throws Exception
	{
		Pattern p = new Pattern(PhysicalEntity.class, "PE");
		p.add(new SelfOrThis(new ParticipatesInConv(RelType.INPUT)), "PE", "Conv");

		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_21151"), p); // p38alpha-beta

		Assert.assertTrue(list.size() == 2);
		Assert.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_50156")));
		Assert.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_21151")));
	}

	@Test
	public void testExceptions() throws Exception
	{
		try
		{
			Pattern p = new Pattern(PhysicalEntity.class, "PE");
			p.add(new ConversionSide(ConversionSide.Type.OTHER_SIDE), "PE", "Conv", "PE2");

			Assert.assertFalse("Should have reached here", false);
		}
		catch (Exception e)
		{
			Assert.assertTrue(true);
		}
	}
}
