package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Utility class to merge multiple biopax models into one. Note that this merger does not preserve
 * the integrity of the passed models. Target will be a merged model and source will become
 * unusable.
 */

public class Merger implements Visitor
{
// ------------------------------ FIELDS ------------------------------

	private static final Logger log = LoggerFactory.getLogger(Merger.class);

	private final Traverser traverser;

	private final HashMap<Integer, List<BioPAXElement>> equivalenceMap =
			new HashMap<Integer, List<BioPAXElement>>();
	private final EditorMap map;

	// Keep track of merged elements
	private final HashSet<BioPAXElement> mergedElements =
			new HashSet<>();

	// Keep track of new elements
	private final HashSet<BioPAXElement> addedElements = new HashSet<>();

	// --------------------------- CONSTRUCTORS ---------------------------

	/**
	 * @param map a class to editor map containing the editors for the elements of models to be
	 *            modified.
	 */
	public Merger(EditorMap map)
	{
		this.map = map;
		traverser = new Traverser(map, this);
	}

// ------------------------ INTERFACE METHODS ------------------------

	// --------------------- Interface Visitor ---------------------

	/**
	 * Checks whether <em>model</em> contains <em>bpe</em> element, and if it does, then it updates the
	 * value of the equivalent element for <em>bpe</em> by using the specific <em>editor</em>.
	 *
	 * @param domain owner 
	 * @param range property value
	 * @param model  model containing the equivalent element's equivalent
	 * @param editor biopax property editor specific for the value type to be updated
	 */
	public void visit(BioPAXElement domain, Object range, Model model, PropertyEditor editor)
	{
		if (range instanceof BioPAXElement)
		{
			BioPAXElement bpe = (BioPAXElement) range;

			// do nothing if you already inserted this
			if (!model.contains(bpe))
			{
				//if there is an identical
				if (model.getByID(bpe.getUri()) != null)
				{
					if (editor.isMultipleCardinality())
					{
						editor.removeValueFromBean(bpe, domain);
					}
					editor.setValueToBean(getIdentical(bpe), domain);
				}
			}
		}
	}

// -------------------------- OTHER METHODS --------------------------

	/**
	 * After a merge is accomplished, this set will contain the merged elements. This is not an
	 * essential method for paxtools functionality, but it may be useful for 3rd party applications.
	 *
	 * @return a hashet of merged elements in the target
	 * @see #merge
	 */
	public HashSet<BioPAXElement> getMergedElements()
	{
		return this.mergedElements;
	}

	/**
	 * After a merge is accomplished, this set will contain the newly added elements. This is not an
	 * essential method for paxtools functionallity, but it may be useful for 3rd party applications.
	 *
	 * @return a hashet of newly added elements in the target
	 * @see #merge
	 */
	public HashSet<BioPAXElement> getAddedElements()
	{
		return this.addedElements;
	}

	/**
	 * Merges the <em>source</em> models into <em>target</em> model.
	 *
	 * @param target  model into which merging process will be done
	 * @param sources model(s) that are going to be merged with <em>target</em>
	 */
	public void merge
			(Model target, Model... sources)
	{
		// Empty merged and added elements sets
		mergedElements.clear();
		addedElements.clear();

		// Fill equivalence map with objects from  target model
		Collection<BioPAXElement> targetElements = target.getObjects();
		for (BioPAXElement t_bpe : targetElements)
		{
			this.addIntoEquivalanceMap(t_bpe);
		}

		// Try to insert every biopax element in every source one by one
		for (Model source : sources)
		{
			Collection<BioPAXElement> sourceElements = source.getObjects();
			for (BioPAXElement bpe : sourceElements)
			{
				insert(target, bpe);
			}
		}
	}

