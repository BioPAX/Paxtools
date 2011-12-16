package org.biopax.paxtools.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.level2.control;
import org.biopax.paxtools.model.level2.conversion;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.util.AbstractFilterSet;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.*;


/**
 * This is the base adapter for all editor maps. A {@link PropertyEditor} is an object that can
 * manipulate a certain property. Editor maps are responsible for initializing these editors and
 * providing them. The default level is the latest official release of BioPAX.
 */
public abstract class EditorMapAdapter implements EditorMap
{

	protected final Map<String, Set<PropertyEditor>> propertyToEditorMap = new HashMap<String, Set<PropertyEditor>>();

	protected final Map<Class<? extends BioPAXElement>, Map<String, PropertyEditor>> classToEditorMap =

			new HashMap<Class<? extends BioPAXElement>, Map<String, PropertyEditor>>();

	protected final Map<Class<? extends BioPAXElement>, Set<ObjectPropertyEditor>> classToInverseEditorMap =
			new HashMap<Class<? extends BioPAXElement>, Set<ObjectPropertyEditor>>();

	protected final Map<Class<? extends BioPAXElement>, Set<PropertyEditor>> classToEditorSet =
			new HashMap<Class<? extends BioPAXElement>, Set<PropertyEditor>>();


	private static final Log log = LogFactory.getLog(EditorMapAdapter.class);


	public Set<PropertyEditor> getEditorsOf(BioPAXElement bpe)
	{
		return getEditorsOf(bpe.getModelInterface());
	}

	public Set<PropertyEditor> getEditorsOf(Class<? extends BioPAXElement> domain)
	{
		return this.classToEditorSet.get(domain);
	}

	public Set<ObjectPropertyEditor> getInverseEditorsOf(BioPAXElement bpe)
	{
		return this.getInverseEditorsOf(bpe.getModelInterface());
	}

	public Set<ObjectPropertyEditor> getInverseEditorsOf(Class<? extends BioPAXElement> domain)
	{
		return this.classToInverseEditorMap.get(domain);
	}


	public <D extends BioPAXElement> PropertyEditor<? super D, ?> getEditorForProperty(String property,
			Class<D> javaClass)

	{
		Map<String, PropertyEditor> classEditors = this.classToEditorMap.get(javaClass);
		PropertyEditor<? super D, ?> result = null;
		if (classEditors != null)
		{
			result = classEditors.get(property);

			if (result == null)
			{
				if (log.isDebugEnabled()) log.debug("Could not locate controller for " + property + " | " +
				                                    javaClass);
			} else if (log.isDebugEnabled()) log.debug("Editors are only defined for public BioPAX interfaces");
		}
		return result;

	}

	public <D extends BioPAXElement> Set<PropertyEditor<? extends D, ?>> getSubclassEditorsForProperty(String
			property,
			Class<D> domain)
	{
		return new SubDomainFilterSet<D>(this.getEditorsForProperty(property), domain);

	}

	public Set<PropertyEditor> getEditorsForProperty(String property)
	{
		return this.propertyToEditorMap.get(property);
	}

	public <E extends BioPAXElement> Set<Class<E>> getKnownSubClassesOf(Class<E> javaClass)
	{
		return new SubClassFilterSet(classToEditorMap.keySet(), javaClass);
	}


	protected boolean isInBioPAXNameSpace(String nameSpace)
	{
		return nameSpace != null && nameSpace.startsWith(BioPAXLevel.BP_PREFIX);
	}

	protected PropertyEditor createAndRegisterBeanEditor(String pName, Class domain,
			Map<Class<? extends BioPAXElement>, Set<Class<? extends BioPAXElement>>> rRestrictions)
	{
		PropertyEditor editor = PropertyEditor.createPropertyEditor(domain, pName);
		if (editor instanceof ObjectPropertyEditor && rRestrictions != null)
		{
			((ObjectPropertyEditor) editor).setRangeRestriction(rRestrictions);
		}

		if (editor != null)
		{
			Set<PropertyEditor> beanEditorsForProperty = this.propertyToEditorMap.get(pName);
			if (beanEditorsForProperty == null)
			{
				beanEditorsForProperty = new HashSet<PropertyEditor>();
				propertyToEditorMap.put(pName, beanEditorsForProperty);
			}
			beanEditorsForProperty.add(editor);

			registerEditorsWithSubClasses(editor, domain);
		} else
		{
			if (log.isWarnEnabled()) log.warn("property = " + pName + "\ndomain = " + domain);
		}
		return editor;
	}

