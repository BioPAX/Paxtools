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
import generated.psimil2.*; // this is to be auto-generated by the maven jaxb2 plugin and put in the target/generated-sources dir (default).
import org.biopax.paxtools.model.BioPAXLevel;

import java.util.List;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * The converter class. 
 * 1 - Unmarshalls PSI data.
 * 2 - Creates a set of EntryMapper threads, each of which is mapping a single PSI Entry object.
 * 3 - Creates a BioPAXMarshaller class to aggregate and marshall the data.
 *
 * @author Benjamin Gross
 */
public class PSIMIBioPAXConverter {

	/**
	 * PSI L2 generated package
	 */
	private static final String PSI_MI_L2_GENERATED_PACKAGE = "generated.psimil2";

	/**
	 * Ref to bp level
	 */
	private BioPAXLevel bpLevel;

	/**
	 * Ref to unmarshaller.
	 */
	private Unmarshaller unmarshaller;

	/**
	 * Constructor.
	 *
	 * @param bpLevel BioPAXLevel
	 * @throws JAXBException
	 * @throws IllegalArgumentException
	 */
	public PSIMIBioPAXConverter(BioPAXLevel bpLevel) throws JAXBException, IllegalArgumentException {

		// set member vars
		this.bpLevel = bpLevel;
		JAXBContext context = JAXBContext.newInstance(PSI_MI_L2_GENERATED_PACKAGE);
		unmarshaller = context.createUnmarshaller();
	}

	/**
	 * Converts the psi data in inputStream and places into outputStream.
	 * Streams will be closed by the converter.
	 *
	 * @throws IOException
	 * @throws JAXBException
	 * @throws IllegalArgumentException
	 */
	public boolean convert(InputStream inputStream, OutputStream outputStream)
		throws IOException, JAXBException, IllegalArgumentException {

		// check args
		if (inputStream == null || outputStream == null) {
			throw new IllegalArgumentException("One or more null arguments to PSIMIBioPAXConverter.convert()");
		}

		// unmarshall the data, close the stream
		EntrySet es = (EntrySet)unmarshaller.unmarshal(inputStream);

		// get entry list
		List<EntrySet.Entry> entries =  es.getEntry();

		// create, start a marshaller
		BioPAXMarshallerImp biopaxMarshaller = new BioPAXMarshallerImp(bpLevel, outputStream, entries.size());
		biopaxMarshaller.start();

		// iterate through the list
		for (EntrySet.Entry entry : entries) {

			// create a biopax mapper
			BioPAXMapper bpMapper = new BioPAXMapperImp(bpLevel);
			bpMapper.setNamespace(EntryMapper.RDF_ID_PREFIX);

			// create and start PSIMapper
			(new EntryMapper(bpMapper, biopaxMarshaller, entry)).start();
		}

		// outta here
		inputStream.close();
		return true;
	}
}
