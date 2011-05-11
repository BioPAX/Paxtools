package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.util.Filter;

import java.util.Stack;

/**
 * This is a convenience all-in-one Traverser/Visitor that
 * keeps track in the model and prevents infinite loops.
 * Like it's for the {@link Traverser}, there is no any 
 * particular order in which it processes properties.
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
	private final Stack<BioPAXElement> visited;
		
	public AbstractTraverser(EditorMap editorMap, Filter<PropertyEditor>... filters)
	{
		super(editorMap, null, filters);
		visited = new Stack<BioPAXElement>();
		setVisitor(this);
	}

	protected Stack<BioPAXElement> getVisited() {
		return visited;
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
			final Stack<BioPAXElement> path = getVisited();
		
			if(range instanceof BioPAXElement) {
				if(path.contains(range)) {
				    if(log.isInfoEnabled())
				    	log.info(((BioPAXElement)range).getRDFId() 
				    		+ " already visited (cycle!): " + path.toString());
					return;
				}
 
				path.push((BioPAXElement) range);
				
				if(log.isTraceEnabled())
					log.trace("visits " + domain + "." 
						+ editor.getProperty() +
						"=>" + path.toString());
			}
			
			// actions
			visit(range, domain, model, editor);
			
			if(range instanceof BioPAXElement) {
				path.pop();
			}
	}
	
}
