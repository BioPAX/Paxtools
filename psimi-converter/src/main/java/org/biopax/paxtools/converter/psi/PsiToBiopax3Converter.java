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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * The converter class. 
 * 1 - Unmarshalls PSI-MI or PSI-MITAB data.
 * 2 - Creates a set of EntryMapper threads, each of which is mapping a single PSI Entry object.
 * 3 - Creates a BioPAXMarshaller class to aggregate and marshall the data.
 *
 * @author Igor Rodchenkov (rodche)
 */
public class PsiToBiopax3Converter {

	private final String xmlBase; //common URI prefix

	/**
	 * Ref to boolean which indicates conversion is complete.
	 */
	protected boolean conversionIsComplete;

	/**
	 * Constructor.
	 * Will use the default empty string xml:base.
	 *
	 */
	public PsiToBiopax3Converter() {
		this.xmlBase = "";
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
	 * @param inputStream PSI-MI
	 * @param outputStream BioPAX
	 *
	 * @throws IOException
	 * @throws PsimiXmlReaderException
	 */
	public void convert(InputStream inputStream, OutputStream outputStream)
		throws IOException, PsimiXmlReaderException {

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
		convert(entrySet, outputStream);
	}

	
	/**
	 * Converts the PSI-MITAB inputStream into BioPAX outputStream.
	 * Streams will be closed by the converter.
	 *
	 * @param inputStream psimitab
	 * @param outputStream biopax
	 *
	 * @throws IOException
	 * @throws PsimiTabException
	 */
	public void convertTab(InputStream inputStream, OutputStream outputStream)
		throws IOException, PsimiTabException {

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
		convert(entrySet, outputStream);
	}	
	
	/**
	 * Converts the PSI interactions from the EntrySet and places into BioPAX output stream.
	 * Stream will be closed by the converter.
	 *
	 * @param entrySet
	 * @param outputStream
	 */
	public void convert(EntrySet entrySet, OutputStream outputStream) {

		// check args
		if (entrySet == null || outputStream == null) {
			throw new IllegalArgumentException("convert: one or more null arguments.");
		}
		if (entrySet.getLevel() != 2) {
			throw new IllegalArgumentException("convert: only PSI-MI Level 2.5 is supported.");
		}

		// create biopax marshaller
		final BioPAXMarshaller biopaxMarshaller = new BioPAXMarshaller(xmlBase, outputStream);

		ExecutorService exec = Executors.newCachedThreadPool();	
		
		// iterate through the list
		for (Entry entry : entrySet.getEntries()) {
			// create and start PSIMapper; use the same xml:base
			exec.execute(new EntryMapper(xmlBase, biopaxMarshaller, entry));
		}
		
		exec.shutdown(); //no more tasks
		// wait for marshalling to complete
		try {
			exec.awaitTermination(86400, TimeUnit.SECONDS); //a day, at most ;)
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted!", e);
		}
		
		//try to release some RAM earlier
		entrySet.getEntries().clear();
		entrySet = null;
		System.gc();
		
		biopaxMarshaller.marshallData();
	}

	/**
	 * @return the xmlBase
	 */
	public String getXmlBase() {
		return xmlBase;
	}
	
}
