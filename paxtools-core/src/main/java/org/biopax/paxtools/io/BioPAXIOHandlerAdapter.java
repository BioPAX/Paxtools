package org.biopax.paxtools.io;

import org.biopax.paxtools.controller.*;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.deltaGprimeO;
import org.biopax.paxtools.model.level2.kPrime;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.util.BioPaxIOException;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;


public abstract class BioPAXIOHandlerAdapter implements BioPAXIOHandler
{

	private boolean fixReusedPEPs = false;

	private static final Logger log = LoggerFactory.getLogger(BioPAXIOHandlerAdapter.class);

	protected BioPAXLevel level;

	protected BioPAXFactory factory;

	protected EditorMap editorMap;

	protected Map<String, String> namespaces;

	protected static final String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	protected static final String rdfs = "http://www.w3.org/2000/01/rdf-schema#";

	protected String bp; //current BioPAX namespace prefix value

	protected static final String xsd = "http://www.w3.org/2001/XMLSchema#";

	protected static final String owl = "owl=http://www.w3.org/2002/07/owl#";

	protected String base;

	public BioPAXIOHandlerAdapter()
	{
		this(null, null);
	}

	public BioPAXIOHandlerAdapter(BioPAXLevel level)
	{
		this(null, level);
	}

	public BioPAXIOHandlerAdapter(BioPAXFactory factory, BioPAXLevel level)
	{
		resetLevel(level, factory);
	}

	/**
	 * Updates the level and factory for this I/O
	 * (final - because used in the constructor)
	 * @param level BioPAX Level
	 * @param factory concrete BioPAX factory impl.
	 */
	protected final void resetLevel(BioPAXLevel level, BioPAXFactory factory)
	{
		this.level = (level != null) ? level : BioPAXLevel.L3;
		this.factory = (factory != null) ? factory : this.level.getDefaultFactory();

		// default flags
		if (this.level == BioPAXLevel.L2)
		{
			this.fixReusedPEPs = true;
		}

		bp = this.level.getNameSpace();

		resetEditorMap(); //implemented by concrete subclasses
	}

	/**
	 * Updates the member EditorMap for the new BioPAX level and factory (different implementations of
	 * EditorMap can be used in modules, e.g. SimpleEditorMap and JenaEditorMap.)
	 */
	protected abstract void resetEditorMap();

	/**
	 * According the BioPAX documentation, it is illegal to reuse a Physical Entity Participant (PEP).
	 * If this value is set to <em>true</em> (default value), a reused PEP will be duplicated while
	 * converting the OWL file into a model.
	 * @see org.biopax.paxtools.controller.ReusedPEPHelper
	 */

	private ReusedPEPHelper reusedPEPHelper;

	/**
	 * Enables (true) or disables (false) the fixing of reused peps.
	 * @param fixReusedPEPs true if fixing is desired
	 * @see #fixReusedPEPs
	 */
	public void fixReusedPEPs(boolean fixReusedPEPs)
	{
		this.fixReusedPEPs = fixReusedPEPs;
	}

	/**
	 * Workaround for a very common Level 2 issue. Most level2 exports in the past reused PhysicalEntityParticipants
	 * as if they represent states. This is problematic because PEPs also contain stoichiometry information which is
	 * specific to a reaction. BioPAX spec says that PEPs should not be reused across reactions.
	 *
	 * As Level2 exports getting obsolete this method is slated for deprecation.
	 * @return true if this Handler automatically splits reused PEPs to interaction specific PEPS.
	 */
	public boolean isFixReusedPEPs()
	{
		return fixReusedPEPs;
	}

	/**
	 * This is a helper class initialized only if fixReusedPEPs is true.
	 * @return helper object
	 */
	protected ReusedPEPHelper getReusedPEPHelper()
	{
		return reusedPEPHelper;
	}


	public BioPAXFactory getFactory()
	{
		return factory;
	}


