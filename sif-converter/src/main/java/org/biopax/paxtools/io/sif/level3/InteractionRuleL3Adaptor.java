package org.biopax.paxtools.io.sif.level3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.io.sif.InteractionSet;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;

import java.util.*;

/**
 */
public abstract class InteractionRuleL3Adaptor implements InteractionRuleL3
{
	private final Log log = LogFactory.getLog(ParticipatesRule.class);

	public final void inferInteractions(InteractionSet interactionSet, BioPAXElement entity, Model model)
	{
		if (entity instanceof PhysicalEntity)
		{
			inferInteractionsFromPE((InteractionSetL3) interactionSet, ((PhysicalEntity) entity), model);
		} else
		{
			if (log.isInfoEnabled()) log.info("Not a PE Skipping." + entity.getRDFId());
		}
	}

    public void initOptions(Map options)
    {
	   if(options==null)
		{
			options = new HashMap();
		}
		initOptionsNotNull(options);
    }

	protected void initOptionsNotNull(Map options)
	{

	}

	protected BioPAXElement getEntityReferenceOrGroup(PhysicalEntity pe, InteractionSetL3 set)
	{

		BioPAXElement bpe = set.getElementToGroupMap().get(pe);
		if(bpe == null && pe instanceof SimplePhysicalEntity)
		{
			EntityReference er = ((SimplePhysicalEntity) pe).getEntityReference();
			if (er != null)
			{
				bpe = set.getElementToGroupMap().get(er);
				if (bpe == null)
				bpe = er;
			} else
			{
				if (log.isWarnEnabled())
					log.warn("SimplePhysicalEntity with ID " + pe.getRDFId() + " has NULL EntityReference");
			}
		}
		return bpe;
	}

	protected Set<BioPAXElement> collectEntities(Set<PhysicalEntity> pes, InteractionSetL3 set)
	{
		Set<BioPAXElement> entities = new HashSet<BioPAXElement>();
		for (PhysicalEntity pe : pes)
		{
			BioPAXElement entity = this.getEntityReferenceOrGroup(pe, set);
			if (entity != null) entities.add(entity);
			if (entity instanceof Group)
			{
				getMembersRecursively(entities, (Group) entity);
			}
		}
		return entities;
	}

	private void getMembersRecursively(Set<BioPAXElement> entities, Group group)
	{
		entities.addAll(group.members);
		for (Group subgroup : group.subgroups)
		{
			getMembersRecursively(entities, subgroup);
		}
	}


	protected void createClique(InteractionSet interactionSet, List<BioPAXElement> components,
			BinaryInteractionType type, BioPAXElement... mediators)
	{

		for (int j = 0; j < components.size(); j++)
		{
			for (int i = 0; i < j; i++)
			{
				SimpleInteraction interaction =
						new SimpleInteraction(components.get(i), components.get(j), type, mediators);
				interactionSet.add(interaction);
			}
		}
	}
}
