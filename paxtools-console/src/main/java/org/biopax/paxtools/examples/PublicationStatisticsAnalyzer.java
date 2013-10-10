package org.biopax.paxtools.examples;

import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.PublicationXref;

import java.io.FileNotFoundException;

/**
 * A simple method that prints the publication statistics.
 *
 */
public class PublicationStatisticsAnalyzer
{
	public static void main(String[] args) throws FileNotFoundException
	{
		if(args.length != 2) {
			System.out.println("\nUse Parameters: " +
			                   "biopaxFile pathwayFullRdfId\n");
			System.exit(-1);
		}

		Model model = Macros.open(args[0]);
		int size = model.getObjects(PublicationXref.class).size();

	}
}