	protected void registerEditorsWithSubClasses(PropertyEditor editor, Class<? extends BioPAXElement> domain)
	{

		for (Class<? extends BioPAXElement> c : classToEditorMap.keySet())
		{
			if (domain.isAssignableFrom(c))
			{
				//workaround for participants - can be replaced w/ a general
				// annotation based system. For the time being, I am just handling it
				//as a special case
				if ((editor.getProperty().equals("PARTICIPANTS") &&
				     (conversion.class.isAssignableFrom(c) || control.class.isAssignableFrom(c))) ||
				    (editor.getProperty().equals("participant") &&
				     (Conversion.class.isAssignableFrom(c) || Control.class.isAssignableFrom(c))))
				{
					if (log.isDebugEnabled())
					{
						log.debug("skipping restricted participant property");
					}
				} else
				{
					classToEditorMap.get(c).put(editor.getProperty(), editor);
				}
			}

		}

		if (editor instanceof ObjectPropertyEditor)
		{
			registerInverseEditors((ObjectPropertyEditor) editor);
		}
	}

	private void registerInverseEditors(ObjectPropertyEditor editor)
	{
		if (editor.hasInverseLink())
		{
			for (Class<? extends BioPAXElement> c : classToInverseEditorMap.keySet())
			{
				if (checkInverseRangeIsAssignable(editor, c))
				{
					classToInverseEditorMap.get(c).add(editor);
				}
			}
		}
	}

	private boolean checkInverseRangeIsAssignable(ObjectPropertyEditor editor, Class<? extends BioPAXElement> c)
	{
		if (editor.getRange().isAssignableFrom(c))
		{
			Set<Class<? extends BioPAXElement>> restrictedRanges = editor.getRestrictedRangesFor(editor.getDomain());
			if (restrictedRanges.isEmpty()) return true;
			else
			{
				for (Class<? extends BioPAXElement> restrictedRange : restrictedRanges)
				{
					if (restrictedRange.isAssignableFrom(c))
					{
						return true;
					}

				}
				return false;
			}
		}
		return false;
	}


	protected void registerModelClass(String localName)
	{
		try
		{
			Class<? extends BioPAXElement> domain = getModelInterface(localName);

			HashMap<String, PropertyEditor> peMap = new HashMap<String, PropertyEditor>();
			classToEditorMap.put(domain, peMap);
			classToInverseEditorMap.put(domain, new HashSet<ObjectPropertyEditor>());
			classToEditorSet.put(domain, new ValueSet(peMap.values()));
		}
		catch (IllegalBioPAXArgumentException e)
		{
			if (log.isDebugEnabled())
			{
				log.debug("Skipping (" + e.getMessage() + ")");
			}
		}
	}


	private class SubClassFilterSet<E> extends AbstractFilterSet<Class<? extends BioPAXElement>, Class<E>>
	{
		private Class superClass = null;

		public SubClassFilterSet(Set<Class<? extends BioPAXElement>> baseSet, Class<E> superClass)
		{
			super(baseSet);
			this.superClass = superClass;
		}


		@Override public boolean filter(Class<? extends BioPAXElement> subClass)
		{
			return superClass.isAssignableFrom(subClass);
		}
	}

	private class SubDomainFilterSet<D extends BioPAXElement>
			extends AbstractFilterSet<PropertyEditor, PropertyEditor<? extends D, ?>>
	{
		private Class<D> domain;

		public SubDomainFilterSet(Set<PropertyEditor> baseSet, Class<D> domain)
		{
			super(baseSet);
			this.domain = domain;
		}

		@Override public boolean filter(PropertyEditor editor)
		{
			return domain.isAssignableFrom(editor.getDomain());
		}
	}

	protected Class<? extends BioPAXElement> getModelInterface(String localName)
	{
		try
		{
			Class modelInterface = Class.forName(this.getLevel().getPackageName() + "." + localName);
			if (BioPAXElement.class.isAssignableFrom(modelInterface))
			{
				return modelInterface;
			} else
			{
				throw new IllegalBioPAXArgumentException(
						"BioPAXElement is not assignable from class:" + modelInterface.getSimpleName());
			}
		}
		catch (ClassNotFoundException e)
		{
			throw new IllegalBioPAXArgumentException("Could not locate interface for:" + localName);
		}
	}

