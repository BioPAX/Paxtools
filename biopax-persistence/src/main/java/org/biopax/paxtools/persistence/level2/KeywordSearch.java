/*
 * KeywordSearch.java
 *
 * 2007.05.25 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.persistence.level2;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.lucene.analysis.Analyzer;
import org.hibernate.search.Search;
import org.biopax.paxtools.model.BioPAXElement;
import org.apache.lucene.queryParser.QueryParser;
import org.biopax.paxtools.proxy.level2.BioPAXElementProxy;
import org.hibernate.ejb.EntityManagerImpl;
import org.hibernate.search.FullTextSession;

/**
 * keyword search class
 * @author yoneki
 */
public class KeywordSearch {
	/**
	 * Hibernate-RDB context
	 */
	HiRDBSession session = null;
	ArrayList<String> fieldNames = null;

	/**
	 * construct
	 * @param session
	 */
	public KeywordSearch(HiRDBSession session) {
		this.session = session;
		fieldNames = new ArrayList<String>();
		fieldNames.add(BioPAXElementProxy.SEARCH_FIELD_NAME);
		fieldNames.add(BioPAXElementProxy.SEARCH_FIELD_SYNONYMS);
		fieldNames.add(BioPAXElementProxy.SEARCH_FIELD_TERM);
		fieldNames.add(BioPAXElementProxy.SEARCH_FIELD_EC_NUMBER);
		fieldNames.add(BioPAXElementProxy.SEARCH_FIELD_SEQUENCE);
		fieldNames.add(BioPAXElementProxy.SEARCH_FIELD_XREF_DB);
		fieldNames.add(BioPAXElementProxy.SEARCH_FIELD_XREF_ID);
		fieldNames.add(BioPAXElementProxy.SEARCH_FIELD_AVAILABILITY);
		fieldNames.add(BioPAXElementProxy.SEARCH_FIELD_COMMENT);
		fieldNames.add(BioPAXElementProxy.SEARCH_FIELD_KEYWORD);
	}

	/**
	 * get field names
	 * @return List of field names
	 */
	public List<String> getFieldNames() {
		return fieldNames;
	}
	
	/**
	 * keyword search
	 * @param keyword keyword
	 * @return List of BioPAXElement
	 */
	public List<BioPAXElement> search(String keyword) {
		if (session.setup() == false)
			return null;
		EntityManager em = session.getEntityManager();
		List result = new ArrayList();
		for (String fieldName: getFieldNames()) {
			result.addAll(fetch(em, fieldName, keyword));
		}
		return result;
	}

	/**
	 * keyword search
	 * @param fieldName field name
	 * @param keyword keyword
	 * @return List of BioPAXElement
	 */
	public List<BioPAXElement> search(String fieldName, String keyword) {
		if (session.setup() == false)
			return null;
		EntityManager em = session.getEntityManager();
		List result = fetch(em, fieldName, keyword);
		return result;
	}

	public List<BioPAXElement> fetch(EntityManager em, String fieldName, String keyword) {
		return fetch(em, fieldName, keyword, null);
	}

	public List<BioPAXElement> fetch(EntityManager em, String fieldName, String keyword, Class resultClass) {
		FullTextSession fullTextSession = Search.createFullTextSession(((EntityManagerImpl)em).getSession());
		ArrayList result = new ArrayList();
		if (keyword == null || keyword.length() == 0)
			return result;
		Class analyzer = session.getConnect().getAnalyzer();
		List ql = null;
		try {
			QueryParser qp = new QueryParser(fieldName, (Analyzer)analyzer.newInstance());
			org.apache.lucene.search.Query query = qp.parse(keyword);
			org.hibernate.Query fullTextQuery = fullTextSession.createFullTextQuery(query);
			ql = fullTextQuery.list();
		} catch (Exception e) {
			System.out.println("KeywordSearch.searchSub() Exception!");
			e.printStackTrace();
		}
		if (ql != null) {
			for (Object o: ql) {
				if (resultClass == null || resultClass.isInstance(o)) {
					result.add(o);
				}
			}
		}
		return result;
	}
}

