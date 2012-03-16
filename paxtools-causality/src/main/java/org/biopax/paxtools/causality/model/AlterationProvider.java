package org.biopax.paxtools.causality.model;

import java.util.Map;

/**
 * A provider of the altered entities.
 *
 * @author Ozgun Babur
 */
public interface AlterationProvider
{
	public Map<Alteration, Change[]> getAlterations(Node node);
}
