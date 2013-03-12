package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.pattern.Constraint;
import org.biopax.paxtools.pattern.MappedConst;

import java.io.FileDescriptor;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Some predefined constraints.
 *
 * @author Ozgun Babur
 */
public class ConBox
{
	/**
	 * From EntityReference to the member PhysicalEntity
	 * @return generative constraint to get to the member PhysicalEntity of the EntityReference
	 */
	public static Constraint erToPE()
	{
		return new PathConstraint("EntityReference/entityReferenceOf");
	}

	/**
	 * From SimplePhysicalEntity to its EntityReference.
	 * @return generative constraint to get the EntityReference of the SimplePhysicalEntity
	 */
	public static Constraint peToER()
	{
		return new PathConstraint("SimplePhysicalEntity/entityReference");
	}

	/**
	 * From PhysicalEntity to the downstream Control
	 * @return generative constraint to get the Control that the PhysicalEntity is a controller
	 */
	public static Constraint peToControl()
	{
		return new PathConstraint("PhysicalEntity/controllerOf");
	}

	/**
	 * From Control to the controlled Process
	 * @return generative constraint to get the controlled Process of the Control
	 */
	public static Constraint controlled()
	{
		return new PathConstraint("Control/controlled");
	}

	/**
	 * From Control to the controller PhysicalEntity
	 * @return generative constraint to get the controller PhysicalEntity of the Control
	 */
	public static Constraint controllerPE()
	{
		return new PathConstraint("Control/controller:PhysicalEntity");
	}

	/**
	 * From Control to the controlled Conversion, traversing downstream Controls recursively.
	 * @return generative constraint to get the downstream (controlled) Conversion of the Control
	 */
	public static Constraint controlToConv()
	{
		return new PathConstraint("Control/controlled*:Conversion");
	}

	/**
	 * From Control to the controlled TemplateReaction, traversing downstream Controls recursively.
	 * @return generative constraint to get the downstream (controlled) TemplateReaction of the
	 * Control
	 */
	public static Constraint controlToTempReac()
	{
		return new PathConstraint("Control/controlled*:TemplateReaction");
	}

	/**
	 * From Control to the controlled Interaction, traversing downstream Controls recursively.
	 * @return generative constraint to get the downstream (controlled) Interaction of the Control
	 */
	public static Constraint controlToInter()
	{
		return new PathConstraint("Control/controlled*:Interaction");
	}

	/**
	 * From Conversion to the upstream Control (and its upstream Control recursively).
	 * @return generative constraint to get the upstream Controls of the Conversion
	 */
	public static Constraint convToControl()
	{
		return new PathConstraint("Conversion/controlledOf*");
	}

	/**
	 * From Interaction to the upstream Control (and its upstream Control recursively).
	 * @return generative constraint to get the upstream Controls of the Interaction
	 */
	public static Constraint interToControl()
	{
		return new PathConstraint("Interaction/controlledOf*");
	}

	/**
	 * From PhysicalEntity to the controlled Conversion, skipping the middle Control.
	 * @return generative constraint to get the Conversion that the PhysicalEntity controls
	 */
	public static Constraint controlsConv()
	{
		return new PathConstraint("PhysicalEntity/controllerOf/controlled*:Conversion");
	}

	/**
	 * From PhysicalEntity to the controlled Interaction, skipping the middle Control.
	 * @return generative constraint to get the Interaction that the PhysicalEntity controls
	 */
	public static Constraint controlsInteraction()
	{
		return new PathConstraint("PhysicalEntity/controllerOf/controlled*:Interaction");
	}

	/**
	 * From PhysicalEntity to its generic equivalents, i.e. either parent PhysicalEntity recursively
	 * or member PhysicalEntity recursively. Note that another member of the parent is not a generic
	 * equivalent.
	 * @return generative constraint to get the generic equivalents of the PhysicalEntity
	 */
	public static Constraint genericEquiv()
	{
		return new SelfOrThis(new MultiPathConstraint("PhysicalEntity/memberPhysicalEntity*",
			"PhysicalEntity/memberPhysicalEntityOf*"));
	}

