package org.biopax.paxtools.util;

import org.biopax.paxtools.impl.BioPAXElementImpl;

public class DataSourceFilterFactory extends BasicFilterFactory{

	public DataSourceFilterFactory() {
		super(BioPAXElementImpl.FIELD_DATASOURCE);
	}
}
