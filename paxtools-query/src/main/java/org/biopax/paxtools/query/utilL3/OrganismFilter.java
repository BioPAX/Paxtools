package org.biopax.paxtools.query.utilL3;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.level3.PhysicalEntity;

/**
 * Filter by organism. Applied to PhysicalEntity. Checks if their EntityReference is associated with
 * one of the given organisms.
 *
 * @author Ozgun Babur
 */
public class OrganismFilter extends StringFieldFilter
{
	/**
	 * Constructor.
	 * @param organism organisms to select
	 */
	public OrganismFilter(String[] organism)
	{
		super(true, organism);
	}

	/**
	 * Creates the accessor from PhysicalEntity to the organism of related EntityReference.
	 */
	@Override
	public void createFieldAccessors()
	{
		addAccessor(new PathAccessor("PhysicalEntity/entityReference/organism/name"),
			PhysicalEntity.class);
		addAccessor(new PathAccessor("PhysicalEntity/entityReference/organism/xref:UnificationXref/id"),
			PhysicalEntity.class);
	}
}
