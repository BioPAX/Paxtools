package org.biopax.paxtools.io.sif.level3;

import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.io.sif.InteractionSet;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.EntityReference;

/**
 * Set of inferred interactions. This set handles grouping operations.
 */
public class InteractionSetL3 extends InteractionSet
{
	/**
	 * A map for the groups in the model.
	 */
    private GroupMap groupMap;

	/**
	 * Constructor with the model.
	 * @param model model to mine binary interactions
	 */
    public InteractionSetL3(Model model)
	{
		this.groupMap = Grouper.inferGroups(model);
	}

	/**
	 * Getter for the groupMap
	 * @return groupMap
	 */
	public GroupMap getGroupMap()
	{
		return groupMap;
	}

	/**
	 * Creates membership links between group nodes and members in the generated SIF graph.
	 */
    public void convertGroupsToInteractions()
    {
        for (Group group : this.groupMap.getMap().values())
        {
            for (EntityReference member : group.members)
            {
                this.add(new SimpleInteraction(groupMap.getEntityReferenceOrGroup(member),group,group.isComplex?
		                BinaryInteractionType.COMPONENT_OF:BinaryInteractionType.GENERIC_OF,
                                               group.sources));
            }
            for (Group subgroup : group.subgroups)
            {
                this.add(new SimpleInteraction(subgroup,group,group.isComplex?BinaryInteractionType
		                .COMPONENT_OF:BinaryInteractionType.GENERIC_OF,
                                               group.sources));
            }
        }
    }

	/**
	 * This method iteratively replaces groups with BioPAX elements
	 */
	public void expandGroups()
	{
		for (SimpleInteraction simpleInteraction : this)
		{

		}
	}


}

