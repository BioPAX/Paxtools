package org.biopax.paxtools.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
 */
public abstract class AbstractTraverser extends Traverser implements Visitor
{
	private final static Log log = LogFactory.getLog(AbstractTraverser.class);
	protected final Stack<BioPAXElement> path;
		
	public AbstractTraverser(EditorMap editorMap, 
		@SuppressWarnings("rawtypes") Filter<PropertyEditor>... filters)
	{
		super(editorMap, null, filters);
		setVisitor(this);
		this.path = new Stack<BioPAXElement>();
	}

	protected Stack<BioPAXElement> getVisited() { return path; }

	/**
	 * This is to implement a real action here: 
	 * do something, return or even to continue (traverse)
	 * into the child (range) element's properties if it's a BioPAX object.
	 * 
	 * @param range property value
	 * @param domain parent/owner BioPAX element
	 * @param model the BioPAX model of interest
	 * @param editor the property editor
	 */
	protected abstract void visit(Object range, BioPAXElement domain, Model model, PropertyEditor<?,?> editor);
		
	/**
	 * Calls the protected abstract method visit that is to be
	 * implemented in subclasses of this abstract class.
	 * 
	 * @param domain BioPAX Element
	 * @param range property value (can be BioPAX element, primitive, enum, string)
	 * @param model the BioPAX model of interest
	 * @param editor parent's property PropertyEditor
	 */
	public void visit(BioPAXElement domain, Object range, Model model, PropertyEditor<?,?> editor) {
		// actions
		visit(range, domain, model, editor);
	}


	@Override
	public <D extends BioPAXElement> void traverse(D element, Model model) {
		if(!path.contains(element)) {
			path.push(element);
			super.traverse(element, model);//calls visit method for each property value, taking prop. filters into acc.
			path.pop();
		} else {
			log.debug("Escaped a loop: " + path.toString() + ", again " + element.getRDFId());
		}
	}

	/**
	 * Clears the state from the last use.
	 */
	public void reset() {
		path.clear();
	}
}
