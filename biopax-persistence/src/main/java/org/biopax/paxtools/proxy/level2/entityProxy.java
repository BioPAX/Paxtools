/*
 * EntityProxy.java
 *
 * 2007.03.15 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.*;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;
import org.biopax.paxtools.proxy.StringSetBridge;

/**
 * Proxy for entity
 */
// InteractionParticipantProxy interaction.PARTICIPANTS
// entityProxy InteractionParticipantProxy
// 2007.09.05
@Entity(name="l2entity")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
abstract public class entityProxy extends InteractionParticipantProxy implements entity, Serializable {
	
	protected entityProxy() {
		// not get object. because this object has not factory.
	}
	
	public void addAVAILABILITY(String AVAILABILITY_TEXT) {
		((entity)object).addAVAILABILITY(AVAILABILITY_TEXT);
	}
	
	public void addDATA_SOURCE(dataSource DATA_SOURCE_INST) {
		((entity)object).addDATA_SOURCE(DATA_SOURCE_INST);
	}
	
	public void addSYNONYMS(String SYNONYMS_TEXT) {
		((entity)object).addSYNONYMS(SYNONYMS_TEXT);
	}
	
	@CollectionOfElements @Column(name="availability_x", columnDefinition="text")
	@FieldBridge(impl=StringSetBridge.class)
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_AVAILABILITY, index=Index.TOKENIZED)
	public Set<String> getAVAILABILITY() {
		return ((entity)object).getAVAILABILITY();
	}

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=dataSourceProxy.class)
	@JoinTable(name="l2entity_data_source")
	public Set<dataSource> getDATA_SOURCE() {
		return ((entity)object).getDATA_SOURCE();
	}
	
	@Basic @Column(name="name_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_NAME, index=Index.TOKENIZED)
	public String getNAME() {
		return ((entity)object).getNAME();
	}
	
	public void setNAME(String NAME) {
		((entity)object).setNAME(NAME);
	}
	
	@Basic @Column(name="short_name_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_NAME, index=Index.TOKENIZED)
	public String getSHORT_NAME() {
		return ((entity)object).getSHORT_NAME();
	}
	
	@CollectionOfElements @Column(name="synonyms_x", columnDefinition="text")
	@FieldBridge(impl=StringSetBridge.class)
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_SYNONYMS, index=Index.TOKENIZED)
	public Set<String> getSYNONYMS() {
		return ((entity)object).getSYNONYMS();
	}
	
	public void removeAVAILABILITY(String AVAILABILITY_TEXT) {
		((entity)object).removeAVAILABILITY(AVAILABILITY_TEXT);
	}
	
	public void removeDATA_SOURCE(dataSource DATA_SOURCE_INST) {
		((entity)object).removeDATA_SOURCE(DATA_SOURCE_INST);
	}
	
	public void removeSYNONYMS(String SYNONYMS_TEXT) {
		((entity)object).removeSYNONYMS(SYNONYMS_TEXT);
	}
	
	public void setAVAILABILITY(Set<String> AVAILABILITY_TEXT) {
		((entity)object).setAVAILABILITY(AVAILABILITY_TEXT);
	}
	
	public void setDATA_SOURCE(Set<dataSource> DATA_SOURCE) {
		((entity)object).setDATA_SOURCE(DATA_SOURCE);
	}
	
	public void setSHORT_NAME(String SHORT_NAME) {
		((entity)object).setSHORT_NAME(SHORT_NAME);
	}
	
	public void setSYNONYMS(Set<String> SYNONYMS) {
		((entity)object).setSYNONYMS(SYNONYMS);
	}

// --------------------- XReferrable ---------------------

	public void addXREF(xref XREF) {
		((entity)object).addXREF(XREF);
	}

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=xrefProxy.class)
	@JoinTable(name="l2entity_xref")
	public Set<xref> getXREF() {
		return ((entity)object).getXREF();
	}

	public void removeXREF(xref XREF) {
		((entity)object).removeXREF(XREF);
	}

	public void setXREF(Set<xref> XREF) {
		((entity)object).setXREF(XREF);
	}

//	@Transient
	public Set<unificationXref> findCommonUnifications(XReferrable that)
	{
		return ((XReferrable) object).findCommonUnifications(that);
	}

//	@Transient
	public Set<relationshipXref> findCommonRelationships(XReferrable that)
	{
		return ((XReferrable) object).findCommonRelationships(that);
	}

//	@Transient
	public Set<publicationXref> findCommonPublications(XReferrable that)
	{
		return ((XReferrable) object).findCommonPublications(that);
	}

// --------------------- InteractionParticipant ---------------------

	@Transient
	public Set<interaction> isPARTICIPANTSof() {
		return ((entity)object).isPARTICIPANTSof();
	}

}

