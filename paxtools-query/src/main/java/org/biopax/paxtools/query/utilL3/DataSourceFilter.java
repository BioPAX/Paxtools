package org.biopax.paxtools.query.utilL3;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.level3.Entity;

/**
 * Filter by data source. Applied to Entity.
 *
 * @author Ozgun Babur
 */
public class DataSourceFilter extends StringFieldFilter
{
	/**
	 * Constructor.
	 * @param sources organisms to select
	 */
	public DataSourceFilter(String[] sources)
	{
		super(true, sources);
	}

	/**
	 * Creates the accessor from Entity to its data source name.
	 */
	@Override
	public void createFieldAccessors()
	{
		addAccessor(new PathAccessor("Entity/dataSource/name"), Entity.class);
	}
}
