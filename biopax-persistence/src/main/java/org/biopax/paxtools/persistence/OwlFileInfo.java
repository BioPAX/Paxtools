/*
 * OwlFileInfo.java
 *
 * 2007.04.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.persistence;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.annotations.CollectionOfElements;

/**
 * OWL file information record for RDB
 * @author yoneki
 */
@Entity(name="owlfileinfo")
public class OwlFileInfo implements Serializable {
	String keyName;
	List<String> ids = new LinkedList<String>();
	Date uploadDate = new Date();

	/** Creates a new instance of OwlFileInfo */
	public OwlFileInfo() {
	}
	
	@Id
	// MySQLは長さが決まっていないテキストをキーにできない。
	// 2007.08.31
	//@Column(columnDefinition="TEXT")
	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String value) {
		keyName = value;
	}

	@CollectionOfElements @Column(columnDefinition="TEXT")
	public List<String> getRDFId() {
		return ids;
	}

	public void setRDFId(List<String> value) {
		ids = value;
	}
	
	public void addRDFId(String value) {
		ids.add(value);
	}

	@Temporal(TemporalType.TIMESTAMP) @Column(name="uploaddate")
	public Date getUploadDate() {
		return uploadDate;
	}

	public void setUploadDate(Date value) {
		uploadDate = value;
	}

}
