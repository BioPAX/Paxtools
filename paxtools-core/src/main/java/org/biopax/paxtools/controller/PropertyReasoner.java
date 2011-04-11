package org.biopax.paxtools.controller;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.*;

/**
 * A tool to set or infer a particular BioPAX
 * property for an object and all its children.
 * 
 * Tip: this (or similar) tool can be also used to:
 * "enrich" existing BioPAX data with supplementary metadata,
 * e.g., rdfs:seeAlso, comments, inferred names, xrefs, etc.!
 *
 * @author rodche
 */
public class PropertyReasoner extends AbstractTraverser
{
	private static final Log LOG = LogFactory.getLog(PropertyReasoner.class);
	
	private final String property;
    private final Set<Class<? extends BioPAXElement>> domains;
    private boolean override;
    private final Stack<Object> value;
	
	public PropertyReasoner(String property, EditorMap editorMap, 
			PropertyFilter... filters) 
	{
        super(editorMap, filters);
        this.property = property;
        this.domains = new HashSet<Class<? extends BioPAXElement>>();
        this.domains.add(BioPAXElement.class); // default - any
        this.override = false; // do not replace existing values 
        this.value = new Stack<Object>();
    }
 
	
	/**
	 * When set (not empty list), only instances of the listed types can be
	 * updated by {@link #run(BioPAXElement, Object)} method, although the {@link #property}
	 * value of every element (where apply) can be still considered for its
	 * children (of the specified type).
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
	 * - when {@link #override} is true, and {@link #property} is
	 * a functional BioPAX property: A, B and C all get the X value
	 * (if default {@link #value} was null, otherwise - they get the default value).
	 * 
	 * - when {@link #override} is false, and {@link #property} is
	 * a functional BioPAX property: A, B - stay unmodified, C acquires Y value (B's).
	 * 
	 * - when {@link #override} is false, and {@link #property} is
	 * a multiple cardinality property: - B and C will have both X and Y values!
	 * 
	 * 
	 * @param override the override mode to set (see the above explanation)
	 * 
	 * @see #run(BioPAXElement, Object)
	 */
	public void setOverride(boolean override) {
		this.override = override;
	}

	
	/**
	 * This traverse method, first, takes care about the 
	 * {@link #property} we are interested in, then proceeds- 
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
		
		if(LOG.isDebugEnabled())
			LOG.debug(bpe + ", " + property 
					+ " stack: " + this.value);
		
		PropertyEditor editor = editorMap
			.getEditorForProperty(property, bpe.getModelInterface());
		if (editor != null) { // will consider its value
			boolean isMul = editor.isMultipleCardinality();
			if (isInstanceofOneOf(domains, bpe)) { // - allowed to modify
				Object existing = editor.getValueFromBean(bpe);
				if (!isMul) {
					Object value = this.value.peek();
					if (editor.isUnknown(existing)) {
						editor.setValueToBean(value, bpe);
						comment(bpe, value, false);
					} else if(override) {
						editor.setValueToBean(value, bpe);
						comment(bpe, existing, true);
						comment(bpe, value, false);
					}
				} else { // multiple cardinality
					if(override) { // clear existing
						for (Object v : (Set) existing) {
							editor.removeValueFromBean(v, bpe);
							comment(bpe, v, true);
						}
					}
					
					// add all new values from stack
					for (Object values : this.value) {
						// add values (parents's or default)
						if (values != null) {
							for (Object v : (Set) values) {
								if (!((Set) existing).contains(v)) {
									editor.setValueToBean(v, bpe);
									comment(bpe, v, false);
								}
							}
						}
					}
				}
			} 

			if (!override) {
				// save current value (does not matter modified or not)
				Object existing = editor.getValueFromBean(bpe);
				if (isMul) {
					this.value.push(new HashSet((Set) existing));
				} else {
					this.value.push(existing);
				}
			} else {
				// just repeat the same
				this.value.push(this.value.peek());
			}
		}
		
		// continue as usual (to visit every property)
		super.traverse(bpe, model);
		
		if(editor != null) {
			// return to previous parent value/state
			this.value.pop();
		}
	}
	

	/**
	 * Simply, calls {@link #traverse(BioPAXElement, Model)} 
	 * and goes deeper when the property's range/value is a BioPAX object.
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
	 * when one of its property values was
	 * inferred from parent's.
	 * 
	 * @param bpe BioPAX object
	 * @param v value
	 * @param property BioPAX propertyname
	 * @param unset whether it's about removed/added value (true/false)
	 */
	private void comment(BioPAXElement bpe, Object v, boolean unset) 
	{
		PropertyEditor pe = editorMap.getEditorForProperty("comment", bpe.getModelInterface());
		if(pe == null) // L2?
			pe = editorMap.getEditorForProperty("COMMENT", bpe.getModelInterface());
		
		if(pe != null) {
			String msg = ((unset)? "REMOVED" : "ADDED")
				+ " by a reasoner: " + property + ", value: " 
				+ ((v instanceof BioPAXElement)? "rdfID=" 
				+ ((BioPAXElement)v).getRDFId() : v);
			pe.setValueToBean(msg, bpe);
			if(LOG.isDebugEnabled())
				LOG.debug(msg);
		}
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
	 * Updates the {@link #property} value of a BioPAX element and 
	 * its children. The root element (first parameter) 
	 * does not necessarily have the property though.
	 * 
	 * For example, if X is the {@link #property} value of A; Y - of B; 
	 * and C has unknown value; and A has "child" B, which in turn has C 
	 * (not necessarily immediate); and A, B, C are instances of 
	 * one of classes listed in {@link #domains} (if any), then 
	 * the following results are expected
	 * (note: even if, e.g., B would not pass the domains filter, 
	 * the results for A and C will be the same):
	 * 
	 * - value X will be considered a replacement/addition to Y for B, 
	 * and both X and Y - for C. The result depends on {@link #override}, 
	 * the default {@link #value} and the property's cardinality.
	 * 
	 * A default value (or a {@link Set} or values, object or primitive) 
	 * is to apply when both current and parent's property values are
	 * yet unknown. However, if {@link #override} is true, the default
	 * value, if given, will unconditionally replace all existing.
	 * 
	 * Warning: when defaultValue==null and override==true, it will
	 * clear the property values of all corresponding elements.
	 * 
	 * @see #setDomains(Class...)
	 * @see #setOverride(boolean)
	 *  
	 * @param element
	 * @param defaultValue a default value or set of values
	 */
    public void run(BioPAXElement element, Object defaultValue)
	{
    	value.clear();
    	value.push(defaultValue);
    	traverse(element, null);
	}
}

