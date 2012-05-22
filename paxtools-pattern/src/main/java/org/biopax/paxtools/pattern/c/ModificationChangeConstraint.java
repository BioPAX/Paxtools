package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.ModificationFeature;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.pattern.Match;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class ModificationChangeConstraint extends ConstraintAdapter
{
	boolean activating;
	
	static PathAccessor pa = new PathAccessor("PhysicalEntity/feature:ModificationFeature");
	
	protected final static String[] general = new String[]{
		"phospho", "ubiqutin", "acetyl", "myristoyl", "palmitoyl", "glucosyl"};
	
	Map<EntityReference, Set<ModificationFeature>> activityFeat;
	Map<EntityReference, Set<ModificationFeature>> inactivityFeat;

	Map<EntityReference, Set<String>> activityStr;
	Map<EntityReference, Set<String>> inactivityStr;

	public ModificationChangeConstraint(boolean activating,
		Map<EntityReference, Set<ModificationFeature>> activityFeat,
		Map<EntityReference, Set<ModificationFeature>> inactivityFeat)
	{
		this.activating = activating;
		this.activityFeat = activityFeat;
		this.inactivityFeat = inactivityFeat;

		activityStr = extractModifNames(activityFeat);
		inactivityStr = extractModifNames(inactivityFeat);
	}

	@Override
	public int getVariableSize()
	{
		return 2;
	}

	@Override
	public boolean satisfies(Match match, int... ind)
	{
		BioPAXElement ele1 = match.get(ind[0]);
		BioPAXElement ele2 = match.get(ind[1]);

		EntityReference er = ((SimplePhysicalEntity) ele1).getEntityReference();

		Set set1 = pa.getValueFromBean(ele1);
		Set set2 = pa.getValueFromBean(ele2);

		Set gain = new HashSet(set2);
		gain.removeAll(set1);

		Set loss = new HashSet(set1);
		loss.removeAll(set2);

		int activatingCnt = 0;
		int inhibitingCnt = 0;

		for (Object o : gain)
		{
			if (activityFeat.get(er).contains(o)) activatingCnt++;
			if (inactivityFeat.get(er).contains(o)) inhibitingCnt++;
		}
		for (Object o : loss)
		{
			if (inactivityFeat.get(er).contains(o)) activatingCnt++;
			if (activityFeat.get(er).contains(o)) inhibitingCnt++;
		}

		// Match without considering the locations

		Set<String> gainTypes = null;
		Set<String> lossTypes = null;

		if (activatingCnt + inhibitingCnt == 0)
		{
			gainTypes = extractModifNames(gain);
			lossTypes = extractModifNames(loss);

			for (String s : gainTypes)
			{
				if (activityStr.get(er).contains(s)) activatingCnt++;
				if (inactivityStr.get(er).contains(s)) inhibitingCnt++;
			}
			for (String s : lossTypes)
			{
				if (inactivityStr.get(er).contains(s)) activatingCnt++;
				if (activityStr.get(er).contains(s)) inhibitingCnt++;
			}
		}

		// Try to match modifications with approximate name matching

		if (activatingCnt + inhibitingCnt == 0)
		{
			for (String genName : general)
			{
				boolean foundInActivating = setContainsGeneralTerm(activityStr.get(er), genName);
				boolean foundInInhibiting = setContainsGeneralTerm(inactivityStr.get(er), genName);

				if (foundInActivating == foundInInhibiting) continue;

				boolean foundInGain = setContainsGeneralTerm(gainTypes, genName);
				boolean foundInLose = setContainsGeneralTerm(lossTypes, genName);

				if (foundInGain == foundInLose) continue;

				if (foundInActivating && foundInGain) activatingCnt++;
				else if (foundInInhibiting && foundInLose) activatingCnt++;
				else if (foundInActivating && foundInLose) inhibitingCnt++;
				else /*if (foundInInhibiting && foundInGain)*/ inhibitingCnt++;
			}
		}

		if (activatingCnt > 0 && inhibitingCnt > 0) return false;
		return activating ? activatingCnt > 0 : inhibitingCnt > 0;
	}
	

	protected Map<EntityReference, Set<String>> extractModifNames(Map mfMap)
	{
		Map<EntityReference, Set<String>> map = new HashMap<EntityReference, Set<String>>();

		for (Object o : mfMap.keySet())
		{
			EntityReference er = (EntityReference) o;
			map.put(er, extractModifNames((Set) mfMap.get(er)));
		}

		return map;
	}

	protected Set<String> extractModifNames(Set mfSet)
	{
		Set<String> set = new HashSet<String>();

		for (Object o : mfSet)
		{
			ModificationFeature mf = (ModificationFeature) o;
			if (mf.getModificationType() != null && !mf.getModificationType().getTerm().isEmpty())
			{
				set.add(mf.getModificationType().getTerm().iterator().next());
			}
		}
		return set;
	}

	protected boolean setContainsGeneralTerm(Set<String> set, String term)
	{
		for (String s : set)
		{
			if (s.contains(term)) return true;
		}
		return false;
	}
}
