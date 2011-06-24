package org.biopax.paxtools.io;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.controller.EditorMap;
import org.biopax.paxtools.controller.ObjectPropertyEditor;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.controller.ReusedPEPHelper;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.deltaGprimeO;
import org.biopax.paxtools.model.level2.kPrime;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.util.BioPaxIOException;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;


/**
 * TODO:Class description User: demir Date: Jun 30, 2009 Time: 2:44:32 PM
 */
public abstract class BioPAXIOHandlerAdapter implements BioPAXIOHandler
{
	private boolean treatNilAsNull;

	private boolean convertingFromLevel1ToLevel2 = false;

	private boolean fixReusedPEPs = false;

	private static final Log log = LogFactory.getLog(BioPAXIOHandlerAdapter.class);

	protected BioPAXLevel level = BioPAXLevel.L3;

	protected BioPAXFactory factory;
	
	protected EditorMap editorMap;

	protected Map<String, String> namespaces;

	protected static final String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	protected static final String rdfs = "http://www.w3.org/2000/01/rdf-schema#";
	protected static String bp;
	protected static final String xsd = "http://www.w3.org/2001/XMLSchema#";
	protected static final String owl = "owl=http://www.w3.org/2002/07/owl#";


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

	protected void resetLevel(BioPAXLevel level, BioPAXFactory factory)
	{
		if (level != null)
		{
			this.level = level;
		}
		this.factory = (factory != null) ? factory : this.level.getDefaultFactory();

		// default flags
		if (this.level == BioPAXLevel.L1)
		{
			this.convertingFromLevel1ToLevel2 = true;
			this.fixReusedPEPs = true;
		}
		else if (this.level == BioPAXLevel.L2)
		{
			this.fixReusedPEPs = true;
		}

		bp = this.level.getNameSpace();
		
		resetEditorMap();
	}

	/**
	 * Updates the member EditorMap for the new BioPAX level and factory (different implementations of
	 * EditorMap can be used in modules, e.g. SimpleEditorMap and JenaEditorMap.)
	 *	 
	 */
	protected abstract void resetEditorMap();

	/**
	 * According the BioPAX documentation, it is illegal to reuse a Physical Entity Participant (PEP).
	 * If this value is set to <em>true</em> (default value), a reused PEP will be duplicated while
	 * converting the OWL file into a model.
	 *
	 * @see org.biopax.paxtools.controller.ReusedPEPHelper
	 */

	private ReusedPEPHelper reusedPEPHelper;

	/**
	 * Enables (true) or disables (false) the fixing of reused peps.
	 *
	 * @param fixReusedPEPs true if fixing is desired
	 * @see #fixReusedPEPs
	 */
	public void fixReusedPEPs(boolean fixReusedPEPs)
	{
		this.fixReusedPEPs = fixReusedPEPs;
	}

	public void treatNilAsNull(boolean treatNILasNull)
	{
		this.treatNilAsNull = treatNILasNull;
	}

	public void setConvertingFromLevel1ToLevel2(boolean convertingFromLevel1ToLevel2)
	{
		this.convertingFromLevel1ToLevel2 = convertingFromLevel1ToLevel2;
	}

	public boolean isTreatNilAsNull()
	{
		return treatNilAsNull;
	}

	public void setTreatNilAsNull(boolean treatNilAsNull)
	{
		this.treatNilAsNull = treatNilAsNull;
	}

	public boolean isConvertingFromLevel1ToLevel2()
	{
		return convertingFromLevel1ToLevel2;
	}

	public boolean isFixReusedPEPs()
	{
		return fixReusedPEPs;
	}

	public ReusedPEPHelper getReusedPEPHelper()
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
	 * This is experimental and is not not complete. Files have to be given in dependency order. i.e. a
	 * former file should not point to the latter. Use it at your own risk.
	 *
	 * @param files Dependency ordered biopax owl file names
	 * @return a merged model.
	 * @throws java.io.FileNotFoundException if any file can not be found
	 */

	public Model convertFromMultipleOwlFiles(String... files)
			throws FileNotFoundException
	{
		Model model = this.factory.createModel();

		for (String file : files)
		{
			FileInputStream in =
					new FileInputStream(new File(file));
			if (log.isDebugEnabled())
			{
				log.debug("start reading file:" + file);
			}
//			createAndBind(in, model);

			if (log.isDebugEnabled())
			{
				log.debug("read file: " + file);
			}
		}
		return model;
	}


