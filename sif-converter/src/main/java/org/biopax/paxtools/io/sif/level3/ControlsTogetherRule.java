package org.biopax.paxtools.io.sif.level3;

import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 *
 * @author Emek Demir
 * @author Ozgun Babur
 */
public class ControlsTogetherRule implements InteractionRuleL3
{
	public void inferInteractions(Set<SimpleInteraction> interactionSet,
		Object entity, Model model, Map options)
	{
		inferInteractions(interactionSet, ((EntityReference) entity), model, options);
	}

	public void inferInteractions(Set<SimpleInteraction> interactionSet,
		EntityReference A, Model model, Map options)
	{
		boolean mineCoControl = !options.containsKey(CO_CONTROL) ||
			options.get(CO_CONTROL).equals(true);

		// Return if there is nothing to find
		if (!mineCoControl)
		{
			return;
		}
		for (PhysicalEntity pe : A.getEntityReferenceOf())
		{
			processPhysicalEntity(interactionSet, A, pe);
		}
	}

	private void processPhysicalEntity(Set<SimpleInteraction> interactionSet, EntityReference A,
		PhysicalEntity pe)
	{
		// Iterate over controls of A
		for (Interaction inter : pe.getParticipantOf())
		{
			if (!(inter instanceof Control)) continue;

			Control ctrl = (Control) inter;

			// Iterate other controllers
			iterateControllers(ctrl, A, interactionSet);
			// Iterate upward controls
			proceedUpwards(ctrl, A, interactionSet);
			// Iterate downward controls
			proceedDownwards(ctrl, A, interactionSet);
		}
		for (Complex comp : pe.getComponentOf())
		{
			processPhysicalEntity(interactionSet, A, comp);
		}
	}

	/**
	 * Iterates downwards processes, initiates a downwards search of controls and
	 * upwards search of controls and conversions.
	 *
	 * @param ctrl		   to look downward
	 * @param A			  first entity in A-B binary interaction
	 * @param interactionSet set to collect inferred interactions
	 */
	private void proceedDownwards(Control ctrl, EntityReference A,
		Set<SimpleInteraction> interactionSet)
	{
		// Iterate each controlled process
		for (Process prcss : ctrl.getControlled())
		{
			// Search downwards of controls
			if (prcss instanceof Control)
			{
				searchDownwards((Control) prcss, A, interactionSet);
			}

			// Search other upwards control trees of all processes (controls and conversions)
			for (Control cnt : prcss.getControlledOf())
			{
				if (cnt != ctrl) // Do not go where we came from
				{
					searchUpwards(cnt, A, interactionSet);
				}
			}
		}
	}

	/**
	 * Tries to infer relations with the controllers of the control and proceeds
	 * downwards.
	 *
	 * @param ctrl		   to search downwards
	 * @param A			  A of A-B
	 * @param interactionSet set to collect inferred interactions
	 */
	private void searchDownwards(Control ctrl, EntityReference A,
		Set<SimpleInteraction> interactionSet)
	{
		// Search for rules
		iterateControllers(ctrl, A, interactionSet);
		proceedDownwards(ctrl, A, interactionSet);
	}

	/**
	 * Tries to infer relations with the controllers of the control and proceeds to
	 * upwards.
	 *
	 * @param ctrl		   to search upwards
	 * @param A			  A in A-B
	 * @param interactionSet set to collect inferred rules
	 */
	private void searchUpwards(Control ctrl, EntityReference A,
		Set<SimpleInteraction> interactionSet)
	{
		iterateControllers(ctrl, A, interactionSet);
		proceedUpwards(ctrl, A, interactionSet);
	}

	/**
	 * Iterates the controller of ctrl and infers possible binary interactions.
	 *
	 * @param ctrl	to iterate controllers
	 * @param A		A in A-B ctrl
	 * @param interactionSet set to collect inferred interactions
	 */
	private void iterateControllers(Control ctrl, EntityReference A,
		Set<SimpleInteraction> interactionSet)
	{
		for (Controller pe : ctrl.getController())
		{
			if (pe instanceof SimplePhysicalEntity)
			{
				EntityReference B = ((SimplePhysicalEntity) pe).getEntityReference();

				if (B!=null || B!= A)
				{
					interactionSet.add(new SimpleInteraction(A, B, CO_CONTROL));
				}
			}
			else if (pe instanceof Complex)
			{
				for (EntityReference B : ((Complex) pe).getMemberReferences())
				{
					if (B != A)
					{
						interactionSet.add(new SimpleInteraction(A, B, CO_CONTROL));
					}
				}
			}
		}
	}

	/**
	 * Iterates upward controls and initiates an upward search for each one.
	 *
	 * @param ctrl		   to proceed upwards
	 * @param A			  A in A-B
	 * @param interactionSet set to collect inferred interactions
	 */
	private void proceedUpwards(Control ctrl, EntityReference A,
		Set<SimpleInteraction> interactionSet)
	{
		for (Control cnt : ctrl.getControlledOf())
		{
			searchUpwards(cnt, A, interactionSet);
		}
	}

	public List<BinaryInteractionType> getRuleTypes()
	{
		return Arrays.asList(CO_CONTROL);
	}
}