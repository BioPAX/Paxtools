package org.biopax.paxtools.pattern.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class provides a mapping between HGNC IDs and Symbols.
 *
 * @author Ozgun Babur
 */
public class HGNC
{
	/**
	 * Map from symbol to id.
	 */
	private static Map<String, String> sym2id;

	/**
	 * Map from id to symbol.
	 */
	private static Map<String, String> id2sym;

	/**
	 * Provides HGNC ID of the given approved gene symbol.
	 * @param symbol symbol
	 * @return hgnc id
	 */
	public static String getID(String symbol)
	{
		return sym2id.get(symbol);
	}

	/**
	 * Gets the symbol of the given hgnc id.
	 * @param hgncID hgnc id
	 * @return symbol
	 */
	public static String getSymbol(String hgncID)
	{
		return id2sym.get(hgncID);
	}

	/**
	 * Checks if the hgnc id exists.
	 * @param id hgnc id
	 * @return true if id exists
	 */
	public static boolean containsID(String id)
	{
		return id2sym.containsKey(id);
	}

	/**
	 * Checks if the given symbol exists.
	 * @param symbol symbol
	 * @return true if exists
	 */
	public static boolean containsSymbol(String symbol)
	{
		return sym2id.containsKey(symbol);
	}

	/**
	 * Gets the set of all symbols.
	 * @return all symbols
	 */
	public static Set<String> getSymbols()
	{
		return sym2id.keySet();
	}

	/**
	 * Initializes resources.
	 */
	static
	{
		try
		{
			sym2id = new HashMap<String, String>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
				HGNC.class.getResourceAsStream("hgnc.txt")));
			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				String[] token = line.split("\t");
				String sym = token[1];
				String id = token[0];
				sym2id.put(sym, id);
			}
			reader.close();

			id2sym = new HashMap<String, String>();
			for (String key : sym2id.keySet())
			{
				id2sym.put(sym2id.get(key), key);
			}

		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
