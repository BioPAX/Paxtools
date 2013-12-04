package org.biopax.paxtools.pattern;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.constraint.*;

import java.util.Map;
import java.util.Set;

import static org.biopax.paxtools.pattern.constraint.ConBox.*;

/**
 * This class contains several pattern samples.
 *
 * @author Ozgun Babur
 */
public class PatternBox
{
	/**
	 * Pattern for two different EntityReference have member PhysicalEntity in the same Complex.
	 * Complex membership can be through multiple nesting and/or through homology relations.
	 * @return the pattern
	 */
	public static Pattern inSameComplex()
	{
		Pattern p = new Pattern(EntityReference.class, "first ER");
		p.add(erToPE(), "first ER", "first simple PE");
		p.add(linkToComplex(), "first simple PE", "Complex");
		p.add(new Type(Complex.class), "Complex");
		p.add(linkToSimple(), "Complex", "second simple PE");
		p.add(new PEChainsIntersect(false, true), "first simple PE", "Complex", "second simple PE", "Complex");
		p.add(peToER(), "second simple PE", "second ER");
		p.add(equal(false), "first ER", "second ER");
		return p;
	}

	/**
	 * Pattern for two different EntityReference have member PhysicalEntity in the same Complex, and
	 * the Complex has an activity. Complex membership can be through multiple nesting and/or
	 * through homology relations.
	 * @return the pattern
	 */
	public static Pattern inSameActiveComplex()
	{
		Pattern p = inSameComplex();
		p.add(new ActivityConstraint(true), "Complex");
		return p;
	}

	/**
	 * Pattern for two different EntityReference have member PhysicalEntity in the same Complex, and
	 * the Complex has transcriptional activity. Complex membership can be through multiple nesting
	 * and/or through homology relations.
	 * @return the pattern
	 */
	public static Pattern inSameComplexHavingTransActivity()
	{
		Pattern p = inSameComplex();

		p.add(peToControl(), "Complex", "Control");
		p.add(controlToTempReac(), "Control", "TR");
		p.add(new NOT(participantER()), "TR", "first ER");
		p.add(new NOT(participantER()), "TR", "second ER");
		return p;
	}

	/**
	 * Pattern for two different EntityReference have member PhysicalEntity in the same Complex, and
	 * the Complex is controlling a Conversion. Complex membership can be through multiple nesting
	 * and/or through homology relations.
	 * @return the pattern
	 */
	public static Pattern inSameComplexEffectingConversion()
	{
		Pattern p = inSameComplex();

		p.add(peToControl(), "Complex", "Control");
		p.add(controlToConv(), "Control", "Conversion");
		p.add(new NOT(participantER()), "Control", "first ER");
		p.add(new NOT(participantER()), "Control", "second ER");
		return p;
	}

	/**
	 * Pattern for a EntityReference has a member PhysicalEntity that is controlling a state change
	 * reaction of another EntityReference.
	 * @return the pattern
	 */
	public static Pattern controlsStateChange()
	{

		Pattern p = new Pattern(ProteinReference.class, "controller PR");
		p.add(erToPE(), "controller PR", "controller simple PE");
		p.add(linkToComplex(), "controller simple PE", "controller PE");
		p.add(peToControl(), "controller PE", "Control");
		p.add(controlToConv(), "Control", "Conversion");
		p.add(new NOT(participantER()), "Conversion", "controller PR");
		p.add(new InputOrOutput(RelType.INPUT, true), "Conversion", "input PE");
		p.add(linkToSimple(), "input PE", "input simple PE");
		p.add(new Type(Protein.class), "input simple PE");
		p.add(peToER(), "input simple PE", "changed PR");
		p.add(new OtherSide(), "input PE", "Conversion", "output PE");
		p.add(equal(false), "input PE", "output PE");
		p.add(linkToSimple(), "output PE", "output simple PE");
		p.add(peToER(), "output simple PE", "changed PR");

		return p;
	}

	/**
	 * Pattern for a ProteinReference has a member PhysicalEntity that is controlling a
	 * transportation of another ProteinReference.
	 * @return the pattern
	 */
	public static Pattern controlsTransport()
	{
		Pattern p = controlsStateChange();
		p.add(new OR(
			new MappedConst(hasDifferentCompartments(), 0, 1),
			new MappedConst(hasDifferentCompartments(), 2, 3)),
			"input simple PE", "output simple PE", "input PE", "output PE");

		return p;
	}

