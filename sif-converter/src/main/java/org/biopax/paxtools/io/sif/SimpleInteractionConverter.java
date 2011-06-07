package org.biopax.paxtools.io.sif;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level3.EntityReference;

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
	 * @param rules interaction rule set to be used in the conversion
	 */
	public SimpleInteractionConverter(Map options, InteractionRule... rules)
	{
		this.options = options;
		this.rules = rules;
	}

	/**
	 * Infers simple interactions from the interactions found in the <em>model</em> for every
	 * interaction rule given; and returns this inferred simple interactions.
	 * @param model model from which simple interactions are going to be inferred
	 * @return a set of inferred simple interactions
	 */
	public Set<SimpleInteraction> inferInteractions(Model model)
	{
		if (model.getLevel() == BioPAXLevel.L2)
		{
			Set<SimpleInteraction> interactions = new HashSet<SimpleInteraction>();
			Set<physicalEntity> bioPAXElements = model.getObjects(physicalEntity.class);

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
						log.error("Exception while applying rule :" + this.getClass().getSimpleName() +
						          "to the element: " + pe.getRDFId(), e);
						if (e instanceof MaximumInteractionThresholdExceedException)
						{
							throw (MaximumInteractionThresholdExceedException) e;
						}
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
		} else if (model.getLevel() == BioPAXLevel.L3)
		{
			Set<SimpleInteraction> interactions = new HashSet<SimpleInteraction>();
			Set<EntityReference> bioPAXElements = model.getObjects(EntityReference.class);
			for (EntityReference er : bioPAXElements)
			{
				for (InteractionRule rule : rules)
				{
					try
					{
						rule.inferInteractions(interactions, er, model, options);
					}
					catch (Exception e)
					{
						log.error("Exception while applying rule :" + this.getClass().getSimpleName() +
						          "to the element: " + er.getRDFId(), e);
						if (e instanceof MaximumInteractionThresholdExceedException)
						{
							throw (MaximumInteractionThresholdExceedException) e;
						}
					}
				}
			}
			return interactions;
		} else return null;
	}

	/**
	 * Infers simple interactions from the <em>model</em> using {@link
	 * #inferInteractions(org.biopax.paxtools.model.Model)} and wrties them to an output stream.
	 * @param model model from which simple interactions are going to be inferred
	 * @param out output stream to which simple interactions will be written
	 * @exception IOException in case of problems with output.
	 */
	public void writeInteractionsInSIF(Model model, OutputStream out) throws IOException
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
	 * are separated by a semi column. For example the call
	 * converter.writeInteractionsInSIFNX(level2,out,true,"NAME","XREF");
	 * <p/>
	 * will output:
	 * <p/>
	 * id    aName   uniprot:xxx;entrez-gene:yyy
	 * <p/>
	 * in the entity section
	 * @param model
	 * @param edgeStream
	 * @exception IOException
	 */
	public void writeInteractionsInSIFNX(Model model, OutputStream edgeStream, OutputStream nodeStream,
	                                     List<String> interactorPropertyPaths, List<String> mediatorPropertyPaths)
			throws IOException
	{
		Set<SimpleInteraction> interactionSet = inferInteractions(model);
		Writer writer = new OutputStreamWriter(edgeStream);
		Set<BioPAXElement> entities = new HashSet<BioPAXElement>();
		List<PathAccessor> interactorAccessors = null;
		List<PathAccessor> mediatorAccessors = null;

		if (interactorPropertyPaths != null)
		{
			interactorAccessors = new ArrayList<PathAccessor>(interactorPropertyPaths.size());
			for (String s : interactorPropertyPaths)
			{
				interactorAccessors.add(new PathAccessor(s, model.getLevel()));
			}
		}
		if (mediatorPropertyPaths != null)
		{
			mediatorAccessors = new ArrayList<PathAccessor>(mediatorPropertyPaths.size());
			for (String s : mediatorPropertyPaths)
			{
				mediatorAccessors.add(new PathAccessor(s, model.getLevel()));
			}
		}


		for (SimpleInteraction si : interactionSet)
		{
			writer.write(si.toString());
			entities.add(si.getSource());
			entities.add(si.getTarget());
			if (mediatorAccessors != null)
			{
				for (PathAccessor mediatorAccessor : mediatorAccessors)
				{
					for (BioPAXElement mediator : si.getMediators())
					{
						StringBuilder cell = new StringBuilder("\t");
						if (mediatorAccessor.getDomain().isInstance(mediator))
						{
							cell.append(
									valuesToString(mediatorAccessor.getValueFromBean(mediator)));
						} else cell.append("not applicable");
						writer.write(cell.toString());
					}
				}
			}
			writer.write("\n");
		}
		writer.flush();
		writer = new OutputStreamWriter(nodeStream);
		for (BioPAXElement entity : entities)
		{
			if (entity == null) continue;
			
			writer.write(entity.getRDFId());
			if (interactorAccessors != null)
			{
				for (PathAccessor accessor : interactorAccessors)
				{
					writer.write("\t");
					if (accessor == null) writer.write("(not applicable)");
					else if (accessor.isUnknown(entity)) writer.write("(not specified)");
					else
					{
						Set values = accessor.getValueFromBean(entity);
						writer.write(valuesToString(values));
					}
				}
			}

			writer.write("\n");
		}
		writer.close();
	}

	private String valuesToString(Set values)
	{
		StringBuilder bldr = new StringBuilder();
		for (Object value : values)
		{
			bldr.append(value).append(";");
		}
		if (bldr.length() > 0) bldr.deleteCharAt(bldr.length() - 1);
		return bldr.toString();
	}

	public static List<InteractionRule> getRules(BioPAXLevel level)
	{
		List<InteractionRule> list = new ArrayList<InteractionRule>(5);
		if (level == BioPAXLevel.L2)
		{
			list.add(new org.biopax.paxtools.io.sif.level2.ComponentRule());
			list.add(new org.biopax.paxtools.io.sif.level2.ConsecutiveCatalysisRule());
			list.add(new org.biopax.paxtools.io.sif.level2.ControlRule());
			list.add(new org.biopax.paxtools.io.sif.level2.ControlsTogetherRule());
			list.add(new org.biopax.paxtools.io.sif.level2.ParticipatesRule());
			list.add(new org.biopax.paxtools.io.sif.level2.AffectsRule());
		} else if (level == BioPAXLevel.L3)
		{
			list.add(new org.biopax.paxtools.io.sif.level3.ComponentRule());
			list.add(new org.biopax.paxtools.io.sif.level3.ConsecutiveCatalysisRule());
			list.add(new org.biopax.paxtools.io.sif.level3.ControlRule());
			list.add(new org.biopax.paxtools.io.sif.level3.ControlsTogetherRule());
			list.add(new org.biopax.paxtools.io.sif.level3.ParticipatesRule());
		}
		return list;
	}
}