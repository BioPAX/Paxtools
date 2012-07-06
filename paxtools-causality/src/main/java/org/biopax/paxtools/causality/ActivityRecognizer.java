package org.biopax.paxtools.causality;

import org.biopax.paxtools.causality.wrapper.Graph;
import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.Searcher;
import org.biopax.paxtools.pattern.c.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * @author Ozgun Babur
 */
public class ActivityRecognizer
{
	private Map<ProteinReference, Set<PhysicalEntity>> effectingMap =
		new HashMap<ProteinReference, Set<PhysicalEntity>>();

	private Map<ProteinReference, Set<PhysicalEntity>> activeMap =
		new HashMap<ProteinReference, Set<PhysicalEntity>>();

	private Map<ProteinReference, Set<PhysicalEntity>> inactiveMap =
		new HashMap<ProteinReference, Set<PhysicalEntity>>();

	private Map<ProteinReference, Set<String>> activatingLocMap =
		new HashMap<ProteinReference, Set<String>>();

	private Map<ProteinReference, Set<String>> inactivatingLocMap =
		new HashMap<ProteinReference, Set<String>>();

	private Map<ProteinReference, Set<EntityReference>> activatingBindingMap =
		new HashMap<ProteinReference, Set<EntityReference>>();

	private Map<ProteinReference, Set<EntityReference>> inactivatingBindingMap =
		new HashMap<ProteinReference, Set<EntityReference>>();

	private Map<ProteinReference, Set<Conversion>> activatingConv =
		new HashMap<ProteinReference, Set<Conversion>>();

	private Map<ProteinReference, Set<Conversion>> inactivatingConv =
		new HashMap<ProteinReference, Set<Conversion>>();

	private static final Random rand = new Random();

	Model model;

	private static final PathAccessor MODIF_TERMS = new PathAccessor(
		"PhysicalEntity/feature:ModificationFeature/modificationType/term");

	private static final PathAccessor MODIF = new PathAccessor(
		"PhysicalEntity/feature:ModificationFeature");

	private static final PathAccessor LOCATION = new PathAccessor(
		"PhysicalEntity/cellularLocation/term");

	private static final PathAccessor MODIF_2_TERM = new PathAccessor(
		"ModificationFeature/modificationType/term");

	private Pattern inOutFromPRPattern = prepareInOutFromPRPattern();
	private Pattern linkedPEToComplexPattern = prepareLinkedPEToComplexPattern();
	private Pattern linkedPEToMemberPattern = prepareLinkedPEToMemberPattern();
	private Pattern linkedPEToMemberERPattern = prepareLinkedPEToMemberERPattern();
	private Pattern producingConvPattern = prepareProducingConvPattern();
	private Pattern transcriptionConvPattern = prepareTranscriptionConvPattern();
	private Pattern degradingConvPattern = prepareDegradingConvPattern();
	private Pattern associationPattern = prepareAssociationPattern();
	private Pattern dissociationPattern = prepareDissociationPattern();

	private String[] generalTerms = new String[]{
		"phospho", "ubiqutin", "acetyl", "myristoyl", "palmitoyl", "glucosyl"};

	public ActivityRecognizer(Model model)
	{
		this.model = model;
	}

	private void initMaps()
	{
		for (ProteinReference pr : model.getObjects(ProteinReference.class))
		{
			if (isHuman(pr))
			{
				effectingMap.put(pr, new HashSet<PhysicalEntity>());
				activeMap.put(pr, new HashSet<PhysicalEntity>());
				inactiveMap.put(pr, new HashSet<PhysicalEntity>());
				activatingLocMap.put(pr, new HashSet<String>());
				inactivatingLocMap.put(pr, new HashSet<String>());
				activatingBindingMap.put(pr, new HashSet<EntityReference>());
				inactivatingBindingMap.put(pr, new HashSet<EntityReference>());
				activatingConv.put(pr, new HashSet<Conversion>());
				inactivatingConv.put(pr, new HashSet<Conversion>());
			}
		}
	}

