package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.ObjectPropertyEditor;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;

import java.lang.reflect.Array;

public class Mock
{

	public MockFactory factory = new MockFactory(BioPAXLevel.L3);

	public Model model = factory.createModel();

	public EditorMap map = SimpleEditorMap.get(BioPAXLevel.L3);

	public <T extends BioPAXElement> T[] create(Class<T> biopaxClass, int number, String pre)
	{
		T[] result = (T[]) Array.newInstance(biopaxClass, number);

		for (int i = 0; i < number; i++)
		{
			result[i] = model.addNew(biopaxClass, biopaxClass.getSimpleName() + pre + i);
		}
		return result;
	}

	public <T extends BioPAXElement> T[] create(Class<T> biopaxClass, int number)
	{
		return this.create(biopaxClass, number, "");
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
