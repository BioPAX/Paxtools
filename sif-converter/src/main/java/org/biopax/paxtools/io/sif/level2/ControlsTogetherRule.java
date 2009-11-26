package org.biopax.paxtools.io.sif.level2;

import org.biopax.paxtools.io.sif.InteractionRule;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.io.sif.BinaryInteractionType;
import static org.biopax.paxtools.io.sif.BinaryInteractionType.*;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Implements following rules. In all rules A and B has control on same process.
 *
 * <ul>
 * <li><b>Co-Control.Dependent.Similar:</b> A and B has dependent activity,
 * and they have similar effects.</li>
 * <li><b>Co-Control.Dependent.Anti:</b> A and B has dependent activity,
 * and they have different effects.
 * 		Most probably one inhibits the other's control.</li>
 * <li><b>Co-Control.Independent.Similar:</b> A and B are independent,
 * and they have similar effects.</li>
 * <li><b>Co-Control.Independent.Anti:</b> A and B are independent,
 * and they have different effects.</li>
 * </ul>
 *
 * Dependency between two entities is defined as follows:
 * <br>
 * <code>A depends B iff {A = B || A depends C, and B controls C}</code>
 * <p>
 * This class performs a recursive search both downwards and and upwards of a
 * control that A is a controller. Upwards search always proceed to upwards, i.e.
 * never switch back to downwards at any point. If this is directly upwards of the
 * first control that we start the search, then all reachable B have dependent
 * activity to A. Downward search, however, is different. It can switch to an
 * upwards search when possible, to infer independent controls.
 * <p>
 * There appears three possible search patterns, starting from the first control
 * (of A), each provides only dependent or independent relations:
 *
 * <ul>
 * <li>Upwards only --> dependent relation</li>
 * <li>Downwards only --> dependent relation</li>
 * <li>Downwards then upwards --> independent relation</li>
 * </ul>
 *
 * During the search the sign of relations are also considered to differentiate
 * between similar or anti controls.
 *
 * @author Emek Demir
 * @author Ozgun Babur
 */
public class ControlsTogetherRule implements InteractionRule
{
	// Variables for remembering rule options

	private boolean mineDepSim;
	private boolean mineDepAnti;
	private boolean mineIndepSim;
	private boolean mineIndepAnti;

	public void inferInteractions(Set<SimpleInteraction> interactionSet,
	                              physicalEntity A, Model model,
	                              Map options)
	{
		mineDepSim = !options.containsKey(CO_CONTROL_DEPENDENT_SIMILAR) ||
			options.get(CO_CONTROL_DEPENDENT_SIMILAR).equals(true);

		mineDepAnti = !options.containsKey(CO_CONTROL_DEPENDENT_ANTI) ||
			options.get(CO_CONTROL_DEPENDENT_ANTI).equals(true);

		mineIndepSim = !options.containsKey(CO_CONTROL_INDEPENDENT_SIMILAR) ||
			options.get(CO_CONTROL_INDEPENDENT_SIMILAR).equals(true);

		mineIndepAnti = !options.containsKey(CO_CONTROL_INDEPENDENT_ANTI) ||
			options.get(CO_CONTROL_INDEPENDENT_ANTI).equals(true);

		// Return if there is nothing to find
		if (!mineDepSim && !mineDepAnti && !mineIndepSim && !mineIndepAnti)
		{
			return;
		}

		// Iterate over controls of A

		for (control ctrl : A.getAllInteractions(control.class))
		{
			// Iterate other controllers

			if (mineDepSim)
			{
				iterateControllers(ctrl, A, DEPENDENT, SIMILAR, interactionSet);
			}

			// Iterate upward controls

			if (mineDepSim || mineDepAnti)
			{
				proceedUpwards(ctrl, A, DEPENDENT, SIMILAR, interactionSet);
			}

			// Determine effect of this control to the downward control
			int sign = getSign(ctrl.getCONTROL_TYPE());

			// Iterate downward controls
			proceedDownwards(ctrl, A, sign, interactionSet);
		}
	}

	/**
	 * Iterates downwards processes, initiates a downwards search of controls and
	 * upwards search of controls and conversions.
	 *
	 * @param ctrl           to look downward
	 * @param A              first entity in A-B binary interaction
	 * @param similarity     similarity of effect between A and controllers of
	 *                       ctrl
	 * @param interactionSet set to collect inferred interactions
	 */
	private void proceedDownwards(control ctrl,
	                              physicalEntity A,
	                              int similarity,
	                              Set<SimpleInteraction> interactionSet)
	{
		// Iterate each controlled process

		for (process prcss : ctrl.getCONTROLLED())
		{
			// Search downwards of controls

			if (prcss instanceof control)
			{
				searchDownwards((control) prcss, A, similarity, interactionSet);
			}

			// Search other upwards control trees of all processes (controls and conversions)

			if (mineIndepSim || mineIndepAnti)
			{
				for (control cnt : prcss.isCONTROLLEDOf())
				{
					if (cnt != ctrl) // Do not go where we came from
					{
						searchUpwards(cnt,
							A,
							INDEPENDENT,
							similarity,
							interactionSet);
					}
				}
			}
		}
	}

