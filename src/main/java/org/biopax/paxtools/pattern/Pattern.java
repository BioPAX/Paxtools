package org.biopax.paxtools.pattern;

import org.biopax.paxtools.model.BioPAXElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A pattern is a list of mapped constraints.
 *
 * @author Ozgun Babur
 */
public class Pattern
{
	/**
	 * How many elements are there in a pattern match.
	 */
	protected int variableSize;

	/**
	 * Class of the first elements to match with this pattern.
	 */
	protected Class<? extends BioPAXElement> startingClass;

	/**
	 * List of mapped constraints.
	 */
	protected List<MappedConst> constraints;

	/**
	 * Indexes in a pattern can be labeled using this map.
	 */
	protected Map<String, Integer> labelMap;

	/**
	 * Constructor with constraints.
	 * @param variableSize size of the pattern
	 * @param startingClass type of initial element
	 * @param constraints the list of constraints
	 */
	public Pattern(int variableSize, Class<? extends BioPAXElement> startingClass,
		List<MappedConst> constraints)
	{
		this.variableSize = variableSize;
		this.startingClass = startingClass;
		this.constraints = constraints;
		this.labelMap = new HashMap<String, Integer>();
	}

	/**
	 * Constructor without constraints.
	 * @param variableSize size of the pattern
	 * @param startingClass type of the initial element
	 */
	public Pattern(int variableSize, Class<? extends BioPAXElement> startingClass)
	{
		this(variableSize, startingClass, new ArrayList<MappedConst>());
	}

	/**
	 * Constructor without constraints, and with a label for the element at index 0.
	 * @param variableSize size of the pattern
	 * @param startingClass type of the initial element
	 * @param label a label for the initial element
	 */
	public Pattern(int variableSize, Class<? extends BioPAXElement> startingClass, String label)
	{
		this(variableSize, startingClass);
		label(label, 0);
	}

	/**
	 * Creates a mapped constraint with the given constraint and the indexes it applies.
	 * @param constr constraint to add
	 * @param ind indices to map the constraint to the element in the pattern
	 */
	public void addConstraint(Constraint constr, int ... ind)
	{
		assert ind.length > 0;
		assert checkIndsInRange(ind);
		assert constr.getVariableSize() == ind.length;
		constraints.add(new MappedConst(constr, ind));
	}

	/**
	 * Creates a mapped constraint with the given constraint and the indexes it applies. Also labels
	 * the last given index.
	 * @param constr constraint to add
	 * @param label a label for the last of the given indices
	 * @param ind indices to map the constraint to the element in the pattern
	 */
	public void addConstraint(Constraint constr, String label, int ... ind)
	{
		addConstraint(constr, ind);
		label(label, ind[ind.length - 1]);
	}

	/**
	 * Appends the constraints in the parameter pattern to the desired location. Indexes in the
	 * constraint mappings are translated so that 0 is translated to ind0, and others are translated
	 * to orig + indAppend - 1. All slots of this pattern should already be full before calling this
	 * method. This method makes room for the new variables. Labels in the parameter pattern is
	 * transferred to this pattern. If there are equivalent labels, then these slots are mapped.
	 * 
	 * @param p the parameter pattern
	 * @param ind0 index 0 of the parameter pattern will map to this index of this pattern
	 */
	public void addPattern(Pattern p, int ind0)
	{
		int indAppend = this.getVariableSize();

		if (ind0 >= indAppend)
			throw new IllegalArgumentException("indAppend should always be greater than ind0");

		increaseVariableSizeFor(p);

		Map<Integer, Integer> labTransMap = new HashMap<Integer, Integer>();

		for (String key : p.labelMap.keySet())
		{
			if (this.labelMap.containsKey(key))
			{
				labTransMap.put(p.labelMap.get(key), this.labelMap.get(key));
			}
		}

		List<Integer> mappedIndexes = new ArrayList<Integer>(labTransMap.keySet());

		for (MappedConst mc : p.constraints)
		{
			Constraint c = mc.getConstr();
			int[] inds = mc.getInds();

			int[] t = new int[inds.length];
			for (int j = 0; j < t.length; j++)
			{
				if (inds[j] == 0)
				{
					t[j] =  ind0;
				}
				else if (mappedIndexes.contains(inds[j]))
				{
					t[j] = labTransMap.get(inds[j]);
				}
				else
				{
					// Find the number of mapped indexes smaller than current one
					int mappedSmaller = 1; // 0 was mapped for sure
					for (Integer ind : labTransMap.keySet())
					{
						if (ind < inds[j]) mappedSmaller++;
					}

					t[j] =  inds[j] + indAppend - mappedSmaller;
				}
			}

			addConstraint(c, t);
		}
		
		for (String key : p.labelMap.keySet())
		{
			// Skip equivalent labels. They were mapped already.
			if (labelMap.containsKey(key)) continue;

			int loc = p.indexOf(key);

			// If ind0 already has a label, don not transfer
			if (loc == 0 && labelMap.containsValue(ind0)) continue;

			label(key, loc == 0 ? ind0 : loc + indAppend - 1);
		}
	}

