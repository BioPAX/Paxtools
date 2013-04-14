package org.biopax.paxtools.util;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.impl.level3.PhysicalEntityImpl;

/**
 * A Filter factory implementation to define
 * the full-text search filter "by data source".
 * It is defined in the {@link PhysicalEntityImpl},
 * but any other (one) indexed entity can be used as well.
 * 
 * @author rodche
 */
public class DataSourceFilterFactory extends BasicFilterFactory{

	public DataSourceFilterFactory() {
		super(BioPAXElementImpl.FIELD_DATASOURCE);
	}
}
