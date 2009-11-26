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
     * @param bpe BioPAX element to be used for the operation on <em>model</em>
     * @param model model on which the visitting operation will be done
     * @param editor editor for accessing the properties/values of the BioPAX element
     */
    public void visit(BioPAXElement bpe, Model model, PropertyEditor editor);
}
