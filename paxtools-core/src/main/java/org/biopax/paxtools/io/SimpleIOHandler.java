package org.biopax.paxtools.io;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.controller.AbstractPropertyEditor;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.BiochemicalPathwayStep;
import org.biopax.paxtools.model.level3.Named;
import org.biopax.paxtools.util.BioPaxIOException;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.*;

import static javax.xml.stream.XMLStreamConstants.*;

/**
 * Simple BioPAX reader/writer.
 *
 * This class provides a JAXP based I/O handler. As compared to Jena based implementation it offers ~10x performance,
 * significantly less memory requirements when reading large files and a lightweight deployement. It, however,
 * is not as robust as the Jena based reader and can not read non-RDF/XML OWL formats or non-UTF encodings.For those,
 * you might want to use the JenaIOHandler class
 */
public final class SimpleIOHandler extends BioPAXIOHandlerAdapter
{
	private static final Log log = LogFactory.getLog(SimpleIOHandler.class);

	private XMLStreamReader r;

	boolean propertyContext = false;

	private List<Triple> triples;

	private boolean mergeDuplicates;

	private static final String owlNS = "http://www.w3.org/2002/07/owl#";

	private static final String xsdNS = "http://www.w3.org/2001/XMLSchema#";

	private static final String rdfNS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	private static final String RDF_ID = "rdf:ID=\"";

	private static final String RDF_about = "rdf:about=\"";

	private static final String newline = System.getProperty("line.separator");

	private static final String close = "\">";

	private boolean normalizeNameSpaces;

	private boolean absoluteUris;


	// --------------------------- CONSTRUCTORS ---------------------------

	/**
	 * Basic constructor, defaults to Level 3 and default BioPAXFactory
	 */
	public SimpleIOHandler()
	{
		this(null, null);
	}

	/**
	 * Basic constructor, defaults to level.defaultFactory
	 * @param level BioPAXLevel to handle.
	 */
	public SimpleIOHandler(BioPAXLevel level)
	{
		this(level.getDefaultFactory(), level);
	}

	/**
	 * Full constructor
	 * @param factory to create BioPAX objects
	 * @param level BioPAX level to handle.
	 */
	public SimpleIOHandler(BioPAXFactory factory, BioPAXLevel level)
	{
		super(factory, level);
		normalizeNameSpaces = true;
		mergeDuplicates = false;
	}

	/**
	 * If set to true, the reader will merge duplicate individuals, i.e. individuals that has the same ID. Otherwise
	 * it will throw an exception. By default it is set to false.
	 * @param mergeDuplicates true/false
	 */
	public void mergeDuplicates(boolean mergeDuplicates)
	{
		this.mergeDuplicates = mergeDuplicates;
	}

	/**
	 * If set to true, property editors will check restrictions at the subclass level and throw an exception if
	 * violated. This is true by default.
	 *
	 * WARNING: This is a static parameter and will change behaviour for the whole thread. Do not change unless you
	 * need to read a (relatively exotic) BioPAX export that violates subclass level restrictions.
	 * @param checkRestrictions true/false
	 */
	public void checkRestrictions(boolean checkRestrictions)
	{
		AbstractPropertyEditor.checkRestrictions.set(checkRestrictions);
	}
	// -------------------------- OTHER METHODS --------------------------

	/**
	 * This method resets the editor map. Editor maps are used to map property names to property editors. Resetting the
	 * editor map might be required to switch between levels.
	 */
	@Override
	protected void resetEditorMap()
	{
		setEditorMap(SimpleEditorMap.get(this.getLevel())); // was 'level' - bug!
	}

	/**
	 * This may be required for external applications to access the specific information (e.g.,
	 * location) when reporting XML exceptions.
	 * @return current XML stream state summary
	 */
	public String getXmlStreamInfo()
	{
		StringBuilder sb = new StringBuilder();

		int event = r.getEventType();
		if (event == START_ELEMENT || event == END_ELEMENT || event == ENTITY_REFERENCE)
		{
			sb.append(r.getLocalName());
		}

		if (r.getLocation() != null)
		{
			sb.append(" line ");
			sb.append(r.getLocation().getLineNumber());
			sb.append(" column ");
			sb.append(r.getLocation().getColumnNumber());
		}

		return sb.toString();
	}