	/**
	 * Pattern for a ProteinReference has a member PhysicalEntity that is controlling a reaction
	 * that changes cellular location of a small molecule.
	 * @return the pattern
	 */
	public static Pattern transportsChemical(Set<String> ubiqueIDs)
	{

		Pattern p = new Pattern(ProteinReference.class, "controller PR");
		p.add(erToPE(), "controller PR", "controller simple PE");
		p.add(linkToComplex(), "controller simple PE", "controller PE");
		p.add(peToControl(), "controller PE", "Control");
		p.add(controlToConv(), "Control", "Conversion");
		p.add(new InputOrOutput(RelType.INPUT, true), "Conversion", "input PE");
		p.add(linkToSimple(), "input PE", "input simple PE");
		p.add(new Type(SmallMolecule.class), "input simple PE");
		if (ubiqueIDs != null) p.add(notUbique(ubiqueIDs), "input simple PE");
		p.add(notGeneric(), "input simple PE");
		p.add(peToER(), "input simple PE", "changed SMR");
		p.add(new OtherSide(), "input PE", "Conversion", "output PE");
		p.add(equal(false), "input PE", "output PE");
		p.add(linkToSimple(), "output PE", "output simple PE");
		p.add(notGeneric(), "output simple PE");
		p.add(peToER(), "output simple PE", "changed SMR");
		if (ubiqueIDs != null) p.add(notUbique(ubiqueIDs), "changed SMR");
		p.add(new OR(
			new MappedConst(hasDifferentCompartments(), 0, 1),
			new MappedConst(hasDifferentCompartments(), 2, 3)),
			"input simple PE", "output simple PE", "input PE", "output PE");

		return p;
	}

	/**
	 * Pattern for a EntityReference has a member PhysicalEntity that is controlling a state change
	 * reaction of another EntityReference. In this case the controller is also an input to the
	 * reaction. The affected protein is the one that is represented with different non-generic
	 * physical entities at left and right of the reaction.
	 * @return the pattern
	 */
	public static Pattern controlsStateChangeBothControlAndPart()
	{
		Pattern p = new Pattern(ProteinReference.class, "controller PR");
		p.add(erToPE(), "controller PR", "controller simple PE");
		p.add(notGeneric(), "controller simple PE");
		p.add(linkToComplex(), "controller simple PE", "controller PE");
		p.add(peToControl(), "controller PE", "Control");
		p.add(controlToConv(), "Control", "Conversion");

		// the controller PE is also an input
		p.add(new ParticipatesInConv(RelType.INPUT, true), "controller PE", "Conversion");

		// same controller simple PE is also an output
		p.add(linkToComplex(), "controller simple PE", "special output PE");
		p.add(equal(false), "special output PE", "controller PE");
		p.add(new ParticipatesInConv(RelType.OUTPUT, true), "special output PE", "Conversion");

		Pattern p2 = stateChange(true);
		p.add(p2);

		// non-generic input and outputs are only associated with one side
		p.add(equal(false), "input simple PE", "output simple PE");
		p.add(new NOT(simplePEToConv(RelType.OUTPUT)), "input simple PE", "Conversion");
		p.add(new NOT(simplePEToConv(RelType.INPUT)), "output simple PE", "Conversion");

		p.add(equal(false), "controller PR", "changed ER");
		p.add(type(ProteinReference.class), "changed ER");

		return p;
	}

	/**
	 * Pattern for a EntityReference has a member PhysicalEntity that is controlling a state change
	 * reaction of another EntityReference. This pattern is different from the original
	 * controls-state-change. The controller in this case is not modeled as a controller, but as a
	 * participant of the conversion, and it is at both sides.
	 * @param considerGenerics option to handle generic memberships in the pattern
	 * @return the pattern
	 */
	public static Pattern controlsStateChangeButIsParticipant(boolean considerGenerics)
	{
		Pattern p = new Pattern(EntityReference.class, "controller ER");
		p.add(erToPE(), "controller ER", "controller simple PE");
		p.add(notGeneric(), "controller simple PE");
		Constraint c = considerGenerics ? linkToComplex() : withComplexes();
		p.add(c, "controller simple PE", "controller PE");
		p.add(participatesInConv(), "controller PE", "Conversion");
		p.add(left(), "Conversion", "controller PE");
		p.add(right(), "Conversion", "controller PE");
		// The controller ER is not associated with the Conversion in another way.
		p.add(new NOT(new InterToPartER(1)), "Conversion", "controller PE", "controller ER");

		Pattern p2 = stateChange(considerGenerics);
		p.add(p2);

		p.add(equal(false), "controller ER", "changed ER");
		p.add(equal(false), "controller PE", "input PE");
		p.add(equal(false), "controller PE", "output PE");

		return p;
	}

