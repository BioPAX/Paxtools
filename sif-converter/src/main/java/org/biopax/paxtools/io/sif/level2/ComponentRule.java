package org.biopax.paxtools.io.sif.level2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.io.sif.BinaryInteractionType;
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
 *
 * @author Ozgun Babur
 */
public class ComponentRule implements InteractionRuleL2
{
    private long threshold;
    private static Log log = LogFactory.getLog(ComponentRule.class);
    boolean suppressExceptions;

    public ComponentRule()
    {
        this(Integer.MAX_VALUE, false);
    }

    public ComponentRule(int threshold)
    {
        this(threshold, false);
    }

    public ComponentRule(int threshold, boolean suppressExceptions)
    {
      this.threshold = threshold;
      this.suppressExceptions = suppressExceptions;
    }


    public void inferInteractions(Set<SimpleInteraction> interactionSet,
		Object entity,
		Model model, Map options)
	{
		inferInteractions(interactionSet, ((physicalEntity) entity), model, options);
	}

    public void inferInteractions(Set<SimpleInteraction> interactionSet,
                                  physicalEntity A,
                                  Model model, Map options) {
        if (A instanceof complex) {
            // All rules are invalid when A is not simple, so just return.
            return;
        }

        // Iterate all PEPs of A and process that goes into a complex

        for (physicalEntityParticipant pep : A.isPHYSICAL_ENTITYof()) {
            if (pep.isCOMPONENTof() != null) {
                complex comp = pep.isCOMPONENTof();

                processComplex(interactionSet, A, comp, options);
            }
        }
    }

    /**
     * This method is called for each complex that A is in, regardless of the level
     * of nesting. If it is also detected that this complex is the most outer
     * complex, then another recursive search is initiated for mining
     * Component.InSame rule.
     *
     * @param interactionSet interaction repository
     * @param A              first physical entity
     * @param options        options map
     * @param comp           complex being processed
     */
    private void processComplex(Set<SimpleInteraction> interactionSet,
                                physicalEntity A,
                                complex comp, Map options) {
        if (!options.containsKey(COMPONENT_OF) ||
                options.get(COMPONENT_OF).equals(Boolean.TRUE)) {
            // Add Component.Of rule
            SimpleInteraction si = new SimpleInteraction(A, comp, COMPONENT_OF);
            si.addMediator(comp);
            interactionSet.add(si);
        }

        // Flag for detecting if this complex is most outer one.
        boolean mostOuter = true;

        // Iterate all PEPs of complex and process that goes into a complex

        for (physicalEntityParticipant pep : comp.isPHYSICAL_ENTITYof()) {
            if (pep.isCOMPONENTof() != null) {
                complex outer = pep.isCOMPONENTof();
                mostOuter = false;
                processComplex(interactionSet, A, outer, options);
            }
        }

        // Search towards other members only if this is the most outer complex
        // and if options let for sure

        if (mostOuter && (!options.containsKey(IN_SAME_COMPONENT) ||
                options.get(IN_SAME_COMPONENT).equals(Boolean.TRUE))) {
            // Iterate other members for components_of_same_complex rule
            processComplexMembers(interactionSet, A, comp, 0);
        }
    }

    /**
     * Recursive method for mining rule Component.InSame. We search towards
     * children only because we make sure that we start form the most outer
     * complex.
     *
     * @param interactionSet repository of rules
     * @param pe             A
     * @param comp           common complex
     * @param size           threshold control
     */
    private void processComplexMembers(Set<SimpleInteraction> interactionSet,
                                       physicalEntity pe,
                                       complex comp, int size) {


        Set<physicalEntityParticipant> components = comp.getCOMPONENTS();
        if((size +=components.size()) >threshold)
        {
            log.warn("This complex is too large. Skipping");
            if(suppressExceptions)
                        return;
            else
                throw new MaximumInteractionThresholdExceedException(pe.toString());
        }
        for (physicalEntityParticipant pep : components) {
            physicalEntity member = pep.getPHYSICAL_ENTITY();

            if (pe != member) {
                if (member instanceof complex) {
                    // recursive call for inner complex
                    processComplexMembers(interactionSet, pe, (complex) member, size);
                } else {
                    // rule generation for simple member
                    SimpleInteraction si = new SimpleInteraction(pe,
                            member,
                            IN_SAME_COMPONENT);
                    si.addMediator(comp);
                    interactionSet.add(si);
                }
            }
        }
    }

    public List<BinaryInteractionType> getRuleTypes() {
        return Arrays.asList(COMPONENT_OF, IN_SAME_COMPONENT);
    }

}

