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
 * We periodically, manually download
 * <a href="http://www.genenames.org/cgi-bin/download?col=gd_hgnc_id&col=gd_app_sym&col=gd_prev_sym&status=Approved&status_opt=2&where=&order_by=gd_app_sym_sort&format=text&limit=&hgnc_dbtag=on&submit=submit">
 * the ID mapping file from genenames.org</a>
 *
 * @author Ozgun Babur
 */
public class HGNC
{
	private static Map<String, String> sym2id;
	private static Map<String, String> id2sym;
	private static Map<String, String> old2new;

	public static String getSymbol(String idOrSymbol)
	{
		if (id2sym.containsKey(idOrSymbol)) return id2sym.get(idOrSymbol);
		else if (sym2id.containsKey(idOrSymbol)) return idOrSymbol;
		else if (old2new.containsKey(idOrSymbol)) return old2new.get(idOrSymbol);
		else if (!idOrSymbol.toUpperCase().equals(idOrSymbol))
			return getSymbol(idOrSymbol.toUpperCase());
		else return null;
	}

	static
	{
		try
		{
			sym2id = new HashMap<String, String>();
			id2sym = new HashMap<String, String>();
			old2new = new HashMap<String, String>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
				HGNC.class.getResourceAsStream("hgnc.txt")));

			reader.readLine(); //skip header
			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				String[] token = line.split("\t");
				String sym = token[1].trim();
				String id = token[0].trim();
				sym2id.put(sym, id);
				id2sym.put(id, sym);

				if (token.length > 2)
				{
					String olds = token[2];
					for (String old : olds.split(","))
					{
						old = old.trim();
						old2new.put(old, sym);
					}
				}
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
