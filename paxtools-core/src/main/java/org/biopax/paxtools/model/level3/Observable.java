package org.biopax.paxtools.model.level3;

import java.util.Set;

/**
 * User: demir Date: Aug 17, 2007 Time: 5:47:27 PM
 */
public interface Observable
{

	  Set<Evidence> getEvidence();

	  void addEvidence(Evidence newEvidence);

	  void removeEvidence(Evidence oldEvidence);
}
