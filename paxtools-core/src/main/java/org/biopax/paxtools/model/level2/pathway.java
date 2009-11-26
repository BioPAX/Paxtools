package org.biopax.paxtools.model.level2;

import java.util.Set;


public interface pathway extends process
{
// -------------------------- OTHER METHODS --------------------------

	public void addPATHWAY_COMPONENTS(pathwayComponent PATHWAY_COMPONENT);


	public bioSource getORGANISM();
// --------------------- ACCESORS and MUTATORS---------------------


	public Set<pathwayComponent> getPATHWAY_COMPONENTS();

	public void removePATHWAY_COMPONENTS(pathwayComponent PATHWAY_COMPONENT);

	public void setORGANISM(bioSource ORGANISM);

	void setPATHWAY_COMPONENTS(Set<pathwayComponent> PATHWAY_COMPONENTS);
}