package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.pattern.Constraint;

/**
 * Some predefined constraints.
 *
 * @author Ozgun Babur
 */
public class ConBox
{
	public static Constraint erToPE()
	{
		return new PathConstraint("EntityReference/entityReferenceOf");
	}

	public static Constraint peToER()
	{
		return new PathConstraint("SimplePhysicalEntity/entityReference");
	}

	public static Constraint downControl()
	{
		return new PathConstraint("PhysicalEntity/controllerOf");
	}

	public static Constraint controlled()
	{
		return new PathConstraint("Control/controlled");
	}

	public static Constraint controllerPE()
	{
		return new PathConstraint("Control/controller:PhysicalEntity");
	}

	public static Constraint controlToConv()
	{
		return new PathConstraint("Control/controlled*:Conversion");
	}

	public static Constraint convToControl()
	{
		return new PathConstraint("Conversion/controlledOf*");
	}

	public static Constraint controlsConv()
	{
		return new PathConstraint("PhysicalEntity/controllerOf/controlled*:Conversion");
	}

	public static Constraint genericEquiv()
	{
		return new SelfOrThis(new MultiPathConstraint("PhysicalEntity/memberPhysicalEntity*",
			"PhysicalEntity/memberPhysicalEntityOf*"));
	}

	public static Constraint complexMembers()
	{
		return new PathConstraint("Complex/component*");
	}

	public static Constraint simpleMembers()
	{
		return new PathConstraint("Complex/component*:SimplePhysicalEntity");
	}

	public static Constraint withComplexMembers()
	{
		return new SelfOrThis(complexMembers());
	}

	public static Constraint withSimpleMembers()
	{
		return new SelfOrThis(simpleMembers());
	}

	public static Constraint complexes()
	{
		return new PathConstraint("PhysicalEntity/componentOf*");
	}

	public static Constraint withComplexes()
	{
		return new SelfOrThis(complexes());
	}

	public static Constraint left()
	{
		return new PathConstraint("Conversion/left");
	}

	public static Constraint right()
	{
		return new PathConstraint("Conversion/right");
	}

	public static Constraint participatesInConv()
	{
		return new PathConstraint("PhysicalEntity/participantOf:Conversion");
	}

	public static Constraint compToER()
	{
		return new MultiPathConstraint("Complex/component*:SimplePhysicalEntity/entityReference",
			"SimplePhysicalEntity/entityReference");
	}
}
