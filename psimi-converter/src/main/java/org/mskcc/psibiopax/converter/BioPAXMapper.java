// $Id: BioPAXMapper.java,v 1.2 2009/11/23 13:59:42 rodche Exp $
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

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;

import java.util.List;
import java.util.Set;

/**
 * An interface which provides methods to query a Paxtools model.
 */
public interface BioPAXMapper {

	/**
	 * Gets the biopax model.
	 *
	 * @return Model
	 */
	Model getModel();

	/**
	 * Gets a unification xref.
	 *
	 * @param id String
	 * @return <T extends BioPAXElement>
	 */
	<T extends BioPAXElement> T getUnificationXref(String id);

	/**
	 * Gets a relationship xref.
	 *
	 * @param id String
	 * @param refType String
	 * @param refTypeID String
	 * @return <T extends BioPAXElement>
	 */
	<T extends BioPAXElement> T getRelationshipXref(String id, String refType, String refTypeID);

	/**
	 * Gets a publication xref.
	 *
	 * @param id String
	 * @return <T extends BioPAXElement>
	 */
	<T extends BioPAXElement> T getPublicationXref(String id);

	/**
	 * Gets an evidence object.
	 *
	 * @param id String
	 * @param bpXrefs Set<? extends BioPAXElement>
	 * @param evidenceCodes Set<? extends BioPAXElement>
	 * @param scoresOrConfidences Set<? extends BioPAXElement>
	 * @param comments Set<String>
	 * @param experimentalForms Set<BioPAXElement>
	 * @return <T extends BioPAXElement>
	 */
	<T extends BioPAXElement> T getEvidence(String id,
											Set<? extends BioPAXElement> bpXrefs,
											Set<? extends BioPAXElement> evidenceCodes,
											Set<? extends BioPAXElement> scoresOrConfidence,
											Set<String> comments,
											Set<? extends BioPAXElement> experimentalForms);

	/**
	 * Gets a confidence/score object.
	 *
	 * @param id String
	 * @param value String
	 * @param bpXrefs Set<? extends BioPAXElement>
	 * @param comments Set<String>
	 * @return <T extends BioPAXElement>
	 */
	<T extends BioPAXElement> T getScoreOrConfidence(String id,
													 String value,
													 Set<? extends BioPAXElement> bpXrefs,
													 Set<String> comments);

	/**
	 * Gets a experimental form object.
	 *
	 * @param id String
	 * @param formType BioPAXElement
	 * @param participant BioPAXElement
	 * @return <T extends BioPAXElement>
	 */
	<T extends BioPAXElement> T getExperimentalForm(String id,
													BioPAXElement formType,
													BioPAXElement participant);
													 

	/**
	 * Gets an existing open/controlled vocabulary object.
	 *
	 * @param termToSearch String
	 * @return <T extends BioPAXElement>
	 */
	<T extends BioPAXElement> T getOpenControlledVocabulary(String termToSearch);

	/**
	 * Gets a open/controlled vocabulary object.
	 *
	 * @param id String
	 * @param term String
	 * @param bpXrefs Set<? extends BioPAXElement>
	 * @return <T extends BioPAXElement>
	 */
	<T extends BioPAXElement> T getOpenControlledVocabulary(String id, String term,
															Set<? extends BioPAXElement> bpXrefs);

	/**
	 * Given an RDF ID, returns a matching model element
	 *
	 * @param rdfID
	 * @return BioPAXElement
	 */
	BioPAXElement getBioPAXElement(String rdfID);

	/**
	 * Gets an interaction.
	 *
	 * @param id String
	 * @param name String
	 * @param shortName String
	 * @param availability Set<String>
	 * @param participants Set<? extends BioPAXElement>
	 * @param bpEvidence Set<? extends BioPAXElement>
	 * @return <T extends BioPAXElement>
	 */
	<T extends BioPAXElement> T getInteraction(String id,
											   String name, String shortName,
											   Set<String> availability,
											   Set<? extends BioPAXElement> participants,
											   Set<? extends BioPAXElement> bpEvidence);

	/**
	 * Gets a participant.
	 *
	 * @param id String
	 * @param features <? extends BioPAXElement>
	 * @param cellularLocation BioPAXElement
	 * @param bpPhysicalEntity BioPAXElement
	 * @return <T extends BioPAXElement>
	 */
	<T extends BioPAXElement> T getParticipant(String id,
											   Set<? extends BioPAXElement> features,
											   BioPAXElement cellularLocation,
											   BioPAXElement bpPhysicalEntity);

