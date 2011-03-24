package org.biopax.paxtools.controller;

import org.apache.commons.collections15.set.CompositeSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This is a "simple" BioPAX merger, a utility class to merge one 
 * BioPAX model (source) into the other (target), based on the RDFId (URI) identity. 
 * Merging of normalized, self-consistent models normally gives the best (self-consistent) result.
 * <p/>
 * Note, "based on the RDFId (URI) identity" means that it skips, i.e., does not copy 
 * a source's element to the target (model) nor updates sources' object properties, if the 
 * target contains the element with the same RDFId. However, where required, 
 * it will update (re-wire) all the object properties within the target model afterwards 
 * - to make sure they do not refer to the skipped objects (from "source") anymore.
 * <p/>
 * Note also that this merger does not preserve the integrity of the passed models! 
 * 'Target' will be a merged model (and often becomes "more integral"), and 'source' 
 * may become "unusable" (in fact, - still somewhat usable, but modified for sure, 
 * with some of its elements' object properties now refer to the "target" model elements).
 * <p/>
 * Finally, although called Simple Merger, it is in fact an advanced BioPAX utility, 
 * which should be used wisely. Otherwise, it can actually waste resources. 
 * So consider using model.add(..), model.addNew(..) approach first (or instead), 
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
	 * When no sources provided, it will just refresh all the object properties
	 * in the target model (this may actually fix dangling values, and generates useful logs)
	 *
	 * Note: both target and source models (would be nice to believe but) 
	 * are not necessarily self-consistent, i.e.,  
	 * may already have cross-links and dangling elements...
	 *
	 * @param target model into which merging process will be done
	 * @param sources models, if any, that are going to be merged with <em>target</em>
	 */
	public void merge(Model target, Model... sources) {
		CompositeSet<BioPAXElement> objects = new CompositeSet<BioPAXElement>();
		/* collect all the objects and then merge at once 
		 * (do not merge for each model separately: this is not only less expensive 
		 * but also more reliable approach, because models may in fact overlap!)
		 */
		for (Model source : sources) {
			if (source != null) {
				objects.addComposited(source.getObjects());
			}
		}
		merge(target, objects.toCollection());
	}
	
	
	/**
	 * Merges the <em>elements</em> into <em>target</em> model.
	 * When no elements provided, it will refresh all the object properties
	 * in the target model (this may actually fix dangling values)
	 *
	 * Note: both target and source models (would be nice to believe but) 
	 * are not necessarily self-consistent, i.e.,  
	 * may already have cross-links and dangling elements...
	 *
	 * @param target model into which merging process will be done
	 * @param elements elements, if any, that are going to be merged with <em>target</em>
	 */
	public void merge(Model target, Collection<? extends BioPAXElement> elements) 
	{
		for (BioPAXElement bpe : elements) {
			BioPAXElement targetElement = target.getByID(bpe.getRDFId());
			/*
			 * if there is present the element with the same id, skip, do not
			 * merge this one (see the warning below...)
			 */
			if (targetElement == null) {
				target.add(bpe);
				/*
				 * Warning: concrete target Model implementations may add not
				 * only 'bpe' but also all its dependents (using
				 * cascades/recursion); it might also override target's
				 * properties with the corresponding ones from the source, even
				 * though SimpleMerger tends to avoid this; also, is such cases,
				 * the number of times this loop body is called can be less that
				 * the number of elements in sourceElements set that were't
				 * originally present in the target model, or - even equals to
				 * one)
				 */
			}
		}
		
		/* Now that target model has all the IDs from both models
		 * (might also have an object property value that is not yet added to the model!)
		 * let's "re-wire" object relationships.
		 * 
		 * Remark: one may think she could iterate over 
		 * newly added elements only.., but, in fact,
		 * things are way more tricky (models intersect)...
		 * So, we are going to refresh the objects properties (re-link to 'target') 
		 * not only for just added elements, nor even for all source elements, 
		 * but - for all 'target' elements!
		 */
		Set<BioPAXElement> targetObjs = new HashSet<BioPAXElement>(target.getObjects());
		for (BioPAXElement targetElement : targetObjs)
		{
			updateObjectFields(targetElement, target);
		}
	}

	
	/**
	 * Merges the <em>source</em> element (and its "downstream" dependents) 
	 * into <em>target</em> model if its RDFId is not yet there. 
	 * 
	 * Dependents, though, are not explicitly added to the target model, 
	 * but the corresponding object properties of the element either 
	 * become magically 'fixed' (point to target's elements if found) 
	 * or "dangling" (not null though, but still refer to external objects,
	 * which simply will be skipped if one exports to OWL using 
	 * e.g. org.biopax.paxtools.io.simpleIO.SimpleExporter).
	 * The same apply to other merge methods in this class.
	 *
	 * @param target
	 * @param source
	 * 
	 */
	public void merge(Model target, BioPAXElement source)
	{
		merge(target, Collections.singleton(source));
	}


	/**
	 * Updates each value of <em>existing</em> element, using the value(s) of <em>update</em>.
	 *
	 * @param update BioPAX element of which values are used for update
	 */
	private void updateObjectFields(BioPAXElement update, Model target)
	{
		Set<PropertyEditor> editors =
				map.getEditorsOf(update);
		for (PropertyEditor editor : editors)
		{
			if (editor instanceof ObjectPropertyEditor)
			{
				if (editor.isMultipleCardinality())
				{
					Set<BioPAXElement> values = new HashSet<BioPAXElement>(
							(Set<BioPAXElement>) editor.getValueFromBean(update));
					for (BioPAXElement value : values) // threw concurrent modification exception here; fixed above.
					{
						migrateToTarget(update, target, editor, value);
					}
				}
				else
				{
					BioPAXElement value = (BioPAXElement) editor.getValueFromBean(update);
					migrateToTarget(update, target, editor, value);
				}
			}
		}
	}


	private void migrateToTarget(BioPAXElement update, Model target,
	                             PropertyEditor editor, BioPAXElement value)
	{
		if (value != null)
		{
			BioPAXElement newValue = target.getByID(value.getRDFId());
			if (newValue == null)
			{
				log.info("Target model does not have id=" + value.getRDFId()
					+ " (that aslo means, the value wasn't in the 'source' model);"
					//+ " won't touch this (but when exporting to OWL, this 'dangling' property becomes empty)"
					+ " adding it now!"
					);
				target.add(value);
				updateObjectFields(value, target); // recursion!
				return;
			}
			
			if(!value.isEquivalent(newValue)) {
				// are they at least of the same type?
				if(newValue.getModelInterface().equals(value.getModelInterface())) {
					String msg = "Target object value: " 
						+ newValue + " (" + newValue.getModelInterface().getSimpleName() 
						+ "), with the same RDFId (" + newValue.getRDFId() + "), "
						+ " might have a DIFFERENT semantics/type from the source's: " 
						+ value + " (" + value.getModelInterface().getSimpleName() + ")!";
					log.error(msg); // but we can live with it
				} //else - nothing - exception will be thrown below, anyway!
			}
			
			if (editor.isMultipleCardinality())
			{
				editor.removeValueFromBean(value, update);
			}
			editor.setValueToBean(newValue, update);
		}
	}

}