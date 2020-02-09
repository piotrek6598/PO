package indekser;


import java.util.*;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.lucene.analysis.pl.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.en.*;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.uhighlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;
import static org.fusesource.jansi.Ansi.ansi;

import org.fusesource.jansi.AnsiConsole;



/**
 * Klasa umożliwiająca opecja na indexie.
 * Przechowuje writer'a i reader'a oraz obiekt
 * klasy umożliwiącej ekstrakcję tekstu z dokumentu 
 * Umożliwia zapisywanie i odczytywanie danych z indexu oraz
 * przeprowadzanie zapytań 
 */
public class IndexOperator  {
	private IndexWriter writer;
	private IndexReader reader;
	private TextExtractor textExtractor;
	
	/**
	 * Konstruktor obiektu klasy
	 * 
	 * @param dir String reprezentujacy ścieżkę do indexu
	 */
	public IndexOperator (String dir) {
		textExtractor = new TextExtractor();
		makeIndexWriter(dir);
		makeIndexReader(dir);
	}
	
	/**
	 * Tworzenie analizatora, który analizuje pola w danym jezyku
	 * analizatorem przystosowanym do tego jezyka
	 */
	private PerFieldAnalyzerWrapper makeAnalyzerWrapper () {
		Map <String, Analyzer> analyzerMap = new HashMap<String, Analyzer>();
		analyzerMap.put("path", new StandardAnalyzer());
		analyzerMap.put("name", new StandardAnalyzer());
		analyzerMap.put("contentsPL", new PolishAnalyzer());
		analyzerMap.put("contentsEN", new EnglishAnalyzer());
		analyzerMap.put("watchedPath", new StandardAnalyzer());
		PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(new StandardAnalyzer(), analyzerMap);
		return wrapper;
	}
	
	/**
	 * Tworzenie writer'a w trybie CREATE_OR_APPEND
	 * do indeksu o podanej sciezce.
	 * Jeśli indeks nie istnieje lub nie udało się utworzyć
	 * writer'a wypisuje informację o błędzie oraz kończy działanie
	 * programu
	 * 
	 * @param dir String reprezentujacy sciezke do indexu
	 */
	private void makeIndexWriter (String dir) {
		try {
			Directory indexDir = FSDirectory.open(Paths.get(dir));
			IndexWriterConfig iwc = new IndexWriterConfig (makeAnalyzerWrapper());
			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			writer = new IndexWriter(indexDir, iwc);
			writer.commit();
		}
		catch (IOException e) {
			System.err.println ("Caught a " + e.getClass() +
					"\n with message " + e.getMessage());
			System.exit(1);
		}
	}
	
	/**
	 * Tworzenie reader'a, jeśli nie udało sie utworzyć readera,
	 * wypisuje informacje o błędzie, oraz kończy działanie programu
	 * 
	 * @param dir String reprezentujący ścieżkę do indexu.
	 */
	private void makeIndexReader (String dir) {
		try {
			reader = DirectoryReader.open(writer);
		}
		catch (IOException e) {
			System.err.println("Caught a " + e.getClass() + 
					"\n with message: " + e.getMessage());
			System.exit(1);
		}
	}
	
	/**
	 * Zamykanie writer'a oraz reader'a
	 */
	public void closeIndexModifiers () {
		try {
			writer.close();
			reader.close();
		}
		catch (IOException e) {
			System.err.println("Caught a " + e.getClass() + 
					"\n with message: " + e.getMessage());
		}
	}
	
	/**
	 * Dodawanie pliku do indexu
	 * Jeśli plik jest innego typu niż pliki podlegające indexowaniu
	 * to jest on pomijany
	 * 
	 * @param file Ścieżka pliku
	 */
	public void addFile (Path file) {
		
		// Sprawdzenie czy plik może być indexowany
		if (!isAllowedToIndex (file))
			return;
		if (!Files.isRegularFile(file))
			return;
		
		// Sprawdzenie tekstu pliku
		String lang = textExtractor.detectLanguage(file);
		
		
		// Tworzenie dokumentu na podstawie pliku
		Document newDoc = null;
		try {
			newDoc = makeDoc(file, lang);
		}
		catch (Exception e) {
			return;
		}
		
		// Dodawanie documentu do indeksu
		try {
				writer.updateDocument(new Term ("path", file.toString()), newDoc);
				writer.commit();
		}
		catch (IOException e) {
			System.err.println("Caught a " + e.getClass() + 
					"\n with message " + e.getMessage());
		}
	}
	
