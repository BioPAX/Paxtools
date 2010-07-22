package org.biopax.paxtools.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.impl.ModelImpl;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;

import java.util.*;

/**
 * Utility class to merge two (normalized) 
 * biopax models into one based on the RDFId
 * (URI) identity.
 * 
 * Note that this merger does not preserve 
 * the integrity of the passed models! 
 * 'Target' will be a merged model and 
 * 'source' may become unusable.
 * 
 * Use With Care!
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
	 * Merges the <em>source</em> model into <em>target</em> model.
	 *
	 * @param target model into which merging process will be done
	 * @param source model that is going to be merged with <em>target</em>
	 */
	public void merge(Model target, Model source)
	{
		// this may work not as expected for some Models...
		if(!(target instanceof ModelImpl)) {
			log.warn("'target': using user's Model implementation, "
					+ target.getClass().getCanonicalName());
		}
		if(!(source instanceof ModelImpl)) {
			log.warn("'source': using user's Model implementation,"
					+ source.getClass().getCanonicalName());
		}
		
		// get all the objects from source, iterate
		Set<BioPAXElement> sourceElements = source.getObjects();
		for (BioPAXElement bpe : sourceElements)
		{
			BioPAXElement paxElement = target.getByID(bpe.getRDFId());
			/* 
			 * if there is an element with the same id,
			 * no not merge this one (see the warning below...)
			 */
			if (paxElement == null)
			{
				target.add(bpe);
				/* Warning: 
				 * concrete target Model implementations
				 * may add not only 'bpe' but also
				 * all its dependents (using cascades/recursion); 
				 * it might also override target's properties
				 * with the corresponding ones from the source, 
				 * even though SimpleMerger avoids this; 
				 * also, is such cases, the number of times
				 * this loop body is called can be less that
				 * the number of elements in sourceElements set 
				 * that were't originally present in the target 
				 * model, or - even equals to one)
				 */
			}
		}

		// "re-wire" object relationships
		for (BioPAXElement bpe : sourceElements)
		{
			updateObjectFields(bpe, target);
		}
	}

	/**
	 * Merges the <em>source</em> element 
	 * and its "downstream" dependents into <em>target</em> model.
	 * 
	 * @param target
	 * @param source
	 */
	public void merge(Model target, BioPAXElement source) {
		Model m = map.getLevel().getDefaultFactory().createModel();
		(new Fetcher(map)).fetch(source, m);
		merge(target, m);
	}
	
	
	/**
	 * Updates each value of <em>existing</em> element, using the value(s) of <em>update</em>.
	 *
	 * @param update BioPAX element of which values are ued for update
	 */
	private void updateObjectFields(BioPAXElement update, Model target)
	{
		Set<PropertyEditor> editors =
				map.getEditorsOf(update);
		for (PropertyEditor editor : editors)
		{
			if (editor instanceof ObjectPropertyEditor)
			{
				if(editor.isMultipleCardinality())
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
		if (value!=null) {
			BioPAXElement newValue = target.getByID(value.getRDFId());
			if(newValue == null) 
				throw new IllegalStateException("Target model must " +
					"have got the element with id=" + value.getRDFId()
					+ " at this point, but getById returned null!");
			editor.removePropertyFromBean(value,update);
			editor.setPropertyToBean(update, newValue);
		}
	}

}