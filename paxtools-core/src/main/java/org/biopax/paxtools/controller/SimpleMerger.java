package org.biopax.paxtools.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.util.Filter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A "simple" BioPAX merger, a utility class to merge
 * 'source' BioPAX models or a set of elements into the target model,
 * using (URI) identity only. Merging into a normalized,
 * self-consistent model normally gives better results 
 * (it depends on the application though).
 * 
 * One can also "merge" a model to itself, i.e.: merge(target,target),
 * or to an empty one, which adds all implicit child elements 
 * to the model and makes it self-integral.
 * 
 * Note, "URI identity" means that it does not copy
 * a source element to the target model if the target already has an element 
 * with the same URI. However, it will update (re-wire) all the object 
 * properties of new elements to make sure they do not refer to any objects 
 * outside the updated target model.
 * 
 * We do not guarantee the integrity of the source models after the merge is done
 * (some object properties will refer to target elements).
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
	
	private Filter<BioPAXElement> mergeObjPropOf;

	/**
	 * @param map a class to editor map for the elements to be modified.
	 */
	public SimpleMerger(EditorMap map)
	{
		this.map = map;
	}

	/** 
	 * @param map a class to editor map for the elements to be modified.
	 * @param mergeObjPropOf when not null, all multiple-cardinality properties 
	 * 						of a source biopax object that passes this filter are updated
	 * 						and also copied to the corresponding (same URI) target object,
	 *                      unless the source and target are the same thing 
	 *                      (in which case, we simply migrate object properties 
	 *                      to target model objects). 
	 */
	public SimpleMerger(EditorMap map, Filter<BioPAXElement> mergeObjPropOf)
	{
		this(map);
		this.mergeObjPropOf = mergeObjPropOf;
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
				 * the number of elements in sourceElements set that weren't
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
	 * @param target the BioPAX model to merge into
	 * @param source object to add or merge
	 */
	public void merge(Model target, BioPAXElement source)
	{
		merge(target, Collections.singleton(source));
	}


	/**
	 * Updates each value of <em>existing</em> element, using the value(s) of <em>update</em>.
	 * @param source BioPAX element of which values are used for update
	 * @param target the BioPAX model
	 */
	private void updateObjectFields(BioPAXElement source, Model target)
	{
		//Skip if target model had another object with the same URI, 
		//provided that "always copy" filters were not set;
		//i.e., source is to be entirely replaced with another object 
		//with the same URI already present in the target model.
		BioPAXElement keep = target.getByID(source.getRDFId());
		if(keep != source && mergeObjPropOf==null) 
		{
			return; //nothing to do
		}
		
		Set<PropertyEditor> editors = map.getEditorsOf(source);
		for (PropertyEditor editor : editors)
		{
			if (editor instanceof ObjectPropertyEditor)
			{
				//copy set of prop. values (to avoid concurrent modification exception)
				Set<BioPAXElement> values = new HashSet<BioPAXElement>(
						(Set<BioPAXElement>) editor.getValueFromBean(source));
				if(keep == source) 
				{
					for (BioPAXElement value : values) {
						migrateToTarget(source, target, editor, value);
					}
				} else if(mergeObjPropOf!=null && mergeObjPropOf.filter(source)
						&& editor.isMultipleCardinality()) //copy only for mul. card. props
				{
					for (BioPAXElement value : values) {
						mergeToTarget(keep, target, editor, value);
					}
				}
			} else //primitive or string property editor (e.g., comment, name)
				if(mergeObjPropOf!=null && mergeObjPropOf.filter(source)
					&& editor.isMultipleCardinality()) //copy only for mul. card. props
			{
				Set<Object> values = new HashSet<Object>(
						(Set<Object>) editor.getValueFromBean(source));
				for (Object value : values) {
					mergeToTarget(keep, target, editor, value);
				}
			}
		}
	}
	

	private void migrateToTarget(BioPAXElement source, Model target, PropertyEditor editor, BioPAXElement value)
	{
		if (value != null)
		{
			BioPAXElement newValue = target.getByID(value.getRDFId());
			//not null at this point, because every source element was found 
			//and either added to the target model, or target had an object with the same URI (to replace this value).
			assert newValue != null : "'newValue' is null (a design flaw in the 'merge' method)";		
			if (newValue != value) { //not using 'equals' intentionally
				editor.removeValueFromBean(value, source);
				editor.setValueToBean(newValue, source);
			} 
		}
	}
	
	private void mergeToTarget(BioPAXElement targetElement, Model target, PropertyEditor editor, Object value)
	{
		if (value != null) {
			Object newValue = (value instanceof BioPAXElement) 
					? target.getByID(((BioPAXElement)value).getRDFId()) : value;
			editor.setValueToBean(newValue, targetElement);
		}
	}

}