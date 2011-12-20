package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.CovalentBindingFeature;
import org.biopax.paxtools.model.level3.SequenceModificationVocabulary;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
@Proxy(proxyClass= CovalentBindingFeature.class)
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class CovalentBindingFeatureImpl extends BindingFeatureImpl implements CovalentBindingFeature
{
	private SequenceModificationVocabulary modificationType;

	public CovalentBindingFeatureImpl() {
	}
	
	@Override @Transient
	public Class<? extends CovalentBindingFeature> getModelInterface()
	{
		return CovalentBindingFeature.class;
	}

	@ManyToOne(targetEntity = SequenceModificationVocabularyImpl.class)//, cascade = {CascadeType.ALL})
	public SequenceModificationVocabulary getModificationType()
	{
		return this.modificationType;
	}

	public void setModificationType(SequenceModificationVocabulary modificationType)
	{
		this.modificationType = modificationType;
	}

	@Override
	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		if(!(element instanceof CovalentBindingFeature))
			return false;
		
		CovalentBindingFeature that = (CovalentBindingFeature) element;
		SequenceModificationVocabulary type = that.getModificationType();
		boolean value = (type == null) ?
		                this.getModificationType() == null :
		                this.getModificationType().equals(type);
		return value && super.semanticallyEquivalent(element);
	}

	@Override
	public int equivalenceCode()
	{
		return super.equivalenceCode() +
		       29 * (modificationType == null ? 0 : modificationType.equivalenceCode());
	}
}