	/**
	 * From Complex to its members recursively (also getting member of the inner complexes).
	 * @return generative constraint to get the members of the Complex
	 */
	public static Constraint complexMembers()
	{
		return new PathConstraint("Complex/component*");
	}

	/**
	 * From complex to its simple members (members which are of type SimplePhysicalEntity)
	 * recursively.
	 * @return generative constraint to get the simple members of the Complex recursively
	 */
	public static Constraint simpleMembers()
	{
		return new PathConstraint("Complex/component*:SimplePhysicalEntity");
	}

	/**
	 * From Complex to its members, or the complex itself.
	 * @return generative constraint to get the members of a complex and to itself
	 */
	public static Constraint withComplexMembers()
	{
		return new SelfOrThis(complexMembers());
	}

	/**
	 * From complex to its simple members recursively, or the Complex itself
	 * @return generative constraint to get the simple members of the Complex recursively, or the
	 * Complex itself
	 */
	public static Constraint withSimpleMembers()
	{
		return new SelfOrThis(simpleMembers());
	}

	/**
	 * From PhysicalEntity to its parent Complex recursively.
	 * @return generative constraint to get the parent Complex of the PhysicalEntity recursively
	 */
	public static Constraint complexes()
	{
		return new PathConstraint("PhysicalEntity/componentOf*");
	}

	/**
	 * From PhysicalEntity to parent Complex recursively, or to itself.
	 * @return generative constraint to get the parent Complex of PhysicalEntity recursively, ot to
	 * itself
	 */
	public static Constraint withComplexes()
	{
		return new SelfOrThis(complexes());
	}

	/**
	 * From Conversion to its left participants.
	 * @return generative constraint to get the left participants of the Conversion
	 */
	public static Constraint left()
	{
		return new PathConstraint("Conversion/left");
	}

	/**
	 * From Conversion to its right participants.
	 * @return generative constraint to get the right participants of the Conversion
	 */
	public static Constraint right()
	{
		return new PathConstraint("Conversion/right");
	}

	/**
	 * From PhysicalEntity to the Conversion that it participates.
	 * @return generative constraint to get the Conversions that the PhysicalEntity is a participant
	 * of
	 */
	public static Constraint participatesInConv()
	{
		return new PathConstraint("PhysicalEntity/participantOf:Conversion");
	}

	/**
	 * From PhysicalEntity to the Interaction that it participates.
	 * @return generative constraint to get the Interactions that the PhysicalEntity is a
	 * participant of
	 */
	public static Constraint participatesInInter()
	{
		return new PathConstraint("PhysicalEntity/participantOf");
	}

	/**
	 * From Complex or SimplePhysicalEntity to the related EntityReference. If Complex, then
	 * EntityReference of simple members (recursively) are related.
	 * @return generative constraint to get the related EntityReference of the Complex or
	 * SimplePhysicalEntity
	 */
	public static Constraint compToER()
	{
		return new MultiPathConstraint("Complex/component*:SimplePhysicalEntity/entityReference",
			"SimplePhysicalEntity/entityReference");
	}

	/**
	 * Filters Named to contain a specific name.
	 * @param name name to require
	 * @return constraint to check if the name exists in among names
	 */
	public static Constraint nameEquals(String name)
	{
		return new Field("Named/name", name);
	}

	/**
	 * Filters out ubiquitous elements.
	 * @param ubiques set of ubique IDs
	 * @return constraint to filter out ubique based on IDs
	 */
	public static Constraint notUbique(Set<String> ubiques)
	{
		return new NOT(new IDConstraint(ubiques));
	}

	/**
	 * Gets a constraint to ensure that ensures only one of the two PhysicalEntities has an
	 * activity. Size of this constraint is 2.
	 * @return constraint to make sure only one of the PhysicalEntity has an activity
	 */
	public static Constraint differentialActivity(boolean activating)
	{
		if (activating) return new AND(new MappedConst(new ActivityConstraint(true), 1), new MappedConst(new ActivityConstraint(false), 0));
		else return new AND(new MappedConst(new ActivityConstraint(true), 0), new MappedConst(new ActivityConstraint(false), 1));
	}

