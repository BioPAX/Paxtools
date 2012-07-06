package org.biopax.paxtools.causality;

import org.biopax.paxtools.causality.util.HGNCUtil;
import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.*;
import org.biopax.paxtools.pattern.c.*;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * This is a temporary class for analyzing the big picture we have. I should have made this an 
 * independent project and use paxtools, but I am lazy. 
 * 
 * @author Ozgun Babur
 */
public class NetworkAnalyzer
{
	@Test
	@Ignore
	public void lookForGeneralInhibitors() throws FileNotFoundException
	{
		SimpleIOHandler h = new SimpleIOHandler();
		Model model = h.convertFromOWL(new FileInputStream("/home/ozgun/Desktop/all.owl"));

		for (ProteinReference er : model.getObjects(ProteinReference.class))
		{
			PathAccessor pa = new PathAccessor("EntityReference/entityReferenceOf/componentOf*");
			Set comps = pa.getValueFromBean(er);

			List<Match> matches = Searcher.search(er, PatternBox.inSameComplex());

			Set<EntityReference> interacting = collect(matches, 4);
			
			matches = Searcher.search(er, PatternBox.inSameComplexHavingTransActivity());

			Set<EntityReference> transAct = collect(matches, 4);
			
			matches = Searcher.search(er, PatternBox.inSameComplexEffectingConversion());

			Set<EntityReference> effConv = collect(matches, 4);

			matches = Searcher.search(er, PatternBox.hasActivity());

			Set<Control> controls = collect(matches, 2);

			System.out.println(comps.size() + "\t" + interacting.size() + "\t" + transAct.size() + "\t" + effConv.size() + "\t" + controls.size() + "\t" + er.getDisplayName());
		}
	}
	
	protected Set collect(Collection<Match> matches, int index)
	{
		Set set = new HashSet();
		for (Match match : matches)
		{
			set.add(match.get(index));
		}
		return set;
	}

	@Test
	@Ignore
	public void clip() throws FileNotFoundException
	{
		Pattern p = prepareModifierConv();

		Searcher.searchInFile(p, "/home/ozgun/Desktop/cpath2.owl", "/home/ozgun/Desktop/pattern-matches/modifier_with_generics_and_complexes.owl");

//		SimpleIOHandler h = new SimpleIOHandler();
//		Model model = h.convertFromOWL(new FileInputStream("/home/ozgun/Desktop/cpath2_prepared.owl"));
//
//		List<Match> mathes = Searcher.search(model.getByID("urn:miriam:uniprot:Q01094"), p);
//		System.out.println("mathes.size() = " + mathes.size());
	}

	@Test
	@Ignore
	public void searchSummary() throws FileNotFoundException
	{
		Pattern p = prepareTransEffectorPattern();

		SimpleIOHandler h = new SimpleIOHandler();
		Model model = h.convertFromOWL(new FileInputStream("/home/ozgun/Desktop/cpath2.owl"));

		System.out.println("PC_Pathway_ID\tPathway_Name\tPathway_Source_Name\t" +
			"Transcriptional_Gene\tDownstream_Gene\tRegulation_Type");

		Set<String> memory = new HashSet<String>();
		
		for (ProteinReference pr : model.getObjects(ProteinReference.class))
		{
			String sym = getSymbol(pr);
			
			if (sym == null) continue;

			for (Match match : Searcher.search(pr, p))
			{
				if (!(match.get(6) instanceof ProteinReference)) continue;

				ProteinReference targ = (ProteinReference) match.get(6);
				String tarSym = getSymbol(targ);
				
				if (tarSym == null) continue;

				Control ctrl = (Control) match.get(3);

				String reg = sym + "\t" + tarSym + "\t" + (ctrl.getControlType() != null && 
					ctrl.getControlType().toString().startsWith("I") ? "Inhibition" : "Activation");

				if (memory.contains(reg)) continue;
				else memory.add(reg);
				
				Set<Pathway> pathways = ctrl.getPathwayComponentOf();
				if (pathways.isEmpty())
				{
					TemplateReaction tr = (TemplateReaction) match.get(4);
					pathways = tr.getPathwayComponentOf();
				}
				
				if (pathways.isEmpty())
				{
					System.out.println("\t\t\t" + reg);
				}
				else
				{
					Pathway pat = pathways.iterator().next();
					System.out.println(pat.getRDFId() + "\t" + pat.getDisplayName() + "\t" +
						pat.getDataSource().iterator().next().toString() + "\t" + reg);
				}
			}
			
//			Set<ProteinReference> prs = Searcher.searchAndCollect(pr, p, 6, ProteinReference.class);
//
//			if (prs.size() > 0)
//			{
//				System.out.println(sym + "\t" + prs.size());
//			}
		}
	}

