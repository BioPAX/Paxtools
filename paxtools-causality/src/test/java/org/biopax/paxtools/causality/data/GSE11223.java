package org.biopax.paxtools.causality.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * This class helps to access the categories in the experiment GSE11223.
 *
 * @author Ozgun Babur
 */
public class GSE11223
{
	public static final String GSE_ID = "GSE11223";

	public static final int[] Normal_Uninflamed_sigmoid_colon = new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,49,55,58,60,66,72};
	public static final int[] Normal_Uninflamed_terminal_ileum = new int[]{18,19,20,50,59,63};
	public static final int[] Normal_Uninflamed_descending_colon = new int[]{21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,47,48,52,54,57,65,69};
	public static final int[] Normal_Uninflamed_ascending_colon = new int[]{36,37,38,39,40,41,42,43,44,45,46,51,53,56,61,64,68};
	public static final int[] UC_Uninflamed_sigmoid_colon = new int[]{80,83,85,86,88,89,91,92,93,94,95,96,97,98,100,101,104,111,113,119,161,166,169,186,188};
	public static final int[] Normal_Inflamed_sigmoid_colon = new int[]{62,67,71};
	public static final int[] Normal_Inflamed_descending_colon = new int[]{70};
	public static final int[] UC_Inflamed_terminal_ileum = new int[]{73};
	public static final int[] UC_Inflamed_sigmoid_colon = new int[]{74,78,81,82,87,90,99,106,109,110,112,114,115,117,118,120,141,144,159,164,171,174,175,178,180,181,183,191,195,198,200,201};
	public static final int[] UC_Inflamed_descending_colon = new int[]{75,77,79,121,124,143,158,162,163,170,173,177,182,185,187,190,193,197,199};
	public static final int[] UC_Uninflamed_ascending_colon = new int[]{76,84,126,128,130,145,146,147,148,149,150,151,152,153,154,155,156,167,179,194,196};
	public static final int[] UC_Uninflamed_terminal_ileum = new int[]{102,103,105,116,160};
	public static final int[] UC_Uninflamed_descending_colon = new int[]{107,108,122,123,132,133,134,135,136,137,138,139,140,165,168};
	public static final int[] UC_Inflamed_ascending_colon = new int[]{125,127,129,131,142,157,172,176,184,189,192};

	private static void parse() throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(
			GSE11223.class.getResourceAsStream("GSE11223_samples.txt")));

		final Map<String, List<Integer>> map = new HashMap<String, List<Integer>>();

		for (String line = reader.readLine(); line != null; line = reader.readLine())
		{
			String[] token = line.split("\t");
			int no = Integer.parseInt(token[0]);
			String categ = token[1].substring(token[1].indexOf(" ") + 1);

			if (!map.containsKey(categ)) map.put(categ, new ArrayList<Integer>());

			map.get(categ).add(no);
		}
		
		reader.close();

		List<String> keys = new ArrayList<String>(map.keySet());
		
		Collections.sort(keys, new Comparator<String>()
		{
			@Override
			public int compare(String s1, String s2)
			{
				return map.get(s1).get(0) - map.get(s2).get(0);
			}
		});
		
		for (String categ : keys)
		{
			System.out.print("\tpublic static final int[] " + categ.replaceAll(" ", "_") +
				" = new int[]{");

			List<Integer> nos = map.get(categ);
			System.out.print(nos.get(0));
			for (int i = 1; i < nos.size(); i++)
			{
				System.out.print("," + nos.get(i));
			}
			System.out.println("};");
		}
	}

	public static void main(String[] args) throws IOException
	{
		parse();
	}
	
}
