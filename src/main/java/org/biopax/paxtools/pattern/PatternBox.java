package org.biopax.paxtools.pattern;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.constraint.*;

import java.util.Map;
import java.util.Set;

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
		p.addConstraint(ConBox.erToPE(), "first ER", "first simple PE");
		p.addConstraint(ConBox.linkToComplex(), "first simple PE", "Complex");
		p.addConstraint(new Type(Complex.class), "Complex");
		p.addConstraint(ConBox.linkToSimple(), "Complex", "second simple PE");
		p.addConstraint(new PEChainsIntersect(false, true), "first simple PE", "Complex", "second simple PE", "Complex");
		p.addConstraint(ConBox.peToER(), "second simple PE", "second ER");
		p.addConstraint(ConBox.equal(false), "first ER", "second ER");
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
		p.addConstraint(new ActivityConstraint(true), "Complex");
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

		p.addConstraint(ConBox.peToControl(), "Complex", "Control");
		p.addConstraint(ConBox.controlToTempReac(), "Control", "TR");
		p.addConstraint(new NOT(ConBox.participantER()), "TR", "first ER");
		p.addConstraint(new NOT(ConBox.participantER()), "TR", "second ER");
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

		p.addConstraint(ConBox.peToControl(), "Complex", "Control");
		p.addConstraint(ConBox.controlToConv(), "Control", "Conversion");
		p.addConstraint(new NOT(ConBox.participantER()), "Control", "first ER");
		p.addConstraint(new NOT(ConBox.participantER()), "Control", "second ER");
		return p;
	}

	/**
	 * Pattern for a EntityReference has a member PhysicalEntity that is controlling a state change
	 * reaction of another EntityReference.
	 * @param considerGenerics option to handle generic memberships in the pattern
	 * @return the pattern
	 */
	public static Pattern controlsStateChange(boolean considerGenerics)
	{
		Pattern p = new Pattern(EntityReference.class, "controller ER");
		p.addConstraint(ConBox.erToPE(), "controller ER", "controller simple PE");
		Constraint c = considerGenerics ? ConBox.linkToComplex() : ConBox.withComplexes();
		p.addConstraint(c,"controller simple PE", "controller PE");
		p.addConstraint(ConBox.peToControl(), "controller PE", "Control");
		p.addConstraint(ConBox.controlToConv(), "Control", "Conversion");

		Pattern p2 = stateChange(considerGenerics);
		p.addPattern(p2);

		p.addConstraint(ConBox.equal(false), "controller ER", "changed ER");

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
		p.addConstraint(new InputOrOutput(RelType.INPUT, true), "Conversion", "input PE");
		Constraint c = considerGenerics ? ConBox.linkToSimple() : ConBox.withSimpleMembers();
		p.addConstraint(c, "input PE", "input simple PE");
		p.addConstraint(ConBox.peToER(), "input simple PE", "changed ER");
		p.addConstraint(new OtherSide(), "input PE", "Conversion", "output PE");
		p.addConstraint(ConBox.equal(false), "input PE", "output PE");
		c = considerGenerics ? ConBox.linkToComplex() : ConBox.withComplexes();
		p.addConstraint(c, "output PE", "output simple PE");
		p.addConstraint(ConBox.peToER(), "output simple PE", "changed ER");
		return p;
	}

	/**
	 * Pattern for detecting two EntityReferences are controlling consecutive reactions, where
	 * output of one reaction is input to the other.
	 * @param ubiqueIDs IDs of ubiquitous molecules, ignored if null
	 * @return the pattern
	 */
	public static Pattern consecutiveCatalysis(Set<String> ubiqueIDs)
	{
		Pattern p = new Pattern(EntityReference.class, "first ER");
		p.addConstraint(ConBox.erToPE(), "first ER", "first simple controller PE");
		p.addConstraint(ConBox.linkToComplex(), "first simple controller PE", "first controller PE");
		p.addConstraint(ConBox.peToControl(), "first controller PE", "first Control");
		p.addConstraint(ConBox.controlToConv(), "first Control", "first Conversion");
		p.addConstraint(new ParticipatingPE(RelType.OUTPUT, false), "first Control", "first Conversion", "linker PE");
		if (ubiqueIDs != null) p.addConstraint(ConBox.notUbique(ubiqueIDs), "linker PE");
		p.addConstraint(new ParticipatesInConv(RelType.INPUT, false), "linker PE", "second Conversion");
		p.addConstraint(new RelatedControl(RelType.INPUT), "linker PE", "second Conversion", "second Control");
		p.addConstraint(ConBox.controllerPE(), "second Control", "second controller PE");
		p.addConstraint(new NOT(ConBox.compToER()), "second controller PE", "first ER");
		p.addConstraint(ConBox.linkToSimple(), "second controller PE", "second simple controller PE");
		p.addConstraint(ConBox.peToER(), "second simple controller PE", "second ER");
		return p;
	}

	public static Pattern peInOut()
	{
		Pattern p = new Pattern(EntityReference.class, "changed ER");
		p.addConstraint(ConBox.erToPE(), "changed ER", "input simple PE");
		p.addConstraint(ConBox.linkToComplex(), "input simple PE", "input PE");
		p.addConstraint(new ParticipatesInConv(RelType.INPUT, true), "input PE", "Conversion");
		p.addConstraint(new OtherSide(), "input PE", "Conversion", "output PE");
		p.addConstraint(ConBox.equal(false), "input PE", "output PE");
		p.addConstraint(ConBox.linkToSimple(), "output PE", "output simple PE");
		p.addConstraint(ConBox.peToER(), "output simple PE", "changed ER");
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
		p.addConstraint(ConBox.erToPE(), "modified ER", "first PE");
		p.addConstraint(ConBox.participatesInConv(), "first PE", "Conversion");
		p.addConstraint(new OtherSide(), "Conversion", "second PE");
		p.addConstraint(ConBox.equal(false), "first PE", "second PE");
		p.addConstraint(ConBox.peToER(), "second PE", "modified ER");
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
		p.addConstraint(new OR(
			new MappedConst(ConBox.differentialActivity(activating), 0, 1),
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
		p.addConstraint(ConBox.erToPE(), "ER", "SPE");
		p.addConstraint(ConBox.linkToComplex(), "SPE", "PE");
		p.addConstraint(ConBox.participatesInConv(), "PE", "Conversion");
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
		p.addConstraint(ConBox.peToER(), "SPE", "ER");
		p.addConstraint(ConBox.linkToComplex(), "SPE", "PE");
		p.addConstraint(ConBox.peToControl(), "PE", "Control");
		p.addConstraint(ConBox.controlToInter(), "Control", "Inter");
		p.addConstraint(new NOT(ConBox.participantER()), "Inter", "ER");
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
		p.addConstraint(ConBox.erToPE(), "first PR", "first simple PE");
		p.addConstraint(ConBox.linkToComplex(), "first simple PE", "Complex");
		p.addConstraint(new Type(Complex.class), "Complex");
		p.addConstraint(ConBox.linkToSimple(), "Complex", "second simple PE");
		p.addConstraint(ConBox.peToER(), "second simple PE", "second PR");
		p.addConstraint(ConBox.equal(false), "first PR", "second PR");
		return p;
	}

	/**
	 * Finds proteins that interact through a MolecularInteraction.
	 * @return the pattern
	 */
	public static Pattern physicallyInteracts()
	{
		Pattern p = new Pattern(ProteinReference.class, "first PR");
		p.addConstraint(ConBox.erToPE(), "first PR", "first simple PE");
		p.addConstraint(ConBox.linkToComplex(), "first simple PE", "first PE");
		p.addConstraint(ConBox.molecularInteraction(), "first PE", "Interaction");
		p.addConstraint(ConBox.participant(), "Interaction", "second PE");
		p.addConstraint(ConBox.linkToSimple(), "second PE", "second simple PE");
		p.addConstraint(ConBox.peToER(), "second simple PE", "second ER");
		p.addConstraint(ConBox.equal(false), "first ER", "second ER");
		return p;
	}

	/**
	 * Finds transcription factors that trans-activate or trans-inhibit an entity.
	 * @return the pattern
	 */
	public static Pattern transcriptionWithTemplateReac()
	{
		Pattern p = new Pattern(ProteinReference.class, "TF PR");
		p.addConstraint(ConBox.erToPE(), "TF PR", "TF SPE");
		p.addConstraint(ConBox.linkToComplex(), "TF SPE", "TF PE");
		p.addConstraint(ConBox.peToControl(), "TF PE", "Control");
		p.addConstraint(ConBox.controlToTempReac(), "Control", "TempReac");
		p.addConstraint(ConBox.product(), "TempReac", "product PE");
		p.addConstraint(ConBox.linkToSimple(), "product PE", "product SPE");
		p.addConstraint(ConBox.peToER(), "product SPE", "product ER");
		p.addConstraint(ConBox.equal(false), "TF PR", "product ER");
		return p;
	}

	/**
	 * Finds the cases where transcription relation is shown using a Conversion instead of a
	 * TemplateReaction.
	 * @return the pattern
	 */
	public static Pattern transcriptionWithConversion()
	{
		Pattern p = new Pattern(ProteinReference.class, "TF PR");
		p.addConstraint(ConBox.erToPE(), "TF PR", "TF SPE");
		p.addConstraint(ConBox.linkToComplex(), "TF SPE", "TF PE");
		p.addConstraint(ConBox.peToControl(), "TF PE", "Control");
		p.addConstraint(ConBox.controlToConv(), "Control", "Conversion");
		p.addConstraint(new Empty(ConBox.left()), "Conversion");
		p.addConstraint(ConBox.right(), "Conversion", "right PE");
		p.addConstraint(ConBox.linkToSimple(), "right PE", "right SPE");
		p.addConstraint(ConBox.peToER(), "product ER");
		p.addConstraint(ConBox.equal(false), "TF PR", "product ER");
		return p;
	}

	/**
	 * Finds cases where proteins affect their degradation.
	 * @return the pattern
	 */
	public static Pattern degradation()
	{
		Pattern p = new Pattern(ProteinReference.class, "upstream ER");
		p.addConstraint(ConBox.erToPE(), "upstream SPE");
		p.addConstraint(ConBox.linkToComplex(), "upstream SPE", "upstream PE");
		p.addConstraint(ConBox.peToControl(), "upstream PE", "Control");
		p.addConstraint(ConBox.controlToConv(), "Control", "Conversion");
		p.addConstraint(new Empty(ConBox.right()), "Conversion");
		p.addConstraint(ConBox.left(), "Conversion", "left PE");
		p.addConstraint(ConBox.linkToSimple(), "left PE", "left SPE");
		p.addConstraint(ConBox.peToER(), "left SPE", "downstream ER");
		p.addConstraint(ConBox.equal(false), "upstream ER", "downstream ER");
		return p;
	}
}
