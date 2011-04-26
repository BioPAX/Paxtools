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

// imports

import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This class waits for each EntryProcessor thread to finish.
 * After thread(s) completion, combines set of Paxerve Models
 * into single Model for marshalling.
 *
 * @author Benjamin Gross
 */
public class BioPAXMarshallerImp extends Thread implements BioPAXMarshaller {

    /**
	 * Ref to PSIMIBioPAXConverter.
	 */
	private PSIMIBioPAXConverter converter;

	/**
	 * Ref to biopax level.
	 */
	private BioPAXLevel bpLevel;

	/**
	 * Used for synchronization.
	 */
	private final Object synObj;

	/**
	 * numEntries complete.
	 */
	private int numEntries;

	/*
	 * number of entries completed.
	 */
	private int entriesComplete;

	/**
	 * Ref to file output stream.
	 */
	private OutputStream outputStream;

	/**
	 * Our list of BioPAXContainers.
	 */
	private List<Model> bpModelList;

	/*
	 * Constructor.
	 *
	 * @param converter PSIMIBioPAXConverter
	 * @parma bpLevel BioPAXLevel 
	 * @param outputStream OutputStream - will be closed by this class
	 * @param numEntries int
	 */
	public BioPAXMarshallerImp(PSIMIBioPAXConverter converter, BioPAXLevel bpLevel, OutputStream outputStream, int numEntries) {

		this.converter = converter;
		this.bpLevel = bpLevel;
		this.numEntries = numEntries;
		this.synObj = new Object();
		this.bpModelList = new ArrayList<Model>();
		this.outputStream = outputStream;
	}

	/**
	 * Our implementation of BioPAXMarshaller interface.
	 *
	 * @param bpModel Model
	 */
	public void addModel(Model bpModel) {

		// add model to our list,
		// increment number of entries
		synchronized (synObj) {
			++entriesComplete;
			bpModelList.add(bpModel);
		}
	}

	/**
	 * Our implementation of run.
	 */
	public void run() {

		// loop until all entries are processed
		while (true) {

			// have all the entries completed ?
			synchronized(synObj) {
				if (entriesComplete == numEntries) {
					break;
				}
			}

			// sleep for a bit
			try {
				sleep(100);
			}
			catch (InterruptedException e){
				e.printStackTrace();
				break;
			}
		}

		// marshall bpcontainer list into owl
		marshallData();
	}

	/**
	 * Combines set of Model(s) into single
	 * paxerve Model.
	 */
	private void marshallData() {

		// combine all models into a single model
		Model completeModel = null;
		if (bpLevel == BioPAXLevel.L2) {
			completeModel = BioPAXLevel.L2.getDefaultFactory().createModel();
		}
		else if (bpLevel == BioPAXLevel.L3) {
			completeModel = BioPAXLevel.L3.getDefaultFactory().createModel();
		}

		completeModel.getNameSpacePrefixMap().put("", EntryMapper.RDF_ID_PREFIX);
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
			this.converter.conversionIsComplete = true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
