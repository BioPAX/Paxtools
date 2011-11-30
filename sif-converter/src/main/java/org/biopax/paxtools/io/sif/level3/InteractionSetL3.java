package org.biopax.paxtools.io.sif.level3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.io.sif.InteractionSet;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;

import java.util.Map;

/**
 */
public class InteractionSetL3 extends InteractionSet
{

	private Map<BioPAXElement, Group> element2GroupMap;

	private Log log= LogFactory.getLog(InteractionSetL3.class);

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
				this.add(new SimpleInteraction(getEntityReferenceOrGroup(member),group,group.type,group.sources));
			}
			for (Group subgroup : group.subgroups)
			{
				this.add(new SimpleInteraction(subgroup,group,group.type,group.sources));
			}

		}
	}


	protected BioPAXElement getEntityReferenceOrGroup(BioPAXElement bpe)
	{

		BioPAXElement entity = getElementToGroupMap().get(bpe);
		if (entity == null)
		{
			if (bpe instanceof EntityReference)
			{
				entity = bpe;
			} else if (bpe instanceof SimplePhysicalEntity)
			{
				EntityReference er = ((SimplePhysicalEntity) bpe).getEntityReference();
				if (er != null)
				{
					entity = getElementToGroupMap().get(er);
					if (entity == null) entity = er;
				} else
				{
					if (log.isWarnEnabled())
						log.warn("SimplePhysicalEntity with ID " + bpe.getRDFId() + " has NULL EntityReference");
				}
			}
		}
		return entity;
	}
}