	public void setFactory(BioPAXFactory factory)
	{
		this.factory = factory;
	}


	public EditorMap getEditorMap()
	{
		return editorMap;
	}

	public void setEditorMap(EditorMap editorMap)
	{
		this.editorMap = editorMap;
	}

	public BioPAXLevel getLevel()
	{
		return level;
	}

	/**
	 * Reads a BioPAX model from an OWL file input stream (<em>in</em>) and converts it to a model.
	 * @param in inputStream from which the model will be read
	 * @return an empty model in case of invalid input.
	 */
	public Model convertFromOWL(InputStream in)
	{
		init(in);

		//cache the namespaces.
		namespaces = this.readNameSpaces();

		autodetectBiopaxLevel(); // this may update level, editorMap and factory!

//		bp = level.getNameSpace();

		Model model = factory.createModel();

		model.getNameSpacePrefixMap().putAll(namespaces);

		model.setXmlBase(base);

		boolean fixingPEPS = model.getLevel() == BioPAXLevel.L2 && this.isFixReusedPEPs();
		if (fixingPEPS)
		{
			reusedPEPHelper = new ReusedPEPHelper(model);
		}

		createAndBind(model);

		if (fixingPEPS)
		{
			this.getReusedPEPHelper().copyPEPFields();
		}
		
		reset(in);

		return model;
	}

	protected void reset(InputStream in)
	{
		try
		{
			in.close();

		}
		catch (IOException e)
		{
			throw new RuntimeException("Failed to close the file");
		}

	}

	private void autodetectBiopaxLevel()
	{
		BioPAXLevel filelevel = null;
		for (String namespaceValue : namespaces.values())
		{
			filelevel = BioPAXLevel.getLevelFromNameSpace(namespaceValue);
			if (filelevel != null)
			{
				if (log.isDebugEnabled())
					log.debug("Auto-detected biopax " + filelevel + " (current settings are for Level " + level + ")");
				break;
			}
		}

		if (filelevel == null)
		{
			log.error("Cannot detect biopax level.");
			throw new BioPaxIOException("Cannot detect biopax level.");
		} else if (level != filelevel)
		{
			if (log.isDebugEnabled()) log.debug("Reset to the default factory for the detected BioPAX level.");
			resetLevel(filelevel, filelevel.getDefaultFactory());
		}
	}

	/**
	 * This method is called by the reader for each OWL instance in the OWL model. It creates a POJO instance, with
	 * the given id and inserts it into the model. The inserted object is "clean" in the sense that its properties are
	 * not set yet.
	 *
	 * Implementers of this abstract class can override this method to inject code during object creation.
	 * @param model to be inserted
	 * @param id of the new object. The model should not contain another object with the same ID.
	 * @param localName of the class to be instantiated.
	 */
	protected void createAndAdd(Model model, String id, String localName)
	{
		BioPAXElement bpe = this.getFactory().create(localName, id);

		if (log.isTraceEnabled())
		{
			log.trace("id:" + id + " " + localName + " : " + bpe);
		}
		/* null might occur here,
		 * so the following is to prevent the NullPointerException
		 * and to continue the model assembling.
		 */
		if (bpe != null)
		{
			model.add(bpe);
		} else
		{
			log.warn("null object created during reading. It might not be an official BioPAX class.ID: " + id +
			         " Class " +
			         "name " + localName);
		}
	}

	/**
	 * This method provides a hook for the implementers of this abstract class to perform the initial reading from the
	 * input stream.
	 *
	 * @param in BioPAX RDF/XML input stream
	 */
	protected abstract void init(InputStream in);

	/**
	 * This method provides a hook for the implementers of this abstract class to set the namespaces of the model.
	 * @return a map of namespaces.
	 */
	protected abstract Map<String, String> readNameSpaces();

	/**
	 * This method provides a hook for the implementers of this abstract class to create objects themselves and bind
	 * the properties to the objects.
	 * @param model to be populated
	 */
	protected abstract void createAndBind(Model model);

