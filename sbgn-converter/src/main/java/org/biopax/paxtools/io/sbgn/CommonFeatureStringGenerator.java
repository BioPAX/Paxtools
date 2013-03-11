package org.biopax.paxtools.io.sbgn;

import org.biopax.paxtools.model.level3.*;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.ObjectFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Prepares displayable Stat class to generate labels like "pY@123" for modification features and
 * like x[30 - 122] for fragment features.
 *
 * @author Ozgun Babur
 */
public class CommonFeatureStringGenerator implements FeatureDecorator
{
	/**
	 * Map from modification term to its symbol.
	 */
	private static Map<String, String> symbolMapping;

	/**
	 * Map from location term (amino acid name) to is symbol (letter).
	 */
	private static Map<String, String> locMapping;

	/**
	 * Creates State to represent the entity feature.
	 * @param ef feature to represent
	 * @param factory factory that can create the State class
	 * @return State representing the feature
	 */
	@Override
	public Glyph.State createStateVar(EntityFeature ef, ObjectFactory factory)
	{
		if (ef instanceof FragmentFeature)
		{
			FragmentFeature ff = (FragmentFeature) ef;
			SequenceLocation loc = ff.getFeatureLocation();
			if (loc instanceof SequenceInterval)
			{
				SequenceInterval si = (SequenceInterval) loc;
				SequenceSite begin = si.getSequenceIntervalBegin();
				SequenceSite end = si.getSequenceIntervalEnd();
				
				if (begin != null && end != null)
				{
					Glyph.State state = factory.createGlyphState();
					state.setValue("x[" + begin.getSequencePosition() + " - " +
						end.getSequencePosition() + "]");
					return state;
				}
			}
		}
		else if (ef instanceof ModificationFeature)
		{
			ModificationFeature mf = (ModificationFeature) ef;
			SequenceModificationVocabulary modType = mf.getModificationType();
			
			if (modType != null)
			{
				Set<String> terms = modType.getTerm();
				if (terms != null && !terms.isEmpty())
				{
					String orig = terms.iterator().next();
					String term = orig.toLowerCase();
					
					String s = symbolMapping.containsKey(term) ? symbolMapping.get(term) : orig;

					Glyph.State state = factory.createGlyphState();
					state.setValue(s);
					
					SequenceLocation loc = mf.getFeatureLocation();
					if (locMapping.containsKey(term))
					{
						state.setVariable(locMapping.get(term));
					}

					if (loc instanceof SequenceSite)
					{
						SequenceSite ss = (SequenceSite) loc;
						state.setVariable((state.getVariable() != null ? state.getVariable() : "") +
							ss.getSequencePosition());
					}
					
					return state;
				}
			}
		}

		// Binding features are ignored
		return null;
	}

	/**
	 * Initializes resources.
	 */
	static
	{
		symbolMapping = new HashMap<String, String>();
		locMapping = new HashMap<String, String>();
		
		try
		{
			InputStream is = CommonFeatureStringGenerator.class.getResourceAsStream(
				"feature-shorts.txt");

			BufferedReader reader = new BufferedReader(new InputStreamReader(is));

			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				String[] token = line.split("\t");

				if (token.length > 1 && token[1] != null && token[1].length() > 0)
				{
					String key = token[0].replace("\"", "").toLowerCase();
					symbolMapping.put(key, token[1].replace("\"", ""));
					
					if (token.length > 2 && token[2] != null && token[2].length() > 0)
					{
						locMapping.put(key, token[2].replace("\"", ""));
					}
				}
			}

			reader.close();
		}
		catch(Exception e){e.printStackTrace();}
	}
}
