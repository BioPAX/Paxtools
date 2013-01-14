package org.biopax.paxtools.io.sif.level2;

import org.biopax.paxtools.io.sif.InteractionRule;
import org.biopax.paxtools.io.sif.InteractionSet;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.physicalEntity;

/**
 * This interface defines a rule which can be run on BioPAX model L2 to derive
 * simple interactions. All new rules should implement this interface.
 *
 * @author Emek Demir
 * @author Ozgun Babur
 */
public interface InteractionRuleL2 extends InteractionRule
{
	/**
	 * This method populates the interactionSet with simple interactions that can
	 * be derived from the model based on this rule.
	 * @param interactionSet to be populated
	 * @param entity first physicalEntity
	 * @param model BioPAX model
	 */
	void inferInteractionsFromPE(InteractionSet interactionSet, physicalEntity entity, Model model);
}