	public void run()
	{
		initMaps();
		initEffecting();
		System.out.println("inited effecting");
		initWithNCIActivityLabels();
		System.out.println("inited nci labels");
		processDegradingReactions();
		System.out.println("processed degrading");
		extractAffectingLocAndBinding();
		System.out.println("extracteed effective loc and binding");
		assesConversions();
		System.out.println("assessed conversion");
		decideToTheTypeOfTheEffecting();
		System.out.println("decided to the type of affecting");
		extractAffectingLocAndBinding();
		System.out.println("extracted effecting loc and binding");
		assesConversions();
		System.out.println("assesed conversions");
		sanityCheck();
		embedRecognitionsInModel();
	}
	
	private void initWithNCIActivityLabels()
	{
		for (ProteinReference er : effectingMap.keySet())
		{
			for (PhysicalEntity pe : Searcher.searchAndCollect(
				er.getEntityReferenceOf(), linkedPEToComplexPattern, 1, PhysicalEntity.class))
			{
				if (hasNCIActiveLabel(pe) || activeInName(pe) ||
					(hasActiveMember(pe) && !hasInactiveMember(pe)))
				{
					activeMap.get(er).add(pe);
					
					if (inactiveMap.get(er).contains(pe))
					{
						System.err.println("Inactive map already contains this pe: " + pe.getDisplayName());
					}
				}
				else if (hasNCIInactiveLabel(pe) || inactiveInName(pe) || hasInactiveMember(pe))
				{
					inactiveMap.get(er).add(pe);

					if (activeMap.get(er).contains(pe)) System.err.println("Active map already contains this pe: " + pe.getDisplayName());
				}
			}
		}
	}

	private void processDegradingReactions()
	{
		for (ProteinReference pr : effectingMap.keySet())
		{
			for (Conversion cnv : Searcher.searchAndCollect(
				pr.getEntityReferenceOf(), degradingConvPattern, 1, Conversion.class))
			{
				inactivatingConv.get(pr).add(cnv);

				PhysicalEntity pe = cnv.getLeft().iterator().next();
				if (!activeMap.get(pr).contains(pe) && pe.getControllerOf().isEmpty())
				{
					inactiveMap.get(pr).add(pe);
				}
			}
		}
	}
	
	private void initEffecting()
	{
		for (ProteinReference pr : effectingMap.keySet())
		{
			for (SimplePhysicalEntity spe : pr.getEntityReferenceOf())
			{
				for (PhysicalEntity pe : Searcher.searchAndCollect(
					spe, linkedPEToComplexPattern, 1, PhysicalEntity.class))
				{
					if (!pe.getControllerOf().isEmpty()) effectingMap.get(pr).add(pe);
				}
			}
		}
	}
	
	private void extractAffectingLocAndBinding()
	{
		for (ProteinReference pr :effectingMap.keySet())
		{
			// Locations

			Set actLoc = LOCATION.getValueFromBeans(activeMap.get(pr));
			Set inactLoc = LOCATION.getValueFromBeans(inactiveMap.get(pr));

			removeCommon(actLoc, inactLoc);

			activatingLocMap.put(pr, actLoc);
			inactivatingLocMap.put(pr, inactLoc);

			// Binding

			Set<EntityReference> actBinding = new HashSet<EntityReference>();
			Set<EntityReference> inactBinding = new HashSet<EntityReference>();

			for (EntityReference er : Searcher.searchAndCollect(
				activeMap.get(pr), linkedPEToMemberERPattern, 2, EntityReference.class))
			{
				actBinding.add(er);
			}

			for (EntityReference er : Searcher.searchAndCollect(
				inactiveMap.get(pr), linkedPEToMemberERPattern, 2, EntityReference.class))
			{
				inactBinding.add(er);
			}

			removeCommon(actBinding, inactBinding);
			activatingBindingMap.put(pr, actBinding);
			inactivatingBindingMap.put(pr, inactBinding);
		}
	}
	
