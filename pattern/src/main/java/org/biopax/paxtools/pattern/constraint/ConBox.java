package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.pattern.Constraint;
import org.biopax.paxtools.pattern.MappedConst;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.biopax.paxtools.pattern.util.RelType;

import java.util.Arrays;
import java.util.HashSet;

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
	 * From PhysicalEntity to the downstream Conversion.
	 * @return generative constraint to get the Conversion that the PhysicalEntity is a controller
	 */
	public static Constraint peToControlledConv()
	{
		return new PathConstraint("PhysicalEntity/controllerOf/controlled*:Conversion");
	}

	/**
	 * From PhysicalEntity to the downstream Interaction.
	 * @return generative constraint to get the Interaction that the PhysicalEntity is a controller
	 */
	public static Constraint peToControlledInter()
	{
		return new PathConstraint("PhysicalEntity/controllerOf/controlled*:Interaction");
	}

	/**
	 * From PhysicalEntity to the related Interaction.
	 * @return generative constraint to get the Interaction that the PhysicalEntity is a controller
	 */
	public static Constraint peToInter()
	{
		return new OR(
			new MappedConst(peToControlledInter(), 0, 1),
			new MappedConst(participatesInInter(), 0, 1));
	}

	/**
	 * From simple PhysicalEntity to related Conversion. The relation can be through complexes and
	 * generics.
	 *
	 * @param type relationship type
	 * @return generative constraint
	 */
	public static Constraint simplePEToConv(RelType type)
	{
		return new ConstraintChain(linkToComplex(), new ParticipatesInConv(type));
	}

	/**
	 * From Interaction to the related PhysicalEntity.
	 * @return the constraint
	 */
	public static Constraint interToPE()
	{
		return new OR(
			new MappedConst(interToController(), 0, 1),
			new MappedConst(participant(), 0, 1));
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
	 * From Conversion to the controller of the upstream Control (and its upstream Control
	 * recursively).
	 * @return the constraint
	 */
	public static Constraint convToController()
	{
		return new PathConstraint("Conversion/controlledOf*/controller");
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
	 * Starts from an Interaction and gets next Interactions in temporal order, if ever defined in
	 * a Pathway.
	 * @return generative constraint
	 */
	public static Constraint nextInteraction()
	{
		return new PathConstraint("Interaction/stepProcessOf/nextStep/stepProcess:Interaction");
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
	 * From Interaction to the controlling Controls recursively, and their controller PEs.
	 * @return the constraint
	 */
	public static Constraint interToController()
	{
		return new PathConstraint("Interaction/controlledOf*:Control/controller:PhysicalEntity");
	}

	/**
	 * From Complex or SimplePhysicalEntity to the related EntityReference. If Complex, then
	 * EntityReference of simple members (recursively) are related.
	 * @return generative constraint to get the related EntityReference of the Complex or
	 * SimplePhysicalEntity
	 */
	public static Constraint compToER()
	{
		return new PathConstraint("Complex/component*:SimplePhysicalEntity/entityReference");
	}

	/**
	 * Filters Named to contain a specific name.
	 * @param name name to require
	 * @return constraint to check if the name exists in among names
	 */
	public static Constraint nameEquals(String name)
	{
		return new Field("Named/name", Field.Operation.INTERSECT, name);
	}

	/**
	 * Filters Named to contain a name from the input set.
	 * @param name name to require
	 * @return constraint
	 */
	public static Constraint nameEquals(String... name)
	{
		return new Field("Named/name", Field.Operation.INTERSECT,
			new HashSet<>(Arrays.asList(name)));
	}

//	/**
//	 * Filters out ubiquitous elements.
//	 * @param ubiques set of ubique IDs
//	 * @return constraint to filter out ubique based on IDs
//	 */
//	public static Constraint notUbique(Set<String> ubiques)
//	{
//		return new NOT(new IDConstraint(ubiques));
//	}

	/**
	 * Gets a constraint to ensure that ensures only one of the two PhysicalEntities has an
	 * activity. Size of this constraint is 2.
	 *
	 * @param activating true/false (TODO explain)
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
		return new ConstraintChain(participant(), linkToSpecific(), peToER(), linkedER(false));
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
				Field.Operation.INTERSECT, "Homo sapiens"), 0),
			new MappedConst(new Field("PhysicalEntity/entityReference/organism/displayName",
				Field.Operation.INTERSECT, "Homo sapiens"), 0));
	}

	/**
	 * Makes sure that the object has an Xref with the given ID. This id is not an RDF ID, it is
	 * the value of the Xref, like gene symbol.
	 * @param xrefID xref id
	 * @return constraint
	 */
	public static Constraint hasXref(String xrefID)
	{
		return new Field("XReferrable/xref/id", Field.Operation.INTERSECT, xrefID);
	}

	/**
	 * Makes sure that the object is associated with a Provenance with the given name. This id is not an RDF ID, it is
	 * the name of the resource, like TRANSFAC.
	 * @param name name of the provenance
	 * @return constraint
	 */
	public static Constraint hasProvenance(String name)
	{
		return new Field("Entity/dataSource/name", Field.Operation.INTERSECT, name);
	}

	/**
	 * Makes sure that the second element (PhysicalEntity) is not a participant of the first element
	 * (Interaction).
	 * @return constraint to make sure that the PhysicalEntity in second index is not a participant
	 * of the Interaction in first index
	 */
	public static Constraint notAParticipant()
	{
		return new NOT(new Field("Interaction/participant", Field.Operation.INTERSECT,
			Field.USE_SECOND_ARG));
	}

	/**
	 * Makes sure that the given physical entity is not related to the entity reference.
	 * @return the constraint
	 * todo: method not tested
	 */
	public static Constraint peNotRelatedToER()
	{
		return new NOT(new ConstraintChain(linkToSpecific(), peToER()));
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
		return new FieldOfMultiple(new MappedConst(new LinkedPE(LinkedPE.Type.TO_SPECIFIC), 0),
			"PhysicalEntity/feature:ModificationFeature/modificationType/term",
			Field.Operation.INTERSECT, modifTerm);
	}

	/**
	 * Makes a linker constraint from PhysicalEntity to its linked PhysicalEntity towards member
	 * direction.
	 * @return the constraint
	 */
	public static Constraint linkToSpecific()
	{
		return new LinkedPE(LinkedPE.Type.TO_SPECIFIC);
	}

	/**
	 * Makes a linker constraint from PhysicalEntity to its linked PhysicalEntity towards complex
	 * direction.
	 * @return the constraint
	 */
	public static Constraint linkToComplex()
	{
		return new LinkedPE(LinkedPE.Type.TO_GENERAL);
	}

	public static Constraint linkedER(boolean up)
	{
		return new SelfOrThis(new PathConstraint("EntityReference/memberEntityReference" + (up ? "Of" : "") + "*"));
	}

	/**
	 * Makes a linker constraint from PhysicalEntity to its linked PhysicalEntity towards member
	 * direction.
	 * @param blacklist used to detect ubiquitous small molecules
	 * @return the constraint
	 */
	public static Constraint linkToSimple(Blacklist blacklist)
	{
		return new LinkedPE(LinkedPE.Type.TO_SPECIFIC, blacklist);
	}

	/**
	 * Makes a linker constraint from PhysicalEntity to its linked PhysicalEntity towards complex
	 * direction.
	 * @param blacklist used to detect ubiquitous small molecules
	 * @return the constraint
	 */
	public static Constraint linkToComplex(Blacklist blacklist)
	{
		return new LinkedPE(LinkedPE.Type.TO_GENERAL, blacklist);
	}

	/**
	 * Makes a linker constraint from PhysicalEntity to its linked PhysicalEntity towards complex
	 * direction.
	 *
	 * @param equal true/false (TODO explain)
	 * @return the constraint
	 */
	public static Constraint equal(boolean equal)
	{
		return new Equality(equal);
	}

	/**
	 * Creates an element type constraint.
	 *
	 * @param clazz a BioPAX type, i.e., corresponding interface class
	 * @return the constraint
	 */
	public static Constraint type(Class<? extends BioPAXElement> clazz)
	{
		return new Type(clazz);
	}

	/**
	 * Makes sure that the PhysicalEntity do not have member physical entities..
	 * @return the constraint
	 */
	public static Constraint notGeneric()
	{
		return new Empty(new PathConstraint("PhysicalEntity/memberPhysicalEntity"));
	}

	/**
	 * Makes sure the participant degree (number of Conversions that this is a participant) of the
	 * PhysicalEntity is less than or equal to the parameter.
	 *
	 * @param limit max degree limit
	 * @return the constraint
	 */
	public static Constraint maxDegree(int limit)
	{
		return new Size(new PathConstraint("PhysicalEntity/participantOf:Conversion"), limit,
			Size.Type.LESS_OR_EQUAL);
	}

	public static Constraint source(String dbname)
	{
		return new Field("Entity/dataSource/displayName", Field.Operation.INTERSECT, dbname);
	}

	/**
	 * Makes sure that the two interactions are members of the same pathway.
	 * @return non-generative constraint
	 */
	public static Constraint inSamePathway()
	{
		String s1 = "Interaction/stepProcessOf/pathwayOrderOf";
		String s2 = "Interaction/pathwayComponentOf";
		return new OR(new MappedConst(new Field(s1, s1, Field.Operation.INTERSECT), 0, 1),
			new MappedConst(new Field(s2, s2, Field.Operation.INTERSECT), 0, 1));
	}

	/**
	 * Makes sure that the PhysicalEntity is controlling more reactions than it participates
	 * (excluding complex assembly).
	 * @return non-generative constraint
	 */
	public static Constraint moreControllerThanParticipant()
	{
		return new ConstraintAdapter(1)
		{
			PathAccessor partConv = new PathAccessor("PhysicalEntity/participantOf:Conversion");
			PathAccessor partCompAss = new PathAccessor("PhysicalEntity/participantOf:ComplexAssembly");
			PathAccessor effects = new PathAccessor("PhysicalEntity/controllerOf/controlled*:Conversion");

			@Override
			public boolean satisfies(Match match, int... ind)
			{
				PhysicalEntity pe = (PhysicalEntity) match.get(ind[0]);

				int partCnvCnt = partConv.getValueFromBean(pe).size();
				int partCACnt = partCompAss.getValueFromBean(pe).size();
				int effCnt = effects.getValueFromBean(pe).size();

				return (partCnvCnt - partCACnt) <= effCnt;
			}
		};
	}

	/**
	 * Checks if two physical entities have non-empty and different compartments.
	 * @return the constraint
	 */
	public static Constraint hasDifferentCompartments()
	{
		String acStr = "PhysicalEntity/cellularLocation/term";
		return new Field(acStr, acStr, Field.Operation.NOT_EMPTY_AND_NOT_INTERSECT);
	}

	/**
	 * Checks if the molecule is a prey of a Y2H experiment.
	 * @return the constraint
	 */
	public static Constraint isPrey()
	{
		return new Field("PhysicalEntity/evidence/experimentalForm/experimentalFormDescription/term",
			Field.Operation.INTERSECT, "prey");
	}

	/**
	 * Checks if the molecule is a bait of a Y2H experiment.
	 * @return the constraint
	 */
	public static Constraint isBait()
	{
		return new Field("PhysicalEntity/evidence/experimentalForm/experimentalFormDescription/term",
			Field.Operation.INTERSECT, "bait");
	}
}
