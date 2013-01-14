package org.biopax.paxtools.conversion;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provides a mapping Entrez Gene IDs and gene symbols.
 *
 * @author Ozgun Babur
 */
public class EntrezGene
{
	private static Map<String, String> sym2id;
	private static Map<String, String> id2sym;

	public static void main(String[] args)
	{
		System.out.println("getSymbol(\"367\") = " + getSymbol("367"));
		System.out.println("getID(\"AR\") = " + getID("AR"));
	}

	/**
	 * Provides Entrez Gene ID of the given gene symbol.
	 * @param symbol gene symbol
	 * @return EG ID
	 */
	public static String getID(String symbol)
	{
		return sym2id.get(symbol);
	}

	public static String getSymbol(String id)
	{
		return id2sym.get(id);
	}

	public static boolean containsID(String id)
	{
		return id2sym.containsKey(id);
	}

	public static boolean containsSymbol(String symbol)
	{
		return sym2id.containsKey(symbol);
	}

	static
	{
		try
		{
			sym2id = new HashMap<String, String>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
				HGNC.class.getResourceAsStream("EntrezGene.txt")));
			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				String[] token = line.split("\t");

				if (token.length < 2) continue;

				String sym = token[0];
				String id = token[1];
				if (sym.length() > 0 && id.length() > 0) sym2id.put(sym, id);
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
