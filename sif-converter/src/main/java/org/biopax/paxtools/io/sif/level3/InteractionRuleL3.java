package org.biopax.paxtools.io.sif.level3;

import org.biopax.paxtools.io.sif.InteractionRule;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.PhysicalEntity;

/**
 * This interface defines a rule which can be run on BioPAX model L2 to derive
 * simple interactions. All new rules should implement this interface.
 *
 * @author Emek Demir
 * @author Ozgun Babur
 */
public interface InteractionRuleL3 extends InteractionRule
{
	/**
	 * This method populates the interactionSet with simple interactions that can
	 * be derived from the model based on this rule.
	 * @param interactionSet to be populated
	 * @param pe PhysicalEntity that will be the seed of the inference
	 * @param model BioPAX model
	 */
	void inferInteractionsFromPE(InteractionSetL3 interactionSet, PhysicalEntity pe, Model model);
}