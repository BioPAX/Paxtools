package org.biopax.paxtools.io.sif.level3;

import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.biopax.paxtools.io.sif.BinaryInteractionType.IN_SAME_COMPONENT;

/**
 * Component.InSame: A and B are components of same flattened complex structure,
 * A and B are simple. Component.Of: A is component of B, B is complex, A may be
 * nested multiple levels in B.
 *
 * @author Ozgun Babur
 */
public class ComponentRule implements InteractionRuleL3
{
	public void inferInteractions(Set<SimpleInteraction> interactionSet, Object AObj,
		Model model, Map options)
	{
		inferInteractions(interactionSet, (EntityReference) AObj, model, options);
	}

	public void inferInteractions(Set<SimpleInteraction> interactionSet, EntityReference A,
		Model model, Map options)
	{
		// Iterate all PEs of A and process that goes into a complex
		for (SimplePhysicalEntity pe : A.getEntityReferenceOf())
		{
			if (!pe.getComponentOf().isEmpty())
			{
				for (Complex cmp : pe.getComponentOf())
				{
					processComplex(interactionSet, A, cmp, options);
				}
			}
		}
	}

	/**
	 * This method is called for each complex that A is in, regardless of the level
	 * of nesting. If it is also detected that this complex is the most outer
	 * complex, then another recursive search is initiated for mining
	 * Component.InSame rule.
	 *
	 * @param interactionSet interaction repository
	 * @param A			  first physical entity
	 * @param options		options map
	 * @param comp		   complex being processed
	 */
	private void processComplex(Set<SimpleInteraction> interactionSet, EntityReference A,
		Complex comp, Map options)
	{
		// Flag for detecting if this complex is most outer one.
		boolean mostOuter = true;

		// Iterate all PEPs of complex and process that goes into a complex
		for (Complex outer : comp.getComponentOf())
		{
			mostOuter = false;
			processComplex(interactionSet, A, outer, options);
		}

		// Search towards other members only if this is the most outer complex
		// and if options let for sure
		if (mostOuter && (!options.containsKey(IN_SAME_COMPONENT) ||
			options.get(IN_SAME_COMPONENT).equals(Boolean.TRUE)))
		{
			// Iterate other members for components_of_same_complex rule
			for (EntityReference B : comp.getMemberReferences())
			{
				if (B!=null && !B.equals(A))
				{
					SimpleInteraction si = new SimpleInteraction(A, B, IN_SAME_COMPONENT);
					si.addMediator(comp);
					interactionSet.add(si);
				}
			}
		}
	}

	public List<BinaryInteractionType> getRuleTypes()
	{
		return Arrays.asList(IN_SAME_COMPONENT);
	}
}
