package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.BindingFeature;
import org.biopax.paxtools.model.BioPAXElement;


class BindingFeatureImpl extends EntityFeatureImpl
		implements BindingFeature
{

	private BindingFeature bindsTo;
	private Boolean intramolecular;

	//
	// BioPAXElement interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	public Class<? extends BindingFeature> getModelInterface()
	{
		return BindingFeature.class;
	}

	//
	// nonCovalentFeature interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	// Property BOUND-TO


	public BindingFeature getBindsTo()
	{
		return bindsTo;
	}

	public void setBindsTo(BindingFeature bindsTo)
	{
		if (!(this.bindsTo == bindsTo))
		{
			BindingFeature old = this.bindsTo;
			this.bindsTo = bindsTo;


			if (old != null && old.getBindsTo() == this)
			{
				old.setBindsTo(null);
			}
			if (this.bindsTo != null && this.bindsTo.getBindsTo() != this)
			{
				this.bindsTo.setBindsTo(this);
			}
		}
	}

	public Boolean getIntraMolecular()
	{
		return intramolecular;
	}

	public void setIntraMolecular(Boolean intramolecular)
	{
		this.intramolecular = intramolecular;
	}


	@Override
	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		BindingFeature that = (BindingFeature) element;
		return super.semanticallyEquivalent(element) &&
		                this.getIntraMolecular() == that.getIntraMolecular() 
		                && (this.bindsTo == null) 
		                	? that.getBindsTo()==null : 
		                		this.bindsTo.equals(that.getBindsTo());
	}

	@Override
	public int equivalenceCode()
	{
		return super
				.equivalenceCode()+(this.intramolecular?29:0);
	}
}
