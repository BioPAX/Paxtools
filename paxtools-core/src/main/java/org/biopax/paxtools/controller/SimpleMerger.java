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
 * a source's element to the target (model) nor it updates sources' object properties,
 * if the target already contains the element with the same RDFId. However, after all,
 * it does update (re-wire) all the object properties of just added source elements
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
		for (BioPAXElement bpe : elements)
		{
			BioPAXElement targetElement = target.getByID(bpe.getRDFId());
			/*
			 * if there is present the element with the same id, skip, do not
			 * merge this one (see the warning below...)
			 */
			if (targetElement == null)
			{
				target.add(bpe);
				/*
				 * Warning: concrete target Model implementations may add 
				 * child elements automatically (e.g., using jpa
				 * cascades/recursion); it might also override target's
				 * properties with the corresponding ones from the source, even
				 * though SimpleMerger is not supposed to do this; also, is such cases,
				 * the number of times this loop body is called can be less that
				 * the number of elements in sourceElements set that were't
				 * originally present in the target model, or - even equals to
				 * one)
				 */
			}
		}

		/* 
				 * Now that target model contains all source IDs,
				 * although it might still refer to "external" child objects,
				 * (i.e., such property values that were not listed in the
				 * sources, thus - not in target model map yet),
				 * let's update new objects' object fields to target's values:
				 *
				 * REM: here we iterate over all source elements!
				 * But, things can be more tricky (when models already intersect
				 * or the target refers to external child elements), in which
				 * case, one may (but not necessarily have to) refresh the properties
				 * (re-link everything to target's) by calling merge(target,target) -
				 * i.e., merge to itself!
				 */
		for (BioPAXElement bpe : elements)
		{
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
				Set<BioPAXElement> values = new HashSet<BioPAXElement>((Set<BioPAXElement>) editor.getValueFromBean(
						update));
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

			if (newValue == null) // not yet in the target model
			{
				if(log.isDebugEnabled())
					log.debug("Target model does not have " + value.getRDFId() +
				         " (i.e, a prop. value wasn't in the source model either);" 
						+ " adding now... bean: "+ update.getRDFId() + " property: "
						+ editor.getProperty());
				target.add(value);
				updateObjectFields(value, target); // recursion!
			} else if (!value.equals(newValue))
			{
				// newValue is a different, not null BioPAX element
				if (!value.isEquivalent(newValue))
				{
					String msg = "(Updating object fields) " + "the replacement (target) object " + newValue + " (" +
					             newValue.getModelInterface().getSimpleName() + "), with the same RDFId (" +
					             newValue.getRDFId() + "), " + " is not equivalent to the source: " + value + " (" +
					             value.getModelInterface().getSimpleName() + ")!";
					log.warn(msg); // we can live with it in some cases...
					//(exception may be thrown below)
				}

				/* 
				 * "setValueToBean" comes first to prevent deleting of current value 
				 * even though it cannot be replaced with newValue 
				 * due to the property range error
				 */
				editor.setValueToBean(newValue, update);
				if (editor.isMultipleCardinality())
				{
					editor.removeValueFromBean(value, update);
				}
			} else
			{
				// skip (same values)
			}

		}
	}

}