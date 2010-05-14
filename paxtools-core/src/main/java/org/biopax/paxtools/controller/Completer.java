package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is used for getting a valid subgraph from a set of BioPAX elements. For instance, if a
 * BioPAX graph contains a complex with some members, but the subgraph contains the same complex
 * without the members, then it is not valid. Similarly, an interaction is only valid with its
 * participants; however, including its controls is optional.
 *
 * @author Ozgun Babur
 */
public class Completer implements Visitor
{
	protected TraverserBilinked traverser;

	Set<BioPAXElement> completed;

	public Completer(EditorMap map)
	{
		this.traverser = new TraverserBilinked(map, this, new PropertyFilterBilinked()
		{
			public boolean filter(PropertyEditor editor)
			{
				return editor instanceof ObjectPropertyEditor &&
					((ObjectPropertyEditor) editor).isCompleteForward();
			}

			public boolean filterInverse(PropertyEditor editor)
			{
				return editor instanceof ObjectPropertyEditor &&
					((ObjectPropertyEditor) editor).isCompleteBackward();
			}
		});

		completed = new HashSet<BioPAXElement>();
	}

	public Set<BioPAXElement> complete(Collection<BioPAXElement> elements, Model model)
	{
		for (BioPAXElement element : elements)
		{
			if (!completed.contains(element))
			{
				completed.add(element);
				traverser.traverse(element, model);
			}
		}

		return completed;
	}

	public void visit(BioPAXElement domain, Object range, Model model, PropertyEditor editor)
	{
		if (range instanceof BioPAXElement)
		{
			BioPAXElement element = (BioPAXElement) range;

			if (!completed.contains(element))
			{
				completed.add(element);

				traverser.traverse(element, model);
			}
		}
	}
}
