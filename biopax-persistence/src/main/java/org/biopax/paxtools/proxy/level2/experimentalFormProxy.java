/*
 * ExperimentalFormProxy.java
 *
 * 2007.04.05 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Proxy for experimentalForm
 */
@Entity(name="l2experimentalform")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class experimentalFormProxy extends utilityClassProxy implements experimentalForm, Serializable {
	public experimentalFormProxy() {
	}

	@Transient
	public Class getModelInterface()
	{
		return experimentalForm.class;
	}

	public void addEXPERIMENTAL_FORM_TYPE(openControlledVocabulary EXPERIMENTAL_FORM_TYPE) {
		((experimentalForm)object).addEXPERIMENTAL_FORM_TYPE(EXPERIMENTAL_FORM_TYPE);
	}

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=openControlledVocabularyProxy.class)
	@JoinTable(name="l2expfm_experimental_form_type")
	public Set<openControlledVocabulary> getEXPERIMENTAL_FORM_TYPE() {
		return ((experimentalForm)object).getEXPERIMENTAL_FORM_TYPE();
	}

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity=physicalEntityParticipantProxy.class)
	@JoinColumn(name="participant_x")
	public physicalEntityParticipant getPARTICIPANT() {
		return ((experimentalForm)object).getPARTICIPANT();
	}

	public void removeEXPERIMENTAL_FORM_TYPE(openControlledVocabulary EXPERIMENTAL_FORM_TYPE) {
		((experimentalForm)object).removeEXPERIMENTAL_FORM_TYPE(EXPERIMENTAL_FORM_TYPE);
	}

	public void setEXPERIMENTAL_FORM_TYPE(Set<openControlledVocabulary> EXPERIMENTAL_FORM_TYPE) {
		((experimentalForm)object).setEXPERIMENTAL_FORM_TYPE(EXPERIMENTAL_FORM_TYPE);
	}

	public void setPARTICIPANT(physicalEntityParticipant PARTICIPANT) {
		((experimentalForm)object).setPARTICIPANT(PARTICIPANT);
	}
}

