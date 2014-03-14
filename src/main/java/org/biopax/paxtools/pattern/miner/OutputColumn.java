package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.controller.PathAccessor;

import java.util.*;

/**
 * This class is used for defining a custom column in the te
 * @author Ozgun Babur
 */
public class OutputColumn
{
	/**
	 * Type of the output column.
	 */
	private Type type;
	private PathAccessor[] accessors;

	/**
	 * Constructor with output type. The <code>field</code> parameter can either be a value among
	 * OutputValue.Type enum (excluding the value <code>CUSTOM</code>), or a PathAccessor string to
	 * apply to mediator objects.
	 * @param field type of the custom field
	 */
	public OutputColumn(String field)
	{
		if (field == null)
		{
			throw new IllegalArgumentException("The field parameter has to be specified");
		}

		try
		{
			type = Type.valueOf(field.toUpperCase());
			return;
		}
		catch (IllegalArgumentException e){}

		if (type == Type.CUSTOM)
		{
			throw new IllegalArgumentException("The \"custom\" type should be stated " +
				"implicitly by passing path accessor strings as parameter.");
		}

		if (type == null)
		{
			type = Type.CUSTOM;
			String[] param = field.split(";");
			accessors = new PathAccessor[param.length];
			for (int i = 0; i < accessors.length; i++)
			{
				if (!param[i].contains("/"))
				{
					throw new IllegalArgumentException("The parameter column field is not " +
						"recognized as a pre-defined type. It also does not qualify as a path" +
						" accessor argument string.");
				}

				accessors[i] = new PathAccessor(param[i]);
			}
		}
	}

	/**
	 * Get the string to write in the output file.
	 * @param inter the binary interaction
	 * @return column value
	 */
	public String getColumnValue(SIFInteraction inter)
	{
		switch (type)
		{
			case MEDIATOR: return concat(inter.getMediatorIDs());
			case PATHWAY: return concat(inter.getPathwayNames());
			case PUBMED: return concat(inter.getPubmedIDs());
			case RESOURCE: return concat(inter.getDataSources());
			case SOURCE_LOC: return concat(inter.getCellularLocationsOfSource());
			case TARGET_LOC: return concat(inter.getCellularLocationsOfTarget());
			case CUSTOM:
			{
				Set<String> set = new HashSet<String>();
				for (PathAccessor acc : accessors)
				{
					for (Object o : acc.getValueFromBeans(inter.mediators))
					{
						set.add(o.toString());
					}
				}
				List<String> list = new ArrayList<String>(set);
				Collections.sort(list);
				return concat(list);
			}
			default: throw new RuntimeException("Unhandled type: " + type +
				". This shouldn't be happening.");
		}
	}

	/**
	 * Concatenates the given collection of strings into a single string where values are separated
	 * with a semicolon.
	 * @param col string collection
	 * @return concatenated string
	 */
	private String concat(Collection<String> col)
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

	/**
	 * Type of the output column.
	 */
	public enum Type
	{
		MEDIATOR,
		PUBMED,
		PATHWAY,
		RESOURCE,
		SOURCE_LOC,
		TARGET_LOC,
		CUSTOM
	}
}
