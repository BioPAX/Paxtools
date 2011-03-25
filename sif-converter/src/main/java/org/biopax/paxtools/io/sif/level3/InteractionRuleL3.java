package org.biopax.paxtools.io.sif.level3;

import org.biopax.paxtools.io.sif.InteractionRule;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.EntityReference;

import java.util.Map;
import java.util.Set;

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
	 * @param entRef first EntityReference
	 * @param model BioPAX model
	 * @param options
	 */
	public void inferInteractions(
		Set<SimpleInteraction> interactionSet,
		EntityReference entRef,
		Model model,
		Map options);
}