package org.biopax.paxtools.model.level3;


import java.util.Set;

/**
 * Tagging interface for entities that can participate in a pathway
 * and can be targeted by a control : Pathway and Interaction
 */
public interface Process extends Entity
{
	public Set<Control> getControlledOf();
    	
	public Set<Pathway> getPathwayComponentOf();

	public Set<PathwayStep> getStepProcessOf();

}
