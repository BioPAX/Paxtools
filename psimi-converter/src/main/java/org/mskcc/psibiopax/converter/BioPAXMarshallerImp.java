// $Id: BioPAXMarshallerImp.java,v 1.1 2009/11/22 15:50:28 rodche Exp $
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


import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * After each EntryProcessor thread is finished, 
 * combines the set of generated Paxtools models
 * into a single Model for marshalling.
 *
 * @author Benjamin Gross, rodche (re-factoring)
 */
class BioPAXMarshallerImp implements BioPAXMarshaller {
	
    /**
	 * Ref to PSIMIBioPAXConverter.
	 */
	private final PSIMIBioPAXConverter converter;

	/**
	 * Ref to file output stream.
	 */
	private OutputStream outputStream;

	/**
	 * Our list of BioPAXContainers.
	 */
	private List<Model> bpModelList;

	/**
	 * Constructor.
	 *
	 * @param converter PSIMIBioPAXConverter
	 * @param outputStream OutputStream - will be closed by this class 
	 */
	public BioPAXMarshallerImp(PSIMIBioPAXConverter converter, OutputStream outputStream) {
		this.converter = converter;
		this.bpModelList = new ArrayList<Model>();
		this.outputStream = outputStream;
	}

	/**
	 * Our implementation of BioPAXMarshaller interface.
	 *
	 * @param bpModel Model
	 */
	public void addModel(Model bpModel) {
		bpModelList.add(bpModel);
	}


	@Override
	public void marshallData() {
		// combine all models into a single model
		Model completeModel = converter.getBpLevel().getDefaultFactory().createModel();
		completeModel.setXmlBase(converter.getXmlBase());
		
		// we suppose there are no any URI clashes between elements 
		// from different models (true if URI generator was fair/reliable...);
		// otherwise this throws a paxtools exception: "already have element with the same ID"
		for (Model bpModel : bpModelList) {
			Set<BioPAXElement> elementList = bpModel.getObjects();
			for (BioPAXElement elementInstance : elementList) {
				completeModel.add(elementInstance);
			}
		}

		// write out the file
		try {
			BioPAXIOHandler io = new SimpleIOHandler();
			io.convertToOWL(completeModel, outputStream);
			outputStream.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
