package org.biopax.paxtools.util;

import org.biopax.paxtools.impl.BioPAXElementImpl;

/**
 * @author rodche //TODO annotate
 */
public class DataSourceFilterFactory extends BasicFilterFactory{

	public DataSourceFilterFactory() {
		super(BioPAXElementImpl.FIELD_DATASOURCE);
	}
}
