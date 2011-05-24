package org.biopax.paxtools.io.jena;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.io.BioPAXIOHandlerAdapter;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.biopax.paxtools.model.BioPAXLevel.isInBioPAXNameSpace;


/**
 * Provides I/O support for BioPAX models presented in OWL format using the {@link com.hp.hpl.jena}
 * package.
 */
public class JenaIOHandler extends BioPAXIOHandlerAdapter
{
// ------------------------------ FIELDS ------------------------------


	private static final Log log = LogFactory.getLog(JenaIOHandler.class);

	// ------------------------------ MAPS ------------------------------
	private HashMap<Object, Individual> objectToIndividualMap;

	OntModel ontModel;

	/**
	 * If set to true, "error-mode: strict" option will be passed to Jena reader.
	 */

	// --------------------------- CONSTRUCTORS ---------------------------
	public JenaIOHandler()
	{
		this(null, null);
	}

	public JenaIOHandler(BioPAXLevel level)
	{
		this(level.getDefaultFactory(), level);
	}

	public JenaIOHandler(BioPAXFactory factory, BioPAXLevel level)
	{
		super(factory, level);
		resetEditorMap();
	}

	// -------------------------- OTHER METHODS --------------------------

	@Override
	protected void resetEditorMap()
	{
		setEditorMap(new JenaEditorMap(this.getLevel()));
	}

	protected void init(InputStream in)
	{
		ontModel = readJenaModel(in);
	}

	protected Map<String, String> readNameSpaces()
	{
		Map<String, String> namespaces = new HashMap<String, String>();
		namespaces.putAll(ontModel.getNsPrefixMap());
		return namespaces;
	}


	protected void createAndBind(Model model)
	{
		try
		{
			createObjects(ontModel, model);
			bindPropertiesToObjects(ontModel, model);
		}
		catch (IllegalBioPAXArgumentException e)
		{
			log.error(e);
		}
	}

	/**
	 * Writes a model in OWL format using the an output stream.
	 * @param model model to be converted into OWL format
	 * @param outputStream output stream to which the model will be written
	 */
	public void convertToOWL(Model model, OutputStream outputStream)
	{
		objectToIndividualMap = new HashMap<Object, Individual>();
		OntModel ontModel = initializeEmptyOntModel(model);
		createIndividuals(ontModel, model);
		bindObjectsToProperties(ontModel, model);
		RDFWriter writer = ontModel.getWriter("RDF/XML-ABBREV");
		writer.setProperty("relativeURIs", "same-document, relative, parent, absolute");
		String base = model.getNameSpacePrefixMap().get("");
		if (log.isDebugEnabled())
		{
			log.debug("base = " + base);
		}
		writer.setProperty("xmlbase", base);
		writer.setProperty("showXmlDeclaration", "true");
		writer.write(ontModel, outputStream, base);
		//objectToIndividualMap = null;
	}

	public OntModel readJenaModel(InputStream in)
	{
		OntModel ontModel = org.biopax.paxtools.io.jena.JenaHelper.createModel();
//		ontModel.setStrictMode(true);
//		ontModel.getReader().setProperty("error-mode", "strict");
		ontModel.read(in, "");
		ontModel.loadImports();
		return ontModel;
	}


	private void createObjects(OntModel ontModel, Model model)
	{
		ExtendedIterator extendedIterator = ontModel.listIndividuals();
		while (extendedIterator.hasNext())
		{
			Individual individual = (Individual) extendedIterator.next();
			OntClass ontClass = (OntClass) individual.getRDFType().as(OntClass.class);
			createAndAdd(model, individual.getURI(), ontClass.getLocalName());
		}
	}


	private void bindPropertiesToObjects(OntModel ontModel, Model model) throws IllegalBioPAXArgumentException
	{
		ExtendedIterator extendedIterator = ontModel.listIndividuals();
		while (extendedIterator.hasNext())
		{
			Individual individual = (Individual) extendedIterator.next();
			BioPAXElement bpe = model.getByID(individual.getURI());
			StmtIterator stmtIterator = individual.listProperties();
			while (stmtIterator.hasNext())
			{
				Statement statement = (Statement) stmtIterator.next();
				Property predicate = statement.getPredicate();

				try
				{
					if (isInBioPAXNameSpace(predicate.getNameSpace()))
					{
						bindProperty(predicate, bpe, individual, model);
					} else if (predicate.getLocalName().equals("comment") && predicate.getNameSpace().equals(
							"http://www.w3.org/1999/02/22-rdf-syntax-ns#"))
					{
						PropertyEditor editor = getRDFCommentEditor(bpe);

						OntProperty ontProperty;
						try
						{
							ontProperty = ((OntProperty) predicate.as(OntProperty.class));
						}
						catch (ConversionException e)
						{
							throw new IllegalBioPAXArgumentException(
									"Unknown property! " + predicate + " bpe:" + bpe.getRDFId(), e);
						}
						checkCardinalityAndBindValue(bpe, individual, model, ontProperty, editor);
					} else if (log.isDebugEnabled())
					{
						log.debug("Skipping non-biopax statement:" + predicate);
					}

				}
				catch (IllegalBioPAXArgumentException e)
				{

					log.error("Conversion error. " + e);

				}
				catch (IllegalAccessException ex)
				{
					throw new IllegalBioPAXArgumentException("Conversion failed.", ex);
				}
				catch (InvocationTargetException ex)
				{
					throw new IllegalBioPAXArgumentException("Conversion failed.", ex);
				}
			}
		}
	}


