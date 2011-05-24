package org.biopax.paxtools.io;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.controller.*;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.model.level3.Named;
import org.biopax.paxtools.util.BioPaxIOException;
import org.biopax.paxtools.util.Filter;
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
 */
public class SimpleIOHandler extends BioPAXIOHandlerAdapter
{
	private static final Log log = LogFactory.getLog(SimpleIOHandler.class);

	private XMLStreamReader r;
	boolean propertyContext = false;
	private String base;
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
	
	
	// --------------------------- CONSTRUCTORS ---------------------------

	public SimpleIOHandler()
	{
		this(null, null);
	}

	public SimpleIOHandler(BioPAXLevel level)
	{
		this(level.getDefaultFactory(), level);
	}

	public SimpleIOHandler(BioPAXFactory factory, BioPAXLevel level)
	{
		super(factory, level);
		normalizeNameSpaces = true;
		mergeDuplicates = false;
	}

	public void mergeDuplicates(boolean mergeDuplicates)
	{
		this.mergeDuplicates = mergeDuplicates;
	}
	// -------------------------- OTHER METHODS --------------------------	

	@Override
	final protected void resetEditorMap()
	{
		setEditorMap(SimpleEditorMap.get(this.getLevel())); // was 'level' - bug!
	}

	/**
	 * This may be required for external applications to access the specific information (e.g.,
	 * location) when reporting XML exceptions.
	 *
	 * @return
	 */
	public String getXmlStreamInfo()
	{
		StringBuffer sb = new StringBuffer();

		int event = r.getEventType();
		if (event == START_ELEMENT
		    || event == END_ELEMENT
		    || event == ENTITY_REFERENCE)
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


	protected void init(InputStream in)
	{
		try
		{
			r = XMLInputFactory.newInstance().createXMLStreamReader(in);
			triples = new LinkedList<Triple>();

		}
		catch (XMLStreamException e)
		{
			//e.printStackTrace();
			throw new BioPaxIOException(
					e.getClass().getSimpleName()
					+ " " + e.getMessage()
					+ "; " + e.getLocation());
		}

	}

	protected Map<String, String> readNameSpaces()
	{
		Map<String, String> ns = new HashMap<String, String>();
		try
		{
			if (r.getEventType() == START_DOCUMENT)
			{
				r.next();
			}
			else
			{
				throw new BioPaxIOException("Unexpected element at start");
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
				if (pre.equals(""))
				{
					base = namespace;
				}

				ns.put(pre, namespace);
			}


		}
		catch (XMLStreamException e)
		{
			throw new BioPaxIOException(
					e.getClass().getSimpleName()
					+ " " + e.getMessage()
					+ "; " + e.getLocation());
		}
		return ns;

	}

	protected void createAndBind(Model model)
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
							Class<? extends BioPAXElement> clazz = level.getInterfaceForName(lname);
							if (this.getFactory().canInstantiate(clazz))
							{
								processIndividual(model);
							}
							else
							{
								if (log.isTraceEnabled())
								{
									log.trace("Ignoring element: " + r.getLocalName());
								}
								skip();
							}

						}
						break;
					case CHARACTERS:
						if (log.isTraceEnabled())
						{
							StringBuffer sb = new StringBuffer("Ignoring text ");
							if(r.hasName())
								sb.append(r.getLocalName());
							if (r.hasText())
								sb.append(r.getText());
							log.trace(sb.toString());
						}
						break;
					case END_ELEMENT:
						if (log.isTraceEnabled())
						{
							log.trace(r);
						}
						break;
					default:
						if (log.isTraceEnabled())
						{
							log.trace("Test this!:" + type);
						}  //TODO
				}
				r.next();

			}

		}
		catch (XMLStreamException e)
		{
			throw new BioPaxIOException(
					e.getClass().getSimpleName()
					+ " " + e.getMessage()
					+ "; " + e.getLocation());
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
	 * <p/>
	 * This method also throws related to the binding exceptions.
	 *
	 * @param triple
	 * @param model
	 */
	private void bindValue(Triple triple, Model model)
	{
		if (log.isDebugEnabled())
		{
			log.debug(triple);
		}
		BioPAXElement domain = model.getByID(triple.domain);
		if (domain == null)
		{
			System.out.println("remove!!");
		}
		PropertyEditor editor =
				this.getEditorMap().getEditorForProperty(triple.property, domain.getModelInterface());

		bindValue(triple.range, editor, domain, model);
	}


	private String processIndividual(Model model) throws XMLStreamException
	{
		String s = r.getLocalName();
		String id = null;
		try
		{
			id = getId();
		}
		catch (NullPointerException e)
		{
			throw new BioPaxIOException(
					"Error processing individual "
					+ s + ". rdf:ID or rdf:about not found!", e);
		}

		if (this.getFactory().canInstantiate((this.getLevel().getInterfaceForName(s))))
		{

			if (!mergeDuplicates || (model.getByID(id))==null)
			{
					createBpe(s, id,model);
			}

		}
		else
		{

			if (r.hasText())
			{
				log.warn("Unknown class :" + r.getText());
			}
			else
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
		BioPAXElement bpe = this.getFactory().create(s, id);
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
		}
		else
		{
			id = base + id;
		}
		return id;
	}

	private void processProperty(Model model, String ownerID) throws XMLStreamException
	{
		if (r.getNamespaceURI().equals(rdfs) && r.getLocalName().equals("comment"))
		{
			BioPAXElement paxElement = model.getByID(ownerID);
			PropertyEditor commentor = getRDFCommentEditor(paxElement);
			r.next();
			assert r.getEventType() == CHARACTERS;
			String text = r.getText();
			commentor.setValueToBean(text, paxElement);
			log.warn("rdfs:comment is converted into the bp:comment; " +
			         "however, this can be overridden " +
			         "if there exists another bp:comment (element: " +
			         paxElement.getRDFId() + " text: " + text + ")");
			gotoEndElement();
		}
		else if (r.getNamespaceURI().equals(bp))
		{
			String property = r.getLocalName();
			String resource = r.getAttributeValue(rdf, "resource");
			if (resource != null)
			{
				if (resource.startsWith("#"))
				{
					resource = base + resource.substring(1, resource.length());

				}
				gotoEndElement();
			}
			else
			{
				r.next();
				boolean found = false;
				while (r.getEventType() != END_ELEMENT)
				{
					if (!found && r.getEventType() == CHARACTERS)
					{
						String text = r.getText();
						if (resource != null)
						{
							resource += text;
						}
						else
						{
							resource = text;
						}

						if (log.isTraceEnabled())
						{
							log.trace("text=" + text);
						}
					}
					else if (r.getEventType() == START_ELEMENT)
					{
						propertyContext = false;
						resource = processIndividual(model);
						found = true;
					}
					r.next();
				}
				resource = (!found && resource != null) ? resource.replaceAll("[\n\r\t ]+", " ") :
				           resource;
				if (resource != null)
				{
					resource = resource.trim();
				}
			}

			log.trace("setting = " + resource);
			triples.add(new Triple(ownerID, resource, property));
			propertyContext = false;
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
			StringBuffer sb = new StringBuffer();
			sb.append("domain: ");
			sb.append(domain);
			sb.append(", property: ");
			sb.append(property);
			sb.append(", range: ");
			sb.append(range);
			return sb.toString();
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
     * @param model model to be converted into OWL format
     * @param outputStream output stream into which the output will be written
     * @throws BioPaxIOException in case of I/O problems
     */
    public void convertToOWL(Model model, OutputStream outputStream) 
    {    	
        initializeExporter(model);

		try {
			Writer out = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
	        writeObjects(out, model);
	        out.close();
		} catch (IOException e) {
			throw new BioPaxIOException("Cannot convert to OWL!", e);
		}
    }

    
    /**
     * Similar to {@link #convertToOWL(Model, OutputStream)}, but 
     * extracts a sub-model, converts it into BioPAX (OWL) format, 
     * and writes it into the outputStream. 
     * Saved data can be then read via {@link BioPAXIOHandler}
     * interface (e.g., {@link SimpleIOHandler}).
     *
     * @param model model to be converted into OWL format
     * @param outputStream output stream into which the output will be written
     * @param ids the list of "root" element IDs to export (with all their properties/children altogether)
     * @throws IOException in case of I/O problems
     */
    public void convertToOWL(Model model, OutputStream outputStream, String... ids)
    {
		if (ids.length == 0) {
			convertToOWL(model, outputStream);
		}
		else {
			Model m = model.getLevel().getDefaultFactory().createModel();
			String base = model.getNameSpacePrefixMap().get("");
			m.getNameSpacePrefixMap().put("", base);
			//to avoid 'nextStep' that may lead to infinite loops -
			Filter<PropertyEditor> filter = new Filter<PropertyEditor>() {
				public boolean filter(PropertyEditor editor) {
					return !"nextStep".equalsIgnoreCase(editor.getProperty())
					 && !"NEXT-STEP".equalsIgnoreCase(editor.getProperty());
				}
			};
			Fetcher fetcher = new Fetcher(
					SimpleEditorMap.get(model.getLevel()), filter);
			
			for(String uri : ids) {
				BioPAXElement bpe = model.getByID(uri);
				if(bpe != null) {
					fetcher.fetch(bpe, m);
				}
			}
			
			convertToOWL(m, outputStream);
		} 
    }
    
    
    /**
     * Writes the XML representation of individual BioPAX element that 
     * is BioPAX-like but only for display or debug purpose (incomplete).
     * 
     * Note: use {@link #convertToOWL(Model, OutputStream)} instead 
     * if you have a model and want to save and later restore it.
     * 
     * @param out
     * @param bean
     * @throws IOException
     */
    public void writeObject(Writer out, BioPAXElement bean) throws IOException {
    	if(bp == null) bp = "bp";
		String name = bp + ":" + bean.getModelInterface().getSimpleName();
		writeIDLine(out, bean, name);
		
		Set<PropertyEditor> editors = editorMap.getEditorsOf(bean);
		if(editors==null || editors.isEmpty()) {
			log.info("no editors for " + bean.getRDFId() + " | " 
					+ bean.getModelInterface().getSimpleName());
			out.write(newline+"</" + name + ">");
			return;
		}
		
		for (PropertyEditor editor : editors) {
			Set value = editor.getValueFromBean(bean); //value is never null
			for (Object valueElement : value)
			{
				if(!editor.isUnknown(valueElement))
				writeStatementFor(bean, editor, valueElement, out);
			}
		}
		
		out.write(newline+"</" + name + ">");
	}
 
    
    private void writeObjects(Writer out, Model model) throws IOException
    {
    	writeHeader(out);
    	
        Set<BioPAXElement> bioPAXElements = model.getObjects();
        for (BioPAXElement bean : bioPAXElements)
        {
            writeObject(out, bean);
        }
        
        out.write(newline+"</rdf:RDF>");
    }


	private void writeStatementFor(BioPAXElement bean, PropertyEditor editor,
                                   Object value, Writer out)
            throws IOException
    {
        assert (bean != null && editor != null && value!=null);
        
        //fix (for L3 only): skip 'name' if it's present in the displayName, etc..
        if(editor.getProperty().equalsIgnoreCase("name") 
        		&& bean instanceof Named) { // the latter maybe not necessary...
        	Named named = (Named) bean;
        	if(value != null && 
        		(value.equals(named.getDisplayName())
        				|| value.equals(named.getStandardName()))) {
        		return;
        	}
        }
        
        String prop = bp + ":" + editor.getProperty();
        out.write(newline+" <" + prop);

        if (value instanceof BioPAXElement)
        {
            String id = ((BioPAXElement) value).getRDFId();
            assert id!=null;
            if (base!=null && id.startsWith(base))
            {
                //id = id.substring(id.lastIndexOf('#'));
            	id = '#' + id.substring(base.length());
            }
            out.write(" rdf:resource=\"" + id + "\" />");
        }
        else
        {
            String type = findLiteralType(editor);
            String valString = StringEscapeUtils.escapeXml(value.toString());
            out.write(" rdf:datatype = \"xsd:" + type + "\">" + valString +
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
        }
        else if (range.equals(double.class))
        {
            type = "double";
        }
        else if (range.equals(int.class))
        {
            type = "int";
        }
        else if (range.equals(float.class))
        {
            type = "float";
        }
        else if (range.equals(float.class))
        {
            type = "float";
        }
	    else if (range.equals(boolean.class))
	    {
	        type = "boolean";
	    }
        return type;
    }


    private void writeIDLine(Writer out, BioPAXElement bpe, String name)
            throws IOException
    {

        out.write(newline+newline+"<" + name + " ");
        String s = bpe.getRDFId();
        if (base != null && s.startsWith(base))
        {
            String id = s.substring(base.length());
            out.write(RDF_ID + id + close);
        }
        else
        {
            out.write(RDF_about + s + close);
        }


    }

    
    private void initializeExporter(Model model)
    {
        base = null;
        bp = null;
        namespaces = new HashMap<String, String>(model.getNameSpacePrefixMap());
        normalizeNameSpaces(); // - for this reader/exporter tool
        // also save the changes to the model?
        if(normalizeNameSpaces) {
	        model.getNameSpacePrefixMap().clear();
	        model.getNameSpacePrefixMap().putAll(namespaces);
        }
        
        level = model.getLevel();
        resetEditorMap();
    }

    
    private void normalizeNameSpaces() {
        String owlPre = null;
        String rdfPre = null;
        String xsdPre = null;

	    Map<String, String> reverseMap = new HashMap<String, String>();
        for (String pre : namespaces.keySet())
        {
            String ns = namespaces.get(pre);
            if (rdfNS.equalsIgnoreCase(ns)) {
                rdfPre = pre;
            }
            else if (owlNS.equalsIgnoreCase(ns)) {
                owlPre = pre;
            }
            else if (xsdNS.equalsIgnoreCase(ns)) {
                xsdPre = pre;
            }

            if(ns != null) {
            	reverseMap.put(ns, pre);
            }
        }
        
        if (owlPre != null) {
            reverseMap.remove(namespaces.get(owlPre));
            namespaces.remove(owlPre);
        }
        
        if (rdfPre != null) {
            reverseMap.remove(namespaces.get(rdfPre));
            namespaces.remove(rdfPre);
        }

        if (xsdPre != null) {
            reverseMap.remove(namespaces.get(xsdPre));
            namespaces.remove(xsdPre);
        }

        namespaces.put("rdf", rdfNS);
        namespaces.put("owl", owlNS);
        namespaces.put("xsd", xsdNS);
	}

    
	private void writeHeader(Writer out)
            throws IOException
    {
        //Header
        out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.write(newline+"<rdf:RDF");
        String bpns = this.editorMap.getLevel().getNameSpace();
        for (String pre : namespaces.keySet())
        {
            String ns = namespaces.get(pre);
            if (pre.equals(""))
            {
                base = ns;
            }
            else
            {
                if (ns.equalsIgnoreCase(bpns))
                {
                    bp = pre;
                }
	            else if(pre.equals("bp"))
                {
	                pre = "oldbp";
                }
                pre = ":" + pre;

            }
            out.write(newline+" xmlns" + pre + "=\"" + ns + "\"");
        }
        if (bp == null)
        {
            bp = "bp";
            out.write(newline+" xmlns:bp" + "=\"" + bpns + "\"");
        }
        if (base != null)
        {
            out.write(newline+" xml:base=\"" + base + "\"");
        }

        out.write(">");
        out.write(newline+"<owl:Ontology rdf:about=\"\">");
        out.write(newline+" <owl:imports rdf:resource=\""+bpns+"\" />");
        out.write(newline+"</owl:Ontology>");
    }
    
    
    public void normalizeNameSpaces(boolean normalizeNameSpaces) {
		this.normalizeNameSpaces = normalizeNameSpaces;
	}
}

