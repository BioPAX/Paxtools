package org.biopax.paxtools.causality;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class FeatureCollector
{
	public Map<EntityReference, Set<ModificationFeature>> collectFeatures(Model model,
		boolean activeOnly)
	{
		Map<EntityReference, Set<ModificationFeature>> map =
			new HashMap<EntityReference, Set<ModificationFeature>>();

		for (EntityReference er : model.getObjects(EntityReference.class))
		{
			if (!map.containsKey(er)) map.put(er, new HashSet<ModificationFeature>());

			for (SimplePhysicalEntity spe : er.getEntityReferenceOf())
			{
				if (activeOnly && !hasActivity(spe)) continue;

				for (EntityFeature feat : spe.getFeature())
				{
					if (feat instanceof ModificationFeature)
					{
						map.get(er).add((ModificationFeature) feat);
					}
				}
			}
		}
		return map;
	}

	protected boolean hasActivity(PhysicalEntity pe)
	{
		return hasBasicActivity(pe) || hasActivityInGeneric(pe, true) ||
			hasActivityInGeneric(pe, false) || hasActivityInComplex(pe);
	}

	protected boolean hasActivityInComplex(PhysicalEntity pe)
	{
		for (Complex cmp : pe.getComponentOf())
		{
			if (hasBasicActivity(cmp) || hasActivityInComplex(cmp) ||
				hasActivityInGeneric(cmp, true) || hasActivityInGeneric(cmp, false))
			{
				return true;
			}
		}
		return false;
	}

	protected boolean hasActivityInGeneric(PhysicalEntity pe, boolean up)
	{
		for (PhysicalEntity gen : up ?
			pe.getMemberPhysicalEntityOf() : pe.getMemberPhysicalEntity())
		{
			if (hasBasicActivity(gen) || hasActivityInGeneric(gen, up) || hasActivityInComplex(gen))
			{
				return true;
			}
		}
		return false;
	}

	protected boolean hasBasicActivity(PhysicalEntity pe)
	{
		return !pe.getControllerOf().isEmpty();
	}

}
