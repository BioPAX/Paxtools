package org.biopax.paxtools.io.sif;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;

import java.util.List;
import java.util.Map;

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
	 * @param set to be populated
	 * @param entity this must be a physicalEntity for L2, and PhysicalEntity for L3
	 * @param model BioPAX model
	 */
	public void inferInteractions(
		InteractionSet  set,
		BioPAXElement entity,
		Model model);

	/**
	 * Gets a list of the rule types that this class implements.
	 * @return supported rules
	 */
	public List<BinaryInteractionType> getRuleTypes();

	public void initOptions(Map options);
}
