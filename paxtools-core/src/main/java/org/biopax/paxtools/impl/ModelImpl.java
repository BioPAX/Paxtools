package org.biopax.paxtools.impl;

import org.biopax.paxtools.controller.ModelUtils;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.controller.SimpleMerger;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.util.BPCollections;
import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.*;

/**
 * This is the default implementation of the {@link Model}. Use a factory to create a model.
 */
public class ModelImpl implements Model
{
// ------------------------------ FIELDS ------------------------------

	private static final long serialVersionUID = -2087521863213381434L;
	protected final Map<String, BioPAXElement> idMap;
    private final Map<String, String> nameSpacePrefixMap;
	private BioPAXLevel level;
	private transient BioPAXFactory factory;
    private final transient Set<BioPAXElement> exposedObjectSet;
    private boolean addDependencies = false;
    private String xmlBase;
    private String uri;
    private String name;

// --------------------------- CONSTRUCTORS ---------------------------

    protected ModelImpl() {
		idMap = BPCollections.I.createMap();
        nameSpacePrefixMap = new HashMap<String, String>();
        this.exposedObjectSet = new UnmodifiableImplicitSet(idMap.values());
	}

    protected ModelImpl(BioPAXLevel level)
	{
	   this(level.getDefaultFactory());
	}

	public ModelImpl(BioPAXFactory factory)
	{
		this();
		this.factory = factory;
		this.level = factory.getLevel();
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public synchronized boolean containsID(String id) {
        return this.idMap.containsKey(id);
    }

    
    public synchronized BioPAXElement getByID(String id) {
    	BioPAXElement ret = this.idMap.get(id);
    	if(ret != null) {
    		assert ret.getUri().equals(id);
    	}
        return ret;
    }


    public Map<String, String> getNameSpacePrefixMap()
	{
		return nameSpacePrefixMap;
	}

    
    private synchronized void setNameSpacePrefixMap(Map<String, String> nameSpacePrefixMap) {
		this.nameSpacePrefixMap.clear();
		this.nameSpacePrefixMap.putAll(nameSpacePrefixMap);
	}
    

    public void setFactory(BioPAXFactory factory)
	{
		this.factory = factory;
		this.level = factory.getLevel();
	}

// ------------------------ INTERFACE METHODS ------------------------

// --------------------- Interface Model ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

	public synchronized Set<BioPAXElement> getObjects()
	{
		return exposedObjectSet;
	}

	public synchronized <T extends BioPAXElement> Set<T> getObjects(Class<T> filterBy)
	{
		return new ClassFilterSet<BioPAXElement,T>(exposedObjectSet, filterBy);
	}

	synchronized void synchronizedsetObjects(Set<BioPAXElement> objects) {   	
   		idMap.clear();
       	for(BioPAXElement bpe : objects) {
       		add(bpe);
       	}
    }

    
	public synchronized void remove(BioPAXElement aBioPAXElement)
	{		
		if(this.contains(aBioPAXElement))
			this.idMap.remove(aBioPAXElement.getUri());
	}
                            
	public synchronized <T extends BioPAXElement> T addNew(Class<T> c, String id)
	{
		T paxElement = factory.create(c, id);
		this.add(paxElement);
		return paxElement;
	}

	/**
	 * This method returns true if given element 
	 * is the same object ("==") as the object stored in the model
	 * usually (for self-consistent models) but not necessarily under the element's ID.
	 * 
	 * @param aBioPAXElement BioPAX object (individual)
	 * @return true/false - whether this model contains the object or not
	 */
	public synchronized boolean contains(BioPAXElement aBioPAXElement)
	{
		return this.idMap.get(aBioPAXElement.getUri()) == aBioPAXElement;
	}

// -------------------------- OTHER METHODS --------------------------

	public String getUri() {
		return uri;
	}

	public void setUri(String modelUri) {
		this.uri = modelUri;
	}

	public String getName() {
		return name;
	}

	public void setName(String modelName) {
		this.name = modelName;
	}

	public synchronized void add(BioPAXElement aBioPAXElement)
	{
		String rdfId = aBioPAXElement.getUri();
        if(!this.level.hasElement(aBioPAXElement))
        {
            throw new IllegalBioPAXArgumentException(
                "Given object is of wrong level");
        }
        
        if (rdfId == null)
		{
			throw new IllegalBioPAXArgumentException(
				"null ID: every object must have an RDF ID");
		}
		else if (this.idMap.containsKey(rdfId))
		{
			throw new IllegalBioPAXArgumentException(
				"I already have an object with the same ID: " + rdfId +
					". Try removing it first");
		}
		else if (this.contains(aBioPAXElement))
		{
			throw new IllegalBioPAXArgumentException(
				"duplicate element:" + aBioPAXElement);
		}
		else
		{
			this.idMap.put(rdfId, aBioPAXElement);
		}
	}


    public BioPAXLevel getLevel()
	{
		return level;
	}

	// used by hibernate
    synchronized void setLevel(BioPAXLevel level) {
		this.level = level;
		this.factory = level.getDefaultFactory();
	}
	

    public void setAddDependencies(boolean value) {
        this.addDependencies = value;
    }

    
    public boolean isAddDependencies() {
        return addDependencies;
    }

    private class UnmodifiableImplicitSet implements Set<BioPAXElement>
	{
		private final Collection<BioPAXElement> elements;

		public UnmodifiableImplicitSet(
			Collection<BioPAXElement> elements)
		{

			this.elements = elements;
		}

		public int size()
		{
			return elements.size();
		}

		public boolean isEmpty()
		{
			return elements.isEmpty();
		}

		public boolean contains(Object o)
		{
			return elements.contains(o);
		}

		public Iterator<BioPAXElement> iterator()
		{
			return elements.iterator();
		}

		public Object[] toArray()
		{
			return elements.toArray();
		}

		public <T> T[] toArray(T[] a)
		{
            return elements.toArray(a);
		}

		public boolean add(BioPAXElement bioPAXElement)
		{
			throw new UnsupportedOperationException();
		}

		public boolean remove(Object o)
		{
			throw new UnsupportedOperationException();
		}

		public boolean containsAll(Collection<?> c)
		{
			return elements.containsAll(c);
		}

		public boolean addAll(Collection<? extends BioPAXElement> c)
		{
			throw new UnsupportedOperationException();
		}

		public boolean retainAll(Collection<?> c)
		{
			throw new UnsupportedOperationException();
		}

		public boolean removeAll(Collection<?> c)
		{
			throw new UnsupportedOperationException();
		}

		public void clear()
		{
			throw new UnsupportedOperationException();
		}
	}

    /**
     * It does not automatically replace or clean up the old 
     * element's object properties, therefore, some child 
     * elements may become "dangling" if they were used by
     * the replaced element only.
     * 
     * Can also clear object properties (- replace with null).
     */
	public synchronized void replace(final BioPAXElement existing, final BioPAXElement replacement) 
	{
		 ModelUtils.replace(this, Collections.singletonMap(existing, replacement));
		 remove(existing);
		 if(replacement != null)
			 add(replacement);
	}
	
	
	/**
	 * This is default implementation that uses the 
	 * id-based merging ({@link SimpleMerger#merge(Model, Model...)})
	 * 
	 * NOTE: some applications, such as those dealing with persistence/transactions 
	 * or advanced BioPAX alignment/comparison algorithms (like the Patch), 
	 * may have to implement and use a more specific method instead.
	 * 
	 * @see SimpleMerger
	 * @see Model#merge(Model)
	 */
	public synchronized void merge(Model source) {
		SimpleMerger merger = new SimpleMerger(
			SimpleEditorMap.get(level));
		if(source == null)
			merger.merge(this, this); //repairs itself
		else
			merger.merge(this, source);
	}

	
	/**
	 * 
	 * This implementation "repairs" the model 
	 * without unnecessarily copying objects:
     * - recursively adds lost "children" (not null object property values
     *   for which {@link Model#contains(BioPAXElement)} returns False)
     * - updates object properties (should refer to model's elements)
	 * 
	 */
	@Override
	public synchronized void repair() {
		// updates props and children
		merge(null);
	}

	@Override
	public void setXmlBase(String base) {
		this.xmlBase = base;
	}

	@Override
	public String getXmlBase() {
		return this.xmlBase;
	}
}
