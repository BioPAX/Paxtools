package org.biopax.paxtools.causality;

import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Couldn't get Giovanni's implementation, so re-implemented pval calculation method by simulation.
 * 
 * @author Ozgun Babur
 */
public class MEMo
{
	private static final Random rand = new Random();

	public void randomize(boolean[][] m)
	{
		for (int k = 0; k < 10000; k++)
		{
			int[] row = selectTwoRows(m);
			int c1 = selectFirstCol(m, row);
			if (c1 < 0) continue;
			int c2 = selectSecondCol(m, row, c1);
			if (c2 < 0) continue;

		}

	}

	private int[] selectTwoRows(boolean[][] m)
	{
		int i = rand.nextInt(m.length);
		int j = i;
		while (i == j) j = rand.nextInt(m.length);
		return new int[]{i, j};
	}
	
	private int selectFirstCol(boolean[][]m, int[] row)
	{
		for (int k = 0; k < 100; k++)
		{
			int i = rand.nextInt(m[row[0]].length);
			if (m[row[0]][i] == true && m[row[1]][i] == false) return i;
		}
		return -1;
	}
	private int selectSecondCol(boolean[][]m, int[] row, int col1)
	{
		for (int k = 0; k < 100; k++)
		{
			int i = rand.nextInt(m[row[0]].length);
			if (i != col1 && m[row[0]][i] == false && m[row[1]][i] == true) return i;
		}
		return -1;
	}

	@Test
	@Ignore
	public void SIFStat() throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader("/home/ozgun/Desktop/SIF.txt"));
		
		Set<String> genes = new HashSet<String>();
		int scCnt = 0;
		int trCnt = 0;

		for (String line = reader.readLine(); line != null; line = reader.readLine())
		{
			String[] token = line.split("\t");
			
			if (token[1].equals("BINDS_TO")) continue;
			if (token[1].equals("TRANSCRIPTION")) continue;
			else if (token[1].equals("STATE_CHANGE")) scCnt++;
			else if (token[1].equals("TRANSCRIPTION")) trCnt++;


			genes.add(token[0]);
			genes.add(token[2]);
		}

		reader.close();

		System.out.println("trCnt = " + trCnt);
		System.out.println("scCnt = " + scCnt);
		System.out.println("genes = " + genes.size());
	}
}