	/**
	 * This method currently only fixes reusedPEPs if the option is set. As L2 is becoming obsolete this method will be
	 * slated for deprecation.
	 * @param bpe to be bound
	 * @param value to be assigned.
	 * @return a "fixed" value.
	 */
	protected Object resourceFixes(BioPAXElement bpe, Object value)
	{
		if (this.isFixReusedPEPs() && value instanceof physicalEntityParticipant)
		{
			value = this.getReusedPEPHelper().fixReusedPEP((physicalEntityParticipant) value, bpe);
		}
		return value;
	}

	/**
	 * This method binds the value to the bpe. Actual assignment is handled by the editor - but this method performs
	 * most of the workarounds and also error handling due to invalid parameters.
	 * @param valueString to be assigned
	 * @param editor that maps to the property
	 * @param bpe to be bound
	 * @param model to be populated.
	 */
	protected void bindValue(String valueString, PropertyEditor editor, BioPAXElement bpe, Model model)
	{
		if (log.isDebugEnabled())
		{
			log.debug("Binding: " + bpe + '(' + bpe.getModelInterface() + " has  " + editor + ' ' + valueString);
		}
		Object value = valueString;

		if (editor instanceof ObjectPropertyEditor)
		{
			value = model.getByID(valueString);
			value = resourceFixes(bpe, value);
			if (value == null)
			{
				throw new IllegalBioPAXArgumentException(
					"Illegal or Dangling Value/Reference: " + valueString + " (element: " + bpe.getUri() +
					" property: " + editor.getProperty() + ")");
			}
		}
		if (editor == null)
		{
			log.error("Editor is null. This probably means an invalid BioPAX property. Failed to set " + valueString);
		} else
		{
			editor.setValueToBean(value, bpe);
		}
	}

	/**
	 * Paxtools maps BioPAX:comment (L3) and BioPAX:COMMENT (L2) to rdf:comment. This method handles that.
	 * @param bpe to be bound.
	 * @return a property editor responsible for editing comments.
	 */
	protected StringPropertyEditor getRDFCommentEditor(BioPAXElement bpe)
	{
		StringPropertyEditor editor;
		Class<? extends BioPAXElement> inter = bpe.getModelInterface();
		if (this.getLevel().equals(BioPAXLevel.L3))
		{
			editor = (StringPropertyEditor) this.getEditorMap().getEditorForProperty("comment", inter);
		} else
		{
			editor = (StringPropertyEditor) this.getEditorMap().getEditorForProperty("COMMENT", inter);
		}
		return editor;
	}

	/**
	 * Similar to {@link BioPAXIOHandler#convertToOWL(org.biopax.paxtools.model.Model,
	 * java.io.OutputStream)} (org.biopax.paxtools.model.Model, Object)}, but
	 * extracts a sub-model, converts it into BioPAX (OWL) format,
	 * and writes it into the outputStream.
	 * Saved data can be then read via {@link BioPAXIOHandler}
	 * interface (e.g., {@link SimpleIOHandler}).
	 * @param model model to be converted into OWL format
	 * @param outputStream output stream into which the output will be written
	 * @param ids optional list of "root" element absolute URIs;
	 *            direct/indirect child objects are auto-exported as well.
	 */
	public void convertToOWL(Model model, OutputStream outputStream, String... ids)
	{
		if (ids.length == 0)
		{
			convertToOWL(model, outputStream);
		} else
		{
			Model m = model.getLevel().getDefaultFactory().createModel();
			m.setXmlBase(model.getXmlBase());

			Fetcher fetcher = new Fetcher(SimpleEditorMap.get(model.getLevel())); //no Filters anymore

			for (String uri : ids)
			{
				BioPAXElement bpe = model.getByID(uri);
				if (bpe != null)
				{
					fetcher.fetch(bpe, m);
				}
			}

			convertToOWL(m, outputStream);
		}
	}
}