	/**
	 * Insert the BioPAX element into the <em>target</em> model unless it's there already.
	 * But if target model has an "equal" object (same URI, type),
	 * update that object with this element's values not replacing the existing object
	 * (this can copy new property values, such as xrefs, comments, etc.)
	 *
	 * @param target model into which bpe will be inserted
	 * @param bpe    BioPAX element to be inserted into target
	 */
	private void insert(Model target, BioPAXElement bpe)
	{
		// do nothing if you already inserted this
		if (!target.contains(bpe)) //get by URI and also compare objects using '=='
		{
			//but if there is another object with same URI
			BioPAXElement ibpe = target.getByID(bpe.getUri());
			if (bpe.equals(ibpe)) {
				updateObjectFields(bpe, ibpe, target);
				// We have a merged element, add it into the tracker
				mergedElements.add(ibpe);
			}
			else {
				target.add(bpe);
				this.addIntoEquivalanceMap(bpe);
				traverser.traverse(bpe, target);
				// We have a new element, add it into the tracker
				addedElements.add(bpe);
			}
		}
	}

	/**
	 * Searches the target model for an identical of given BioPAX element, and returns this element if
	 * it finds it.
	 *
	 * @param bpe BioPAX element for which equivalent will be searched in target.
	 * @return the BioPAX element that is found in target model, if there is none it returns null
	 */
	private BioPAXElement getIdentical(BioPAXElement bpe)
	{
		int key = bpe.hashCode();
		List<BioPAXElement> list = equivalenceMap.get(key);
		if (list != null)
		{
			for (BioPAXElement other : list)
			{
				if (other.equals(bpe))
				{
					return other;
				}
			}
		}
		return null;
	}

	/**
	 * Updates each value of <em>existing</em> element, using the value(s) of <em>update</em>.
	 *
	 * @param update   BioPAX element of which values are used for update
	 * @param existing BioPAX element to be updated
	 * @param target
	 */
	private void updateObjectFields(BioPAXElement update,
	                                BioPAXElement existing, Model target)
	{
		Set<PropertyEditor> editors = map.getEditorsOf(update);
		for (PropertyEditor editor : editors) {
			updateObjectFieldsForEditor(editor, update, existing, target);
		}
	}

	/**
	 * Updates the value of <em>existing</em> element, using the value of <em>update</em>.
	 * Editor is used for the modification.
	 *
	 * @param editor   editor for the specific value to be updated
	 * @param update   BioPAX element of which value is used for the update
	 * @param existing BioPAX element to be updated
	 * @param target
	 */
	private void updateObjectFieldsForEditor(PropertyEditor editor,
	                                         BioPAXElement update,
	                                         BioPAXElement existing,
	                                         Model target)
	{
		if (editor.isMultipleCardinality())
		{
			for (Object updateValue : editor.getValueFromBean(update))
			{
				updateField(editor, updateValue, existing, target);
			}
		}
		else
		{
			Set existingValue = editor.getValueFromBean(existing);
			Set updateValue = editor.getValueFromBean(update);
			if (editor.isUnknown(existingValue))
			{
				if (!editor.isUnknown(updateValue))
				{
					updateField(editor, updateValue.iterator().next(), existing, target);
				}
			}
			else {
				if (!existingValue.equals(updateValue))
					log.info("Keep existing single cardinality property value: " +
						existingValue + " (ignore: " + updateValue + ")" );
			}
		}
	}

	/**
	 * Updates <em>existing</em>, using the <em>updateValue</em> by the editor.
	 *
	 * @param editor      editor for the specific value to be updated
	 * @param updateValue the value for the update
	 * @param existing    BioPAX element to be updated
	 * @param target
	 */
	private void updateField(PropertyEditor editor, Object updateValue,
	                         BioPAXElement existing, Model target)
	{
		if (updateValue instanceof BioPAXElement)
		{
			BioPAXElement bpe = (BioPAXElement) updateValue;
			//Now there are two possibilities


			BioPAXElement ibpe = target.getByID(bpe.getUri());
			//1. has an identical in the target
			if (ibpe != null)
			{
				updateValue = ibpe;
			}
			//2. it has no identical in the target
			//I do not have to do anything as it will eventually
			//be moved.

		}
		editor.setValueToBean(updateValue, existing);
	}

	/**
	 * Adds a BioPAX element into the equivalence map.
	 *
	 * @param bpe BioPAX element to be added into the map.
	 */
	private void addIntoEquivalanceMap(BioPAXElement bpe)
	{
		int key = bpe.hashCode();
		List<BioPAXElement> list = equivalenceMap.get(key);
		if (list == null)
		{
			list = new ArrayList<>();
			equivalenceMap.put(key, list);
		}
		list.add(bpe);
	}
}
