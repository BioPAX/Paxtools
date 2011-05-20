package org.biopax.paxtools.io.jena;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.controller.EditorMapAdapter;
import org.biopax.paxtools.controller.ObjectPropertyEditor;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;


/**
 * This class is an editor map that uses Jena to read BioPAX OWL definition and creates property
 * editors for BioPAX interfaces.
 * <p/>
 * This editor map is extensively used for I/O, modification, and querying operations applied on the
 * model.
 */
public class JenaEditorMap extends EditorMapAdapter {
// ------------------------------ FIELDS ------------------------------

    private static final Log log = LogFactory.getLog(JenaEditorMap.class);

	private BioPAXLevel level;

	// --------------------------- CONSTRUCTORS ---------------------------

    public JenaEditorMap() {
        this(null);
    }

    public JenaEditorMap(BioPAXLevel level) {
        this.level = level;
        OntModel ontologyDefinition =
                org.biopax.paxtools.io.jena.JenaHelper.createModel();

        ontologyDefinition.read(this.level.getLevelFileAsStream(),
                this.level.getNameSpace());
        init();
        preprocessClasses(ontologyDefinition);
        preprocessProperties(ontologyDefinition);
        preprocessRestrictions(ontologyDefinition);
    }

    protected void init() {

    }

    private void preprocessClasses(OntModel model) {
        //Let's iterate over all classes
        ExtendedIterator extendedIterator = model.listClasses();
        while (extendedIterator.hasNext()) {

            checkOntologyClassAndRegister((OntClass) extendedIterator.next());
        }
    }

    protected void checkOntologyClassAndRegister(OntClass ontClass) {

        if (isInBioPAXNameSpace(ontClass.getNameSpace())) {
            registerModelClass(ontClass.getLocalName());
        }
    }


    private void preprocessProperties(OntModel model) {
        //Let's iterate over all properties
        ExtendedIterator extendedIterator = model.listDatatypeProperties();
        iteratePropertiesAndResolveDomain(extendedIterator);

        extendedIterator = model.listObjectProperties();
        iteratePropertiesAndResolveDomain(extendedIterator);
    }

    private void iteratePropertiesAndResolveDomain(
            ExtendedIterator extendedIterator) {
        while (extendedIterator.hasNext()) {
            OntProperty property =
                    (OntProperty) extendedIterator.next();
            // Let's try to map the domain of the property to a java bean
            OntResource domain = retrieveDomain(property);




            //Now there are two possibilities
            //Check this by evaluating the name space
            //TODO find a more ontology-conscient way
            recursivelyResolveUnionClasses(property, domain);
        }
    }

    private void recursivelyResolveUnionClasses(OntProperty property, OntResource domain) {
        String nameSpace = domain.getNameSpace();
        if (isInBioPAXNameSpace(nameSpace)) {
            //A: it is a class and we are clear to go
            createAndRegisterBeanEditor(property, domain);
        } else {
            //B: it is a union of classes ( things can actually be more
            //complicated but for now these are the two cases with BioPAX
            ExtendedIterator unionSetClassIterator =
                    ((UnionClass) domain.as(UnionClass.class))
                            .listOperands();
            while (unionSetClassIterator.hasNext()) {
                OntClass ontClass =
                        (OntClass) unionSetClassIterator.next();
                recursivelyResolveUnionClasses(property, ontClass);
            }
        }
    }

    protected void createAndRegisterBeanEditor(OntProperty property, OntResource domain) {
        createAndRegisterBeanEditor(property.getLocalName(),
                extractClass(domain.asClass()), null);
    }

    private OntResource retrieveDomain(OntProperty property) {
        OntResource domain = property.getDomain();

        if (domain == null) {

            //this is here because subproperties does not automatically
            //return their "inherited" domains. This is a very rare case
            OntProperty superProperty = property;
            while (domain == null) {
                superProperty = superProperty.getSuperProperty();
                domain = superProperty.getDomain();
            }

        }
        return domain;
    }

    private void preprocessRestrictions(OntModel model) {
        ExtendedIterator extendedIterator = model.listRestrictions();
        while (extendedIterator.hasNext()) {
            Restriction restriction = (Restriction) extendedIterator.next();
            try {
                preprocessRestriction(restriction);
            } catch (Exception ex) {
                if (log.isInfoEnabled()) {
                    log.info("Skipping. " + ex.getMessage());
                }
            }
        }
    }

