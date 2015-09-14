package org.biopax.paxtools.pattern.util;

import org.biopax.paxtools.model.level3.*;

import java.io.*;
import java.util.*;

/**
 * A blacklist is used for not using ubiquitous small molecules in patterns. This class knows how to
 * read itself from an InputStream, and can write itself to an OutputStream.
 *
 * @author Ozgun Babur
 */
public class Blacklist
{
	/**
	 * Holds ID of blacklisted small molecule's reference. Maps them to the context of ubiquity.
	 * When the context is both INPUT and OUTPUT, it is represented with a null value.
	 */
	private Map<String, RelType> context;

	/**
	 * Maps IDs of blacklisted small molecule references to their ubiquity scores.
	 */
	private Map<String, Integer> score;

	/**
	 * The deliminator string in the data.
	 */
	private static final String DELIM = "\t";

	/**
	 * Constructor for a blank blacklist.
	 */
	public Blacklist()
	{
		context = new HashMap<String, RelType>();
		score = new HashMap<String, Integer>();
	}

	/**
	 * Constructor with resource file name.
	 *
	 * @param filename file path to import the blacklist entries from
	 */
	public Blacklist(String filename)
	{
		this();
		load(filename);
	}

	/**
	 * Constructor with resource input stream.
	 *
	 * @param is input stream to read/init the blacklist from
	 */
	public Blacklist(InputStream is)
	{
		this();
		load(is);
	}

	//----- Section: Input / Output ---------------------------------------------------------------|

	/**
	 * Reads data from the given file.
	 */
	private void load(String filename)
	{
		try
		{
			load(new FileInputStream(filename));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Reads data from the input stream and loads itself.
	 */
	private void load(InputStream is)
	{
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));

			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				String[] tok = line.split(DELIM);
				if (tok.length >= 3)
				{
					addEntry(tok[0], Integer.parseInt(tok[1]), convertContext(tok[2]));
				}
			}

