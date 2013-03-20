package org.biopax.paxtools.io.sif.level2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.io.sif.BinaryInteractionType;
import org.biopax.paxtools.io.sif.InteractionSet;
import org.biopax.paxtools.io.sif.MaximumInteractionThresholdExceedException;
import org.biopax.paxtools.io.sif.SimpleInteraction;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.complex;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.biopax.paxtools.io.sif.BinaryInteractionType.COMPONENT_OF;
import static org.biopax.paxtools.io.sif.BinaryInteractionType.IN_SAME_COMPONENT;

/**
 * Component.InSame: A and B are components of same flattened complex structure,
 * A and B are simple. Component.Of: A is component of B, B is complex, A may be
 * nested multiple levels in B.
 * @author Ozgun Babur
 */
public class ComponentRule extends InteractionRuleL2Adaptor
{
	/**
	 * Supported interaction types.
	 */
	private static List<BinaryInteractionType> binaryInteractionTypes =
		Arrays.asList(COMPONENT_OF, IN_SAME_COMPONENT);

	/**
	 * Maximum number of members of a complex to process.
	 */
	private long threshold;

	/**
	 * Log for logging.
	 */
	private static Log log = LogFactory.getLog(ComponentRule.class);

	/**
	 * Option to just skip large complexes without throwing an exception.
	 */
	boolean suppressExceptions;

	/**
	 * Option to mine COMPONENT_OF type.
	 */
	private boolean componentOf;

	/**
	 * Option to mine IN_SAME_COMPONENT type.
	 */
	private boolean inSameComponent;

	/**
	 * Constructor with default values.
	 */
	public ComponentRule()
	{
		this(Integer.MAX_VALUE, false);
	}

	/**
	 * Constructor with threshold
	 * @param threshold limit for the member size of the complex to mine
	 */
	public ComponentRule(int threshold)
	{
		this(threshold, false);
	}

	/**
	 * Constructor with threshold and exception suppressing option.
	 * @param threshold limit for the member size of the complex to mine
	 * @param suppressExceptions if true, does not throw exception for large complexes, just skips
	 * them
	 */
	public ComponentRule(int threshold, boolean suppressExceptions)
	{
		this.threshold = threshold;
		this.suppressExceptions = suppressExceptions;
	}

	/**
	 * Infer starting from the given physicalEntity.
	 * @param interactionSet to be populated
	 * @param A source of the interaction
	 * @param model BioPAX model
	 */
	@Override public void inferInteractionsFromPE(InteractionSet interactionSet, physicalEntity A, Model model)
	{
		if (!(A instanceof complex))
		{

			// Iterate all PEPs of A and process that goes into a complex

			for (physicalEntityParticipant pep : A.isPHYSICAL_ENTITYof())
			{
				if (pep.isCOMPONENTof() != null)
				{
					complex comp = pep.isCOMPONENTof();

					processComplex(interactionSet, A, comp);
				}
			}
		}
	}

	/**
	 * This method is called for each complex that A is in, regardless of the level
	 * of nesting. If it is also detected that this complex is the most outer
	 * complex, then another recursive search is initiated for mining
	 * Component.InSame rule.
	 * @param interactionSet interaction repository
	 * @param A first physical entity
	 * @param comp complex being processed
	 */
	private void processComplex(InteractionSet interactionSet, physicalEntity A, complex comp)
	{

		if (componentOf)
		{
			// Add Component.Of rule
			SimpleInteraction si = new SimpleInteraction(A, comp, COMPONENT_OF);
			si.addMediator(comp);
			interactionSet.add(si);
		}

		// Flag for detecting if this complex is most outer one.
		boolean mostOuter = true;

		// Iterate all PEPs of complex and process that goes into a complex

		for (physicalEntityParticipant pep : comp.isPHYSICAL_ENTITYof())
		{
			if (pep.isCOMPONENTof() != null)
			{
				complex outer = pep.isCOMPONENTof();
				mostOuter = false;
				processComplex(interactionSet, A, outer);
			}
		}

		// Search towards other members only if this is the most outer complex
		// and if options let for sure


		if (mostOuter && inSameComponent)
		{
			// Iterate other members for components_of_same_complex rule
			processComplexMembers(interactionSet, A, comp, 0);
		}
	}

	/**
	 * Recursive method for mining rule Component.InSame. We search towards
	 * children only because we make sure that we start form the most outer
	 * complex.
	 * @param interactionSet repository of rules
	 * @param pe A
	 * @param comp common complex
	 * @param size threshold control
	 */
	private void processComplexMembers(InteractionSet interactionSet, physicalEntity pe, complex comp, int size)
	{


		Set<physicalEntityParticipant> components = comp.getCOMPONENTS();
		if ((size += components.size()) > threshold)
		{
			log.warn("This complex is too large. Skipping");
			if (suppressExceptions) return;
			else throw new MaximumInteractionThresholdExceedException(pe.toString());
		}
		for (physicalEntityParticipant pep : components)
		{
			physicalEntity member = pep.getPHYSICAL_ENTITY();

			if (pe != member)
			{
				if (member instanceof complex)
				{
					// recursive call for inner complex
					processComplexMembers(interactionSet, pe, (complex) member, size);
				} else
				{
					// rule generation for simple member
					SimpleInteraction si = new SimpleInteraction(pe, member, IN_SAME_COMPONENT);
					si.addMediator(comp);
					interactionSet.add(si);
				}
			}
		}
	}

	/**
	 * Gets supported interaction types.
	 * @return supported interaction types
	 */
	public List<BinaryInteractionType> getRuleTypes()
	{
		return binaryInteractionTypes;
	}

	/**
	 * Initializes options.
	 * @param options options map
	 */
	@Override public void initOptionsNotNull(Map options)
	{
		componentOf = !options.containsKey(COMPONENT_OF) || options.get(COMPONENT_OF).equals(Boolean.TRUE);
		inSameComponent =
				!options.containsKey(IN_SAME_COMPONENT) || options.get(IN_SAME_COMPONENT).equals(Boolean.TRUE);

	}
}