	private void bindProperty(Property predicate, BioPAXElement bpe, Individual individual, Model model)
			throws IllegalAccessException, InvocationTargetException
	{
		OntProperty ontProperty;
		try
		{
			ontProperty = ((OntProperty) predicate.as(OntProperty.class));
		}
		catch (ConversionException e)
		{
			throw new IllegalBioPAXArgumentException("Unknown property! " + predicate + " bpe:" + bpe.getRDFId(), e);
		}
		String localName = ontProperty.getLocalName();
		PropertyEditor editor = this.getEditorMap().getEditorForProperty(localName, bpe.getModelInterface());
		if (editor != null)
		{
			checkCardinalityAndBindValue(bpe, individual, model, ontProperty, editor);
		} else
		{
			throw new IllegalBioPAXArgumentException(
					"Could not locate editor! " + predicate + " element:" + bpe.getRDFId() + " property:" +
					localName);
		}
	}

	private void checkCardinalityAndBindValue(BioPAXElement bpe, Individual individual, Model model,
	                                          OntProperty ontProperty, PropertyEditor editor)
	{
		if (editor.isMultipleCardinality())
		{
			NodeIterator nodeIterator = individual.listPropertyValues(ontProperty);
			while (nodeIterator.hasNext())
			{
				RDFNode propertyValue = (RDFNode) nodeIterator.next();
				bindValue(propertyValue, editor, bpe, model);
			}
		} else
		{
			RDFNode propertyValue = individual.getPropertyValue(ontProperty);
			bindValue(propertyValue, editor, bpe, model);
		}
	}

	private void bindValue(RDFNode propertyValue, PropertyEditor editor, BioPAXElement bpe, Model model)
	{

		String stringValue = null;
		if (propertyValue.isResource())
		{
			stringValue = ((Resource) propertyValue).getURI();

		} else if (propertyValue.isLiteral())
		{
			stringValue = ((Literal) propertyValue).getString();
		} else
		{
			log.error("Unexpected state." + propertyValue + " is not a resource or literal.");
		}

		bindValue(stringValue, editor, bpe, model);

	}


// --------------------------- WRITER METHODS ---------------------------

	private OntModel initializeEmptyOntModel(Model model)
	{
		OntModel ontModel = org.biopax.paxtools.io.jena.JenaHelper.createModel();

		String xmlBase = model.getNameSpacePrefixMap().get("");
		if (xmlBase == null || xmlBase.equals(""))
		{
			xmlBase = "http://biopax.org/paxtools";
		}
		Ontology base = ontModel.createOntology(xmlBase);
		String uri = model.getLevel().getNameSpace();
		uri = uri.substring(0, uri.length() - 1);
		if (log.isDebugEnabled())
		{
			log.debug("uri = " + uri);
		}
		ontModel.setNsPrefixes(model.getNameSpacePrefixMap());
		base.addImport(ontModel.createResource(uri));
		ontModel.loadImports();
		return ontModel;
	}

	private void createIndividuals(OntModel ontModel, Model model)
	{
		for (BioPAXElement bp : model.getObjects())
		{
			String name = bp.getModelInterface().getName();
			name = name.substring(name.lastIndexOf('.') + 1);
			OntClass ontClass = ontModel.getOntClass(this.getLevel().getNameSpace() + name);
			if (log.isTraceEnabled())
			{
				log.trace("ontClass = " + ontClass);
			}

			Individual individual = ontModel.createIndividual(bp.getRDFId(), ontClass);
			if (log.isTraceEnabled())
			{
				log.trace("individual = " + individual);
			}

			objectToIndividualMap.put(bp, individual);
		}
	}

	private void bindObjectsToProperties(OntModel ontModel, Model model)
	{
		for (BioPAXElement bean : model.getObjects())
		{
			Set<PropertyEditor> beanEditors = this.getEditorMap().getEditorsOf(bean);

			for (PropertyEditor propertyEditor : beanEditors)
			{
				insertStatement(propertyEditor, bean, ontModel);
			}
		}
	}

	/**
	 * Builds an OWL statement from the given <em>bean</em> using the <em>editor</em> and inserts it
	 * into an ontology model.
	 * @param editor editor to be used for the statement build
	 * @param bean bean which the statement is about
	 * @param ontModel ontology model into which the built statement will be inserted
	 */
	private void insertStatement(PropertyEditor editor, BioPAXElement bean, OntModel ontModel)

	{
		Set value = editor.getValueFromBean(bean);// value cannot be null
		for (Object valueElement : value)
		{
			if (!editor.isUnknown(valueElement)) buildStatementFor(bean, editor, valueElement, ontModel);
		}
	}


	private void buildStatementFor(BioPAXElement bean, PropertyEditor editor, Object value, OntModel ontModel)
	{
		assert (bean != null && editor != null);
		Property property = ontModel.getProperty(this.getLevel().getNameSpace() + editor.getProperty());
		Individual ind = objectToIndividualMap.get(bean);
		Class range = editor.getRange();
		JenaEditorMap editorMap = (JenaEditorMap) this.getEditorMap();
		XSDDatatype dataType = editorMap.getDataTypeFor(editor);
		if (dataType != null)
		{
			ind.addProperty(property, ontModel.createTypedLiteral(value.toString(), dataType));
		} else
		{
			Individual valueInd = objectToIndividualMap.get(value);
			if (valueInd == null) // TODO not sure about Boolean case here, but this makes tests pass...
			{
				throw new IllegalBioPAXArgumentException(
						range + " : for value '" + value + "' coresponding individual value is NULL " +
						"(objectToIndividualMap)");
			}

			ind.addProperty(property, valueInd);
		}
	}

}


