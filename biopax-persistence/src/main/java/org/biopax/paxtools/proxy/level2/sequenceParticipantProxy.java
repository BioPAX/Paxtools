/*
 * sequenceParticipantProxy.java
 *
 * 2007.04.02 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.sequenceFeature;
import org.biopax.paxtools.model.level2.sequenceParticipant;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import java.util.Set;

/**
 * Proxy for sequenceParticipant
 */
@Entity(name="l2sequenceparticipant")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class sequenceParticipantProxy extends physicalEntityParticipantProxy implements sequenceParticipant {
	public sequenceParticipantProxy() {
	}

	@Transient
	public Class getModelInterface()
	{
		return sequenceParticipant.class;
	}

	public void addSEQUENCE_FEATURE_LIST(sequenceFeature SEQUENCE_FEATURE) {
		((sequenceParticipant)object).addSEQUENCE_FEATURE_LIST(SEQUENCE_FEATURE);
	}

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=sequenceFeatureProxy.class)
	@JoinTable(name="l2seqp_sequence_feature_list")
	public Set<sequenceFeature> getSEQUENCE_FEATURE_LIST() {
		return ((sequenceParticipant)object).getSEQUENCE_FEATURE_LIST();
	}

	public void removeSEQUENCE_FEATURE_LIST(sequenceFeature SEQUENCE_FEATURE) {
		((sequenceParticipant)object).removeSEQUENCE_FEATURE_LIST(SEQUENCE_FEATURE);
	}

	public void setSEQUENCE_FEATURE_LIST(Set<sequenceFeature> SEQUENCE_FEATURE_LIST) {
		((sequenceParticipant)object).setSEQUENCE_FEATURE_LIST(SEQUENCE_FEATURE_LIST);
	}
}

