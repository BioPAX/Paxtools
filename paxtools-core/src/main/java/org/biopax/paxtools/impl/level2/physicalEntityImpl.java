package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.interaction;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.util.ClassFilterSet;

import java.util.HashSet;
import java.util.Set;

class physicalEntityImpl extends entityImpl implements physicalEntity
{
// ------------------------------ FIELDS ------------------------------

	private Set<physicalEntityParticipant> PHYSICAL_ENTITYof;

// --------------------------- CONSTRUCTORS ---------------------------

	public physicalEntityImpl()
	{
		this.PHYSICAL_ENTITYof = new HashSet<physicalEntityParticipant>();
	}

// ------------------------ INTERFACE METHODS ------------------------

// --------------------- Interface BioPAXElement ---------------------


	public Class<? extends BioPAXElement> getModelInterface()
	{
		return physicalEntity.class;
	}

// --------------------- Interface physicalEntity ---------------------

	public Set<physicalEntityParticipant> isPHYSICAL_ENTITYof()
	{
		return PHYSICAL_ENTITYof;
	}

	public Set<interaction> getAllInteractions()
	{
		HashSet<interaction> allInteractions = new HashSet<interaction>(
			this.isPARTICIPANTSof());
		Set<physicalEntityParticipant> peps =
			this.isPHYSICAL_ENTITYof();
		for (physicalEntityParticipant pep : peps)
		{
			Set<interaction> participantSof = pep.isPARTICIPANTSof();
			if (!participantSof.isEmpty())
			{
				assert participantSof.size() == 1;
				allInteractions.add(participantSof.iterator().next());
			}
		}
		return allInteractions;
	}

	public <T extends interaction> Set<T> getAllInteractions(
		Class<T> ofType)
	{
		return new ClassFilterSet<interaction, T>(getAllInteractions(), ofType);
	}

// -------------------------- OTHER METHODS --------------------------

	public void addPHYSICAL_ENTITYof(physicalEntityParticipant pep)
	{
		assert pep.getPHYSICAL_ENTITY().equals(this);
		this.PHYSICAL_ENTITYof.add(pep);

	}

	public void removePHYSICAL_ENTITYof(physicalEntityParticipant pep)
	{
		assert pep.getPHYSICAL_ENTITY().equals(this);
		this.PHYSICAL_ENTITYof.remove(pep);
	}

}