	/**
	 * Pattern for a Conversion has an input PhysicalEntity and another output PhysicalEntity that
	 * belongs to the same EntityReference.
	 * @param considerGenerics option to handle generic memberships in the pattern
	 * @return the pattern
	 */
	public static Pattern stateChange(boolean considerGenerics)
	{
		Pattern p = new Pattern(Conversion.class, "Conversion");
		p.add(new InputOrOutput(RelType.INPUT, true), "Conversion", "input PE");
		Constraint c = considerGenerics ? linkToSimple() : withSimpleMembers();
		p.add(c, "input PE", "input simple PE");
		p.add(notGeneric(), "input simple PE");
		p.add(peToER(), "input simple PE", "changed ER");
		p.add(new OtherSide(), "input PE", "Conversion", "output PE");
		p.add(equal(false), "input PE", "output PE");
		p.add(c, "output PE", "output simple PE");
		p.add(notGeneric(), "output simple PE");
		p.add(peToER(), "output simple PE", "changed ER");
		return p;
	}

	/**
	 * Pattern for an entity is producing a small molecule, and hte small molecule controls state
	 * change of another molecule.
	 * @return the pattern
	 */
	public static Pattern controlsStateChangeThroughControllerSmallMolecule(Set<String> ubiqueIDs)
	{
		Pattern p = new Pattern(ProteinReference.class, "upper controller PR");
		p.add(erToPE(), "upper controller PR", "upper controller simple PE");
		p.add(notGeneric(), "upper controller simple PE");
		p.add(linkToComplex(), "upper controller simple PE", "upper controller PE");
		p.add(peToControl(), "upper controller PE", "upper Control");
		p.add(controlToConv(), "upper Control", "upper Conversion");
		p.add(new NOT(participantER()), "upper Conversion", "upper controller PR");
		p.add(new InputOrOutput(RelType.OUTPUT, true), "upper Conversion", "controller PE");
		p.add(type(SmallMolecule.class), "controller PE");
		if (ubiqueIDs != null) p.add(notUbique(ubiqueIDs), "controller PE");
		p.add(peToControl(), "controller PE", "Control");
		p.add(controlToConv(), "Control", "Conversion");
		p.add(equal(false), "upper Conversion", "Conversion");

		p.add(new OR(new MappedConst(inSamePathway(), 0, 1),
			new MappedConst(moreControllerThanParticipant(), 2)),
			"upper Conversion", "Conversion", "controller PE");

		Pattern p2 = stateChange(true);
		p.add(p2);

		p.add(type(ProteinReference.class), "changed ER");
		p.add(equal(false), "upper controller PR", "changed ER");
		p.add(new NOT(participantER()), "Conversion", "upper controller PR");

		return p;
	}

	/**
	 * Pattern for an entity is producing a small molecule, and hte small molecule controls state
	 * change of another molecule.
	 * @return the pattern
	 */
	public static Pattern controlsStateChangeThroughBindingSmallMolecule(Set<String> ubiqueIDs)
	{
		Pattern p = new Pattern(ProteinReference.class, "upper controller PR");
		p.add(erToPE(), "upper controller PR", "upper controller simple PE");
		p.add(notGeneric(), "upper controller simple PE");
		p.add(linkToComplex(), "upper controller simple PE", "upper controller PE");
		p.add(peToControl(), "upper controller PE", "upper Control");
		p.add(controlToConv(), "upper Control", "upper Conversion");
		p.add(new NOT(participantER()), "upper Conversion", "upper controller PR");
		p.add(new InputOrOutput(RelType.OUTPUT, true), "upper Conversion", "SM");
		p.add(type(SmallMolecule.class), "SM");
		if (ubiqueIDs != null) p.add(notUbique(ubiqueIDs), "SM");
		p.add(new ParticipatesInConv(RelType.INPUT, true), "SM", "Conversion");
		p.add(peToER(), "SM", "SM ER");
		p.add(equal(false), "upper Conversion", "Conversion");
		p.add(inSamePathway(), "upper Conversion", "Conversion");

		Pattern p2 = stateChange(true);
		p.add(p2);

		p.add(type(ProteinReference.class), "changed ER");
		p.add(equal(false), "upper controller PR", "changed ER");
		p.add(new NOT(participantER()), "Conversion", "upper controller PR");
		p.add(compToER(), "output PE", "SM ER");

		return p;
	}