	@Override protected void init(InputStream in)
	{
		try
		{
			XMLInputFactory xmlf = XMLInputFactory.newInstance();
			//this is to return string with encoded chars as one event (not splitting)
			xmlf.setProperty("javax.xml.stream.isCoalescing", true);
			r = xmlf.createXMLStreamReader(in);
			
			triples = new LinkedList<Triple>();

		}
		catch (XMLStreamException e)
		{
			//e.printStackTrace();
			throw new BioPaxIOException(e.getClass().getSimpleName() + " " + e.getMessage() + "; " + e.getLocation());
		}

	}

	@Override protected void reset(InputStream in)
	{

		this.triples=null;
		try
		{
			r.close();
		}
		catch (XMLStreamException e)
		{
			throw new RuntimeException("Can't close the stream");
		}
		r=null;
		super.reset(in);
	}

	@Override protected Map<String, String> readNameSpaces()
	{
		Map<String, String> ns = new HashMap<String, String>();
		try
		{
			if (r.getEventType() == START_DOCUMENT)
			{
				r.next();
			} else
			{
				throw new BioPaxIOException("Unexpected element at start");
			}

			// Skip any comment before we read the RDF headers
			while (r.getEventType() == COMMENT)
			{
				r.next();
			}

			if (r.getEventType() != START_ELEMENT)
			{
				throw new BioPaxIOException("Unexpected element at start: " + r.getEventType());
			}

			if (!r.getLocalName().equalsIgnoreCase("rdf"))
			{
				throw new BioPaxIOException("Unexpected element at start: " + r.getLocalName());
			}

			int count = r.getNamespaceCount();
			for (int i = 0; i < count; i++)
			{
				String pre = r.getNamespacePrefix(i);
				if (pre == null)
				{
					pre = "";
				}
				String namespace = r.getNamespaceURI(pre);

				ns.put(pre, namespace);
			}

			base = null;
			for (int i = 0; i < r.getAttributeCount(); i++)
			{
				if ("base".equalsIgnoreCase(r.getAttributeLocalName(i)) &&
				    "xml".equalsIgnoreCase(r.getAttributePrefix(i)))
				{
					base = r.getAttributeValue(i);
				}
			}
		}
		catch (XMLStreamException e)
		{
			throw new BioPaxIOException(e.getClass().getSimpleName() + " " + e.getMessage() + "; " + e.getLocation());
		}
		return ns;

	}

	@Override protected void createAndBind(Model model)
	{
		try
		{
			int type;
			while ((type = r.getEventType()) != END_DOCUMENT)
			{
				switch (type)
				{
					case START_ELEMENT:
						if (BioPAXLevel.isInBioPAXNameSpace(r.getName().getNamespaceURI()))
						{
							String lname = r.getLocalName();
							BioPAXLevel level = getLevel();
							Class<? extends BioPAXElement> clazz;
							try
							{
								clazz = level.getInterfaceForName(lname);
							}
							catch (IllegalBioPAXArgumentException e)
							{
								log.fatal("Unknown BioPAX element location " + lname + " at " + r.getLocation());
								throw e; // for backward compatibility
							}
							if (this.getFactory().canInstantiate(clazz))
							{
								processIndividual(model);
							} else
							{
								if (log.isTraceEnabled())
									log.trace("Ignoring element: " + lname);
								skip();
							}

						}
						break;
					case CHARACTERS:
						if (log.isTraceEnabled())
						{
							StringBuilder sb = new StringBuilder("Ignoring text ");
							if (r.hasName()) sb.append(r.getLocalName());
							if (r.hasText()) sb.append(r.getText());
							if (log.isTraceEnabled())
								log.trace(sb.toString());
						}
						break;
					case END_ELEMENT:
						if (log.isTraceEnabled())
							log.trace(r);
						break;
					default:
						if (log.isTraceEnabled())
							log.trace("Test this!:" + type);
				}
				r.next();

			}
		r.close();
		}
		catch (XMLStreamException e)
		{
			throw new BioPaxIOException(e.getClass().getSimpleName() + " " + e.getMessage() + "; " + e.getLocation());
		}

		for (Triple triple : triples)
		{
			try
			{
				bindValue(triple, model);
			}
			catch (IllegalBioPAXArgumentException e)
			{
				log.warn("Binding " + e);
			}
		}

	}

