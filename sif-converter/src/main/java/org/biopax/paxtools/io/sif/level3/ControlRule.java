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
public class ControlRule implements InteractionRuleL3
{
	private final Log log = LogFactory.getLog(ControlRule.class);

	public void inferInteractions(Set<SimpleInteraction> interactionSet, Object entity, Model model, Map options)
	{
		inferInteractions(interactionSet, ((EntityReference) entity), model, options);
	}

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
		boolean mineStatetateChange = !options.containsKey(STATE_CHANGE) || options.get(STATE_CHANGE).equals(
				Boolean.TRUE);
		boolean mineMetabolicChange = !options.containsKey(METABOLIC_CATALYSIS) || options.get(
				METABOLIC_CATALYSIS).equals(Boolean.TRUE);
		// Iterate over all associated controls
		for (SimplePhysicalEntity pe : A.getEntityReferenceOf())
		{
			processPhysicalEntity(interactionSet, A, mineStatetateChange, mineMetabolicChange, pe);
		}
	}

	private void processPhysicalEntity(Set<SimpleInteraction> interactionSet, EntityReference A,
	                                   boolean mineStatetateChange, boolean mineMetabolicChange, PhysicalEntity pe)
	{
		for (Interaction inter : pe.getParticipantOf())
		{
			if (inter instanceof Control)
			{
				Control cont = (Control) inter;
				// Iterate over all affected conversions of this control
				for (Conversion conv : getAffectedConversions(cont, null))
				{
					Set<EntityReference> presenceSet = collectEntities(conv.getLeft(), null);
					collectEntities(conv.getRight(), presenceSet);
					// Collect left and right simple physical entities of conversion in lists
					List<EntityReference> left = collectSimpleEntities(conv.getLeft());
					List<EntityReference> right = collectSimpleEntities(conv.getRight());
					// Detect physical entities which appear on both sides.
					List<EntityReference> bothsided = new ArrayList<EntityReference>();
					for (EntityReference B : left)
					{
						if (right.contains(B))
						{
							bothsided.add(B);
						}
					}
					// Create simple interactions
					// Try creating a rule for each physical entity in presence list.
					for (EntityReference B : presenceSet)
					{
						// Consider only molecules that is changed by the conversion
						if (!entityHasAChange(B, conv))
						{
							continue;
						}
						// Affecting a complex is accepted as type of state change.
						// If it is simple, then we check if it is also on both sides, regarding the
						// possibility that it may be nested in a complex.
						if (B instanceof Complex || bothsided.contains(B))
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
			processPhysicalEntity(interactionSet, A, mineStatetateChange, mineMetabolicChange, comp);
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
			} else if (prcss instanceof Control)
			{
				getAffectedConversions((Control) prcss, convList);
			}
		}
		return convList;
	}

	/**
	 * Collects the associated physical entities of the given participant set.
	 * @param pes participants
	 * @param enSet physical entity set to collect in
	 * @return associated physical entities
	 */
	private Set<EntityReference> collectEntities(Set<PhysicalEntity> pes, Set<EntityReference> enSet)
	{
		if (enSet == null)
		{
			enSet = new HashSet<EntityReference>();
		}
		for (PhysicalEntity pe : pes)
		{
			//todo handle complexes
			getErAndAddifNotNull(enSet, pe);
		}
		return enSet;
	}

	private void getErAndAddifNotNull(Collection<EntityReference> enSet, PhysicalEntity pe)
	{
		if (pe instanceof SimplePhysicalEntity)
		{
			EntityReference er = ((SimplePhysicalEntity) pe).getEntityReference();
			if (er != null) enSet.add(er);
			else log.warn("SimplePhysicalEntity " + pe + " has NO (NULL) entityReference. " +
			              " (its interactions, if any, are likely to be ignored!)");
		}
	}

	/**
	 * Collects the associated non-complex physical entities of the given
	 * participant set. This means we are not interested in complexes, but their
	 * members, at any nesting.
	 * @param pes participants
	 * @return associated physical entities
	 */
	private List<EntityReference> collectSimpleEntities(Set<PhysicalEntity> pes)
	{
		List<EntityReference> erList = new ArrayList<EntityReference>();
		for (PhysicalEntity pe : pes)
		{
			if (pe instanceof Complex)
			{
				collectSimpleMembersOfComplex(erList, (Complex) pe);
			} else if (pe instanceof SimplePhysicalEntity)
			getErAndAddifNotNull(erList,pe);
		}
		return erList;
	}

	/**
	 * Recursive method for collecting simple members of the given complex in the
	 * given list.
	 * @param list where to collect
	 * @param comp complex to collect members
	 */
	private void collectSimpleMembersOfComplex(List<EntityReference> list, Complex comp)
	{
		for (PhysicalEntity pe : comp.getComponent())
		{
			if (pe instanceof Complex)
			{
				collectSimpleMembersOfComplex(list, (Complex) pe);
			} else if (pe instanceof SimplePhysicalEntity)
			{
				getErAndAddifNotNull(list,pe);
			}
		}
	}

	private Set<SimplePhysicalEntity> getSimplePEsInComplex(Complex comp, Set<SimplePhysicalEntity> set)
	{
		if (set == null) set = new HashSet<SimplePhysicalEntity>();
		for (PhysicalEntity pe : comp.getComponent())
		{
			if (pe instanceof SimplePhysicalEntity)
			{
				set.add((SimplePhysicalEntity) pe);
			} else if (pe instanceof Complex)
			{
				// Check for immediate cyclic complex
				if (!pe.equals(comp) && !comp.getComponentOf().contains(pe))
				{
					getSimplePEsInComplex((Complex) pe, set);
				}
			}
		}
		return set;
	}

	/**
	 * Sometimes an entity is both an input and output to a conversion without any state change.
	 * Normally this phenomena should be modeled using controller property of conversion. In other
	 * cases this method detects entities that goes in and out without any change.
	 * @param entity
	 * @param conv
	 * @return true if entity has a change in conversion
	 */
	private boolean entityHasAChange(EntityReference entity, Conversion conv)
	{
		Set<SimplePhysicalEntity> left = getAssociatedStates(entity, conv.getLeft());
		Set<SimplePhysicalEntity> right = getAssociatedStates(entity, conv.getRight());
		for (SimplePhysicalEntity lpe : left)
		{
			for (SimplePhysicalEntity rpe : right)
			{
				if (!lpe.equals(rpe)) return true;
			}
		}
		return false;
	}

	private Set<SimplePhysicalEntity> getAssociatedStates(EntityReference er, Set<PhysicalEntity> pes)
	{
		Set<SimplePhysicalEntity> set = new HashSet<SimplePhysicalEntity>();

		if (er == null)
		{
			if (log.isWarnEnabled()) log.warn("Skipping ");
			return set; // empty
		}

		for (PhysicalEntity pe : pes)
		{
			if (pe instanceof SimplePhysicalEntity && er.equals(((SimplePhysicalEntity) pe).getEntityReference()))
			{
				set.add((SimplePhysicalEntity) pe);
			} else if (pe instanceof Complex)
			{
				for (SimplePhysicalEntity spe : getSimplePEsInComplex((Complex) pe, null))
				{
					set.add(spe);
				}
			}
		}
		return set;
	}

	public List<BinaryInteractionType> getRuleTypes()
	{
		return Arrays.asList(STATE_CHANGE, METABOLIC_CATALYSIS);
	}
}