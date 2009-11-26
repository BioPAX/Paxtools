package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.ControlledVocabulary;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.SetEquivalanceChecker;
import org.biopax.paxtools.util.ClassFilterSet;

import java.util.HashSet;
import java.util.Set;

class ControlledVocabularyImpl extends L3ElementImpl implements
	ControlledVocabulary
{

	private Set<String> term;
	private ReferenceHelper referenceHelper;

	/**
	 * Constructor.
	 */
	public ControlledVocabularyImpl()
	{
		this.term = new HashSet<String>();
		this.referenceHelper = new ReferenceHelper(this);
	}

	//
	// BioPAXElement, Xreferrable implementation
	//
	////////////////////////////////////////////////////////////////////////////

	public Class<? extends ControlledVocabulary> getModelInterface()
	{
		return ControlledVocabulary.class;
	}


	public Set<Xref> getXref()
	{
		return referenceHelper.getXref();
	}

	public void setXref(Set<Xref> Xref)
	{
		referenceHelper.setXref(Xref);
	}

	public void addXref(Xref Xref)
	{
		referenceHelper.addXref(Xref);
	}

	public void removeXref(Xref Xref)
	{
		referenceHelper.removeXref(Xref);
	}

	//
	// ControlledVocabulary interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	// Property term

	public Set<String> getTerm()
	{
		return term;
	}

	public void setTerm(Set<String> term)
	{
		this.term = term;
	}

	public void addTerm(String term)
	{
		this.term.add(term);
	}

	public void removeTerm(String term)
	{
		this.term.remove(term);
	}
	
	@Override
	protected boolean semanticallyEquivalent(BioPAXElement element) {
		if(! (element instanceof ControlledVocabulary)) return false;
		
		ControlledVocabulary that = (ControlledVocabulary) element;
		Set<String> terms = new HashSet<String>(term.size());
		terms.addAll(term);
		terms.retainAll(that.getTerm());
		
		return getModelInterface().equals(that.getModelInterface()) 
				&& ( (getTerm().isEmpty() && that.getTerm().isEmpty()) || !terms.isEmpty() )
				&& SetEquivalanceChecker.isEquivalentIntersection(
						new ClassFilterSet<UnificationXref>(getXref(), UnificationXref.class),
						new ClassFilterSet<UnificationXref>(that.getXref(), UnificationXref.class)
					);		
	}
	
	@Override
	public String toString() {
		return getTerm().toString().replaceAll("\\]|\\[", "");
	}
}