	private void assesConversions()
	{
		for (ProteinReference pr : effectingMap.keySet())
		{
			// Label the conversions that are used instead of template reactions

			for (Conversion cnv : Searcher.searchAndCollect(
				pr.getEntityReferenceOf(), transcriptionConvPattern, 1, Conversion.class))
			{
				if (!activatingConv.get(pr).contains(cnv))
				{
					activatingConv.get(pr).add(cnv);
					cnv.addComment(Graph.IS_TRANSCRIPTION);
				}
			}

			// Label the conversions that produce active and inactive states

			if (hasCommon(activeMap.get(pr), inactiveMap.get(pr)))
			{
				System.err.println("Active and inactive states overlap. PR = " + pr.getDisplayName());
			}
			
			for (Conversion cnv : Searcher.searchAndCollect(
				activeMap.get(pr), producingConvPattern, 1, Conversion.class))
			{
				activatingConv.get(pr).add(cnv);
				if (inactivatingConv.get(pr).contains(cnv)) inactivatingConv.get(pr).remove(cnv);
			}

			for (Conversion cnv : Searcher.searchAndCollect(
				inactiveMap.get(pr), producingConvPattern, 1, Conversion.class))
			{
				inactivatingConv.get(pr).add(cnv);
				if (activatingConv.get(pr).contains(cnv)) activatingConv.get(pr).remove(cnv);
			}

			// Predict other conversions from features, binding, and location.

			for (Match match : Searcher.search(pr, inOutFromPRPattern))
			{
				PhysicalEntity inPE = (PhysicalEntity) match.get(1);
				PhysicalEntity outPE = (PhysicalEntity) match.get(5);
				Conversion conv = (Conversion) match.get(3);
				
				// Skip if already predicted
				
				if (activatingConv.get(pr).contains(conv) || 
					inactivatingConv.get(pr).contains(conv))
				{
					continue;
				}

				// Predict by feature change

				if (inPE != outPE)
				{
					Set<ModificationFeature> gained = getFeatures(outPE);
					Set<ModificationFeature> lost = getFeatures(inPE);
					removeCommon(gained, lost);

					int pred = predictByFeature(pr, gained, lost);
					if (pred == 1) activatingConv.get(pr).add(conv);
					else if (pred == -1) inactivatingConv.get(pr).add(conv);
					
					if (pred != 0) continue;
				}

				// Predict by binding change

				PhysicalEntity inCmp = (PhysicalEntity) match.get(2);
				PhysicalEntity outCmp = (PhysicalEntity) match.get(4);

				Set<EntityReference> gained = Searcher.searchAndCollect(outCmp, linkedPEToMemberERPattern, 2, EntityReference.class);
				Set<EntityReference> lost = Searcher.searchAndCollect(inCmp, linkedPEToMemberERPattern, 2, EntityReference.class);
				removeCommon(gained, lost);

				int pred = predictByBinding(pr, gained, lost);
				if (pred == 1) activatingConv.get(pr).add(conv);
				else if (pred == -1) inactivatingConv.get(pr).add(conv);

				if (pred != 0) continue;

				// Predict by location change

				Set gainLoc = LOCATION.getValueFromBean(outPE);
				gainLoc.addAll(LOCATION.getValueFromBean(outCmp));
				Set lostLoc = LOCATION.getValueFromBean(inPE);
				lostLoc.addAll(LOCATION.getValueFromBean(inCmp));
				
				pred = predictByLocation(pr, gainLoc, lostLoc);
				if (pred == 1) activatingConv.get(pr).add(conv);
				else if (pred == -1) inactivatingConv.get(pr).add(conv);
			}
		}
	}

