package org.biopax.paxtools.causality.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ozgun Babur
 */
public class EGUtil
{
	private static Map<String, String> sym2id;

	public static void main(String[] args)
	{
		System.out.println(getEGID("BAX"));
	}

	/**
	 * Provides HGNC ID of the given approved gene symbol.
	 * @param symbol
	 * @return
	 */
	public static String getEGID(String symbol)
	{
		return sym2id.get(symbol);
	}

	static
	{
		try
		{
			sym2id = new HashMap<String, String>();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
				EGUtil.class.getResourceAsStream("EntrezGene.txt")));

			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				String[] token = line.split("\t");
				if (token.length < 2) continue;
				String sym = token[0];
				String id = token[1];
				sym2id.put(sym, id);
			}
			reader.close();
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
