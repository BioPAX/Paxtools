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
	private final Log log = LogFactory.getLog(ControlRule.class);

	private static List<BinaryInteractionType> binaryInteractionTypes =
			Arrays.asList(METABOLIC_CATALYSIS, STATE_CHANGE);

	private boolean mineStateChange;

    private boolean storeStateChange;

	private boolean mineMetabolicChange;
    public static final String STORE_STATE_CHANGE = "StoreStateChange";

    public HashMap<BioPAXElement, Set<PEStateChange>> getStateChanges()
    {
        return stateChanges;
    }

    private HashMap<BioPAXElement, Set<PEStateChange>> stateChanges;

    public void initOptionsNotNull(Map options)
	{
		mineStateChange = !checkOption(STATE_CHANGE,Boolean.FALSE, options);
		mineMetabolicChange =!checkOption(METABOLIC_CATALYSIS,Boolean.FALSE, options);
        storeStateChange = checkOption(STORE_STATE_CHANGE,Boolean.TRUE, options);
        if(storeStateChange)
        {
            stateChanges = new HashMap<BioPAXElement, Set<PEStateChange>>();
        }
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
                if (!(target instanceof Group) || ((Group) target).getType() != BinaryInteractionType.COMPONENT_OF)
                {
                    mineTarget(source, target, is3, cont, conv, intersection);
                }
            }
		}
	}

	private void mineTarget(BioPAXElement source, BioPAXElement target, InteractionSetL3 is3, Control cont,
			Conversion conv, Set<BioPAXElement> intersection)
	{
		if (Simplify.entityHasAChange(target, conv, is3.getGroupMap(),stateChanges))
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
					createAndAdd(source, target, is3, METABOLIC_CATALYSIS,cont, conv);
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

	public List<BinaryInteractionType> getRuleTypes()
	{
		return binaryInteractionTypes;
	}
}