	private int predictByFeature(ProteinReference pr,
		Set<ModificationFeature> gained, Set<ModificationFeature> lost)
	{
		Set activFeats = MODIF.getValueFromBeans(activeMap.get(pr));
		Set inactivFeats = MODIF.getValueFromBeans(inactiveMap.get(pr));
		removeCommon(activFeats, inactivFeats);

		boolean activated = false;
		boolean inactivated = false;
		
		for (ModificationFeature mf : gained) if (activFeats.contains(mf)) activated = true; else if (inactivFeats.contains(mf)) inactivated = true;
		for (ModificationFeature mf : lost) if (inactivFeats.contains(mf)) activated = true; else if (activFeats.contains(mf)) inactivated = true;
		
		if (activated && inactivated) return 0;
		if (activated) return 1;
		if (inactivated) return -1;

		// Nothing predicted with exact matching, so try ignoring locations of modifications

		Set<String> activTerms = extractTerms(activFeats);
		Set<String> inactivTerms = extractTerms(inactivFeats);
		removeCommon(activTerms, inactivTerms);

		Set<String> gainedTerms = extractTerms(gained);
		Set<String> lostTerms = extractTerms(lost);
		removeCommon(gainedTerms, lostTerms);

		for (String term : gainedTerms) if (activTerms.contains(term)) activated = true; else if (inactivTerms.contains(term)) inactivated = true;
		for (String term : lostTerms) if (inactivTerms.contains(term)) activated = true; else if (activTerms.contains(term)) inactivated = true;

		if (activated && inactivated) return 0;
		if (activated) return 1;
		if (inactivated) return -1;

		// Nothing predicted with ignoring locations, so just use similarity of terms

		Set<String> generalActiv = getGeneralWords(activTerms);
		Set<String> generalInactiv = getGeneralWords(inactivTerms);
		removeCommon(generalActiv, generalInactiv);

		Set<String> generalGained = getGeneralWords(gainedTerms);
		Set<String> generalLost = getGeneralWords(lostTerms);
		removeCommon(generalGained, generalLost);

		for (String term : generalGained) if (generalActiv.contains(term)) activated = true; else if (generalInactiv.contains(term)) inactivated = true;
		for (String term : generalLost) if (generalInactiv.contains(term)) activated = true; else if (generalActiv.contains(term)) inactivated = true;

		if (activated && inactivated) return 0;
		if (activated) return 1;
		if (inactivated) return -1;
		return 0;
	}

	private int predictByBinding(ProteinReference pr,
		Set<EntityReference> gained, Set<EntityReference> lost)
	{
		boolean activating = hasCommon(gained, activatingBindingMap.get(pr)) || 
			hasCommon(lost, inactivatingBindingMap.get(pr));

		boolean inactivating = hasCommon(gained, inactivatingBindingMap.get(pr)) || 
			hasCommon(lost, activatingBindingMap.get(pr));
		
		if (activating && inactivating) return 0;
		else if (activating) return 1;
		else if (inactivating) return -1;
		return 0;
	}

	private int predictByLocation(ProteinReference pr,
		Set<String> gained, Set<String> lost)
	{
		boolean activating = hasCommon(gained, activatingLocMap.get(pr)) ||
			hasCommon(lost, inactivatingLocMap.get(pr));

		boolean inactivating = hasCommon(gained, inactivatingLocMap.get(pr)) ||
			hasCommon(lost, activatingLocMap.get(pr));

		if (activating && inactivating) return 0;
		else if (activating) return 1;
		else if (inactivating) return -1;
		return 0;
	}

	private void decideToTheTypeOfTheEffecting()
	{
		for (ProteinReference pr : effectingMap.keySet())
		{
			for (PhysicalEntity pe : effectingMap.get(pr))
			{
				if (activeMap.get(pr).contains(pe) || inactiveMap.get(pr).contains(pe)) continue;
				
				boolean inactive = false;
				
				for (Conversion cnv : Searcher.searchAndCollect(
					pe, producingConvPattern, 1, Conversion.class))
				{
					if (inactivatingConv.get(pr).contains(cnv))
					{
						inactive = true;
						break;
					}
				}

				if (!inactive)
				{
					Set<EntityReference> friends = Searcher.searchAndCollect(
						pe, linkedPEToMemberERPattern, 2, EntityReference.class);
					
					if (hasCommon(friends, inactivatingBindingMap.get(pr))) inactive = true;
				}
				
				if (inactive) inactiveMap.get(pr).add(pe);
				else activeMap.get(pr).add(pe);
			}
		}
	}
	
