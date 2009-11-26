package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.TemplateReactionRegulation;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.BioPAXElement;
/**
 * TODO:Class description
 * User: demir
 * Date: Aug 18, 2008
 * Time: 10:13:08 AM
 */
public class TemplateReactionRegulationImpl extends ControlImpl implements TemplateReactionRegulation
{
	
	public Class<? extends TemplateReactionRegulation> getModelInterface()
	{
		return TemplateReactionRegulation.class;
	}


}
