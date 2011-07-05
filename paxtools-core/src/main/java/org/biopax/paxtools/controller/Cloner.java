package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.Model;

import java.util.Set;

/**
 * Specifically "Clones" the BioPAX elements set
 * (traverses to obtain dependent elements), 
 * puts them to the new model using the visitor and traverser framework;
 * ignores elements that are not in the source list (compare to {@link Fetcher})
 *
 * @see org.biopax.paxtools.controller.Visitor
 * @see org.biopax.paxtools.controller.Traverser
 */
public class Cloner implements Visitor
{
	Traverser traverser;
	private BioPAXFactory factory;
	private Model targetModel;

	public Cloner(EditorMap map, BioPAXFactory factory)
	{
		this.factory = factory;
		traverser = new Traverser(map, this);
	}

	
	/**
	 * For each element from the 'toBeCloned' list,
	 * it creates a copy in the new model, setting all 
	 * the data properties; however, object property values
	 * that refer to BioPAX elements not in 'toBeCloned' list
	 * are ignored.
	 * 
	 * @param source
	 * @param toBeCloned
	 * @return
	 */
	public Model clone(Model source, Set<BioPAXElement> toBeCloned)
	{
		targetModel = factory.createModel();

		for (BioPAXElement bpe : toBeCloned)
		{
			// make a copy (all properties are empty except for ID)
			targetModel.addNew(bpe.getModelInterface(), bpe.getRDFId());
		}

		for (BioPAXElement bpe : toBeCloned)
		{
			traverser.traverse(bpe, source);
		}

		return targetModel;
	}

// --------------------- Interface Visitor ---------------------

	public void visit(BioPAXElement domain, Object range, Model model, PropertyEditor editor)
	{
		BioPAXElement targetDomain = targetModel.getByID(domain.getRDFId());
		if (range instanceof BioPAXElement)
		{
			BioPAXElement bpe = (BioPAXElement) range;
			if (targetModel.containsID(bpe.getRDFId()))
			{
				editor.setValueToBean(targetModel.getByID(bpe.getRDFId()), targetDomain);
			} 
			else {
				// ignore the element that is not in the source list
			}
		}
		else
		{
			editor.setValueToBean(range, targetDomain);
		}
	}
}



