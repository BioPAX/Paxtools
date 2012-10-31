package org.biopax.paxtools.io.sbgn;

import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.PhysicalEntity;

import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class ListUbiqueDetector implements UbiqueDetector
{
	Set<String> ubiqueIDs;

	public ListUbiqueDetector(Set<String> ubiqueIDs)
	{
		this.ubiqueIDs = ubiqueIDs;
	}

	@Override
	public boolean isUbique(PhysicalEntity pe)
	{
		return ubiqueIDs.contains(pe.getRDFId());
	}
}
