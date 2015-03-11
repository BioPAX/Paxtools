package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;

/**
 * "Clones" a BioPAX element - using direct properties and dependent children only.
 * (shallow copy).
 *
 * Compare to {@link Fetcher}
 *
 * @see org.biopax.paxtools.controller.Visitor
 * @see org.biopax.paxtools.controller.Traverser
 */
public class ShallowCopy implements Visitor
{
	Traverser traverser;

    private BioPAXElement copy;
    private BioPAXLevel level;

	/**
	 * Editor map based constructor.
	 * @param map that determines the BioPAX Level
	 * @deprecated use Level based constructor instead.
	 */
    public ShallowCopy(EditorMap map)
	{
		traverser = new Traverser(map, this);
		this.level = map.getLevel();
	}

	/**
	 * BioPAXLevel based constructor
	 * @param level used for the cloning operation.
	 */
    public ShallowCopy(BioPAXLevel level)
    {
    	this.level = level;
    	traverser = new Traverser(SimpleEditorMap.get(level), this);
    }

	/**
	 * Empty constructos that defaults to BioPAX L3.
	 */
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
		// to avoid unnecessary checks/warnings when existing valid element is being cloned  
		//(e.g., copying BPS.stepProcess values, if there is a Conversion, which was set via stepConversion).
		AbstractPropertyEditor.checkRestrictions.set(false);
		traverser.traverse(source, null);
		AbstractPropertyEditor.checkRestrictions.set(true);//back to default
		return copy;
	}

	
// --------------------- Interface Visitor ---------------------

	public void visit(BioPAXElement domain, Object range, Model model, PropertyEditor editor)
	{
        editor.setValueToBean(range, copy);
	}
}



