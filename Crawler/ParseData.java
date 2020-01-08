/*
'''
Name:        Crawler functions
Website:     http://www.thomas-specq.work
Link:        <a href="http://www.thomas-specq.work">Freelance Web Design & Développement</a>
Author:      Thomas Specq

Created:     24/08/2017
Copyright:   (c) Thomas Specq 2017
Licence:     BSD

'''
*/

package crawler;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ParseData {
	
	/**
	 * 'linkList' inclue l'ensemble des urls que nous allons crawler par la suite.
	 */	
	public ArrayList<String> linkList = new ArrayList<String>();
	
	/**
	 * 'emailList' inclue la liste des e-mails que nous avons deja crawle (des pages web precedente). 
	 */	
	public ArrayList<String> emailList = new ArrayList<String>();
	
	/**
	 * 'linkVisited' inclue la liste des web que nous avons deja visite.
	 */	
	public ArrayList<String> linkVisited = new ArrayList<String>();
	
	/**
	 * 'externalWebsite' inclue la liste des sites web externes (avec nom de domaine different du nom de domaine de l url de base),
	 * cette liste inclue l'ensemble des sites web externes que nous avons deja crawle (des pages web precedente).
	 */	
	public ArrayList<String> externalWebsite = new ArrayList<String>();
	
	/**
	 * 'phoneList' inclue la liste des numero de telephone crawle,
	 * cette liste inclue l'ensemble des numero de telephone que nous avons deja crawle (des pages web precedente).
	 */	
	public ArrayList<String> phoneList = new ArrayList<String>();
	
	/**
	 * 'tempEmailList' inclue la liste des e-mails que nous venons de crawler a la page a laquelle nous sommes connecte. 
	 */	
	public ArrayList<String> tempEmailList = new ArrayList<String>();
	
	/**
	 * 'tempExternalWebsite' inclue la liste des sites web externes (avec nom de domaine different du nom de domaine de l url de base),
	 *  cette liste inclue la liste des sites web externes que nous venons de crawler a la page a laquelle nous sommes connectee. 
	 */	
	public ArrayList<String> tempExternalWebsite = new ArrayList<String>();
	
	/**
	 * 'tempPhoneList' inclue la liste des numeros de telephone trouve sur la page. 
	 */	
	public ArrayList<String> tempPhoneList = new ArrayList<String>();
		
	/**
	 * L'url que notre crawler s'apprete a crawler, celle-ci nous vient de la liste 'linkList'. 
	 */	
	public String url;
	
	/**
	 * Nom de domaine de l'URL de base.
	 */	
	public String domain;
	
	/**
	 * Expression reguliere d'un email. ceci nous servira par la suite afin de reperer les e-mails dans les codes sources de la page que nous venons de crawler.
	 */	
	private final String Email_Pattern = "[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+";
	
	/**
	 * Expression reguliere d'un fichier avec extensions. nous servira par la suite afin de normaliser les urls a crawler et retirer celle qui ne peuvent l etre.
	 */	
	private final String Ignore_Suffix_Pattern = "(.*/)*.+\\.(css|js|bmp|gif|jpe?g|JPG|png|pps|tiff?|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf|doc|ppt|rm|smil|wmv|swf|wma|zip|rar|gz)$";
	
	/**
	 * Expression reguliere d'un fichier avec extensions. nous servira par la suite afin de normaliser les urls a crawler et retirer celle qui ne peuvent l etre.
	 */	
	private final String Suffix_Pattern = "[a-zA-Z0-9-]+\\.(css|js|bmp|gif|jpe?g|JPG|png|pps|tiff?|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf|doc|ppt|rm|smil|wmv|swf|wma|zip|rar|gz).+$";
	
	/**
	 * Expression reguliere d'un numero de telephone francais. nous servira par la suite afin de reperer les numeros de telephone sur une page.
	 */	
	private final String Phone_Pattern = "(\\(0\\)\\d{1}|\\d{2})(\\s|\\.|\\-)\\d{2}(\\s|\\.|\\-)\\d{2}(\\s|\\.|\\-)\\d{2}(\\s|\\.|\\-)\\d{2}";
	
	/**
	 * Expression reguliere d'un site web. ceci nous servira par la suite afin de reperer les sites web dans les codes sources de la page que nous venons de crawler.
	 */	
	private final String Website_Pattern = "(http://|https://)+[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+";
	
	/**
	 * Cette map inclurat les telephone, sites web et adresse mail si present dans un rayon de 300 lettres autour du site web.
	 */
	public Map<String, ArrayList<String>> mapData = new HashMap<String, ArrayList<String>>();
	
	/**
	 * Nombre de boucle effectuer par doAll().
	 */
	public Integer loopCount = 0;
	
	/**
	 * Nous indique si la page peut etre crawlee. 
	 */
	public boolean crawlable;
	
	/**
	 * La methode crawlUrl sert a executer l'ensemble des methodes necessaires au bon fonctionnement de notre crawler mais sans toutefois specifier l'url de depart,
	 * pour cela la methode launchCrawler() partir de l'url de base et relancerat notre mnethode crawlUrl() jusqu'a qu'il n'y ait plus d'urls a visiter.
	 * La variable loopCount serat augmente a chaque boucle afin de nous permettre de compter le nombre de celle-ci.
	 */	
	public void crawlUrl (String url) throws IOException{

		getDomainName();
		if(testContent(url)==true){
		getLinkAndMail(url);
		setUrlandRemoveFromLinkList(linkList);
		}
		loopCount ++;
		clearTempList();		
	}
	
	/**
	 * La methode testContent() nous sert a connaitre si la page qui va etre crawle est au format text, html, xml ou xhtml.
	 * Si c'est le cas, la variable crawlable deviendrat true.
	 */	
	public boolean testContent(String url) {
		
		Connection.Response res;
		try {
			res = Jsoup.connect(url).ignoreContentType(true).timeout(10*1000).execute();
			
			/**
			 * ContentType nous indique le type de content de la page auquel nous venons de nous connecter.
			 */	
			String contentType=res.contentType();
			crawlable = false;
			
			/**
			 * Nous ne prenons que les pages ecrites en text, html, xml, xhtml.
			 */
			if(contentType.contains("text")){
				crawlable = true;}
				if(contentType.contains("html")){ 
					crawlable = true;}
					if(contentType.contains("xml")){ 
						crawlable = true;}
						if(contentType.contains("xhtml")){
							crawlable = true;}
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		return crawlable;
	}
	
	/**
	 * Cette methode est utilisee afin de trouver le nom de domaine de notre url de base (seedUrl).
	 */	
	public void getDomainName(){
		
	/**
	 * Nous retirons http://www. ou http:// de notre Url de base.
	 */	
		domain = Main.seedUrl;
	if (Main.seedUrl.startsWith("http://www.")){
		domain = domain.replaceAll("http://www.","");}
	if (Main.seedUrl.startsWith("http://")){
		domain = domain.replaceAll("http://","");}
	}
	
	/**
	 * Cette methode nous permet de crawler une page web et de recuperer les e-mails et site web externes qui nous interesse.
	 * Nos listes vont etre mises a jour:	
	 * 'linkVisited' qui inclue la liste des sites web que nous avons deja visite.
	 * 'linkList' qui inclue l'ensemble des urls que nous allons crawler par la suite, 
	 * 'emailList' qui inclue la liste des e-mails que nous avons deja crawler (inclue celle des pages web precedente), 
	 * 'tempEmailList' qui inclue la liste des e-mails que nous venons de crawler actuellement a la page a laquelle nous sommes connecte, 
	 * 'externalWebsite' qui inclue la liste des sites web externes (avec nom de domaine different du nom de domaine de l url de base),
	 * cette liste inclue l'ensemble des sites web externes que nous avons deja crawle (inclue aussi celle des pages web precedente),
	 * 'tempExternalWebsite' qui inclue la liste des sites web externes (avec nom de domaine different du nom de domaine de l url de base),
	 *  cette liste inclue la liste des sites web externes que nous venons de crawler actuellement a la page a laquelle nous sommes connectee.
	 * 'tempPhoneList' inclue la liste des numeros de telephone trouve sur la page. 
	 * 'phoneList' inclue la liste totale des numeros de telephone crawle.
	 */	
	public void getLinkAndMail(String url) throws IOException{
	
	/**
	* Initialisation du document doc dans lequel nous allons le code source de notre page web.
	*/	
	Document doc = null;
	
	/**
	 * Attente de 0 a 2 seconde de maniere aleatoire afin que le crawler n'adopte pas un comportement de programme effectuant un DDOS et soit moins suspect.
	 */	
	Random rand = new Random();
	int  n = rand.nextInt(2000);
	try {
	    Thread.sleep(n); 
	} catch(InterruptedException ex) {
	    Thread.currentThread().interrupt();}
	
	/**
	 * Le crawler se connecte a l'URL fournis.
	 */	
	doc = Jsoup.connect(url).ignoreContentType(true).timeout(1000000).userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0").ignoreHttpErrors(true).get();
	String total = doc.toString();

	/**
	 * Le document doc2 va analyser le contenu du document doc. 
	 */	
	Document doc2 = Jsoup.parse(total);

	/**
	 * Recherche de tous les liens de type a href dans le document doc2.
	 */
	Elements links = doc2.select("a[href]");
	for (Element link : links) {
		
	/**
	 * Normalisation des URLs, 
	 * 'linkHrefUnformatted' est la variable contenant l'URL non formatee. Si ce lien est compose d'extension non-desiree (ex: .png), nous allons les retirer.
	 * Idem si ce lien est compose de saut de ligne. Une mauvaise url peut poser soucis au connecteur Jsoup.
	 */	
	String linkHrefUnformatted = link.attr("href");
	linkHrefUnformatted = linkHrefUnformatted.toString().replaceAll(Ignore_Suffix_Pattern, "$1").replaceAll("\n", "").replaceAll("\r", "");
	
	String linkHref = "";
	
	/**
	 * Nous allons rechercher toutes les extensions possible au sein des adresses trouvees (ex: .png, .doc) et ceux afin de les exclure par la suite.
	 */	
	Pattern pattern5 = Pattern.compile(Suffix_Pattern);
	Matcher matcher5 = pattern5.matcher(linkHrefUnformatted);
	while (matcher5.find()) {
		
		/**
		* Nous eliminons toute les adresses avec . + extension, ceci afin d eviter tout conflit lors du crawl.
		*/
		linkHrefUnformatted = "";
	}
	
	/**
	 * Nous allons ensuite formatter ces liens afin de les rendre operable par notre crawler, rajouter http:// si non-present.
	 * Rajouter l'url de base si l adresse url commence par /.
	 */
	if (linkHrefUnformatted.startsWith("http://")){
		linkHref = linkHrefUnformatted;}
	else if (linkHrefUnformatted.startsWith("https://")){
		linkHref = linkHrefUnformatted;}
	else if (linkHrefUnformatted.startsWith("www")){
		linkHref = "http://" + linkHrefUnformatted;}
	else if (linkHrefUnformatted.startsWith("/")){
		linkHref = Main.seedUrl + linkHrefUnformatted;}
	
	/**
	 * Si nos URLs ne contiennent pas http:// alors il nous faut les normaliser 
	 * en ajoutant l'url de base, un slash et nos URL non formattee
	 */
	if (!linkHref.contains("http://")){
		if (!linkHref.contains("https://")){
		linkHref = Main.seedUrl +"/"+ linkHrefUnformatted;}}
	
	/** Afin de ne pas prendre d'adresse externe inutile, les plus courantes non en relation avec nos sites de start-ups sont retiree 
	 * (ex: facebook, youtube etc...).
	 */	
	if(!linkHref.contains("facebook")){
		if(!linkHref.contains("linkedin")){
			if(!linkHref.contains("twitter")){
				if(!linkHref.contains("goo.gl")){
					if(!linkHref.contains("google")){
						if(!linkHref.contains("vimeo")){
							if(!linkHref.contains("youtube")){
								if(!linkHref.contains("viadeo")){
	
	/**
	 * Si le lien en question a le meme nom de domaine que celui de notre url de base et ne contient pas d'elements problematiques 
	 * (/download/, /telechargements/, /images/, /ressources/ ces elements laisse a supposer que notre crawler se situe dans un repertoire comportant 
	 * de nombreuses URLs avec extensions non souhaites), alors seulement nous pouvons ajouter ce lien dans la liste des liens que nous allons crawler par la suite.
	 * Seul la liste 'linkList' sera ici mise a jour.
	 */								
	if(linkHref.startsWith(Main.seedUrl)){
			if(!linkHref.contains("download")){
							if(!linkHref.contains("telechargements")){
								if(!linkHref.contains("images")){
									if(!linkHref.contains("ressources")){
										if(!linkHref.contains("attachment")){
											if(!linkHref.contains("format=")){
												if(!linkHref.contains("uploads")){
													if(!linkHref.contains("mailto")){
														if(!linkHref.contains("javascript")){
															if(!linkHref.contains("javaScript")){
																if(!linkHref.contains("video")){
																	if(!linkHref.contains("ovh")){
					
		/**
		* Si tout les filtres ont ete passe, nous pouvons updater notre listes de liens a visiter.
		*/
		linkList.add(linkHref);
			}}}}}}}}}}}}
	
	 /**
	 * Si l'url en question ne fait pas partie du meme domaine que celui que nous crawlons, alors celle-ci peut faire partie des sites web externes.
	 * Nous recherchons ici les sites web des start-ups.
     */  
	else{
			
		  /**
			 * Nous recherchons egalement parmis les liens les adresses URL de site externes car ceci peuvent inclure les sites web des 
			 * start-ups que nous recherchons. Les 2 listes 'externalWebsite' et 'tempExternalWebsite' seront mise a jour.
		   * Nous pouvons filtrer et retirer les URL contenant d'autres elements HTML non en relation avec nos sites
		   * (ex: mailto, #, javascript:).
		*/
									if(!linkHref.contains(domain)){
										if(!linkHref.contains("mailto")){
											if(!linkHref.contains("#")){
												if(!linkHref.contains("javascript:")){
													if(!linkHref.contains("javaScript:")){
																											
		/**	
		* L'adresse externe en question doit forcement contenir un dot.
		*/										
														if(linkHref.contains(".")){
													
		/**	
		 * Si l'adresse externe ne fait pas partie de la liste des adresses URL externes deja crawle, alors nous pouvons l'ajouter a notre liste. 
		*/
													if (!externalWebsite.contains(linkHref)){
			
			
				/**
				* Si tout les filtres ont ete passe, nous pouvons updater nos listes de liens externes.
				*/
		  externalWebsite.add(linkHref);
		  tempExternalWebsite.add(linkHref);
		}}}}}}}}}}}}}}}}}}
	
	/**
	 * La methode getMail va crawler l'ensemble des codes sources de l'url afin de retrouver des adresses e-mails potentielles.
	 * Celle-ci auront la meme expression reguliere que notre regex Email_Pattern.
	 */
	getMail(doc2.toString());

	/**
	 * La methode getPhone va crawler l'ensemble des codes sources de l'url afin de retrouver les numeros de telephone Francais potentiel.
	 * Ceux-ci auront la meme expression reguliere que notre regex Phone_Pattern.
	 */
	getPhone(doc2.toString());
	
	/**
	 * Nos listes ayant ete mise a jour, nous allons retirer tout les doublons de celle-ci.
	 */
	Set<String> set1 = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
	set1.addAll(linkList);
	linkList = new ArrayList<String>(set1);
	
	Set<String> set2 = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
	set2.addAll(emailList);
	emailList = new ArrayList<String>(set2);
	
	Set<String> set3 = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
	set3.addAll(externalWebsite);
	externalWebsite = new ArrayList<String>(set3);
	
	Set<String> set4 = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
	set4.addAll(phoneList);
	phoneList = new ArrayList<String>(set4);
	
	/**
	 * Afin de nous focaliser sur les pages qui nous interesse vraiment, nous mettons en haut du classement de liste les sites a visiter en priorite, 
	 * celle contenant a titre d'exemple les mots-cles : entrepreneur, start-up, partenaire, createur, pousse, startup, contact, portfolio...
	 */
	for(int i=0; i<linkList.size(); i++){
		String temp = linkList.get(i);
		if(temp.contains("entrepreneur")||temp.contains("start-up")||temp.contains("incub")||temp.contains("societe")||temp.contains("projet")||temp.contains("partenaire")||temp.contains("pousse")||temp.contains("createur")||temp.contains("startup")||temp.contains("contact")|temp.contains("contact")||temp.contains("portfolio")||temp.contains("entreprise")||temp.contains("resident")||temp.contains("portfolio")||temp.contains("entreprise")||temp.contains("project")){
			linkList.remove(i);
			linkList.add(0, temp);				
		}}}

	/**
	 * La methode suivante nous sert a designer la nouvelle url que notre programme va crawler et a mettre a jour les listes 'linkVisited' et 'linkList'. 
	 */
	public void setUrlandRemoveFromLinkList(ArrayList<String> linkList){
		
		/**
		 * Afin de ne pas crawler 2 fois la meme page de base, celle ci est directement ajouter dans notre liste des pages visites.
		 */	
		linkVisited.add(Main.seedUrl);
		
		/**
		 * Dans notre liste des pages URL a visiter 'linkList', 
		 * nous recherchons un lien URL qui ne fait pas partie des pages deja visitees (la liste 'linkVisited'). 
		 * Une fois trouve, celle-ci devient la future url que notre programme va crawler.
		 * Nous pouvons deja ajouter celle-ci a notre liste des pages visitees.
		 * Nous pouvons ensuite retirer de notre liste des liens a visiter 'linkList', la liste des pages deja visitees.
		 * La boucle peut ensuite cesser.
		 */	
		for(int i=0; i<linkList.size(); i++){
		if(!linkVisited.contains(linkList.get(i))){
		this.url = linkList.get(i);
		linkVisited.add(linkList.get(i));
		linkList.removeAll(linkVisited);
		break;
		}}}
	
	/**
	 * Cette methode nous indique si la liste des liens a visiter est vide ou non.
	 */	
	public boolean emptyList(){
		if(linkList.isEmpty()){
			return true;
		}
		else
		{
		    return false;
		}}
	
	/**
	 * Methode utilisee pour nettoyer les listes temporaires d'emails et de sites web externes suite au crawl d'une page. 
	 */	
	public void clearTempList(){
		tempEmailList.clear();
		tempExternalWebsite.clear();
		tempPhoneList.clear();
	}
	
	/**
	 * Methode utilisee afin de recuperer les e-mails. 
	 */
	public void getMail(String doc2){
		
		/**
		 * Nous allons utiliser un 'matcher' afin de reperer les adresses e-mails dans le document doc2' 
		 * les 2 listes emailList et tempEmailList sont mise a jour avec le resultat de nos match.  
		 */
		Pattern p = Pattern.compile(Email_Pattern, Pattern.CASE_INSENSITIVE);
		Matcher matcher = p.matcher(doc2.toString());
		while (matcher.find()) {
			
			/**
			 * Nous prenons les adresses mails ayant un nom de domaine different du site web que nous venons de crawler. 
			 */
			String temp1 = matcher.group().toString();
			if(!temp1.contains(domain)){
				
				/**
				 * Nous prenons les adresses mails ne faisant pas deja partie de la liste des emails que nous avons deja crawle.
				 */
				if (!emailList.contains(temp1)){
					
			/**
			 * Lancement de la methode updateMapData() consistant a recueiller des informations utiles autour de notre e-mail trouve
			 * Celle-ci chercherat dans les 800 caracteres avant la decouverte de notre e-mail et 400 caractere apres (estime pas essai et erreur).
			*/	
			
			if(matcher.start()-800>=0){
				if(matcher.end()+400<doc2.length()){
			String limitedDoc = doc2.substring(matcher.start()-800, matcher.end()+400);
			updateMapData(limitedDoc);
			}}
			
			/**
			* Si tout les filtres ont ete passe, nous pouvons updater nos listes d'e-mails.
			*/
			emailList.add(matcher.group());
			tempEmailList.add(matcher.group());
		}}}
		
	}
	
	/**
	 * Methode utilise afin de recuperer les numero de telephone. 
	 */
	public void getPhone(String doc2){
		Pattern pattern1 = Pattern.compile(Phone_Pattern);
		Matcher matcher1 = pattern1.matcher(doc2.toString());
		while (matcher1.find()) {
			
			/**
			 * Lancement de la methode updateMapData() consistant a recueiller des informations utiles autour de notre e-mail trouve.
			 * Celle-ci chercherat dans les 800 caracteres avant la decouverte de notre e-mail et 400 caractere apres (estime pas essai et erreur).
			*/
			if(matcher1.start()-800>=0){
				if(matcher1.end()+400<doc2.length()){

			String limitedDoc1 = doc2.substring(matcher1.start()-800, matcher1.end()+400);
			updateMapData(limitedDoc1);
			}}
			
			/**
			* Si tout les filtres ont ete passe, nous pouvons updater nos listes de telephone.
			*/
			phoneList.add(matcher1.group());
			tempPhoneList.add(matcher1.group());
			}
	}
	
	/**
	 * Lorsqu'un e-mail ou numero de telephone a ete trouve dans la methode getPhone() ou getMail(),
	 * Une nouvelle variable String fut cree, prenant les 800 caracteres avant la decouverte et 400 caractere apres la decouverte.
	 * Le but de cette methode est de rechercher des patterns de sites web, e-mails ou numero de telephone dans cet espace.
	 */
	public void updateMapData(String limitedDoc){
		
		/**
		 * Initialisation des variables.
		 */
		String phone = "";
		String mail = "";
		String website = "";
		
		/**
		 * Recherche de toute les expressions regulieres de numero de telephone dans l'echantillon choisi.
		 */
		Pattern pattern3 = Pattern.compile(Phone_Pattern);
		Matcher matcher3 = pattern3.matcher(limitedDoc);
		while (matcher3.find()) {
			
			/**
			* Si un numero de telephone fut trouve, nous allons l'ajouter a la variable phone. 
			*/
			phone = matcher3.group().toString();
			}
		
		/**
		 * Recherche de toute les expressions regulieres d'e-mails dans l'echantillon choisi.
		 */
		Pattern p4 = Pattern.compile(Email_Pattern, Pattern.CASE_INSENSITIVE);
		Matcher matcher4 = p4.matcher(limitedDoc);
		while (matcher4.find()) {
			
			/**
			* Si un e-mail fut trouve, nous allons l'ajouter a la variable mail. 
			*/
			mail = matcher4.group().toString();
			}
		
		/**
		 * Recherche de toute les expressions regulieres de site web dans l'echantillon choisi.
		 */
		Pattern p5 = Pattern.compile(Website_Pattern, Pattern.CASE_INSENSITIVE);
		Matcher matcher5 = p5.matcher(limitedDoc);
		while (matcher5.find()) {
				if(matcher5.group().toString().contains(domain)){
				}else{
					if(!matcher5.group().toString().contains("facebook")){
						if(!matcher5.group().toString().contains("linkedin")){
							if(!matcher5.group().toString().contains("twitter")){
								if(!matcher5.group().toString().contains("goo.gl")){
									if(!matcher5.group().toString().contains("google")){
										if(!matcher5.group().toString().contains("vimeo")){
											if(!matcher5.group().toString().contains("youtube")){
												if(!matcher5.group().toString().contains("viadeo")){
													
			/**
			* Si un site web fur trouve et qu il ne fait partie des sites populaires de reseau sociaux, alors il est ajoute. 
			*/
			website = matcher5.group().toString();}										
		}}}}}}}}}

		/**
		* Une fois toute les variables trouvees nous pouvons mettre a jour la Map les incluant (si elle ne sont deja presente).
		* Cette methode nous permet d associer plus facilement les numero de telephone, site web et telephone entre eux.
		*/
		
		Random rand = new Random();
		int  n = rand.nextInt(998) + 1;
		

        if(!phoneList.contains(phone)){
        	if(!emailList.contains(mail)){
        		if(url!=null){
        
		if (website.isEmpty()){
		website = loopCount.toString()+"-"+n;
		mapData.put(website, new ArrayList<String>(Arrays.asList(mail, phone, url)));
		
		}else
		if (!mapData.containsKey(website)){
		mapData.put(website, new ArrayList<String>(Arrays.asList(mail, phone, url)));}	
		
	}}}}
	
	/**
	* ClearAllList nous permet de vider toutes les listes.
	*/	
	 public void clearAllList(){
		 phoneList.clear();
		emailList.clear();
		externalWebsite.clear();
		linkList.clear();
		mapData.clear();	 
	 }
	
	/**
	* La methode permettant de lancer le crawler a partir d'une URL fournie.
	*/	
	public void launchCrawler(String seedUrl){
		
		 try {
			 		  
		/**
		* Nous tentons de nous connecter une 1ere fois a l url de base  afin de verifier s il existe une redirection ou non.
		*/	
		Response response = Jsoup.connect(seedUrl).followRedirects(true).timeout(1000000).execute();	
		
		/**
		* Si nous sommes redirige, nous allons verifier que la nouvelle URL ne se termine par / (et retirons ce / si c'est le cas),
		* l'adresse de redirection deviendrat alors la nouvelle adresse de base 'seedUrl'.
		*/	
		if(response.url().toString().endsWith("/")){
		seedUrl = response.url().toString().substring(0, response.url().toString().length() - 1);
		}else{seedUrl = response.url().toString();}
		
		/**
		* Lancement du premier crawl sur l'URL de base. La liste des URL a crawler par la suite etant vide, nous commencons avec celle-ci
		* la 2eme utilisation de la methode crawlUrl se ferat sur la liste des URL crawlee.
		*/
		crawlUrl(seedUrl);
	
		/**
		* Calcul du temps de debut de crawl de l'URL et de sa fin (4 min plus tard).
		*/
		long startTime = System.currentTimeMillis();
		long endTime = startTime + 400000;
		
		/**
		* Suite au 1er crawl, la liste des liens a visiter a du s'agrandir. Nous lancons donc le crawl systematique de toutes les adresses URLs 
		* inclus dans cette liste jusqu'a ce que la liste se vide completement ou que l'on soit arrive a la fin du temps imparti.
		*/
		while(emptyList()==false){
			if(System.currentTimeMillis() <= endTime){
		crawlUrl(url);

		}else break;
		}
		
		/**
		* Nous retirons de la liste externalWebsite toute occurence presente dans notre mapData.
		*/
		for(int i=0; i<externalWebsite.size(); i++){
		if(mapData.containsKey(externalWebsite.get(i))){
			externalWebsite.remove(i);				
		}
		}

			/**
			* Nous allons ecrire le resultat du crawl (la variable mapData) dans un fichier texte helloWorld.
			*/
			try(  PrintWriter out = new PrintWriter( new FileWriter("/home/thomas/Desktop/helloWorld.txt", true ))  ){
				out.println("Domain Crawled : "+seedUrl);
			    out.println(mapData);
			    out.println();
			    
			    /**
				* Au cas ou la mapData est trop petite, nous pouvons egalement ecrire les sites web externes crawle.
				*/
			    if(mapData.size()<4){
			    out.println("Website Crawled : "+seedUrl);
			    out.println(externalWebsite);
			    out.println();	}
			}
			
		/**
		* Une fois ceci fait, nous pouvons vider la liste mapData. linkList et externalWebsite pour le prochain crawl.
		*/
		clearAllList();
		  } 
		  
		  catch (InterruptedIOException iioe)
		  {
				   System.err.println ("Remote host timed out during read operation");
				   
				   /**
					* Une fois ceci fait, nous pouvons vider la liste mapData. linkList et externalWebsite pour le prochain crawl.
					*/
				   clearAllList();
		  }
				// Exception thrown when general network I/O error occurs
		  catch (IOException ioe)
		  {
			 System.err.println ("Network I/O error - " + ioe);
			 
			 /**
				* Une fois ceci fait, nous pouvons vider la liste mapData. linkList et externalWebsite pour le prochain crawl.
				*/
			 clearAllList();
		  } catch (Exception e) 
		  {
			  System.err.println ("error " + e);
			  
			  /**
				* Une fois ceci fait, nous pouvons vider la liste mapData. linkList et externalWebsite pour le prochain crawl.
				*/
			  clearAllList();	      }
	}
	}
	


