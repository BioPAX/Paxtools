/*
 * HiRDBConnect.java
 *
 * 2007.08.24 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.persistence.level2;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import org.biopax.paxtools.io.jena.JenaIOHandler;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.persistence.OwlFileInfo;
import org.biopax.paxtools.persistence.ProxyIdInfo;
import org.biopax.paxtools.proxy.level2.BioPAXElementProxy;
import org.biopax.paxtools.proxy.level2.BioPAXFactoryForPersistence;
import org.hibernate.cfg.Configuration;
import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.biopax.paxtools.persistence.RDBConnect;

/**
 * RDB hibernation connect class
 * @author yoneki
 */
public class HiRDBConnect {
	public final static String PUNAME_POSTGRESQL = "PaxtoolsForPostgreSQL";
	public final static String PUNAME_MYSQL = "PaxtoolsForMySQL";
	
	String puName = PUNAME_POSTGRESQL;
	String connectionURL = "jdbc:postgresql://localhost/PAXTOOLS";
	String user = "paxtools";
	String password = "";
	String indexBase = "";
	EntityManagerFactory emFactory = null;
	JenaIOHandler jenaIOH = null;
	BioPAXFactory bpFactory = null;
	String analyzerName = "org.biopax.paxtools.persistence.PaxtoolsTextAnalyzer";
	Class analyzer = org.biopax.paxtools.persistence.PaxtoolsTextAnalyzer.class;
	RDBConnect rdbConnect = null;
	// for Test
	public boolean bPrintTime = false;
	public boolean bShowSQL = false;
	public boolean bProgress = false;
	public long vacuumLoopCount = 10000;
	// when first upload, set cleanUpload flag true for speed up.
	public boolean bCleanUpload = false;
	/**
	 * contructor
	 */
	public HiRDBConnect() {
	}
	
	/**
	 * contructor
	 * @param puName persistence unit name. "PaxtoolsForPostgreSQL" / "PaxtoolsForMySQL"
	 * @param connectionURL jdbc connection path. e.g. "jdbc:postgresql://localhost/PAXTOOLS"
	 * @param user jdbc user
	 * @param password jdbc password
	 * @param indexBase Lucene index base directory path
	 */
	public HiRDBConnect(String puName, String connectionURL, String user, String password, String indexBase) {
		setPUName(puName);
		setConnectionURL(connectionURL);
		setUser(user);
		setPassword(password);
		setIndexBase(indexBase);
	}

	/**
	 * persistence unit name
	 */
	public String getPUName() {
		return this.puName;
	}

	/**
	 * persistence unit name
	 */
	public void setPUName(String value) {
		this.puName = value;
	}
	
	/**
	 * connection URL (JDBC path)
	 */
	public String getConnectionURL() {
		return this.connectionURL;
	}
	
	/**
	 * connection URL (JDBC path)
	 */
	public void setConnectionURL(String value) {
		this.connectionURL = value;
	}

	/**
	 * database user
	 */
	public void setUser(String value) {
		user = value;
	}
	
	/**
	 * database user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * database user password
	 */
	public void setPassword(String value) {
		password = value;
	}

	/**
	 * database user password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * index base
	 */
	public void setIndexBase(String value) {
		indexBase = value;
	}

	/**
	 * index base
	 */
	public String getIndexBase() {
		return indexBase;
	}

	/**
	 * text analyzer for lucene
	 */
	public Class getAnalyzer() {
		return analyzer;
	}
	
