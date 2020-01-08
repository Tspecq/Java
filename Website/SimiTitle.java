/*

'''
Name:        Calculate the similarities of 2 titles

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
import java.util.List;

import com.eris.erisclassification.CountWords;
import com.eris.erisclassification.Inflector;

import edu.cmu.lti.RelatednessCalculator;
import edu.cmu.lti.WuPalmer;
import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

//Calculation of the similarities between titles
public class SimiTitle {

	private static ILexicalDatabase db = new NictWordNet();

	private static RelatednessCalculator rc = new WuPalmer(db);

	public static double titleScore(String title1, String title2) {

		Inflector inf = new Inflector();
		WS4JConfiguration.getInstance().setMFS(true);

		ArrayList<String> titleWord1 = CountWords.arrayText(title1);
		ArrayList<String> titleWord2 = CountWords.arrayText(title2);

		double sum = 0;
		int n = 0;
		double inter = 0;

		for (int i = 0; i < titleWord1.size(); i++) {
			String word1 = inf.singularize(titleWord1.get(i));
			for (int j = 0; j < titleWord2.size(); j++) {
				String word2 = inf.singularize(titleWord2.get(j));

				List<POS[]> posPairs = rc.getPOSPairs();
				double maxScore = -1D;

				for (POS[] posPair : posPairs) {
					List<Concept> synsets1 = (List<Concept>) db.getAllConcepts(
							word1, posPair[0].toString());
					List<Concept> synsets2 = (List<Concept>) db.getAllConcepts(
							word2, posPair[1].toString());

					for (Concept synset1 : synsets1) {
						for (Concept synset2 : synsets2) {
							Relatedness relatedness = rc
									.calcRelatednessOfSynset(synset1, synset2);
							double score = relatedness.getScore();
							if (score <= 1) {
								if (score > maxScore) {
									maxScore = score;
									inter = maxScore;
								}
							}
						}
					}
				}

				if (inter == -1D) {
					inter = 0.0;
				}
			}

			if (inter >= 0.22) {
				sum += inter;
				n++;
			}
		}

		if (sum != 0) {
			return (sum / n);
		} else {
			return 0;
		}
	}

	// Word order score of similarity, only apply if the sentences have the same
	// amount of words
	public static HashMap<String, Integer> sentenceMap(String title1,
			String title2) {

		Inflector inf = new Inflector();
		HashMap<String, Integer> temp = new HashMap<String, Integer>();
		ArrayList<String> titleWord1 = CountWords.arrayText(title1);
		ArrayList<String> titleWord2 = CountWords.arrayText(title2);

		int sizeArr1 = titleWord1.size();
		for (int i = 0; i < sizeArr1; i++) {
			String key1 = inf.singularize(titleWord1.get(i));
			temp.put(key1, i);
		}

		int n = 0;
		for (int j = 0; j < titleWord2.size(); j++) {
			String key2 = inf.singularize(titleWord2.get(j));
			if (temp.containsKey(key2)) {
			} else {
				temp.put(key2, sizeArr1 + n);
				n++;
			}
		}
		return temp;
	}

	public static HashMap<String, Integer> sentenceWordOrder(String title1,
			String title2) {

		Inflector inf = new Inflector();
		HashMap<String, Integer> sentenceMap = sentenceMap(title1, title2);
		HashMap<String, Integer> tempo = new HashMap<String, Integer>();
		ArrayList<String> titleWord1 = CountWords.arrayText(title1);

		for (int i = 0; i < titleWord1.size(); i++) {
			String key = inf.singularize(titleWord1.get(i));
			if (sentenceMap.containsKey(key)) {
				tempo.put(key, sentenceMap.get(key));
			}
		}
		return tempo;
	}

	public static int orderScore(String title1, String title2) {

		HashMap<String, Integer> temp1 = sentenceWordOrder(title1, title2);
		HashMap<String, Integer> temp2 = sentenceWordOrder(title2, title1);
		ArrayList<Integer> valArr1 = new ArrayList<Integer>();
		ArrayList<Integer> valArr2 = new ArrayList<Integer>();
		for (String key : temp1.keySet()) {
			valArr1.add(temp1.get(key));
		}
		for (String key : temp2.keySet()) {
			valArr2.add(temp2.get(key));
		}

		int simi = 0;
		int inter1 = 0;
		int inter2 = 0;
		int size1 = temp1.size();
		int size2 = temp2.size();

		if (size1 == size2) {

			for (int i = 0; i < size1; i++) {
				int value1 = valArr1.get(i);
				int value2 = valArr2.get(i);

				inter1 += (value1 - value2);
				inter2 += (value1 + value2);
			}
			System.out.println(inter1);
			System.out.println(inter2);
			if (inter1 < 0) {
				inter1 = (-inter1);
			}
			if (inter2 < 0) {
				inter2 = (-inter2);
			}
			simi = (1 - (inter1) / (inter2));
			return simi;
		} else {
			return 0;
		}
	}
}
