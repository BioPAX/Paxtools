package org.biopax.paxtools.causality.model;

import java.util.List;
import java.util.Set;

/**
 * A provider of the altered entities.
 *
 * @author Ozgun Babur
 */
public interface AlterationProvider
{
	public List<Set<Alteration>> getAlterations(Node node);
	
	public boolean isAlteredMutuallyExclusive(Node n1, Node n2);
	
	public boolean isAlteredTogether(Node n1, Node n2);
}
