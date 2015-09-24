package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.FragmentFeature;


public class FragmentFeatureImpl extends EntityFeatureImpl implements FragmentFeature
{
	public FragmentFeatureImpl() {}
	
	@Override
	public Class<? extends FragmentFeature> getModelInterface()
	{
		return FragmentFeature.class;
	}

	@Override
	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		if(!(element instanceof FragmentFeature))
			return false;
		else
			return super.atEquivalentLocation(((FragmentFeature) element));
	}

    @Override
    public String toString()
    {
        return "Fragment:"+this.getFeatureLocation();
    }
}
