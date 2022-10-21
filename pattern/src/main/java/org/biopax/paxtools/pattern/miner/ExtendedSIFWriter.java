package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.RelationshipXref;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;

/**
 * This class is used for writing the binary interactions to a text output stream in the
 * so called Pathway Commons EXTENDED_BINARY_SIF format.
 *
 * @author Ozgun Babur
 */
public class ExtendedSIFWriter
{
	/**
	 * Path accessors for participant properties.
	 */
	private static final PathAccessor[] ACCS = new PathAccessor[]{
		new PathAccessor("Named/displayName"),
		new PathAccessor("XReferrable/xref:UnificationXref"),
		new PathAccessor("XReferrable/xref:RelationshipXref")};

	/**
	 * Writes down the given inferred binary interactions into the output stream
	 * using the Pathway Commons one-file EXTENDED_BINARY_SIF format,
	 * where edges (interactions) are written first, followed by a single
	 * blank line, followed by the nodes section (participant details and annotations).
	 * Closes the output stream at the end.
	 * @param inters binary interactions
	 * @param out stream to write
	 * @return true if any output produced successfully
	 */
	public static boolean write(Set<SIFInteraction> inters, OutputStream out)
	{
		SIFToText stt = new CustomFormat(
			OutputColumn.Type.RESOURCE.name(),
			OutputColumn.Type.PUBMED.name(),
			OutputColumn.Type.PATHWAY.name(),
			OutputColumn.Type.MEDIATOR.name());

		if (!inters.isEmpty())
		{
			List<SIFInteraction> interList = new ArrayList<>(inters);
			Collections.sort(interList);
			try {
				OutputStreamWriter writer = new OutputStreamWriter(out);
				writer.write("PARTICIPANT_A\tINTERACTION_TYPE\tPARTICIPANT_B\t" +
								"INTERACTION_DATA_SOURCE\tINTERACTION_PUBMED_ID\tPATHWAY_NAMES\tMEDIATOR_IDS");
				for (SIFInteraction inter : interList) {
					writer.write("\n" + stt.convert(inter));
				}
				writer.write("\n\n");//last line's EOL, + one blank line
				writeSourceAndTargetDetails(inters, writer);
				writer.close();
				return true;
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		return false;
	}

	/**
	 * Writes down the given interactions into the given "edges" output stream.
	 * Closes the stream at the end.
	 *
	 * @param inters binary interactions
	 * @param out stream to write
	 * @return true if any output produced successfully
	 */
	public static boolean writeInteractions(Set<SIFInteraction> inters, OutputStream out)
	{
		SIFToText stt = new CustomFormat(
				OutputColumn.Type.RESOURCE.name(),
				OutputColumn.Type.PUBMED.name(),
				OutputColumn.Type.PATHWAY.name(),
				OutputColumn.Type.MEDIATOR.name());

		if (!inters.isEmpty())
		{
			List<SIFInteraction> interList = new ArrayList<>(inters);
			Collections.sort(interList);
			try
			{
				OutputStreamWriter writer = new OutputStreamWriter(out);
				writer.write("PARTICIPANT_A\tINTERACTION_TYPE\tPARTICIPANT_B\t" +
						"INTERACTION_DATA_SOURCE\tINTERACTION_PUBMED_ID\tPATHWAY_NAMES\tMEDIATOR_IDS");
				for (SIFInteraction inter : interList)
				{
					writer.write("\n" + stt.convert(inter));
				}
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

	/**
	 * Writes down the interaction participants' (nodes) details
	 * to the given output stream. Closes the stream at the end.
	 *
	 * @param inters binary interactions
	 * @param out stream to write
	 * @return true if any output produced successfully
	 */
	public static boolean writeParticipants(Set<SIFInteraction> inters, OutputStream out)
	{
		if (!inters.isEmpty())
		{
			try
			{
				OutputStreamWriter writer = new OutputStreamWriter(out);
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
		//write the column headers row first
		writer.write("PARTICIPANT\tPARTICIPANT_TYPE\tPARTICIPANT_NAME\tUNIFICATION_XREF\tRELATIONSHIP_XREF");
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
				map.put(inter.sourceID, new HashSet<>());
			if (!map.containsKey(inter.targetID))
				map.put(inter.targetID, new HashSet<>());

			map.get(inter.sourceID).addAll(inter.sourceERs);
			map.get(inter.targetID).addAll(inter.targetERs);
		}
		return map;
	}

	private static String getParticipantTypes(Set<BioPAXElement> elements)
	{
		Set<String> set = new HashSet<>();
		for (BioPAXElement ele : elements)
		{
			String name = ele.getModelInterface().getName();
			set.add(name.substring(name.lastIndexOf(".") + 1));
		}
		List<String> list = new ArrayList<>(set);
		Collections.sort(list);
		return concat(list);
	}

	private static String getValue(Set<BioPAXElement> elements, PathAccessor pa)
	{
		Set<String> set = new HashSet<>();
		for (Object o : pa.getValueFromBeans(elements))
		{
			if(o instanceof RelationshipXref)
			{
				RelationshipXref rx = (RelationshipXref) o;
				if(rx.getRelationshipType() != null
						&& rx.getRelationshipType().toString().toLowerCase().contains("identity")) {
					set.add(rx.toString());
				}
			}
			else {
				set.add(o.toString());
			}
		}
		List<String> list = new ArrayList<>(set);
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
			if (first)
				first = false;
			else
				b.append(";");

			b.append(s);
		}
		return b.toString();
	}
}
