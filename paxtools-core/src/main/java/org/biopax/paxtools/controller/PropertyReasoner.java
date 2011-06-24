package org.biopax.paxtools.controller;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.util.Filter;

/**
 * A tool to set or infer a particular BioPAX
 * property for an object and all its children.
 * For best (consistent) results, it's recommended
 * to generally start from a "root" element, or, at least,
 * - the parent-most one that have given propertyName. 
 * 
 * However, for some (if not - most) BioPAX properties, except for, 
 * e.g., 'dataSource', 'xref', 'organism', 'cellularLocation', -
 * using of this tool does NOT make sense; i.e., one should not normally
 * "infer" (apply to children elements) such properties as 'displayName', 'ph',
 * 'nextStep' (for sure!), etc..
 * 
 * Tip: use with care; write tests for your case!
 *
 * @author rodche
 */
public class PropertyReasoner extends AbstractTraverser
{
	private static final Log LOG = LogFactory.getLog(PropertyReasoner.class);
	
	private String propertyName;
    private final Set<Class<? extends BioPAXElement>> domains;
    private boolean override;
    private final Stack<Set> valueStack;
    private boolean generateComments;
	
	public PropertyReasoner(String propertyName, EditorMap editorMap, 
			Filter<PropertyEditor>... propertyFilters)
	{
        super(editorMap, propertyFilters);
        this.propertyName = propertyName;
        this.domains = new HashSet<Class<? extends BioPAXElement>>();
        this.domains.add(BioPAXElement.class); // default - any
        this.override = false; // do not replace existing values 
        this.valueStack = new Stack<Set>();
        this.generateComments = true;
    }

	
	/**
	 * Constructor,
	 * 
	 * which additionally sets a BioPAX property filter 
	 * for {@link #traverse(BioPAXElement, Model)} to call
	 * {@link #visit(Object, BioPAXElement, Model, PropertyEditor)} 
	 * for each *object* property, except for 'nextStep'.
	 * 
	 * @param property
	 * @param editorMap
	 */
	public PropertyReasoner(String property, EditorMap editorMap) 
	{
        this(property, editorMap, new Filter<PropertyEditor>() {
			public boolean filter(PropertyEditor editor) {
				return (editor instanceof ObjectPropertyEditor)
						&& !editor.getProperty().equals("nextStep")
						&& !editor.getProperty().equals("NEXT-STEP");
			}
		});
    }
	
	

	/**
	 * @return the BioPAX property name
	 */
	public String getPropertyName() {
		return propertyName;
	}


