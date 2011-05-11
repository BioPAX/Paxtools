package org.biopax.paxtools.io.sif.level2;

import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.*;

import java.util.*;

import static org.biopax.paxtools.io.sif.BinaryInteractionType.METABOLIC_CATALYSIS;
import static org.biopax.paxtools.io.sif.BinaryInteractionType.STATE_CHANGE;

/**
 * A controls a conversion which B is at left or right or both. -
 * Controls.StateChange (B at both sides (one side may be as a member of a
 * complex), or B is complex) - Controls.MetabolicChange (B at one side only)
 *
 * @author Ozgun Babur Date: Dec 29, 2007 Time: 1:27:55 AM
 */
public class ControlRule implements InteractionRuleL2
{
	public void inferInteractions(Set<SimpleInteraction> interactionSet,
		Object entity,
		Model model, Map options)
	{
		inferInteractions(interactionSet, ((physicalEntity) entity), model, options);
	}

	/**
	 * When options map is null, then all rules are generated. Otherwise only rules
	 * that are contained in the options map as a key are generated.
	 *
	 * @param interactionSet set to fill in
	 * @param A              first physical entity
	 * @param model          biopax graph - may be null, has no use here
	 * @param options        options map
	 */
	public void inferInteractions(Set<SimpleInteraction> interactionSet,
	                              physicalEntity A,
	                              Model model, Map options)
	{
		// Read options

		boolean mineStatetateChange = !options.containsKey(STATE_CHANGE) ||
			options.get(STATE_CHANGE).equals(Boolean.TRUE);

		boolean mineMetabolicChange = !options.containsKey(METABOLIC_CATALYSIS) ||
			options.get(METABOLIC_CATALYSIS).equals(Boolean.TRUE);

		// Iterate over all associated controls

		for (control cont : A.getAllInteractions(control.class))
		{
			// Iterate over all affected conversions of this control

			for (conversion conv : getAffectedConversions(cont, null))
			{
				Set<physicalEntity> presenceSet =
					collectEntities(conv.getLEFT(), null);
				collectEntities(conv.getRIGHT(), presenceSet);

				// Collect left and right simple physical entities of conversion in lists

				List<physicalEntity> left =
					collectSimpleEntities(conv.getLEFT());
				List<physicalEntity> right =
					collectSimpleEntities(conv.getRIGHT());

				// Detect physical entities which appear on both sides.

				List<physicalEntity> bothsided =
					new ArrayList<physicalEntity>();

				for (physicalEntity B : left)
				{
					if (right.contains(B))
					{
						bothsided.add(B);
					}
				}

				// Create simple interactions
				// Try creating a rule for each physical entity in presence list.

				for (physicalEntity B : presenceSet)
				{
					// Consider only molecules that is changed by the conversion
					if (!entityHasAChange(B, conv))
					{
						continue;
					}

					// Affecting a complex is accepted as type of state change.
					// If it is simple, then we check if it is also on both sides, regarding the
					// possibility that it may be nested in a complex.

					if (B instanceof complex || bothsided.contains(B))
					{
						if (mineStatetateChange)
						{
                            SimpleInteraction sc = new SimpleInteraction(A,B,STATE_CHANGE);
                            sc.addMediator(cont);
                            sc.addMediator(conv);
                            interactionSet.add(sc);						}
					}

					// Else it is a simple molecule appearing on one side of conversion. This means
					// it is metabolic change.

					else
					{
						if (mineMetabolicChange)
						{
                            SimpleInteraction mc = new SimpleInteraction(A,B,METABOLIC_CATALYSIS);
                            mc.addMediator(cont);
                            mc.addMediator(conv);
                            interactionSet.add(mc);
						}
					}
				}
			}
		}
	}

	/**
	 * Creates a list of conversions on which this control has an effect. If the
	 * control controls another control, then it is traversed recursively to find
	 * the affected conversions.
	 *
	 * @param cont     control
	 * @param convList list of affected conversions
	 * @return list of affected conversions
	 */
	private List<conversion> getAffectedConversions(control cont,
	                                                List<conversion> convList)
	{
		if (convList == null)
		{
			convList = new ArrayList<conversion>();
		}

		for (process prcss : cont.getCONTROLLED())
		{
			if (prcss instanceof conversion)
			{
				convList.add((conversion) prcss);
			}
			else if (prcss instanceof control)
			{
				getAffectedConversions((control) prcss, convList);
			}
		}

		return convList;
	}