	/**
	 * Reads a BioPAX model from an OWL file input stream (<em>in</em>) and converts it to a model.
	 *
	 * @param in inputStream from which the model will be read
	 * @return an empty model in case of invalid input.
	 */
	public Model convertFromOWL(InputStream in)
	{
		init(in);

		//cache the namespaces.
		namespaces = this.readNameSpaces();

		autodetectBiopaxLevel(); // this may update level, editorMap and factory!

		bp = level.getNameSpace();
		Model model = factory.createModel();
		model.getNameSpacePrefixMap().putAll(namespaces);

		boolean fixingPEPS = model.getLevel() == BioPAXLevel.L2 && this.isFixReusedPEPs();
		if (fixingPEPS)
		{
			reusedPEPHelper =
					new ReusedPEPHelper(model);
		}

		createAndBind(model);

		if (fixingPEPS)
		{
			this.getReusedPEPHelper().copyPEPFields();
		}

		return model;
	}


	private void autodetectBiopaxLevel()
	{
		BioPAXLevel filelevel = null;
		for (String namespaceValue : namespaces.values())
		{
			filelevel = BioPAXLevel.getLevelFromNameSpace(namespaceValue);
			if (filelevel != null)
			{
				if(log.isDebugEnabled())
					log.debug("Auto-detected biopax " + filelevel
					+ " (current settings are for Level " 
					+ level + ")");
				break;
			}
		}

		if (filelevel == null)
		{
			log.error("Cannot detect biopax level.");
			throw new BioPaxIOException("Cannot detect biopax level.");
		}
		else if (level != filelevel || filelevel != factory.getLevel())
		{
			if(log.isDebugEnabled())
				log.debug("Reset to the default factory for the detected BioPAX level.");
			resetLevel(filelevel, filelevel.getDefaultFactory());
		}
	}


	protected void createAndAdd(Model model, String id, String localName)
	{
		BioPAXElement bp = this.getFactory().create(localName, id);
		
		if (log.isTraceEnabled())
		{
			log.trace("id:" + id + " " + localName + " : " + bp);
		}
		/* null might occur here,
		 * so the following is to prevent the NullPointerException
		 * and to continue the model assembling.
		 */
		if (bp != null)
		{
			model.add(bp);
		}
	}

	protected abstract void init(InputStream in);

	protected abstract Map<String, String> readNameSpaces();

	protected abstract void createAndBind(Model model);

	protected BioPAXElement literalFixes(PropertyEditor editor,
	                                     BioPAXElement bpe, Model model, String value)
	{
		BioPAXElement created = null;

		if (this.isConvertingFromLevel1ToLevel2())
		{
			if (editor.getProperty().equals("DELTA-G"))
			{
				deltaGprimeO aDeltaGprime0 =
						model.addNew(deltaGprimeO.class,
								(bpe.getRDFId() +
								 "-DELTA-G"));
				aDeltaGprime0
						.setDELTA_G_PRIME_O(
								Float.valueOf(value));
				created = aDeltaGprime0;
			}
			if (editor.getProperty().equals("KEQ"))
			{
				kPrime aKPrime = model
						.addNew(kPrime.class, (bpe.getRDFId() + "-KEQ"));
				aKPrime.setK_PRIME(Float.valueOf(value));
				created = aKPrime;
			}

		}

		return created;
	}

	protected Object resourceFixes(BioPAXElement bpe, Object value)
	{
		if (this.isFixReusedPEPs() &&
		    value instanceof physicalEntityParticipant)
		{
			value = this.getReusedPEPHelper()
					.fixReusedPEP((physicalEntityParticipant) value, bpe);
		}
		return value;
	}

	protected void bindValue(String valueString, PropertyEditor editor,
	                         BioPAXElement bpe, Model model)
	{

		if (log.isDebugEnabled())
		{
			log.debug("Binding: " + bpe + '(' + bpe.getModelInterface() + " has  " + editor + ' ' +
			          valueString);
		}
		Object value = valueString;

		if (editor instanceof ObjectPropertyEditor)
		{
			value = model.getByID(valueString);
			value = resourceFixes(bpe, value);
			if (value == null)
			{
				value = literalFixes(editor, bpe, model, valueString);
				if (value == null)
				{
					throw new IllegalBioPAXArgumentException(
							"Illegal or Dangling Value/Reference: " + valueString
							+ " (element: " + bpe.getRDFId()
							+ " property: " + editor.getProperty()
							+ ")");
				}
			}
			else if (this.isTreatNilAsNull()
			         && valueString.trim().equalsIgnoreCase("NIL"))
			{
				value = null;
			}
		}
		if (editor == null)
		{
			log.error(
					"Editor is null. This probably means an invalid BioPAX property. Failed to set " +
					valueString);
		}
		else
		{
			editor.setValueToBean(value, bpe);
		}
	}

	protected PropertyEditor getRDFCommentEditor(BioPAXElement bpe)
	{
		PropertyEditor editor;
		Class<? extends BioPAXElement> inter = bpe.getModelInterface();
		if (this.getLevel().equals(BioPAXLevel.L3))
		{
			editor = this.getEditorMap().getEditorForProperty(
					"comment", inter);
		}
		else
		{
			editor = this.getEditorMap().getEditorForProperty(
					"COMMENT", inter);
		}
		return editor;
	}

}
