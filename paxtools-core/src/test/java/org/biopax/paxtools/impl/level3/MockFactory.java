package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.controller.*;
import org.biopax.paxtools.impl.BioPAXFactoryAdaptor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.lang.reflect.Field;
import java.util.*;

/**
 * This factory returns decorated objects for testing.
 */
public class MockFactory extends BioPAXFactoryAdaptor
{
// ------------------------------ FIELDS ------------------------------



    private static final List<String> strings = Arrays
            .asList(" ",
                    "alpha",
                    "beta",
                    "gamma",
                    "_~/-\\\t\b,",
                    "\udddd\ucccc\uaaaa\ubbbb");
    private static final List<Float> floats =
            Arrays.asList(Float.MAX_VALUE, 1.0F, 0.0F, Float.MIN_VALUE);
    private static final List<Double> doubles =
            Arrays.asList(Double.MAX_VALUE, 1.0, 0.0, Double.MIN_VALUE);
    private static final List<Integer> ints =
            Arrays.asList(Integer.MAX_VALUE, 1, 0, Integer.MIN_VALUE + 1);
    private static final List<Boolean> booleans =
            Arrays.asList(Boolean.TRUE,Boolean.FALSE);

    private static final String base="mock://id/";

    private int id = 0;


    private final EditorMap map = SimpleEditorMap.L3;
    private BioPAXLevel level;

// --------------------------- CONSTRUCTORS ---------------------------

    public MockFactory(BioPAXLevel level)
    {
      this.level = level;
    }

    @Override
    public <T extends BioPAXElement> T create(Class<T> aClass, String uri)
    {
        T t = this.getLevel().getDefaultFactory().create(aClass, uri);
        populateMock(t);
        return t;
    }

    @Override
    public BioPAXElement create(String localName, String uri) {
        BioPAXElement bpe = this.getLevel().getDefaultFactory().create(localName, uri);
        populateMock(bpe);
        return bpe;
    }

    private void populateMock(BioPAXElement bpe) {
        Set<PropertyEditor> propertyEditors =
                map.getEditorsOf(bpe);

        for (PropertyEditor propertyEditor : propertyEditors) {
            boolean multiple = propertyEditor.isMultipleCardinality();
            Object value = null;
            if (propertyEditor instanceof StringPropertyEditor) {
                value = multiple ? strings : strings.get(4);
            } else {
                Class range = propertyEditor.getRange();
                if (propertyEditor instanceof PrimitivePropertyEditor) {
                    if (range == float.class) {
                        value = multiple ? floats : floats.get(1);
                    } else if (range == double.class) {
                        value = multiple ? doubles : doubles.get(1);
                    } else if (range == int.class) {
                        value = multiple ? ints : ints.get(1);
                    }
                    else if (range == Boolean.class) {
                        value = multiple ? booleans : booleans.get(1);
                    }
                } else if (propertyEditor instanceof EnumeratedPropertyEditor) {
                    Field[] fields = range.getFields();
                    if (multiple) {
                        value = new HashSet();
                    }
                    for (Field field : fields) {
                        if (field.isEnumConstant()) {
                            try {
                                if (multiple) {
                                    ((Set) value).add(field.get(bpe));
                                } else {
                                    value = field.get(bpe);
                                    break;
                                }
                            }
                            catch (IllegalAccessException e) {
                                throw new IllegalBioPAXArgumentException();
                            }
                        }
                    }
                } else {
                    if (!Entity.class.isAssignableFrom(range)) {
                        if (multiple) {
                            value =
                                    createRestrictedMock((ObjectPropertyEditor) propertyEditor, bpe, 3);
                        } else {
                            value = createRestrictedMock((ObjectPropertyEditor) propertyEditor, bpe, 1)
                                    .iterator().next();
                        }
                    }
                }
            }
            if (value != null) {
                if (multiple) {
                    Collection values = ((Collection) value);
                    if (!values.isEmpty()) {
                        Integer max =
                                propertyEditor.getMaxCardinality(
                                        bpe.getModelInterface());
                        values = upToMax(values, max);

                        for (Object o : values) {
                            propertyEditor.setValueToBean(o, bpe);
                        }
                    } else {
                        propertyEditor.setValueToBean(value, bpe);
                    }
                }
            }
        }
    }

    private HashSet<BioPAXElement> createRestrictedMock(ObjectPropertyEditor propertyEditor,
                                                        BioPAXElement bpe, int k) {
        HashSet<BioPAXElement> hashSet = new HashSet<BioPAXElement>();


	   Set<Class<? extends BioPAXElement>> rranges = propertyEditor.getRestrictedRangesFor(
			   bpe.getModelInterface());
	    for (Class<? extends BioPAXElement> rrange : rranges)
	    {
		      hashSet.add(createMock(rrange, bpe.getModelInterface()));
	    }
        return hashSet;
    }

    private BioPAXElement createMock(Class toCreate, Class domain) {
        assert domain != null;
        Class actual;
        actual = findConcreteMockClass(toCreate, domain);
        if (actual != null) {
            return map.getLevel().getDefaultFactory()
            	.create(actual, base+id++);
        } else {
            System.out.println("actual = " + actual);
            System.out.println("toCreate = " + toCreate);
            return null;
        }
    }

    private Class findConcreteMockClass(Class toCreate, Class domain) {
        Class actual = null;
        if (map.getLevel().getDefaultFactory()
                .canInstantiate(toCreate)
                &&
                !toCreate.isAssignableFrom(domain)) {
            actual = toCreate;
        } else {
            Set<Class<? extends BioPAXElement>> classesOf = map.getKnownSubClassesOf(toCreate);
            for (Class subclass : classesOf) {
                if (!subclass.isAssignableFrom(domain) &&
                        subclass != toCreate &&
                        subclass.getPackage().getName()
                                .startsWith("org.biopax.paxtools.model")) {
                    actual = findConcreteMockClass(subclass, domain);
                    break;
                }
            }
        }
        if(actual==null)
        {
            System.out.println("Failed to find restricted domain:" + domain +"for class " +toCreate +". " +
                    "This might be a bug or a self reference that might cause cycles");
        }
        return actual;
    }

    private Collection upToMax(Collection values, Integer max) {
        int size = values.size();
        if (max != null && max < size) {
            values = new ArrayList(values);
            for (int i = size - 1; i == max; i--) {
                ((List) values).remove(i);
            }
            assert values.size() == max;
        }
        return values;
    }



    @Override
    public <T extends BioPAXElement> T createInstance(Class<T> aClass, String uri) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        throw new UnsupportedOperationException();
    }


    public BioPAXLevel getLevel() {
        return this.level;
    }
}