			reader.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			context = null;
			score = null;
		}
	}

	/**
	 * Adds a new blacklisted ID.
	 * @param id ID of the blacklisted molecule
	 * @param score the ubiquity score
	 * @param context context of ubiquity
	 */
	public void addEntry(String id, int score, RelType context)
	{
		this.score.put(id, score);
		this.context.put(id, context);
	}

	/**
	 * Gets the IDs of the blacklisted molecules.
	 *
	 * @return IDs
	 */
	public Set<String> getListed()
	{
		return score.keySet();
	}

	/**
	 * Dumps data to the given file.
	 *
	 * @param filename output file name
	 */
	public void write(String filename)
	{
		try
		{
			write(new FileOutputStream(filename));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Dumps data to the given output stream.
	 *
	 * @param os output stream
	 */
	public void write(OutputStream os)
	{
		List<String> ids = new ArrayList<String>(score.keySet());
		final Map<String, Integer> score = this.score;
		Collections.sort(ids, new Comparator<String>()
		{
			@Override
			public int compare(String o1, String o2)
			{
				return score.get(o2).compareTo(score.get(o1));
			}
		});

		try
		{
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));

			boolean notFirst = false;

			for (String id : ids)
			{
				if (notFirst) writer.write("\n");
				else notFirst = true;

				writer.write(id + DELIM + score.get(id) + DELIM + convertContext(context.get(id)));
			}

			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Converts enum context to text.
	 * @param type context
	 * @return text value
	 */
	private String convertContext(RelType type)
	{
		if (type == null) return "B";

		switch (type)
		{
			case INPUT: return "I";
			case OUTPUT: return "O";
			default: return "B";
		}
	}

	/**
	 * Converts text context to enum.
	 * @param type context
	 * @return enum value
	 */
	private RelType convertContext(String type)
	{
		if (type.equals("I")) return RelType.INPUT;
		if (type.equals("O")) return RelType.OUTPUT;
		if (type.equals("B")) return null;
		throw new IllegalArgumentException("Unknown context: " + type);
	}

	// --------- Section: Accessory methods -------------------------------------------------------|

	/**
	 * Gets the subset with the least score.
	 */
	private Set<String> getLeastUbique(Collection<String> ids)
	{
		Set<String> select = new HashSet<String>();

		int s = getLeastScore(ids);

		for (String id : ids)
		{
			if (score.get(id) == s) select.add(id);
		}

		return select;
	}

	/**
	 * Gets the least score of the given ids.
	 */
	private int getLeastScore(Collection<String> ids)
	{
		int s = Integer.MAX_VALUE;

		for (String id : ids)
		{
			if (score.get(id) < s) s = score.get(id);
		}

		return s;
	}

	/**
	 * Gets the context of the ubiquity of the ID. Be careful with the result. If the result is
	 * null, then either the ID may not be ubique, or the ID may be ubique without a context (which
	 * means in both contexts).
	 * @param id ID to check
	 * @return context of ubiquity
	 */
	public RelType getContext(String id)
	{
		return context.get(id);
	}

	/**
	 * Checks if the given ID is blacklisted in at least one context.
	 */
	private boolean isUbique(String id)
	{
		return isUbique(id, null);
	}

	/**
	 * Checks if the given ID is blacklisted in both contexts together.
	 */
	private boolean isUbiqueInBothContexts(String id)
	{
		return context.containsKey(id) && context.get(id) == null;
	}

	/**
	 * Checks if the given ID is blacklisted in the given context.
	 */
	private boolean isUbique(String id, RelType context)
	{
		if (context == null) return this.context.containsKey(id);

		if (!isUbique(id)) return false;

		RelType ctx = this.context.get(id);
		return ctx == null || ctx.equals(context);
	}

	/**
	 * Checks if the given entity is blacklisted in at least one context.
	 *
	 * @param pe physical entity BioPAX object
	 * @return true/false
	 */
	public boolean isUbique(PhysicalEntity pe)
	{
		String id = getSMRID(pe);
		return id != null && isUbique(id);
	}

	/**
	 * Checks if the given entity is blacklisted in both context together.
	 *
	 * @param pe physical entity BioPAX object
	 * @return true/false
	 */
	public boolean isUbiqueInBothContexts(PhysicalEntity pe)
	{
		String id = getSMRID(pe);
		return id != null && isUbiqueInBothContexts(id);
	}

	/**
	 * Checks if the given entity is blacklisted for the given Conversion assuming the Conversion
	 * flows towards the given direction, and the entity is in given context.
	 *
	 * @param pe physical entity BioPAX object
	 * @param conv conversion interaction (BioPAX)
	 * @param dir conversion direction
	 * @param context relationship type - context
	 * @return true/false
	 */
	public boolean isUbique(PhysicalEntity pe, Conversion conv, ConversionDirectionType dir,
		RelType context)
	{
		String id = getSMRID(pe);
		if (id == null) return false;

		if (dir == null)
			throw new IllegalArgumentException("The conversion direction has to be specified.");

		if (context == null)
			throw new IllegalArgumentException("The context has to be only one type.");

		Set<PhysicalEntity> parts;

		if (dir == ConversionDirectionType.REVERSIBLE)
		{
			if (conv.getLeft().contains(pe)) parts = conv.getLeft();
			else if (conv.getRight().contains(pe)) parts = conv.getRight();
			else throw new IllegalArgumentException("The PhysicalEntity has to be at least one " +
					"side of the Conversion");
		}
		else
		{
			parts = dir == ConversionDirectionType.LEFT_TO_RIGHT ?
				context == RelType.INPUT ? conv.getLeft() : conv.getRight() :
				context == RelType.OUTPUT ? conv.getLeft() : conv.getRight();
		}

		// if the Conversion direction is reversible, then don't mind the current context
		if (dir == ConversionDirectionType.REVERSIBLE)
			return getUbiques(parts, null).contains(pe);
		else return getUbiques(parts, context).contains(pe);
	}

	/**
	 * Gets the ID of the reference of the given entity if it is a small molecule.
	 */
	private String getSMRID(PhysicalEntity pe)
	{
		if (pe instanceof SmallMolecule)
		{
			EntityReference er = ((SmallMolecule) pe).getEntityReference();
			if (er != null) return er.getRDFId();
		}
		return null;
	}

	/**
	 * Gets the ubiquitous small molecules among the given set and in the given context. It is
	 * assumed that the given set is either left or right of a Conversion. If there is no
	 * non-ubiquitous element in the set, then the least ubique(s) are removed from the result.
	 * @param entities left or right of a conversion
	 * @param context are these entities input or output
	 * @return ubiquitous small molecules in the given context
	 */
	public Collection<SmallMolecule> getUbiques(Set<PhysicalEntity> entities, RelType context)
	{
		Map<String, SmallMolecule> ubiques = new HashMap<String, SmallMolecule>();
		boolean allUbiques = true;

		for (PhysicalEntity pe : entities)
		{
			if (pe instanceof SmallMolecule)
			{
				EntityReference er = ((SmallMolecule) pe).getEntityReference();

				if (er != null && isUbique(er.getRDFId(), context))
				{
					ubiques.put(er.getRDFId(), (SmallMolecule) pe);
				}
				else
				{
					allUbiques = false;
				}
			}
			else allUbiques = false;
		}

		if (allUbiques && !ubiques.isEmpty())
		{
			Set<String> least = getLeastUbique(ubiques.keySet());
			for (String id : least)
			{
				ubiques.remove(id);
			}
		}

		return ubiques.values();
	}

	/**
	 * Gets the non-ubiquitous physical entities in the given set and in the given context. It is
	 * assumed that the given set is either left or right of a Conversion. If there is no
	 * non-ubiquitous element in the set, then the least ubique(s) are added to the result.
	 * @param entities left or right of a conversion
	 * @param ctx are these entities input or output
	 * @return non-ubiquitous physical entities in the given context
	 */
	public Set<PhysicalEntity> getNonUbiques(Set<PhysicalEntity> entities, RelType ctx)
	{
		Collection<SmallMolecule> ubiques = getUbiques(entities, ctx);
		if (ubiques.isEmpty()) return entities;

		Set<PhysicalEntity> result = new HashSet<PhysicalEntity>(entities);
		result.removeAll(ubiques);
		return result;
	}

	public Set getNonUbiqueObjects(Set objects)
	{
		Set result = new HashSet();
		for (Object o : objects)
		{
			if (o instanceof SmallMolecule && !isUbique((SmallMolecule) o)) result.add(o);
		}
		return result;
	}
}
