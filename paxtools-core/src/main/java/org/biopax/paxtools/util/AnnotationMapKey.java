/**
 * 
 */
package org.biopax.paxtools.util;

import org.biopax.paxtools.model.BioPAXElement;

/**
 * Constant names to use
 * with {@link BioPAXElement#getAnnotations()} map.
 * 
 * @author rodche
 * @deprecated this enumeration is not used and probably not required at all.
 */
public enum AnnotationMapKey {
	PARENT_PATHWAYS,
	ORGANISMS,
	;
	
	@Override
	public String toString() {
		return name();
	}
}
