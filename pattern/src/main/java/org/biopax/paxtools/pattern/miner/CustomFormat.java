package org.biopax.paxtools.pattern.miner;

/**
 * Used for customizing the columns in the SIF text output.
 * @author Ozgun Babur
 */
public class CustomFormat implements SIFToText
{
	/**
	 * Value generators for custom columns.
	 */
	private OutputColumn[] columns;

	public CustomFormat(String... cols)
	{
		columns = new OutputColumn[cols.length];

		for (int i = 0; i < cols.length; i++)
		{
			columns[i] = new OutputColumn(cols[i]);
		}
	}

	/**
	 * Prepares the line in the output file for the given interaction.
	 * @param inter the interaction
	 * @return output line.
	 */
	@Override
	public String convert(SIFInteraction inter)
	{
		String s = inter.toString();

		for (OutputColumn column : columns)
		{
			s += "\t" + column.getColumnValue(inter);
		}

		return s;
	}
}