	/**
	 * Usuwanie wszystkich dokumentów z indexu
	 */
	public void deleteDocs () {
		try {
			writer.deleteAll();
			writer.commit();
		}
		catch (IOException e) {
			System.err.println("Caught a " + e.getClass() + 
					"\n with message: " + e.getMessage());
		}
	}
	
	/**
	 * Dodawanie katalogu do indexu. Jeśli katalog ma być dodatkowo
	 * obserwowany to parametr newWatchedDir przyjmuje true
	 * 
	 * @param dir Sćieżka katalogu
	 * @param newWatchedDir Okręśla czy katalog ma być obserwowany
	 */
	public void addDir (Path dir, boolean newWatchedDir) {
		// Przechodzenie poddrzewa plików reprezentowane przez katalog
		try {
			walkDir(dir);
		}
		catch (IOException e) {
			return;
		}
		
		// Zapamietywanie sciezki
		if (newWatchedDir) {
			Field watchedPath = new StringField ("watchedPath", "true", Field.Store.YES);
			Field path = new StringField ("path", dir.toString(), Field.Store.YES);
			Document doc = new Document();
			doc.add(path);
			doc.add(watchedPath);
		
			try {
				writer.updateDocument(new Term ("path", dir.toString()), doc);
				writer.commit();
			}
			catch (IOException e) {
				System.err.println("Caught a " + e.getClass() + 
						"\n with messaga: " + e.getMessage());
			}
		}
	}
	
	/**
	 * Usuwanie pliku lub katalogu, parametr removeWatchedDir określa
	 * czy ścieżka ma zostać usunięta z monitorowanych.
	 * Jeśli przekazana została ścieżka reprezentująca plik usuwany
	 * jest tylko dokument reprezentujący plik.
	 * 
	 * @param dir Ścieżka pliku lub katalogu
	 * @param removeWatchedDir Określa czy dalej obserwować ścieżkę
	 */
	public void removeDir (Path dir, boolean removeWatchedDir) {
		// Usuwanie dokumentów
		// Jeśli dir to plik to usuwany jest dokument odpowiadający plikowi
		// Jeśli dir to ścieżka to usuwane są wszystkie dokumenty znajdujące
		// się w tej ścieżce (lub głębiej w poddrzewie plików)
		// Dokumenty są usuwane w ten sposób, że jeśli dir to plik to
		// w drzewie plików istnieje tylko jeden plik dla którego dir
		// jest prefiksem ścieżki, jeśli dir to ścieżka to każdy plik
		// będaćy w poddrzewie plików reprezentowanych przez ścieżkę ma ścieżkę
		// zawierającą prefix dir
		
		Term t = new Term ("path", dir.toString());
		Term t2 = new Term ("watchedPath", "false");
		Query query1 = new PrefixQuery (t);
		Query query2 = new TermQuery (t2);
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		queryBuilder.add(query1, BooleanClause.Occur.MUST);
		queryBuilder.add(query2, BooleanClause.Occur.MUST);
		BooleanQuery query = queryBuilder.build();
		try {
			writer.deleteDocuments(query);
			writer.commit();
			
		}
		catch (IOException e) {
			System.err.println("Caught a " + e.getClass() + 
					"\n with message" + e.getMessage());
		}
		
		// Usuwanie sciezki z obserowowanych
		if (removeWatchedDir) {
			try {
				writer.deleteDocuments(t);
				writer.commit();
			}
			catch (IOException e) {
				System.err.println("Caught a " + e.getClass() + 
						"\n with message: " + e.getMessage());
			}
		}
	}
	
	/**
	 * Reindexowanie plików
	 * Wszystkie dokumenty nie reprezentujące obserwowanych
	 * ścieżek są usuwane, a następnie dodawane są wszystkie
	 * pliki w obserowowanych ścieżkach
	 */
	public void reindex () {
		try {
			Term t = new Term ("watchedPath", "false");
			writer.deleteDocuments(t);
			String[] tab = getWatchedDir();
			for (int i = 0; i < tab.length; i++)
				addDir(Paths.get(tab[i]), false);
		}
		catch (IOException e) {
			System.err.println("Caught a " + e.getClass() + 
					"\n with message" + e.getMessage());
		}
		
	}
	
	/**
	 * Wypisywanie obserwowanych ścieżek
	 */
	public void printWatchedDir () {
		try  {
			Term t = new Term ("watchedPath", "true");
			IndexSearcher searcher = new IndexSearcher(reader);
			Query query = new TermQuery (t);
			TopDocs result = searcher.search(query, Integer.MAX_VALUE);
			ScoreDoc[] docs = result.scoreDocs;
			for (int i = 0; i < docs.length; i++) {
				System.out.println 
				(reader.document(docs[i].doc).getField("path").stringValue());
			}
		}
		catch (IOException e) {
			System.err.println("Caught a " + e.getClass() + 
					"\n with message: " + e.getMessage());
		}
	}
	