	/**
	 * Pattern for a Protein controlling a reaction whose participant is a small molecule.
	 * @return the pattern
	 */
	public static Pattern controlsMetabolicCatalysis(Set<String> ubiqueIDs)
	{
		Pattern p = new Pattern(ProteinReference.class, "controller PR");
		p.add(erToPE(), "controller PR", "controller simple PE");
		p.add(notGeneric(), "controller simple PE");
		p.add(linkToComplex(), "controller simple PE", "controller PE");
		p.add(peToControl(), "controller PE", "Control");
		p.add(controlToConv(), "Control", "Conversion");
		p.add(new NOT(participantER()), "Conversion", "controller PR");
		p.add(participant(), "Conversion", "part PE");
		p.add(linkToSimple(), "part PE", "part SM");
		if (ubiqueIDs != null) p.add(notUbique(ubiqueIDs), "part SM");
		p.add(notGeneric(), "part SM");
		p.add(type(SmallMolecule.class), "part SM");
		p.add(peToER(), "part SM", "part SMR");
		if (ubiqueIDs != null) p.add(notUbique(ubiqueIDs), "part SMR");

		// The small molecule is associated only with left or right, but not both.
		p.add(new XOR(
			new MappedConst(new InterToPartER(InterToPartER.Direction.LEFT), 0, 1),
			new MappedConst(new InterToPartER(InterToPartER.Direction.RIGHT), 0, 1)),
			"Conversion", "part SMR");

		return p;
	}

	/**
	 * Pattern for a Protein controlling a reaction whose participant is a small molecule.
	 * @return the pattern
	 */
	public static Pattern meabolicCatalysisSubclass(Set<String> ubiqueIDs, boolean comsumption)
	{
		Pattern p = new Pattern(ProteinReference.class, "controller PR");
		p.add(erToPE(), "controller PR", "controller simple PE");
		p.add(notGeneric(), "controller simple PE");
		p.add(linkToComplex(), "controller simple PE", "controller PE");
		p.add(peToControl(), "controller PE", "Control");
		p.add(controlToConv(), "Control", "Conversion");
		p.add(new NOT(participantER()), "Conversion", "controller PR");

		p.add(new ParticipatingPE(comsumption ? RelType.INPUT : RelType.OUTPUT, false),
			"Control", "Conversion", "part PE");

		p.add(linkToSimple(), "part PE", "part SM");
		if (ubiqueIDs != null) p.add(notUbique(ubiqueIDs), "part SM");
		p.add(notGeneric(), "part SM");
		p.add(type(SmallMolecule.class), "part SM");
		p.add(peToER(), "part SM", "part SMR");
		if (ubiqueIDs != null) p.add(notUbique(ubiqueIDs), "part SMR");

		// The small molecule is associated only with left or right, but not both.
		p.add(new XOR(
			new MappedConst(new InterToPartER(InterToPartER.Direction.LEFT), 0, 1),
			new MappedConst(new InterToPartER(InterToPartER.Direction.RIGHT), 0, 1)),
			"Conversion", "part SMR");

		return p;
	}


