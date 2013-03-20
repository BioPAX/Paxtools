package org.biopax.paxtools.io.sif.level3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.io.sif.BinaryInteractionType;
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
	/**
	 * Log for logging.
	 */
	private final Log log = LogFactory.getLog(ControlRule.class);

	/**
	 * Supported interaction types.
	 */
	private static List<BinaryInteractionType> binaryInteractionTypes =
			Arrays.asList(METABOLIC_CATALYSIS, STATE_CHANGE);

	/**
	 * Option to mine STATE_CHANGE rule.
	 */
	private boolean mineStateChange;

	/**
	 * Option to mine METABOLIC_CHANGE rule.
	 */
	private boolean mineMetabolicChange;

	/**
	 * Map form the source element to the state change data.
	 */
	private HashMap<BioPAXElement, Set<PEStateChange>> stateChanges = new HashMap<BioPAXElement,
			Set<PEStateChange>>();

	/**
	 * Initializes options.
	 * @param options options map
	 */
	public void initOptionsNotNull(Map options)
	{
		mineStateChange = !checkOption(STATE_CHANGE, Boolean.FALSE, options);
		mineMetabolicChange = !checkOption(METABOLIC_CATALYSIS, Boolean.FALSE, options);
	}

	/**
	 * When options map is null, then all rules are generated. Otherwise only rules
	 * that are contained in the options map as a key are generated.
	 * @param is3 set to fill in
	 * @param model biopax graph - may be null, has no use here
	 */
	public void inferInteractionsFromPE(InteractionSetL3 is3, PhysicalEntity pe, Model model)
	{
		BioPAXElement source = is3.getGroupMap().getEntityReferenceOrGroup(pe);
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

	/**
	 * Continue inference with the Control and the Conversion.
	 * @param is3 mined interactions
	 * @param source source of interaction
	 * @param cont the control that source is controller
	 * @param conv the conversion that control is controlling
	 */
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
			if (source != target)
			{
				if (!(target instanceof Group) || !((Group) target).isComplex())
				{
					mineTarget(source, target, is3, cont, conv, intersection);
				}
			}
		}
	}

	/**
	 * Create the interaction if the target is changing state.
	 * @param source source of interaction
	 * @param target target of interaction
	 * @param is3 mined rules
	 * @param cont the control that source is controller
	 * @param conv the conversion that control is controlling
	 * @param intersection elements that are both at left and right of the conversion
	 */
	private void mineTarget(BioPAXElement source, BioPAXElement target, InteractionSetL3 is3, Control cont,
			Conversion conv, Set<BioPAXElement> intersection)
	{
		if (Simplify.entityHasAChange(target, conv, is3.getGroupMap(), stateChanges.get(target)))
		{

			// If it is simple, then we check if it is also on both sides, regarding the
			// possibility that it may be nested in a complex.
			if (intersection.contains(target))
			{
				if (mineStateChange)
				{
					createAndAdd(source, target, is3, STATE_CHANGE, cont, conv);
				}
			}
			// Else it is a simple molecule appearing on one side of conversion. This means
			// it is metabolic change.
			else
			{
				if (mineMetabolicChange)
				{
					createAndAdd(source, target, is3, METABOLIC_CATALYSIS, cont, conv);
				}
			}
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
	 * Gets supported interaction types.
	 * @return supported interaction types
	 */
	public List<BinaryInteractionType> getRuleTypes()
	{
		return binaryInteractionTypes;
	}
}