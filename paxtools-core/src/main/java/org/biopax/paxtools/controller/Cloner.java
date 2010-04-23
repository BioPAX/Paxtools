package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.Model;

import java.util.Set;

/**
 * This class is used to clone an element (traverse it to obtain its dependent elements) and to add
 * this element into a model using the visitor and traverser framework.
 *
 * @see org.biopax.paxtools.controller.Visitor
 * @see org.biopax.paxtools.controller.Traverser
 */
public class Cloner implements Visitor
{
	Traverser traverser;
	private EditorMap map;
	private BioPAXFactory factory;
	private Model currentTarget;

	public Cloner(EditorMap map, BioPAXFactory factory)
	{

		this.map = map;
		this.factory = factory;
		traverser = new Traverser(map, this);
	}

	public Model clone(Model source, Set<BioPAXElement> toBeCloned)
	{
		Model target = factory.createModel();
		for (BioPAXElement bpe : toBeCloned)
		{
			cloneInto(source, target, bpe);
		}
		return target;
	}

	/**
	 * Clones and adds the BioPAX element into the model and traverses the element for its dependent
	 * elements.
	 * @param source Source model for the objects to be cloned
	 * @param target Target model to add clones
	 * @param bpe BioPAXElement to be cloned
	 */
	public void cloneInto(Model source, Model target, BioPAXElement... bpe)
	{
		currentTarget = target;
		for (BioPAXElement paxElement : bpe)
		{
			traverser.traverse(paxElement, source);
		}
		currentTarget=null;
	}

// --------------------- Interface Visitor ---------------------

	public void visit(BioPAXElement domain, Object range, Model model, PropertyEditor editor)
	{
		if (range != null)
		{
			if (range instanceof BioPAXElement && !model.getObjects().contains(range))
			{
				BioPAXElement bpe = (BioPAXElement) range;

				BioPAXElement clone = factory.reflectivelyCreate(bpe.getClass());

				currentTarget.add(clone);
				traverser.traverse(bpe, model);
			}
			else
			{
				editor.setPropertyToBean(getClone(domain),range);
			}
		}

	}

	private BioPAXElement getClone(BioPAXElement domain)
	{
		return null;
	}


}



