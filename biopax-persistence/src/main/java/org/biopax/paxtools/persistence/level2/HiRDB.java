/*
 * HiRDB.java
 *
 * 2007.04.06 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.persistence.level2;

import javax.persistence.EntityManager;
import org.biopax.paxtools.model.BioPAXElement;

/**
 * RDB hibernation context class
 * @author yoneki
 */
public class HiRDB {
	HiRDBConnect connect = null;
	HiRDBSession session = null;

	// for Test
	public boolean bPrintTime = false;
	
	/**
	 * contructor
	 */
	public HiRDB() {
	}
	
	/**
	 * contructor
	 * @param puName persistence unit name. "PaxtoolsForPostgreSQL" / "PaxtoolsForMySQL"
	 * @param connectionURL jdbc connection path. e.g. "jdbc:postgresql://localhost/PAXTOOLS"
	 * @param user jdbc user
	 * @param password jdbc password
	 * @param indexBase Lucene index base directory path
	 */
	public HiRDB(String puName, String connectionURL, String user, String password, String indexBase) {
		setConnect(new HiRDBConnect(puName, connectionURL, user, password, indexBase));
		setSession(new HiRDBSession(connect));
	}

	/**
	 * connect
	 */
	public HiRDBConnect getConnect() {
		return connect;
	}

	public void setConnect(HiRDBConnect connect) {
		this.connect = connect;
	}

	/**
	 * session
	 */
	public HiRDBSession getSession() {
		return session;
	}

	public void setSession(HiRDBSession session) {
		this.session = session;
	}

	long printTime() {
		java.util.Date d = new java.util.Date();
		if (bPrintTime)
			System.out.println(d.toString());
		return d.getTime();
	}

	void printTimespan(String message, long timespan) {
		if (bPrintTime)
			System.out.println(message + " " + String.valueOf(timespan) + " ms");
	}

	/**
	 * setup before upload / download
	 * @return true is OK
	 */
	public boolean setup() {
		if (connect == null)
			return false;
		connect.setup();
		if (session == null)
			setSession(new HiRDBSession(connect));
		session.setup();
		return session != null && session.getEntityManager() != null;
	}

	/**
	 * get element by rdf:ID
	 * @param rdfID rdf:ID
	 * @return BioPAXElement
	 */
	public BioPAXElement getElementByRDFID(String rdfID) {
		if (setup() == false)
			return null;
		return session.getElementByRDFID(rdfID);
	}

//	public EntityManager createEntityManager() {
//		if (connect == null)
//			return null;
//		return connect.createEntityManager();
//	}

	public EntityManager getEntityManager() {
		if (session == null)
			return null;
		return session.getEntityManager();
	}

	/**
	 * create keyword search
	 * @return KeywordSearch
	 */
	public KeywordSearch createKeywordSearch() {
		return session.createKeywordSearch();
	}

	/**
	 * create pathway retrieval search
	 * @return PathwayRetrievalSearch
	 */
//	public PathwayRetrievalSearch createPathwayRetrievalSearch() {
//		return new PathwayRetrievalSearch(this);
//	}
	
	/**
	 * create misc search
	 * @return MiscSearch
	 */
	public MiscSearch createMiscSearch() {
		return session.createMiscSearch();
	}

	/**
	 * create pathway search
	 * @return PathwaySearch
	 */
	public PathwaySearch createPathwaySearch() {
		return session.createPathwaySearch();
	}

	/**
	 * create interaction search
	 * @return InteractionSearch
	 */
	public InteractionSearch createInteractionSearch() {
		return session.createInteractionSearch();
	}

	/**
	 * create physicalEntity search
	 * @return PhysicalEntitySearch
	 */
	public PhysicalEntitySearch createPhysicalEntitySearch() {
		return session.createPhysicalEntitySearch();
	}
}