	/**
	 * Collects the associated physical entities of the given participant set.
	 *
	 * @param partics participants
	 * @param peSet   physical entity set to collect in
	 * @return associated physical entities
	 */
	private Set<physicalEntity> collectEntities(
		Set<physicalEntityParticipant> partics,
		Set<physicalEntity> peSet)
	{
		if (peSet == null)
		{
			peSet = new HashSet<physicalEntity>();
		}

		for (physicalEntityParticipant partic : partics)
		{
			peSet.add(partic.getPHYSICAL_ENTITY());
		}

		return peSet;
	}

	/**
	 * Collects the associated non-complex physical entities of the given
	 * participant set. This means we are not interested in complexes, but their
	 * members, at any nesting.
	 *
	 * @param partics participants
	 * @return associated physical entities
	 */
	private List<physicalEntity> collectSimpleEntities(
		Set<physicalEntityParticipant> partics)
	{
		List<physicalEntity> peList = new ArrayList<physicalEntity>();

		for (physicalEntityParticipant partic : partics)
		{
			physicalEntity pe = partic.getPHYSICAL_ENTITY();

			if (pe instanceof complex)
			{
				collectSimpleMembersOfComplex(peList, (complex) pe);
			}
			else
			{
				peList.add(pe);
			}
		}

		return peList;
	}

	/**
	 * Recursive method for collecting simple members of the given complex in the
	 * given list.
	 *
	 * @param list where to collect
	 * @param comp complex to collect members
	 */
	private void collectSimpleMembersOfComplex(List<physicalEntity> list,
	                                           complex comp)
	{
		for (physicalEntityParticipant pep : comp.getCOMPONENTS())
		{
			physicalEntity pe = pep.getPHYSICAL_ENTITY();

			if (pe instanceof complex)
			{
				collectSimpleMembersOfComplex(list, (complex) pe);
			}
			else
			{
				list.add(pe);
			}
		}
	}

	/**
	 * Sometimes an entity is both an input and output to a conversion without any state change.
	 * Normally this phenomena should be modeled using controller property of conversion. In other
	 * cases this method detects entities that goes in and out without any change.
	 *
	 * @param entity
	 * @param conv
	 * @return true if entity has a change in conversion
	 */
	private boolean entityHasAChange(physicalEntity entity, conversion conv)
	{
		Set<StateWrapper> leftonly = new HashSet<StateWrapper>();
		Set<StateWrapper> rightonly = new HashSet<StateWrapper>();
		Set<StateWrapper> both = new HashSet<StateWrapper>();

		for (physicalEntityParticipant pep : conv.getLEFT())
		{
			if (pep.getPHYSICAL_ENTITY() == entity)
			{
				leftonly.add(new StateWrapper(pep));
			}
		}

		for (physicalEntityParticipant pep : conv.getRIGHT())
		{
			if (pep.getPHYSICAL_ENTITY() == entity)
			{
				StateWrapper sw = new StateWrapper(pep);
				if (leftonly.contains(sw))
				{
					leftonly.remove(sw);
					both.add(sw);
				}
				else if (!both.contains(sw))
				{
					rightonly.add(sw);
				}
			}
		}

		return !leftonly.isEmpty() || !rightonly.isEmpty();
	}

	public List<BinaryInteractionType> getRuleTypes()
	{
		return Arrays.asList(STATE_CHANGE,
                METABOLIC_CATALYSIS);
	}

	private class StateWrapper
	{
		final physicalEntityParticipant pep;

		private StateWrapper(physicalEntityParticipant pep)
		{
			this.pep = pep;
		}

		public int hashCode()
		{
			return pep.stateCode();
		}

		public boolean equals(Object obj)
		{
			if (obj instanceof StateWrapper)
			{
				StateWrapper sw = (StateWrapper) obj;

				return pep.isInEquivalentState(sw.pep);
			}

			return false;
		}
	}
}
