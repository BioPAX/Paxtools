package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.Model;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
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
	private BioPAXFactory factory;
	private Model targetModel;
	private Map<String, BioPAXElement> sourceMap;
	private Map<String, BioPAXElement> targetMap;

	public Cloner(EditorMap map, BioPAXFactory factory)
	{
		this.factory = factory;
		traverser = new Traverser(map, this);
		sourceMap = new HashMap<String, BioPAXElement>();
		targetMap = new HashMap<String, BioPAXElement>();
	}

	public Model clone(Model source, Set<BioPAXElement> toBeCloned)
	{
		targetModel = factory.createModel();

		for (BioPAXElement bpe : toBeCloned)
		{
			sourceMap.put(bpe.getRDFId(), bpe);
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
		if (!targetMap.containsKey(domain.getRDFId()))
		{
			BioPAXElement targetDomain = factory.reflectivelyCreate(domain.getModelInterface());
			targetDomain.setRDFId(domain.getRDFId());
			targetMap.put(targetDomain.getRDFId(), targetDomain);
			targetModel.add(targetDomain);
		}

		if (range instanceof BioPAXElement)
		{
			BioPAXElement bpe = (BioPAXElement) range;
			if (sourceMap.containsKey(bpe.getRDFId()))
			{
				if (!targetMap.containsKey(bpe.getRDFId()))
				{
					traverser.traverse(bpe, model);
				}

				editor.setPropertyToBean(
					targetMap.get(domain.getRDFId()), targetMap.get(bpe.getRDFId()));
			}
		}
		else
		{
			editor.setPropertyToBean(targetMap.get(domain.getRDFId()), range);
		}
	}
}



