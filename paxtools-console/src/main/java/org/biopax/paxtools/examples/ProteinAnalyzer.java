package org.biopax.paxtools.examples;

import cpath.service.jaxb.ErrorResponse;
import cpath.service.jaxb.SearchHit;
import cpath.service.jaxb.SearchResponse;
import cpath.service.jaxb.ServiceResponse;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.io.pathwayCommons.PathwayCommons2Client;
import org.biopax.paxtools.io.sif.SimpleInteractionConverter;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.Xref;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * A program which takes two or more protein names from the command line and
 * queries/saves the neighborhood of each protein. It also queries the
 * paths between these proteins, converts the resulting model to SIF and
 * lists the publications that supports each interaction in the final model.
 * All queries are run via Pathway Commons 2 WEB Service and the results are
 * restricted to Homo sapiens proteins.
 */
public class ProteinAnalyzer
{
 protected final static String outputPath = "files/";

 public static void main(String[] arg) throws IOException
 {
  /* Expects two or more protein names */
  if (arg.length < 2)
  {
   System.err.println(
     "Usage: ProteinAnalyzer protein1 protein2 [protein3 [protein4 [...]]]");
   System.exit(-1);
  }

  // This IO Handler will be used to export pathways in BioPAX format
  SimpleIOHandler ioHandler = new SimpleIOHandler();

  // Create the Pathway Commons client and configure it
  PathwayCommons2Client pc2 = new PathwayCommons2Client();
  // Search only for Proteins
  pc2.setType("Protein");
  // Restrict results to H. sapiens
  pc2.getOrganisms().add("homo sapiens");
  // Expand the graph query limit to get more results
  pc2.setGraphQueryLimit(2);

  // General set to collect ids of all matching protein
  Set<String> allProteinIds = new HashSet<String>();

  for (String protein : arg)
  {
   // Search PC2 for the given protein name
   ServiceResponse serviceResponse = pc2.find(protein);
   if (serviceResponse instanceof ErrorResponse || serviceResponse.isEmpty())
   {
    System.err.println("No results for protein:" + protein);
    System.exit(-1);
   }

   // Collect all ids associated to this search
   SearchResponse searchResponse = (SearchResponse) serviceResponse;
   Set<String> ids = new HashSet<String>();
   for (SearchHit searchHit : searchResponse.getSearchHit())
   {
    ids.add(searchHit.getUri());
   }

   // Also add all these ids to overall set
   allProteinIds.addAll(ids);

   // Query the neighborhood of this protein
   Model proteinNeighborhood = pc2.getNeighborhood(ids);
   // And export the resultant network in BioPAX format
   FileOutputStream fileStream = new FileOutputStream(
     outputPath + protein + ".owl");
   ioHandler.convertToOWL(proteinNeighborhood, fileStream);
   fileStream.close();
  }

  // Query the paths between all given proteins
  Model pathsBtwModel = pc2.getPathsBetween(allProteinIds);
  // Save the model in BioPAX format
  FileOutputStream pathsBtwFile = new FileOutputStream(
    outputPath + "pathBetween.owl");
  ioHandler.convertToOWL(pathsBtwModel, pathsBtwFile);
  pathsBtwFile.close();

  // Also save the model in Simple Interaction Format with all default rules
  SimpleInteractionConverter sifConverter = new SimpleInteractionConverter(
    new org.biopax.paxtools.io.sif.level3.ComponentRule(),
    new org.biopax.paxtools.io.sif.level3.ConsecutiveCatalysisRule(),
    new org.biopax.paxtools.io.sif.level3.ControlRule(),
    new org.biopax.paxtools.io.sif.level3.ControlsTogetherRule(),
    new org.biopax.paxtools.io.sif.level3.ParticipatesRule());
  FileOutputStream pathsBtwSIFFile = new FileOutputStream(
    outputPath + "pathBetween.sif");
  sifConverter.writeInteractionsInSIF(pathsBtwModel, pathsBtwSIFFile);
  pathsBtwSIFFile.close();

  // Now, get all interactions in the models to extract publication information
  for (Interaction interaction : pathsBtwModel.getObjects(Interaction.class))
  {
   // Print the name of the interaction
   System.out.println("* " + interaction.getDisplayName());

   // Get all external references
   for (Xref xref : interaction.getXref())
   {
    String db = xref.getDb(),
      id = xref.getId(),
      url = "";

    // Convert PubMed ids to URLs for easy access
    if (db.equalsIgnoreCase("PubMed"))
     url = " (http://www.ncbi.nlm.nih.gov/pubmed/" + id + ")";

    // Print the external reference information
    System.out.println("\t" + xref.getDb() + ":" + xref.getId() + url);
   }
  }
 }
}