	/**
	 * Zwraca tablicę String przechowującą tekstową reprezentację
	 * obserwowanych ścieżek
	 */
	public String[] getWatchedDir() {
		try {
			Term t = new Term ("watchedPath", "true");
			IndexSearcher searcher = new IndexSearcher (reader);
			Query query = new TermQuery(t);
			TopDocs result = searcher.search(query, Integer.MAX_VALUE);
			ScoreDoc[] docs = result.scoreDocs;
			String[] tab = new String[docs.length];
			for (int i = 0; i < docs.length; i++) {
				tab[i] = reader.document(docs[i].doc).getField("path").stringValue();
			}
			return tab;
		}
		catch (IOException e) {
			System.err.println("Caught a " + e.getClass() + 
					"\n with message: " + e.getMessage());
			return null;
		}
	}
	
	/**
	 * Wyszukiwanie termów
	 * 
	 * @param maxResultSize Maksymalna liczba wyników
	 * @param lang Język wyszukiwania
	 * @param phrase - wyszukiwane słowo
	 * @param details - określana czy szczegóły wyniku są wypisywane
	 * @param color - określa czy szukane słowo ma być zaznaczone kolorem
	 */
	public void termQuerry (int maxResultSize, String lang, String phrase, 
			boolean details, boolean color) {
		IndexSearcher searcher = new IndexSearcher (reader);
		Analyzer analyzer;
		String field;
		if (lang.equals("pl")) {
			field = "contentsPL";
			analyzer = new PolishAnalyzer();
		}
		else {
			field = "contentsEN";
			analyzer = new EnglishAnalyzer();
		} 
		QueryParser parser = new QueryParser(field, analyzer);
		Query query;
		try {
			query = parser.parse(phrase);
		}
		catch (ParseException e) {
			System.err.println("Caught a " + e.getClass() + 
					"\n with message: " + e.getMessage());
			return;
		}
		querry (maxResultSize, searcher, query, lang, details, color);
	}
	
	/**
	 * Wyszukiwanie fraz
	 * 
	 * @param maxResultSize Maksymalna liczba wyników
	 * @param lang - Język wyszukiwania
	 * @param details - określa czy szczegóły wyniku są wypisywane
	 * @param color - określa czy szukana fraza ma byc zaznaczona kolorem
	 * @param terms String reprezentujący frazę
	 */
	public void phraseQuerry (int maxResultSize, String lang, boolean details,
			boolean color, String terms) {
		IndexSearcher searcher = new IndexSearcher (reader);
		Query query;
		String field;
		QueryBuilder builder;
		
		if (lang.equals("pl")) {
			builder = new QueryBuilder(new PolishAnalyzer());
			field = "contentsPL";
		}
		else {
			builder = new QueryBuilder(new EnglishAnalyzer());
			field = "contentsEN";
		}
		query = builder.createPhraseQuery(field, terms);
		
		querry (maxResultSize, searcher, query, lang, details, color);
	}
	
	/**
	 * Wyszukiwanie rozmyte
	 * 
	 * @param maxResultSize - Maksymalna liczba wyników
	 * @param lang - Język wyszukiwania
	 * @param phrase - Wyszukiwane słowo
	 * @param details - określa czy szczegóły wyniku są wypisywane
	 * @param color - określa czy szukane słowo ma być zaznaczone kolorem
	 */
	public void fuzzyQuerry (int maxResultSize, String lang, String phrase,
			boolean details, boolean color) {
		Term term;
		if (lang.equals("pl")) {
			term = new Term ("contentsPL", phrase);
		}
		else {
			term = new Term ("contentsEN", phrase);
		}
		IndexSearcher searcher = new IndexSearcher (reader);
		Query query = new FuzzyQuery (term);
		querry (maxResultSize, searcher, query, lang, details, color);
	}
	
	/**
	 * Tworzenie dokumentu na podstawie pliku
	 * 
	 * @param file - Ścieżka reprezentująca plik
	 * @param lang - Język pliku
	 * @return Document reprezentujący plik w indexie
	 * @throws Exception - jeśli nie udało się ekstrakcja tekstu z pliku
	 */
	private Document makeDoc (Path file, String lang) throws Exception {
		Document doc = new Document ();
		String textContents = textExtractor.extractTextFromFile(file);
		
		Field path = new StringField ("path", file.toString(), Field.Store.YES);
		Field name = new StringField ("name", textExtractor.getName(file), Field.Store.YES);
		Field watchedPath = new StringField ("watchedPath", "false", Field.Store.YES);
		Field contentsPL, contentsEN;
		if (lang.equals("pl")) {
			contentsPL = new TextField ("contentsPL", textContents, Field.Store.YES);
			contentsEN = new TextField ("contentsEN", "", Field.Store.YES);
		}
		else {
			contentsPL = new TextField ("contentsPL", "", Field.Store.YES);
			contentsEN = new TextField ("contentsEN", textContents, Field.Store.YES);
		}
		
		doc.add(path);
		doc.add(name);
		doc.add(contentsPL);
		doc.add(contentsEN);
		doc.add(watchedPath);
		
		
		return doc;
	}
	
