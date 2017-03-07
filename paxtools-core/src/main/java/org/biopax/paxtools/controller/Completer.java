package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.pathway;
import org.biopax.paxtools.model.level3.Pathway;

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

	private final Set<BioPAXElement> completed;

	private boolean skipSubPathways;

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

	/**
	 * @deprecated use {@link #complete(Collection)} instead (- model is never explicitly used there)
	 * @param elements
	 * @param model
	 * @return
	 */
	public Set<BioPAXElement> complete(Collection<BioPAXElement> elements, Model model) {
		return complete(elements);
	}

	public Set<BioPAXElement> complete(Collection<BioPAXElement> elements)
	{
		completed.clear();

		for (BioPAXElement element : elements)
		{
			if (!completed.contains(element))
			{
				completed.add(element);
				traverser.traverse(element, null); //model is not required here because of 'visit' impl. below
			}
		}

		return completed;
	}

	public void visit(BioPAXElement domain, Object range, Model model, PropertyEditor editor)
	{
		if (range instanceof BioPAXElement) //!=null works too (because of prop. filters)
		{
			BioPAXElement element = (BioPAXElement) range;
			if (!completed.contains(element))
			{
				completed.add(element);

				if( !(skipSubPathways && (element instanceof Pathway || element instanceof pathway)) )
					traverser.traverse(element, null);
			}
		}
	}

	/**
	 * Use this property to optionally
	 * skip (if true) traversing into sub-pathways;
	 * i.e., when the value of BioPAX property 'pathwayComponent' or 'controlled' is a pathway.
	 *
	 * @param skipSubPathways true/false
	 */
	public void setSkipSubPathways(boolean skipSubPathways) {
		this.skipSubPathways = skipSubPathways;
	}

	public boolean isSkipSubPathways() {
		return skipSubPathways;
	}
}
