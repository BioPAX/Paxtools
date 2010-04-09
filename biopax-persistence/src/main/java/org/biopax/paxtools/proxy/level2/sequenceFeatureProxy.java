/*
 * sequenceFeatureProxy.java
 *
 * 2007.04.02 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.*;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.util.Set;

import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.biopax.paxtools.proxy.StringSetBridge;

/**
 * Proxy for sequenceFeature
 */
@Entity(name="l2sequencefeature")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class sequenceFeatureProxy extends utilityClassProxy implements sequenceFeature {
	public sequenceFeatureProxy() {
	}
	@Transient
	public Class getModelInterface()
	{
		return sequenceFeature.class;
	}


	public void addFEATURE_LOCATION(sequenceLocation FEATURE_LOCATION) {
		((sequenceFeature)object).addFEATURE_LOCATION(FEATURE_LOCATION);
	}

	public void addSYNONYMS(String SYNONYMS) {
		((sequenceFeature)object).addSYNONYMS(SYNONYMS);
	}

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=sequenceLocationProxy.class)
	@JoinTable(name="l2seqfeature_feature_location")
	public Set<sequenceLocation> getFEATURE_LOCATION() {
		return ((sequenceFeature)object).getFEATURE_LOCATION();
	}

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity=openControlledVocabularyProxy.class)
	@JoinColumn(name="feature_type_x")
	public openControlledVocabulary getFEATURE_TYPE() {
		return ((sequenceFeature)object).getFEATURE_TYPE();
	}

	@Basic @Column(name="name_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_NAME, index=Index.TOKENIZED)
	public String getNAME() {
		return ((sequenceFeature)object).getNAME();
	}

	@Basic @Column(name="short_name_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_NAME, index=Index.TOKENIZED)
	public String getSHORT_NAME() {
		return ((sequenceFeature)object).getSHORT_NAME();
	}

	@CollectionOfElements @Column(name="synonyms_x", columnDefinition="text")
	@FieldBridge(impl=StringSetBridge.class)
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_SYNONYMS, index=Index.TOKENIZED)
	public Set<String> getSYNONYMS() {
		return ((sequenceFeature)object).getSYNONYMS();
	}

	public void removeFEATURE_LOCATION(sequenceLocation FEATURE_LOCATION) {
		((sequenceFeature)object).removeFEATURE_LOCATION(FEATURE_LOCATION);
	}

	public void removeSYNONYMS(String SYNONYMS) {
		((sequenceFeature)object).removeSYNONYMS(SYNONYMS);
	}

	public void setFEATURE_LOCATION(Set<sequenceLocation> FEATURE_LOCATION) {
		((sequenceFeature)object).setFEATURE_LOCATION(FEATURE_LOCATION);
	}

	public void setFEATURE_TYPE(openControlledVocabulary FEATURE_TYPE) {
		((sequenceFeature)object).setFEATURE_TYPE(FEATURE_TYPE);
	}

	public void setNAME(String NAME) {
		((sequenceFeature)object).setNAME(NAME);
	}

	public void setSHORT_NAME(String SHORT_NAME) {
		((sequenceFeature)object).setSHORT_NAME(SHORT_NAME);
	}

	public void setSYNONYMS(Set<String> SYNONYMS) {
		((sequenceFeature)object).setSYNONYMS(SYNONYMS);
	}

// --------------------- XReferrable ---------------------

	public void addXREF(xref XREF) {
		((sequenceFeature)object).addXREF(XREF);
	}

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=xrefProxy.class)
	@JoinTable(name="l2seqfeature_xref")
	public Set<xref> getXREF() {
		return ((sequenceFeature)object).getXREF();
	}

	public void removeXREF(xref XREF) {
		((sequenceFeature)object).removeXREF(XREF);
	}

	public void setXREF(Set<xref> XREF) {
		((sequenceFeature)object).setXREF(XREF);
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


}

