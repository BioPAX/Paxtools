package org.biopax.paxtools.impl;

import org.biopax.paxtools.controller.*;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

/**
 * This factory returns decorated objects for testing.
 */
public class MockFactory extends BioPAXFactory
{
// ------------------------------ FIELDS ------------------------------


	private static final List<String> strings = Arrays.asList(" ", "alpha", "beta", "gamma");

	private static final List<Float> floats = Arrays.asList(Float.MAX_VALUE, 1.0F, 0.0F, Float.MIN_VALUE);

	private static final List<Double> doubles = Arrays.asList(Double.MAX_VALUE, 1.0, 0.0, Double.MIN_VALUE);

	private static final List<Integer> ints = Arrays.asList(Integer.MAX_VALUE, 1, 0, Integer.MIN_VALUE + 1);

	private static final List<Boolean> booleans = Arrays.asList(Boolean.TRUE, Boolean.FALSE);

	private static final String base = "mock://id/";

	private int id = 0;


	private EditorMap map;

	private BioPAXLevel level;

// --------------------------- CONSTRUCTORS ---------------------------

	public MockFactory(BioPAXLevel level)
	{
		this.level = level;
		this.map = SimpleEditorMap.get(level);
	}

	@Override
	public <T extends BioPAXElement> T create(Class<T> aClass, String uri)
	{
		T t = this.getLevel().getDefaultFactory().create(aClass, uri);
		populateMock(t);
		return t;
	}

	@Override
	public BioPAXElement create(String localName, String uri)
	{
		BioPAXElement bpe = this.getLevel().getDefaultFactory().create(localName, uri);
		populateMock(bpe);
		return bpe;
	}

	private void populateMock(BioPAXElement bpe)
	{
		Set<PropertyEditor> propertyEditors = map.getEditorsOf(bpe);

		for (PropertyEditor propertyEditor : propertyEditors)
		{
			boolean multiple = propertyEditor.isMultipleCardinality();
			Object value = null;
			if (propertyEditor instanceof StringPropertyEditor)
			{
				value = getStrings(bpe, multiple);
			} else
			{
				Class range = propertyEditor.getRange();
				if (propertyEditor instanceof PrimitivePropertyEditor)
				{
					if (range == float.class)
					{
						value = multiple ? floats : floats.get(1);
					} else if (range == double.class)
					{
						value = multiple ? doubles : doubles.get(1);
					} else if (range == int.class)
					{
						value = multiple ? ints : ints.get(1);
					} else if (range == Boolean.class)
					{
						value = multiple ? booleans : booleans.get(1);
					}
				} else if (propertyEditor instanceof EnumeratedPropertyEditor)
				{
					Field[] fields = range.getFields();
					if (multiple)
					{
						value = new HashSet();
					}
					for (Field field : fields)
					{
						if (field.isEnumConstant())
						{
							try
							{
								if (multiple)
								{
									((Set) value).add(field.get(bpe));
								} else
								{
									value = field.get(bpe);
									break;
								}
							}
							catch (IllegalAccessException e)
							{
								throw new IllegalBioPAXArgumentException();
							}
						}
					}
				} else
				{
//					if (!Entity.class.isAssignableFrom(range))
//					{
//						if (multiple)
//						{
//							value = createRestrictedMock((ObjectPropertyEditor) propertyEditor, bpe, 3);
//						} else
//						{
//							value = createRestrictedMock((ObjectPropertyEditor) propertyEditor, bpe,
//							                             1).iterator().next();
//						}
//					}
				}
			}
			if (value != null)
			{
				if (multiple)
				{
					Collection values = ((Collection) value);
					if (!values.isEmpty())
					{
						Integer max = propertyEditor.getMaxCardinality(bpe.getModelInterface());
						values = upToMax(values, max);

						for (Object o : values)
						{
							propertyEditor.setValueToBean(o, bpe);
						}
					} else
					{
						propertyEditor.setValueToBean(value, bpe);
					}
				}
			}
		}
	}

	private Object getStrings(BioPAXElement bpe, boolean multiple)
	{
		if (multiple)
		{
			ArrayList<String> list = new ArrayList<>(4);
			for (String str : strings)
			{
				list.add(bpe.getUri() + str);
			}
			return list;
		} else return bpe.getUri() + strings.get(3);
	}

