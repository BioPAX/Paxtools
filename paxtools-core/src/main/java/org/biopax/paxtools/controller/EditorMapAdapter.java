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

	protected final Map<String, Set<PropertyEditor>> propertyToEditorMap =
			new HashMap<String, Set<PropertyEditor>>();

	protected final Map<Class<? extends BioPAXElement>,Map<String, PropertyEditor>> classToEditorMap =

			new HashMap<Class<? extends BioPAXElement>, Map<String, PropertyEditor>>();

	protected final Map<Class<? extends BioPAXElement>, Set<ObjectPropertyEditor>>
			classToInverseEditorMap =
			new HashMap<Class<? extends BioPAXElement>, Set<ObjectPropertyEditor>>();

	protected final Map<Class<? extends BioPAXElement>, Set<PropertyEditor>>
			classToEditorSet =
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
		PropertyEditor<? super D,?> result = this.classToEditorMap.get(javaClass).get(property);

		if (result == null)
		{
			if (log.isDebugEnabled()) log.debug("Could not locate controller for " + property + " | " + javaClass);
		}

		return result;
	}

	public <D extends BioPAXElement> Set<PropertyEditor<? extends D, ?>> getSubclassEditorsForProperty(
			String property, Class<D> domain)
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

	protected PropertyEditor createAndRegisterBeanEditor(String pName, Class domain,Map<Class<? extends BioPAXElement>,
			Set<Class<? extends BioPAXElement>>> rRestrictions)
	{
		PropertyEditor editor = PropertyEditor.createPropertyEditor(domain, pName);
		if(editor instanceof ObjectPropertyEditor && rRestrictions!=null)
		{
			((ObjectPropertyEditor)editor).setRangeRestriction(rRestrictions);
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
				if ((editor.getProperty().equals("PARTICIPANTS") && (conversion.class.isAssignableFrom(c) ||
				                                                     control.class.isAssignableFrom(c))) ||
				    (editor.getProperty().equals("participant") && (Conversion.class.isAssignableFrom(c) ||
				                                                    Control.class.isAssignableFrom(c))))
				{
					if (log.isDebugEnabled())
					{
						log.debug("skipping restricted participant property");
					}
				}
				else
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
		if(editor.hasInverseLink())
		{
			for (Class<? extends BioPAXElement> c : classToInverseEditorMap.keySet())
			{
				if(checkInverseRangeIsAssignable(editor, c))
				{
					classToInverseEditorMap.get(c).add(editor);
				}
			}
		}
	}

	private boolean checkInverseRangeIsAssignable(ObjectPropertyEditor editor, Class<? extends BioPAXElement> c)
	{
		if (editor.getRange().isAssignableFrom(c) )
		{
			Set<Class<? extends BioPAXElement>> restrictedRanges =
					editor.getRestrictedRangesFor(editor.getDomain());
			if(restrictedRanges.isEmpty()) return true;
			else
			{
				for (Class<? extends BioPAXElement> restrictedRange : restrictedRanges)
				{
					if(restrictedRange.isAssignableFrom(c))
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
			classToEditorSet.put(domain,new ValueSet(peMap.values()));
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
		extends AbstractFilterSet<PropertyEditor,PropertyEditor<? extends D,?>>
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

}
