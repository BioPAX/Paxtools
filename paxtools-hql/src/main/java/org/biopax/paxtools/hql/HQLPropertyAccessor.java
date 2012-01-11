package org.biopax.paxtools.hql;

import org.biopax.paxtools.controller.PropertyAccessorAdapter;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;

import java.util.*;

/**
 *
 */
public class HQLPropertyAccessor<D extends BioPAXElement, R> extends PropertyAccessorAdapter<D, R> {

    private static final String DOMAIN = "domain";

    private String qString;

    private Query query;

    private static HashSet<String> propertyX = new HashSet<String>(
            Arrays.asList("standardName", "displayName", "template", "entityReference"));

    private static HashSet<String> mixedCase = new HashSet<String>(
            Arrays.asList("kEQ", "eCNumber"));

    private PropertyEditor<D, R> editor;


    public HQLPropertyAccessor(PropertyEditor<D, R> editor) {
        super(editor.getDomain(), editor.getRange(), editor.isMultipleCardinality());
        this.editor = editor;

        String domainClass = domain.getName();
        String domainName = "d" + domain.getSimpleName();

        String property = editor.getProperty();
        property = processPropertyExceptions(domain, property);


        qString = " FROM " + domainClass + " as " + domainName;
        qString += " WHERE " + domainName + " in(:" + DOMAIN + ") ";   //TODO
        qString += editor.isMultipleCardinality() ? domainName +" left join fetch " + domainName + "." + property : " ";
    }

    private String processPropertyExceptions(Class<D> domain, String property) {
        if (domain.equals(Control.class) && property.equals("controller")) {
            property = "pathwayController  left join fetch dControl.peController";
        } else if (propertyX.contains(property)) {
            property = property + "X";
        } else if (mixedCase.contains(property)) {
            property = property.substring(0, 1).toUpperCase() + property.substring(1);
        }
        return property;
    }

    public void init(Session session) {
        query = session.createQuery(qString);
        query.setReadOnly(true);
    }

    @Override
    public Set<? extends R> getValueFromBean(D bean)
            throws IllegalBioPAXArgumentException {
        Set<R> values = editor.getValueFromBean(bean);
        Hibernate.initialize(values);
        return values;

    }

    @Override
    public Set<? extends R> getValueFromBeans(Collection<? extends D> beans)
            throws IllegalBioPAXArgumentException {

        return editor.getValueFromBeans(fetch(beans));

    }

    public List fetch(Collection<? extends D> beans) {
        if (!beans.isEmpty()) {
            query.setParameterList(DOMAIN, beans);
            return query.list();
        } else return Collections.emptyList();
    }

    @Override
    public boolean isUnknown(Object value) {
        return editor.isUnknown(value);
    }


}
