package org.biopax.paxtools.util;


import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.ModelUtils;
import org.biopax.paxtools.controller.ObjectPropertyEditor;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.hibernate.search.bridge.StringBridge;

import java.util.Set;

/**
 * Custom string bridge implementation. 
 * Recursively index and store 
 * (if Store.YES was specified in the corresponding field annotation) 
 * all child elements's names/comments to parent's document (index).
 * This perhaps creates the largest and least specific index field 
 * (e.g., all children's sequence and comments will be processed/included)
 * For better search results, @Boost other (true BioPAX properties) fields
 * in the model, such as 'name', 'xref', etc. 
 * 
 * The generated index field can be used to find, e.g., a protein or 
 * interaction by db identifier or entity reference/gene name!   
 */
public class ChildDataStringBridge implements StringBridge {

	ModelUtils modelUtils = new ModelUtils(BioPAXLevel.L3);
	EditorMap editorMap = SimpleEditorMap.get(BioPAXLevel.L3);
	
	public String objectToString(Object object) {
		// string to return
		StringBuffer sb = new StringBuffer();

		if(object instanceof BioPAXElement) {
			indexWithAllChildren((BioPAXElement) object, sb);
		} else if (object instanceof Set) {
			//it's a set (otherwise, exception is thrown, - means illegal use of this bridge)
			Set<BioPAXElement> items = (Set<BioPAXElement>)object;
			for(BioPAXElement bpe : items) {
				indexWithAllChildren(bpe, sb);
			}
		}

		return sb.toString();
	}
	
	
	public void indexWithAllChildren(BioPAXElement value, StringBuffer sb) {
		index(value, sb);
		// collect this and its child biopax objects
		Set<BioPAXElement> elms = modelUtils.getAllChildren(value).getObjects();
		for(BioPAXElement bpe : elms) {
			index(bpe, sb);
		}	
	}
	
	
	public void index(BioPAXElement bpe, StringBuffer sb) {
		// all data type property values become (indexed) keywords
		Set<PropertyEditor> props = editorMap.getEditorsOf(bpe);
		for (PropertyEditor pe : props) {
			if (pe instanceof ObjectPropertyEditor)
				continue;
			Set values = pe.getValueFromBean(bpe);
			for (Object v : values) {
				if (!pe.isUnknown(v)) {
					sb.append(v.toString()).append(" ");
				}
			}
		}
	}
	
}

