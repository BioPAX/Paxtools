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
 * Base class for level 3 interactions rules.
 */
public abstract class InteractionRuleL3Adaptor implements InteractionRuleL3
{
	/**
	 * Log for logging.
	 */
	private final Log log = LogFactory.getLog(ParticipatesRule.class);

	/**
	 * Selects only PhysicalEntity as seed.
	 * @param interactionSet inferred interactions
	 * @param entity this must be a PhysicalEntity for L3
	 * @param model BioPAX model
	 */
	public final void inferInteractions(InteractionSet interactionSet, BioPAXElement entity,
		Model model)
	{
		if (entity instanceof PhysicalEntity)
		{
			inferInteractionsFromPE((InteractionSetL3) interactionSet, ((PhysicalEntity) entity),
				model);
		}
		else
		{
			if (log.isInfoEnabled()) log.info("Not a PE Skipping." + entity.getRDFId());
		}
	}

	/**
	 * Initializes the options.
	 * @param options options map
	 */
	public void initOptions(Map options)
	{
		if (options == null)
		{
			options = new HashMap();
		}
		initOptionsNotNull(options);
	}

	/**
	 * Does nothing for this class. Initializes the options where this is overwritten.
	 * @param options options map
	 */
	protected void initOptionsNotNull(Map options)
	{
	}

	/**
	 * Checks if the options map has the given option with the given value.
	 * @param key key of the option
	 * @param value value of the option
	 * @param options options map
	 * @return true if the options map has the given option with the given value
	 */
    protected boolean checkOption(Object key, Object value, Map options)
    {
        return options.containsKey(key)&&options.get(key).equals(value);
    }

	/**
	 *
	 * @param pes
	 * @param set
	 * @return
	 */
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

	/**
	 * Gets members of the Group recursively traversing sub-Groups.
	 * @param entities member set
	 * @param group group to query
	 */
	private void getMembersRecursively(Set<BioPAXElement> entities, Group group)
	{
		entities.addAll(group.members);
		for (Group subgroup : group.subgroups)
		{
			getMembersRecursively(entities, subgroup);
		}
	}

	/**
	 * Creates an interactions between every ordered pair of components.
	 * @param interactionSet inferred interactions
	 * @param components nodes of the clique
	 * @param type interaction type
	 * @param mediators mediator elements of te interaction
	 */
	protected void createClique(InteractionSetL3 interactionSet, List<BioPAXElement> components,
			BinaryInteractionType type, BioPAXElement... mediators)
	{
        GroupMap groupMap = interactionSet.getGroupMap();

		for (int j = 0; j < components.size(); j++)
		{
			for (int i = 0; i < j; i++) {
                createAndAdd(
                        groupMap.getEntityReferenceOrGroup(components.get(i)),
                        groupMap.getEntityReferenceOrGroup(components.get(j)), interactionSet,
                        type,
                        mediators);
            }
        }
	}

	/**
	 * Creates the binary interaction and adds to the inferred interaction list.
	 * @param source source of the interaction
	 * @param target target of the interaction
	 * @param is3 inferred interactions
	 * @param type interaction type
	 * @param mediators mediators of the interaction
	 */
    protected void createAndAdd(
            BioPAXElement source,
            BioPAXElement target,
            InteractionSetL3 is3,
            BinaryInteractionType type,
            BioPAXElement... mediators)
    {
        if(source!=null && target!=null && !source.equals(target))
        {
            SimpleInteraction sc = new SimpleInteraction(source, target, type);
            for (BioPAXElement mediator : mediators) {
                sc.addMediator(mediator);
            }
            is3.add(sc);
        }
    }
}
