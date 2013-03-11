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
 *
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
