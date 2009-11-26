package org.biopax.paxtools.model.level2;

import java.util.ArrayList;
import java.util.Set;


/**
 * User: demir Date: Nov 13, 2007 Time: 9:52:47 PM
 */
class InteractionHelper
{
	public static openControlledVocabulary inferLocation(
		interaction interaction)
	{
		// I do not want to intialize a map for such a simple operation so
		// I will use two array lists instead


		ArrayList<openControlledVocabulary> ocvs = new ArrayList<openControlledVocabulary>(5);
		ArrayList<Integer> counts = new ArrayList<Integer>(5);

		Set<InteractionParticipant> ips =
			interaction.getPARTICIPANTS();
		for (InteractionParticipant ip : ips)
		{

			if (ip instanceof physicalEntityParticipant)
			{
				openControlledVocabulary ocv =
					((physicalEntityParticipant) ip).getCELLULAR_LOCATION();
				int i = ocvs.indexOf(ocv);
				if(i==-1)
				{
					counts.add(1);
					ocvs.add(ocv);

				}
				else
				{
					counts.add(i, counts.get(i) + 1);
				}
			}

		}
		int max = -1;
		for (int count : counts)
		{
			if(count>max)
			{
				max = count;
			}
		}
		int maxIndex = counts.indexOf(max);
		if(maxIndex == -1)
		{
			return null;
		}
		else
		{
			return ocvs.get(maxIndex);
		}
	}
}