	/**
	 * Binds property.
	 *
	 * This method also throws exceptions related to binding.
	 * @param triple A java object that represents an RDF Triple - domain-property-range.
	 * @param model that is being populated.
	 */
	private void bindValue(Triple triple, Model model)
	{
		if (log.isDebugEnabled())
		{
			log.debug(triple);
		}
		BioPAXElement domain = model.getByID(triple.domain);

		PropertyEditor editor = this.getEditorMap().getEditorForProperty(triple.property, domain.getModelInterface());

		bindValue(triple.range, editor, domain, model);
	}


	private String processIndividual(Model model) throws XMLStreamException
	{
		String s = r.getLocalName();
		String id;
		try
		{
			id = getId();
		}
		catch (NullPointerException e)
		{
			throw new BioPaxIOException("Error processing individual " + s + ". rdf:ID or rdf:about not found!", e);
		}

		if (factory.canInstantiate((level.getInterfaceForName(s))))
		{
			BioPAXElement bpe = model.getByID(id);
			if (!mergeDuplicates || bpe == null)
			{
				createBpe(s, id, model); //throws an exception when (mergeDuplicates==false && bpe!=null)
			} else if(!s.equals(bpe.getModelInterface().getSimpleName())) {
				//i.e., type mismatch,
				//whereas (mergeDuplicates==true && bpe != null) is true already -
				throw new BioPaxIOException(String.format("processIndividual: " +
						"despite mergeDuplicates is True, failed/skipped creating an instance " +
						"of %s, URI:%s, because previously added object (same URI) was of different type: %s",
						s, id, bpe.getModelInterface().getSimpleName()));
			}
		} else {
			if (r.hasText())
			{
				log.warn("Unknown class :" + r.getText());
			} else
			{
				log.warn("Unknown class :" + r);
			}
			skip();
		}
		propertyContext = true;
		r.next();
		while (r.getEventType() != END_ELEMENT)
		{
			if (r.getEventType() == START_ELEMENT)
			{
				processProperty(model, id);
				propertyContext = true;

			}
			r.next();
		}
		return id;
	}

	private void createBpe(String s, String id, Model model)
	{
		BioPAXElement bpe = factory.create(s, id);
		model.add(bpe);
	}

	private void skip() throws XMLStreamException
	{
		int depth = 1;
		while (!(r.getEventType() == END_ELEMENT && depth == 0))
		{
			r.next();
			switch (r.getEventType())
			{
				case START_ELEMENT:
					depth++;
					break;
				case END_ELEMENT:
					depth--;
					break;
			}
		}
	}

	public String getId()
	{
		String id = r.getAttributeValue(rdf, "ID");
		if (id == null)
		{
			id = r.getAttributeValue(rdf, "about");
			if (id.startsWith("#"))
			{
				id = base + id.substring(1, id.length());

			}
		} else if (base != null)
		{
			id = base + id;
		}

		return id;
	}

