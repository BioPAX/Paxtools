package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.experimentalForm;
import org.biopax.paxtools.model.level2.openControlledVocabulary;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;

import java.util.HashSet;
import java.util.Set;

/**
 */
class experimentalFormImpl extends BioPAXLevel2ElementImpl
	implements experimentalForm
{
// ------------------------------ FIELDS ------------------------------

	private physicalEntityParticipant PARTICIPANT;

	private Set<openControlledVocabulary> EXPERIMENTAL_FORM_TYPE;

// --------------------------- CONSTRUCTORS ---------------------------

	public experimentalFormImpl()
	{
		this.EXPERIMENTAL_FORM_TYPE = new HashSet<openControlledVocabulary>();
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------



	public Class<? extends BioPAXElement> getModelInterface()
	{
		return experimentalForm.class;
	}

// --------------------- Interface experimentalForm ---------------------


	public physicalEntityParticipant getPARTICIPANT()
	{
		return PARTICIPANT;
	}

	public void setPARTICIPANT(physicalEntityParticipant PARTICIPANT)
	{
		this.PARTICIPANT = PARTICIPANT;
	}

	public void setEXPERIMENTAL_FORM_TYPE(
		Set<openControlledVocabulary> EXPERIMENTAL_FORM_TYPE)
	{
		this.EXPERIMENTAL_FORM_TYPE = EXPERIMENTAL_FORM_TYPE;
	}

// --------------------- ACCESORS and MUTATORS---------------------

	public Set<openControlledVocabulary> getEXPERIMENTAL_FORM_TYPE()
	{
		return EXPERIMENTAL_FORM_TYPE;
	}

	public void addEXPERIMENTAL_FORM_TYPE(
		openControlledVocabulary EXPERIMENTAL_FORM_TYPE)
	{
		this.EXPERIMENTAL_FORM_TYPE.add(EXPERIMENTAL_FORM_TYPE);
	}

	public void removeEXPERIMENTAL_FORM_TYPE(
		openControlledVocabulary EXPERIMENTAL_FORM_TYPE)
	{
		this.EXPERIMENTAL_FORM_TYPE.remove(EXPERIMENTAL_FORM_TYPE);
	}

// --------------------- CANONICAL METHODS ------------------------

	public String toString()
	{
		String s = "";
		for (openControlledVocabulary ocv : EXPERIMENTAL_FORM_TYPE)
		{
			if (s.length() > 0) s += "; ";
			s += ocv;
		}
		return s;
	}
}
