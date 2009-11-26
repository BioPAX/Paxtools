package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.*;

import java.util.HashSet;
import java.util.Set;

/**
 */
class openControlledVocabularyImpl extends BioPAXLevel2ElementImpl
	implements openControlledVocabulary
{
// ------------------------------ FIELDS ------------------------------

	private Set<String> TERM;

	private final ReferenceHelper referenceHelper;

// --------------------------- CONSTRUCTORS ---------------------------

	public openControlledVocabularyImpl()
	{
		this.TERM = new HashSet<String>();
		this.referenceHelper = new ReferenceHelper(this);
	}

// ------------------------ INTERFACE METHODS ------------------------

// --------------------- Interface BioPAXElement ---------------------


	/**
	 * TODO: think about this.
	 *
	 * @return false
	 */
    protected boolean semanticallyEquivalent(BioPAXElement o)
	{
		if (o!= null && o instanceof openControlledVocabulary)
		{
			openControlledVocabulary that =
				(openControlledVocabulary) o;

			return
				this.equals(that) ||
				hasCommonTerm(that) ||
				!this.findCommonUnifications(that).isEmpty();
		}
		return false;
	}

	private boolean hasCommonTerm(openControlledVocabulary that)
	{
		for (String s : TERM)
		{
			if (that.getTERM().contains(s))
			{
				return true;
			}
		}
		return false;
	}

	public int equivalenceCode()
	{
		return hashCode();
	}

	public Class<? extends BioPAXElement> getModelInterface()
	{
		return openControlledVocabulary.class;
	}

	public String toString()
	{
		String s = "";

		for (String term : TERM)
		{
			if (s.length() > 0) s += "; ";
			s += term;
		}
		return s;
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

// --------------------- Interface openControlledVocabulary ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

	public Set<String> getTERM()
	{
		return TERM;
	}

	public void setTERM(Set<String> TERM)
	{
		this.TERM = TERM;
	}

	public void addTERM(String TERM)
	{
		this.TERM.add(TERM);
	}

	public void removeTERM(String TERM)
	{
		this.TERM.remove(TERM);
	}
}
