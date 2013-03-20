package org.biopax.paxtools.io.sif.level3;

import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.PhysicalEntity;

import java.lang.reflect.Array;
import java.util.*;

import static org.biopax.paxtools.io.sif.BinaryInteractionType.COMPONENT_OF;
import static org.biopax.paxtools.io.sif.BinaryInteractionType.IN_SAME_COMPONENT;

/**
 * This rule class mines complex membership relations. It mines two types of relations.
 * COMPONENT_OF: A is a member of the Complex B, membership relation can be multi step.
 * IN_SAME_COMPONENT: A and B are members of the same complex, and this same complex can be in
 * different level of nesting.
 * @author Emek Demir
 */
public class ComponentRule extends InteractionRuleL3Adaptor
{
	/**
	 * List of interaction types mined.
	 */
	private static List<BinaryInteractionType> binaryInteractionTypes =
		Arrays.asList(IN_SAME_COMPONENT);

	/**
	 * Option to mine IN_SAME_COMPONENT.
	 */
	private boolean inSameComponent;

	/**
	 * Option to mine COMPONENT_OF.
	 */
	private boolean componentOf;

	/**
	 * Infer interactions where A = the given PhysicalEntity.
	 * @param interactionSet to be populated
	 * @param pe PhysicalEntity that will be the seed of the inference
	 * @param model BioPAX model
	 */
	public void inferInteractionsFromPE(InteractionSetL3 interactionSet, PhysicalEntity pe,
		Model model)
	{
		if (pe instanceof Complex)
		{
			Group group = interactionSet.getGroupMap().getMap().get(pe);
			if (group != null)
			{
				Set<EntityReference> members = group.members;
				Set<Group> subGroups = group.subgroups;
				ArrayList<BioPAXElement> components = new ArrayList<BioPAXElement>(members.size() +
					subGroups.size());
				components.addAll(members);
				components.addAll(subGroups);

				if (inSameComponent)
				{
					BioPAXElement[] sources = group.sources.toArray((BioPAXElement[])
						Array.newInstance(BioPAXElement.class, group.sources.size()));

					createClique(interactionSet, components,
						BinaryInteractionType.IN_SAME_COMPONENT, sources);
				}
				if (componentOf)
				{
					for (BioPAXElement component : components)
					{

						addComponent(component, group, interactionSet);
					}
				}
			}
		}
	}

	/**
	 * Adds the component to the group.
	 * @param component component to add
	 * @param group group to add to
	 * @param interactionSet current set of inferred interactions
	 */
	private void addComponent(BioPAXElement component, Group group, InteractionSetL3 interactionSet)
	{
		createAndAdd(interactionSet.getGroupMap().getEntityReferenceOrGroup(component),
			group,interactionSet, BinaryInteractionType.COMPONENT_OF);
	}

	/**
	 * Initializes options.
	 * @param options options map
	 */
	@Override public void initOptionsNotNull(Map options)
	{
		inSameComponent = !checkOption(IN_SAME_COMPONENT,Boolean.FALSE,options);
		componentOf = !checkOption(COMPONENT_OF,Boolean.FALSE,options);
	}

	/**
	 * Gets supported interaction types.
	 * @return interaction types supported
	 */
	public List<BinaryInteractionType> getRuleTypes()
	{
		return binaryInteractionTypes;
	}
}