	/**
	 * Gets a physical Entity.
	 *
	 * @param physicalEntityType String
	 * @param id String
	 * @param name String
	 * @param shortName String
	 * @param synonyms Set<String>
	 * @param bpXrefs Set<? extends BioPAXElement>
	 * @parma entityRefId String
	 * @param bioSource BioPAXElement
	 * @param sequence String
	 * @return <T extends BioPAXElement>
	 */
	<T extends BioPAXElement> T getPhysicalEntity(String physicalEntityType, String id,
												  String name, String shortName,
												  Set<String> synonyms,
												  Set<? extends BioPAXElement> bpXrefs,
												  String entityRefId,
												  BioPAXElement bioSource,
												  String sequence);

	/**
	 * Gets a biosource.
	 *
	 * @param id String
	 * @param taxonXref BioPAXElement
	 * @param cellType BioPAXElement
	 * @param tissue BioPAXElement
	 * @param name String
	 * @return <T extends BioPAXElement>
	 */
	<T extends BioPAXElement> T getBioSource(String id, BioPAXElement taxonXref,
											 BioPAXElement cellType, BioPAXElement tissue, String name);

	/**
	 * Used to add feature attributes to given sequence or entity feature.
	 *
	 * @param bpSequenceFeature BioPAXElement
	 * @param bpXrefs Set<? extends BioPAXElement>
	 * @param featureLocations Set<? extends BioPAXElement>
	 * @param featureType BioPAXElement
	 * @return <T extends BioPAXElement>
	 */
	<T extends BioPAXElement> T getFeature(BioPAXElement bpFeature,
										   Set<? extends BioPAXElement> bpXrefs,
										   Set<? extends BioPAXElement> featureLocations,
										   BioPAXElement featureType);

	/**
	 * Gets a sequence or entity feature.
	 *
	 * @param id String
	 * @param bpXrefs Set<? extends BioPAXElement>
	 * @param featureLocations Set<? extends BioPAXElement>
	 * @param featureType BioPAXElement
	 * @return <T extends BioPAXElement>
	 */
	<T extends BioPAXElement> T getFeature(String id,
										   Set<? extends BioPAXElement> bpXrefs,
										   Set<? extends BioPAXElement> featureLocations,
										   BioPAXElement featureType);

	/**
	 * Gets a sequence location.
	 *
	 * @param seqLocationID String
	 * @param beginSeqSiteID String
	 * @parma endSeqSiteID String
	 * @param beginSequenceInterval long
	 * @param endSequenceInterval long
	 * @return <T extends BioPAXElement>
	 */
	<T extends BioPAXElement> T getSequenceLocation(String seqLocationID,
													String beginSeqSiteID, String endSeqSiteID,
													long beginSequenceInterval,
													long endSequenceInterval);

	/**
	 * Given an xref (BioPAXElement) returns its id.
	 *
	 * @param bpXref BioPAXElement
	 * @return String
	 */
	String getXrefID(BioPAXElement bpXref);


	/**
	 * Sets given xref's db and id.
	 *
	 * @param xrefType BioPAXElement
	 * @param db String
	 * @param id String
	 */
	void setXrefDBAndID(BioPAXElement bpXref, String db, String id);

	/**
	 * Sets biopax model namespace.
	 *
	 * @param namespace String
	 */
	void setNamespace(String namespace);

	/**
	 * Creates a data source on the model.
	 *
	 * @param id String
	 * @param name String
	 * @param xrefs Set<BioPAXElement>
	 */
	void setModelDataSource(String id, String name, Set<? extends BioPAXElement> xrefs);

	/**
	 * Creates a data source and adds to given interaction.
	 *
	 * @parma interaction Object
	 * @param id String
	 * @param name String
	 * @param bpXrefs Set<BioPAXElement>
	 */
	<T extends BioPAXElement> void setInteractionDataSource(T interaction, String id, String name, Set<? extends BioPAXElement> bpXrefs);



	/**
	 * Given a set of evidence objects, determines if interaction (that evidence obj is derived from)
	 * is a genetic interaction.
	 *
	 * @param geneticInteractionTerms List<String>
     * @param bpEvidence Set<? extends BioPAXElement>
	 * @return boolean
	 */
	boolean isGeneticInteraction(final List<String> geneticInteractionTerms,
								 Set<? extends BioPAXElement> bpEvidence);
	
}
