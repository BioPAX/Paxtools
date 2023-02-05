package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.level2.conversion;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;

import java.util.Map;
import java.util.Set;

/**
 * Encapsulation of scores of conversions, and related information
 */
public class ConversionScore implements Comparable
{
	private conversion conv1, conv2;
	private Double score;
	private boolean reverseMatch;
	private Map<physicalEntityParticipant, physicalEntityParticipant> pepMap;

	public ConversionScore(conversion conv1, conversion conv2, Double score,
	                       Map<physicalEntityParticipant, physicalEntityParticipant> pepMap,
	                       boolean reverseMatch)
	{
		this.conv1 = conv1;
		this.conv2 = conv2;
		this.score = score;
		this.reverseMatch = reverseMatch;
		this.pepMap = pepMap;
	}

	public Double getScore()
	{
		return score;
	}

	public conversion getConversion1()
	{
		return conv1;
	}

	public conversion getConversion2()
	{
		return conv2;
	}

	public boolean isReverseMatch()
	{
		return reverseMatch;
	}

	public physicalEntityParticipant getMatch(physicalEntityParticipant pep1)
	{
		return pepMap.get(pep1);
	}

	public Set<physicalEntityParticipant> getMatchedPEPs()
	{
		return pepMap.keySet();
	}

	public int compareTo(Object obj)
	{
		return ((int) Math.signum(
				this.getScore() - ((ConversionScore) obj).getScore()));
	}
}