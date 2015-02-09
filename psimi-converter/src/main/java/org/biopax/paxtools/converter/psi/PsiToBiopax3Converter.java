// $Id: PsiToBiopax3Converter.java,v 1.2 2009/11/23 13:59:42 rodche Exp $
//------------------------------------------------------------------------------
/** Copyright (c) 2009 Memorial Sloan-Kettering Cancer Center.
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/
package org.biopax.paxtools.converter.psi;


import psidev.psi.mi.tab.PsimiTabException;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.converter.tab2xml.Tab2Xml;
import psidev.psi.mi.tab.converter.tab2xml.XmlConversionException;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.xml.model.Entry;
import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.PsimiXmlReaderException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;

/**
 * The PSIMI 2.5 to BioPAX Level3 converter. 
 *
 * @author Igor Rodchenkov (rodche)
 */
public class PsiToBiopax3Converter {

	private final String xmlBase; //common URI prefix

	/**
	 * Indicates whether conversion is complete.
	 */
	protected boolean conversionIsComplete;

	/**
	 * Constructor.
	 * Will use the default empty string xml:base.
	 *
	 */
	public PsiToBiopax3Converter() {
		this.xmlBase = null;
	}

	/**
	 * Constructor.
	 * 
	 * @param xmlBase
	 */
	public PsiToBiopax3Converter(String xmlBase) {
		this.xmlBase = xmlBase;
	}

	
	/**
	 * Converts the PSI-MI inputStream into BioPAX outputStream.
	 * Streams will be closed by the converter.
	 * 
	 * Note: for huge models (> 1 or 2 Gb data), and when a ByteArrayOutputStream 
	 * is used, the OutOfMemoryError will be thrown (increasing the "heap" RAM won't help;
	 * but using a FileOutputStream will do).
	 *
	 * @param inputStream PSI-MI
	 * @param outputStream BioPAX
	 * @param forceInteractionToComplex - always generate Complex instead of MolecularInteraction
	 *
	 * @throws IOException
	 * @throws PsimiXmlReaderException
	 */
	public void convert(InputStream inputStream, OutputStream outputStream, 
			boolean forceInteractionToComplex) throws IOException, PsimiXmlReaderException {

		// check args
		if (inputStream == null || outputStream == null) {
			throw new IllegalArgumentException("convert(): " +
					"one or more null arguments.");
		}

		// unmarshall the data, close the stream
		PsimiXmlReader reader = new PsimiXmlReader();
		EntrySet entrySet = reader.read(inputStream);
		inputStream.close();
		
		// convert
		convert(entrySet, outputStream, forceInteractionToComplex);
	}

	
	/**
	 * Converts the PSI-MITAB inputStream into BioPAX outputStream.
	 * Streams will be closed by the converter.
	 *
	 * @param inputStream psimitab
	 * @param outputStream biopax
	 * @param forceInteractionToComplex - always generate Complex instead of MolecularInteraction
	 *
	 * @throws IOException
	 * @throws PsimiTabException
	 */
	public void convertTab(InputStream inputStream, OutputStream outputStream, 
			boolean forceInteractionToComplex) throws IOException, PsimiTabException {

		// check args
		if (inputStream == null || outputStream == null) {
			throw new IllegalArgumentException("convertTab(): " +
					"one or more null arguments.");
		}

		// unmarshall the data, close the stream
		PsimiTabReader reader = new PsimiTabReader();
		Collection<BinaryInteraction> interactions = reader.read(inputStream);
		Tab2Xml tab2Xml = new Tab2Xml();
		EntrySet entrySet;
		try {
			entrySet = tab2Xml.convert(interactions);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (XmlConversionException e) {
			throw new RuntimeException(e);
		}
		inputStream.close();
		
		// convert
		convert(entrySet, outputStream, forceInteractionToComplex);
	}	
	
	/**
	 * Converts the PSI interactions from the EntrySet and places into BioPAX output stream.
	 * Stream will be closed by the converter.
	 *
	 * @param entrySet
	 * @param outputStream
	 * @param forceInteractionToComplex - always generate Complex instead of MolecularInteraction
	 */
	public void convert(EntrySet entrySet, OutputStream outputStream, 
			boolean forceInteractionToComplex) {

		// check args
		if (entrySet == null || outputStream == null) {
			throw new IllegalArgumentException("convert: one or more null arguments.");
		}
		if (entrySet.getLevel() != 2) {
			throw new IllegalArgumentException("convert: only PSI-MI Level 2.5 is supported.");
		}

		//create a new empty BioPAX Model
		final Model model = BioPAXLevel.L3.getDefaultFactory().createModel();
		model.setXmlBase(xmlBase);
		
		// convert all psimi entries
		EntryMapper entryMapper = new EntryMapper(model, forceInteractionToComplex);
		for (Entry entry : entrySet.getEntries()) {
			entryMapper.run(entry);
		}
		
		//try to release some RAM earlier
		entrySet.getEntries().clear();
		entrySet = null;
		
		// write BioPAX RDF/XML
		(new SimpleIOHandler()).convertToOWL(model, outputStream);
	}

	/**
	 * @return the xmlBase
	 */
	public String getXmlBase() {
		return xmlBase;
	}
	
}
