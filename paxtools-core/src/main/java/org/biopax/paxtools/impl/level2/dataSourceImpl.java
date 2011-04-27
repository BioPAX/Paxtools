package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.*;

import java.util.HashSet;
import java.util.Set;

/**
 */
class dataSourceImpl extends BioPAXLevel2ElementImpl implements dataSource
{
// ------------------------------ FIELDS ------------------------------

	private Set<String> NAME;

	private final ReferenceHelper referenceHelper;

// --------------------------- CONSTRUCTORS ---------------------------

	public dataSourceImpl()
	{
		this.NAME = new HashSet<String>();
		this.referenceHelper = new ReferenceHelper(this);
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------


	public Class<? extends BioPAXElement> getModelInterface()
	{
		return dataSource.class;
	}

// --------------------- Interface XReferrable ---------------------


	public Set<xref> getXREF()
	{
		return referenceHelper.getXREF();
	}

	public void setXREF(Set<xref> XREF)
	{
		referenceHelper.setXREF(XREF);
	}

	public void addXREF(xref XREF)
	{
		referenceHelper.addXREF(XREF);
	}

	public void removeXREF(xref XREF)
	{
		referenceHelper.removeXREF(XREF);
	}

	public Set<unificationXref> findCommonUnifications(XReferrable that)
	{
		return referenceHelper.findCommonUnifications(that);
	}

	public Set<relationshipXref> findCommonRelationships(XReferrable that)
	{
		return referenceHelper.findCommonRelationships(that);
	}

	public Set<publicationXref> findCommonPublications(XReferrable that)
	{
		return referenceHelper.findCommonPublications(that);
	}

// --------------------- Interface dataSource ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

	public Set<String> getNAME()
	{
		return NAME;
	}

	public void setNAME(Set<String> NAME)
	{
		this.NAME = NAME;
	}

	public void addNAME(String NAME)
	{
		this.NAME.add(NAME);
	}

	public void removeNAME(String NAME)
	{
		this.NAME.remove(NAME);
	}

// --------------------- Other Methods ----------------------------

	public String toString()
	{
		String s = "";

		for (String name : NAME)
		{
			if (s.length() > 0) s += "; ";
			s += name;
		}
		Set<xref> xref = this.getXREF();
		if (!xref.isEmpty())
		{
			s += " (";
			for (xref anXref : xref)
			{

				s += anXref ;
			}
			s += ")";
		}
		return s;
	}
}
