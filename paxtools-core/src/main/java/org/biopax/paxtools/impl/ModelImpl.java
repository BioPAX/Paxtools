package org.biopax.paxtools.impl;

import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.controller.SimpleMerger;
import org.biopax.paxtools.controller.Traverser;
import org.biopax.paxtools.controller.Visitor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.*;

/**
 * This is the default implementation of the {@link Model}.
 */
public class ModelImpl implements Model
{
// ------------------------------ FIELDS ------------------------------

	private static final long serialVersionUID = -2087521863213381434L;
	protected final Map<String, BioPAXElement> idMap;
    private final Map<String, String> nameSpacePrefixMap;
	private BioPAXLevel level;
	private transient BioPAXFactory factory;
    private transient final Set<BioPAXElement> exposedObjectSet;
    private boolean addDependencies = false;

// --------------------------- CONSTRUCTORS ---------------------------

    protected ModelImpl() {
		idMap = new HashMap<String, BioPAXElement>();
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

    public boolean containsID(String id) {
        return this.idMap.containsKey(id);
    }

    
    public BioPAXElement getByID(String id) {
    	BioPAXElement ret = this.idMap.get(id);
    	if(ret != null) {
    		assert ret.getRDFId().equals(id);
    	}
        return ret;
    }


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
    

    public void setFactory(BioPAXFactory factory)
	{
		this.factory = factory;
		this.level = factory.getLevel();
	}

// ------------------------ INTERFACE METHODS ------------------------

// --------------------- Interface Model ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

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
		T paxElement = factory.create(c, id);
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


    public BioPAXLevel getLevel()
	{
		return level;
	}

	// used by hibernate
	void setLevel(BioPAXLevel level) {
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
		// a visitor to replace the element in the model
		Visitor visitor = new Visitor() {
			@Override
			public void visit(BioPAXElement domain, Object range, Model model, PropertyEditor editor) {
				if(range instanceof BioPAXElement && range.equals(existing))
				{
					if(editor.isMultipleCardinality()) {
						if(replacement != null)
							editor.setValueToBean(replacement, domain);
						editor.removeValueFromBean(existing, domain);
					} else {
						editor.setValueToBean(replacement, domain);
					}
				}
			}
		};
		
		EditorMap em = new SimpleEditorMap(level);
		Traverser traverser = new Traverser(em, visitor);
		for(BioPAXElement bpe : getObjects()) {
			traverser.traverse(bpe, null);
		}
		
		/* next, remove the old element and 
		 * its direct children from the model
		 * (can be added later by 'merge' model to itself
		 * if they are still used by other elements)
		 */
		
		// another 'visitor' to remove child elements from the model
		Visitor visitor2 = new Visitor() {
			@Override
			public void visit(BioPAXElement domain, Object range, Model model, PropertyEditor editor) {
				if(range instanceof BioPAXElement 
						&& model.contains((BioPAXElement) range))
				{
					model.remove((BioPAXElement) range);
				}
			}
		};
		Traverser traverser2 = new Traverser(em, visitor2);
		traverser2.traverse(existing, this);
		remove(existing);
		
		// add new one 
		if(replacement != null) {
			if(!containsID(replacement.getRDFId()))
				add(replacement); // - does not add children for now...
		}
		
		// auto-updates object properties and finds/adds child elements
		merge(this);
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
