package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.SequenceLocation;
import org.biopax.paxtools.model.level3.SequenceRegionVocabulary;
import org.biopax.paxtools.util.BPCollections;

import java.util.Set;


public class SequenceLocationImpl extends L3ElementImpl
	implements SequenceLocation
{
	private Set<SequenceRegionVocabulary> regionType;

	/**
	 * Constructor.
	 */
	public SequenceLocationImpl()
	{
		this.regionType = BPCollections.I.createSafeSet();
    }

	//
	// BioPAXElement interface implementation
	//
	////////////////////////////////////////////////////////////////////////////
	public Class<? extends SequenceLocation> getModelInterface()
	{
		return SequenceLocation.class;
	}

}
