package indekser;

import org.apache.tika.exception.TikaException;
import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.Tika;

import java.io.*;
import java.nio.file.Path;

/**
 * Klasa umożlwiające ekstrakcję tekstu z pliku
 * oraz wykrywanie języka w którym plik jest napisany.
 * Przechowuje narzędzie do ekstrakcji tekstu oraz
 * detektor języka.
 */
public class TextExtractor {
	private Tika tika;
	private OptimaizeLangDetector langDetector;
	
	/**
	 * Konstruktor obiektu klasy
	 */
	public TextExtractor () {
		this.tika = new Tika();
		this.langDetector = new OptimaizeLangDetector();
		this.langDetector.loadModels();
	}
	
	/**
	 * Dokonuje ekstrakcji teksy z pliku
	 * 
	 * @param file Ścieżka reprezentująca plik
	 * @return String reprezentujący zawartość pliku
	 * @throws Exception jeśli wystąpił błąd
	 */
	public String extractTextFromFile (Path file) throws Exception {
		String s;
		if (!isParsingPossible(file)) {
			throw new Exception();
		}
		
		try {
			s = tika.parseToString(file);
		}
		catch (IOException exc) {
			throw new Exception ();
		}
		catch (TikaException exc) {
			throw new Exception ();
		}
		
		return s;
	}
	
	/**
	 * Dokonuje próby parsowania pliku
	 * Sprawdza typ zawartośći pliku
	 * 
	 * @param file Ścieżka reprezentująca plik
	 * @return true jeśli typ jest odpowiedni lub false jeśli nie jest
	 * lub wystąpił bład
	 */
	public boolean isParsingPossible (Path file) {
		try {
			String s = tika.detect(file);
			if (s.equals("text/plain") ||
					s.equals("application/pdf") ||
					s.equals("application/rtf") ||
					s.contains("application/vnd.oasis.opendocument") ||
					s.contains("application/vnd.openxml")) 
			{
				return true;
			}
			else {
				return false;
			}
		}
		catch (IOException e) {
			return false;
		}
	}
	
	/**
	 * Wykrywanie języka pliku
	 * 
	 * @param file Ścieżka reprezentująca plik
	 * @return "pl" jeśli plik jest w języku polskim,
	 * "en" w przeciwnym wypadku.
	 * 
	 * Korzysta z założenia że obsługiwane pliki są albo
	 * w języku polskim albo angielskim, jeśli wystąpił błąd
	 * przyjmuje domyślny język pliku tj. angielski
	 */
	public String detectLanguage (Path file) {
		try {
			String s = tika.parseToString(file);
			langDetector.addText(s);
			s = langDetector.detect().getLanguage();
			langDetector.reset();
			return s;
		}
		catch (IOException e) {
			return "en";
		}
		catch (TikaException e) {
			return "en";
		}
	}
	
	/**
	 * Ekstrakcja nazwy pliku
	 * 
	 * @param file Ścieżka reprezentująca pli
	 * @return Nazwa pliku
	 */
	public String getName (Path file) {
		return file.getFileName().toString();
	}
}
