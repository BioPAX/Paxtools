package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;

/**
 * Specifically "Clones" the BioPAX elements set
 * (traverses to obtain dependent elements),
 * puts them to the new model using the visitor and traverser framework;
 * ignores elements that are not in the source list (compare to {@link Fetcher})
 *
 * @see org.biopax.paxtools.controller.Visitor
 * @see org.biopax.paxtools.controller.Traverser
 */
public class ShallowCopy implements Visitor
{
	Traverser traverser;

    private BioPAXElement copy;
    private BioPAXLevel level;

    
    public ShallowCopy(EditorMap map)
	{
		traverser = new Traverser(map, this);
		this.level = map.getLevel();
	}

    
    public ShallowCopy(BioPAXLevel l)
    {
        this(SimpleEditorMap.get(l));
    }

    
    public ShallowCopy()
    {
        this(BioPAXLevel.L3);
    }

    
    /**
	 * Creates a copy of the BioPAX object with all its properties
	 * are the same, and also adds it to a model.
	 *
	 * @param model
     * @param source
	 * @param newID
     * @return
	 */
	public <T extends BioPAXElement> T copy(Model model, T source, String newID)
	{
		T copy = copy(source, newID);
		model.add(copy);
		return copy;
    }


	/**
	 * Returns a copy of the BioPAX element 
	 * (with all the property values are same)
	 * 
	 * @param <T>
	 * @param source
	 * @param newID
	 * @return
	 */
	public <T extends BioPAXElement> T copy(T source, String newID) 
	{
		T copy = (T) level.getDefaultFactory().create(
				(Class<T>) source.getModelInterface(), newID);
		this.copy = copy;
		traverser.traverse(source, null);
		return copy;
	}

	
// --------------------- Interface Visitor ---------------------

	public void visit(BioPAXElement domain, Object range, Model model, PropertyEditor editor)
	{
        editor.setValueToBean(range, copy);
	}
}



