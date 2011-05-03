package org.biopax.paxtools.controller;


import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.*;

/**
 * This class accepts an xPath like syntax to access a property path from a bean.
 */
public class PathAccessor extends PropertyAccessorAdapter<BioPAXElement, Set>
{

	List<ObjectPropertyEditor> editors;
	PropertyEditor lastStep;
	public PathAccessor(List<PropertyEditor> editors)
	{
		super(editors.get(0).getDomain(), editors.get(editors.size() - 1).getRange(), true);
		lastStep = editors.get(editors.size() - 1);
		this.editors = new ArrayList<ObjectPropertyEditor>(editors.size()-1);
		for (PropertyEditor editor : editors)
		{
			if (editors.indexOf(editor) != editors.size() - 1) this.editors.add((ObjectPropertyEditor) editor);
		}
	}

	public PathAccessor(String path, BioPAXLevel level)
	{
		this(parse(path, level));
	}

	private static PathAccessor getPathAccessor(List<PropertyEditor> editors)
	{
		if (editors == null || editors.isEmpty()) return null;

		return new PathAccessor(editors);
	}

	private static List<PropertyEditor> parse(String path, BioPAXLevel level)
	{
		StringTokenizer st = new StringTokenizer(path, "/");
		String domainstr = st.nextToken();

		SimpleEditorMap sem = SimpleEditorMap.get(level);
		Class<? extends BioPAXElement> domain = level.getInterfaceForName(domainstr);

		if (domain == null) throw new IllegalBioPAXArgumentException(
				"Could not parse path. Starting string " + domainstr + " did not resolve to any" +
				"BioPAX classes in level " + level +".");

		Class<? extends BioPAXElement> intermediate = domain;

		List<PropertyEditor>  editors = new ArrayList<PropertyEditor>(st.countTokens());

		while (st.hasMoreTokens())
		{
			PropertyEditor editor = sem.getEditorForProperty(st.nextToken(), intermediate);
			editors.add(editor);
			if(st.hasMoreTokens())
				intermediate = editor.getRange();
		}
		return editors;

	}


	public Set getValueFromBean(BioPAXElement bean) throws IllegalBioPAXArgumentException
	{
		Set<BioPAXElement> objects = new HashSet<BioPAXElement>();

		objects.add(bean);

		for (ObjectPropertyEditor editor : editors)
		{
			objects = traverse(objects, editor, BioPAXElement.class);
		}

		return traverse(objects, lastStep, lastStep.getRange());
	}


	private <R> Set<R> traverse(Set<BioPAXElement> objects, PropertyEditor editor,
	                                        Class<R> returnType)
	{
		Set<R> values = new HashSet<R>();

		for (BioPAXElement object : objects)
		{
			if(editor.isMultipleCardinality())
			{
				values.addAll((Set<R>) editor.getValueFromBean(object));
			}
			else
			{
				values.add((R) editor.getValueFromBean(object));
			}
		}
		return values;
	}
}