	private void sanityCheck()
	{
		// check if actives and inactives overlap
		for (ProteinReference pr : effectingMap.keySet())
		{
			if (hasCommon(activeMap.get(pr), inactiveMap.get(pr)))
			{
				System.err.println("Active and inactive sets overlap: " + pr.getDisplayName());
			}
		}

		// check if activating and inactivating conversions overlap

		for (ProteinReference pr : effectingMap.keySet())
		{
			if (hasCommon(activatingConv.get(pr), inactivatingConv.get(pr)))
			{
				System.err.println("Activating and inactivating conversions overlap: " + pr.getDisplayName());
			}
		}
	}
	
	private void embedRecognitionsInModel()
	{
		for (ProteinReference pr : effectingMap.keySet())
		{
			for (PhysicalEntity pe : effectingMap.get(pr))
			{
				if (activeMap.get(pr).contains(pe))
				{
					pr.addComment(Graph.ACTIVE_STATE + pe.getRDFId());
					pe.addComment(Graph.ACTIVE_STATE + pr.getRDFId());
				}
				else if (inactiveMap.get(pr).contains(pe))
				{
					pr.addComment(Graph.INACTIVE_STATE + pe.getRDFId());
					pe.addComment(Graph.INACTIVE_STATE + pr.getRDFId());
				}
			}
		}

		for (ProteinReference pr : activatingConv.keySet())
		{
			for (Conversion cnv : activatingConv.get(pr))
			{
				pr.addComment(Graph.ACTIVATING_CONV + cnv.getRDFId());
				cnv.addComment(Graph.ACTIVATING_CONV + pr.getRDFId());
			}
		}
		for (ProteinReference pr : inactivatingConv.keySet())
		{
			for (Conversion cnv : inactivatingConv.get(pr))
			{
				pr.addComment(Graph.INACTIVATING_CONV + cnv.getRDFId());
				cnv.addComment(Graph.INACTIVATING_CONV + pr.getRDFId());
			}
		}

		embedInhibitoryBinding();
	}

	private void embedInhibitoryBinding()
	{
		for (ProteinReference pr : effectingMap.keySet())
		{
			for (Match match : Searcher.search(pr, associationPattern))
			{
				Conversion cnv = (Conversion) match.get(3);
				EntityReference er = (EntityReference) match.getLast();
				
				if (inactivatingConv.get(pr).contains(cnv) && 
					inactivatingBindingMap.get(pr).contains(er))
				{
					PhysicalEntity pe = (PhysicalEntity) match.get(8);

					Control ctrl = model.addNew(Control.class, generateID());
					ctrl.addController(pe);
					ctrl.addControlled(cnv);
					ctrl.setControlType(ControlType.ACTIVATION);
				}
			}

			for (Match match : Searcher.search(pr, dissociationPattern))
			{
				Conversion cnv = (Conversion) match.get(3);
				EntityReference er = (EntityReference) match.getLast();

				if (inactivatingConv.get(pr).contains(cnv) &&
					inactivatingBindingMap.get(pr).contains(er))
				{
					PhysicalEntity pe = (PhysicalEntity) match.get(10);

					Control ctrl = model.addNew(Control.class, generateID());
					ctrl.addController(pe);
					ctrl.addControlled(cnv);
					ctrl.setControlType(ControlType.INHIBITION);
				}
			}
		}
	}
	
	// Section: Unit methods

	private Set<ModificationFeature> getFeatures(PhysicalEntity pe)
	{
		return MODIF.getValueFromBean(pe);
	}

	private boolean hasNCIActiveLabel(PhysicalEntity pe)
	{
		return MODIF_TERMS.getValueFromBean(pe).contains("residue modification, active");
	}

	private boolean hasNCIInactiveLabel(PhysicalEntity pe)
	{
		return MODIF_TERMS.getValueFromBean(pe).contains("residue modification, inactive");
	}

	private boolean isHuman(ProteinReference pr)
	{
		return pr.getOrganism() != null && pr.getOrganism().getDisplayName().equals("Homo sapiens");
	}

	private boolean activeInString(String s)
	{
		return s.toLowerCase().contains("activ") && !s.toLowerCase().contains("inactiv");
	}

