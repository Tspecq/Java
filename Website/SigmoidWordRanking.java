/*

'''
Name:        Make Ranking of the explanations

Author:      Thomas Specq
Website:     http://www.thomas-specq.work
Link:        <a href="http://www.thomas-specq.work">Freelance Web Design & Développement</a>
Created:     24/08/2017
Copyright:   (c) Thomas Specq 2017
Licence:     BSD

'''
*/

package com.eris.erisclassification;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.analysis.function.Sigmoid;

//Sigmoid ranking test
public class SigmoidWordRanking {

	public static double grade;

	public static String alternative;

	public static HashMap<String, Double> wordTotalScore;

	//
	public static HashMap<String, Double> wordAlterScore(String alternative,
			double grade) {
		ArrayList<String> wordArray = CountWords.arrayText(alternative);
		HashMap<String, Double> wordAlterScore = new HashMap<String, Double>();
		for (int i = 0; i < wordArray.size(); i++) {
			String key = wordArray.get(i);
			if (!wordAlterScore.containsKey(key)) {
				wordAlterScore.put(key, grade);
			}
		}
		return wordAlterScore;
	}

	//
	public static HashMap<String, Double> englishWordMap(File listOfEngWord) {
		return CountWords.wordList(listOfEngWord);
	}

	//
	public static HashMap<String, Double> wordTotalScore(
			HashMap<String, Double> wordAlterScore,
			HashMap<String, Double> wordTotalScore) {
		for (String key : wordAlterScore.keySet()) {
			if (wordTotalScore.containsKey(key)) {
				double normScore = (wordAlterScore.get(key) - 2.5)
						+ wordTotalScore.get(key);
				wordTotalScore.put(key, normScore);
			}
		}
		return wordTotalScore;
	}

	//
	public static double sigmoGrade(double grade) {
		Sigmoid sg = new Sigmoid(-50, 50);
		return sg.value(grade);
	}

	//
	public static HashMap<String, Double> wordTotalSigmo(
			HashMap<String, Double> wordTotalScore) {
		for (String key : wordTotalScore.keySet()) {
			double sigmoidScore = sigmoGrade(wordTotalScore.get(key));
			wordTotalScore.put(key, sigmoidScore);
		}
		return wordTotalScore;
	}

	//
	public static double sigmoScore(HashMap<String, Double> wordTotalSigmo,
			String alternative) {
		double SigmoScore = 0;
		ArrayList<String> wordArray = CountWords.arrayText(alternative);
		for (int i = 0; i < wordArray.size(); i++) {
			String key = wordArray.get(i);
			if (wordTotalSigmo.containsKey(key)) {
				SigmoScore += wordTotalSigmo.get(key);
			}
		}
		SigmoScore = SigmoScore / (wordArray.size());
		return SigmoScore;
	}

	//
	public static void updateSer(String alterPath, String wordListSer,
			double score) throws Exception {

		if (score < 6) {
			if (score >= 0) {
				File alterFile = new File(alterPath);
				File wordListFile = new File(wordListSer);
				String alterString = CountWords.getContents(alterFile);

				HashMap<String, Double> mapWordList = MapUtils
						.mapToRead(wordListSer);
				HashMap<String, Double> wordAlterScore = SigmoidWordRanking
						.wordAlterScore(alterString, score);
				HashMap<String, Double> wordTotalScore = SigmoidWordRanking
						.wordTotalScore(wordAlterScore, mapWordList);

				if (wordListFile.exists()) {
					wordListFile.delete();
					MapUtils.createSer(wordTotalScore, wordListFile);
				} else
					MapUtils.createSer(wordTotalScore, wordListFile);
			}
		} else
			System.out.println("The Score must be between 0 and 5");

	}

}
