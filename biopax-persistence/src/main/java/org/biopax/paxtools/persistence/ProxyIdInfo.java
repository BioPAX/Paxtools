/*
 * ProxyIdInfo.java
 *
 * 2007.09.04 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.persistence;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * proxy ID and rdf:ID information record for RDB
 * @author yoneki
 */
@Entity(name="proxyidinfo")
public class ProxyIdInfo implements Serializable {
	long proxyId;
	String rdfId;
	Date uploadDate = new Date();

	public ProxyIdInfo() {
	}
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	public long getProxyId() {
		return proxyId;
	}

	public void setProxyId(long value) {
		proxyId = value;
	}

	@Basic @Column(columnDefinition="text")
	public String getRDFId() {
		return rdfId;
	}

	public void setRDFId(String value) {
		rdfId = value;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getUploadDate() {
		return uploadDate;
	}

	public void setUploadDate(Date value) {
		uploadDate = value;
	}
}

