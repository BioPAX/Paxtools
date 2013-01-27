package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.BindingFeature;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

@Entity
@Proxy(proxyClass= BindingFeature.class)
@Indexed
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class BindingFeatureImpl extends EntityFeatureImpl
		implements BindingFeature
{

	private BindingFeature bindsTo;
	private Boolean intramolecular;

	//
	// BioPAXElement interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	public BindingFeatureImpl() {
	}
	
	
	@Transient
	public Class<? extends BindingFeature> getModelInterface()
	{
		return BindingFeature.class;
	}


	//
	// nonCovalentFeature interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	// Property BOUND-TO

	@OneToOne(targetEntity = BindingFeatureImpl.class)//, cascade={CascadeType.ALL})
	public BindingFeature getBindsTo()
	{
		return bindsTo;
	}

	/**
	 * This method will set the paired binding feature that binds to this feature.
	 * This method will preserve the symmetric bidirectional semantics. If not-null old feature's
	 * bindsTo will be set to null and if not null new feature's binds to will set to this
	 * @param bindsTo paired binding feature.
	 */
	public void setBindsTo(BindingFeature bindsTo)
	{
		//Check if we need to update
		if (this.bindsTo != bindsTo)
		{
			//ok go ahead - first get a pointer to the old binds to as we will swap this soon.
			BindingFeature old = this.bindsTo;
			//swap it
			this.bindsTo = bindsTo;

			//make sure old feature no longer points to this
			if (old != null && old.getBindsTo() == this)
			{
				old.setBindsTo(null);
			}
			//make sure new feature points to this
			if (!(this.bindsTo == null || this.bindsTo.getBindsTo() == this))
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
		if(!(element instanceof BindingFeature))
			return false;
		
		BindingFeature that = (BindingFeature) element;
		
		return  super.semanticallyEquivalent(element) 
				&& this.getIntraMolecular() == that.getIntraMolecular() 
		        && ((this.bindsTo == null) 
		        		? that.getBindsTo()==null 
		                : this.bindsTo.equals(that.getBindsTo())
		        	); //todo check equivalence sem.
	}

	@Override
	public int equivalenceCode()
	{
		return super
				.equivalenceCode()+(this.intramolecular?29:0);
	}
}
