package wyszukiwarka;

import indekser.*;

import java.util.*;

/**
 * Klasa umożliwiająca zadawanie zapytań
 */
public class Wyszukiwarka {
	private static IndexOperator index;
	
	public static void main(String[] args) {
		String lang = "en";
		String queryType = "term";
		String line[];
		String query;
		boolean details = false;
		boolean color = false;
		int limit = Integer.MAX_VALUE;
		
		String dir = System.getProperty("user.home");
		dir += "/.index";
		index = new IndexOperator (dir);
		
		Scanner scan = new Scanner (System.in);
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				index.closeIndexModifiers();
			}
		});
		
		while (true) {
			System.out.print("> ");
			line = scan.nextLine().split(" ");
			if (line[0].charAt(0) == '%') {
				if (line[0].equals("%limit")) {
					limit = Integer.valueOf(line[1]);
					if (limit == 0)
						limit = Integer.MAX_VALUE;
				}
				else if (line[0].equals("%lang")) {
					lang = line[1];
				}
				else if (line[0].equals("%details")) {
					if (line[1].equals("on"))
						details = true;
					else
						details = false;
				}
				else if (line[0].equals("%color")) {
					if (line[1].equals("on"))
						color = true;
					else
						color = false;
				}
				else if (line[0].equals("%phrase")) {
					queryType = "phrase";
				}
				else if (line[0].equals("%fuzzy")) {
					queryType = "fuzzy";
				}
				else if (line[0].equals("%term")) {
					queryType = "term";
				}
				else {
					System.out.println("Wrong command");
				}
			}
			else {
				if (queryType.equals("phrase")) {
					query = line[0];
					for (int i = 1; i < line.length; i++) {
						query += " " + line[i];
					}
				}
				else {
					query = line[0];
				}
				doQuery (lang, queryType, details, color, limit, query);
			}
		}
	}

	/**
	 * Wywoływanie odpowiedniego zapytania na indexie
	 * 
	 * @param lang - język zapytania
	 * @param queryType - typ wyszukiwania
	 * @param details - określa czy szczegóły są wyświetlane
	 * @param color - określa czy szukany tekst ma być wyświetlony na czerowno
	 * @param limit - limit znalezionych wyników
	 * @param query - wyszukiwane słowo lub fraza
	 */
	private static void doQuery (String lang, String queryType, boolean details, boolean color, int limit, String query) {
			if (queryType.equals("term")) {
				index.termQuerry(limit, lang, query, details, color);
			}
			else if (queryType.equals("fuzzy")) {
				index.fuzzyQuerry(limit, lang, query, details, color);
			}
			else if (queryType.equals("phrase")) {
				index.phraseQuerry(limit, lang, details, color, query);
			}
	}
}
