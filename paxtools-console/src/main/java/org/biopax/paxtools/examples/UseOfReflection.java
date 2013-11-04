/**
 * 
 */
package org.biopax.paxtools.examples;

import java.util.HashSet;
import java.util.Set;

import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.model.BioPAXElement;

/**
 * Examples on how to use Paxtools BioPAX property editors and accessors API
 * (based on java reflection).
 * 
 * @author rodche
 * 
 */
public final class UseOfReflection {

	/**
	 * Example 1.
	 * 
	 * How to get values from an object biopax property if the type of the biopax object
	 * is not known at runtime, and you do not want to always remember the
	 * domain and range of the property nor write many if-else statements to
	 * find out.
	 * 
	 * @param entity
	 * @param organisms
	 */
	public static Set<? extends BioPAXElement> getObjectBiopaxPropertyValues(BioPAXElement bpe, String property) {
		Set<BioPAXElement> values = new HashSet<BioPAXElement>();

		// get the BioPAX L3 property editors map
		EditorMap em = SimpleEditorMap.L3;

		// get the 'organism' biopax property editor, 
		// if exists for this type of bpe
		@SuppressWarnings("unchecked") PropertyEditor<BioPAXElement, BioPAXElement> editor
			= (PropertyEditor<BioPAXElement, BioPAXElement>) em
				.getEditorForProperty(property, bpe.getModelInterface());

		// if the biopax object does have such property, get values
		if (editor != null) {
			return editor.getValueFromBean(bpe);
		} else 
			return values;
	}
	
	
	
	/**
	 * Example 2.
	 * 
	 * How to get values from a biopax property if the type of the biopax object
	 * is not known at runtime, and you do not want to always remember the
	 * domain and range of the property nor write many if-else statements to
	 * find out.
	 * 
	 * @param entity
	 * @param organisms
	 */
	public static Set getBiopaxPropertyValues(BioPAXElement bpe, String property) {

		// get the BioPAX L3 property editors map
		EditorMap em = SimpleEditorMap.L3;

		// get the 'organism' biopax property editor, 
		// if exists for this type of bpe
		@SuppressWarnings("unchecked") PropertyEditor<BioPAXElement, Object> editor
			= (PropertyEditor<BioPAXElement, Object>) em
				.getEditorForProperty(property, bpe.getModelInterface());

		// if the biopax object does have such property, get values
		if (editor != null) {
			return editor.getValueFromBean(bpe);
		} else 
			return null;
	}
	
	
}
