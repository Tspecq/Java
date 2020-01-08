/*

'''
Name:        Calculate the similarities of 2 explanation

Author:      Thomas Specq
Website:     http://www.thomas-specq.work
Link:        <a href="http://www.thomas-specq.work">Freelance Web Design & Développement</a>
Created:     24/08/2017
Copyright:   (c) Thomas Specq 2017
Licence:     BSD

'''
*/

package com.eris.eriscombination;

import java.util.ArrayList;
import java.util.HashMap;

import com.eris.erisclassification.CountWords;

import edu.cmu.lti.jawjaw.JAWJAW;
import edu.cmu.lti.jawjaw.pobj.POS;

//Calculation of the similarity factor between 2 explanations given by the users
public class SimiExplanation {

	public static ArrayList<String> copyArray(ArrayList<String> arrayText,
			ArrayList<String> arrayHypo) {
		for (int i = 0; i < arrayText.size(); i++) {
			if (!arrayHypo.contains(arrayText.get(i))) {
				arrayText.remove(arrayText.get(i));
			}
		}
		return arrayText;
	}

	public static ArrayList<String> getSynonyms(String word) {
		ArrayList<String> synonyms = new ArrayList<String>();
		for (POS pos : POS.values()) {
			synonyms.addAll(JAWJAW.findSynonyms(word, pos));
		}
		return synonyms;
	}

	public static double explanationScore(String explanation1, String explanation2) {

		HashMap<String, Double> firstFeatures = CountWords
				.countWords(explanation1);
		HashMap<String, Double> secondFeatures = CountWords
				.countWords(explanation2);
		double cosResult1st = CosineSimilarity.calculateCosineSimilarity(
				firstFeatures, secondFeatures);

		if (firstFeatures.equals(secondFeatures)) {
			double cosResult3rd = 1;
			return cosResult3rd;

		} else {

			double maxScore = cosResult1st;

			ArrayList<String> tempKey = new ArrayList<String>();
			for (String key : firstFeatures.keySet()) {
				tempKey.add(key);
			}

			ArrayList<Double> tempValue = new ArrayList<Double>();
			for (String key : firstFeatures.keySet()) {
				tempValue.add(firstFeatures.get(key));
			}

			HashMap<String, Double> firstFeatures2nd = new HashMap<String, Double>();
			HashMap<String, Double> firstFeatures3rd = new HashMap<String, Double>();

			for (int a = 0; a < tempKey.size(); a++) {
				String key = tempKey.get(a);
				if (!key.contains("_")) {

					ArrayList<String> arrSys = getSynonyms(key);
					for (int i = 0; i < arrSys.size(); i++) {
						if (!arrSys.get(i).contains("_")
								&& tempKey.contains(key)) {
							int index1 = tempKey.indexOf(key);
							tempKey.set(index1, null);
							tempKey.set(index1, arrSys.get(i));
						} else {
						}

						for (int j = 0; j < tempKey.size(); j++) {
							if (!firstFeatures2nd.containsKey(tempKey.get(j))) {
								firstFeatures2nd.put(tempKey.get(j),
										tempValue.get(j));
							} else {
								Double updatedValue = firstFeatures2nd
										.get(tempKey.get(j)) + tempValue.get(j);
								firstFeatures2nd.put(tempKey.get(j),
										updatedValue);
							}
						}

						double cosResult2nd = CosineSimilarity
								.calculateCosineSimilarity(firstFeatures2nd,
										secondFeatures);

						if (cosResult2nd <= maxScore) {
							if (tempKey.contains(arrSys.get(i))) {
								int index2 = tempKey.indexOf(arrSys.get(i));
								tempKey.set(index2, null);
								tempKey.set(index2, key);
							}
						} else {
							maxScore = cosResult2nd;
							firstFeatures3rd.clear();
							firstFeatures3rd.putAll(firstFeatures2nd);
						}
						if (a != tempKey.size() - 1) {
							if (firstFeatures3rd.isEmpty()) {
								firstFeatures3rd.putAll(firstFeatures2nd);
							}
						}
						firstFeatures2nd.clear();
					}
				}
			}

			if (firstFeatures3rd.isEmpty()) {
				firstFeatures3rd.putAll(firstFeatures);
			}
			double cosResult3rd = CosineSimilarity.calculateCosineSimilarity(
					firstFeatures3rd, secondFeatures);
			return cosResult3rd;
		}
	}
}
