package org.biopax.paxtools.controller;


import org.apache.commons.collections15.set.CompositeSet;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.*;

/**
 * This class accepts an xPath like syntax to access a property path from a bean.
 */
public class PathAccessor extends PropertyAccessorAdapter<BioPAXElement, Object>
{

	List<? extends PropertyAccessor<? extends BioPAXElement, ? extends BioPAXElement>> objectAccessors;

	PropertyAccessor lastStep;

	public PathAccessor(List<? extends PropertyAccessor> accessors)
	{
		super(accessors.get(0).getDomain(), accessors.get(accessors.size() - 1).getRange(), true);
		this.objectAccessors = new ArrayList<PropertyAccessor<? extends BioPAXElement, ? extends BioPAXElement>>();
	}

	public PathAccessor(String path, BioPAXLevel level)
	{
		this(parse(path, level));
	}

	private static List<? extends PropertyAccessor> parse(String path, BioPAXLevel level)
	{
		StringTokenizer st = new StringTokenizer(path, "/");
		String domainstr = st.nextToken();

		SimpleEditorMap sem = SimpleEditorMap.get(level);
		Class<? extends BioPAXElement> domain = level.getInterfaceForName(domainstr);

		if (domain == null) throw new IllegalBioPAXArgumentException(
				"Could not parse path. Starting string " + domainstr + " did not resolve to any" +
				"BioPAX classes in level " + level + ".");

		Class<? extends BioPAXElement> intermediate = domain;

		List<PropertyEditor> editors = new ArrayList<PropertyEditor>(st.countTokens());

		while (st.hasMoreTokens())
		{
			PropertyEditor editor = sem.getEditorForProperty(st.nextToken(), intermediate);
			editors.add(editor);
			if (st.hasMoreTokens()) intermediate = editor.getRange();
		}
		return editors;

	}


	public Set getValueFromBean(BioPAXElement bean) throws IllegalBioPAXArgumentException
	{
		Set<BioPAXElement> bpes = new HashSet<BioPAXElement>();

		bpes.add(bean);

		for (PropertyAccessor objectAccessor : objectAccessors)
		{
			CompositeSet<BioPAXElement> nextBpes = new CompositeSet<BioPAXElement>();
			for (BioPAXElement bpe : bpes)
			{
				nextBpes.addAll(objectAccessor.getValueFromBean(bpe));
			}
			bpes = nextBpes;
		}
		CompositeSet values = new CompositeSet();
		for (BioPAXElement bpe : bpes)
		{
			values.addAll(lastStep.getValueFromBean(bpe));
		}
		return values;
	}

	@Override public boolean isUnknown(Object value)
	{
		return value == null || !(value instanceof Set) || ((Set) value).isEmpty();
	}

}
