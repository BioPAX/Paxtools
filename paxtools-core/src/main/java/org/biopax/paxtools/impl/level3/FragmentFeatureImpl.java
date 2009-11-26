package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.FragmentFeature;
import org.biopax.paxtools.model.BioPAXElement;

/**
 * TODO:Class description User: demir Date: Aug 14, 2008 Time: 7:47:25 PM
 */
public class FragmentFeatureImpl extends EntityFeatureImpl implements FragmentFeature
{
	@Override
	public Class<? extends FragmentFeature> getModelInterface()
	{
		return FragmentFeature.class;
	}

	@Override
	public int equivalenceCode()
	{
		return super.locationCode();
	}


	@Override
	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		return super.atEquivalentLocation(((FragmentFeature) element));
	}
}
