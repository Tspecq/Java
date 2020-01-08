/*
'''
Name:        Main class crawler

Author:      Thomas Specq
Website:     http://www.thomas-specq.work
Link:        <a href="http://www.thomas-specq.work">Freelance Web Design & Développement</a>
Created:     24/08/2017
Copyright:   (c) Thomas Specq 2017
Licence:     BSD

'''
*/

package crawler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;


public class Main {
	
	public static String seedUrl;
	
	public static void main(String[] args) throws FileNotFoundException{
	 // TODO Auto-generated method stub

	  ParseData pd = new ParseData(); 
	  
	  /**
		* Liste venant du fichier texte contenant les url des pepinieres
		*/
	  ArrayList<String>seedUrlList  = new ArrayList<String>();
	  
	  /**
		* Nous uploadons la liste des urls depuis le fichier texte nomme pepiniere.txt dans une variable seedUrlList
		*/
	  Scanner s = new Scanner(new File("/home/thomas/Desktop/pepiniere.txt"));
	  while (s.hasNext()){
		  seedUrlList.add(s.next());
	  }
	  s.close();
	  
	 /**
	 * Chaque URL de la liste serat prise en compte a tout de role et crawler grace a notre programme
	 * Nous allons formatter ces liens afin de les rendre operable par notre crawler, rajouter http:// si non-present.
	 */
	  for (int i = 0; i < seedUrlList.size(); i++) {
	  seedUrl = seedUrlList.get(i);

	  System.out.println(seedUrl);
	  
	  /**
		 * Lancement du crawler. S il nous est possible de nous connecter a l'URL (si testContent(seedUrl)==true) alors nous continuons.
		 */
	  if(pd.testContent(seedUrl)==true){
	  pd.launchCrawler(seedUrl);
	  }
	  }
	  
	}
	}
