package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * This class is a composite property accessor that allows users to chain multiple
 * property accessors to define paths in the BioPAX object graph.
 * 
 * The path can be defined by either explicitly providing a set of property accessors or
 * by an xPath like syntax.
 * 
 * The path can contain transitive and or restricted sub paths:
 * A transitive sub-path is traversed recursively as many times as possible. e.g. Complex/component* will
 * return all components of the complex and the components of the components if those components
 * are complex recursively.
 * 
 * A restricted subpath is restricted with a class and only follows through the domains that are
 * assignable from the the restriction class. e.g. Control/controlled:Pathway/name will only return the
 * names of the pathways that are being controlled, but not interactions.
 */
public class PathAccessor extends PropertyAccessorAdapter<BioPAXElement, Object>
{

	List<PropertyAccessor<? extends BioPAXElement, ?>> accessors;

	Class<? extends BioPAXElement> domain;

	List<Class<? extends BioPAXElement>> domainOrder = new ArrayList<Class<? extends BioPAXElement>>();

	public static final Logger log = LoggerFactory.getLogger(PathAccessor.class);

	BioPAXLevel level;

	/**
	 * Constructor for defining the access path with a list of accessors. All of the accessors must be of the same
	 * level. In the case one accessor's range can not be assigned by the domain of the next( broken path),
	 * this accessor will return an empty set.
	 * @param objectAccessors A list of object accessors.
	 * @param level biopax level
	 */
	public PathAccessor(List<PropertyAccessor<? extends BioPAXElement, ?>> objectAccessors, BioPAXLevel level)
	{
		super(BioPAXElement.class, Object.class, true);
		this.accessors = objectAccessors;
		this.domain = objectAccessors.get(0).getDomain();
		this.level = level;
	}


	/**
	 * Constructor for defining the access path via a XPath like string.
	 * @param path The string defining the path. The following operators can be used:
	 * <ul>
	 * <li>The first token is the class name of the starting domain.</li>
	 * <li>The next tokens are names of the properties, separated by "/"</li>
	 * <li>A property step can be restricted to a domain by defining the restriction class with ":" after the
	 * property </li>
	 * <li>A property step can be declared transitive by adding a "*" at the end. </li>
	 * <li>Two accessors can be joined by "|" (Currently not implemented)</li>
	 * <li>A subpath can be defined by a substring enclosed in "(" ")"</li>
	 * <li>Subpaths can also be restricted or made transitive</li>
	 * </ul>
	 * @param level BioPAX level that this path is defined in.
	 */
	public PathAccessor(String path, BioPAXLevel level)
	{
		super(BioPAXElement.class, Object.class, true);
		this.level = level;
		parsePath(path, 0);
	}

	private PathAccessor(StringTokenizer tk, BioPAXLevel level, Class<? extends BioPAXElement> domain, int depth)
	{
		super(BioPAXElement.class, Object.class, true);
		this.domain = domain;
		this.level = level;
		if (iterateTheRemainingPath(depth, tk) != depth)
			throw new IllegalBioPAXArgumentException("Unexpected element. Parentheses do not match!");
	}

	private int parsePath(String path, int depth)
	{
		StringTokenizer st = new StringTokenizer(path, "/:*|()", true);
		domain = getClass(st.nextToken());
		this.accessors = new ArrayList<PropertyAccessor<? extends BioPAXElement, ?>>();
		if (st.nextToken().equals("/"))
		{
			PropertyAccessor stepAccessor = getStepAccessor(level, st, domain);

			// If the domain is more specific than the domain of the property accessor, then we need to filter it.
			if (!domain.equals(stepAccessor.getDomain()))
			{
				assert stepAccessor.getDomain().isAssignableFrom(domain) :
					"There must be a serous bug in the design of property editor map!";

				stepAccessor = FilteredByDomainPropertyAccessor.create(stepAccessor, domain);
			}

			accessors.add(stepAccessor);
			domainOrder.add(domain);
		} else throw new IllegalArgumentException();
		return iterateTheRemainingPath(depth, st);
	}

	private int iterateTheRemainingPath(int depth, StringTokenizer st)
	{
		Class<? extends BioPAXElement> intermediate = getNextDomain();
		while (st.hasMoreTokens())
		{
			String s = st.nextToken();
			if (s.equals("("))
			{
				PathAccessor subPath = new PathAccessor(st, level, intermediate, depth++);
				accessors.add(subPath);
				domainOrder.add(intermediate);
				intermediate = getNextDomain();
			} else if (s.equals(")"))
			{
				return depth;
			} else if (s.equals("/"))
			{
				accessors.add(getStepAccessor(level, st, intermediate));
				domainOrder.add(intermediate);
				intermediate = getNextDomain();

			} else if (s.equals("*"))
			{
				PropertyAccessor lastAccessor =
						accessors.remove(accessors.size() - 1);
				accessors.add(TransitivePropertyAccessor.create(lastAccessor));

			} else if (s.equals(":"))
			{
				Class<? extends BioPAXElement> restricted = getClass(st.nextToken());
				if (restricted != null)
				{
					PropertyAccessor<? extends BioPAXElement, ?> lastAccessor = accessors.remove(accessors.size() - 1);
					accessors.add(FilteredPropertyAccessor.create(lastAccessor, restricted));
				}
				intermediate = restricted;
			} else if (s.equals("|"))
			{
				throw new UnsupportedOperationException("Not implemented yet");
			} else throw new IllegalArgumentException();

		}
		return depth;
	}

	private Class<? extends BioPAXElement> getNextDomain()
	{
		return accessors.isEmpty() ? domain : getLastAccessor();
	}

