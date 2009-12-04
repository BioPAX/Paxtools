/*
 * SequenceLocationProxy.java
 *
 * 2007.04.02 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import java.util.Set;
import javax.persistence.*;

/**
 * Proxy for sequenceLocation
 */
@Entity(name="l3sequencelocation")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class SequenceLocationProxy extends Level3ElementProxy 
	implements SequenceLocation 
{
	public SequenceLocationProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return SequenceLocation.class;
	}

	// Property LOCATION-TYPE

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = SequenceRegionVocabularyProxy.class)
	@JoinTable(name="l3seqloc_region_type")
	public Set<SequenceRegionVocabulary> getRegionType() {
		return ((SequenceLocation)object).getRegionType();
	}

	public void addRegionType(SequenceRegionVocabulary regionType) {
		((SequenceLocation)object).addRegionType(regionType);
	}

	public void removeRegionType(SequenceRegionVocabulary regionType) {
		((SequenceLocation)object).removeRegionType(regionType);
	}

	public void setRegionType(Set<SequenceRegionVocabulary> regionType) {
		((SequenceLocation)object).setRegionType(regionType);
	}
}
