package org.biopax.paxtools.io.sbgn;

import org.biopax.paxtools.model.level3.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class CommonFeatureStringGenerator implements FeatureDecorator
{
	private static Map<String, String> mapping;
	
	@Override
	public String getStringFor(EntityFeature ef)
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
					return "[" + begin.getSequencePosition() + " - " +
						end.getSequencePosition() + "]";
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
					
					String s = mapping.containsKey(term) ? mapping.get(term) : orig;

					SequenceLocation loc = mf.getFeatureLocation();
					if (loc instanceof SequenceSite)
					{
						SequenceSite ss = (SequenceSite) loc;
						s += "@" + ss.getSequencePosition();
					}

					return s;
				}
			}
		}

		return null;
	}
	
	static
	{
		mapping = new HashMap<String, String>();
		try
		{
			InputStream is = CommonFeatureStringGenerator.class.getResourceAsStream(
				"feature-shorts.txt");

			BufferedReader reader = new BufferedReader(new InputStreamReader(is));

			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				String[] token = line.split("\t");

				if (token.length == 2 && token[1] != null && token[1].length() > 0)
				{
					mapping.put(token[0].replace("\"", "").toLowerCase(),
						token[1].replace("\"", ""));
				}
			}

			reader.close();


		}
		catch(Exception e){e.printStackTrace();}
	}
}