	/**
	 * Pattern for detecting two EntityReferences are controlling consecutive reactions, where
	 * output of one reaction is input to the other.
	 * @param ubiqueIDs IDs of ubiquitous molecules, ignored if null
	 * @return the pattern
	 */
	public static Pattern catalysisPrecedes(Set<String> ubiqueIDs)
	{
		Pattern p = new Pattern(ProteinReference.class, "first PR");
		p.add(erToPE(), "first PR", "first simple controller PE");
		p.add(notGeneric(), "first simple controller PE");
		p.add(linkToComplex(), "first simple controller PE", "first controller PE");
		p.add(peToControl(), "first controller PE", "first Control");
		p.add(controlToConv(), "first Control", "first Conversion");
		p.add(new ParticipatingPE(RelType.OUTPUT, false), "first Control", "first Conversion", "linker PE");
		if (ubiqueIDs != null) p.add(notUbique(ubiqueIDs), "linker PE");
		p.add(new ParticipatesInConv(RelType.INPUT, false), "linker PE", "second Conversion");
		p.add(equal(false), "first Conversion", "second Conversion");
		p.add(new RelatedControl(RelType.INPUT), "linker PE", "second Conversion", "second Control");
		p.add(controllerPE(), "second Control", "second controller PE");
		p.add(new NOT(compToER()), "second controller PE", "first PR");
		p.add(linkToSimple(), "second controller PE", "second simple controller PE");
		p.add(notGeneric(), "second simple controller PE");
		p.add(type(Protein.class), "second simple controller PE");
		p.add(peToER(), "second simple controller PE", "second PR");
		p.add(equal(false), "first PR", "second PR");
		return p;
	}

	public static Pattern peInOut()
	{
		Pattern p = new Pattern(EntityReference.class, "changed ER");
		p.add(erToPE(), "changed ER", "input simple PE");
		p.add(linkToComplex(), "input simple PE", "input PE");
		p.add(new ParticipatesInConv(RelType.INPUT, true), "input PE", "Conversion");
		p.add(new OtherSide(), "input PE", "Conversion", "output PE");
		p.add(equal(false), "input PE", "output PE");
		p.add(linkToSimple(), "output PE", "output simple PE");
		p.add(peToER(), "output simple PE", "changed ER");
		return p;
	}

	/**
	 * Pattern for an EntityReference has distinct PhysicalEntities associated with both left and
	 * right of a Conversion.
	 * @return the pattern
	 */
	public static Pattern modifiedPESimple()
	{
		Pattern p = new Pattern(EntityReference.class, "modified ER");
		p.add(erToPE(), "modified ER", "first PE");
		p.add(participatesInConv(), "first PE", "Conversion");
		p.add(new OtherSide(), "Conversion", "second PE");
		p.add(equal(false), "first PE", "second PE");
		p.add(peToER(), "second PE", "modified ER");
		return p;
	}

	/**
	 * Pattern for the activity of an EntityReference is changed through a Conversion.
	 * @param activating desired change
	 * @param activityFeat modification features associated with activity
	 * @param inactivityFeat modification features associated with inactivity
	 * @return the pattern
	 */
	public static Pattern actChange(boolean activating,
		Map<EntityReference, Set<ModificationFeature>> activityFeat,
		Map<EntityReference, Set<ModificationFeature>> inactivityFeat)
	{
		Pattern p = peInOut();
		p.add(new OR(
			new MappedConst(differentialActivity(activating), 0, 1),
			new MappedConst(new ModificationChangeConstraint(activating, activityFeat, inactivityFeat), 0, 1)),
			"input simple PE", "output simple PE");
		return p;
	}

	/**
	 * Pattern for finding Conversions that an EntityReference is participating.
	 * @return the pattern
	 */
	public static Pattern modifierConv()
	{
		Pattern p = new Pattern(EntityReference.class, "ER");
		p.add(erToPE(), "ER", "SPE");
		p.add(linkToComplex(), "SPE", "PE");
		p.add(participatesInConv(), "PE", "Conversion");
		return p;
	}

	/**
	 * Pattern for detecting PhysicalEntity that controls a Conversion whose participants are not
	 * associated with the EntityReference of the initial PhysicalEntity.
	 * @return the pattern
	 */
	public static Pattern hasNonSelfEffect()
	{
		Pattern p = new Pattern(PhysicalEntity.class, "SPE");
		p.add(peToER(), "SPE", "ER");
		p.add(linkToComplex(), "SPE", "PE");
		p.add(peToControl(), "PE", "Control");
		p.add(controlToInter(), "Control", "Inter");
		p.add(new NOT(participantER()), "Inter", "ER");
		return p;
	}

