package org.biopax.paxtools.pattern;

import org.biopax.paxtools.pattern.util.HGNC;

import java.io.*;
import java.util.*;

/**
 * @author Ozgun Babur
 */
public class SPIKEParser
{
	static final boolean SKIP_TRANSCRIPTION = false;

	public static void main(String[] args) throws IOException
	{
		SPIKEParser parser = new SPIKEParser();
		parser.parse("/home/ozgun/Desktop/signal-db/LatestSpikeDB.xml",
			"/home/ozgun/Desktop/signal-db/SPIKE-ParsedFromXML.txt");
	}
	public void parse(String inputFile, String outputFile) throws IOException
	{
		Map<String, Gene> id2gene = new HashMap<String, Gene>();
		Map<String, Group> id2group = new HashMap<String, Group>();
		Set<Regulation> regs = new HashSet<Regulation>();

		BufferedReader reader = new BufferedReader(new FileReader(inputFile));

		Set<String> blocks = new HashSet<String>();

		String build = null;
		int indent = 10;
		for (String line = reader.readLine(); line != null; line = reader.readLine())
		{
			if (!line.trim().startsWith("<")) continue;

			if (build != null)
			{
				if (line.indexOf("<") > indent)
				{
					build += "\n" + line;
				}
				else
				{
					blocks.add(build);
					build = null;
				}
			}

			if (line.trim().startsWith("<Gene ") ||
				line.trim().startsWith("<Group ") ||
				line.trim().startsWith("<Regulation "))
			{
				assert build == null;
				build = line;
				indent = line.indexOf("<");
			}
		}

		reader.close();

		System.out.println("blocks = " + blocks.size());

		for (String block : blocks)
		{
			if (block.trim().startsWith("<Gene"))
			{
				Gene g = new Gene(block);
				id2gene.put(g.id, g);
			}
			else if (block.trim().startsWith("<Group"))
			{
				Group g = new Group(block);
				id2group.put(g.id, g);
			}
			else if (block.trim().startsWith("<Regulation"))
			{
				regs.add(new Regulation(block));
			}
		}

		System.out.println("regs = " + regs.size());
		System.out.println("id2gene = " + id2gene.size());
		System.out.println("id2group = " + id2group.size());


		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

		int i = 0;
		for (Regulation reg : regs)
		{
			if (SKIP_TRANSCRIPTION && reg.transcription)
			{
				i++;
				continue;
			}

			List<Gene> sources = getGenes(reg.source, id2gene, id2group);
			List<Gene> targets = getGenes(reg.target, id2gene, id2group);

			for (Gene source : sources)
			{
				for (Gene target : targets)
				{
					String s = HGNC.getSymbol(source.name);
					String t = HGNC.getSymbol(target.name);
					if (s != null && t != null)
						writer.write("\n" + s + "\t" + t + "\t" + (reg.transcription ? "T" : ""));
				}
			}
		}

		writer.close();

		if (i > 0) System.out.println(i + " TR interactions skipped.");
	}

	List<Gene> getGenes(String id, Map<String, Gene> id2gene, Map<String, Group> id2group)
	{
		if (id2gene.containsKey(id)) return Arrays.asList(id2gene.get(id));

		if (!id2group.containsKey(id)) return Collections.emptyList();

		Group g = id2group.get(id);

		List<Gene> list = new ArrayList<Gene>();

		for (String memID : g.members)
		{
			list.addAll(getGenes(memID, id2gene, id2group));
		}
		return list;
	}

	class Gene
	{
		String id;
		String name;
		String egID;

		Gene(String block)
		{
			for (String line : block.split("\n"))
			{
				line = line.trim();
				if (line.startsWith("<Gene "))
				{
					int i = line.indexOf("name=") + 6;
					name = line.substring(i, line.indexOf("\"", i));
					i = line.indexOf("id=") + 4;
					id = line.substring(i, line.indexOf("\"", i));
				}
				else if (line.startsWith("<XRef db=\"1\""))
				{
					egID = line.substring(line.lastIndexOf("=\"") + 2, line.lastIndexOf("\""));
				}
			}

			assert id != null;
			assert egID != null : "EG ID null. ID = " + id;
			assert name != null : "Name null. ID = " + id;

			if (eg2symbol.containsKey(egID))
			{
				name = eg2symbol.get(egID);
			}
		}
	}

	class Group
	{
		String id;
		List<String> members;

		Group(String block)
		{
			members = new ArrayList<String>();

			for (String line : block.split("\n"))
			{
				line = line.trim();

				if (line.startsWith("<Group "))
				{
					int i = line.indexOf("id=") + 4;
					id = line.substring(i, line.indexOf("\"", i));
				}
				else if (line.startsWith("<Member "))
				{
					members.add(line.substring(line.lastIndexOf("=\"") + 2, line.lastIndexOf("\"")));
				}
			}

			assert id != null;
		}
	}

	class Regulation
	{
		String source;
		String target;
		boolean transcription;

		Regulation(String block)
		{
			for (String line : block.split("\n"))
			{
				line = line.trim();
				if (line.startsWith("<Regulation"))
				{
					int ind = line.indexOf("mechanism=\"") + 11;
					String mec = line.substring(ind, line.indexOf("\"", ind));
					transcription = mec.equals("Transcription Regulation");
				}
				else if (line.startsWith("<Source "))
				{
					assert source == null;
					source = line.substring(line.lastIndexOf("=\"") + 2, line.lastIndexOf("\""));
				}
				else if (line.startsWith("<PhysicalTarget "))
				{
					assert target == null;
					target = line.substring(line.lastIndexOf("=\"") + 2, line.lastIndexOf("\""));
				}
			}
			assert source != null : "Source null = " + block;
			assert target != null;
		}

	}

	static Map<String, String> eg2symbol = getEG2HGNC();
	public static Map<String, String> getEG2HGNC()
	{
		Map<String, String> map = new HashMap<String, String>();
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader("/home/ozgun/Desktop/hgnc2eg.txt"));

			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				String[] token = line.split("\t");

				if (token.length < 2) continue;

				map.put(token[1], token[0]);
			}
			reader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return map;
	}

}
