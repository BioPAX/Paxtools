package org.biopax.paxtools.model.level2;

import java.util.Set;


public interface pathway extends process
{
// -------------------------- OTHER METHODS --------------------------

	void addPATHWAY_COMPONENTS(pathwayComponent PATHWAY_COMPONENT);


	bioSource getORGANISM();
// --------------------- ACCESORS and MUTATORS---------------------


	Set<pathwayComponent> getPATHWAY_COMPONENTS();

	void removePATHWAY_COMPONENTS(pathwayComponent PATHWAY_COMPONENT);

	void setORGANISM(bioSource ORGANISM);

	void setPATHWAY_COMPONENTS(Set<pathwayComponent> PATHWAY_COMPONENTS);
}