/*
 * PublicationXrefProxy.java
 *
 * 2007.12.04 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;

import java.util.Set;
import org.biopax.paxtools.proxy.StringSetBridge;

/**
 * Proxy for publicationXref
 */
@Entity(name="l3publicationxref")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class PublicationXrefProxy extends XrefProxy implements PublicationXref {
	public PublicationXrefProxy() {
	}

	// Property AUTHORS

	@CollectionOfElements @Column(name="author_x", columnDefinition="text")
	@FieldBridge(impl=StringSetBridge.class)
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
	public Set<String> getAuthor() {
		return ((PublicationXref)object).getAuthor();
	}

	public void addAuthor(String AUTHORS) {
		((PublicationXref)object).addAuthor(AUTHORS);
	}

	public void removeAuthor(String AUTHORS) {
		((PublicationXref)object).removeAuthor(AUTHORS);
	}

	public void setAuthor(Set<String> AUTHORS) {
		((PublicationXref)object).setAuthor(AUTHORS);
	}

    // Property SOURCE

	@CollectionOfElements @Column(name="source_x", columnDefinition="text")
	@FieldBridge(impl=StringSetBridge.class)
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
	public Set<String> getSource() {
		return ((PublicationXref)object).getSource();
	}

	public void addSource(String SOURCE) {
		((PublicationXref)object).addSource(SOURCE);
	}

	public void removeSource(String SOURCE) {
		((PublicationXref)object).removeSource(SOURCE);
	}

	public void setSource(Set<String> SOURCE) {
		((PublicationXref)object).setSource(SOURCE);
	}

    // Property TITLE

	@Basic @Column(name="title_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
	public String getTitle() {
		return ((PublicationXref)object).getTitle();
	}

	public void setTitle(String TITLE) {
		((PublicationXref)object).setTitle(TITLE);
	}

    // Property URL

	@CollectionOfElements @Column(name="url_x", columnDefinition="text")
	@FieldBridge(impl=StringSetBridge.class)
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
	public Set<String> getUrl() {
		return ((PublicationXref)object).getUrl();
	}

	public void addUrl(String URL) {
		((PublicationXref)object).addUrl(URL);
	}

	public void removeUrl(String URL) {
		((PublicationXref)object).removeUrl(URL);
	}

	public void setUrl(Set<String> URL) {
		((PublicationXref)object).setUrl(URL);
	}

    // Property YEAR

	@Basic @Column(name="year_x")
	public int getYear() {
		return ((PublicationXref)object).getYear();
	}

	public void setYear(int YEAR) {
		((PublicationXref)object).setYear(YEAR);
	}
	
	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return PublicationXref.class;
	}
}