	private Class<? extends BioPAXElement> getLastAccessor()
	{
		return (Class<? extends BioPAXElement>) accessors.get(accessors.size() - 1).getRange();
	}

	/**
	 * This constructor defaults to BioPAX Level 3.
	 * @param path The string defining the path. The following operators can be used:
	 * <ul>
	 * <li>The first token is the class name of the starting domain.</li>
	 * <li>The next tokens are names of the properties, separated by "/"</li>
	 * <li>A property step can be restricted to a domain by defining the restriction class with ":" after the
	 * property </li>
	 * <li>A property step can be declared transitive by adding a "*" at the end. </li>
	 * <li>Two accessors can be joined by "|" (Currently not implemented)</li>
	 * <li>A subpath can be defined by a substring enclosed in "(" ")"</li>
	 * <li>Subpaths can also be restricted or made transitive</li>
	 * </ul>
	 */
	public PathAccessor(String path)
	{
		this(path, BioPAXLevel.L3);
	}

	private Class<? extends BioPAXElement> getClass(String domainstr)
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
		return getValueFromBeans(bpes);
	}

	@Override
	public Set getValueFromBeans(Collection<? extends BioPAXElement> beans) throws IllegalBioPAXArgumentException
	{
		Collection<? extends BioPAXElement> bpes = beans;
		for (int i = 0; i < accessors.size() - 1; i++)
		{
			PropertyAccessor accessor = accessors.get(i);
			if (log.isTraceEnabled()) log.trace(String.valueOf(accessor));
			HashSet<BioPAXElement> nextBpes = new HashSet<BioPAXElement>();
			for (BioPAXElement bpe : bpes)
			{
				if (log.isTraceEnabled()) log.trace("\t" + bpe);
				Set valueFromBean = accessor.getValueFromBean(bpe);
				if (!valueFromBean.isEmpty())
				{
					if (log.isTraceEnabled()) log.trace("\t\tv:" + valueFromBean);
					nextBpes.addAll(valueFromBean);
					if (log.isTraceEnabled()) log.trace("\t\tn:" + nextBpes);
				}
			}
			bpes = nextBpes;
		}
		HashSet values = new HashSet();
		PropertyAccessor lastStep = accessors.get(accessors.size() - 1);
		Class<? extends BioPAXElement> lastDomain = domainOrder.get(domainOrder.size() - 1);
		if(log.isTraceEnabled()) log.trace(String.valueOf(lastStep));
		for (BioPAXElement bpe : bpes)
		{
			if (!lastDomain.isInstance(bpe)) continue;

			log.trace("\t" + bpe);
			Set valueFromBean = lastStep.getValueFromBean(bpe);
			if (!valueFromBean.isEmpty())
			{
				values.addAll(valueFromBean);
				log.trace("\t" + values);
			}
		}
		return values;
	}

	/**
	 * This method runs the path query on all the elements within the model.
	 * @param model to be queried
	 * @return a merged set of all values that is reachable by the paths starting from all applicable objects in
	 * the model. For example running ProteinReference/xref:UnificationXref on the model will get all the
	 * unification xrefs of all ProteinReferences.
	 */
	public Set getValueFromModel(Model model)
	{
		Set<? extends BioPAXElement> domains = new HashSet<BioPAXElement>(model.getObjects(this.getDomain()));
		return getValueFromBeans(domains);
	}

	private <D extends BioPAXElement> PropertyAccessor getStepAccessor(BioPAXLevel level, StringTokenizer ct,
			Class<D> domain)
	{
		String property = ct.nextToken();
		PropertyAccessor simple = null;
		if (property.endsWith("Of"))
		{
			String forwardName = property.substring(0, property.length() - 2);
			Set<ObjectPropertyEditor> iEds = SimpleEditorMap.get(level).getInverseEditorsOf(domain);
			if (iEds == null)
			{
				throw new IllegalBioPAXArgumentException("No inverse editors defined for " + domain);
			}
			for (ObjectPropertyEditor ope : iEds)
			{
				if (ope.property.equals(forwardName))
				{
					if (simple == null) simple = ope.getInverseAccessor();//TODO why not simply break, after assignment, instead using 'if'?
				}

			}
		} else
		{
			simple = SimpleEditorMap.get(level).getEditorForProperty(property, domain);
			if (simple == null)
			{
				Set<PropertyEditor<? extends D, ?>> subclassEditorsForProperty =
						SimpleEditorMap.get(level).getSubclassEditorsForProperty(property, domain);
				simple = new UnionPropertyAccessor(subclassEditorsForProperty, domain);

			}
		}
		return simple;
	}

	@Override public boolean isUnknown(Object value)
	{
		if (value instanceof Set)
		{
			for (Object o : (Set) value)
			{
				if (!isSingleUnknown(o))
				{
					return false; // found a "known" value
				}
			}
			// empty set or all unknown
			return true;
		} else
		{
			return isSingleUnknown(value);
		}
	}

	private boolean isSingleUnknown(Object value)
	{
		return value == null || BioPAXElement.UNKNOWN_DOUBLE.equals(value) ||
		       BioPAXElement.UNKNOWN_FLOAT.equals(value) || BioPAXElement.UNKNOWN_INT.equals(value);
	}

	/**
	 * @param bpe BioPAXElement to be checked.
	 * @return true if bpe is an instance of the domain of this accessor.
	 */
	public boolean applies(BioPAXElement bpe)
	{
		Class domain = accessors.iterator().next().getDomain();
		return domain.isInstance(bpe);
	}

}
