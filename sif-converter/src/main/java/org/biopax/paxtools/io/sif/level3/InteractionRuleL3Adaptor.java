package org.biopax.paxtools.io.sif.level3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 */
public abstract class InteractionRuleL3Adaptor implements InteractionRuleL3
{
	private final Log log = LogFactory.getLog(ParticipatesRule.class);

	/**
	 * An option for reaching to EntityReference of member PhysicalEntity of a PhysicalEntity.
	 */
	public static final String REACH_GENERIC_MEMBERS = "REACH_GENERIC_MEMBERS";


	public final void inferInteractions(Set<SimpleInteraction> interactionSet, Object entity,
		Model model, Map options)
	{
		if(entity instanceof EntityReference)
		{
			inferInteractions(interactionSet, ((EntityReference) entity),model,options);
		}
	}

	protected Set<EntityReference> collectEntityReferences(PhysicalEntity pe, Map options)
	{
		return collectEntityReferences(pe, null,options);
	}

	protected Set<EntityReference> collectEntityReferences(PhysicalEntity pe,
		Set<EntityReference> enSet, Map options)
	{
		if (enSet == null)
		{
			enSet = new HashSet<EntityReference>();
		}

		if (pe instanceof SimplePhysicalEntity)
		{
			EntityReference er = ((SimplePhysicalEntity) pe).getEntityReference();
			if (er != null) enSet.add(er);
			else if (log.isWarnEnabled()) log.warn("SimplePhysicalEntity with ID " +
				pe.getRDFId() + " has NULL EntityReference");
		}
		else if (pe instanceof Complex)
		{
			for (PhysicalEntity mem : ((Complex) pe).getComponent())
			{
				collectEntityReferences(mem, enSet, options);
			}
		}

		if (options.containsKey(REACH_GENERIC_MEMBERS))
		{
			for (PhysicalEntity mem : pe.getMemberPhysicalEntity())
			{
				collectEntityReferences(mem, enSet, options);
			}
		}
		return enSet;
	}

	protected Set<EntityReference> collectEntityReferences(Set<PhysicalEntity> pes,
		Set<EntityReference> enSet, Map options)
	{
		if (enSet == null)
		{
			enSet = new HashSet<EntityReference>();
		}

		for (PhysicalEntity pe : pes)
		{
			collectEntityReferences(pe, enSet, options);
		}
		return enSet;
	}
}
