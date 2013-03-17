package org.biopax.paxtools.pattern;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Named;

/**
 * A pattern match is an array of biopax elements that satisfies the list of mapped constraints in a
 * pattern.
 *
 * @author Ozgun Babur
 */
public class Match implements Cloneable
{
	/**
	 * Array of variables.
	 */
	private BioPAXElement[] variables;

	/**
	 * Constructor with size.
	 * @param size array size
	 */
	public Match(int size)
	{
		this.variables = new BioPAXElement[size];
	}

	/**
	 * Getter for the element array.
	 * @return element array
	 */
	public BioPAXElement[] getVariables()
	{
		return variables;
	}

	/**
	 * Gets element at the index.
	 * @param index index of the element to get
	 * @return element at the index
	 */
	public BioPAXElement get(int index)
	{
		return variables[index];
	}

	/**
	 * Gets element corresponding to the given label in the pattern.
	 * @param label label of the element in the pattern
	 * @param p related pattern
	 * @return element of the given label
	 * @throws IllegalArgumentException if the label not in the pattern
	 */
	public BioPAXElement get(String label, Pattern p)
	{
		return variables[p.indexOf(label)];
	}

	/**
	 * Gets first element of the match
	 * @return first element
	 */
	public BioPAXElement getFirst()
	{
		return variables[0];
	}

	/**
	 * Gets last element of the match.
	 * @return last element
	 */
	public BioPAXElement getLast()
	{
		return variables[variables.length - 1];
	}

	/**
	 * Gets the array size.
	 * @return array size
	 */
	public int varSize()
	{
		return variables.length;
	}

	/**
	 * Sets the given element to the given index.
	 * @param ele element to set
	 * @param index index to set
	 */
	public void set(BioPAXElement ele, int index)
	{
		variables[index] = ele;
	}

	/**
	 * Checks if all given indices are assigned.
	 * @param ind indices to check
	 * @return true if none of them are null
	 */
	public boolean varsPresent(int ... ind)
	{
		for (int i : ind)
		{
			if (variables[i] == null) return false;
		}
		return true;
	}

	/**
	 * Clones a match.
	 * @return clone of the match
	 */
	@Override
	public Object clone()
	{
		Match m = null;
		try
		{
			m = (Match) super.clone();
			m.variables = new BioPAXElement[variables.length];
			System.arraycopy(variables, 0, m.variables, 0, variables.length);
			return m;
		}
		catch (CloneNotSupportedException e)
		{
			throw new RuntimeException("super.clone() not supported.");
		}
	}

	/**
	 * Gets name of variables.
	 * @return names of variables
	 */
	@Override
	public String toString()
	{
		String s = "";

		int  i = 0;
		for (BioPAXElement ele : variables)
		{
			if (ele != null) s += i + " - " + getAName(ele) + "\n";
			i++;
		}
		return s;
	}

	/**
	 * Finds a name for the variable.
	 * @param ele element to check
	 * @return a name
	 */
	public String getAName(BioPAXElement ele)
	{
		String name = null;
		
		if (ele instanceof Named)
		{
			Named n = (Named) ele;
			if (n.getDisplayName() != null && n.getDisplayName().length() > 0) 
				name = n.getDisplayName();
			else if (n.getStandardName() != null && n.getStandardName().length() > 0) 
				name = n.getStandardName();
			else if (!n.getName().isEmpty() && n.getName().iterator().next().length() > 0)
				name = n.getName().iterator().next();
		}
		if (name == null ) name = ele.getRDFId();
		
		return name + " (" + ele.getModelInterface().getName().substring(
			ele.getModelInterface().getName().lastIndexOf(".") + 1) + ")";
	}
}
