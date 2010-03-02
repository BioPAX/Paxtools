package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.PublicationXref;

import java.util.HashSet;
import java.util.Set;

class PublicationXrefImpl extends XrefImpl implements PublicationXref
{
	private String title;
	private Set<String> url;
	private Set<String> source;
	private Set<String> author;
	private int year = BioPAXElement.UNKNOWN_INT;

	/**
	 * Constructor.
	 */
	public PublicationXrefImpl()
	{
		this.url = new HashSet<String>();
		this.source = new HashSet<String>();
		this.author = new HashSet<String>();
	}

	//
	// BioPAXElement interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	public Class<? extends PublicationXref> getModelInterface()
	{
		return PublicationXref.class;
	}

	//
	// PublicationXref interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

    // Property author

	public Set<String> getAuthor()
	{
		return author;
	}

	public void setAuthor(Set<String> author)
	{
		this.author = author;
	}

	public void addAuthor(String author)
	{
		this.author.add(author);
	}

	public void removeAuthor(String author)
	{
		this.author.remove(author);
	}

    // Property source

	public Set<String> getSource()
	{
		return source;
	}

	public void setSource(Set<String> source)
	{
		this.source = source;
	}

	public void addSource(String source)
	{
		this.source.add(source);
	}

	public void removeSource(String source)
	{
		this.source.remove(source);
	}

    // Property title

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

    // Property url

	public Set<String> getUrl()
	{
		return url;
	}

	public void setUrl(Set<String> url)
	{
		this.url = url;
	}

	public void addUrl(String url)
	{
		this.url.add(url);
	}

	public void removeUrl(String url)
	{
		this.url.remove(url);
	}

    // Property year

	public int getYear()
	{
		return year;
	}

	public void setYear(int year)
	{
		this.year = year;
	}
	
	@Override
	protected boolean semanticallyEquivalent(BioPAXElement other) {
		if(!(other instanceof PublicationXref)) return false;
		
		PublicationXref that = (PublicationXref) other;
		boolean eqv = (year == that.getYear()) &&
			(title != null ? 
				title.equals(that.getTitle()) 
				: that.getTitle() == null)
			&& author.containsAll(that.getAuthor())
			&& source.containsAll(that.getSource())
			&& url.containsAll(that.getUrl());
		
		return eqv	&& super.semanticallyEquivalent(other);
	}
}
