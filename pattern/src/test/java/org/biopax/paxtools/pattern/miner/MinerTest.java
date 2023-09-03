package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.constraint.ConBox;
import org.biopax.paxtools.pattern.constraint.Type;
import org.junit.jupiter.api.Disabled;

import java.io.*;
import java.util.*;

/**
 * This class demonstrates a typical use of this framework. A user is defining a new pattern and
 * how to extract the result in a text file, and launches the search dialog.
 *
 * The pattern captures two proteins appear to be associated with the same molecular complex.
 *
 * @author Ozgun Babur
 */
@Disabled
public class MinerTest
{
	public static void main(String[] args)
	{
		// Define and initialize the miner.

		Miner miner = new MinerAdapter("Appear-in-same-complex", "The pattern captures two " +
			"proteins appear to be members of the same complex. There may be a nesting hierarchy " +
			"in the complex, and the proteins can be represented with generic entities, again " +
			"through multiple generic-member relations.")
		{
			/**
			 * The pattern is composed of two proteins associated to a complex as members. The
			 * relation can be through nested memberships or through generic relations.
			 */
			public Pattern constructPattern()
			{
				Pattern p = new Pattern(ProteinReference.class, "Protein 1");
				p.add(ConBox.erToPE(), "Protein 1", "SPE1");
				p.add(ConBox.linkToComplex(), "SPE1", "Complex");
				p.add(new Type(Complex.class), "Complex");
				p.add(ConBox.linkToSpecific(), "Complex", "SPE2");
				p.add(ConBox.peToER(), "SPE2", "Protein 2");
				p.add(ConBox.equal(false), "Protein 1", "Protein 2");
				p.add(new Type(ProteinReference.class), "Protein 2");

				return p;
			}

			/**
			 * Writes the result as "P1 P2", where P1 and P2 are gene symbols of proteins and the
			 * whitespace is tab. The relation is undirected, so "P2 P1" is treated as the same
			 * relation.
			 */
			public void writeResult(Map<BioPAXElement, List<Match>> matches, OutputStream out)
				throws IOException
			{
				writeResultAsSIF(matches, out, false, "Protein 1", "Protein 2");
			}
		};

		// Launch the GUI that will assist choosing a source model, and output file name
		Dialog d = new Dialog(miner);
		d.setVisible(true);
	}
}