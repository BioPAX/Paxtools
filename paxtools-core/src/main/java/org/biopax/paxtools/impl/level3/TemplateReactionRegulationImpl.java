package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.TemplateReactionRegulation;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
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
