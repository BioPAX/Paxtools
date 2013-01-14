package org.biopax.paxtools.causality;

import org.biopax.paxtools.controller.PathAccessor;
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
		boolean active)
	{
		Map<EntityReference, Set<ModificationFeature>> map =
			new HashMap<EntityReference, Set<ModificationFeature>>();

		for (EntityReference er : model.getObjects(EntityReference.class))
		{
			HashSet<ModificationFeature> features = new HashSet<ModificationFeature>();

			for (SimplePhysicalEntity spe : er.getEntityReferenceOf())
			{
				boolean isAct = finalDecisionActive(spe);
				boolean isInact = finalDecisionInactive(spe);

				if (isAct && isInact) System.out.println("both active and inactive: " + spe.getDisplayName());

				if ((active && !isAct) || (!active && !isInact)) continue;

				for (EntityFeature feat : spe.getFeature())
				{
					if (feat instanceof ModificationFeature)
					{
						features.add((ModificationFeature) feat);
					}
				}
			}
			map.put(er, features);
		}
		return map;
	}

	protected boolean finalDecisionActive(PhysicalEntity pe)
	{
		return !labeledInactive(pe) && !activityInName(pe, false) &&
			(hasActivity(pe) || labeledActive(pe) || activityInName(pe, true));
	}

	protected boolean finalDecisionInactive(PhysicalEntity pe)
	{
		return !labeledActive(pe) && !activityInName(pe, true) &&
			(activityInName(pe, false) || labeledInactive(pe) || hasUbiquitin(pe));
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

	protected boolean labeledActive(PhysicalEntity pe)
	{
		return hasFeature(pe, "residue modification, active");
	}
	
	protected boolean labeledInactive(PhysicalEntity pe)
	{
		return hasFeature(pe, "residue modification, inactive");
	}

	protected boolean hasUbiquitin(PhysicalEntity pe)
	{
		return hasFeature(pe, "ubiquitin");
	}

	protected boolean hasFeature(PhysicalEntity pe, String term)
	{
		for (Object o : new PathAccessor("PhysicalEntity/feature:ModificationFeature/modificationType/term").getValueFromBean(pe))
		{
			if (o.toString().contains(term)) return true;
		}
		return false;
	}
	
	protected boolean nameContains(Named named, String term)
	{
		if (named.getDisplayName() != null && named.getDisplayName().toLowerCase().contains(term))
			return true;
		if (named.getStandardName() != null && named.getStandardName().toLowerCase().contains(term))
			return true;
		for (String name : named.getName())
		{
			if (name.toLowerCase().contains(term)) return true;
		}
		return false;
	}
	
	protected boolean activityInName(Named named, boolean active)
	{
		if (!active) return nameContains(named, "inactiv");
		return nameContains(named, "activ") && !nameContains(named, "inactiv");
	}
}
