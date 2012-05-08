package org.biopax.paxtools.controller;

import org.apache.commons.collections15.set.CompositeSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.impl.ModelImpl;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This is a "simple" BioPAX merger, a utility class to merge
 * 'source' BioPAX models or a set of elements into the target model,
 * using the RDFId (URI) identity only. Merging of normalized,
 * self-consistent models normally gives "better" results
 * (though it depends on the application).
 * <p/>
 * One can also "merge" a model to itself, i.e.: merge(target,target),
 * which adds those missing child elements that were not added
 * (explicitly) to the model (via model.add*) and makes it more integral.
 * <p/>
 * Note, "RDFId (URI) identity" means that it skips, i.e., does not copy
 * a source's element to the target model, if the target already contains the element 
 * with the same RDFId. However, after all,
 * it does update (re-wire) all the object properties of source elements
 * to make sure they do not refer to the skipped objects (from the "source") anymore
 * (if something is missing, it will be added at this second pass).
 * <p/>
 * Note also that this merger does not guarantee the integrity of the passed models:
 * 'target' will be the merged model (often, "more integral"), and the 'source'
 * may be trashed (in fact, - still somewhat usable, but modified for sure,
 * with some of its object properties now refer to target's elements).
 * <p/>
 * Finally, although called Simple Merger, it is in fact an advanced BioPAX utility,
 * which should be used wisely. Otherwise, it can actually waste resources.
 * So, consider using model.add(..), model.addNew(..) approach first (or instead),
 * especially, when you're adding "new" things (ID not present in the target model),
 * or/and target model does not contain any references to the source or another one, etc.
 */
public class SimpleMerger
{
	private static final Log log = LogFactory.getLog(SimpleMerger.class);

	private final EditorMap map;

	/**
	 * @param map a class to editor map for the elements to be modified.
	 */
	public SimpleMerger(EditorMap map)
	{
		this.map = map;
	}


	/**
	 * Merges the <em>source</em> models into <em>target</em> model.
	 * <p/>
	 * Note: both target and source models are not necessarily self-consistent,
	 * i.e., they may already contain external and dangling elements...
	 * @param target model into which merging process will be done
	 * @param sources models, if any, that are going to be merged/updated to <em>target</em>
	 */
	public void merge(Model target, Model... sources)
	{
		CompositeSet<BioPAXElement> objects = new CompositeSet<BioPAXElement>();
		/* collect all the objects and then merge at once 
		 * (do not merge for each model separately: this is not only less expensive 
		 * but also more reliable approach, because models may in fact overlap!)
		 */
		for (Model source : sources)
		{
			if (source != null)
			{
				objects.addComposited(source.getObjects());
			}
		}
		merge(target, objects.toCollection());
	}


	/**
	 * Merges the <em>elements</em> into <em>target</em> model.
	 * @param target model into which merging process will be done
	 * @param elements elements, if any, that are going to be merged/updated to <em>target</em>
	 */
	public void merge(Model target, Collection<? extends BioPAXElement> elements)
	{
		// First, fix 'target' model: find implicit objects
		// if there are different objects with the same URI, 
		// only one will be added to the model (no guarantee which one),
		// but next steps should fix it (update object references to the added one)
		@SuppressWarnings("unchecked") // safe, - no filters (empty array)
		final Fetcher fetcher = new Fetcher(map);
		for(BioPAXElement se :  new HashSet<BioPAXElement>(target.getObjects())) {
			fetcher.fetch(se, target);
		}
		
		// Second, auto-complete source 'elements' by discovering all the implicit elements there
		// copy all elements, as the collection can be immutable or unsafe to add elements there
		final Set<BioPAXElement> sources = new HashSet<BioPAXElement>(elements);
		for(BioPAXElement se : elements) {
			sources.addAll(fetcher.fetch(se));
		}
		
		
		// Next, we only copy elements having new URIs -
		for (BioPAXElement bpe : sources)
		{
			/* if there exists target element with the same id, 
			 * do not copy this one! (this 'source' element will 
			 * be soon replaced with the target's, same id, one 
			 * in all parent objects)
			 */
			if (!target.containsID(bpe.getRDFId()))
			{
				/*
				 * Warning: other than the default (ModelImpl) target Model 
				 * implementations may add child elements recursively (e.g., 
				 * using jpa cascades/recursion); it might also override target's
				 * properties with the corresponding ones from the source, even
				 * though SimpleMerger is not supposed to do this; also, is such cases,
				 * the number of times this loop body is called can be less that
				 * the number of elements in sourceElements set that were't
				 * originally present in the target model, or - even equals to
				 * one)
				 */
				target.add(bpe);
			} 
		}

		// Finally, update object references -
		// for all elements in the 'target', - because 'target' model 
		// itself might have had issues as well, particularly, more 
		// than one child object having the same URI (see comments above)
		for (BioPAXElement bpe : target.getObjects()) {
			updateObjectFields(bpe, target);
			updateInverseObjectFields(bpe, target);
		}
		
	}


	/**
	 * Merges the <em>source</em> element (and its "downstream" dependents)
	 * into <em>target</em> model if its RDFId is not yet there.
	 * <p/>
	 * Dependents, though, are not explicitly added to the target model,
	 * but the corresponding object properties of the element either
	 * become magically 'fixed' (point to target's elements if found)
	 * or "dangling" (not null though, but still refer to external objects,
	 * which simply will be skipped if one exports to OWL using
	 * e.g. SimpleIO).
	 * The same apply to other merge methods in this class.
	 * @param target
	 * @param source
	 */
	public void merge(Model target, BioPAXElement source)
	{
		merge(target, Collections.singleton(source));
	}


	/**
	 * Updates each value of <em>existing</em> element, using the value(s) of <em>update</em>.
	 * @param update BioPAX element of which values are used for update
	 */
	private void updateObjectFields(BioPAXElement update, Model target)
	{
		Set<PropertyEditor> editors = map.getEditorsOf(update);
		for (PropertyEditor editor : editors)
		{
			if (editor instanceof ObjectPropertyEditor)
			{
				Set<BioPAXElement> values = new HashSet<BioPAXElement>(
					(Set<BioPAXElement>) editor.getValueFromBean(update));
				for (BioPAXElement value : values) // threw concurrent modification exception here; fixed above.
				{
					migrateToTarget(update, target, editor, value);
				}
			}
		}
	}


	private void updateInverseObjectFields(BioPAXElement bpe, Model target)
	{
		// get inverse prop. editors, e.g., for xrefOf(), entityReferenceOf(), etc..
		// (in fact, they have same names as normal BioPAX properties, i.e., 'xref', etc..)
		Set<ObjectPropertyEditor> editors = map.getInverseEditorsOf(bpe);
		for (ObjectPropertyEditor editor : editors)
		{
			Set<BioPAXElement> values = new HashSet<BioPAXElement>(
				(Set<BioPAXElement>) editor.getInverseAccessor().getValueFromBean(bpe));
			for (BioPAXElement value : values) 
			{
				//extra fix for the default (in-memory) Model implementation only
				if(value != null && target instanceof ModelImpl && !target.contains(value)) {
					log.warn("Updating inverse property " + editor.getProperty() + 
						"Of: value " + value.getRDFId() + "(" + value.getModelInterface().getSimpleName() 
						+ ") " + " will be removed (not found in the merged model)");
					if (editor.isInverseMultipleCardinality()) {
						// mind the args order (it's reverse compared to "normal" editors use)!
						editor.removeValueFromBean(bpe, value);
					} else 
						editor.setValueToBean(null, value);
				}
			}
		}
	}
	

	private void migrateToTarget(BioPAXElement update, Model target, PropertyEditor editor, BioPAXElement value)
	{
		if (value != null)
		{
			BioPAXElement newValue = target.getByID(value.getRDFId());
			if (!newValue.equals(value)) {
				// newValue is a different, not null BioPAX element
				if (log.isDebugEnabled() && !newValue.isEquivalent(value))
				{
					String msg = "Updating property " + editor.getProperty() +
						"the replacement (target) object " + newValue + " (" +
					    newValue.getModelInterface().getSimpleName() + "), with the same URI (" +
					    newValue.getRDFId() + "), " + " is not equivalent to the source: " + 
					    value + " (" + value.getModelInterface().getSimpleName() + ")!";
					log.debug(msg); // we can live with it in some cases...(exception may be thrown below)
				}

				/* 
				 * "setValueToBean" comes first to prevent deleting of current value 
				 * even though it cannot be replaced with newValue 
				 * due to the property range error
				 */
				editor.setValueToBean(newValue, update);
				if (editor.isMultipleCardinality()) {
					editor.removeValueFromBean(value, update);
				}
			} 
		}
	}

}