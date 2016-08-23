package org.biopax.paxtools.io.sbgn.idmapping;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class provides a mapping between HGNC IDs and approved gene symbols.
 *
 * @author Ozgun Babur
 */
public class HGNC
{
	private static Map<String, String> sym2id;
	private static Map<String, String> id2sym;

	public static void main(String[] args)
	{
		System.out.println(getID("BAX"));
	}

	/**
	 * Provides HGNC ID of the given approved gene symbol.
	 * @param symbol HGNC Symbol (aka gene name/symbol)
	 * @return HGNC ID
	 */
	public static String getID(String symbol)
	{
		return sym2id.get(symbol);
	}

	public static String getSymbol(String hgncID)
	{
		return id2sym.get(hgncID);
	}

	public static boolean containsID(String id)
	{
		return id2sym.containsKey(id);
	}

	public static boolean containsSymbol(String symbol)
	{
		return sym2id.containsKey(symbol);
	}

	public static Set<String> getSymbols()
	{
		return sym2id.keySet();
	}

	static
	{
		try
		{
			sym2id = new HashMap<String, String>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
				HGNC.class.getResourceAsStream("HGNC.txt")));
			reader.readLine(); // skip header
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
