package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.TemplateReactionRegulation;


public class TemplateReactionRegulationImpl extends ControlImpl implements TemplateReactionRegulation
{
	public TemplateReactionRegulationImpl() {
	}
	
    public Class<? extends TemplateReactionRegulation> getModelInterface()
	{
		return TemplateReactionRegulation.class;
	}
}
