package org.biopax.paxtools.query.wrapperL3;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.PhysicalEntity;

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
	public DataSourceFilter(String... sources)
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