	/**
	 * From Interaction to its PhysicalEntity participants.
	 * @return generative constraint to get the participants of the Interaction
	 */
	public static Constraint participant()
	{
		return new PathConstraint("Interaction/participant:PhysicalEntity");
	}

	/**
	 * From Interaction to the related EntityReference of its participants.
	 * @return generative constraint to get the related PhysicalEntity of the participants of an
	 * Interaction
	 */
	public static Constraint participantER()
	{
		return new MultiPathConstraint("Interaction/participant:PhysicalEntity/entityReference",
			"Interaction/participant:PhysicalEntity/component*/entityReference");
	}

	/**
	 * From TemplateReaction to its products.
	 * @return generative constraint to get the products of the TemplateReaction
	 */
	public static Constraint product()
	{
		return new PathConstraint("TemplateReaction/product");
	}

	/**
	 * Makes sure the Interaction has no Control
	 * @return constraint to filter out Interactions with a Control
	 */
	public static Constraint notControlled()
	{
		return new Empty(new PathConstraint("Interaction/controlledOf"));
	}

	/**
	 * Makes sure the EntityReference or the PhysicalEntity belongs to human. Please note that this
	 * check depends on the display name of the related BioSource object to be "Homo sapiens". If
	 * that is not the case, the constraint won't work.
	 * @return constraint to make sure the EntityReference or the PhysicalEntity belongs to human
	 */
	public static Constraint isHuman()
	{
		return new OR(
			new MappedConst(new Field("SequenceEntityReference/organism/displayName",
				"Homo sapiens"), 0),
			new MappedConst(new Field("PhysicalEntity/entityReference/organism/displayName",
				"Homo sapiens"), 0));
	}

	/**
	 * From PhysicalEntity to the MolecularInteraction.
	 * @return generative constraint to get the MolecularInteraction the the PhysicalEntity is
	 * participant
	 */
	public static Constraint molecularInteraction()
	{
		return new PathConstraint("PhysicalEntity/participantOf:MolecularInteraction");
	}

	/**
	 * Makes sure that the second element (PhysicalEntity) is not a participant of the first element
	 * (Interaction).
	 * @return constraint to make sure that the PhysicalEntity in second index is not a participant
	 * of the Interaction in first index
	 */
	public static Constraint notAParticipant()
	{
		return new NOT(new Field("Interaction/participant", Field.USE_SECOND_ARG));
	}

	/**
	 * Makes sure the second element (Control) is not a controller to the first element
	 * (Interaction).
	 * @return constraint to filter out cases where the Control at the second index is controlling
	 * the Interaction at the first index.
	 */
	public static Constraint notControlsThis()
	{
		// Asserts the control (first variable), does not control the conversion (second variable)
		return new NOT(ConBox.controlToInter());
	}

	/**
	 * Makes sure a PhysicalEntity of any linked member PhysicalEntities toward members are not
	 * labeled as inactive.
	 * @return constraint to filter out PhysicalEntity labeled as inactive.
	 */
	public static Constraint notLabeledInactive()
	{
		// Asserts the PE (and lower equivalent) are not labeled inactive
		return new NOT(ConBox.modificationConstraint("residue modification, inactive"));
	}

	/**
	 * Makes sure that the PhysicalEntity or any linked PE contains the modification term. Size = 1.
	 * @param modifTerm term to check
	 * @return constraint to filter out PhysicalEntity not associated to a modification term
	 */
	public static Constraint modificationConstraint(String modifTerm)
	{
		return new FieldOfMultiple(new MappedConst(new LinkedPE(LinkedPE.Type.TO_MEMBER), 0),
			"PhysicalEntity/feature:ModificationFeature/modificationType/term", modifTerm);
	}
}
