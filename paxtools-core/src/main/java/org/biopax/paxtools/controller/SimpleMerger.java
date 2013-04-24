package org.biopax.paxtools.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
 * which adds those implicit child elements that were not added
 * yet to the model (via model.add*) and makes it more integral.
 * <p/>
 * Note, "RDFId (URI) identity" means that it skips, i.e., does not copy
 * a source's element to the target model, if the target already contains an element 
 * with the same RDFId. However, it does update (re-wire) all the object properties
 * to make sure they do not refer to the skipped objects (from the "source") anymore
 * <p/>
 * Note also that this merger does not guarantee the integrity of the passed models:
 * 'target' will be the merged model (often, "more integral"), and the 'source'
 * may be trashed (in fact, - still somewhat usable,
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
	 * Merges the <em>source</em> models into <em>target</em> model,
	 * one after another (in the order they are listed).
	 * 
	 * If the target model is self-integral (complete) or empty, then
	 * the result of the merge will be also a complete model (contain 
	 * unique objects with unique URIs, all objects referenced from 
	 * any other object in the model).
	 * 
	 * Source models do not necessarily have to be complete and may even
	 * indirectly contain different objects of the same type with the same 
	 * URI. Though, in most cases, one probably wants target model be complete
	 * or empty for the best results. So, if your target is incomplete or you are
	 * not quite sure, do simply merge it as the first source to a new empty model.
	 *       
	 * @param target model into which merging process will be done
	 * @param sources models to be merged/updated to <em>target</em>; order can be important
	 */
	public void merge(Model target, Model... sources)
	{
		for (Model source : sources)
			if (source != null)
				merge(target, source.getObjects());
	}


	/**
	 * Merges the <em>elements</em> and all other (child) biopax objects
	 * they refer to into the <em>target</em> model.
	 * 
	 * @param target
	 * @param elements elements that are going to be merged/updated to <em>target</em>
	 */
	public void merge(Model target, Collection<? extends BioPAXElement> elements)
	{
		@SuppressWarnings("unchecked")
		final Fetcher fetcher = new Fetcher(map);
		
		// Auto-complete source 'elements' by discovering all the implicit elements there
		// copy all elements, as the collection can be immutable or unsafe to add elements to
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
				 * due to the property range error (setValueToBean throws exception)
				 */
				editor.setValueToBean(newValue, update);
				if(editor.isMultipleCardinality())
					editor.removeValueFromBean(value, update);
			} 
		}
	}

}