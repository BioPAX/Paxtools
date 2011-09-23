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

	public final void inferInteractions(Set<SimpleInteraction> interactionSet, Object entity,
		Model model, Map options)
	{
		if(entity instanceof EntityReference)
		{
			inferInteractions(interactionSet, ((EntityReference) entity),model,options);
		}
	}

	protected Set<EntityReference> collectEntityReferences(PhysicalEntity pe)
	{
		return collectEntityReferences(pe, null);
	}

	protected Set<EntityReference> collectEntityReferences(PhysicalEntity pe,
		Set<EntityReference> enSet)
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
				collectEntityReferences(mem, enSet);
			}
		}

		for (PhysicalEntity mem : pe.getMemberPhysicalEntity())
		{
			collectEntityReferences(mem, enSet);
		}
		return enSet;
	}

	protected Set<EntityReference> collectEntityReferences(Set<PhysicalEntity> pes,
		Set<EntityReference> enSet)
	{
		if (enSet == null)
		{
			enSet = new HashSet<EntityReference>();
		}

		for (PhysicalEntity pe : pes)
		{
			collectEntityReferences(pe, enSet);
		}
		return enSet;
	}
}
