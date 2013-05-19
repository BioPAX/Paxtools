package org.biopax.paxtools.io.sbgn;

import org.biopax.paxtools.model.level3.EntityFeature;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.ObjectFactory;

/**
 * Generates a State class representing an entity feature.
 *
 * @author Ozgun Babur
 */
public interface FeatureDecorator
{
	/**
	 * Creates the State for the given EntityFeature.
	 * @param ef the feature
	 * @param factory factory that can create State class
	 * @return State representing the feature
	 */
	public Glyph.State createStateVar(EntityFeature ef, ObjectFactory factory);
}
