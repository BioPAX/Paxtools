// $Id: PSIMIBioPAXConverter.java,v 1.2 2009/11/23 13:59:42 rodche Exp $
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
package org.mskcc.psibiopax.converter;

// imports
import org.biopax.paxtools.model.BioPAXLevel;

import psidev.psi.mi.xml.model.Entry;
import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.PsimiXmlReaderException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The converter class. 
 * 1 - Unmarshalls PSI data.
 * 2 - Creates a set of EntryMapper threads, each of which is mapping a single PSI Entry object.
 * 3 - Creates a BioPAXMarshaller class to aggregate and marshall the data.
 *
 * @author Benjamin Gross
 */
public class PSIMIBioPAXConverter implements PSIMIConverter {

	/**
	 * Ref to bp level
	 */
	private BioPAXLevel bpLevel;

	/**
	 * Ref to boolean which indicates conversion is complete.
	 */
	protected boolean conversionIsComplete;

	/**
	 * Constructor.
	 *
	 * @param bpLevel BioPAXLevel
	 */
	public PSIMIBioPAXConverter(BioPAXLevel bpLevel) {

		// set member vars
		this.bpLevel = bpLevel;
	}

	/**
	 * Converts the psi data in inputStream and places into outputStream.
	 * Streams will be closed by the converter.
	 *
	 * @param inputStream InputStream
	 * @param outputStream OutputStream
	 * @return boolean
	 *
	 * @throws IOException
	 * @throws PsimiXmlReaderException
	 */
	public boolean convert(InputStream inputStream, OutputStream outputStream)
		throws IOException, PsimiXmlReaderException {

		// check args
		if (inputStream == null || outputStream == null) {
			throw new IllegalArgumentException("One or more null arguments to PSIMIBioPAXConverter.convert()");
		}

		// unmarshall the data, close the stream
		PsimiXmlReader reader = new PsimiXmlReader();
		EntrySet entrySet = reader.read(inputStream);

		// convert
		boolean result = convert(entrySet, outputStream);

		// outta here
		inputStream.close();
		return result;
	}

	/**
	 * Converts the psi data in the EntrySet and places into outputstream.
	 * Stream will be closed by the converter.
	 *
	 * @param entrySet EntrySet
	 * @param outputStream OutputStream
	 * @return boolean
	 *
	 * @throws PsimiXmlReaderException
	 */
	public boolean convert(EntrySet entrySet, OutputStream outputStream) {

		// check args
		if (entrySet == null || outputStream == null) {
			throw new IllegalArgumentException("One or more null arguments to PSIMIBioPAXConverter.convert()");
		}
		if (entrySet.getLevel() != 2) {
			throw new IllegalArgumentException("Only PSI-MI Level 2.5 is supported.");
		}

		// init
		this.conversionIsComplete = false;

		// create, start a marshaller
		BioPAXMarshallerImp biopaxMarshaller =
			new BioPAXMarshallerImp(this, bpLevel, outputStream, entrySet.getEntries().size());
		biopaxMarshaller.start();

		// iterate through the list
		for (Entry entry : entrySet.getEntries()) {

			// create a biopax mapper
			BioPAXMapper bpMapper = new BioPAXMapperImp(bpLevel);
			bpMapper.setNamespace(EntryMapper.RDF_ID_PREFIX);

			// create and start PSIMapper
            (new EntryMapper(bpMapper, biopaxMarshaller, entry)).start();
		}

		// wait for marshalling to complete
		while (true) {

			// sleep for a bit
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e){
				e.printStackTrace();
				System.exit(1);
			}

            if (conversionIsComplete) {
                break;
            }
		}

		// outta here
		return true;
	}
}
