package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.Match;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This constraint is used to collect related EntityReference of the controllers of a Control.
 *
 * Var0 - Control
 * Var1 - ER of a controller PE
 *
 * @author Ozgun Babur
 */
public class ControlToControllerER extends ConstraintAdapter
{
	/**
	 * For navigating from Control to any PhysicalEntity controllers.
	 */
	static final PathAccessor TO_CONTROLLER = new PathAccessor("Control/controller*:PhysicalEntity");

	/**
	 * For getting ERs of PEs, also traversing generic-member relations.
	 */
	static final PathAccessor TO_SUB_ERS = new PathAccessor(
		"SimplePhysicalEntity/memberPhysicalEntity*:SimplePhysicalEntity/entityReference");

	/**
	 * Constructor.
	 */
	public ControlToControllerER()
	{
		super(2);
	}

	/**
	 * This is a generative constraint.
	 * @return true
	 */
	@Override
	public boolean canGenerate()
	{
		return true;
	}

	/**
	 * Navigates to the related ERs of the controllers of the given Control.
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return related ERs
	 */
	@Override
	public Collection<BioPAXElement> generate(Match match, int... ind)
	{
		Control ctrl = (Control) match.get(ind[0]);
		return new HashSet<>(getRelatedERs(ctrl));
	}

	public Set<EntityReference> getRelatedERs(Control ctrl)
	{
		Set<EntityReference> ers = new HashSet<>();

		for (Object o : TO_CONTROLLER.getValueFromBean(ctrl))
		{
			if (o instanceof PhysicalEntity) ers.addAll(getRelatedERs((PhysicalEntity) o));
		}

		return ers;
	}

	public Set<EntityReference> getRelatedERs(PhysicalEntity pe)
	{
		Set<EntityReference> ers = new HashSet<>();

		if (pe instanceof Complex)
		{
			ers.addAll(((Complex) pe).getMemberReferences());
		}
		else if (pe instanceof SimplePhysicalEntity)
		{
			EntityReference er = ((SimplePhysicalEntity) pe).getEntityReference();
			if (er != null) ers.add(er);

			for (Object o : TO_SUB_ERS.getValueFromBean(pe))
			{
				if (o instanceof EntityReference) ers.add((EntityReference) o);
			}
		}

		return ers;
	}
}
