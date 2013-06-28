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
	protected int lastIndex;

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
	 * Constructor with the starting class.
	 * @param startingClass class of first element in the pattern
	 */
	private Pattern(Class<? extends BioPAXElement> startingClass)
	{
		this.startingClass = startingClass;
		this.labelMap = new HashMap<String, Integer>();
		this.constraints = new ArrayList<MappedConst>();
		this.lastIndex = 0;
	}

	/**
	 * Constructor with the first constraint and labels it uses. This constructor is good for
	 * patterns with a single constraint.
	 * @param startingClass type of initial element
	 * @param firstConstraint first constraint
	 * @param label labels for the constraint
	 */
	public Pattern(Class<? extends BioPAXElement> startingClass, Constraint firstConstraint,
		String... label)
	{
		this(startingClass, label[0]);
		add(firstConstraint, label);
	}

	/**
	 * Constructor with a label for the element at index 0.
	 * @param startingClass type of the initial element
	 * @param label a label for the initial element
	 */
	public Pattern(Class<? extends BioPAXElement> startingClass, String label)
	{
		this(startingClass);
		label(label, 0);
	}

	/**
	 * Creates a mapped constraint with the given constraint and the indexes it applies.
	 * @param constr constraint to add
	 * @param ind indices to map the constraint to the element in the pattern
	 */
	private void add(Constraint constr, int... ind)
	{
		assert ind.length > 0;
		assert constr.getVariableSize() == ind.length;

		for (int i = 0; i < (constr.canGenerate() ? ind.length - 1 : ind.length); i++)
		{
			assert ind[i] <= lastIndex;
		}

		constraints.add(new MappedConst(constr, ind));

		if (constr.canGenerate() && ind[ind.length - 1] > lastIndex)
		{
			if (ind[ind.length - 1] - lastIndex > 1) throw new IllegalArgumentException(
				"Generated index too large. Attempting to generate index " + ind[ind.length - 1] +
					" while last index is " + lastIndex);

			else lastIndex++;
		}
	}

	/**
	 * Creates a mapped constraint with the given generative constraint and the indexes it applies.
	 * Also labels the last given index.
	 * @param constr constraint to add
	 * @param label a label for the last of the given indices
	 */
	public void add(Constraint constr, String... label)
	{
		checkLabels(constr.canGenerate(), label);

		int[] ind = convertLabelsToInds(label);

		if (ind.length != constr.getVariableSize())
		{
			throw new IllegalArgumentException("Mapped elements do not match the constraint size.");
		}

		// This will also increment lastIndex if necessary
		add(constr, ind);

		if (!hasLabel(label[label.length - 1]) && constr.canGenerate())
		{
			label(label[label.length - 1], lastIndex);
		}
	}

	/**
	 * Converts the labels to the indexes. Assumes the sanity of the labels already checked and if
	 * any new label exists, it is only one and it is the last one.
	 * @param label labels
	 * @return indexes for labels
	 */
	private int[] convertLabelsToInds(String... label)
	{
		int[] ind = new int[label.length];
		for (int i = 0; i < label.length; i++)
		{
			if (hasLabel(label[i]))
			{
				ind[i] = indexOf(label[i]);
			}
			else ind[i] = lastIndex + 1;
		}
		return ind;
	}

	/**
	 * Converts the indices to the labels. All indices must have an existing label.
	 * @param ind indices
	 * @return labels for indices
	 */
	private String[] convertIndsToLabels(int... ind)
	{
		String[] label = new String[ind.length];
		for (int i = 0; i < ind.length; i++)
		{
			if (!hasLabel(ind[i])) throw new IllegalArgumentException(
				"The index " + ind[i] + " does not have a label.");

			label[i] = getLabel(ind[i]);
		}
		return label;
	}

	/**
	 * Checks if all labels (other than the last if generative) exists.
	 * @param forGenerative whether the check is performed for a generative constraint
	 * @param label labels to check
	 * @throws IllegalArgumentException when a necessary label is not found
	 */
	private void checkLabels(boolean forGenerative, String... label)
	{
		for (int i = 0; i < (forGenerative ? label.length - 1 : label.length); i++)
		{
			if (!hasLabel(label[i])) throw new IllegalArgumentException(
				"Label neither found, nor generated: " + label[i]);
		}
	}

	/**
	 * Appends the constraints in the parameter pattern to the desired location. Indexes in the
	 * constraint mappings are translated so that 0 is translated to ind0, and others are translated
	 * to orig + indAppend - 1. All slots of this pattern should already be full before calling this
	 * method. This method makes room for the new variables. Labels in the parameter pattern is
	 * transferred to this pattern. If there are equivalent labels, then these slots are mapped.
	 * 
	 * @param p the parameter pattern
	 */
	public void add(Pattern p)
	{
		if (!hasLabel(p.getLabel(0))) throw new IllegalArgumentException("The label of first " +
			"element of parameter index \"" + p.getLabel(0) + "\" not found in this pattern.");

		for (MappedConst mc : p.getConstraints())
		{
			add(mc.getConstr(), p.convertIndsToLabels(mc.getInds()));
		}
	}

	/**
	 * Gets the label for the element at the specified index.
	 * @param i index
	 * @return label for the element at the specified index
	 */
	private String getLabel(int i)
	{
		for (String label : labelMap.keySet())
		{
			if (labelMap.get(label) == i) return label;
		}
		return null;
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
	 * Gets the element size of the pattern.
	 * @return size inferred from lastIndex
	 */
	public int size()
	{
		return lastIndex + 1;
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
	public boolean hasLabel(String labelText)
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
			throw new IllegalArgumentException("The label \"" + labelText +
				"\" is absent in pattern.");
		
		return labelMap.get(labelText);
	}

	/**
	 * Changes a label. The oldLabel has to be an existing label and new label has to be a new
	 * label.
	 * @param oldLabel label to update
	 * @param newLabel updated label
	 */
	public void updateLabel(String oldLabel, String newLabel)
	{
		if (hasLabel(newLabel)) throw new IllegalArgumentException(
			"The label \"" + newLabel + "\" already exists.");

		int i = indexOf(oldLabel);
		labelMap.remove(oldLabel);
		labelMap.put(newLabel, i);
	}
}
