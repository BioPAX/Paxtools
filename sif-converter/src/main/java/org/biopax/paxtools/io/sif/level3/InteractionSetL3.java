package org.biopax.paxtools.io.sif.level3;

import org.biopax.paxtools.io.sif.InteractionSet;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.EntityReference;

import java.util.Map;

/**
 */
public class InteractionSetL3 extends InteractionSet
{

	private Map<BioPAXElement, Group> element2GroupMap;

	public InteractionSetL3(Model model)
	{
		this.element2GroupMap = Grouper.inferGroups(model);
	}

	public Map<BioPAXElement, Group> getElementToGroupMap()
	{
		return element2GroupMap;
	}

	public void convertGroupsToInteractions()
	{
		for (Group group : this.getElementToGroupMap().values())
		{
			for (EntityReference member : group.members)
			{
				this.add(new SimpleInteraction(member,group,group.type,group.sources));
			}
			for (Group subgroup : group.subgroups)
			{
				this.add(new SimpleInteraction(subgroup,group,group.type,group.sources));
			}

		}
	}


}

