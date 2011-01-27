package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;

/**
 * A controller that excises/extracts an element and all the elements it is dependent on
 * from a model and adds them into a new model.
 */
public class Excisor implements Visitor
{
    private Model targetModel;
    private Traverser traverser;
    private EditorMap editorMap;

    public Excisor(EditorMap editorMap)
    {
        this.editorMap = editorMap;
        this.traverser = new Traverser(editorMap, this);
    }

    public void visit(BioPAXElement domain, Object range, Model model, PropertyEditor editor)
    {
        // We are only interested in the BioPAXElements since
        // the other ones are handled
        if(range != null && range instanceof BioPAXElement)
        {
            BioPAXElement bpe = (BioPAXElement) range;

            if(!targetModel.contains(bpe))
            {
                targetModel.add(bpe);
                traverser.traverse(bpe, model);
            }
        }
    }

    public Model excise(Model sourceModel, String... ids)
    {
        // Create a new model that will contain the element(s) of interest
        this.targetModel = editorMap.getLevel().getDefaultFactory().createModel();

        for(String id: ids)
        {
            // Get the BioPAX element
            BioPAXElement bpe = sourceModel.getByID(id);
            // Add it to the model
            targetModel.add(bpe);
            // Add the elements that bpe is dependent on
            traverser.traverse(bpe, sourceModel);
        }

        return targetModel;
    }

}
