package org.biopax.paxtools.causality.model;

/**
 * A provider of the altered entities.
 *
 * @author Ozgun Babur
 */
public interface AlterationProvider
{
	public AlterationPack getAlterations(Node node);
}