	private boolean inactiveInString(String s)
	{
		return s.toLowerCase().contains("inactiv");
	}
	
	private boolean activeInName(Named nd)
	{
		for (String name : nd.getName()) if (activeInString(name)) return true;
		return false;
	}
	private boolean inactiveInName(Named nd)
	{
		for (String name : nd.getName()) if (inactiveInString(name)) return true;
		return false;
	}
	
	private boolean hasInactiveMember(PhysicalEntity pe)
	{
		for (PhysicalEntity pe2 : Searcher.searchAndCollect(
			pe, linkedPEToMemberPattern, 1, PhysicalEntity.class))
		{
			if (hasNCIInactiveLabel(pe2) || inactiveInName(pe2)) return true;
		}
		return false;
	}

	private boolean hasActiveMember(PhysicalEntity pe)
	{
		for (PhysicalEntity pe2 : Searcher.searchAndCollect(
			pe, linkedPEToMemberPattern, 1, PhysicalEntity.class))
		{
			if (hasNCIActiveLabel(pe2) || activeInName(pe2)) return true;
		}
		return false;
	}

	private Set<String> extractTerms(Set<ModificationFeature> mods)
	{
		return new HashSet<String>(MODIF_2_TERM.getValueFromBeans(mods));
	}
	
	private Set<String> getGeneralWords(Set<String> terms)
	{
		Set<String> set = new HashSet<String>();

		for (String term : terms)
		{
			term = term.toLowerCase();
			for (String gen : generalTerms)
			{
				if (term.contains(gen)) set.add(gen);
			}
		}
		return set;
	}

	private void removeCommon(Set s1, Set s2)
	{
		Set common = new HashSet(s1);
		common.retainAll(s2);
		s1.removeAll(common);
		s2.removeAll(common);
	}

	private boolean hasCommon(Set s1, Set s2)
	{
		for (Object o : s1)
		{
			if (s2.contains(o)) return true;
		}
		return false;
	}
	
	private String generateID()
	{
		return "ActivityRecognizer" + System.currentTimeMillis() + rand.nextDouble();
	}
	
	// Section: Patterns
	
	private Pattern prepareInOutFromPRPattern()
	{
		Pattern p = new Pattern(7, ProteinReference.class);
		int i = 0;
		p.addConstraint(ConBox.erToPE(), i, ++i);
		p.addConstraint(new LinkedPE(LinkedPE.Type.TO_COMPLEX), i, ++i);
		p.addConstraint(new ParticipatesInConv(RelType.INPUT, true), i, ++i);
		p.addConstraint(new OtherSide(), i-1, i, ++i);
		p.addConstraint(new Equality(false), i-2, i);
		p.addConstraint(new LinkedPE(LinkedPE.Type.TO_MEMBER), i, ++i);
		p.addConstraint(ConBox.peToER(), i, ++i);
		p.addConstraint(new Equality(true), 0, i);
		return p;
	}
	
	private Pattern prepareLinkedPEToComplexPattern()
	{
		Pattern p = new Pattern(2, PhysicalEntity.class);
		p.addConstraint(new LinkedPE(LinkedPE.Type.TO_COMPLEX), 0, 1);
		return p;
	}

	private Pattern prepareLinkedPEToMemberPattern()
	{
		Pattern p = new Pattern(2, PhysicalEntity.class);
		p.addConstraint(new LinkedPE(LinkedPE.Type.TO_MEMBER), 0, 1);
		return p;
	}

	private Pattern prepareLinkedPEToMemberERPattern()
	{
		Pattern p = new Pattern(3, PhysicalEntity.class);
		p.addConstraint(new LinkedPE(LinkedPE.Type.TO_MEMBER), 0, 1);
		p.addConstraint(ConBox.peToER(), 1, 2);
		return p;
	}
	
	private Pattern prepareProducingConvPattern()
	{
		Pattern p = new Pattern(2, PhysicalEntity.class);
		p.addConstraint(new ParticipatesInConv(RelType.OUTPUT, true), 0, 1);
		return p;
	}

