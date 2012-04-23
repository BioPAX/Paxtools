package org.biopax.paxtools.causality.pattern;

import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.PhysicalEntity;

/**
 * @author Ozgun Babur
 */
public enum ConstraintBox
{
	ER_TO_PE(new PathConstraint("EntityReference/entityReferenceOf")),
	CONTROLS(new PathConstraint("PhysicalEntity/controllerOf")),
	CONTROLLED(new PathConstraint("Control/controlled")),
	GENERIC_EQUIV(new SelfOrThisConstraint(new MultiPathConstraint("PhysicalEntity/memberPhysicalEntity*", "PhysicalEntity/memberPhysicalEntityOf*"))),
	WITH_COMPLEX_MEMBERS(new SelfOrThisConstraint(new PathConstraint("Complex/component*"))),
	WITH_COMPLEXES(new SelfOrThisConstraint(new PathConstraint("PhysicalEntity/componentOf*")))
	;
	
	private Constraint con;

	private ConstraintBox(Constraint con)
	{
		this.con = con;
	}

	public Constraint con()
	{
		return con;
	}
}
