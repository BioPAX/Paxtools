package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.ControlledVocabulary;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.paxtools.util.SetEquivalanceChecker;
import org.biopax.paxtools.util.SetStringBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class ControlledVocabularyImpl extends XReferrableImpl implements
	ControlledVocabulary
{

	private Set<String> term;
	private static final Pattern PATTERN =
			Pattern.compile("\\]|\\[");

	/**
	 * Constructor.
	 */
	public ControlledVocabularyImpl()
	{
		this.term = new HashSet<String>();
	}

	//
	// BioPAXElement, Xreferrable implementation
	//
	////////////////////////////////////////////////////////////////////////////

	@Transient
	public Class<? extends ControlledVocabulary> getModelInterface()
	{
		return ControlledVocabulary.class;
	}


	
	//
	// ControlledVocabulary interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	// Property term

	@ElementCollection
	@Field(name = BioPAXElementImpl.SEARCH_FIELD_TERM, index = Index.TOKENIZED)
	@FieldBridge(impl=SetStringBridge.class)
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
		if(term != null && term.length()>0)
			this.term.add(term);
	}

	public void removeTerm(String term)
	{
		if(term != null)
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
				&& (term.isEmpty() && that.getTerm().isEmpty() || !terms.isEmpty() )
				&& SetEquivalanceChecker.isEquivalentIntersection(
						new ClassFilterSet<Xref,UnificationXref>(getXref(), UnificationXref.class),
						new ClassFilterSet<Xref, UnificationXref>(that.getXref(), UnificationXref.class)
					);		
	}
	
	@Override
	public String toString()
	{
		return PATTERN.matcher(term.toString()).replaceAll("");
	}
}
