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

	List<PropertyAccessor<? extends BioPAXElement, ? extends BioPAXElement>> objectAccessors;

	PropertyAccessor lastStep;

	public PathAccessor(List<PropertyAccessor<? extends BioPAXElement, ? extends BioPAXElement>> objectAccessors,
	                    PropertyAccessor lastStep)
	{
		super(BioPAXElement.class, Object.class, true);
		this.objectAccessors = objectAccessors;
		this.lastStep = lastStep;
	}

	public PathAccessor(String path, BioPAXLevel level)
	{
		super(BioPAXElement.class, Object.class, true);

		StringTokenizer st = new StringTokenizer(path, "/");
		String domainstr = st.nextToken();
		Class<? extends BioPAXElement> intermediate = getClass(level, domainstr);

		this.objectAccessors = new ArrayList<PropertyAccessor<? extends BioPAXElement, ? extends BioPAXElement>>(
				st.countTokens() - 1);

		while (st.hasMoreTokens())
		{
			String term = st.nextToken();
			// is this the last token?
			if (st.hasMoreTokens())
			{
				intermediate = extractAccessor(level, intermediate, term);

			} else
			{
				extractLastStep(level, term, intermediate);
			}
		}

	}


	private Class<? extends BioPAXElement> extractAccessor(BioPAXLevel level, Class<? extends BioPAXElement> domain,
	                                                       String term)
	{
		StringTokenizer ct = new StringTokenizer(term, ":");

		PropertyAccessor stepAccessor = getStepAccessor(level, ct, domain);
		Class<? extends BioPAXElement> restricted = getRestricted(level, ct);

		PropertyAccessor accessor = restricted == null ? stepAccessor : FilteredPropertyAccessor.create(stepAccessor,
		                                                                                                restricted);

		objectAccessors.add(accessor);
		domain = restricted == null ? stepAccessor.getRange() : restricted;
		domain = BioPAXElement.class.isAssignableFrom(domain) ? domain : null;
		if (!stepAccessor.getRange().isAssignableFrom(domain))
		{
			throw new IllegalBioPAXArgumentException(
					"Could not parse path." + domain + " can not be reached by property" + stepAccessor);
		}
		return domain;
	}

	private void extractLastStep(BioPAXLevel level, String term, Class<? extends BioPAXElement> domain)
	{
		StringTokenizer ct = new StringTokenizer(term, ":");
		PropertyAccessor accessor = getStepAccessor(level, ct, domain);
		Class<? extends BioPAXElement> restricted = getRestricted(level, ct);

		lastStep = restricted == null ? accessor : FilteredPropertyAccessor.create(accessor, restricted);
		if (lastStep == null)
		{
			throw new IllegalBioPAXArgumentException(
					"Could not parse path." + term + " did not resolve to any BioPAX properties of " + domain + ".");
		}
	}

	private Class<? extends BioPAXElement> getClass(BioPAXLevel level, String domainstr)
	{
		Class<? extends BioPAXElement> domain = level.getInterfaceForName(domainstr);

		if (domain == null) throw new IllegalBioPAXArgumentException(
				"Could not parse path." + domainstr + " did not resolve to any" + "BioPAX classes in level " + level +
				".");

		return domain;
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
				Set valueFromBean = objectAccessor.getValueFromBean(bpe);
				if (valueFromBean != null || valueFromBean.isEmpty())
				{
					nextBpes.addComposited(valueFromBean);
				}
			}
			bpes = nextBpes;
		}
		HashSet values = new HashSet();
		for (BioPAXElement bpe : bpes)
		{

			Set valueFromBean = lastStep.getValueFromBean(bpe);
			if (valueFromBean != null || valueFromBean.isEmpty())
			{
				values.addAll(lastStep.getValueFromBean(bpe));
			}
		}
		return values;
	}

	private Class<? extends BioPAXElement> getRestricted(BioPAXLevel level, StringTokenizer ct)
	{
		return ct.hasMoreTokens() ? getClass(level, ct.nextToken()) : null;
	}

	private <D extends BioPAXElement> PropertyAccessor getStepAccessor(BioPAXLevel level, StringTokenizer ct,
	                                                                   Class<D> domain)
	{
		String property = ct.nextToken();
		boolean transitive = false;
		PropertyAccessor simple = null;
		if (property.endsWith("*"))
		{
			property = property.substring(0, property.length() - 1);
			transitive = true;
		}
		if (property.endsWith("Of"))
		{

			String forwardName = property.substring(0, property.length() - 2);
			for (ObjectPropertyEditor ope : SimpleEditorMap.get(level).getInverseEditorsOf(domain))
			{
				if (ope.property.equals(forwardName))
				{
					if (simple == null) simple = ope.getInverseAccessor();
				}

			}
		} else
		{
			simple = SimpleEditorMap.get(level).getEditorForProperty(property, domain);
			if (simple == null)
			{
				Set<PropertyEditor<? extends D, ?>> subclassEditorsForProperty = SimpleEditorMap.get(
						level).getSubclassEditorsForProperty(property, domain);
				simple = new UnionPropertyAccessor(subclassEditorsForProperty, domain);

			}
		}
		if (transitive)
		{
			return TransitivePropertyAccessor.create(simple);
		} else return simple;
	}

	@Override public boolean isUnknown(Object value)
	{
		if(value instanceof Set) {
			for(Object o : (Set) value) {
				if(!isSingleUnknown(o)) {
					return false; // found a "known" value
				}
			}
			// empty set or all unknown
			return true;
		} else {
			return isSingleUnknown(value);
		}

// was -
//		return value == null || (value instanceof Set && ((Set) value).isEmpty());

	}

	private boolean isSingleUnknown(Object value) {
		return value == null || BioPAXElement.UNKNOWN_DOUBLE.equals(value)
				|| BioPAXElement.UNKNOWN_FLOAT.equals(value)
				|| BioPAXElement.UNKNOWN_INT.equals(value);
	}
}
