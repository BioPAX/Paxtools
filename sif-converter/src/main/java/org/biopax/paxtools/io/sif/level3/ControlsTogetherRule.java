package org.biopax.paxtools.io.sif.level3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;

import java.util.Arrays;
import java.util.List;

import static org.biopax.paxtools.io.sif.BinaryInteractionType.CO_CONTROL;

/**
 * <p/>
 * Implements Co-Control rule, which means A and B have a control over the same interaction.
 * <p/>
 * <p/>
 * This class performs a recursive search both downwards and and upwards of a
 * control that A is a controller. Upwards search always proceed to upwards, i.e.
 * never switch back to downwards at any point. If this is directly upwards of the
 * first control that we start the search, then all reachable B have dependent
 * activity to A. Downward search, however, is different. It can switch to an
 * upwards search when possible, to infer independent controls.
 * <p/>
 * There appears three possible search patterns, starting from the first control
 * (of A), each provides only dependent or independent relations:
 * <p/>
 * <ul>
 * <li>Upwards only --> dependent relation</li>
 * <li>Downwards only --> dependent relation</li>
 * <li>Downwards then upwards --> independent relation</li>
 * </ul>
 * @author Emek Demir
 * @author Ozgun Babur
 */
public class ControlsTogetherRule extends InteractionRuleL3Adaptor
{
	private static List<BinaryInteractionType> binaryInteractionTypes = Arrays.asList(CO_CONTROL);

	Log log = LogFactory.getLog(ControlsTogetherRule.class);
	BioPAXElement current;

	public void inferInteractionsFromPE(InteractionSetL3 l3, PhysicalEntity pe, Model model)
	{
		current = l3.getGroupMap().getEntityReferenceOrGroup(pe);
		if (current != null)
		{
			// Iterate over controls of A
			for (Interaction inter : pe.getParticipantOf())
			{
				if ((inter instanceof Control))
				{
					Control ctrl = (Control) inter;
					// Iterate other controllers
					iterateControllers(ctrl, l3);
					// Iterate upward controls
					proceedUpwards(ctrl, l3);
					// Iterate downward controls
					proceedDownwards(ctrl, l3);
				}
			}
		}
		else
		{
			if(log.isInfoEnabled())
			{
				log.info("Null ER or Group. Skipping"+pe);
			}
		}
	}

	/**
	 * Iterates downwards processes, initiates a downwards search of controls and
	 * upwards search of controls and conversions.
	 * @param ctrl to look downward
	 * @param interactionSet set to collect inferred interactions
	 */
	private void proceedDownwards(Control ctrl, InteractionSetL3 interactionSet)
	{
		// Iterate each controlled process
		for (Process prcss : ctrl.getControlled())
		{
			// Search downwards of controls
			if (prcss instanceof Control)
			{
				searchDownwards((Control) prcss, interactionSet);
			}

			// Search other upwards control trees of all processes (controls and conversions)
			for (Control cnt : prcss.getControlledOf())
			{
				if (cnt != ctrl) // Do not go where we came from
				{
					searchUpwards(cnt, interactionSet);
				}
			}
		}
	}

	/**
	 * Tries to infer relations with the controllers of the control and proceeds
	 * downwards.
	 * @param ctrl to search downwards
	 * @param interactionSet set to collect inferred interactions
	 */
	private void searchDownwards(Control ctrl, InteractionSetL3 interactionSet)
	{
		// Search for rules
		iterateControllers(ctrl, interactionSet);
		proceedDownwards(ctrl, interactionSet);
	}

	/**
	 * Tries to infer relations with the controllers of the control and proceeds to
	 * upwards.
	 * @param ctrl to search upwards
	 * @param interactionSet set to collect inferred rules
	 */
	private void searchUpwards(Control ctrl, InteractionSetL3 interactionSet)
	{
		iterateControllers(ctrl, interactionSet);
		proceedUpwards(ctrl, interactionSet);
	}

	/**
	 * Iterates the controller of ctrl and infers possible binary interactions.
	 * @param ctrl to iterate controllers
	 * @param interactionSet set to collect inferred interactions
	 */
	private void iterateControllers(Control ctrl, InteractionSetL3 interactionSet)
	{
		for (Controller pe : ctrl.getController())
		{
			if (pe instanceof PhysicalEntity)
			{
				BioPAXElement owner = interactionSet.getGroupMap().getEntityReferenceOrGroup(pe);
				if (!current.equals(owner))
				{
					createAndAdd(current, owner, interactionSet,CO_CONTROL);
				}
			}
		}
	}


	/**
	 * Iterates upward controls and initiates an upward search for each one.
	 * @param ctrl to proceed upwards
	 * @param interactionSet set to collect inferred interactions
	 */
	private void proceedUpwards(Control ctrl, InteractionSetL3 interactionSet)
	{
		for (Control cnt : ctrl.getControlledOf())
		{
			searchUpwards(cnt, interactionSet);
		}
	}

	public List<BinaryInteractionType> getRuleTypes()
	{
		return binaryInteractionTypes;
	}

}