	/**
	 * build DDL
	 * @param outputFilePath DDL file path for output
	 */
	public boolean buildDDL(String outpuFilePath) {
		Ejb3Configuration cfg = new Ejb3Configuration();
		cfg = cfg.configure(puName, new HashMap());
		cfg.setProperty("hibernate.connection.url", connectionURL);
		EntityManagerFactory emf = cfg.buildEntityManagerFactory();
		Configuration config = cfg.getHibernateConfiguration();
		SchemaExport se = new SchemaExport(config);
		se.setOutputFile(outpuFilePath) ;
		se.create(false, false);
		return true;
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

	void vacuum() {
		if (rdbConnect != null)
			rdbConnect.vacuum();
	}
	
	/**
	 * setup before upload / download
	 * @return true is OK
	 */
	public boolean setup() {
		if (emFactory == null) {
			long startTime = printTime();
			
			Ejb3Configuration cfg = new Ejb3Configuration();
			cfg = cfg.configure(puName, new HashMap());
			cfg.setProperty("hibernate.connection.url", connectionURL);
			cfg.setProperty("hibernate.connection.username", user);
			cfg.setProperty("hibernate.connection.password", password);
			if (indexBase != null && indexBase.length() > 0)
				cfg.setProperty("hibernate.search.default.indexBase", indexBase);
			if (bShowSQL)
				cfg.setProperty("hibernate.show_sql", "true");

			emFactory = cfg.buildEntityManagerFactory();
			//emFactory = Persistence.createEntityManagerFactory(puName);

			analyzerName = cfg.getProperties().getProperty("hibernate.search.analyzer", analyzerName);
			if (analyzerName.length() > 0) {
				try {
					analyzer = Class.forName(analyzerName);
				}
				catch (Exception e) {
				}
			}

			printTimespan("Ejb3Configuration", printTime() - startTime);
		}
		if (bpFactory == null) {
			long startTime = printTime();
			
			bpFactory = new BioPAXFactoryForPersistence();
			
			printTimespan("BioPAXFactoryForPersistence", printTime() - startTime);
		}
		if (jenaIOH == null) {
			long startTime = printTime();

			jenaIOH = new JenaIOHandler(bpFactory, BioPAXLevel.L2);

			printTimespan("JenaIOHandler", printTime() - startTime);
		}
		if (rdbConnect == null) {
			String driverName = "";
			if (puName.equals(PUNAME_POSTGRESQL))
				driverName = RDBConnect.DRIVERCLASS_POSTGRESQL;
			else if (puName.equals(PUNAME_MYSQL))
				driverName = RDBConnect.DRIVERCLASS_MYSQL;
			if (driverName.length() > 0) {
				rdbConnect = new RDBConnect(driverName, connectionURL);
				rdbConnect.bProgress = bProgress;
				if (!rdbConnect.connect(user, password))
					rdbConnect = null;
			}
		}
		return emFactory != null && bpFactory != null && jenaIOH != null;
	}

	public boolean isSetupOK() {
		return emFactory != null && bpFactory != null && jenaIOH != null;
	}

	/**
	 * upload owl data
	 * @param keyName key of OWL data. e.g. ordinary it may be owl file name.
	 * @param iStream input stream of owl data.
	 * @return true is OK
	 */
	public boolean uploadOWL(String keyName, InputStream iStream) {
		if (setup() == false)
			return false;

		deleteOWL(keyName);

		if (bProgress) {
			printTime();
			System.out.println("JenaIOHandler.convertFromOWL");
		}
		Model model = jenaIOH.convertFromOWL(iStream);
		if (bProgress) {
			printTime();
			System.out.println("Model.getObjects");
		}
		Set<BioPAXElement> elemList = model.getObjects();

		if (bProgress) {
			printTime();
			System.out.println("EntityManagerFactory.createEntityManager");
		}
		EntityManager em = emFactory.createEntityManager();
		if (em == null) {
			return false;
		}

		OwlFileInfo fInfo = new OwlFileInfo();
		fInfo.setKeyName(keyName);
		boolean result = true;

		if (bProgress) {
			printTime();
			System.out.println("element count: " + String.valueOf(elemList.size()));
		}

		EntityTransaction tr = em.getTransaction();
		try {
			tr.begin();
			// proxy ID
			int column = 0;
			for (BioPAXElement elem: elemList) {
				if (elem instanceof BioPAXElementProxy) {
					ProxyIdInfo pii = null;
					if (!bCleanUpload) {
						// エレメント数が多いとき、rdf:IDの重複チェックがどんどん重くなる。
						// 最初のアップロードのときはこれを省略するのが望ましい。
						pii = getProxyIdInfoByRDFID(em, elem.getRDFId());
					}
					if (pii != null) {
						if (bProgress)
							System.out.print("x");
						BioPAXElement delem = getElementByProxyIdInfo(em, pii);
						if (delem != null)
							em.remove(delem);
					}
					else {
						if (bProgress)
							System.out.print("o");
						pii = new ProxyIdInfo();
						pii.setRDFId(elem.getRDFId());
						em.persist(pii);
					}
					column++;
					if (column >= 80) {
						System.out.println("");
						column = 0;
					}
					((BioPAXElementProxy)elem).setProxyId(pii.getProxyId());
				}
			}

			if (bProgress)
				System.out.println("");

			// save entity
			int countAll = 1;
			for (BioPAXElement elem : elemList) {
				if (elem instanceof BioPAXElementProxy) {
					fInfo.addRDFId(elem.getRDFId());
					if (bProgress)
						System.out.println(String.valueOf(countAll) + " " +  String.valueOf(((BioPAXElementProxy)elem).getProxyId()) + " " + ((BioPAXElementProxy)elem).getRDFId());
					em.persist(elem);
				}
				countAll++;
			}
			// save OWL file info
			if (bProgress) {
				printTime();
				System.out.println("owlfileinfo");
			}
			em.persist(fInfo);
			tr.commit();
		}
		catch (Exception e) {
			e.printStackTrace();
			tr.rollback();
		}
		em.close();
		return result;
	}

	public boolean uploadOWLBigFileFirst(String keyName, InputStream iStream) {
		bProgress = true;
		bPrintTime = true;
		bCleanUpload = true;
		return uploadOWL(keyName, iStream);
	}

	/**
	 * list owl data
	 * @param oStream output stream.
	 * @return true is OK
	 */
	public Set<String> listOWL() {
		HashSet<String> keyNames = new HashSet<String>();
		if (setup() == false)
			return keyNames;
		EntityManager em = emFactory.createEntityManager();
		if (em == null)
			return keyNames;

		List rl = em.createQuery(
			"select distinct o.keyName from " + OwlFileInfo.class.getName() + " as o").getResultList();
		if (rl != null && rl.size() > 0) {
			for (Object s: rl) {
				keyNames.add((String)s);
			}
		}
		em.close();

		return keyNames;
	}

	/**
	 * download owl data
	 * @param key key of OWL data. e.g. ordinary it may be owl file name.
	 * @param oStream output stream of owl data.
	 * @return true is OK
	 */
	public boolean downloadOWL(String key, OutputStream oStream) {
		if (setup() == false)
			return false;
		// load OWL file info
		EntityManager em = emFactory.createEntityManager();
		OwlFileInfo fInfo = null;
		try {
			fInfo = (OwlFileInfo)em.find(OwlFileInfo.class, key);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		if (fInfo == null) {
			return false;
		}

		boolean result = true;
		// load entity
		Model model = bpFactory.createModel();
		for (String id: fInfo.getRDFId()) {
			BioPAXElement elem = getElementByRDFID(em, id);
			if (elem == null) {
				return false;
			}
			model.add(elem);
		}
		try {
			if (result)
				jenaIOH.convertToOWL(model, oStream);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		em.close();
		return true;
	}
	
	/**
	 * delete owl data
	 * @param key key of OWL data. e.g. ordinary it may be owl file name.
	 * @return true is OK
	 */
	public boolean deleteOWL(String key) {
		if (setup() == false)
			return false;
		// load OWL file info
		EntityManager em = emFactory.createEntityManager();
		OwlFileInfo fInfo = null;
		try {
			fInfo = (OwlFileInfo)em.find(OwlFileInfo.class, key);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		if (fInfo == null) {
			return false;
		}

		boolean result = true;
		// load entity
		Model model = bpFactory.createModel();
		HashSet<ProxyIdInfo> piiList = new HashSet<ProxyIdInfo>();
		for (String id: fInfo.getRDFId()) {
			ProxyIdInfo pii = getProxyIdInfoByRDFID(em, id);
			if (pii == null) {
				return false;
			}
			BioPAXElement elem = getElementByProxyIdInfo(em, pii);
			if (elem == null) {
				return false;
			}
			piiList.add(pii);
			model.add(elem);
		}

		Set<BioPAXElement> elemList = model.getObjects();

		EntityTransaction tr = em.getTransaction();
		try {
			tr.begin();
			// delete entity
			for (BioPAXElement elem : elemList) {
				em.remove(elem);
			}
			for (ProxyIdInfo pii : piiList) {
				em.remove(pii);
			}
			// delete OWL file info
			em.remove(fInfo);
			tr.commit();
		}
		catch (Exception e) {
			e.printStackTrace();
			tr.rollback();
			result = false;
		}
		
		em.close();
		return true;
	}
	
	/*
	 * create entity manager
	 * @return new entity manager
	 */
	public EntityManager createEntityManager() {
		if (setup() == false)
			return null;
		return emFactory.createEntityManager();
	}

	ProxyIdInfo getProxyIdInfoByRDFID(EntityManager em, String rdfID) {
		try {
			List piiList = em.createQuery("select o from " + ProxyIdInfo.class.getName() + " as o where o.RDFId = '" + rdfID + "'").getResultList();
			if (piiList != null && piiList.size() > 0) {
				return (ProxyIdInfo)piiList.get(0);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	BioPAXElement getElementByProxyIdInfo(EntityManager em, ProxyIdInfo pii) {
		if (setup() == false)
			return null;
		if (pii == null)
			return null;
		BioPAXElement elem = null;
		try {
			elem = (BioPAXElement)em.find(BioPAXElementProxy.class, pii.getProxyId());
		}
		catch (Exception e) {
			e.printStackTrace();
			elem = null;
		}
		return elem;
	}

	/**
	 * get element by rdf:ID
	 * @param em entity manager
	 * @param rdfID rdf:ID
	 * @return BioPAXElement
	 */
	public BioPAXElement getElementByRDFID(EntityManager em, String rdfID) {
		if (setup() == false)
			return null;
		ProxyIdInfo pii = getProxyIdInfoByRDFID(em, rdfID);
		if (pii == null)
			return null;
		return getElementByProxyIdInfo(em, pii);
	}
}