	private void processProperty(Model model, String ownerID) throws XMLStreamException
	{
		if (rdfs.equals(r.getNamespaceURI()) && "comment".equals(r.getLocalName()))
		{
			BioPAXElement paxElement = model.getByID(ownerID);
			AbstractPropertyEditor commentor = getRDFCommentEditor(paxElement);
			r.next();
			assert r.getEventType() == CHARACTERS;
			String text = r.getText();
			commentor.setValueToBean(text, paxElement);
			log.warn("rdfs:comment is converted into the bp:comment; " +
			         "however, this can be overridden " +
			         "if there exists another bp:comment (element: " +
			         paxElement.getUri() + " text: " + text + ")");
			gotoEndElement();
		} else if (bp != null && bp.equals(r.getNamespaceURI()))
		{
			String property = r.getLocalName();
			String resource = r.getAttributeValue(rdf, "resource");
			if (resource != null)
			{
				if (resource.startsWith("#"))
				{
					resource = (base == null ? "" : base) + resource.substring(1, resource.length());
				}
				gotoEndElement();
			} else
			{
				r.next();
				boolean found = false;
				while (r.getEventType() != END_ELEMENT)
				{
					if (!found && r.getEventType() == CHARACTERS)
					{
						StringBuilder buff = new StringBuilder(r.getText());
						r.next();
						while (r.getEventType() == CHARACTERS)
						{
							buff.append(r.getText());
							r.next();
						}
						resource = buff.toString();

					} else if (r.getEventType() == START_ELEMENT)
					{
						propertyContext = false;
						resource = processIndividual(model);
						found = true;
						r.next();
					} else r.next();
				}
				resource = (!found && resource != null) ? resource.replaceAll("[\n\r\t ]+", " ") : resource;
			}

			log.trace("setting = " + resource);
			triples.add(new Triple(ownerID, resource, property));
			propertyContext = false;
		} else
		{
			log.trace("ignoring unknown element " +
			          r.getNamespaceURI() + r.getLocalName());
			gotoEndElement();
		}

	}

	private void gotoEndElement() throws XMLStreamException
	{
		while (r.getEventType() != END_ELEMENT)
		{
			r.next();
		}
	}


	public class Triple
	{
		public String domain, range, property;

		private Triple(String domain, String range, String property)
		{
			this.domain = domain;
			this.range = range;
			this.property = property;

		}

		@Override
		public String toString()
		{
			return String.format("domain: %s, property: %s, range: %s", domain, property, range);
		}

	}


	/**
	 * Converts a model into BioPAX (OWL) format, and writes it into
	 * the outputStream. Saved data can be then read via {@link BioPAXIOHandler}
	 * interface (e.g., {@link SimpleIOHandler}).
	 *
	 * Note: When the model is incomplete (i.e., contains elements that refer externals,
	 * dangling BioPAX elements) and is exported by this method, it works; however one
	 * will find corresponding object properties set to NULL later,
	 * after converting such data back to Model.
	 * 
	 * Note: if the model is very very large, and the output stream is a byte array stream,
	 * then you can eventually get OutOfMemoryError "Requested array size exceeds VM limit"
	 * (max. array size is 2Gb)
	 * 
	 * @param model model to be converted into OWL format
	 * @param outputStream output stream into which the output will be written
	 * @throws BioPaxIOException in case of I/O problems
	 */
	public void convertToOWL(Model model, OutputStream outputStream)
	{
		initializeExporter(model);

		try
		{
			Writer out = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
			writeObjects(out, model);
			out.close();
		}
		catch (IOException e)
		{
			throw new BioPaxIOException("Cannot convert to OWL!", e);
		}
	}


	/**
	 * Writes the XML representation of individual BioPAX element that
	 * is BioPAX-like but only for display or debug purpose (incomplete).
	 *
	 * Note: use {@link BioPAXIOHandler#convertToOWL(org.biopax.paxtools.model.Model, java.io.OutputStream)}
	 * convertToOWL(org.biopax.paxtools.model.Model, Object)} instead
	 * if you have a model and want to save and later restore it.
	 * @param out output
	 * @param bean BioPAX object
	 * @throws IOException when the output writer throws
	 */
	public void writeObject(Writer out, BioPAXElement bean) throws IOException
	{
		String name = "bp:" + bean.getModelInterface().getSimpleName();
		writeIDLine(out, bean, name);

		Set<PropertyEditor> editors = editorMap.getEditorsOf(bean);
		if (editors == null || editors.isEmpty())
		{
			log.info("no editors for " + bean.getUri() + " | " + bean.getModelInterface().getSimpleName());
			out.write(newline + "</" + name + ">");
			return;
		}

		for (PropertyEditor editor : editors)
		{
			Set value = editor.getValueFromBean(bean); //is never null
			for (Object valueElement : value)
			{
				if (!editor.isUnknown(valueElement)) writeStatementFor(bean, editor, valueElement, out);
			}
		}

		out.write(newline + "</" + name + ">");
	}


