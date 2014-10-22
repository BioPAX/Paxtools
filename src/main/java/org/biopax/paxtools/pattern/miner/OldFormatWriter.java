package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;

/**
 * This class is used for writing the binary interactions to a text output stream in the old
 * EXTENDED_BINARY_SIF format.
 *
 * @author Ozgun Babur
 */
public class OldFormatWriter
{
	/**
	 * Path accessors for participant properties.
	 */
	private static final PathAccessor[] ACCS = new PathAccessor[]{
		new PathAccessor("Named/displayName"),
		new PathAccessor("XReferrable/xref:UnificationXref"),
		new PathAccessor("XReferrable/xref:RelationshipXref")};

	/**
	 * Writes down the given interactions into the given output stream in the old
	 * EXTENDED_BINARY_SIF format. Closes the stream at the end.
	 * @param inters binary interactions
	 * @param out stream to write
	 * @return true if any output produced successfully
	 */
	public static boolean write(Set<SIFInteraction> inters, OutputStream out)
	{
		SIFToText stt = new CustomFormat(
			OutputColumn.Type.RESOURCE.name(),
			OutputColumn.Type.PUBMED.name());

		if (!inters.isEmpty())
		{
			List<SIFInteraction> interList = new ArrayList<SIFInteraction>(inters);
			Collections.sort(interList);
			try
			{
				OutputStreamWriter writer = new OutputStreamWriter(out);
				writer.write("PARTICIPANT_A\tINTERACTION_TYPE\tPARTICIPANT_B\t" +
					"INTERACTION_DATA_SOURCE\tINTERACTION_PUBMED_ID");
				for (SIFInteraction inter : interList)
				{
					writer.write("\n" + stt.convert(inter));
				}
				writeSourceAndTargetDetails(inters, writer);
				writer.close();
				return true;
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}

	private static void writeSourceAndTargetDetails(Collection<SIFInteraction> inters, Writer writer)
		throws IOException
	{
		Map<String, Set<BioPAXElement>> map = collectEntityRefs(inters);

		writer.write("\n\nPARTICIPANT\tPARTICIPANT_TYPE\tPARTICIPANT_NAME\t" +
			"UNIFICATION_XREF\tRELATIONSHIP_XREF");

		for (String id : map.keySet())
		{
			writer.write("\n" + id + "\t" + getParticipantTypes(map.get(id)));

			for (PathAccessor acc : ACCS)
			{
				writer.write("\t" + getValue(map.get(id), acc));
			}
		}
	}

	/**
	 * Collects the sources and targets in given interactions.
	 * @param inters binary interactions
	 * @return map from the primary id to the set of related source and target elements.
	 */
	private static Map<String, Set<BioPAXElement>> collectEntityRefs(Collection<SIFInteraction> inters)
	{
		Map<String, Set<BioPAXElement>> map = new HashMap<String, Set<BioPAXElement>>();

		for (SIFInteraction inter : inters)
		{
			if (!map.containsKey(inter.sourceID))
				map.put(inter.sourceID, new HashSet<BioPAXElement>());
			if (!map.containsKey(inter.targetID))
				map.put(inter.targetID, new HashSet<BioPAXElement>());

			map.get(inter.sourceID).addAll(inter.sourceERs);
			map.get(inter.targetID).addAll(inter.targetERs);
		}
		return map;
	}

	private static String getParticipantTypes(Set<BioPAXElement> elements)
	{
		Set<String> set = new HashSet<String>();
		for (BioPAXElement ele : elements)
		{
			String name = ele.getModelInterface().getName();
			set.add(name.substring(name.lastIndexOf(".") + 1));
		}
		List<String> list = new ArrayList<String>(set);
		Collections.sort(list);
		return concat(list);
	}

	private static String getValue(Set<BioPAXElement> elements, PathAccessor pa)
	{
		Set<String> set = new HashSet<String>();
		for (Object o : pa.getValueFromBeans(elements))
		{
			set.add(o.toString());
		}
		List<String> list = new ArrayList<String>(set);
		Collections.sort(list);
		return concat(list);
	}

	/**
	 * Concatenates the given collection of strings into a single string where values are separated
	 * with a semicolon.
	 * @param col string collection
	 * @return concatenated string
	 */
	private static String concat(Collection<String> col)
	{
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for (String s : col)
		{
			if (first) first = false;
			else b.append(";");

			b.append(s);
		}
		return b.toString();
	}
}
