package org.biopax.paxtools.io.sbgn;

import org.biopax.paxtools.model.level3.EntityFeature;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.ObjectFactory;

/**
 * @author Ozgun Babur
 */
public interface FeatureDecorator
{
	public Glyph.State createStateVar(EntityFeature ef, ObjectFactory factory);
}