	// Patterns for PSB

	/**
	 * Finds two Protein that appear together in a Complex.
	 * @return the pattern
	 */
	public static Pattern bindsTo()
	{
		Pattern p = new Pattern(ProteinReference.class, "first PR");
		p.add(erToPE(), "first PR", "first simple PE");
		p.add(linkToComplex(), "first simple PE", "Complex");
		p.add(new Type(Complex.class), "Complex");
		p.add(linkToSimple(), "Complex", "second simple PE");
		p.add(peToER(), "second simple PE", "second PR");
		p.add(equal(false), "first PR", "second PR");
		return p;
	}

	/**
	 * Finds transcription factors that trans-activate or trans-inhibit an entity.
	 * @return the pattern
	 */
	public static Pattern expressionWithTemplateReac()
	{
		Pattern p = new Pattern(ProteinReference.class, "TF PR");
		p.add(erToPE(), "TF PR", "TF SPE");
		p.add(linkToComplex(), "TF SPE", "TF PE");
		p.add(peToControl(), "TF PE", "Control");
		p.add(controlToTempReac(), "Control", "TempReac");
		p.add(product(), "TempReac", "product PE");
		p.add(linkToSimple(), "product PE", "product SPE");
		p.add(new Type(Protein.class), "product SPE");
		p.add(peToER(), "product SPE", "product PR");
		p.add(equal(false), "TF PR", "product PR");
		return p;
	}

	/**
	 * Finds the cases where transcription relation is shown using a Conversion instead of a
	 * TemplateReaction.
	 * @return the pattern
	 */
	public static Pattern expressionWithConversion()
	{
		Pattern p = new Pattern(ProteinReference.class, "TF PR");
		p.add(erToPE(), "TF PR", "TF SPE");
		p.add(linkToComplex(), "TF SPE", "TF PE");
		p.add(peToControl(), "TF PE", "Control");
		p.add(controlToConv(), "Control", "Conversion");
		p.add(new Empty(left()), "Conversion");
		p.add(new Size(right(), 1, Size.Type.EQUAL), "Conversion");
		p.add(right(), "Conversion", "right PE");
		p.add(linkToSimple(), "right PE", "right SPE");
		p.add(peToER(), "right SPE", "product ER");
		p.add(equal(false), "TF PR", "product ER");
		return p;
	}

	/**
	 * Finds cases where proteins affect their degradation.
	 * @return the pattern
	 */
	public static Pattern controlsDegradation()
	{
		Pattern p = new Pattern(ProteinReference.class, "upstream PR");
		p.add(erToPE(), "upstream PR", "upstream SPE");
		p.add(linkToComplex(), "upstream SPE", "upstream PE");
		p.add(peToControl(), "upstream PE", "Control");
		p.add(controlToConv(), "Control", "Conversion");
		p.add(new NOT(participantER()), "Conversion", "upstream PR");
		p.add(new Empty(new InputOrOutput(RelType.OUTPUT, true)), "Conversion");
		p.add(new InputOrOutput(RelType.INPUT, true), "Conversion", "input PE");
		p.add(linkToSimple(), "input PE", "input SPE");
		p.add(peToER(), "input SPE", "downstream PR");
		p.add(type(ProteinReference.class), "downstream PR");
		return p;
	}

	/**
	 * Finds cases where protein A changes state of B, and B is then degraded.
	 *
	 * NOTE: THIS PATTERN DOES NOT WORK. KEEPING ONLY FOR HISTORICAL REASONS.
	 *
	 * @return the pattern
	 */
	public static Pattern controlsDegradationIndirectly()
	{
		Pattern p = controlsStateChange();
		p.add(new Size(new ParticipatesInConv(RelType.INPUT, true), 1, Size.Type.EQUAL), "output PE");
		p.add(new Empty(peToControl()), "output PE");
		p.add(new ParticipatesInConv(RelType.INPUT, true), "output PE", "degrading Conv");
		p.add(new NOT(type(ComplexAssembly.class)), "degrading Conv");
		p.add(new Size(participant(), 1, Size.Type.EQUAL), "degrading Conv");
		p.add(new Empty(new InputOrOutput(RelType.OUTPUT, true)), "degrading Conv");
		p.add(new Empty(convToControl()), "degrading Conv");
		p.add(equal(false), "degrading Conv", "Conversion");
		return p;
	}