	private String getSymbol(ProteinReference pr)
	{
		for (Xref xref : pr.getXref())
		{
			if (xref.getDb().equals("HGNC"))
			{
				String id = xref.getId().substring(xref.getId().indexOf(":") + 1);
				return HGNCUtil.getSymbol(Integer.parseInt(id));
			}
		}
		return null;
	}

	private Pattern prepareModifierConv()
	{
		Pattern p = new Pattern(9, ProteinReference.class);
		int i = 0;
		p.addConstraint(ConBox.erToPE(), i, ++i);
		p.addConstraint(ConBox.complexes(), i, ++i);
		p.addConstraint(ConBox.genericEquiv(), i, ++i);
		p.addConstraint(new Equality(false), i-1, i);
		p.addConstraint(new ParticipatesInConv(RelType.INPUT, true), i, ++i);
		p.addConstraint(new OtherSide(), i-1, i, ++i);
		p.addConstraint(new Equality(false), i-2, i);
		p.addConstraint(ConBox.complexMembers(), i, ++i);
		p.addConstraint(ConBox.genericEquiv(), i, ++i);
		p.addConstraint(new Equality(false), i-1, i);
		p.addConstraint(ConBox.peToER(), i, ++i);
		p.addConstraint(new Equality(true), 0, i);
		return p;
	}

	private Pattern prepareLoop(int loopLength)
	{
		Pattern p = new Pattern(3 + (loopLength * 4), ProteinReference.class);
		int i = 0;
		p.addConstraint(ConBox.isHuman(), i);
		p.addConstraint(ConBox.erToPE(), i, ++i);
		p.addConstraint(new LinkedPE(LinkedPE.Type.TO_COMPLEX), i, ++i);
		p.addConstraint(new ParticipatesInConv(RelType.INPUT, true), i, ++i);
		p.addConstraint(ConBox.notControlled(), i);
		p.addConstraint(new OtherSide(), i-1, i, ++i);
		p.addConstraint(new Equality(false), i-2, i);
		p.addConstraint(new LinkedPE(LinkedPE.Type.TO_MEMBER), i, ++i);
		p.addConstraint(ConBox.peToER(), i, ++i);
		p.addConstraint(new Equality(true), 0, i);

		for (int convNo = 2; convNo <= loopLength; convNo++)
		{
			p.addConstraint(new ParticipatesInConv(RelType.INPUT, true), i-2, ++i);
			p.addConstraint(ConBox.notControlled(), i);

			for (int j = 2; j <= convNo; j++)
			{
				p.addConstraint(new Equality(false), i-(4*(j-1)), i);
			}

			p.addConstraint(new OtherSide(), i-3, i, ++i);

			for (int j = 2; j <= convNo; j++)
			{
				p.addConstraint(new Equality(false), i-(4*(j-1)), i);
			}

			p.addConstraint(new LinkedPE(LinkedPE.Type.TO_MEMBER), i, ++i);
			p.addConstraint(ConBox.peToER(), i, ++i);
			p.addConstraint(new Equality(true), 0, i);
		}

		p.addConstraint(new Equality(true), 2, i-2);
		return p;
	}

