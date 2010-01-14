package org.biopax.paxtools.controller;


import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This is a utility class for traversing over the dependent objects of a biopax
 * element, based on property editors
 */
public class Traverser
{
// ------------------------------ FIELDS ------------------------------

    private final EditorMap editorMap;
    private final Visitor visitor;

// --------------------------- CONSTRUCTORS ---------------------------

    public Traverser(EditorMap editorMap, Visitor visitor)
    {
        this.editorMap = editorMap;
        this.visitor = visitor;
    }

 

// -------------------------- OTHER METHODS --------------------------

    /**
     * Provides {@link Visitor} functionallity regarding the editors'
     * cardinality features. While using all the editors whose domain
     * contains the BioPAX <em>element</em>.
     *  
     * @param element BioPAX element to be traversed
     * @param model model into which <em>element</em> will be traversed
     */
    public void traverse(BioPAXElement element, Model model)
    {
    	if(element == null) {
    		return;
    	}
    	
        Set<PropertyEditor> editors =
                editorMap.getEditorsOf(element);
        for (PropertyEditor editor : editors)
        {
            if (editor instanceof ObjectPropertyEditor)
            {
                if (editor.isMultipleCardinality())
                {
                    Set<BioPAXElement> valueSet = new HashSet<BioPAXElement>(
                            (Collection<? extends BioPAXElement>) editor
                                    .getValueFromBean(element));
                    for (BioPAXElement value : valueSet)
                    {
                        visitor.visit(value, model, editor);
                    }
                }
                else
                {
                    BioPAXElement value =
                            (BioPAXElement) editor.getValueFromBean(element);
                    visitor.visit(value, model, editor);
                }
            }
        }
    }
}
