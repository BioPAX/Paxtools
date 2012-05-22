package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.model.level3.SmallMolecule;
import org.biopax.paxtools.pattern.MappedConst;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.Searcher;
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
		Pattern p = new Pattern(2, PhysicalEntity.class);
		p.addConstraint(new AND(
				new MappedConst(ConBox.withComplexMembers(), 0, 1),
				new MappedConst(new PathConstraint("PhysicalEntity/participantOf/right/entityReference/entityReferenceOf"), 0, 1)),
			0, 1);

		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35409"), p); // Gi family/GNB1/GNG2/GDP

		Assert.assertTrue(list.size() == 1);
		Assert.assertTrue(list.get(0).get(1) == model.getByID("http://pid.nci.nih.gov/biopaxpid_678"));
	}

	@Test
	public void testType() throws Exception
	{
		Pattern p = new Pattern(2, Complex.class);
		p.addConstraint(ConBox.complexMembers(), 0 ,1);
		p.addConstraint(new Type(SmallMolecule.class), 1);

		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35409"), p); // Gi family/GNB1/GNG2/GDP

		Assert.assertTrue(list.size() == 1);
		Assert.assertTrue(list.get(0).get(1) == model.getByID("http://pid.nci.nih.gov/biopaxpid_678"));

		p = new Pattern(2, Complex.class);
		p.addConstraint(ConBox.complexMembers(), 0 ,1);
		p.addConstraint(new Type(SimplePhysicalEntity.class), 1);

		list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35409"), p); // Gi family/GNB1/GNG2/GDP

		Assert.assertTrue(list.size() == 4);
		Assert.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_678")));
	}

	@Test
	public void testEquality() throws Exception
	{
		Pattern p = new Pattern(5, PhysicalEntity.class);
		p.addConstraint(ConBox.genericEquiv(), 0 ,1);
		p.addConstraint(ConBox.genericEquiv(), 0 ,2);
		p.addConstraint(new Equality(false), 1, 2);
		p.addConstraint(ConBox.peToER(), 1,3);
		p.addConstraint(ConBox.peToER(), 2,4);
		p.addConstraint(new Equality(true), 3, 4);

		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_623"), p); // Gi family

		Assert.assertTrue(list.size() == 2);
		Assert.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_637")));
		Assert.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_638")));
	}

	@Test
	public void testNOT() throws Exception
	{
		Pattern p = new Pattern(2, Complex.class);
		p.addConstraint(ConBox.complexMembers(), 0 ,1);
		p.addConstraint(new NOT(new Type(SmallMolecule.class)), 1);

		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_35409"), p); // Gi family/GNB1/GNG2/GDP

		Assert.assertTrue(list.size() == 3);
		Assert.assertFalse(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_678")));
	}

	@Test
	public void testMultiPath() throws Exception
	{
		Pattern p = new Pattern(2, PhysicalEntity.class);
		p.addConstraint(new MultiPathConstraint("PhysicalEntity/controllerOf/controlled", "PhysicalEntity/participantOf:Conversion"), 0 ,1);

		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_21741"), p); // GNB1/GNG2

		Assert.assertTrue(list.size() == 2);
		Assert.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_50156")));
		Assert.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_35537")));
	}

	@Test
	public void testOtherSide() throws Exception
	{
		Pattern p = new Pattern(3, PhysicalEntity.class);
		p.addConstraint(ConBox.participatesInConv(), 0, 1);
		p.addConstraint(new OtherSide(), 0, 1 ,2);

		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_21151"), p); // p38alpha-beta

		Assert.assertTrue(list.size() == 1);
		Assert.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_50156")));
		Assert.assertTrue(collect(list, 2).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_17089")));
	}

	@Test
	public void testParticipatesInConv() throws Exception
	{
		Pattern p = new Pattern(2, PhysicalEntity.class);
		p.addConstraint(new ParticipatesInConv(RelType.INPUT, false), 0, 1);

		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_21151"), p); // p38alpha-beta

		Assert.assertTrue(list.size() == 1);
		Assert.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_50156")));

		p = new Pattern(2, PhysicalEntity.class);
		p.addConstraint(new ParticipatesInConv(RelType.OUTPUT, false), 0, 1);

		list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_21151"), p); // p38alpha-beta

		Assert.assertTrue(list.isEmpty());
	}

	@Test
	public void testSelfOrThis() throws Exception
	{
		Pattern p = new Pattern(2, PhysicalEntity.class);
		p.addConstraint(new SelfOrThis(new ParticipatesInConv(RelType.INPUT, false)), 0, 1);

		List<Match> list = Searcher.search(model.getByID("http://pid.nci.nih.gov/biopaxpid_21151"), p); // p38alpha-beta

		Assert.assertTrue(list.size() == 2);
		Assert.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_50156")));
		Assert.assertTrue(collect(list, 1).contains(model.getByID("http://pid.nci.nih.gov/biopaxpid_21151")));
	}
}
