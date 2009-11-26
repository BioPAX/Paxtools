/*
 * HiRDBSession.java
 *
 * 2007.08.24 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.persistence.level2;

import javax.persistence.EntityManager;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.persistence.level2.InteractionSearch;
import org.biopax.paxtools.persistence.level2.KeywordSearch;
import org.biopax.paxtools.persistence.level2.MiscSearch;
import org.biopax.paxtools.persistence.level2.PathwaySearch;
import org.biopax.paxtools.persistence.level2.PhysicalEntitySearch;

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
	public KeywordSearch createKeywordSearch() {
		return new KeywordSearch(this);
	}

	/**
	 * create misc search
	 * @return MiscSearch
	 */
	public MiscSearch createMiscSearch() {
		return new MiscSearch(this);
	}

	/**
	 * create pathway search
	 * @return PathwaySearch
	 */
	public PathwaySearch createPathwaySearch() {
		return new PathwaySearch(this);
	}

	/**
	 * create interaction search
	 * @return InteractionSearch
	 */
	public InteractionSearch createInteractionSearch() {
		return new InteractionSearch(this);
	}

	/**
	 * create physicalEntity search
	 * @return PhysicalEntitySearch
	 */
	public PhysicalEntitySearch createPhysicalEntitySearch() {
		return new PhysicalEntitySearch(this);
	}
}

