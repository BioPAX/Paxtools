package org.biopax.paxtools.io.sif.level3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.io.sif.InteractionSet;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.PhysicalEntity;

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
		if (options == null)
		{
			options = new HashMap();
		}
		initOptionsNotNull(options);
	}

	protected void initOptionsNotNull(Map options)
	{

	}

    protected boolean checkOption(Object key, Object value, Map options)
    {
        return options.containsKey(key)&&options.get(key).equals(value);
    }

	protected Set<BioPAXElement> collectEntities(Set<PhysicalEntity> pes, InteractionSetL3 set)
	{
		Set<BioPAXElement> entities = new HashSet<BioPAXElement>();
		for (PhysicalEntity pe : pes)
		{
			BioPAXElement entity = set.getGroupMap().getEntityReferenceOrGroup(pe);
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


	protected void createClique(InteractionSetL3 interactionSet, List<BioPAXElement> components,
			BinaryInteractionType type, BioPAXElement... mediators)
	{
        GroupMap groupMap = interactionSet.getGroupMap();

		for (int j = 0; j < components.size(); j++)
		{
			for (int i = 0; i < j; i++)
			{
                SimpleInteraction interaction =
						new SimpleInteraction(groupMap.getEntityReferenceOrGroup(components.get(i)),
						                      groupMap.getEntityReferenceOrGroup(components.get(j)),
						                      type,
						                      mediators);
				interactionSet.add(interaction);
			}
		}
	}
}
