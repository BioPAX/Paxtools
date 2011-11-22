package org.biopax.paxtools.io.sif.level3;

import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.io.sif.SimpleInteraction;
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
 * @author Emek Demir
 */
public class ComponentRule extends InteractionRuleL3Adaptor
{
	private static List<BinaryInteractionType> binaryInteractionTypes = Arrays.asList(IN_SAME_COMPONENT);

	private boolean inSameComponent;

	private boolean componentOf;

	public void inferInteractionsFromPE(InteractionSetL3 interactionSet, PhysicalEntity pe, Model model)
	{
		if (pe instanceof Complex)
		{
			Group group = interactionSet.getElementToGroupMap().get(pe);
			if (group != null)
			{
				Set<EntityReference> members = group.members;
				Set<Group> subGroups = group.subgroups;
				if (inSameComponent)
				{

					ArrayList<BioPAXElement> components =
							new ArrayList<BioPAXElement>(members.size() + subGroups.size());
					components.addAll(members);
					components.addAll(subGroups);



					BioPAXElement[] sources = group.sources.toArray(
							(BioPAXElement[]) Array.newInstance(BioPAXElement.class, group.sources.size()));
					createClique(interactionSet, components, BinaryInteractionType.IN_SAME_COMPONENT, sources);
				}
				if (componentOf)
				{
					for (EntityReference member : members)
					{
						addComponent(member, group, interactionSet);
					}

					for (Group subGroup : subGroups)
					{
						if (subGroup.getType() == BinaryInteractionType.COMPONENT_OF)
						{
							addComponent(subGroup, group, interactionSet);
						}
					}
				}
			}
		}
	}

	private void addComponent(BioPAXElement component, Group group, InteractionSetL3 interactionSet)
	{
		interactionSet.add(new SimpleInteraction(component, group, BinaryInteractionType.COMPONENT_OF));
	}


	@Override public void initOptionsNotNull(Map options)
	{
		super.initOptionsNotNull(options);
		inSameComponent = options.containsKey(IN_SAME_COMPONENT) && (!options.get(IN_SAME_COMPONENT).equals(true));
		componentOf = options.containsKey(COMPONENT_OF) && (!options.get(COMPONENT_OF).equals(false));
	}

	public List<BinaryInteractionType> getRuleTypes()
	{
		return binaryInteractionTypes;
	}
}