	/**
	 * Sprawdzenie czy można indeksować plik
	 * 
	 * @param file Ścieżka reprezentująca plik
	 * @return Wartość true lub false informującą czy można indexować plik
	 */
	private boolean isAllowedToIndex(Path file) {
		return textExtractor.isParsingPossible(file);
	}
	
	
	/**
	 * Przechodzenie poddrzewo plików o danej ścieżce
	 * 
	 * @param dir Ścieżka
	 * @throws IOException - jeśli wystąpił bład (np. ścieżka nie istnieje)
	 */
	private void walkDir (Path dir) throws IOException {
		try {
		Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
				addFile(file);
				
				return FileVisitResult.CONTINUE;
			}
		});
		}
		catch (IOException e) {
			System.err.println("Caught a " + e.getClass() + 
					"\n with message: " + e.getMessage());
			throw new IOException();
		}
	}
	
	/**
	 * Ogólne wyszukiwanie
	 * 
	 * @param maxResultSize - Maksymalna liczba wyniów
	 * @param searcher - IndexSearcher użyty do wyszukiwania
	 * @param query - Zapytanie
	 * @param lang - język zapytania
	 * @param details - określa czy szczegóły wyniku są wypisywane
	 * @param color - określa czy szukany tekst ma być wyświetlony na czerwono
	 */
	private void querry (int maxResultSize, IndexSearcher searcher, Query query,
			String lang, boolean details, boolean color) {
		TopDocs result = null;
		try {
			Analyzer analyzer;
			String field;
			
			// Otrzymywanie wyniku
			result = searcher.search(query, maxResultSize);
			
			// Wypisywanie liczby znalezionych plików
			AnsiConsole.systemInstall();
			System.out.println ("Files count: "  +  ansi().bold().a(result.scoreDocs.length).reset());
			
			// Dobieranie analizatora dla Highlighter'a
			if (lang.equals("pl")) {
				analyzer = new PolishAnalyzer();
				field = "contentsPL";
			}
			else {
				analyzer = new EnglishAnalyzer();
				field = "contentsEN";
			}
			
			UnifiedHighlighter highlighter = new UnifiedHighlighter(searcher, analyzer);
			
			// Wybieranie formatu zaznaczania tekstu
			DefaultPassageFormatter format;
			if (color) {
				format = new DefaultPassageFormatter("<ansiColorRed>", "<ansiColorRed>", "... ", false);
			}
			else {
				format = new DefaultPassageFormatter("","", "... ", false);
			}
			highlighter.setFormatter(format);
			
			// Wypisywanie znalezionego tekstu
			TopDocs[] hits = divideToArray(result);
			for (int i = 0; i < hits.length; i++) {
				System.out.println(ansi().bold().a(searcher.doc(hits[i].scoreDocs[0].doc).get("path")).boldOff());
				if (details) {
					String[] fragments = highlighter.highlight(field, query, hits[i], 10);
					for (String frag : fragments) {
						if (!color) {
							System.out.print(frag);
						}
						else {
							int k = -1;
							String[] parts = frag.split("<ansiColorRed>");
							for (String part : parts) {
								if (k == -1) {
									System.out.print(part);
								}
								else {
									System.out.print(ansi().fgRed().a(part).fgDefault());
								}
								k *= -1;
							}
						}
					}
					System.out.println();
				}
			}
			AnsiConsole.systemUninstall();
		}
		catch (IOException e) {
			System.err.println("Caught a " + e.getClass() + 
					"\n with message: " + e.getMessage());
		}
	}
	
	private TopDocs[] divideToArray (TopDocs docs) {
		TopDocs[] tab = new TopDocs[docs.scoreDocs.length];
		for (int i = 0; i < docs.scoreDocs.length; i++) {
			ScoreDoc[] tab1 = new ScoreDoc[1];
			tab1[0] = docs.scoreDocs[i];
			TotalHits hits = new TotalHits ((long) 1, TotalHits.Relation.EQUAL_TO);
			tab[i] = new TopDocs (hits, tab1);
		}
		return tab;
	}
	
}
