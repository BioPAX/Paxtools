package org.biopax.paxtools.hql;

import org.biopax.paxtools.controller.PropertyAccessorAdapter;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;
import org.hibernate.Query;
import org.hibernate.Session;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class HQLPropertyAccessor<D extends BioPAXElement, R> extends PropertyAccessorAdapter<D, R>
{

	private static final String DOMAIN = "domain";

	private String multipleDomainQueryString;

	private Query multiDomainQuery;

	private String singleDomainQueryString;

	private Query singleDomainQuery;


	private PropertyEditor editor;


	public HQLPropertyAccessor(PropertyEditor editor)
	{
		super(editor.getDomain(), editor.getRange(), editor.isMultipleCardinality());
		this.editor = editor;

		String domainClass = domain.getName();
		String domainName = "d"+domain.getSimpleName();

		String property = editor.getProperty();
		multipleDomainQueryString =
				"SELECT " + domainName + "." + property +
				" FROM "  +domainClass + " as " +domainName +
				" WHERE " + domainName + " in(:" + DOMAIN + ")";

		singleDomainQueryString =
				"SELECT " + domainName + "." + property +
				" FROM "  +domainClass + " as " +domainName +
				" WHERE " + domainName + "=:" + DOMAIN;

	}
	public void init(Session session)
	{
		multiDomainQuery = session.createQuery(multipleDomainQueryString);
		singleDomainQuery = session.createQuery(singleDomainQueryString);
	}

	@Override public Set<? extends R> getValueFromBean(D bean)
			throws IllegalBioPAXArgumentException
	{
		singleDomainQuery.setParameter(DOMAIN,bean);
		List list = singleDomainQuery.list();
		System.out.println("editor = " + editor);
		System.out.println("list = " + list);
		return null; //TODO
	}

	@Override public Set<? extends R> getValueFromBeans(Collection<? extends D> beans)
			throws IllegalBioPAXArgumentException
	{
		multiDomainQuery.setParameterList(DOMAIN,beans);
		List list = multiDomainQuery.list();
		System.out.println("editor = " + editor);
		System.out.println("list = " + list);
		return null; //TODO
	}

	@Override public boolean isUnknown(Object value)
	{
		return editor.isUnknown(value);
	}
}
