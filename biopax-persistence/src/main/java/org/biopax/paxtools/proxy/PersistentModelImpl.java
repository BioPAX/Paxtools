package org.biopax.paxtools.proxy;

import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.proxy.level2.Level2PersistentFactoryImpl;
import org.biopax.paxtools.proxy.level3.Level3PersistentFactoryImpl;
import org.hibernate.Session;
import org.hibernate.Query;

import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Collection;

/**
 * TODO: check, implement, describe (or remove this class...)
 * User: demir
 * Date: Sep 23, 2008
 * Time: 7:28:35 PM
 * 
 * @deprecated use ModelForPersistence instead [rodche, Mar 25, 2010]
 */
public class PersistentModelImpl implements Model
{
    private BioPAXLevel level;
    private Session session;
    private BioPAXFactory factory;
    private boolean addDependencies;

    PersistentModelImpl(BioPAXLevel level, Session session)
    {
        this.level = level;
        this.session = session;

        if(level.getValue()==3)
        {
            this.setFactory(new Level3PersistentFactoryImpl());
        }
        else
        {
            this.setFactory(new Level2PersistentFactoryImpl());
        }

    }

    public Session getSession()
    {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void remove(BioPAXElement aBioPAXElement)
    {
        session.delete(aBioPAXElement);
    }

    public void setFactory(BioPAXFactory factory)
    {
        this.factory = factory;
    }

    public BioPAXLevel getLevel()
    {
        return this.level;
    }

    public void setAddDependencies(boolean value) {
        this.addDependencies = value;
    }

    public boolean isAddDependencies()
    {
        return this.addDependencies;
    }

    public <T extends BioPAXElement> T addNew(Class<T> c, String id)
    {

        T bpe = this.factory.reflectivelyCreate(c);
        bpe.setRDFId(id);
        add(bpe);
        return bpe;
    }

    public boolean contains(BioPAXElement aBioPAXElement)
    {
        return session.contains(aBioPAXElement);
    }

    public Map<String, BioPAXElement> getIdMap() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public BioPAXElement getByID(String id)
    {
        return (BioPAXElement) session.get(BioPAXElement.class, id);
    }

    public Map<String, String> getNameSpacePrefixMap()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Set<BioPAXElement> getObjects()
    {
        String levelElement = this.level.getValue()==3?"l3levelelement":"l2levelelement";
        Query query = session.createQuery("FROM" +levelElement);
        return new hibernateSet<BioPAXElement>(query);
    }

    public <T extends BioPAXElement> Set<T> getObjects(Class<T> filterBy)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void add(BioPAXElement aBioPAXElement)
    {
        session.save(aBioPAXElement);
    }

    private class hibernateSet<T extends BioPAXElement> implements Set<T>
    {
        private Query q;

        private hibernateSet(Query q)
        {
            this.q = q;
        }

        public int size() {
            return q.list().size();
        }

        public boolean isEmpty() {
            return q.list().isEmpty();
        }

        public boolean contains(Object o) {
            return q.list().contains(o);
        }

        public Iterator<T> iterator() {
            return q.iterate();
        }

        public Object[] toArray() {
            return q.list().toArray();
        }

        public <T> T[] toArray(T[] a) {
            throw new UnsupportedOperationException();
        }

        public boolean add(T t) {
            throw new UnsupportedOperationException();
        }

        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean containsAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection<? extends T> c) {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }
    }

    // id, supposed to RDFId, can be different from the PK
	public boolean containsID(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	
	public void updateID(String oldID, String newID) {
		throw new UnsupportedOperationException();
	}
	

}