	/**
	 * A point constraint deals with only one element in a match, checks its validity. This method
	 * injects the parameter constraint multiple times among the list of mapped constraints, to the
	 * specified indexes.
	 *
	 * @param con constraint to add
	 * @param ind indices to add this point constraint
	 */
	public void insertPointConstraint(Constraint con, int ... ind)
	{
		assert con.getVariableSize() == 1;

		for (int i : ind)
		{
			for (int j = 0; j < constraints.size(); j++)
			{
				int[] index = constraints.get(j).getInds();
				if (index[index.length-1] == i)
				{
					constraints.add(j + 1, new MappedConst(con, i));
					break;
				}
			}
		}
	}

	/**
	 * Getter for the constraint list.
	 * @return constraints
	 */
	public List<MappedConst> getConstraints()
	{
		return constraints;
	}

	/**
	 * Checks if these indices are in the range of variable size.
	 * @param ind indices to check
	 * @return true if in the range
	 */
	private boolean checkIndsInRange(int ... ind)
	{
		for (int i : ind) if (i >= variableSize) return false;
		return true;
	}

	/**
	 * Gets the size of the pattern.
	 * @return size of the pattern
	 */
	public int getVariableSize()
	{
		return variableSize;
	}

	/**
	 * Gets the index of last element in the pattern.
	 * @return index of last element
	 */
	public int getLastIndex()
	{
		return variableSize - 1;
	}

	/**
	 * This method changes the size of the pattern. Use with caution, especially if you are
	 * decreasing it.
	 * @param variableSize new pattern size
	 */
	public void setVariableSize(int variableSize)
	{
		this.variableSize = variableSize;
	}

	/**
	 * This method modifies the pattern size by the given amount. Use with caution, especially if
	 * inc is negative.
	 * @param inc amount to add to the size
	 */
	public void increaseVariableSizeBy(int inc)
	{
		this.variableSize += inc;
	}

	/**
	 * Changes the pattern size, making room for the new pattern to add this pattern.
	 * @param p pattern that will probably be added to this pattern
	 */
	public void increaseVariableSizeFor(Pattern p)
	{
		int cnt = 1;
		for (String key : p.labelMap.keySet())
		{
			if (labelMap.containsKey(key) && p.labelMap.get(key) != 0) cnt++;
		}
		this.variableSize += p.getVariableSize() - cnt;
	}

	/**
	 * Gets the type of the initial element.
	 * @return type of first element in a match
	 */
	public Class<? extends BioPAXElement> getStartingClass()
	{
		return startingClass;
	}

	/**
	 * Puts the given label for the given index.
	 * @param labelText the label
	 * @param index index to label
	 */
	public void label(String labelText, int index)
	{
		if (labelMap.containsKey(labelText)) throw new IllegalArgumentException(
			"Label \"" + labelText + "\" already exists.");

		if (labelMap.containsValue(index)) throw new IllegalArgumentException(
			"Index \"" + index + "\" already has a label.");

		labelMap.put(labelText, index);
	}

	/**
	 * Checks if the label is already in use.
	 * @param labelText label to check
	 * @return true if label exists
	 */
	public boolean labelExists(String labelText)
	{
		return labelMap.containsKey(labelText);
	}

	/**
	 * Checks if the given location has a label.
	 * @param index index to check
	 * @return true if a label exists for the given index
	 */
	public boolean hasLabel(int index)
	{
		return labelMap.containsValue(index);
	}

	/**
	 * Gets the index of the given label. The label must exist, otherwise a runtime exception is
	 * thrown.
	 * @param labelText label to check
	 * @return index of the label
	 */
	public int indexOf(String labelText)
	{
		if (!labelMap.containsKey(labelText))
			throw new RuntimeException("The label \"" + labelText + "\" is absent.");
		
		return labelMap.get(labelText);
	}
}
