package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.bioSource;
import org.biopax.paxtools.model.level2.pathway;
import org.biopax.paxtools.model.level2.pathwayComponent;

import java.util.HashSet;
import java.util.Set;

/**
 */
class pathwayImpl extends processImpl implements pathway
{
// ------------------------------ FIELDS ------------------------------

	private Set<pathwayComponent> PATHWAY_COMPONENTS;
	private bioSource ORGANISM;

// --------------------------- CONSTRUCTORS ---------------------------

	public pathwayImpl()
	{
		this.PATHWAY_COMPONENTS = new HashSet<pathwayComponent>();
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------




	public Class<? extends BioPAXElement> getModelInterface()
	{
		return pathway.class;
	}

// --------------------- Interface pathway ---------------------

// --------------------- ACCESORS and MUTATORS---------------------


	public Set<pathwayComponent> getPATHWAY_COMPONENTS()
	{
		return this.PATHWAY_COMPONENTS;
	}

	public void setPATHWAY_COMPONENTS(Set<pathwayComponent> PATHWAY_COMPONENTS)
	{
		if (this.PATHWAY_COMPONENTS != null)
		{
			for (pathwayComponent pathwayComponent : this.PATHWAY_COMPONENTS)
			{
				pathwayComponent.isPATHWAY_COMPONENTSof().remove(this);
			}
		}
		this.PATHWAY_COMPONENTS = PATHWAY_COMPONENTS != null ?
			PATHWAY_COMPONENTS : new HashSet<pathwayComponent>();
		for (pathwayComponent pathwayComponent : this.PATHWAY_COMPONENTS)
		{
			pathwayComponent.isPATHWAY_COMPONENTSof().add(this);
		}
	}

	public void addPATHWAY_COMPONENTS(pathwayComponent PATHWAY_COMPONENT)
	{
		this.PATHWAY_COMPONENTS.add(PATHWAY_COMPONENT);
		PATHWAY_COMPONENT.isPATHWAY_COMPONENTSof().add(this);
	}

	public void removePATHWAY_COMPONENTS(pathwayComponent PATHWAY_COMPONENT)
	{
		this.PATHWAY_COMPONENTS.remove(PATHWAY_COMPONENT);
		PATHWAY_COMPONENT.isPATHWAY_COMPONENTSof().remove(this);
	}

	public bioSource getORGANISM()
	{
		return ORGANISM;
	}

	public void setORGANISM(bioSource ORGANISM)
	{
		this.ORGANISM = ORGANISM;
	}

}
