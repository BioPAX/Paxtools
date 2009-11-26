package org.biopax.paxtools.io.sif;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.entity;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.publicationXref;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;

/**
 * Converts a BioPAX model to SIF (simple interaction format), by inferring the interactions in the
 * model, and describing them in terms of simple interactions.
 */
public class SimpleInteractionConverter
{
	private final InteractionRule[] rules;
	private final Log log = LogFactory.getLog(SimpleInteractionConverter.class);
	private final Map options;
	public static final String REDUCE_COMPLEXES = "REDUCE_COMPLEXES";

	/**
	 * @param rules interaction rule set to be used in the conversion
	 */
	public SimpleInteractionConverter(InteractionRule... rules)
	{
		this(new HashMap(), rules);
	}

	/**
	 * @param options options to be used during the conversion process
	 * @param rules   interaction rule set to be used in the conversion
	 */
	public SimpleInteractionConverter(Map options,
	                                  InteractionRule... rules)
	{
		this.options = options;
		this.rules = rules;
	}

	/**
	 * Infers simple interactions from the interactions found in the <em>model</em> for every
	 * interaction rule given; and returns this inferred simple interactions.
	 *
	 * @param model model from which simple interactions are going to be inferred
	 * @return a set of inferred simple interactions
	 */
	public Set<SimpleInteraction> inferInteractions(Model model)
	{
		Set<SimpleInteraction> interactions = new HashSet<SimpleInteraction>();
		Set<physicalEntity> bioPAXElements =
				model.getObjects(physicalEntity.class);
		for (physicalEntity pe : bioPAXElements)
		{
			for (InteractionRule rule : rules)
			{
				try
				{
					rule.inferInteractions(interactions, pe, model, options);
				}
				catch (Exception e)
				{
					log.error(
							"Exception while applying rule :" +
							this.getClass().getSimpleName() +
							"to the element: " +
							pe.getRDFId(), e);

				}
			}
		}
		if (this.options.containsKey(REDUCE_COMPLEXES))
		{
			Set<SimpleInteraction> reduced = new HashSet<SimpleInteraction>();
			for (SimpleInteraction si : interactions)
			{
				si.reduceComplexes(reduced);
			}
			interactions = reduced;
		}
		log.info(interactions.size() + " interactions inferred");
		return interactions;
	}

	/**
	 * Infers simple interactions from the <em>model</em> using {@link
	 * #inferInteractions(org.biopax.paxtools.model.Model)} and wrties them to an output stream.
	 *
	 * @param model model from which simple interactions are going to be inferred
	 * @param out   output stream to which simple interactions will be written
	 * @throws IOException in case of problems with output.
	 */
	public void writeInteractionsInSIF(Model model, OutputStream out)
			throws IOException
	{
		Set<SimpleInteraction> interactionSet = inferInteractions(model);
		Writer writer = new OutputStreamWriter(out);
		for (SimpleInteraction simpleInteraction : interactionSet)
		{
			writer.write(simpleInteraction.toString() + "\n");
		}
		writer.close();
	}

	/**
	 * This method outputs inferred interactions in sif annotation extended (sifnx). Sifnx is a tab
	 * delimited file with two sections. First section (interactions) is similar to sif - however
	 * there might be publication references(optional) next to each interaction line.
	 * <p/>
	 * The second section entities allows users easily define the properties they would like to
	 * extract from BioPAX model related to the interacting entities. It is in the form id <tab>
	 * property1 <tab> property2 <tab> property3. If the cardinality of property is multiple values
	 * are separated by a semi column. For example the call converter.writeInteractionsInSIFNX(level2,out,true,"NAME","XREF");
	 * <p/>
	 * will output:
	 * <p/>
	 * id    aName   uniprot:xxx;entrez-gene:yyy
	 * <p/>
	 * in the entity section
	 *
	 * @param model
	 * @param edgeStream
	 * @param writePublications
	 * @param entityProperty
	 * @throws IOException
	 */
	public void writeInteractionsInSIFNX(Model model,
	                                     OutputStream edgeStream,
	                                     OutputStream nodeStream,
	                                     boolean writePublications,
	                                     EditorMap map,
	                                     String... entityProperty
	) throws IOException
	{
		Set<SimpleInteraction> interactionSet = inferInteractions(model);
		Writer writer = new OutputStreamWriter(edgeStream);
		Set<entity> entities = new HashSet<entity>();

		List<PropertyEditor> editors = new LinkedList<PropertyEditor>();
		for (String s : entityProperty)
		{
			editors.add(map.getEditorForProperty(s, physicalEntity.class));

		}
		for (SimpleInteraction si : interactionSet)
		{
			writer.write(si.toString());
			entities.add((entity) si.getSource());
			entities.add((entity) si.getTarget());
			if (writePublications)
			{
				writer.write("\t");
				for (publicationXref px : si.getPubs())
				{
					writer.write(px + ";");
				}
			}
			writer.write("\n");
		}
		writer.flush();

		writer = new OutputStreamWriter(nodeStream);
		for (entity entity : entities)
		{
			writer.write(entity.getRDFId());
			for (PropertyEditor editor : editors)
			{
				writer.write("\t");
				if (editor.isMultipleCardinality())
				{
					Set values = (Set) editor.getValueFromBean(entity);
					for (Object value : values)
					{
						writer.write(value + ";");
					}
				}
				else
				{
					Object valueFromBean = editor.getValueFromBean(entity);
					String propertyString =
							(valueFromBean != null) ? valueFromBean.toString() : "NULL";
					writer.write(propertyString);
				}
			}
			writer.write("\n");
		}
		writer.flush();
	}
}