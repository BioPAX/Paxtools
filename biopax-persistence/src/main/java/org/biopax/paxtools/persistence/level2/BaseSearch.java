/*
 * BaseSearch.java
 *
 * 2007.08.01 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.persistence.level2;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import org.biopax.paxtools.proxy.level2.BioPAXElementProxy;

/**
 * base search class
 * @author yoneki
 */
public class BaseSearch {
	/**
	 * Hibernate-RDB context
	 */
	HiRDBSession session = null;
	
	/**
	 * construct
	 * @param context DB connection
	 */
	public BaseSearch(HiRDBSession session) {
		this.session = session;
	}

	Set fetchAllOneClassList(EntityManager em, Class resultClass, Class resultProxyClass) {
		HashSet result = new HashSet();
		List s = em.createQuery(
			"select o from " + resultProxyClass.getName() + " as o").getResultList();
		if (s != null) {
			for (Object o: s) {
				if (resultClass == null || resultClass.isInstance(o)) {
					result.add(o);
				}
			}
		}
		return result;
	}

	Set getAllOneClassList(Class resultClass, Class resultProxyClass) {
		Set result = new HashSet();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		result = fetchAllOneClassList(em, resultClass, resultProxyClass);
		return result;
	}

	Set fetchOneClassByNAME(EntityManager em, Class resultClass, String regex, boolean bIncludeSynonyms) {
		Set result = new HashSet();
		KeywordSearch ks = session.createKeywordSearch();
		for (String fieldName: ks.getFieldNames()) {
			if (fieldName.equals(BioPAXElementProxy.SEARCH_FIELD_NAME) || 
				(bIncludeSynonyms && fieldName.equals(BioPAXElementProxy.SEARCH_FIELD_SYNONYMS))) {
				result.addAll(ks.fetch(em, fieldName, regex, resultClass));
			}
		}
		return result;
	}

	Set getOneClassByNAME(Class resultClass, String regex, boolean bIncludeSynonyms) {
		Set result = new HashSet();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		result = fetchOneClassByNAME(em, resultClass, regex, bIncludeSynonyms);
		return result;
	}
}

