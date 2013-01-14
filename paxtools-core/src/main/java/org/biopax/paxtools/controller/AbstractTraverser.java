package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.util.Filter;

import java.util.Stack;

/**
 * This is an all-in-one Traverser/Visitor combination 
 * to use when deep recursive visiting is required. 
 * Unlike {@link Traverser}, it keeps track of where current 
 * object is in the model and whether it's been already visited, 
 * which helps prevent infinite loops.
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
	private final Stack<String> props;
		
	public AbstractTraverser(EditorMap editorMap, 
		@SuppressWarnings("rawtypes") Filter<PropertyEditor>... filters)
	{
		super(editorMap, null, filters);
		visited = new Stack<BioPAXElement>();
		props = new Stack<String>();
		setVisitor(this);
	}

	protected Stack<BioPAXElement> getVisited() {
		return visited;
	}
	
	
	public Stack<String> getProps() {
		return props;
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
	protected abstract void visit(Object range, BioPAXElement domain, Model model, PropertyEditor<?,?> editor);
		
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
	public void visit(BioPAXElement domain, Object range, Model model, PropertyEditor<?,?> editor) {
			final Stack<BioPAXElement> objPath = getVisited();
			final Stack<String> propsPath = getProps();
		
			if(range instanceof BioPAXElement) {
				if(objPath.contains(range)) {
				    if(log.isInfoEnabled())
				    	log.info(((BioPAXElement)range).getRDFId() 
				    		+ " already visited (cycle!): " + objPath.toString());
					return;
				}
 
				objPath.push((BioPAXElement) range);
			}
			
			propsPath.push(editor.getProperty());
			
			// actions
			visit(range, domain, model, editor);
			
			propsPath.pop();
			
			if(range instanceof BioPAXElement) {
				objPath.pop();
			}
	}

	
	/**
	 * Clears the state (traversed path and objects) from the last run.
	 */
	public void reset() {
		visited.clear();
		props.clear();
	}
	
}