	/**
	 * Two proteins have states that are members of the same complex. Handles nested complexes and
	 * homologies. Also guarantees that relationship to the complex is through different direct
	 * complex members.
	 * @return pattern
	 */
	public static Pattern appearInSameComplex()
	{
		Pattern p = new Pattern(ProteinReference.class, "Protein 1");
		p.add(erToPE(), "Protein 1", "SPE1");
		p.add(notGeneric(), "SPE1");
		p.add(linkToComplex(), "SPE1", "PE1");
		p.add(new PathConstraint("PhysicalEntity/componentOf"), "PE1", "Complex");
		p.add(new PathConstraint("Complex/component"), "Complex", "PE2");
		p.add(equal(false), "PE1", "PE2");
		p.add(linkToSimple(), "PE2", "SPE2");
		p.add(notGeneric(), "SPE2");
		p.add(peToER(), "SPE2", "Protein 2");
		p.add(equal(false), "Protein 1", "Protein 2");
		p.add(new Type(ProteinReference.class), "Protein 2");
		return p;
	}

	/**
	 * A small molecule is in a complex with a protein.
	 * @return pattern
	 */
	public static Pattern chemicalAffectsProteinThroughBinding(Set<String> ubiques)
	{
		Pattern p = new Pattern(SmallMoleculeReference.class, "SMR");
		if (ubiques != null) p.add(notUbique(ubiques), "SMR");
		p.add(erToPE(), "SMR", "SPE1");
		p.add(notGeneric(), "SPE1");
		if (ubiques != null) p.add(notUbique(ubiques), "SPE1");
		p.add(linkToComplex(), "SPE1", "PE1");
		p.add(new PathConstraint("PhysicalEntity/componentOf"), "PE1", "Complex");
		p.add(new PathConstraint("Complex/component"), "Complex", "PE2");
		p.add(equal(false), "PE1", "PE2");
		p.add(linkToSimple(), "PE2", "SPE2");
		p.add(notGeneric(), "SPE2");
		p.add(peToER(), "SPE2", "PR");
		p.add(new Type(ProteinReference.class), "PR");
		return p;
	}

	/**
	 * A small molecule controls an interaction of which the protein is a participant.
	 * @return pattern
	 */
	public static Pattern chemicalAffectsProteinThroughControl(Set<String> ubiques)
	{
		Pattern p = new Pattern(SmallMoleculeReference.class, "controller SMR");
		if (ubiques != null) p.add(notUbique(ubiques), "controller SMR");
		p.add(erToPE(), "controller SMR", "controller simple PE");
		if (ubiques != null) p.add(notUbique(ubiques), "controller simple PE");
		p.add(notGeneric(), "controller simple PE");
		p.add(linkToComplex(), "controller simple PE", "controller PE");
		p.add(peToControl(), "controller PE", "Control");
		p.add(controlToInter(), "Control", "Interaction");
		p.add(new NOT(participantER()), "Interaction", "controller SMR");
		p.add(participant(), "Interaction", "affected PE");
		p.add(linkToSimple(), "affected PE", "affected simple PE");
		p.add(notGeneric(), "affected simple PE");
		p.add(new Type(Protein.class), "affected simple PE");
		p.add(peToER(), "affected simple PE", "affected PR");
		return p;
	}


	public static Pattern interaction()
	{
		Pattern p = new Pattern(ProteinReference.class, "Protein 1");
		p.add(erToPE(), "Protein 1", "SPE1");
		p.add(linkToComplex(), "SPE1", "PE1");
		p.add(peToInter(), "PE1", "Inter");
		p.add(interToPE(), "Inter", "PE2");
		p.add(equal(false), "PE1", "PE2");
		p.add(linkToSimple(), "PE2", "SPE2");
		p.add(equal(false), "SPE1", "SPE2");
		p.add(type(Protein.class), "SPE2");
		p.add(peToER(), "SPE2", "Protein 2");
		p.add(equal(false), "Protein 1", "Protein 2");
		return p;
	}

