package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.SmallMoleculeReference;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.biopax.paxtools.pattern.util.RelType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * This class generates a blacklist for the given model. It is important that the given model is the
 * very big integrated corpus. It won't work on tiny little model.
 *
 * @author Ozgun Babur
 */
public class BlacklistGenerator3
{
	/**
	 * Known ubiquitous small molecule names along with their
	 */
	private Map<String, RelType> knownNames;

	/**
	 * Default constructor.
	 */
	public BlacklistGenerator3(InputStream knownNamesIS)
	{
		knownNames = new HashMap<String, RelType>();
		Scanner sc = new Scanner(knownNamesIS);
		while (sc.hasNextLine())
		{
			String[] token = sc.nextLine().split("\t");
			RelType type = token.length < 2 ? null : token[1].equals("I") ?
				RelType.INPUT : RelType.OUTPUT;
			knownNames.put(token[0], type);
		}
	}

	/**
	 * Default constructor.
	 */
	public BlacklistGenerator3(String knownNamesFile) throws FileNotFoundException
	{
		this(new FileInputStream(knownNamesFile));
	}

	/**
	 * Generates the blacklist.
	 * @param model model to use
	 * @return the blacklist
	 */
	public Blacklist generateBlacklist(Model model)
	{
		Blacklist blacklist = new Blacklist();

		// populate the blacklist

		for (SmallMoleculeReference smr : model.getObjects(SmallMoleculeReference.class))
		{
			String name = smr.getDisplayName();
			if (name == null) continue;
			name = name.toLowerCase();


			if (knownNames.containsKey(name))
			{
				blacklist.addEntry(smr.getUri(), 1,
					knownNames.get(name));
			}
		}

		return blacklist;
	}
}