	private void writeObjects(Writer out, Model model) throws IOException
	{
		writeHeader(out);

		Set<BioPAXElement> bioPAXElements = model.getObjects();
		for (BioPAXElement bean : bioPAXElements)
		{
			writeObject(out, bean);
		}

		out.write(newline + "</rdf:RDF>");
	}


	private void writeStatementFor(BioPAXElement bean, PropertyEditor editor, Object value, Writer out)
			throws IOException
	{
		assert (bean != null && editor != null && value != null);

		//fix (for L3 only): skip 'name' if it's present in the displayName, etc..
		if (editor.getProperty().equalsIgnoreCase("name") && bean instanceof Named)
		{ // the latter maybe not necessary...
			Named named = (Named) bean;
			if(value.equals(named.getDisplayName()) || value.equals(named.getStandardName()))
			{
				return;
			}
		}
		
		if (editor.getProperty().equalsIgnoreCase("stepProcess") && bean instanceof BiochemicalPathwayStep)
		{
			BiochemicalPathwayStep bps = (BiochemicalPathwayStep) bean;
			if(value.equals(bps.getStepConversion()))
			{
				return;
			}
		}

		String prop = "bp:" + editor.getProperty();
		out.write(newline + " <" + prop);

		if (value instanceof BioPAXElement)
		{
			String id = ((BioPAXElement) value).getUri();
			assert id != null;
			if (!absoluteUris && base != null && id.startsWith(base))
			{
				id = '#' + id.substring(base.length());
			}
			out.write(" rdf:resource=\"" + id + "\" />");
		} else
		{
			String type = findLiteralType(editor);
			String valString = StringEscapeUtils.escapeXml(value.toString());
			out.write(" rdf:datatype = \"" + xsd + type + "\">" + valString +
			          "</" + prop + ">");
		}
	}


	private String findLiteralType(PropertyEditor editor)
	{
		Class range = editor.getRange();
		String type = null;
		if (range.isEnum() || range.equals(String.class))
		{
			type = "string";
		} else if (range.equals(double.class) || range.equals(Double.class))
		{
			type = "double";
		} else if (range.equals(int.class) || range.equals(Integer.class))
		{
			type = "int";
		} else if (range.equals(float.class) || range.equals(Float.class))
		{
			type = "float";
		} else if (range.equals(boolean.class) || range.equals(Boolean.class))
		{
			type = "boolean";
		} else if (range.equals(long.class) || range.equals(Long.class))
		{
			type = "long";
		}
		return type;
	}


	private void writeIDLine(Writer out, BioPAXElement bpe, String name) throws IOException
	{

		out.write(newline + newline + "<" + name + " ");
		String s = bpe.getUri();
		if (!absoluteUris &&  base != null && s.startsWith(base))
		{
			String id = s.substring(base.length());
			out.write(RDF_ID + id + close);
		} else
		{
			out.write(RDF_about + s + close);
		}


	}


	private void initializeExporter(Model model)
	{
		base = model.getXmlBase();
		namespaces = new HashMap<String, String>(model.getNameSpacePrefixMap());

		normalizeNameSpaces(); // - for this reader/exporter tool
		// also save the changes to the model?
		if (normalizeNameSpaces)
		{
			model.getNameSpacePrefixMap().clear();
			model.getNameSpacePrefixMap().putAll(namespaces);
		}

		BioPAXLevel lev = model.getLevel();
		if (lev != this.level) resetLevel(lev, lev.getDefaultFactory());
	}