    private void preprocessRestriction(Restriction restriction) {
        OntProperty ontProperty = restriction.getOnProperty();
        Set<PropertyEditor> propertyEditors = propertyToEditorMap.get(
                ontProperty.getLocalName());
        if (propertyEditors == null) {
            throw new IllegalBioPAXArgumentException("No editors for property " +
                    ontProperty.getLocalName());
        }
        ExtendedIterator iterator = restriction.listSubClasses(true);
        while (iterator.hasNext()) {
            OntClass ontClass = (OntClass) iterator.next();
            Class domain;
            try {
                domain = extractClass(ontClass);
            } catch (IllegalBioPAXArgumentException e) {
                if (log.isInfoEnabled()) {
                    log.info("Skipping. (" + e.getMessage() + ")");
                }
                continue;
            }
            for (PropertyEditor propertyEditor : propertyEditors) {
                if (propertyEditor.getDomain().isAssignableFrom(domain)) {
                    if (restriction.isAllValuesFromRestriction()) {
                        AllValuesFromRestriction valuesFromRestriction =
                                restriction.asAllValuesFromRestriction();
                        if (propertyEditor instanceof ObjectPropertyEditor) {
                            OntClass values = (OntClass) valuesFromRestriction.
                                    getAllValuesFrom().
                                    as(OntClass.class);
                            Set<Class<? extends BioPAXElement>> ranges = getSetOfJavaClasses(
                                    values);
                            ((ObjectPropertyEditor) propertyEditor).
                                    addRangeRestriction(domain, ranges);
                        }
                    }
//		todo			restriction.isCardinalityRestriction()||
//						restriction.isMaxCardinalityRestriction()))
//						{
//							System.out.println(domain);
//							System.out.println(propertyEditor.getProperty());
//						}
                }
            }
        }
    }


    protected Class<? extends BioPAXElement> extractClass(Resource resource) {
        String localName = getJavaName(resource);
        return getModelInterface(localName);
    }

    private String getJavaName(Resource resource) {
        // Since java does not allow '-' replace them all with '_'
        return resource.getLocalName().replaceAll("-", "_");
    }

    private Set<Class<? extends BioPAXElement>> getSetOfJavaClasses(OntClass values) {
        HashSet<Class<? extends BioPAXElement>> set = new HashSet<Class<? extends BioPAXElement>>();
        recursivelyTraverse(values, set);
        assert !set.isEmpty();
        return set;
    }

    private void recursivelyTraverse(OntClass values, HashSet<Class<? extends BioPAXElement>> set) {
        if (values.isUnionClass()) {
            UnionClass unionClass = values.asUnionClass();
            ExtendedIterator iterator1 =
                    unionClass.listOperands();
            while (iterator1.hasNext()) {
                recursivelyTraverse((OntClass) iterator1.next(), set);
            }
        } else {
            set.add(extractClass(values));
        }
    }

// ------------------------ INTERFACE METHODS ------------------------

// --------------------- Interface EditorMap ---------------------

    // -------------------------- OTHER METHODS --------------------------

// --------------------- ACCESORS and MUTATORS---------------------

    public XSDDatatype getDataTypeFor(PropertyEditor editor) {
        Class range = editor.getRange();
        XSDDatatype type = null;
        if (range.isEnum() || range.equals(String.class)) {
            type = XSDDatatype.XSDstring;
        } else if (range.equals(double.class)) {
            type = XSDDatatype.XSDdouble;
        } else if (range.equals(int.class)) {
            type = XSDDatatype.XSDint;
        } else if (range.equals(float.class)) {
            type = XSDDatatype.XSDfloat;
        } else if (range.equals(Boolean.class) || range.equals(boolean.class)) {
            type = XSDDatatype.XSDboolean;
        }

        return type;
    }

// -------------------------- INNER CLASSES --------------------------


    void writeSimpleEditorMapProperties(OutputStream out) {
        PrintStream stream = new PrintStream(out);
        for (Class<? extends BioPAXElement> aClass : classToEditorMap.keySet()) {
            stream.print(aClass.getSimpleName() + " ");

        }
        stream.println();
        for (Set<PropertyEditor> propertyEditors : propertyToEditorMap.values()) {
            for (PropertyEditor propertyEditor : propertyEditors) {
                stream.println(propertyEditor.toString());
            }
        }
        stream.close();
    }


	public BioPAXLevel getLevel()
	{
		return level;
	}
}




