/*
 * HiRDBSession.java
 *
 * 2007.08.24 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.persistence.level3;

import javax.persistence.EntityManager;
import org.biopax.paxtools.model.BioPAXElement;

/**
 * RDB hibernation context class
 * @author yoneki
 */
public class HiRDBSession {
	HiRDBConnect connect = null;
	EntityManager em = null;

	/**
	 * contructor
	 */
	public HiRDBSession() {
	}

	/**
	 * contructor
	 * @param connect
	 */
	public HiRDBSession(HiRDBConnect connect) {
		setConnect(connect);
	}

	/**
	 * connect
	 */
	public void setConnect(HiRDBConnect connect) {
		this.connect = connect;
	}

	/**
	 * connect
	 */
	public HiRDBConnect getConnect() {
		return connect;
	}

	/**
	 * setup
	 */
	public boolean setup() {
		if (connect == null)
			return false;
		connect.setup();
		if (em == null) {
			open();
		}
		return em != null;
	}

	/**
	 * open entity manager
	 */
	public boolean open() {
		if (connect != null) {
			em = connect.createEntityManager();
		}
		return em != null;
	}
	
	/**
	 * close entity manager
	 */
	public void close() {
		if (em != null) {
			em.close();
			em = null;
		}
	}
	
	/**
	 * get entity manager
	 */
	public EntityManager getEntityManager() {
		if (em == null)
			open();
		return em;
	}

	/**
	 * get element by rdf:ID
	 * @param rdfID rdf:ID
	 * @return BioPAXElement
	 */
	public BioPAXElement getElementByRDFID(String rdfID) {
		if (setup() == false)
			return null;
		return connect.getElementByRDFID(em, rdfID);
	}

	/**
	 * create keyword search
	 * @return KeywordSearch
	 */
	public KeywordSearch createKeywordSearch_x() {
		return new KeywordSearch(this);
	}
}

