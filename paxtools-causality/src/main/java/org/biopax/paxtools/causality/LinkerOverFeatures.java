package org.biopax.paxtools.causality;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.conversion.HGNC;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.RelatedPEHandler;
import org.biopax.paxtools.pattern.Searcher;
import org.biopax.paxtools.pattern.c.*;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class LinkerOverFeatures
{
	public Model link(Model model) throws FileNotFoundException
	{
		Pattern pattern = prepareLinkablePattern();
//		Pattern pattern = prepareTempPattern();

		Set<BioPAXElement> toExcise = new HashSet<BioPAXElement>();
		Map<String, Set<Interaction>> groups = new HashMap<String, Set<Interaction>>();

		Set<String> links = new HashSet<String>();
		
		int i = 0;
		for (ProteinReference pr : model.getObjects(ProteinReference.class))
		{
			String name = getName(pr);
			System.out.print("\rProtein:" + name + " no:" + (++i) +
				" --- groups size = " + groups.size());

			Set<Interaction> toLink = new HashSet<Interaction>();

			for (Match match : Searcher.search(pr, pattern))
			{
				RelatedPEHandler b1 = new RelatedPEHandler(
					(PhysicalEntity) match.get(1), (PhysicalEntity) match.get(2));
				RelatedPEHandler b2 = new RelatedPEHandler(
					(PhysicalEntity) match.get(5), (PhysicalEntity) match.get(4));

				RelatedPEHandler a1 = new RelatedPEHandler(
					(PhysicalEntity) match.get(7), (PhysicalEntity) match.get(8));
				RelatedPEHandler a2 = new RelatedPEHandler(
					(PhysicalEntity) match.get(11), (PhysicalEntity) match.get(10));

				ChangeComparator com = new ChangeComparator(a1, a2, b1, b2);
				
				assert com.getEdgeSign() != 0;
				addInteractionWithControls((Interaction) match.get(3), toLink);
				toLink.add((Interaction) match.get(6));
				addInteractionWithControls((Interaction) match.get(9), toLink);
				
				links.add(match.get(3).getRDFId() + match.get(9).getRDFId());
			}

			if (!toLink.isEmpty())
			{
				toExcise.addAll(toLink);
				groups.put(name, toLink);
			}
		}

		System.out.println("\nlinks = " + links.size());

		System.out.println("\ngroups.size() = " + groups.size());
		Model clonedModel = Searcher.excise(model, toExcise);

		i = 0;
		for (String name : groups.keySet())
		{
			Set<Interaction> ints = groups.get(name);
			Pathway pathway = clonedModel.addNew(Pathway.class,
				System.currentTimeMillis() + "PaxtoolsPatternGeneratedMatch" + (++i));

			pathway.setDisplayName(name);

			for (Interaction anInt : ints)
			{
				pathway.addPathwayComponent((Process) clonedModel.getByID(anInt.getRDFId()));
			}
		}
		return clonedModel;
	}

	private void addInteractionWithControls(Interaction cnv, Set<Interaction> set)
	{
		set.add(cnv);
		for (Control ctrl : cnv.getControlledOf()) set.add(ctrl);
	}

	private String getName(ProteinReference pr)
	{
		for (Xref xref : pr.getXref())
		{
			if (xref.getDb().equals("HGNC"))
			{
				String id = xref.getId().substring(5);
				return HGNC.getSymbol(id);
			}
		}
		return pr.getDisplayName() != null ? pr.getDisplayName() : "noname";
	}

	
	private Pattern prepareTempPattern()
	{
		Pattern p = new Pattern(7, ProteinReference.class);
		int i = 0;

		p.addConstraint(ConBox.isHuman(), i);
		p.addConstraint(ConBox.erToPE(), i, ++i);
		p.addConstraint(new Field(new PathAccessor("PhysicalEntity/dataSource/displayName"), "Reactome"), i);
//		p.addConstraint(new Field(new PathAccessor("PhysicalEntity/displayName"), "p53"), i);
		p.addConstraint(new LinkedPE(LinkedPE.Type.TO_COMPLEX), i, ++i);
		p.addConstraint(new ParticipatesInConv(RelType.INPUT, true), i, ++i);
		p.addConstraint(new OtherSide(), i-1, i, ++i);
		p.addConstraint(new Equality(false), i, i-2);
		p.addConstraint(new LinkedPE(LinkedPE.Type.TO_MEMBER), i, ++i);
		p.addConstraint(new Equality(false), i, i-4);
		p.addConstraint(ConBox.peToER(), i, ++i);
		p.addConstraint(new Equality(true), i, 0);
		return p;
	}
	private Pattern prepareLinkablePattern()
	{
		Pattern p = new Pattern(13, ProteinReference.class);
		int i = 0;

		// Changed and has activity

		p.addConstraint(ConBox.isHuman(), i);
		p.addConstraint(ConBox.erToPE(), i, ++i);
		p.addConstraint(new LinkedPE(LinkedPE.Type.TO_COMPLEX), i, ++i);
		p.addConstraint(new ParticipatesInConv(RelType.INPUT, true), i, ++i);
		p.addConstraint(new OtherSide(), i-1, i, ++i);
		p.addConstraint(new Equality(false), i, i-2);
		p.addConstraint(new LinkedPE(LinkedPE.Type.TO_MEMBER), i, ++i);
		p.addConstraint(new Equality(false), i, i-4);
		p.addConstraint(ConBox.peToER(), i, 0);
		p.addConstraint(ConBox.peToControl(), i-1, ++i);
		p.addConstraint(ConBox.notControlsThis(), i, i-3);
		p.addConstraint(ConBox.notLabeledInactive(), i-2);
		p.addConstraint(new ControlNotParticipant(), i);

		// Another state changed and is controlled

		p.addConstraint(ConBox.erToPE(), 0, ++i);
		p.addConstraint(new Equality(false), i, i-2);
		p.addConstraint(new Equality(false), i, i-6);
		p.addConstraint(new LinkedPE(LinkedPE.Type.TO_COMPLEX), i, ++i);
		p.addConstraint(new Equality(false), i, i-4);
		p.addConstraint(new Equality(false), i, i-6);
		p.addConstraint(new ParticipatesInConv(RelType.INPUT, true), i, ++i);
		p.addConstraint(new Equality(false), i, i-6);
		p.addConstraint(new OtherSide(), i-1, i, ++i);
		p.addConstraint(new Equality(false), i, i-2);
		p.addConstraint(new Equality(false), i, i-6);
		p.addConstraint(new Equality(false), i, i-8);
		p.addConstraint(new LinkedPE(LinkedPE.Type.TO_MEMBER), i, ++i);
		p.addConstraint(new Equality(false), i, i-4);
		p.addConstraint(new Equality(false), i, i-6);
		p.addConstraint(new Equality(false), i, i-10);
		p.addConstraint(ConBox.peToER(), i, 0);
		p.addConstraint(ConBox.convToControl(), i-2, ++i);

		// Conversions should be linkable
		p.addConstraint(new ChangeLinkableConstraint(), i-5, i-4, i-1, i-2, i-11, i-10, i-7, i-8);

		return p;
	}

	class ChangeLinkableConstraint extends ConstraintAdapter
	{
		@Override
		public boolean satisfies(Match match, int... ind)
		{
			RelatedPEHandler a1 = new RelatedPEHandler(
				(PhysicalEntity) match.get(ind[0]), (PhysicalEntity) match.get(ind[1]));
			RelatedPEHandler a2 = new RelatedPEHandler(
				(PhysicalEntity) match.get(ind[2]), (PhysicalEntity) match.get(ind[3]));
			RelatedPEHandler b1 = new RelatedPEHandler(
				(PhysicalEntity) match.get(ind[4]), (PhysicalEntity) match.get(ind[5]));
			RelatedPEHandler b2 = new RelatedPEHandler(
				(PhysicalEntity) match.get(ind[6]), (PhysicalEntity) match.get(ind[7]));
			
			ChangeComparator com = new ChangeComparator(a1, a2, b1, b2);
			return com.getEdgeSign() != 0;
		}

		@Override
		public int getVariableSize()
		{
			return 8;
		}
	}
}
