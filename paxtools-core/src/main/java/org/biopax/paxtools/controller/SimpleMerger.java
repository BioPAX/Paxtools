package org.biopax.paxtools.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;

import java.util.*;

/**
 * Utility class to merge two already normalized biopax models into one. Note that this merger does not preserve
 * the integrity of the passed models. Target will be a merged model and source will become
 * unusable.
 */

public class SimpleMerger
{
// ------------------------------ FIELDS ------------------------------

	private static final Log log = LogFactory.getLog(SimpleMerger.class);

	private final EditorMap map;


	// --------------------------- CONSTRUCTORS ---------------------------

	/**
	 * @param map a class to editor map containing the editors for the elements of models to be
	 *            modified.
	 */
	public SimpleMerger(EditorMap map)
	{
		this.map = map;
	}

// ------------------------ INTERFACE METHODS ------------------------

	// --------------------- Interface Visitor ---------------------

// -------------------------- OTHER METHODS --------------------------


	/**
	 * Merges the <em>source</em> model into <em>target</em> model.
	 *
	 * @param target model into which merging process will be done
	 * @param source model that is going to be merged with <em>target</em>
	 */
	public void merge(Model target, Model source)
	{

		Set<BioPAXElement> sourceElements = source.getObjects();
		for (BioPAXElement bpe : sourceElements)
		{
			BioPAXElement paxElement = target.getByID(bpe.getRDFId());
			if (paxElement == null)
			{
				target.add(bpe);
			}

		}

		for (BioPAXElement bpe : sourceElements)
		{
			updateObjectFields(bpe, target);
		}
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
					Set<BioPAXElement> values = (Set<BioPAXElement>) editor.getValueFromBean(update);
					for (BioPAXElement value : values)
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
		if (value!=null && !target.contains(value))
		{
			BioPAXElement newValue = target.getByID(value.getRDFId());
			editor.removePropertyFromBean(value,update);
			editor.setPropertyToBean(update, newValue);
		}
	}

}