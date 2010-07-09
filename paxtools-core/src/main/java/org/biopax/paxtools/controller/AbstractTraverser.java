package org.biopax.paxtools.controller;

import java.util.ArrayList;
import java.util.List;

import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.controller.Visitor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;

/**
 * This is a convenience all-in-one Traverser/Visitor that
 * keeps track in the model and prevents infinite loops.
 * 
 * @see Fetcher
 * @see Traverser
 * @see Visitor
 * 
 * @author rodch
 * 
 *
 */
public abstract class AbstractTraverser extends Traverser 
	implements Visitor 
{
	private final List<BioPAXElement> currentParentsList;
		
	public AbstractTraverser(EditorMap editorMap, PropertyFilter... filters)
	{
		super(editorMap, null, filters);
		currentParentsList = new ArrayList<BioPAXElement>();
		setVisitor(this);
	}

	protected List<BioPAXElement> getCurrentParentsList() {
		return currentParentsList;
	}
	
	/**
	 * This is to implement a real action here: 
	 * do something, return or continue (traverse)
	 * into properties.
	 * 
	 * @param range is property value
	 * @param domain is parent BioPAX element
	 * @param editor is the property editor
	 */
	protected abstract void visit(Object range, BioPAXElement domain, Model model, PropertyEditor editor);
		
	/**
	 * Saves/restores the current "path" of the value in the model and 
	 * calls the protected abstract method visitValue that is to be 
	 * implemented in subclasses of this abstract class.
	 * 
	 * @param domain BioPAX Element
	 * @param range property value (can be BioPAX element, primitive, enum, string)
	 * @param model 
	 * @param editor parent's property PropertyEditor
	 */
	public void visit(BioPAXElement domain, Object range, Model model, PropertyEditor editor) {
			final List<BioPAXElement> path = getCurrentParentsList();
		
			if(range instanceof BioPAXElement) {
				if(path.contains(range)) {
				    if(log.isWarnEnabled())
					log.warn("Cyclic inclusion of " 
						+ ((BioPAXElement)range).getRDFId() 
						+ " : " + path.toString());
					return;
				}
 
				path.add((BioPAXElement) range);
				
				if(log.isTraceEnabled())
					log.trace("visits " + domain + "." 
						+ editor.getProperty() +
						"=>" + path.toString());
			}
			
			// actions
			visit(range, domain, model, editor);
			
			if(range instanceof BioPAXElement) {
				path.remove(range);
			}
	}

}