	private HashSet<BioPAXElement> createRestrictedMock(ObjectPropertyEditor propertyEditor, BioPAXElement bpe, int k)
	{
		HashSet<BioPAXElement> hashSet = new HashSet<>();


		Set<Class<? extends BioPAXElement>> rRanges = propertyEditor.getRestrictedRangesFor(bpe.getModelInterface());
		for (Class<? extends BioPAXElement> rRange : rRanges)
		{
			hashSet.add(createMock(rRange, bpe.getModelInterface()));
		}
		return hashSet;
	}

	private BioPAXElement createMock(Class<? extends BioPAXElement> toCreate, Class domain)
	{
		assert domain != null;
		Class<? extends BioPAXElement> actual;
		actual = findConcreteMockClass(toCreate, domain);
		if (actual != null)
		{
			return map.getLevel().getDefaultFactory().create(actual, base + id++);
		} else return null;

	}

	private Class<? extends BioPAXElement> findConcreteMockClass(Class<? extends BioPAXElement> toCreate, Class domain)
	{
		Class<? extends BioPAXElement> actual = null;
		if (map.getLevel().getDefaultFactory().canInstantiate(toCreate) && !toCreate.isAssignableFrom(domain))
		{
			actual = toCreate;
		} else
		{
			Set<? extends Class<? extends BioPAXElement>> knownSubClassesOf = map.getKnownSubClassesOf(toCreate);
			for (Class<? extends BioPAXElement> subclass : knownSubClassesOf)
			{
				if (!subclass.isAssignableFrom(domain) && subclass != toCreate &&
				    subclass.getPackage().getName().startsWith("org.biopax.paxtools.model"))
				{
					actual = findConcreteMockClass(subclass, domain);
					break;
				}
			}
		}
		return actual;
	}

	private Collection upToMax(Collection values, Integer max)
	{
		int size = values.size();
		if (max != null && max < size)
		{
			values = new ArrayList(values);
			for (int i = size - 1; i == max; i--)
			{
				((List) values).remove(i);
			}
			assert values.size() == max;
		}
		return values;
	}


	public BioPAXLevel getLevel()
	{
		return this.level;
	}

	@Override
	public String mapClassName(Class<? extends BioPAXElement> aClass) {
		return this.getLevel().getDefaultFactory().mapClassName(aClass);
	}
	
	
	public <T extends BioPAXElement> T[] create(Model model, Class<T> biopaxClass, int number, String pre)
	{
		T[] result = (T[]) Array.newInstance(biopaxClass, number);

		for (int i = 0; i < number; i++)
		{
			result[i] = model.addNew(biopaxClass, biopaxClass.getSimpleName() + pre + i);
		}
		return result;
	}

	public <T extends BioPAXElement> T[] create(Model model, Class<T> biopaxClass, int number)
	{
		return this.create(model,biopaxClass, number, "");
	}

	public void bindInPairs(ObjectPropertyEditor editor, BioPAXElement... pairs)
	{
		for (int i = 0; i < pairs.length; i++)
		{
			BioPAXElement bean = pairs[i++];
			BioPAXElement value = pairs[i];
			editor.setValueToBean(value, bean);
		}
	}

	public void bindArrays(ObjectPropertyEditor editor, BioPAXElement[] beans, BioPAXElement[] values)
	{
		for (int i = 0; i < beans.length; i++)
		{
			BioPAXElement bean = beans[i];
			BioPAXElement value = values[i];
			editor.setValueToBean(value, bean);
		}
	}

	public void bindInPairs(String editor, BioPAXElement... pairs)
	{
		this.bindInPairs(this.editor(editor, pairs[0].getModelInterface()),pairs);
	}

	public void bindArrays(String editor, BioPAXElement[] beans, BioPAXElement[] values)
	{
		this.bindArrays(this.editor(editor, (Class<? extends BioPAXElement>) beans.getClass().getComponentType()),
		                beans, values);
	}

	public ObjectPropertyEditor editor(String property, Class<? extends BioPAXElement> clazz)
	{
		return (ObjectPropertyEditor) map.getEditorForProperty(property, clazz);
	}

}