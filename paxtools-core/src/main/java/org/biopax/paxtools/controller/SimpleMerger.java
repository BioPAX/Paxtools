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
 * using the RDFId (URI) identity only. Merging into a normalized,
 * self-consistent model normally gives "better" results 
 * (it depends on the application though).
 * 
 * One can also "merge" a model to itself, i.e.: merge(target,target),
 * or to an empty one, which adds all implicit child elements 
 * to the model and makes it complete.
 * 
 * Note, "RDFId (URI) identity" means that it skips, i.e., does not copy
 * a source's element to the target model, if the target already contains the element 
 * with the same URI. However, it will update (re-wire) all the object properties
 * to make sure they do not refer to objects outside the updated target model anymore.
 * 
 * We not guarantee the integrity of the source models after the merge is done
 * (those may be still usable; all object properties will refer to target's elements).
 * 
 * Finally, although called Simple Merger, it is in fact an advanced BioPAX utility,
 * which should be used wisely. Otherwise, it can actually waste resources.
 * So, consider using model.add(..), model.addNew(..) approach first (or instead),
 * especially, when you're adding "new" things (ID not present in the target model),
 * or/and target model does not contain any references to the source or another one, etc.
 */
public class SimpleMerger
{
	private static final Log LOG = LogFactory.getLog(SimpleMerger.class);

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
	 * or empty for the best possible results. So, if your target is incomplete, 
	 * or you are not quite sure, then do simply merge it as the first source 
	 * to a new empty model or itself (or call {@link Model#repair()} first).
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
	 * Merges the <em>elements</em> and all their child biopax objects
	 * into the <em>target</em> model.
	 * 
	 * @see #merge(Model, Model...) for details about the target model.
	 * 
	 * @param target model into which merging will be done
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

		// Finally, update object references
		for (BioPAXElement bpe : sources) {
			updateObjectFields(bpe, target);
		}
		
	}


	/**
	 * Merges the <em>source</em> element (and its "downstream" dependents)
	 * into <em>target</em> model.
	 * 
	 * @see #merge(Model, Collection)
	 * 
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
			assert newValue != null : "'newValue' is null (there's a design flow in the 'merge' method)";
			if (newValue != null && newValue != value) {//not using 'equals' here intentionally
				editor.removeValueFromBean(value, update);
				editor.setValueToBean(newValue, update);
			} 
		}
	}

}