	private class ValueSet extends AbstractSet<PropertyEditor>
	{
		Collection values;

		public ValueSet(Collection<PropertyEditor> values)
		{
			this.values = values;
		}

		@Override public Iterator<PropertyEditor> iterator()
		{
			return values.iterator();
		}

		@Override public int size()
		{
			return values.size();
		}
	}

//	public List<ObjectPropertyEditor> dependencySortProperties()
//	{
//
//
//		HashMap<Class<? extends BioPAXElement>, SortNode> nodes =
//				new HashMap<Class<? extends BioPAXElement>, SortNode>();
//		for (Class<? extends BioPAXElement> aClass : classToEditorMap.keySet())
//		{
//			recursivelyTraverse(nodes, new ArrayList<Class<? extends BioPAXElement>>());
//		}
//
//	}

//	private int recursivelyTraverse(HashMap<Class<? extends BioPAXElement>, SortNode> nodeMap,
//			List<Class<? extends BioPAXElement>> path)
//	{
//		int level = 0;
//		Class<? extends BioPAXElement> domain = path.get(path.size() - 1);
//		SortNode node = nodeMap.get(domain);
//		for (PropertyEditor propertyEditor : getEditorsOf(domain))
//		{
//			if (propertyEditor instanceof ObjectPropertyEditor)
//			{
//				ObjectPropertyEditor ope = (ObjectPropertyEditor) propertyEditor;
//				Class range = ope.getRange();
//				if (!nodeMap.containsKey(range))
//				{
//					SortNode newNode = new SortNode(STATE.REACHED, range);
//					registerNodeAndTraverse(nodeMap, path, node, range, newNode);
//				} else
//				{
//					//Cycle!
//					if (node.cyclic)
//					{
//						if (!node.members.contains(range))
//						{
//							registerNodeAndTraverse(nodeMap, path, node, range, node);
//						}
//					} else
//					{
//						node.cyclic = true;
//						for (Class<? extends BioPAXElement> aClass : path)
//						{
//							SortNode otherNode = nodeMap.get(aClass);
//							otherNode.merge(node, nodeMap);
//						}
//					}
//				}
//			}
//		}
//		SortNode rangeNode = nodeMap.get(range);
//		int level = 0;
//		if (rangeNode == null)
//		{
//			rangeNode = new SortNode(STATE.REACHED, range);
//			nodeMap.put(range, rangeNode);
//			for (PropertyEditor editor : classToEditorMap.get(range).values())
//			{
//				if (editor instanceof ObjectPropertyEditor)
//				{
//					Class<? extends BioPAXElement> nextRange = editor.getRange();
//					level = Math.max(level, recursivelyTraverse(nodeMap, range, nextRange));
//				}
//			}
//			rangeNode.state = STATE.FINISHED;
//		} else if (rangeNode.state == STATE.REACHED)
//		{
//
//		}
//		return level + 1;
//		return 0; //TODO
//	}

	private void registerNodeAndTraverse(HashMap<Class<? extends BioPAXElement>, SortNode> nodeMap,
			List<Class<? extends BioPAXElement>> path, SortNode node, Class range, SortNode newNode)
	{
		nodeMap.put(range, newNode);
		path.add(range);
//		node.level = Math.max(node.level, recursivelyTraverse(nodeMap, path));
	}

	private class SortNode
	{
		STATE state;

		boolean cyclic = false;

		int level = 0;

		Set<Class<? extends BioPAXElement>> members = new HashSet<Class<? extends BioPAXElement>>();


		SortNode(STATE state, Class<? extends BioPAXElement> first)
		{
			this.state = state;
			this.members.add(first);
		}

		void merge(SortNode toMerge, HashMap<Class<? extends BioPAXElement>, SortNode> nodeMap)
		{
			if (this != toMerge)
			{

				this.members.addAll(toMerge.members);
				for (Class<? extends BioPAXElement> member : toMerge.members)
				{
					nodeMap.put(member, this);
				}
			}

		}
	}

	public enum STATE
	{
		REACHED,
		FINISHED
	}

}
