/*

'''
Name:        Get Score of an explanation

Author:      Thomas Specq
Website:     http://www.thomas-specq.work
Link:        <a href="http://www.thomas-specq.work">Freelance Web Design & Développement</a>
Created:     24/08/2017
Copyright:   (c) Thomas Specq 2017
Licence:     BSD

'''
*/

package com.eris.erisclassification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eris.serialMapWiki.SerialMapWiki;
import com.eris.serialMapWiki.SerialMapWikiRepository;

import edu.cmu.lti.jawjaw.JAWJAW;
import edu.cmu.lti.jawjaw.pobj.POS;

@Component
public class GetScore {

	@Autowired
	private SerialMapWikiRepository serialMapWikiRepository;
	
	// Calculate the score of an alternative including just 1 discipline in the
	// equation
	public double getMapScore(String content, String Discipline)
			throws Exception {

		// Get the serial hashmap and transform it to the correct hashmap in
		// order to be processed
		
		SerialMapWiki serialMapWiki = serialMapWikiRepository.findByDiscipline(Discipline);
		
		// Get the word frequency of the text submitted
		HashMap<String, Double> mapText = CountWords.wordFreq(content);

		// Find the maximum value of the total parameter estimates
		double maxValue = (double) new TreeSet(serialMapWiki.getKeyMap().values()).last();

		// This overValue will be used lated in order to add a value to the
		// attribute of WNet
		double overValue = (maxValue) * 0.75;

		ArrayList<String> arrHypo = getHyponyms(Discipline);
		HashMap<String, Double> keyMap = new HashMap<String, Double>();
		{
			// Get the KeyWords in the Text document and outcome its parameters
			// values according to Weka
			for (Map.Entry<String, Double> entry : mapText.entrySet()) {
				if (serialMapWiki.getKeyMap().containsKey(entry.getKey())) {
					keyMap.put(entry.getKey(), serialMapWiki.getKeyMap().get(entry.getKey()));
				}

				// Overvalue in case of hyponyms
				if (arrHypo.contains(entry.getKey())) {
					keyMap.put(entry.getKey(), overValue);
				}
			}
		}

		// Return the square score of a Text by summing all of its squared
		// parameter divided by the numbers of KeyWord, total multiplied by 10
		// 000 for a better readibility
		double MapScore = 0;
		for (Map.Entry<String, Double> entry : keyMap.entrySet()) {
			MapScore += ((1 + entry.getValue()) * (1 + entry.getValue()) - 1);
		}
		MapScore = MapScore / (keyMap.size());
		MapScore = MapScore * 10000;

		return MapScore;
	}

	/*
	 * The score is calculated as the average of the scores by disciplines
	 * divided by the standard deviation if this standard deviation is inferior
	 * or equal to 1, the average will be divided by 1> This calculation method
	 * is done in order to add more value to Text which disciplines involved are
	 * broader (including equally 2 or more disciplines in its approach) instead
	 * of focusing one single discipline . Put null in the Disciplines that are
	 * not used, put the disciplines in ascending order from 1 to 3
	 */
	public double getTotalScore(String content, String Discipline1,
			String Discipline2, String Discipline3) throws Exception {

		if (Discipline1 != null) {
			if (Discipline2 != null) {
				if (Discipline3 != null) {

					double score1 = getMapScore(content, Discipline1);
					double score2 = getMapScore(content, Discipline2);
					double score3 = getMapScore(content, Discipline3);

					double avScore3 = (score1 + score2 + score3) / 3;
					double stdDev3 = Math.sqrt((Math
							.pow((score1 - avScore3), 2)
							+ Math.pow((score2 - avScore3), 2) + Math.pow(
							(score3 - avScore3), 2)) / 3);
					if (stdDev3 <= 1) {
						return score1 * score2 * score3;
					} else {
						return (score1 * score2 * score3 / Math.sqrt((Math.pow(
								(score1 - avScore3), 2)
								+ Math.pow((score2 - avScore3), 2) + Math.pow(
								(score3 - avScore3), 2)) / 3));
					}
				}
			}
		}

		if (Discipline1 != null) {
			if (Discipline2 != null) {

				double score1 = getMapScore(content, Discipline1);
				double score2 = getMapScore(content, Discipline2);
				double avScore2 = (score1 + score2) / 2;
				double stdDev2 = Math
						.sqrt((Math.pow((score1 - avScore2), 2) + Math.pow(
								(score2 - avScore2), 2)) / 2);
				if (stdDev2 <= 1) {
					return score1 * score2;
				} else {
					return score1
							* score2
							/ Math.sqrt((Math.pow((score1 - avScore2), 2) + Math
									.pow((score2 - avScore2), 2)) / 2);
				}
			}
		}

		if (Discipline1 != null) {
			double score1 = getMapScore(content, Discipline1);
			return score1;
		}
		return 0;
	}

	//
	// ArrayList of the mapHypo element *not used at the moment*
	private static ArrayList<String> arrayHypo(File hypoFile) {
		ArrayList<String> arrayHypo = new ArrayList<String>();

		StringBuffer contents = new StringBuffer();
		try {
			BufferedReader input = new BufferedReader(new FileReader(hypoFile));
			try {
				String line1 = null; // not declared within while loop

				while ((line1 = input.readLine()) != null) {
					if (line1.contains("} ")) {
						if (line1.contains(" --")) {
							line1 = line1.substring(line1.indexOf("} ") + 2,
									line1.lastIndexOf(" --"));
						}
						contents.append(line1);
						contents.append(System.getProperty("line.separator"));
					}
				}
			} finally {
				input.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		String line2 = contents.toString();
		arrayHypo.add(line2);
		return arrayHypo;
	}

	// Get the Hyponyms relative to a particular disciplines
	public static ArrayList<String> getHyponyms(String word) {
		ArrayList<String> hyponyms = new ArrayList<String>();
		for (POS pos : POS.values()) {
			hyponyms.addAll(JAWJAW.findHyponyms(word, pos));
		}
		return hyponyms;
	}

}
