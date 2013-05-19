package org.biopax.paxtools.io.sif;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.io.sif.level3.Group;
import org.biopax.paxtools.io.sif.level3.InteractionSetL3;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

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
	/**
	 * Rules used during generation of interactions.
	 */
	private final InteractionRule[] rules;

	/**
	 * Log for logging.
	 */
	private final Log log = LogFactory.getLog(SimpleInteractionConverter.class);

	/**
	 * Options for interaction generation.
	 */
	private final Map options;

	/**
	 * Option to reduce complexes or use them as they are in interactions.
	 */
	public static final String REDUCE_COMPLEXES = "REDUCE_COMPLEXES";

	/**
	 * IDs of unwanted elements in SIF graph.
	 */
	private Set<String> blackList;

	/**
	 * Constructor with the mining rules to use.
	 * @param rules interaction rule set to be used in the conversion
	 */
	public SimpleInteractionConverter(InteractionRule... rules)
	{
		this(new HashMap(), rules);
	}

	/**
	 * Constructor with mining rules and options.
	 * @param options options to be used during the conversion process
	 * @param rules interaction rule set to be used in the conversion
	 */
	public SimpleInteractionConverter(Map options, InteractionRule... rules)
	{
		this(options, null, rules);
	}

	/**
	 * Constructor with mining rules, options, and black list.
	 * @param options options to be used during the conversion process
	 * @param blackList ids of molecules that we do not want them in SIF
	 * @param rules interaction rule set to be used in the conversion
	 */
	public SimpleInteractionConverter(Map options, Set<String> blackList, InteractionRule... rules)
	{

		this.blackList = blackList;
		this.rules = rules;
		this.options = options;
		for (InteractionRule rule : rules)
		{
			rule.initOptions(options);
		}
	}

	/**
	 * Infers simple interactions from the interactions found in the <em>model</em> for every
	 * interaction rule given; and returns this inferred simple interactions.
	 * @param model model from which simple interactions are going to be inferred
	 * @return a set of inferred simple interactions
	 */
	public Set<SimpleInteraction> inferInteractions(Model model)
	{

		switch (model.getLevel())
		{
			case L1:
			case L2:
				return inferL2(model);
			case L3:
				return inferL3(model);
			default:
				throw new IllegalBioPAXArgumentException("Unknown BioPAX Level");
		}
	}

	/**
	 * Infers interactions for L3 models.
	 * @param model L3 model
	 * @return inferred interactions
	 */
	private Set<SimpleInteraction> inferL3(Model model)
	{
		InteractionSetL3 interactions = new InteractionSetL3(model);

		Set<PhysicalEntity> bioPAXElements = model.getObjects(PhysicalEntity.class);
		for (PhysicalEntity er : bioPAXElements)
		{
			for (InteractionRule rule : rules)
			{
				tryInferringRule(model, interactions, er, rule);
			}
		}

        	interactions.convertGroupsToInteractions();

		if (blackList != null)
		{
			removeInteractionsWithBlackListMolecules(interactions, blackList);
		}
		return interactions;
	}

	/**
	 * Tries to use a rule to infer interactions. If an exception occurs other than
	 * MaximumInteractionThresholdExceedException, logs the error and continues.
	 * @param model model to use
	 * @param interactions inferred interactions
	 * @param bpe base element for inference
	 * @param rule current interaction rule
	 * @exception MaximumInteractionThresholdExceedException if generated rules are over a certain
	 * number
	 */
	private void tryInferringRule(Model model, InteractionSet interactions, BioPAXElement bpe,
			InteractionRule rule)
	{
		try
		{
			rule.inferInteractions(interactions, bpe, model);
		}
		catch (MaximumInteractionThresholdExceedException e)
		{
				throw e;
		}
		catch (Exception e)
		{
			log.error("Exception while applying rule :" + this.getClass().getSimpleName() + "to the element: " +
			          bpe.getRDFId(), e);
		}
	}

	/**
	 * Infers interactions on L2 models.
	 * @param model L2 model
	 * @return inferred interactions
	 */
	private Set<SimpleInteraction> inferL2(Model model)
	{
		InteractionSet interactions = new InteractionSet();
		Set<physicalEntity> bioPAXElements = model.getObjects(physicalEntity.class);

		for (physicalEntity pe : bioPAXElements)
		{
			for (InteractionRule rule : rules)
			{
				tryInferringRule(model, interactions, pe, rule);
			}
		}
		if (this.options.containsKey(REDUCE_COMPLEXES))
		{
			InteractionSet reduced = new InteractionSet();
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
	 * Filters out interactions whose source or target are in black list.
	 * @param interactions interactions to filter
	 * @param blackList IDs of unwanted elements
	 */
	protected void removeInteractionsWithBlackListMolecules(Set<SimpleInteraction> interactions,
			Set<String> blackList)
	{
		Iterator<SimpleInteraction> iter = interactions.iterator();
		while (iter.hasNext())
		{
			SimpleInteraction inter = iter.next();
			if (blackList.contains(inter.getSource().getRDFId()) ||
				blackList.contains(inter.getTarget().getRDFId()))
			{
				iter.remove();
			}
		}
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
		Set<SimpleInteraction> interactions = inferInteractions(model);
		Writer writer = new OutputStreamWriter(out);
		for (SimpleInteraction simpleInteraction : interactions)
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
			List<String> interactorPropertyPaths, List<String> mediatorPropertyPaths, boolean writeEntityTypes)
			throws IOException
	{
		Set<SimpleInteraction> interactions = inferInteractions(model);
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

		Set<String> lines = new TreeSet<String>(); //this is also to avoid duplicate records
		for (SimpleInteraction si : interactions)
		{
			StringBuilder sb = new StringBuilder(si.toString());
			entities.add(si.getSource());
			entities.add(si.getTarget());
			if (mediatorAccessors != null)
			{
				for (PathAccessor mediatorAccessor : mediatorAccessors)
				{

					HashSet values = new HashSet();

					for (BioPAXElement mediator : si.getMediators())
					{
						if (mediatorAccessor.applies(mediator))
						{
							values.add(valuesToString(mediatorAccessor.getValueFromBean(mediator)));
						}
					}
					sb.append("\t").append(values.isEmpty() ? "not applicable" : valuesToString(values));
				}
			}
			lines.add(sb.toString());
		}

		Writer writer = new OutputStreamWriter(edgeStream);
		writer.write(StringUtils.join(lines, "\n"));
		writer.flush();
		
		// now write nodes info
		writer = new OutputStreamWriter(nodeStream);
		for (BioPAXElement entity : entities)
		{
			if (entity != null)
			{

				writer.write(entity.getRDFId());
				if (writeEntityTypes) writer.write("\t" + getEntityTypeString(entity));
				if (interactorAccessors != null)
				{
					for (PathAccessor accessor : interactorAccessors)
					{
						writer.write("\t");
						if (accessor == null ||!accessor.applies(entity)) writer.write("(not applicable)");
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
		}

		writer.close();
	}

	/**
	 * Gets the type of the element.
	 * @param entity entity to ask its type
	 * @return type in string
	 */
	private String getEntityTypeString(BioPAXElement entity)
	{
		if(entity instanceof Group)
		{
			return ((Group) entity).groupTypeToString();
		}
		else
			return entity.getModelInterface().getSimpleName();
	}

	/**
	 * Prepared a semicolon separated string of values.
	 * @param values values to list
	 * @return string representing values
	 */
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

	/**
	 * Gets all available interaction rules for the given level.
	 * @param level BioPAX level
	 * @return available rules
	 */
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
			list.add(new org.biopax.paxtools.io.sif.level3.ExpressionRule());
		}
		return list;
	}
}
