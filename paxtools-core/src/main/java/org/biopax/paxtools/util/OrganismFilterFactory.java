package org.biopax.paxtools.util;

import org.biopax.paxtools.impl.BioPAXElementImpl;

/**
 * @author rodche //TODO Annotate
 */
public class OrganismFilterFactory extends BasicFilterFactory{
	
	public OrganismFilterFactory() {
		super(BioPAXElementImpl.FIELD_ORGANISM);
	}
	
}
