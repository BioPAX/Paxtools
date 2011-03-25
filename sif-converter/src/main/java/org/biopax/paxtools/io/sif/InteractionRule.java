package org.biopax.paxtools.io.sif;

import org.biopax.paxtools.model.Model;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This interface defines a rule which can be run on BioPAX model to derive
 * simple interactions. All new rules should implement this interface.  
 * User: demir Date: Dec 28, 2007 Time: 3:49:46 PM
 */
public interface InteractionRule
{
	/**
	 * This method populates the interactionSet with simple interactions that can
	 * be derived from the model based on this rule.
	 * @param interactionSet to be populated
	 * @param entity this must be a physicalEntity for L2, and EntityReference for L3
	 * @param model BioPAX model
	 * @param options
	 */
	public void inferInteractions(
		Set<SimpleInteraction> interactionSet,
		Object entity,
		Model model,
		Map options);

	/**
	 * Gets a list of the rule types that this class implements.
	 * @return supported rules
	 */
	public List<BinaryInteractionType> getRuleTypes();
}
