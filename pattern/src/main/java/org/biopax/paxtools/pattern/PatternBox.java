package org.biopax.paxtools.pattern;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.constraint.*;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.biopax.paxtools.pattern.util.RelType;

import java.util.HashSet;
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
	//---- Section: Binary interaction patterns ---------------------------------------------------|

	/**
	 * Pattern for a EntityReference has a member PhysicalEntity that is controlling a state change
	 * reaction of another EntityReference.
	 * @return the pattern
	 */
	public static Pattern controlsStateChange()
	{
		Pattern p = new Pattern(SequenceEntityReference.class, "controller ER");
		p.add(linkedER(true), "controller ER", "generic controller ER");
		p.add(erToPE(), "generic controller ER", "controller simple PE");
		p.add(linkToComplex(), "controller simple PE", "controller PE");
		p.add(peToControl(), "controller PE", "Control");
		p.add(controlToConv(), "Control", "Conversion");
		p.add(new NOT(participantER()), "Conversion", "controller ER");
		p.add(new Participant(RelType.INPUT, true), "Control", "Conversion", "input PE");

		p.add(new NOT(new ConversionSide(ConversionSide.Type.OTHER_SIDE)), "input PE", "Conversion", "input PE");

		p.add(linkToSpecific(), "input PE", "input simple PE");
		p.add(new Type(SequenceEntity.class), "input simple PE");
		p.add(peToER(), "input simple PE", "changed generic ER");
		p.add(new ConversionSide(ConversionSide.Type.OTHER_SIDE), "input PE", "Conversion", "output PE");

		p.add(new NOT(new ConversionSide(ConversionSide.Type.OTHER_SIDE)), "output PE", "Conversion", "output PE");

		p.add(equal(false), "input PE", "output PE");
		p.add(linkToSpecific(), "output PE", "output simple PE");
		p.add(peToER(), "output simple PE", "changed generic ER");
		p.add(linkedER(false), "changed generic ER", "changed ER");

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
	 *
	 * @param blacklist a skip-list of ubiquitous molecules
	 * @return the pattern
	 */
	public static Pattern controlsTransportOfChemical(Blacklist blacklist)
	{
		Pattern p = new Pattern(SequenceEntityReference.class, "controller ER");
		p.add(linkedER(true), "controller ER", "controller generic ER");
		p.add(erToPE(), "controller generic ER", "controller simple PE");
		p.add(linkToComplex(), "controller simple PE", "controller PE");
		p.add(peToControl(), "controller PE", "Control");
		p.add(controlToConv(), "Control", "Conversion");
		p.add(new Participant(RelType.INPUT, blacklist, true), "Control", "Conversion", "input PE");
		p.add(linkToSimple(blacklist), "input PE", "input simple PE");
		p.add(new Type(SmallMolecule.class), "input simple PE");
		p.add(notGeneric(), "input simple PE");
		p.add(peToER(), "input simple PE", "changed generic SMR");
		p.add(new ConversionSide(ConversionSide.Type.OTHER_SIDE, blacklist, RelType.OUTPUT), "input PE", "Conversion", "output PE");
		p.add(equal(false), "input PE", "output PE");
		p.add(linkToSimple(blacklist), "output PE", "output simple PE");
		p.add(new Type(SmallMolecule.class), "output simple PE");
		p.add(notGeneric(), "output simple PE");
		p.add(peToER(), "output simple PE", "changed generic SMR");
		p.add(linkedER(false), "changed generic SMR", "changed SMR");
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
		Pattern p = new Pattern(SequenceEntityReference.class, "controller ER");
		p.add(linkedER(true), "controller ER", "controller generic ER");
		p.add(erToPE(), "controller generic ER", "controller simple PE");
		p.add(linkToComplex(), "controller simple PE", "controller PE");
		p.add(peToControl(), "controller PE", "Control");
		p.add(controlToConv(), "Control", "Conversion");

		// the controller PE is also an input
		p.add(new ParticipatesInConv(RelType.INPUT), "controller PE", "Conversion");

		// same controller simple PE is also an output
		p.add(linkToComplex(), "controller simple PE", "special output PE");
		p.add(equal(false), "special output PE", "controller PE");
		p.add(new ParticipatesInConv(RelType.OUTPUT), "special output PE", "Conversion");

		stateChange(p, "Control");

		// non-generic input and outputs are only associated with one side
		p.add(equal(false), "input simple PE", "output simple PE");
		p.add(new NOT(simplePEToConv(RelType.OUTPUT)), "input simple PE", "Conversion");
		p.add(new NOT(simplePEToConv(RelType.INPUT)), "output simple PE", "Conversion");

		p.add(equal(false), "controller ER", "changed ER");
		p.add(type(SequenceEntityReference.class), "changed ER");

		return p;
	}

	/**
	 * Pattern for a EntityReference has a member PhysicalEntity that is controlling a state change
	 * reaction of another EntityReference. This pattern is different from the original
	 * controls-state-change. The controller in this case is not modeled as a controller, but as a
	 * participant of the conversion, and it is at both sides.
	 * @return the pattern
	 */
	public static Pattern controlsStateChangeButIsParticipant()
	{
		Pattern p = new Pattern(SequenceEntityReference.class, "controller ER");
		p.add(linkedER(true), "controller ER", "controller generic ER");
		p.add(erToPE(), "controller generic ER", "controller simple PE");
		p.add(linkToComplex(), "controller simple PE", "controller PE");
		p.add(participatesInConv(), "controller PE", "Conversion");
		p.add(left(), "Conversion", "controller PE");
		p.add(right(), "Conversion", "controller PE");
		// The controller ER is not associated with the Conversion in another way.
		p.add(new NOT(new InterToPartER(1)), "Conversion", "controller PE", "controller ER");

		stateChange(p, null);

		p.add(equal(false), "controller ER", "changed ER");
		p.add(equal(false), "controller PE", "input PE");
		p.add(equal(false), "controller PE", "output PE");

		return p;
	}

	/**
	 * Pattern for a Conversion has an input PhysicalEntity and another output PhysicalEntity that
	 * belongs to the same EntityReference.
	 *
	 * @param p pattern to update
	 * @param ctrlLabel label
	 * @return the pattern
	 */
	public static Pattern stateChange(Pattern p, String ctrlLabel)
	{
		if (p == null) p = new Pattern(Conversion.class, "Conversion");

		if (ctrlLabel == null) p.add(new Participant(RelType.INPUT), "Conversion", "input PE");
		else p.add(new Participant(RelType.INPUT, true), ctrlLabel, "Conversion", "input PE");

		p.add(linkToSpecific(), "input PE", "input simple PE");
		p.add(peToER(), "input simple PE", "changed generic ER");
		p.add(new ConversionSide(ConversionSide.Type.OTHER_SIDE), "input PE", "Conversion", "output PE");
		p.add(equal(false), "input PE", "output PE");
		p.add(linkToSpecific(), "output PE", "output simple PE");
		p.add(peToER(), "output simple PE", "changed generic ER");
		p.add(linkedER(false), "changed generic ER", "changed ER");
		return p;
	}

	/**
	 * Pattern for an entity is producing a small molecule, and the small molecule controls state
	 * change of another molecule.
	 *
	 * @param blacklist a skip-list of ubiquitous molecules
	 * @return the pattern
	 */
	public static Pattern controlsStateChangeThroughControllerSmallMolecule(Blacklist blacklist)
	{
		Pattern p = new Pattern(SequenceEntityReference.class, "upper controller ER");
		p.add(linkedER(true), "upper controller ER", "upper controller generic ER");
		p.add(erToPE(), "upper controller generic ER", "upper controller simple PE");
		p.add(linkToComplex(), "upper controller simple PE", "upper controller PE");
		p.add(peToControl(), "upper controller PE", "upper Control");
		p.add(controlToConv(), "upper Control", "upper Conversion");
		p.add(new NOT(participantER()), "upper Conversion", "upper controller ER");
		p.add(new Participant(RelType.OUTPUT, blacklist), "upper Conversion", "controller PE");
		p.add(type(SmallMolecule.class), "controller PE");
		if (blacklist != null) p.add(new NonUbique(blacklist), "controller PE");

		// the linker small mol is at also an input
		p.add(new NOT(new ConstraintChain(
			new ConversionSide(ConversionSide.Type.OTHER_SIDE), linkToSpecific())),
			"controller PE", "upper Conversion", "controller PE");

		p.add(peToControl(), "controller PE", "Control");
		p.add(controlToConv(), "Control", "Conversion");
		p.add(equal(false), "upper Conversion", "Conversion");

//		p.add(nextInteraction(), "upper Conversion", "Conversion");

		stateChange(p, "Control");

		p.add(type(SequenceEntityReference.class), "changed ER");
		p.add(equal(false), "upper controller ER", "changed ER");
		p.add(new NOT(participantER()), "Conversion", "upper controller ER");

		return p;
	}

	/**
	 * Pattern for an entity is producing a small molecule, and the small molecule controls state
	 * change of another molecule.
	 *
	 * @param blacklist a skip-list of ubiquitous molecules
	 * @return the pattern
	 */
	public static Pattern controlsStateChangeThroughBindingSmallMolecule(Blacklist blacklist)
	{
		Pattern p = new Pattern(SequenceEntityReference.class, "upper controller ER");
		p.add(linkedER(true), "upper controller ER", "upper controller generic ER");
		p.add(erToPE(), "upper controller ER", "upper controller simple PE");
		p.add(linkToComplex(), "upper controller simple PE", "upper controller PE");
		p.add(peToControl(), "upper controller PE", "upper Control");
		p.add(controlToConv(), "upper Control", "upper Conversion");
		p.add(new NOT(participantER()), "upper Conversion", "upper controller ER");
		p.add(new Participant(RelType.OUTPUT, blacklist, true), "upper Control", "upper Conversion", "SM");
		p.add(type(SmallMolecule.class), "SM");
		if (blacklist != null) p.add(new NonUbique(blacklist), "SM");

		// the linker small mol is at also an input
		p.add(new NOT(new ConstraintChain(
			new ConversionSide(ConversionSide.Type.OTHER_SIDE), linkToSpecific())),
			"SM", "upper Conversion", "SM");

		p.add(new ParticipatesInConv(RelType.INPUT), "SM", "Conversion");
		p.add(peToER(), "SM", "SM ER");
		p.add(equal(false), "upper Conversion", "Conversion");

//		p.add(nextInteraction(), "upper Conversion", "Conversion");

		stateChange(p, null);

		p.add(type(SequenceEntityReference.class), "changed ER");
		p.add(equal(false), "upper controller ER", "changed ER");
		p.add(new NOT(participantER()), "Conversion", "upper controller ER");
		p.add(compToER(), "output PE", "SM ER");

		return p;
	}

	/**
	 * Finds cases where proteins affect their degradation.
	 * @return the pattern
	 */
	public static Pattern controlsStateChangeThroughDegradation()
	{
		Pattern p = new Pattern(SequenceEntityReference.class, "upstream ER");
		p.add(linkedER(true), "upstream ER", "upstream generic ER");
		p.add(erToPE(), "upstream generic ER", "upstream SPE");
		p.add(linkToComplex(), "upstream SPE", "upstream PE");
		p.add(peToControl(), "upstream PE", "Control");
		p.add(controlToConv(), "Control", "Conversion");
		p.add(new NOT(participantER()), "Conversion", "upstream ER");
		p.add(new Empty(new Participant(RelType.OUTPUT)), "Conversion");
		p.add(new Participant(RelType.INPUT), "Conversion", "input PE");
		p.add(linkToSpecific(), "input PE", "input SPE");
		p.add(peToER(), "input SPE", "downstream generic ER");
		p.add(type(SequenceEntityReference.class), "downstream generic ER");
		p.add(linkedER(false), "downstream generic ER", "downstream ER");
		return p;
	}

	public static Pattern controlsPhosphorylation()
	{
		Pattern p = controlsStateChange();
		p.add(new NOT(ConBox.linkToSpecific()), "input PE", "output simple PE");
		p.add(new NOT(ConBox.linkToSpecific()), "output PE", "input simple PE");
		p.add(new ModificationChangeConstraint(ModificationChangeConstraint.Type.ANY,
			"phospho"), "input simple PE", "output simple PE");
		return p;
	}

	/**
	 * Pattern for a Protein controlling a reaction whose participant is a small molecule.
	 *
	 * @param blacklist a skip-list of ubiquitous molecules
	 * @param consumption true/false (TODO explain)
	 * @return the pattern
	 */
	public static Pattern controlsMetabolicCatalysis(Blacklist blacklist, boolean consumption)
	{
		Pattern p = new Pattern(SequenceEntityReference.class, "controller ER");
		p.add(linkedER(true), "controller ER", "controller generic ER");
		p.add(erToPE(), "controller generic ER", "controller simple PE");
		p.add(linkToComplex(), "controller simple PE", "controller PE");
		p.add(peToControl(), "controller PE", "Control");
		p.add(controlToConv(), "Control", "Conversion");
		p.add(new NOT(participantER()), "Conversion", "controller ER");

		p.add(new Participant(consumption ? RelType.INPUT : RelType.OUTPUT, blacklist, true),
			"Control", "Conversion", "part PE");

		p.add(linkToSimple(blacklist), "part PE", "part SM");
		p.add(notGeneric(), "part SM");
		p.add(type(SmallMolecule.class), "part SM");
		p.add(peToER(), "part SM", "part SMR");

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
	 *
	 * @param blacklist to detect ubiquitous small molecules
	 * @return the pattern
	 */
	public static Pattern catalysisPrecedes(Blacklist blacklist)
	{
		Pattern p = new Pattern(SequenceEntityReference.class, "first ER");
		p.add(linkedER(true), "first ER", "first generic ER");
		p.add(erToPE(), "first generic ER", "first simple controller PE");
		p.add(linkToComplex(), "first simple controller PE", "first controller PE");
		p.add(peToControl(), "first controller PE", "first Control");
		p.add(controlToConv(), "first Control", "first Conversion");
		p.add(new Participant(RelType.OUTPUT, blacklist, true), "first Control", "first Conversion", "linker PE");
		p.add(new NOT(new ConstraintChain(new ConversionSide(ConversionSide.Type.OTHER_SIDE), linkToSpecific())), "linker PE", "first Conversion", "linker PE");
		p.add(type(SmallMolecule.class), "linker PE");
		p.add(new ParticipatesInConv(RelType.INPUT, blacklist), "linker PE", "second Conversion");
		p.add(new NOT(new ConstraintChain(new ConversionSide(ConversionSide.Type.OTHER_SIDE), linkToSpecific())), "linker PE", "second Conversion", "linker PE");
		p.add(equal(false), "first Conversion", "second Conversion");

		// make sure that conversions are not replicates or reverse of each other
		// and also outward facing sides of reactions contain at least one non ubique
		p.add(new ConstraintAdapter(3, blacklist)
		{
			@Override
			public boolean satisfies(Match match, int... ind)
			{
				Conversion cnv1 = (Conversion) match.get(ind[0]);
				Conversion cnv2 = (Conversion) match.get(ind[1]);
				SmallMolecule linker = (SmallMolecule) match.get(ind[2]);

				Set<PhysicalEntity> input1 = cnv1.getLeft().contains(linker) ? cnv1.getRight() : cnv1.getLeft();
				Set<PhysicalEntity> input2 = cnv2.getLeft().contains(linker) ? cnv2.getLeft() : cnv2.getRight();
				Set<PhysicalEntity> output1 = cnv1.getLeft().contains(linker) ? cnv1.getLeft() : cnv1.getRight();
				Set<PhysicalEntity> output2 = cnv2.getLeft().contains(linker) ? cnv2.getRight() : cnv2.getLeft();

				if (input1.equals(input2) && output1.equals(output2)) return false;
				if (input1.equals(output2) && output1.equals(input2)) return false;

				if (blacklist != null)
				{
					Set<PhysicalEntity> set = new HashSet<>(input1);
					set = blacklist.getNonUbiques(set, RelType.INPUT);
					set.removeAll(output2);
					if (set.isEmpty())
					{
						set.addAll(output2);
						set = blacklist.getNonUbiques(set, RelType.OUTPUT);
						set.removeAll(input1);

						if (set.isEmpty()) return false;
					}
				}
				return true;
			}
		}, "first Conversion", "second Conversion", "linker PE");

		p.add(new RelatedControl(RelType.INPUT, blacklist), "linker PE", "second Conversion", "second Control");
		p.add(controllerPE(), "second Control", "second controller PE");
		p.add(new NOT(compToER()), "second controller PE", "first ER");
		p.add(linkToSpecific(), "second controller PE", "second simple controller PE");
		p.add(type(SequenceEntity.class), "second simple controller PE");
		p.add(peToER(), "second simple controller PE", "second generic ER");
		p.add(linkedER(false), "second generic ER", "second ER");
		p.add(equal(false), "first ER", "second ER");
		return p;
	}

	/**
	 * Finds transcription factors that trans-activate or trans-inhibit an entity.
	 * @return the pattern
	 */
	public static Pattern controlsExpressionWithTemplateReac()
	{
		Pattern p = new Pattern(SequenceEntityReference.class, "TF ER");
		p.add(linkedER(true), "TF ER", "TF generic ER");
		p.add(erToPE(), "TF generic ER", "TF SPE");
		p.add(linkToComplex(), "TF SPE", "TF PE");
		p.add(peToControl(), "TF PE", "Control");
		p.add(controlToTempReac(), "Control", "TempReac");
		p.add(product(), "TempReac", "product PE");
		p.add(linkToSpecific(), "product PE", "product SPE");
		p.add(new Type(SequenceEntity.class), "product SPE");
		p.add(peToER(), "product SPE", "product generic ER");
		p.add(linkedER(false), "product generic ER", "product ER");
		p.add(equal(false), "TF ER", "product ER");
		return p;
	}

	/**
	 * Finds the cases where transcription relation is shown using a Conversion instead of a
	 * TemplateReaction.
	 * @return the pattern
	 */
	public static Pattern controlsExpressionWithConversion()
	{
		Pattern p = new Pattern(SequenceEntityReference.class, "TF ER");
		p.add(linkedER(true), "TF ER", "TF generic ER");
		p.add(erToPE(), "TF generic ER", "TF SPE");
		p.add(linkToComplex(), "TF SPE", "TF PE");
		p.add(peToControl(), "TF PE", "Control");
		p.add(controlToConv(), "Control", "Conversion");
		p.add(new Size(right(), 1, Size.Type.EQUAL), "Conversion");
		p.add(new OR(new MappedConst(new Empty(left()), 0), new MappedConst(new ConstraintAdapter(1)
		{
			@Override
			public boolean satisfies(Match match, int... ind)
			{
				Conversion cnv = (Conversion) match.get(ind[0]);
				Set<PhysicalEntity> left = cnv.getLeft();
				if (left.size() > 1) return false;
				if (left.isEmpty()) return true;
				PhysicalEntity pe = left.iterator().next();
				if (pe instanceof NucleicAcid)
				{
					PhysicalEntity rPE = cnv.getRight().iterator().next();
					return rPE instanceof Protein;
				}
				return false;
			}
		}, 0)), "Conversion");
		p.add(right(), "Conversion", "right PE");
		p.add(linkToSpecific(), "right PE", "right SPE");
		p.add(new Type(SequenceEntity.class), "right SPE");
		p.add(peToER(), "right SPE", "product generic ER");
		p.add(linkedER(false), "product generic ER", "product ER");
		p.add(equal(false), "TF ER", "product ER");
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
		p.add(new Size(new ParticipatesInConv(RelType.INPUT), 1, Size.Type.EQUAL), "output PE");
		p.add(new Empty(peToControl()), "output PE");
		p.add(new ParticipatesInConv(RelType.INPUT), "output PE", "degrading Conv");
		p.add(new NOT(type(ComplexAssembly.class)), "degrading Conv");
		p.add(new Size(participant(), 1, Size.Type.EQUAL), "degrading Conv");
		p.add(new Empty(new Participant(RelType.OUTPUT)), "degrading Conv");
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
	public static Pattern inComplexWith()
	{
		Pattern p = new Pattern(SequenceEntityReference.class, "Protein 1");
		p.add(linkedER(true), "Protein 1", "generic Protein 1");
		p.add(erToPE(), "generic Protein 1", "SPE1");
		p.add(linkToComplex(), "SPE1", "PE1");
		p.add(new PathConstraint("PhysicalEntity/componentOf"), "PE1", "Complex");
		p.add(new PathConstraint("Complex/component"), "Complex", "PE2");
		p.add(equal(false), "PE1", "PE2");
		p.add(linkToSpecific(), "PE2", "SPE2");
		p.add(peToER(), "SPE2", "generic Protein 2");
		p.add(linkedER(false), "generic Protein 2", "Protein 2");
		p.add(equal(false), "Protein 1", "Protein 2");
		p.add(new Type(SequenceEntityReference.class), "Protein 2");
		return p;
	}

	/**
	 * A small molecule is in a complex with a protein.
	 *
	 * @param blacklist a skip-list of ubiquitous molecules
	 * @return pattern
	 */
	public static Pattern chemicalAffectsProteinThroughBinding(Blacklist blacklist)
	{
		Pattern p = new Pattern(SmallMoleculeReference.class, "SMR");
		p.add(erToPE(), "SMR", "SPE1");
		p.add(notGeneric(), "SPE1");
		if (blacklist != null) p.add(new NonUbique(blacklist), "SPE1");
		p.add(linkToComplex(), "SPE1", "PE1");
		p.add(new PathConstraint("PhysicalEntity/componentOf"), "PE1", "Complex");
		p.add(new PathConstraint("Complex/component"), "Complex", "PE2");
		p.add(equal(false), "PE1", "PE2");
		p.add(linkToSpecific(), "PE2", "SPE2");
		p.add(new Type(SequenceEntity.class), "SPE2");
		p.add(peToER(), "SPE2", "generic ER");
		p.add(linkedER(false), "generic ER", "ER");
		return p;
	}

	/**
	 * A small molecule controls an interaction of which the protein is a participant.
	 * @return pattern
	 */
	public static Pattern chemicalAffectsProteinThroughControl()
	{
		Pattern p = new Pattern(SmallMoleculeReference.class, "controller SMR");
		p.add(erToPE(), "controller SMR", "controller simple PE");
		p.add(notGeneric(), "controller simple PE");
		p.add(linkToComplex(), "controller simple PE", "controller PE");
		p.add(peToControl(), "controller PE", "Control");
		p.add(controlToInter(), "Control", "Interaction");
		p.add(new NOT(participantER()), "Interaction", "controller SMR");
		p.add(participant(), "Interaction", "affected PE");
		p.add(linkToSpecific(), "affected PE", "affected simple PE");
		p.add(new Type(SequenceEntity.class), "affected simple PE");
		p.add(peToER(), "affected simple PE", "affected generic ER");
		p.add(linkedER(false), "affected generic ER", "affected ER");
		return p;
	}

	/**
	 * Constructs a pattern where first and last proteins are related through an interaction. They
	 * can be participants or controllers. No limitation.
	 * @return the pattern
	 */
	public static Pattern neighborOf()
	{
		Pattern p = new Pattern(SequenceEntityReference.class, "Protein 1");
		p.add(linkedER(true), "Protein 1", "generic Protein 1");
		p.add(erToPE(), "generic Protein 1", "SPE1");
		p.add(linkToComplex(), "SPE1", "PE1");
		p.add(peToInter(), "PE1", "Inter");
		p.add(interToPE(), "Inter", "PE2");
		p.add(linkToSpecific(), "PE2", "SPE2");
		p.add(equal(false), "SPE1", "SPE2");
		p.add(type(SequenceEntity.class), "SPE2");
		p.add(peToER(), "SPE2", "generic Protein 2");
		p.add(linkedER(false), "generic Protein 2", "Protein 2");
		p.add(equal(false), "Protein 1", "Protein 2");
		return p;
	}

	/**
	 * Constructs a pattern where first and last small molecules are substrates to the same
	 * biochemical reaction.
	 *
	 * @param blacklist a skip-list of ubiquitous molecules
	 * @return the pattern
	 */
	public static Pattern reactsWith(Blacklist blacklist)
	{
		Pattern p = new Pattern(SmallMoleculeReference.class, "SMR1");
		p.add(erToPE(), "SMR1", "SPE1");
		p.add(notGeneric(), "SPE1");
		p.add(linkToComplex(blacklist), "SPE1", "PE1");
		p.add(new ParticipatesInConv(RelType.INPUT, blacklist), "PE1", "Conv");
		p.add(type(BiochemicalReaction.class), "Conv");
		p.add(new InterToPartER(InterToPartER.Direction.ONESIDERS), "Conv", "SMR1");
		p.add(new ConversionSide(ConversionSide.Type.SAME_SIDE, blacklist, RelType.INPUT), "PE1", "Conv", "PE2");
		p.add(type(SmallMolecule.class), "PE2");
		p.add(linkToSpecific(), "PE2", "SPE2");
		p.add(notGeneric(), "SPE2");
		p.add(new PEChainsIntersect(false), "SPE1", "PE1", "SPE2", "PE2");
		p.add(peToER(), "SPE2", "SMR2");
		p.add(equal(false), "SMR1", "SMR2");
		p.add(new InterToPartER(InterToPartER.Direction.ONESIDERS), "Conv", "SMR2");
		return p;
	}

	/**
	 * Constructs a pattern where first small molecule is an input a biochemical reaction that
	 * produces the second small molecule.
	 * biochemical reaction.
	 *
	 * @param blacklist a skip-list of ubiquitous molecules
	 * @return the pattern
	 */
	public static Pattern usedToProduce(Blacklist blacklist)
	{
		Pattern p = new Pattern(SmallMoleculeReference.class, "SMR1");
		p.add(erToPE(), "SMR1", "SPE1");
		p.add(notGeneric(), "SPE1");
		p.add(linkToComplex(blacklist), "SPE1", "PE1");
		p.add(new ParticipatesInConv(RelType.INPUT, blacklist), "PE1", "Conv");
		p.add(type(BiochemicalReaction.class), "Conv");
		p.add(new InterToPartER(InterToPartER.Direction.ONESIDERS), "Conv", "SMR1");
		p.add(new ConversionSide(ConversionSide.Type.OTHER_SIDE, blacklist, RelType.OUTPUT), "PE1", "Conv", "PE2");
		p.add(type(SmallMolecule.class), "PE2");
		p.add(linkToSimple(blacklist), "PE2", "SPE2");
		p.add(notGeneric(), "SPE2");
		p.add(equal(false), "SPE1", "SPE2");
		p.add(peToER(), "SPE2", "SMR2");
		p.add(equal(false), "SMR1", "SMR2");
		p.add(new InterToPartER(InterToPartER.Direction.ONESIDERS), "Conv", "SMR2");
		return p;
	}

	/**
	 * Constructs a pattern where first and last molecules are participants of a
	 * MolecularInteraction.
	 * @return the pattern
	 */
	public static Pattern molecularInteraction()
	{
		Pattern p = new Pattern(SequenceEntityReference.class, "Protein 1");
		p.add(linkedER(true), "Protein 1", "generic Protein 1");
		p.add(erToPE(), "generic Protein 1", "SPE1");
		p.add(linkToComplex(), "SPE1", "PE1");
		p.add(new PathConstraint("PhysicalEntity/participantOf:MolecularInteraction"), "PE1", "MI");
		p.add(participant(), "MI", "PE2");
		p.add(equal(false), "PE1", "PE2");

		// participants are not both baits or preys
		p.add(new NOT(new AND(new MappedConst(isPrey(), 0), new MappedConst(isPrey(), 1))), "PE1", "PE2");
		p.add(new NOT(new AND(new MappedConst(isBait(), 0), new MappedConst(isBait(), 1))), "PE1", "PE2");

		p.add(linkToSpecific(), "PE2", "SPE2");
		p.add(type(SequenceEntity.class), "SPE2");
		p.add(new PEChainsIntersect(false), "SPE1", "PE1", "SPE2", "PE2");
		p.add(peToER(), "SPE2", "generic Protein 2");
		p.add(linkedER(false), "generic Protein 2", "Protein 2");
		p.add(equal(false), "Protein 1", "Protein 2");
		return p;
	}


	//----- Section: Other patterns ---------------------------------------------------------------|

	/**
	 * Finds ProteinsReference related to an interaction. If specific types of interactions are
	 * desired, they should be sent as parameter, otherwise leave the parameter empty.
	 *
	 * @param seedType specific BioPAX interaction sub-types (interface classes)
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

		p.add(linkToSpecific(), "PE", "SPE");
		p.add(peToER(), "SPE", "generic PR");
		p.add(new Type(ProteinReference.class), "generic PR");
		p.add(linkedER(false), "generic PR", "PR");
		return p;
	}

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
		p.add(linkToSpecific(), "Complex", "second simple PE");
		p.add(equal(false), "first simple PE", "second simple PE");
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

	public static Pattern peInOut()
	{
		Pattern p = new Pattern(EntityReference.class, "changed ER");
		p.add(erToPE(), "changed ER", "input simple PE");
		p.add(linkToComplex(), "input simple PE", "input PE");
		p.add(new ParticipatesInConv(RelType.INPUT), "input PE", "Conversion");
		p.add(new ConversionSide(ConversionSide.Type.OTHER_SIDE), "input PE", "Conversion", "output PE");
		p.add(equal(false), "input PE", "output PE");
		p.add(linkToSpecific(), "output PE", "output simple PE");
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
		p.add(new ConversionSide(ConversionSide.Type.OTHER_SIDE), "first PE", "Conversion", "second PE");
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
			new MappedConst(new ActivityModificationChangeConstraint(
				activating, activityFeat, inactivityFeat), 0, 1)),
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
		p.add(linkToSpecific(), "Complex", "second simple PE");
		p.add(peToER(), "second simple PE", "second PR");
		p.add(equal(false), "first PR", "second PR");
		return p;
	}

}
