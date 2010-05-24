package org.biopax.paxtools.io.simpleIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.io.BioPAXIOHandlerAdapter;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.util.BioPaxIOException;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import javax.xml.stream.XMLInputFactory;

import static javax.xml.stream.XMLStreamConstants.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * TODO:Class description User: demir Date: Jun 30, 2009 Time: 2:36:56 PM
 */
public class SimpleReader extends BioPAXIOHandlerAdapter
{

	private static final Log log = LogFactory.getLog(SimpleReader.class);

	private XMLStreamReader r;
	boolean propertyContext = false;
	private String base = "";
	private List<Triple> triples;
	private boolean mergeDuplicates;

	// --------------------------- CONSTRUCTORS ---------------------------

	public SimpleReader()
	{
		this(null, null);
	}

	public SimpleReader(BioPAXLevel level)
	{
		this(level.getDefaultFactory(), level);
	}

	public SimpleReader(BioPAXFactory factory, BioPAXLevel level)
	{
		super(factory, level);
		resetEditorMap();
		log.info("new!!--------------------!!!---------------");
	}

	public void mergeDuplicates(boolean mergeDuplicates)
	{
		this.mergeDuplicates = mergeDuplicates;
	}
	// -------------------------- OTHER METHODS --------------------------	

	@Override
	final protected void resetEditorMap()
	{
		setEditorMap(new SimpleEditorMap(this.getLevel())); // was 'level' - bug!
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
							if (this.getFactory().canInstantiate(r.getLocalName()))
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
							sb.append(r.getLocalName());
							if (r.hasText())
							{
								sb.append(r.getText());
							}
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
				this.getEditorMap().getEditorForProperty(triple.property, domain.getClass());

		/* Check for NULL now, and raise the exception; otherwise, the next call (bindValue)
		 * will throw the NullPointerException, anyway, which is difficult to interpret
		 * (e.g. in the Validator).
		 */
		if (editor == null)
		{
		}

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

		if (this.getFactory().canInstantiate(s))
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
		BioPAXElement bpe = this.getFactory().reflectivelyCreate(s);
		bpe.setRDFId(id);
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
			commentor.setPropertyToBean(paxElement, text);
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

	public void convertToOWL(Model model, OutputStream outputStream)
	{
		throw new UnsupportedOperationException();
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

}