	private void normalizeNameSpaces()
	{
		String owlPre = null;
		String rdfPre = null;
		String xsdPre = null;

		for (String pre : namespaces.keySet())
		{
			String ns = namespaces.get(pre);
			if (rdfNS.equalsIgnoreCase(ns))
			{
				rdfPre = pre;
			} else if (owlNS.equalsIgnoreCase(ns))
			{
				owlPre = pre;
			} else if (xsdNS.equalsIgnoreCase(ns))
			{
				xsdPre = pre;
			}
		}

		if (owlPre != null)
		{
			namespaces.remove(owlPre);
		}

		if (rdfPre != null)
		{
			namespaces.remove(rdfPre);
		}

		if (xsdPre != null)
		{
			namespaces.remove(xsdPre);
		}

		namespaces.put("rdf", rdfNS);
		namespaces.put("owl", owlNS);
		namespaces.put("xsd", xsdNS);
	}


	private void writeHeader(Writer out) throws IOException
	{
		//Header
		out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.write(newline + "<rdf:RDF");
		String bpns = this.editorMap.getLevel().getNameSpace();
		for (String pre : namespaces.keySet())
		{
			if (!pre.equals("bp"))
			{
				out.write(newline + " xmlns" +
				          (("".equals(pre)) ? "" : ":" + pre) + "=\"" + namespaces.get(pre) + "\"");
			}
		}
		// write 'xmlns:bp'
		out.write(newline + " xmlns:bp" + "=\"" + bpns + "\"");

		if (base != null)
		{
			out.write(newline + " xml:base=\"" + base + "\"");
		}

		out.write(">");
		out.write(newline + "<owl:Ontology rdf:about=\"\">");
		out.write(newline + " <owl:imports rdf:resource=\"" + bpns + "\" />");
		out.write(newline + "</owl:Ontology>");
	}

	
	/**
	 * Sets the flag used when exporting a BioPAX model to RDF/XML:
	 * true - to always write full URI in rdf:resource and use 
	 * rdf:about instead rdf:ID (does not matter xml:base is set or not).
	 * This is good for the data loading a RDF/SPARQL servers, such as Virtuoso,
	 * so they generate correct and resolvable links from BioPAX URIs
	 * (use of rdf:ID="localId" and rdf:resource="#localID" is known to make
	 * them insert extra '#' between xml:base and localId).
	 * 
	 * @param absoluteUris true/false - whether to force writing absolute URIs (thus, never use rdf:ID)
	 */
	public void absoluteUris(boolean absoluteUris) {
		this.absoluteUris = absoluteUris;
	}
		
	/**
	 * @see #absoluteUris(boolean)
	 * @return true/false
	 */
	public boolean isAbsoluteUris()
	{
		return this.absoluteUris;
	}
	

	/**
	 * Sets the flag used when exporting a BioPAX model to RDF/XML:
	 * true - to clean up current namespaces (e.g., those read from a file)
	 * and use defaults instead (prefixes: 'rdf', 'rdfs', 'owl', 'xsd')
	 * @param normalizeNameSpaces true/false
	 */
	public void normalizeNameSpaces(boolean normalizeNameSpaces)
	{
		this.normalizeNameSpaces = normalizeNameSpaces;
	}

	/**
	 * @see #normalizeNameSpaces()
	 * @return true/false
	 */
	public boolean isNormalizeNameSpaces()
	{
		return this.normalizeNameSpaces;
	}

	/**
	 * @see #mergeDuplicates(boolean)
	 * @return true/false
	 */
	public boolean isMergeDuplicates()
	{
		return this.mergeDuplicates;
	}


	/**
	 * Serializes a (not too large) BioPAX model to the RDF/XML (OWL) formatted string.
	 *
	 * @param model a BioPAX object model to convert to the RDF/XML format
	 * @return the BioPAX data in the RDF/XML format
	 * @throws IllegalArgumentException if model is null
	 * @throws OutOfMemoryError when it cannot be stored in a byte array (max 2Gb).
	 */
	public static String convertToOwl(Model model)
	{
		if (model == null) throw new IllegalArgumentException();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		(new SimpleIOHandler(model.getLevel())).convertToOWL(model, outputStream);

		try
		{
			return outputStream.toString("UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			log.error(e);
			return outputStream.toString();
		}
	}

}

