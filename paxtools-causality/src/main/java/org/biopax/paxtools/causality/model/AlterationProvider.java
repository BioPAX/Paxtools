package org.biopax.paxtools.causality.model;

import java.util.Set;

/**
 * A provider of the altered entities.
 *
 * @author Ozgun Babur
 */
public interface AlterationProvider
{
	public boolean isAltered(Node node);

	public Set<Alteration> getAlterations(Node node);
}
