package org.biopax.paxtools.controller;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.controller.Visitor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;

/**
 * This is another (advanced) "traverser" that "visits" both 
 * 'object' and 'data' properties and also keeps track of its path 
 * through the model.
 * 
 * There are tasks where this class might be very useful, and 
 * where it replaces the Fetcher, giving much more control 
 * over actions, elements, and properties.
 * 
 * @see Fetcher
 * @see Traverser
 * @see Visitor
 * 
 * @author rodch
 *
 */
public abstract class AbstractTraverser {
	protected final Log log;
	protected BioPAXElement start;
	protected String path = ""; // current path within model
	private final EditorMap editorMap;
		
	public AbstractTraverser(EditorMap editorMap) {
		log = LogFactory.getLog(this.getClass());
		this.editorMap = editorMap;
	}
	
	/**
	 * This is to implement a real action here, 
	 * e.g., report errors and/or continue
	 * going into property values.
	 * 
	 * @param value property value
	 * @param parent BioPAX element
	 * @param editor
	 */
	protected abstract void visitValue(Object value, BioPAXElement parent, Model model, PropertyEditor editor);
		

	/**
	 * Saves/restores the current "path" of the value in the model and 
	 * calls the protected abstract method visitValue that is to be 
	 * implemented in subclasses of this abstract class.
	 * 
	 * @param val property value (can be BioPAX element, primitive, enum, string)
	 * @param parent BioPAX Element
	 * @param model 
	 * @param editor parent's property PropertyEditor
	 */
	public void visit(Object val, BioPAXElement parent, Model model, PropertyEditor editor) {
			String oldPath = path; // save the current path
			path += "." + editor.getProperty() + "=" + val;
			visitValue(val, parent, model, editor); // does the job!
			path = oldPath; // reset the previous path
	}
		
	/**
	 * Starts default traversing/visiting
	 * 
	 * @param model
	 * @return can be overridden or ignored
	 */
	public boolean run(BioPAXElement start, Model model) {
		this.start = start;
		path = "";
		if(start != null) {
			traverse(start, model);
		} else if (model != null){
			for(BioPAXElement e : model.getObjects()) {
				traverse(e, model);
			}
		}
		return true;
	}
	
    /**
	 * Provides {@link Visitor} functionality regarding the editors'
	 * cardinality features using all the property editors of 
	 * the BioPAX <em>element</em>.
	 * 
	 * @param element BioPAX element to be traversed
	 * @param model model into which <em>element</em> will be traversed
	 */
	protected void traverse(BioPAXElement element, Model model) {
		if (element == null) {
			return;
		}
		Set<PropertyEditor> editors = editorMap.getEditorsOf(element);
		if(editors == null) {
			log.warn("No editors for : " + element.getModelInterface());
			return;
		}
			
		for (PropertyEditor editor : editors) {
			if (editor.isMultipleCardinality()) {
				for (Object value : 
						(Collection) editor.getValueFromBean(element)) {
					visitValue(value, element, model, editor);
				}
			} else {
				Object value = editor.getValueFromBean(element);
				visitValue(value, element, model, editor);
			}
		}
	}

}
