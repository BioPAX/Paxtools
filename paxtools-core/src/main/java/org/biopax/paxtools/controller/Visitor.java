package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;

/**
 * Basic visitor interface for classes using Traverser.
 */
public interface Visitor
{
// -------------------------- OTHER METHODS --------------------------

    /**
     * An implementation of this method should perform a BioPAX element
     * and editor dependent operation on the <em>model</em>. Examples of uses of this
     * method include adding a BioPAX element--with all its dependent
     * elements that can be reached via the <em>editor</em>--into the model, or
     * updating an element that is equivalent to the given BioPAX element
     * using the latter's/former's values.
     *
     * @param domain BioPAXElement which is the domain of this property
     * @param range  Object which is the range of this property
     * @param model model on which the visiting operation will be done
     * @param editor editor
     */
    public void visit(BioPAXElement domain, Object range, Model model, PropertyEditor editor);
}
