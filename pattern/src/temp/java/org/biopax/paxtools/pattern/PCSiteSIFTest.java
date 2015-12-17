package org.biopax.paxtools.pattern;

import org.biopax.paxtools.pattern.miner.SIFEnum;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * @author Ozgun Babur
 */
public class PCSiteSIFTest
{
	public static void main(String[] args) throws IOException
	{
		PCSiteSIFTest test = new PCSiteSIFTest();
		test.compare();
	}

	public void compare() throws IOException
	{
		String site1 = "http://www.pathwaycommons.org/pc2/downloads";
		String site2 = "http://pathwaycommons.baderlab.org/downloads";

		Map<String, String> names1 = parseFileNames(site1);
		Map<String, String> names2 = parseFileNames(site2);

		Set<String> dbs = new HashSet<String>(names1.keySet());
		dbs.retainAll(names2.keySet());

		for (String db : dbs)
		{
			if (!db.equals("pid")) continue;
			System.out.println("Resource: " + db);
			compareTwoSIFFiles(site1 + "/" + names1.get(db), site2 + "/" + names2.get(db));
		}
	}

	private Map<String, String> parseFileNames(String site) throws IOException
	{
		Map<String, String> map = new HashMap<String, String>();

		Scanner sc = new Scanner(new URL(site).openStream());
		while (sc.hasNextLine())
		{
			String line = sc.nextLine();
			if (line.startsWith("\t\t\t\t<li><a rel=\"nofollow\" href='"))
			{
				String file = line.substring(line.indexOf("downloads/") + 10, line.lastIndexOf("'"));

				if (file.contains(";")) file = file.substring(0, file.lastIndexOf(";"));

				if (file.contains(".BINARY_SIF.hgnc.sif.gz"))
				{
					String[] token = file.split("\\.");
					String res = token[2].toLowerCase();

					if (res.endsWith("_human")) res = res.substring(0, res.lastIndexOf("_"));
					if (RES_REPLACE_MAP.containsKey(res)) res = RES_REPLACE_MAP.get(res);

					map.put(res, file);
				}
			}
		}
		return map;
	}

	private void compareTwoSIFFiles(String file1, String file2) throws IOException
	{
		Map<String, Set<String>> map1 = parseSIFFIle(file1);
		Map<String, Set<String>> map2 = parseSIFFIle(file2);

		Set<String> types = new HashSet<String>(map1.keySet());
		types.retainAll(map2.keySet());

		for (String type : types)
		{
			System.out.print(type);
			Set<String> set1 = new HashSet<String>(map1.get(type));
			Set<String> set2 = new HashSet<String>(set1);
			set1.removeAll(map2.get(type));
			set2.retainAll(map2.get(type));

			System.out.print("\t" + set1.size() + "\t" + set2.size());

			Set<String> set3 = new HashSet<String>(map2.get(type));
			set3.removeAll(set2);

			System.out.println("\t" + set3.size());

			if (type.startsWith("controls-state"))
			{
				for (String s : set3)
				{
					System.out.println(s);
				}
			}

		}
		System.out.println("----------");
	}

	private Map<String, Set<String>> parseSIFFIle(String file) throws IOException
	{
		file = file.replaceAll(" ", "%20");
		Map<String, Set<String>> map = new HashMap<String, Set<String>>();
		Scanner sc = new Scanner(new GZIPInputStream(new URL(file).openStream()));
		while (sc.hasNextLine())
		{
			String line = sc.nextLine();

			String[] token = line.split("\t");

			if (token[1].equals("INTERACTION_TYPE")) continue;

			SIFEnum type = SIFEnum.typeOf(token[1]);
			if (type == null) System.out.println(token[1]);
			boolean directed = type.isDirected();

			if (!map.containsKey(token[1])) map.put(token[1], new HashSet<String>());

			String s = directed || token[0].compareTo(token[2]) < 0 ?
				token[0] + " " + token[2] :
				token[2] + " " + token[0];

			map.get(token[1]).add(s);
		}

		return map;
	}

	private static Map<String, String> RES_REPLACE_MAP = new HashMap<String, String>();
	static
	{
		RES_REPLACE_MAP.put("Comparative Toxicogenomics Database", "ctdbase");
	}
}
