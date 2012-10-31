package org.biopax.paxtools.io.sbgn;

import org.biopax.paxtools.model.level3.EntityFeature;

/**
 * @author Ozgun Babur
 */
public interface FeatureDecorator
{
	public String getStringFor(EntityFeature ef);
}
