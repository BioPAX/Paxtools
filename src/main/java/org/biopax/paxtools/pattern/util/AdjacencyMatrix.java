package org.biopax.paxtools.pattern.util;

import java.util.List;

/**
 * Adjacency matrix representation of a graph.
 * @author Ozgun Babur
 */
public class AdjacencyMatrix
{
	/**
	 * Unique node names.
	 */
	public String[] names;

	/**
	 * Edges matrix. First index is source, second index is target. The matrix should be symmetrical
	 * if the graph is undirected.
	 */
	public boolean[][] matrix;

	/**
	 * Constructor with contents.
	 * @param matrix edges
	 * @param namesList nodes
	 */
	public AdjacencyMatrix(boolean[][] matrix, List<String> namesList)
	{
		if (matrix.length != namesList.size())
		{
			throw new IllegalArgumentException("Matrix row size not equal to nodes size");
		}
		for (boolean[] m : matrix)
		{
			if (m.length != namesList.size())
			{
				throw new IllegalArgumentException("Matrix column size not equal to nodes size");
			}
		}

		this.matrix = matrix;
		this.names = namesList.toArray(new String[namesList.size()]);
	}

	@Override
	public String toString()
	{
		StringBuilder b = new StringBuilder();

		for (String name : names)
		{
			b.append("\t").append(name);
		}
		int i = 0;
		for (boolean[] m : matrix)
		{
			b.append("\n").append(names[i++]);

			for (boolean v : m)
			{
				b.append(v ? "\tX" : "\t");
			}
		}

		return b.toString();
	}
}
