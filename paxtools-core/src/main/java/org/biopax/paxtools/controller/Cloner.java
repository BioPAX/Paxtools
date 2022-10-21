package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;

/**
 * Specifically "Clones" the BioPAX elements set
 * (traverses to obtain dependent elements), 
 * puts them to the new model using the visitor and traverser framework;
 * ignores elements that are not in the source list (compare to {@link Fetcher}).
 *
 * @see org.biopax.paxtools.controller.Visitor
 * @see org.biopax.paxtools.controller.Traverser
 */
public class Cloner implements Visitor
{
	private static final Logger LOG = LoggerFactory.getLogger(Cloner.class);
	
	private final Traverser traverser;
	private final BioPAXFactory factory;

	public Cloner(EditorMap map, BioPAXFactory factory)
	{
		this.factory = factory;
		this.traverser = new Traverser(map, this);
	}

	/**
	 * For each element from the 'toBeCloned' list,
	 * it creates a copy in the new model, setting all 
	 * the data properties; however, object property values
	 * that refer to BioPAX elements not in 'toBeCloned' list
	 * are ignored. As the result, e.g., a Pathway can be incomplete,
	 * missing some or all its sub-processes, xrefs, pathway steps.
	 * 
	 * @param toBeCloned elements to clone
	 * @return a new model containing the cloned biopax objects
	 */
	public Model clone(Collection<BioPAXElement> toBeCloned)
	{
		final Model targetModel = factory.createModel();

		for (BioPAXElement bpe : new HashSet<>(toBeCloned))
		{
			// create a new empty object using the URI (all the biopax properties are empty;
			// addNew fails when there are several objects having the same URI)
			targetModel.addNew(bpe.getModelInterface(), bpe.getUri());
		}

		// a hack to avoid unnecessary checks for the valid sub-model being cloned, 
		// and warnings when the Cloner copies BPS.stepProcess values, 
		// and there is a Conversion among them (-always unless stepConversion is null).
		AbstractPropertyEditor.checkRestrictions.set(false);
		
		for (BioPAXElement bpe : toBeCloned)
		{
			traverser.traverse(bpe, targetModel);
		}
		
		AbstractPropertyEditor.checkRestrictions.set(true); //back to the default mode

		return targetModel;
	}

	//  Implement interface: Visitor
	public void visit(BioPAXElement domain, Object range, Model targetModel, PropertyEditor editor)
	{
		BioPAXElement targetDomain = targetModel.getByID(domain.getUri());
		
		if (range instanceof BioPAXElement)
		{
			BioPAXElement bpe = (BioPAXElement) range;
			BioPAXElement existing = targetModel.getByID(bpe.getUri());
			//set the property value if the value is already present in the target 
			if (existing != null) {
				editor.setValueToBean(existing, targetDomain);
			}
		}
		else
		{
			editor.setValueToBean(range, targetDomain);
		}
	}
}



