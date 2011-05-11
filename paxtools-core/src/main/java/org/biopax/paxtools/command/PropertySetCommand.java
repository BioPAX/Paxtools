package org.biopax.paxtools.command;

import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.Set;

public class PropertySetCommand<D extends BioPAXElement, R> extends AbstractPropertyCommand<D,R>
{
	private R  oldValue;

	PropertySetCommand (D bpe, PropertyEditor<D,R> editor, R value)
	{
		super(bpe, editor, value);
		if (editor.isMultipleCardinality())
		{
			throw new IllegalBioPAXArgumentException();
		}

		Set<R> valueFromBean = editor.getValueFromBean(bpe);
		if(valueFromBean == null || valueFromBean.isEmpty()) oldValue = null;
		else if(valueFromBean.size()>1)
		{
			throw new IllegalBioPAXArgumentException();
		}
		oldValue = valueFromBean.iterator().next();
	}


	@Override
	protected void undoAction()
	{
		editor.setValueToBean(oldValue, bpe);

	}



	@Override
	protected void redoAction()
	{
		editor.setValueToBean(value, bpe);
	}


	public String getPresentationName()
	{
		return "Set " + editor.getProperty() + " of " + bpe + " to " + value;
	}

	public String getUndoPresentationName()
	{
		return "Set " + editor.getProperty() + " of " + bpe + " to " + oldValue;
	}

	public String getRedoPresentationName()
	{
		return "Set " + editor.getProperty() + " of " + bpe + " to " + value;
	}
}