	private Pattern prepareTranscriptionConvPattern()
	{
		Pattern p = new Pattern(2, PhysicalEntity.class);
		p.addConstraint(new ParticipatesInConv(RelType.OUTPUT, true), 0, 1);
		p.addConstraint(new Empty(ConBox.left()), 1);
		p.addConstraint(new Size(ConBox.right(), 1, Size.Type.EQUAL), 1);
		return p;
	}

	private Pattern prepareDegradingConvPattern()
	{
		Pattern p = new Pattern(2, PhysicalEntity.class);
		p.addConstraint(new ParticipatesInConv(RelType.INPUT, true), 0, 1);
		p.addConstraint(new Empty(ConBox.right()), 1);
		p.addConstraint(new Size(ConBox.left(), 1, Size.Type.EQUAL), 1);
		return p;
	}

	private Pattern prepareAssociationPattern()
	{
		Pattern p = new Pattern(12, ProteinReference.class);
		int i = 0;
		p.addConstraint(ConBox.erToPE(), i, ++i);
		p.addConstraint(new LinkedPE(LinkedPE.Type.TO_COMPLEX), i, ++i);
		p.addConstraint(new ParticipatesInConv(RelType.INPUT, true), i, ++i);
		p.addConstraint(new OtherSide(), i-1, i, ++i);
		p.addConstraint(new LinkedPE(LinkedPE.Type.TO_MEMBER), i, ++i);
		p.addConstraint(ConBox.peToER(), i, ++i);
		p.addConstraint(new Equality(true), 0, i);
		p.addConstraint(new OtherSide(), i-2, i-3, ++i);
		p.addConstraint(new Equality(false), i-5, i);
		p.addConstraint(new LinkedPE(LinkedPE.Type.TO_MEMBER), i, ++i);
		p.addConstraint(ConBox.peToER(), i, ++i);
		p.addConstraint(new Equality(false), 0, i);
		p.addConstraint(new LinkedPE(LinkedPE.Type.TO_MEMBER), i-5, ++i);
		p.addConstraint(ConBox.peToER(), i, ++i);
		p.addConstraint(new Equality(true), i-2, i);
		return p;
	}
	
	private Pattern prepareDissociationPattern()
	{
		Pattern p = new Pattern(12, ProteinReference.class);
		int i = 0;
		p.addConstraint(ConBox.erToPE(), i, ++i);
		p.addConstraint(new LinkedPE(LinkedPE.Type.TO_COMPLEX), i, ++i);
		p.addConstraint(new ParticipatesInConv(RelType.INPUT, true), i, ++i);
		p.addConstraint(new OtherSide(), i-1, i, ++i);
		p.addConstraint(new LinkedPE(LinkedPE.Type.TO_MEMBER), i, ++i);
		p.addConstraint(ConBox.peToER(), i, ++i);
		p.addConstraint(new Equality(true), 0, i);
		p.addConstraint(new OtherSide(), i-4, i-3, ++i);
		p.addConstraint(new Equality(false), i-3, i);
		p.addConstraint(new LinkedPE(LinkedPE.Type.TO_MEMBER), i, ++i);
		p.addConstraint(ConBox.peToER(), i, ++i);
		p.addConstraint(new Equality(false), 0, i);
		p.addConstraint(new LinkedPE(LinkedPE.Type.TO_MEMBER), i-7, ++i);
		p.addConstraint(ConBox.peToER(), i, ++i);
		p.addConstraint(new Equality(true), i-2, i);
		return p;
	}
	
	public static void main(String[] args) throws FileNotFoundException
	{
		ActivityRecognizer ar = new ActivityRecognizer(null);
//		ar.degradingConvPattern.setVariableSize(3);
//		ar.degradingConvPattern.addConstraint(ConBox.convToControl(), 1, 2);
		Searcher.searchInFile(ar.transcriptionConvPattern, "/home/ozgun/Desktop/cpath2.owl",
			"/home/ozgun/Desktop/pattern-matches/transcriptionConvPattern.owl", 100, 1);
	}
}
