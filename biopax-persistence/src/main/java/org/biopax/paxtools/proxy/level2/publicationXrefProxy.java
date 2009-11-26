/*
 * PublicationXrefProxy.java
 *
 * 2007.04.06 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.publicationXref;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;
import org.biopax.paxtools.proxy.StringSetBridge;

/**
 * Proxy for publicationXref
 */
@Entity(name="l2publicationxref")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class publicationXrefProxy extends xrefProxy implements publicationXref, Serializable {
	public publicationXrefProxy() {
	}

	@Transient
	public Class getModelInterface()
	{
		return publicationXref.class;
	}

	public void addAUTHORS(String AUTHORS) {
		((publicationXref)object).addAUTHORS(AUTHORS);
	}

	public void addSOURCE(String SOURCE) {
		((publicationXref)object).addSOURCE(SOURCE);
	}

	public void addURL(String URL) {
		((publicationXref)object).addURL(URL);
	}

	@CollectionOfElements @Column(name="authors_x", columnDefinition="text")
	@FieldBridge(impl=StringSetBridge.class)
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
	public Set<String> getAUTHORS() {
		return ((publicationXref)object).getAUTHORS();
	}

	@CollectionOfElements @Column(name="source_x", columnDefinition="text")
	@FieldBridge(impl=StringSetBridge.class)
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
	public Set<String> getSOURCE() {
		return ((publicationXref)object).getSOURCE();
	}

	@Basic @Column(name="title_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
	public String getTITLE() {
		return ((publicationXref)object).getTITLE();
	}

	@CollectionOfElements @Column(name="url_x", columnDefinition="text")
	@FieldBridge(impl=StringSetBridge.class)
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
	public Set<String> getURL() {
		return ((publicationXref)object).getURL();
	}

	@Basic @Column(name="year_x")
	public int getYEAR() {
		return ((publicationXref)object).getYEAR();
	}

	public void removeAUTHORS(String AUTHORS) {
		((publicationXref)object).removeAUTHORS(AUTHORS);
	}

	public void removeSOURCE(String SOURCE) {
		((publicationXref)object).removeSOURCE(SOURCE);
	}

	public void removeURL(String URL) {
		((publicationXref)object).removeURL(URL);
	}

	public void setAUTHORS(Set<String> AUTHORS) {
		((publicationXref)object).setAUTHORS(AUTHORS);
	}

	public void setSOURCE(Set<String> SOURCE) {
		((publicationXref)object).setSOURCE(SOURCE);
	}

	public void setTITLE(String TITLE) {
		((publicationXref)object).setTITLE(TITLE);
	}

	public void setURL(Set<String> URL) {
		((publicationXref)object).setURL(URL);
	}

	public void setYEAR(int YEAR) {
		((publicationXref)object).setYEAR(YEAR);
	}
}

