package org.biopax.paxtools.io.sif.level3;

import org.biopax.paxtools.io.sif.InteractionSet;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.EntityReference;

/**
 */
public class InteractionSetL3 extends InteractionSet
{



    private GroupMap groupMap;

    public InteractionSetL3(Model model)
	{
		this.groupMap = Grouper.inferGroups(model);
	}

	public GroupMap getGroupMap()
	{
		return groupMap;
	}

    public void convertGroupsToInteractions()
    {
        for (Group group : this.groupMap.getMap().values())
        {
            for (EntityReference member : group.members)
            {
                this.add(new SimpleInteraction(groupMap.getEntityReferenceOrGroup(member),group,group.type,group.sources));
            }
            for (Group subgroup : group.subgroups)
            {
                this.add(new SimpleInteraction(subgroup,group,group.type,group.sources));
            }

        }
    }


}