	/**
	 * Tries to infer relations with the controllers of the control and proceeds
	 * downwards.
	 *
	 * @param ctrl           to search downwards
	 * @param A              A of A-B
	 * @param similarity     similarity of effect of A and controllers of ctrl
	 * @param interactionSet set to collect inferred interactions
	 */
	private void searchDownwards(control ctrl,
	                             physicalEntity A,
	                             int similarity,
	                             Set<SimpleInteraction> interactionSet)
	{
		// Search for rules
		iterateControllers(ctrl, A, DEPENDENT, similarity, interactionSet);

		// Find similarity of downward control with control of A
		similarity *= getSign(ctrl.getCONTROL_TYPE());

		proceedDownwards(ctrl, A, similarity, interactionSet);
	}

	/**
	 * Tries to infer relations with the controllers of the control and proceeds to
	 * upwards.
	 *
	 * @param ctrl           to search upwards
	 * @param A              A in A-B
	 * @param dependent      tells if this is a dependent or independent branch
	 * @param similarity     similarity of effect of the previous control with A
	 * @param interactionSet set to collect inferred rules
	 */
	private void searchUpwards(control ctrl,
	                           physicalEntity A,
	                           boolean dependent,
	                           int similarity,
	                           Set<SimpleInteraction> interactionSet)
	{
		// Find similarity of this control with control of A
		similarity *= getSign(ctrl.getCONTROL_TYPE());

		iterateControllers(ctrl, A, dependent, similarity, interactionSet);
		proceedUpwards(ctrl, A, dependent, similarity, interactionSet);
	}

	/**
	 * Iterates the controller of ctrl and infers possible binary interactions.
	 *
	 * @param ctrl           to iterate controllers
	 * @param A              A in A-B
	 * @param dependent      the dependency between A and controllers of ctrl
	 * @param similarity     similarity of effect between A and controllers of
	 *                       ctrl
	 * @param interactionSet set to collect inferred interactions
	 */
	private void iterateControllers(control ctrl,
	                                physicalEntity A,
	                                boolean dependent,
	                                int similarity,
	                                Set<SimpleInteraction> interactionSet)
	{
		if ((similarity == SIMILAR && dependent && mineDepSim) ||
			(similarity == ANTI && dependent && mineDepAnti) ||
			(similarity == SIMILAR && !dependent && mineIndepSim) ||
			(similarity == ANTI && !dependent && mineIndepAnti))
		{
			// Determine type of relation using similarity and dependency
			BinaryInteractionType type = similarity == SIMILAR ?
				dependent ? CO_CONTROL_DEPENDENT_SIMILAR :
					CO_CONTROL_INDEPENDENT_SIMILAR :
				dependent ? CO_CONTROL_DEPENDENT_ANTI :
					CO_CONTROL_INDEPENDENT_ANTI;

			for (physicalEntityParticipant pep : ctrl.getCONTROLLER())
			{
				physicalEntity B = pep.getPHYSICAL_ENTITY();

				if (B != A)
				{
					interactionSet
						.add(new SimpleInteraction(A, B, type));
				}
			}
		}
	}

	/**
	 * Iterates upward controls and initiates an upward search for each one.
	 *
	 * @param ctrl           to proceed upwards
	 * @param A              A in A-B
	 * @param dependent      tells if the upward branch is a dependent or
	 *                       independent branch
	 * @param similarity     similarity of effect between A and predecessor
	 *                       control
	 * @param interactionSet set to collect inferred interactions
	 */
	private void proceedUpwards(control ctrl,
	                            physicalEntity A,
	                            boolean dependent,
	                            int similarity,
	                            Set<SimpleInteraction> interactionSet)
	{
		for (control cnt : ctrl.isCONTROLLEDOf())
		{
			searchUpwards(cnt, A, dependent, similarity, interactionSet);
		}
	}

	/**
	 * Converts control type of the control object to a positive or negative sign.
	 * This is practical for updating the effect by multiplication.
	 *
	 * @param ctrlType type of control
	 * @return 1 is positive, -1 if negative
	 */
	private int getSign(ControlType ctrlType)
	{
		if (ctrlType == null)
		{
			return 1;
		}
		else if (ctrlType == ControlType.ACTIVATION ||
			ctrlType == ControlType.ACTIVATION_ALLOSTERIC ||
			ctrlType == ControlType.ACTIVATION_NONALLOSTERIC ||
			ctrlType == ControlType.ACTIVATION_UNKMECH)
		{
			return 1;
		}
		else if (ctrlType == ControlType.INHIBITION ||
			ctrlType == ControlType.INHIBITION_ALLOSTERIC ||
			ctrlType == ControlType.INHIBITION_COMPETITIVE ||
			ctrlType == ControlType.INHIBITION_IRREVERSIBLE ||
			ctrlType == ControlType.INHIBITION_NONCOMPETITIVE ||
			ctrlType == ControlType.INHIBITION_OTHER ||
			ctrlType == ControlType.INHIBITION_UNCOMPETITIVE ||
			ctrlType == ControlType.INHIBITION_UNKMECH)
		{
			return -1;
		}
		else
		{
			throw new RuntimeException("Unknown control type: " + ctrlType);
		}
	}

	public List<BinaryInteractionType> getRuleTypes()
	{
		return Arrays.asList(
			CO_CONTROL_INDEPENDENT_SIMILAR,
			CO_CONTROL_INDEPENDENT_ANTI,
			CO_CONTROL_DEPENDENT_SIMILAR,
			CO_CONTROL_DEPENDENT_ANTI);
	}

	private static final boolean DEPENDENT = true;
	private static final boolean INDEPENDENT = false;

	private static final int SIMILAR = 1;
	private static final int ANTI = -1;
}

	
