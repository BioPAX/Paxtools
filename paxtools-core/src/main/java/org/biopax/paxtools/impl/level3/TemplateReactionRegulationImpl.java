package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.TemplateReactionRegulation;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
 @Proxy(proxyClass= TemplateReactionRegulation.class)
@Indexed
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class TemplateReactionRegulationImpl extends ControlImpl implements TemplateReactionRegulation
{
	public TemplateReactionRegulationImpl() {
	}
	
	@Transient
    public Class<? extends TemplateReactionRegulation> getModelInterface()
	{
		return TemplateReactionRegulation.class;
	}
}