	/**
	 * @param propertyName the BioPAX property name to use
	 */
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}


	/**
	 * When set (not empty list), only instances of the listed types can be
	 * updated by {@link #run(BioPAXElement, Object)} method, although the 
	 * {@link #propertyName} property valueStack of every element (where apply)
	 * can be still considered for its children (of the specified type).
	 * 
	 * @param domains (optional) the types to modify (others won't be affected)
	 */
	public void setDomains(Class<? extends BioPAXElement>... domains) 
	{
		this.domains.clear();
		if (domains.length > 0)
			this.domains.addAll(Arrays.asList(domains));
		else
			this.domains.add(BioPAXElement.class);
	}


	/**
	 * @see #setOverride(boolean)
	 * @see #run(BioPAXElement, Object)
	 * @return the override
	 */
	public boolean isOverride() {
		return override;
	}


	/**
	 * Sets the {@link #override} mode.
	 * 
	 * Following the A,B,C example in the {@link #run(BioPAXElement, Object)}
	 * method:
	 * 
	 * - when {@link #override} is true, and {@link #propertyName} is
	 * a functional BioPAX property: A, B and C all get the X valueStack
	 * TODO ???(if default {@link #valueStack} was null, otherwise - they get the default valueStack).
	 * 
	 * - when {@link #override} is false, and {@link #propertyName} is
	 * a functional BioPAX property: A, B - stay unmodified, C acquires Y valueStack (B's).
	 * 
	 * - when {@link #override} is false, and {@link #propertyName} is
	 * a multiple cardinality property: - B and C will have both X and Y values!
	 * 
	 * @param override the override mode to set (see the above explanation)
	 * 
	 * @see #run(BioPAXElement, Object)
	 */
	protected void setOverride(boolean override) {
		this.override = override;
	}
	
	
	/**
	 * @return the generateComments
	 */
	public boolean isGenerateComments() {
		return generateComments;
	}


	/**
	 * @param generateComments true/false to generate BioPAX comments on all changes
	 */
	public void setGenerateComments(boolean generateComments) {
		this.generateComments = generateComments;
	}


	/**
	 * This traverse method, first, takes care about the 
	 * {@link #propertyName} we are interested in, then proceeds- 
	 * as the basic {@link Traverser#traverse(BioPAXElement, Model)}
	 * would normally do (i.e., - delivering to the method  
	 * {@link #visit(Object, BioPAXElement, Model, PropertyEditor)}
	 * for all properties without any predefined order).
	 */
	@Override
	public void traverse(BioPAXElement bpe, Model model)
	{
		if (bpe == null)
			return;
		
		if(LOG.isTraceEnabled())
			LOG.trace(bpe + ", " + propertyName 
					+ " stack: " + this.valueStack);
		
		PropertyEditor editor = editorMap
			.getEditorForProperty(propertyName, bpe.getModelInterface());
		
		if (editor != null) { // so, property values may be considered for children...
			if (isInstanceofOneOf(domains, bpe)) { // when is allowed to modify
				Set existingValues = editor.getValueFromBean(bpe);
				if (!editor.isMultipleCardinality()) { 
					//this is for a single cardinality prop. -
					Set value = this.valueStack.peek(); 
					
					//thus, both sets are an empty set or singleton!
					assert(value.isEmpty() || value.size()==1);
					assert(existingValues.isEmpty() || existingValues.size()==1);
					
					if (editor.isUnknown(existingValues)) {
						if(!editor.isUnknown(value)) { // skip repl. unknown with unknown
							editor.setValueToBean(value, bpe);
							comment(bpe, value, false);
						}
					} 
					else if(override 
							// and not replicate the same -
							&& !existingValues.equals(value) 
							&& !existingValues.containsAll(value)) 
					{
							editor.setValueToBean(value, bpe);
							comment(bpe, existingValues, true);
							comment(bpe, value, false);
					}
				} 
				else { // - for a multiple cardinality property
					// to add all from the stack
					if(override && !this.valueStack.contains(existingValues)) 
					{ // clear, skipping those to stay
						for (Object v : existingValues) {
								editor.removeValueFromBean(v, bpe);
								comment(bpe, v, true); //removed
						}
					}
					
					// add all new values (sets)
					for (Set v : this.valueStack) {
						if(!existingValues.containsAll(v)) {
							editor.setValueToBean(v, bpe);
							comment(bpe, v, false); // added
						}
					}
				}
			} 

			if (!override) {
				// save current valueStack (does not matter modified or not)
				this.valueStack.push(editor.getValueFromBean(bpe));
			} else {
				// just repeat the same
				this.valueStack.push(this.valueStack.peek());
			}
		}
		
		// continue as usual (to visit every propertyName)
		super.traverse(bpe, model);
		
		if(editor != null) {
			// return to previous parent valueStack/state
			this.valueStack.pop();
		}
	}
	

	/**
	 * Simply, calls {@link #traverse(BioPAXElement, Model)} 
	 * and goes deeper when the propertyName's range/valueStack is a BioPAX object.
	 */
	@Override
    protected void visit(Object range, BioPAXElement bpe, 
    		Model model, PropertyEditor editor) 
	{    	
    	if (range instanceof BioPAXElement)
		{
    		traverse((BioPAXElement) range, model);
		}
	}

	
	/**
	 * Adds a special comment for a BioPAX element 
	 * when one of its propertyName values was
	 * inferred from parent's.
	 * 
	 * @param bpe BioPAX object
	 * @param v value
	 * @param unset whether it's about removed/added valueStack (true/false)
	 */
	private void comment(BioPAXElement bpe, Object v, boolean unset) 
	{
		if(!generateComments) {
			if(LOG.isDebugEnabled())
				LOG.debug("'comment' is called; won't write any BioPAX comments (generateComments==false)");
			return;
		}
		
		PropertyEditor pe = editorMap.getEditorForProperty("comment", bpe.getModelInterface());
		
		if(pe == null) { // L2?
			pe = editorMap.getEditorForProperty("COMMENT", bpe.getModelInterface());
		}
		
		if(pe != null) {
			String msg;
			
			if(pe.isUnknown(v)) {
				return; // no need to comment on removal or adding of "unknown"
			} 
			
			
			Object val = (!pe.isMultipleCardinality() && v instanceof Set) 
				? ((Set)v).iterator().next() : v;
			
			if(unset){ // unset==true, v is not 'unknown'
				msg = propertyName + " REMOVED by a reasoner: " + list(val);
			} else {
				msg = propertyName + " ADDED by a reasoner: " + list(val);
			}
			
			pe.setValueToBean(msg, bpe);
			
			if(LOG.isDebugEnabled())
				LOG.debug("BioPAX comment generated: " + msg);
		}
	}

	
	private String list(Object val) {
		StringBuffer sb = new StringBuffer();
		
		if(val instanceof Set) {
			for(Object o : (Set) val) {
				sb.append(list(o)).append(" ");
			}
		} else if(val instanceof BioPAXElement) {
			sb.append(((BioPAXElement)val).getRDFId());
		} else {
			sb.append(val);
		}
		
		return sb.toString();
	}


	private static boolean isInstanceofOneOf(
		final Collection<Class<? extends BioPAXElement>> classes, 
		BioPAXElement obj) 
	{
		for(Class<? extends BioPAXElement> c : classes) {
			if(c.isInstance(obj)) {
				return true;
			}
		}	
		return false;
	}
	
   
	/**
	 * Basic, universal method (used by others in this class) -
	 * 
	 * updates the {@link #propertyName} valueStack of a BioPAX element
	 * and its children. The root element (first parameter) does not
	 * necessarily have the propertyName though.
	 * 
	 * For example, if X is the {@link #propertyName} valueStack of A; Y - of B;
	 * and C has unknown valueStack; and A has "child" B, which in turn has C
	 * (not necessarily immediate); and A, B, C are instances of 
	 * one of classes listed in {@link #domains} (if any), then 
	 * the following results are expected
	 * (note: even if, e.g., B would not pass the domains filter, 
	 * the results for A and C will be the same):
	 * 
	 * - valueStack X will be considered a replacement/addition to Y for B,
	 * and both X and Y - for C. The result depends on {@link #override}, 
	 * the default {@link #valueStack} and the propertyName's cardinality.
	 * 
	 * A default valueStack (or a {@link Set} or values, object or primitive)
	 * is to apply when both current and parent's propertyName values are
	 * yet unknown. However, if {@link #override} is true, the default
	 * valueStack, if given, will unconditionally replace all existing.
	 * 
	 * Warning: when defaultValue is null or empty set, and override==true, 
	 * it will clear the propertyName values of all corresponding elements.
	 * 
	 * @see #setDomains(Class...)
	 * @see #setOverride(boolean)
	 *  
	 * @param element
	 * @param defaultValue a default valueStack or set of values
	 */
    protected void run(BioPAXElement element, Object defaultValue)
	{
    	valueStack.clear();
		// default
    	Set valueInSet; 
		if(defaultValue instanceof Set) {
			valueInSet = (Set) defaultValue;
		}
		else if(defaultValue != null) {
			valueInSet =  Collections.singleton(defaultValue);
		}
		else if(isOverride()) {
			valueInSet = Collections.singleton(null);
		} else {
			valueInSet = Collections.EMPTY_SET; 
		}
				
    	valueStack.push(valueInSet);
    	traverse(element, null);
	}
    
    /**
     * For the element and its children,
     * where it's desired (in {@link #domains}) and possible, 
     * it sets the {@link #propertyName} values to "unknown".
     * 
     * @see PropertyReasoner#run(BioPAXElement, Object)
     * 
     * @param element
     */
    public void clearProperty(BioPAXElement element)
	{
    	boolean override = isOverride();
    	setOverride(true);
    	run(element, null);
    	setOverride(override);
	}
    
    
    /**
     * For the element and its children,
     * where it's desired (in {@link #domains}) and allowed (by BioPAX), 
     * it forces the given {@link #propertyName} valueStack replace existing ones.
     *  
     * @see PropertyReasoner#run(BioPAXElement, Object)
     *  
     * @param element
     * @param defaultValue
     */
    public void resetPropertyValue(BioPAXElement element, Object defaultValue)
	{
    	if(defaultValue == null) {
    		throw new NullPointerException("Consider using " +
    			"clearProperty() instead if you really want " +
    			"to set this propertyName to null/unknown for the " +
    			"object and its children!");
    	}
    	
    	boolean override = isOverride();
    	setOverride(true);
    	run(element, defaultValue);
    	setOverride(override);
	}
    
    
    /**
     * For the element and its children,
     * where it's empty, desired (in {@link #domains}) and allowed, 
     * it adds or sets the {@link #propertyName} from parents's 
     * (if the top-most, a parent element that has this propertyName,
     * does not have any valueStack, then given valueStack will be set, and
     * children may inherit it)
     * 
     * @see PropertyReasoner#run(BioPAXElement, Object)
     * 
     * @param element
     * @param addValue
     */
    public void inferPropertyValue(BioPAXElement element, Object addValue)
	{
    	boolean override = isOverride();
    	setOverride(false);
    	run(element, addValue);
    	setOverride(override);
	}
    
    
    /**
     * For the element and its children,
     * where it's empty, desired (in {@link #domains}) and allowed, 
     * it adds or sets the {@link #propertyName} from parents's 
     * (if they have any valueStack)
     * 
     * @see PropertyReasoner#run(BioPAXElement, Object)
     * 
     * @param element
     */
    public void inferPropertyValue(BioPAXElement element)
	{
    	boolean override = isOverride();
    	setOverride(false);
    	run(element, null);
    	setOverride(override);
	}
}