	private Pattern prepareConflictingControl()
	{
		Pattern p = new Pattern(15, EntityReference.class);
		int i = 0;
		p.addConstraint(ConBox.erToPE(), i, ++i);
		p.addConstraint(ConBox.erToPE(), i-1, ++i);
		p.addConstraint(ConBox.notUbique(ConversionLabelerTest.ubiq), i);
		p.addConstraint(new Equality(false), i-1, i);
		p.addConstraint(ConBox.withComplexes(), i-1, ++i);
		p.addConstraint(ConBox.withComplexes(), i-1, ++i);
		p.addConstraint(ConBox.peToControl(), i-1, ++i);
		p.addConstraint(ConBox.peToControl(), i-1, ++i);
		p.addConstraint(new Constraint()
		{
			@Override
			public boolean satisfies(Match match, int... ind)
			{
				Control con1 = (Control) match.get(ind[0]);
				Control con2 = (Control) match.get(ind[1]);
				boolean act1 = con1.getControlType() == null || con1.getControlType().toString().startsWith("A");
				boolean act2 = con2.getControlType() == null || con2.getControlType().toString().startsWith("A");
				return act1 != act2;
			}

			@Override
			public int getVariableSize()
			{
				return 2;
			}

			@Override
			public boolean canGenerate()
			{
				return false;
			}

			@Override
			public Collection<BioPAXElement> generate(Match match, int... ind)
			{
				return null;
			}
		}, i-1, i);
		p.addConstraint(ConBox.controlToConv(), i-1, ++i);
		p.addConstraint(ConBox.controlToConv(), i-1, ++i);
		p.addConstraint(new ParticipatingPE(RelType.OUTPUT), i-3, i-1, ++i);
		p.addConstraint(new ParticipatingPE(RelType.OUTPUT), i-3, i-1, ++i);
		p.addConstraint(ConBox.withSimpleMembers(), i-1, ++i);
		p.addConstraint(ConBox.withSimpleMembers(), i-1, ++i);
		p.addConstraint(ConBox.notUbique(ConversionLabelerTest.ubiq), i);
		p.addConstraint(ConBox.peToER(), i-1, ++i);
		p.addConstraint(ConBox.peToER(), i-1, ++i);
		p.addConstraint(new Equality(true), i-1, i);
		p.addConstraint(new Equality(false), 0, i);
		return p;
	}
	
	private Pattern prepareParentGenericsInDifferentStates()
	{
		Pattern p = new Pattern(6, PhysicalEntity.class);
		int i = 0;
		p.addConstraint(new PathConstraint("PhysicalEntity/memberPhysicalEntityOf*"), i, ++i);
		p.addConstraint(new ParticipatesInConv(RelType.INPUT, true), i, ++i);
		p.addConstraint(new OtherSide(), i-1, i, ++i);
		p.addConstraint(new Equality(false), i-2, i);
		p.addConstraint(new PathConstraint("PhysicalEntity/memberPhysicalEntity*"), i, ++i);
		p.addConstraint(new Equality(true), 0, i);
		p.addConstraint(ConBox.convToControl(), 2, ++i);
		return p;
	}

	private Pattern prepareInactivatedByBinding()
	{
		Pattern p = new Pattern(7, PhysicalEntity.class);
		int i = 0;
		p.addConstraint(ConBox.notUbique(ConversionLabelerTest.ubiq), 0);
		p.addConstraint(ConBox.peToER(), i, ++i);
		p.addConstraint(new ParticipatesInConv(RelType.INPUT, true), i-1, ++i);
		p.addConstraint(new OtherSide(), i-2, i, ++i);
		p.addConstraint(new Type(Complex.class), i);
		p.addConstraint(ConBox.simpleMembers(), i, ++i);
		p.addConstraint(new ModificationConstraint(Collections.singleton("residue modification, inactive")), i);
		p.addConstraint(ConBox.peToER(), i, ++i);
		p.addConstraint(new Equality(true), i, 1);
		p.addConstraint(ConBox.peToControl(), 0, ++i);
		return p;
	}
	
	private Pattern prepareTransEffectorPattern()
	{
		Pattern p = new Pattern(7, ProteinReference.class);
		int i = 0;
		p.addConstraint(ConBox.erToPE(), i, ++i);
		p.addConstraint(new LinkedPE(LinkedPE.Type.TO_COMPLEX), i, ++i);
		p.addConstraint(ConBox.peToControl(), i, ++i);
		p.addConstraint(ConBox.controlToTempReac(), i, ++i);
		p.addConstraint(ConBox.product(), i, ++i);
		p.addConstraint(ConBox.peToER(), i, ++i);
		p.addConstraint(new Equality(false), i, 0);
		return p;
	}
}
