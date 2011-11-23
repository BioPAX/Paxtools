package org.biopax.paxtools.io.sif.level3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.model.BioPAXElement;
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

	private boolean mineStateChange;

	private boolean mineMetabolicChange;

	public void initOptionsNotNull(Map options)
	{
		mineStateChange = !options.containsKey(STATE_CHANGE) || options.get(STATE_CHANGE).equals(Boolean.TRUE);
		mineMetabolicChange =
				!options.containsKey(METABOLIC_CATALYSIS) || options.get(METABOLIC_CATALYSIS).equals(Boolean.TRUE);
	}

	/**
	 * When options map is null, then all rules are generated. Otherwise only rules
	 * that are contained in the options map as a key are generated.
	 * @param is3 set to fill in
	 * @param model biopax graph - may be null, has no use here
	 */
	public void inferInteractionsFromPE(InteractionSetL3 is3, PhysicalEntity pe, Model model)
	{
		BioPAXElement source = this.getEntityReferenceOrGroup(pe, is3);
		for (Interaction inter : pe.getParticipantOf())
		{
			if (inter instanceof Control)
			{
				Control cont = (Control) inter;
				// Iterate over all affected conversions of this control
				for (Conversion conv : getAffectedConversions(cont, null))
				{
					processConversion(is3, source, cont, conv);
				}
			}
		}
	}

	private void processConversion(InteractionSetL3 is3, BioPAXElement source, Control cont, Conversion conv)
	{
		// Collect left and right simple physical entities of conversion in lists
		Set<BioPAXElement> left = collectEntities(conv.getLeft(), is3);
		Set<BioPAXElement> right = collectEntities(conv.getRight(), is3);
		// Detect physical entities which appear on both sides.

		Set<BioPAXElement> intersection = new HashSet<BioPAXElement>(left);
		intersection.retainAll(right);

		Set<BioPAXElement> union = new HashSet<BioPAXElement>(left);
		union.addAll(right);

		// Create simple interactions
		// Try creating a rule for each physical entity in presence list.
		for (BioPAXElement target : union)
		{
			processParticipatingEntities(source, target, is3, cont, conv, intersection);
		}
	}

	private void processParticipatingEntities(BioPAXElement source, BioPAXElement target, InteractionSetL3 is3,
			Control cont, Conversion conv, Set<BioPAXElement> intersection)
	{
		if (source != target)
		{
			if (!(target instanceof Group) || ((Group) target).getType() != BinaryInteractionType.COMPONENT_OF)
			{
				mineTarget(source, target, is3, cont, conv, intersection);
			}
		}
	}

	private void mineTarget(BioPAXElement source, BioPAXElement target, InteractionSetL3 is3, Control cont,
			Conversion conv, Set<BioPAXElement> intersection)
	{
		if (entityHasAChange(target, conv, is3))
		{

			// If it is simple, then we check if it is also on both sides, regarding the
			// possibility that it may be nested in a complex.
			if (intersection.contains(target))
			{
				if (mineStateChange)
				{
					createAndAdd(source, target, is3, cont, conv, STATE_CHANGE);
				}
			}
			// Else it is a simple molecule appearing on one side of conversion. This means
			// it is metabolic change.
			else
			{
				if (mineMetabolicChange)
				{
					createAndAdd(source, target, is3, cont, conv, METABOLIC_CATALYSIS);
				}
			}
		}
	}

	private void createAndAdd(BioPAXElement source, BioPAXElement target, InteractionSetL3 is3, Control cont,
			Conversion conv, BinaryInteractionType type)
	{
		SimpleInteraction sc = new SimpleInteraction(source, target, type);
		sc.addMediator(cont);
		sc.addMediator(conv);
		is3.add(sc);
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
	 * Sometimes an entity is both an input and output to a conversion without any state change.
	 * Normally this phenomena should be modeled using controller property of conversion. In other
	 * cases this method detects entities that goes in and out without any change.
	 * @param conv
	 * @return true if entity has a change in conversion
	 */

	private boolean entityHasAChange(BioPAXElement element, Conversion conv, InteractionSetL3 l3)
	{
		Set<PhysicalEntity> left = getAssociatedStates(element, conv.getLeft(), l3);
		Set<PhysicalEntity> right = getAssociatedStates(element, conv.getRight(), l3);

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

	private Set<PhysicalEntity> getAssociatedStates(BioPAXElement element, Set<PhysicalEntity> pes,
			InteractionSetL3 l3)
	{
		Set<PhysicalEntity> set = new HashSet<PhysicalEntity>();

		if (element == null)
		{
			if (log.isWarnEnabled()) log.warn("Skipping ");
			return set; // empty
		}

		for (PhysicalEntity pe : pes)
		{
			addMappedElement(element, l3, set, pe);
			if (pe instanceof Complex)
			{
				for (PhysicalEntity member : ((Complex) pe).getComponent())
				{
					addMappedElement(element, l3, set, member);
				}
			}
		}
		return set;
	}

	private void addMappedElement(BioPAXElement element, InteractionSetL3 l3, Set<PhysicalEntity> set,
			PhysicalEntity pe)
	{
		if (element.equals(this.getEntityReferenceOrGroup(pe, l3))) set.add(pe);
	}

	public List<BinaryInteractionType> getRuleTypes()
	{
		return binaryInteractionTypes;
	}
}