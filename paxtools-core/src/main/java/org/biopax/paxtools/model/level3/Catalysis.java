package org.biopax.paxtools.model.level3;

import java.util.Set;

//should control conversion

public interface Catalysis extends Control
{
	// Property COFACTOR

	Set<PhysicalEntity> getCofactor();

	void addCofactor(PhysicalEntity cofactor);

	void removeCofactor(PhysicalEntity cofactor);

	void setCofactor(Set<PhysicalEntity> cofactor);

	// Property DIRECTION

	CatalysisDirectionType getCatalysisDirection();

	void setCatalysisDirection(CatalysisDirectionType catalysisDirection);
}
