package org.biopax.paxtools.io.sif.level3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;

import java.util.*;

import static org.biopax.paxtools.io.sif.BinaryInteractionType.METABOLIC_CATALYSIS;
import static org.biopax.paxtools.io.sif.BinaryInteractionType.STATE_CHANGE;

/**
 * A controls a conversion which B is at left or right or both. -
 * Controls.StateChange (B at both sides (one side may be as a member of a
 * complex), or B is complex) - Controls.MetabolicChange (B at one side only)
 * @author Ozgun Babur Date: Dec 29, 2007 Time: 1:27:55 AM
 */
public class ControlRule extends InteractionRuleL3Adaptor
{
	private final Log log = LogFactory.getLog(ControlRule.class);

	private static List<BinaryInteractionType> binaryInteractionTypes =
		Arrays.asList(METABOLIC_CATALYSIS, STATE_CHANGE);

	/**
	 * When options map is null, then all rules are generated. Otherwise only rules
	 * that are contained in the options map as a key are generated.
	 * @param interactionSet set to fill in
	 * @param A first physical entity
	 * @param model biopax graph - may be null, has no use here
	 * @param options options map
	 */
	public void inferInteractions(Set<SimpleInteraction> interactionSet, EntityReference A, Model model, Map options)
	{
		// Read options
		boolean mineStatetateChange = !options.containsKey(STATE_CHANGE) ||
			options.get(STATE_CHANGE).equals(Boolean.TRUE);
		boolean mineMetabolicChange = !options.containsKey(METABOLIC_CATALYSIS) ||
			options.get(METABOLIC_CATALYSIS).equals(Boolean.TRUE);
		// Iterate over all associated controls
		for (SimplePhysicalEntity pe : A.getEntityReferenceOf())
		{
			processPhysicalEntity(interactionSet, A, mineStatetateChange, mineMetabolicChange, pe, options);
		}
	}

	private void processPhysicalEntity(Set<SimpleInteraction> interactionSet, EntityReference A,
		boolean mineStatetateChange, boolean mineMetabolicChange, PhysicalEntity pe, Map options)
	{
		for (Interaction inter : pe.getParticipantOf())
		{
			if (inter instanceof Control)
			{
				Control cont = (Control) inter;
				// Iterate over all affected conversions of this control
				for (Conversion conv : getAffectedConversions(cont, null))
				{
					// Collect left and right simple physical entities of conversion in lists
					Set<EntityReference> left = collectEntityReferences(conv.getLeft(), null, options);
					Set<EntityReference> right = collectEntityReferences(conv.getRight(), null, options);
					// Detect physical entities which appear on both sides.
					List<EntityReference> bothsided = new ArrayList<EntityReference>();
					for (EntityReference B : left)
					{
						if (right.contains(B))
						{
							bothsided.add(B);
						}
					}

					Set<EntityReference> presenceSet = new HashSet<EntityReference>(left);
					presenceSet.addAll(right);

					// Create simple interactions
					// Try creating a rule for each physical entity in presence list.
					for (EntityReference B : presenceSet)
					{
						if (A == B) continue;

						// Consider only molecules that is changed by the conversion
						if (!entityHasAChange(B, conv, options))
						{
							continue;
						}
						// Affecting a complex is accepted as type of state change.
						// If it is simple, then we check if it is also on both sides, regarding the
						// possibility that it may be nested in a complex.
						if (bothsided.contains(B))
						{
							if (mineStatetateChange)
							{
								SimpleInteraction sc = new SimpleInteraction(A, B, STATE_CHANGE);
								sc.addMediator(cont);
								sc.addMediator(conv);
								interactionSet.add(sc);
							}
						}
						// Else it is a simple molecule appearing on one side of conversion. This means
						// it is metabolic change.
						else
						{
							if (mineMetabolicChange)
							{
								SimpleInteraction mc = new SimpleInteraction(A, B, METABOLIC_CATALYSIS);
								mc.addMediator(cont);
								mc.addMediator(conv);
								interactionSet.add(mc);
							}
						}
					}
				}
			}
		}
		for (Complex comp : pe.getComponentOf())
		{
			processPhysicalEntity(interactionSet, A, mineStatetateChange, mineMetabolicChange, comp, options);
		}
	}

	/**
	 * Creates a list of conversions on which this control has an effect. If the
	 * control controls another control, then it is traversed recursively to find
	 * the affected conversions.
	 * @param cont control
	 * @param convList list of affected conversions
	 * @return list of affected conversions
	 */
	private List<Conversion> getAffectedConversions(Control cont, List<Conversion> convList)
	{
		if (convList == null)
		{
			convList = new ArrayList<Conversion>();
		}
		for (Process prcss : cont.getControlled())
		{
			if (prcss instanceof Conversion)
			{
				convList.add((Conversion) prcss);
			}
			else if (prcss instanceof Control)
			{
				getAffectedConversions((Control) prcss, convList);
			}
		}
		return convList;
	}

	/**
	 * Sometimes an entity is both an input and output to a conversion without any state change.
	 * Normally this phenomena should be modeled using controller property of conversion. In other
	 * cases this method detects entities that goes in and out without any change.
	 * @param entity
	 * @param conv
	 * @return true if entity has a change in conversion
	 */
	private boolean entityHasAChange(EntityReference entity, Conversion conv, Map options)
	{
		Set<PhysicalEntity> left = getAssociatedStates(entity, conv.getLeft(), options);
		Set<PhysicalEntity> right = getAssociatedStates(entity, conv.getRight(), options);

		// There should be at least one state found
		assert !left.isEmpty() || !right.isEmpty();

		if (left.isEmpty() || right.isEmpty()) return true;

		for (PhysicalEntity lpe : left)
		{
			for (PhysicalEntity rpe : right)
			{
				if (!lpe.equals(rpe)) return true;
			}
		}
		return false;
	}

	private Set<PhysicalEntity> getAssociatedStates(EntityReference er, Set<PhysicalEntity> pes,
		Map options)
	{
		Set<PhysicalEntity> set = new HashSet<PhysicalEntity>();

		if (er == null)
		{
			if (log.isWarnEnabled()) log.warn("Skipping ");
			return set; // empty
		}

		for (PhysicalEntity pe : pes)
		{
			if (collectEntityReferences(pe, options).contains(er))
				set.add(pe);
		}
		return set;
	}

	public List<BinaryInteractionType> getRuleTypes()
	{
		return binaryInteractionTypes;
	}
}