	/**
	 * Constructs a pattern where first and last proteins are related through an interaction. They
	 * can be participants or controllers. No limitation.
	 * @return the pattern
	 */
	public static Pattern neighborOf()
	{
		Pattern p = new Pattern(ProteinReference.class, "Protein 1");
		p.add(erToPE(), "Protein 1", "SPE1");
		p.add(notGeneric(), "SPE1");
		p.add(linkToComplex(), "SPE1", "PE1");
		p.add(peToInter(), "PE1", "Inter");
		p.add(interToPE(), "Inter", "PE2");
		p.add(linkToSimple(), "PE2", "SPE2");
		p.add(notGeneric(), "SPE2");
		p.add(equal(false), "SPE1", "SPE2");
		p.add(type(Protein.class), "SPE2");
		p.add(peToER(), "SPE2", "Protein 2");
		p.add(equal(false), "Protein 1", "Protein 2");
		return p;
	}

	/**
	 * Constructs a pattern where first and last proteins are related through an interaction. They
	 * can be participants or controllers. No limitation.
	 * @return the pattern
	 */
	public static Pattern neighborOfWithProtAndSM()
	{
		Pattern p = new Pattern(EntityReference.class, "Entity 1");
		p.add(new OR(new MappedConst(type(ProteinReference.class), 0),
			new MappedConst(type(SmallMoleculeReference.class), 0)), "Entity 1");
		p.add(erToPE(), "Entity 1", "SPE1");
		p.add(notGeneric(), "SPE1");
		p.add(linkToComplex(), "SPE1", "PE1");
		p.add(peToInter(), "PE1", "Inter");
		p.add(interToPE(), "Inter", "PE2");
		p.add(linkToSimple(), "PE2", "SPE2");
		p.add(notGeneric(), "SPE2");
		p.add(equal(false), "SPE1", "SPE2");
		p.add(new OR(new MappedConst(type(Protein.class), 0),
			new MappedConst(type(SmallMolecule.class), 0)), "SPE2");
		p.add(peToER(), "SPE2", "Entity 2");
		p.add(equal(false), "Entity 1", "Entity 2");
		return p;
	}

	/**
	 * Constructs a pattern where first and last proteins are participants of a
	 * MolecularInteraction.
	 * @return the pattern
	 */
	public static Pattern molecularInteraction()
	{
		Pattern p = new Pattern(ProteinReference.class, "Protein 1");
		p.add(erToPE(), "Protein 1", "SPE1");
		p.add(notGeneric(), "SPE1");
		p.add(linkToComplex(), "SPE1", "PE1");
		p.add(new PathConstraint("PhysicalEntity/participantOf:MolecularInteraction"), "PE1", "MI");
		p.add(participant(), "MI", "PE2");
		p.add(equal(false), "PE1", "PE2");
		p.add(linkToSimple(), "PE2", "SPE2");
		p.add(notGeneric(), "SPE2");
		p.add(type(Protein.class), "SPE2");
		p.add(new PEChainsIntersect(false), "SPE1", "PE1", "SPE2", "PE2");
		p.add(peToER(), "SPE2", "Protein 2");
		p.add(equal(false), "Protein 1", "Protein 2");
		return p;
	}

	/**
	 * Finds ProteinsReference related to an interaction. If specific types of interactions are
	 * desired, they should be sent as parameter, otherwise leave the parameter empty.
	 * @return pattern
	 */
	public static Pattern relatedProteinRefOfInter(Class<? extends Interaction>... seedType)
	{
		Pattern p = new Pattern(Interaction.class, "Interaction");

		if (seedType.length == 1)
		{
			p.add(new Type(seedType[0]), "Interaction");
		}
		else if (seedType.length > 1)
		{
			MappedConst[] mc = new MappedConst[seedType.length];
			for (int i = 0; i < mc.length; i++)
			{
				mc[i] = new MappedConst(new Type(seedType[i]), 0);
			}

			p.add(new OR(mc), "Interaction");
		}

		p.add(new OR(new MappedConst(participant(), 0, 1),
			new MappedConst(new PathConstraint(
				"Interaction/controlledOf*/controller:PhysicalEntity"), 0, 1)),
			"Interaction", "PE");

		p.add(linkToSimple(), "PE", "SPE");
		p.add(peToER(), "SPE", "PR");
		p.add(new Type(ProteinReference.class), "PR");
		return p;
	}
}
