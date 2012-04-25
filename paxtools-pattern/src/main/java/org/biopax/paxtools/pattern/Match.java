package org.biopax.paxtools.pattern;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Named;

import java.util.Collections;
import java.util.List;

/**
 * @author Ozgun Babur
 */
public class Match implements Cloneable
{
	private BioPAXElement[] variables;

	public Match(int size)
	{
		this.variables = new BioPAXElement[size];
	}

	public BioPAXElement get(int index)
	{
		return variables[index];
	}

	public BioPAXElement getLast()
	{
		return variables[variables.length - 1];
	}

	public void set(BioPAXElement ele, int index)
	{
		variables[index] = ele;
	}

	public boolean varsPresent(int ... ind)
	{
		for (int i : ind)
		{
			if (variables[i] == null) return false;
		}
		return true;
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		Match m = (Match) super.clone();
		m.variables = new BioPAXElement[variables.length];
		System.arraycopy(variables, 0, m.variables, 0, variables.length);
		return m;
	}

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
