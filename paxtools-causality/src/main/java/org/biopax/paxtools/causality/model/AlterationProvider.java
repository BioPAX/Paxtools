package org.biopax.paxtools.causality.model;

/**
 * A provider of the altered entities.
 *
 * @author Ozgun Babur
 */
public interface AlterationProvider
{
	public AlterationPack getAlterations(Node node);

	/**
	 * What ever ID is extracted from the node, this method should function as if the id is
	 * extracted from the related node.
	 *
	 * @param id
	 * @return
	 */
	public AlterationPack getAlterations(String id);
}
