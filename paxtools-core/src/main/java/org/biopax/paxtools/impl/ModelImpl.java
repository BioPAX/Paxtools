package org.biopax.paxtools.impl;

import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.controller.SimpleMerger;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.*;

import javax.persistence.*;

/**
 * This is the default implementation of the {@link Model}.
 */
@Entity
public class ModelImpl implements Model
{
// ------------------------------ FIELDS ------------------------------

	private static final long serialVersionUID = -2087521863213381434L;
	protected final Map<String, BioPAXElement> idMap;
    private final Map<String, String> nameSpacePrefixMap;
	private BioPAXLevel level;
	private transient BioPAXFactory factory;
    private transient final Map<String, BioPAXElement> exposedIdMap;
    private transient final Set<BioPAXElement> exposedObjectSet;
    private boolean addDependencies = false;

// --------------------------- CONSTRUCTORS ---------------------------

    protected ModelImpl() {
		idMap = new HashMap<String, BioPAXElement>();
        nameSpacePrefixMap = new HashMap<String, String>();
        this.exposedIdMap = Collections.unmodifiableMap(idMap);
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

	
	private Long id = 0L;
	
	@Id
    @GeneratedValue
	public Long getId() {
		return id;
	}

	public void setId(Long value) {
		id = value;
	}
	
	
// --------------------- GETTER / SETTER METHODS ---------------------

    /**
     *@deprecated use getByID(id) or containsID(id) instead.
     */
	@Transient
    public Map<String, BioPAXElement> getIdMap()
	{
        return exposedIdMap;
	}

    public boolean containsID(String id) {
        return this.idMap.containsKey(id);
    }
    
    @Transient
    public BioPAXElement getByID(String id) {
    	BioPAXElement ret = this.idMap.get(id);
    	if(ret != null) {
    		assert ret.getRDFId().equals(id);
    	}
        return ret;
    }


    @ElementCollection
    //@Field(name=BioPAXElementImpl.SEARCH_FIELD_NAMESPACE)
    // TODO custom FieldBridge
    public Map<String, String> getNameSpacePrefixMap()
	{
		return nameSpacePrefixMap;
	}

    private void setNameSpacePrefixMap(Map<String, String> nameSpacePrefixMap) {
    	synchronized (this.nameSpacePrefixMap) {
			this.nameSpacePrefixMap.clear();
			this.nameSpacePrefixMap.putAll(nameSpacePrefixMap);
		}
	}
    
    
    @Transient
    public void setFactory(BioPAXFactory factory)
	{
		this.factory = factory;
		this.level = factory.getLevel();
	}

// ------------------------ INTERFACE METHODS ------------------------

// --------------------- Interface Model ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

    @ManyToMany(targetEntity=BioPAXElementImpl.class, cascade={CascadeType.ALL})
	public Set<BioPAXElement> getObjects()
	{
		return exposedObjectSet;
	}

	public <T extends BioPAXElement> Set<T> getObjects(Class<T> filterBy)
	{
		return new ClassFilterSet<T>(exposedObjectSet, filterBy);
	}

    void setObjects(Set<BioPAXElement> objects) {   	
    	synchronized (idMap) {
    		idMap.clear();
        	for(BioPAXElement bpe : objects) {
        		add(bpe);
        	}
		}
    }
	
	public void remove(BioPAXElement aBioPAXElement)
	{
		BioPAXElement deleted = this.idMap.remove(aBioPAXElement.getRDFId());
		
		/* note: idMap.values().remove(aBioPAXElement) would delete it for sure
		 * but... what if its RDFId does not match the corresponding key in the idMap?
		 * So, we go another way (see below) ;-)
		 */
		
		// inconsistent/intermediate model may have
		if( deleted == null) {
			// model stores aBioPAXElement under different ID, doesn't it?
			assert !this.idMap.values().contains(aBioPAXElement);
		} else {
			// it actually deleted aBioPAXElement, not another Object with the same ID, didn't it?
			assert deleted == aBioPAXElement;
		}
	}
                            
	public <T extends BioPAXElement> T addNew(Class<T> c, String id)
	{
		T paxElement = factory.reflectivelyCreate(c, id);
		this.add(paxElement);
		return paxElement;
	}

	/**
	 * This method returns true if and only if the given object is
	 * @param aBioPAXElement
	 * @return
	 */
	public boolean contains(BioPAXElement aBioPAXElement)
	{
		String rdfid = aBioPAXElement.getRDFId();
		return this.idMap.get(rdfid) == aBioPAXElement;
	}

// -------------------------- OTHER METHODS --------------------------

	public void add(BioPAXElement aBioPAXElement)
	{
		String rdfId = aBioPAXElement.getRDFId();
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


	@Enumerated(EnumType.STRING)
    public BioPAXLevel getLevel()
	{
		return level;
	}

	// used by hibernate
	void setLevel(BioPAXLevel level) {
		this.level = level;
		this.factory = level.getDefaultFactory();
	}
	
	
    @Transient
    public void setAddDependencies(boolean value) {
        this.addDependencies = value;
    }

    @Transient
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
     * Note: for safety, updating ID has to be done by replacing the element rather than modifying it...
     * 
     * @deprecated (do re-factoring) to use the new method {@link Model#replace(BioPAXElement, BioPAXElement)} instead
     */
	public synchronized void updateID(String oldID, String newID) {
		throw new UnsupportedOperationException("This method is broken; use Model.replace instead!");
		/*
		if (this.containsID(oldID)) {
			BioPAXElement bpe = getByID(oldID);
			this.idMap.remove(oldID);
			try {
				setIdAndAdd(bpe, newID);
			} catch (IllegalBioPAXArgumentException e) {
				// roolback and fail
				setIdAndAdd(bpe, oldID); 
				throw new IllegalBioPAXArgumentException(
					"Updating ID: " + oldID + " failed (model is unchanged): "
						+ "cannot use new ID: " + newID, e);
			}	
		} else {
			throw new IllegalBioPAXArgumentException(
				"I do not have an object with the ID: " + oldID);
		}
		*/
	}

	/*
	 * TODO implement the 'replace' method
	 */
	public synchronized void replace(BioPAXElement existing, BioPAXElement replacement) {
		throw new UnsupportedOperationException("not implemented yet.");
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
	public void merge(Model source) {
		new SimpleMerger(new SimpleEditorMap(level))
			.merge(this, source);
	}
}
