package indekser;


import java.io.*;
import java.nio.file.*;

/**
 * Klasa umożliwiąją pracę indeksera
 * Przechowuje operator na indexie, który jest
 * tworzony wraz z wlączeniem indeksera
 */
public class Indekser  {
	private static IndexOperator index;
	
	public static void main(String[] args) {
		// Ścieżka podkatalogu z indexem w katalogu domowym
		String dir = System.getProperty("user.home");
		dir += "/.index";
		index = new IndexOperator (dir);
		
		// Zamykanie indexu w momencie zakonczenia pracy programu
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				index.closeIndexModifiers();
			}
		});
		
		// Standardowy tryb monitorowanie jesli nie podano parametrów
		if (args.length == 0) {
			doStandardMode();
		}
		else if (args[0].equals("--purge")) {
			index.deleteDocs();
		}
		else if (args[0].equals("--add")) {
			Path path = Paths.get(args[1]);
			path = path.toAbsolutePath();
			index.addDir(path, true);
		}
		else if (args[0].equals("--rm")) {
			Path path = Paths.get(args[1]);
			path = path.toAbsolutePath();
			index.removeDir(path, true);
		}
		else if (args[0].equals("--reindex")) {
			index.reindex();
		}
		else if (args[0].equals("--list")) {
			index.printWatchedDir();
		}
		else {
			System.err.println ("Wrong command");
		}
	}
	/**
	 * Standardowy tryb indeksera
	 */
	private static void doStandardMode() {
		WatchDir watchDir = null;
		String[] watchedDirs = index.getWatchedDir();
		boolean created = false;
		
		// Rejestrowanie ścieżek w sytemie notyfikacji o zmianie
		// systmu plikóww dla ścieżek obserwowanych przez index
		if (watchedDirs.length > 0) {
			try {
				watchDir = new WatchDir (Paths.get(watchedDirs[0]), true, index);
				
				for (int i = 1; i < watchedDirs.length; i++) {
					watchDir.registerAll(Paths.get(watchedDirs[i]));
				}
				created = true;
			}
			catch (IOException e) {
				System.err.println("Caught a " + e.getClass() + 
						"/n with message: " + e.getMessage());
			}
		}
		
		for (;;) {
			// Obserowanie ścieżek jest takowe istnieją
			if (created) {
				watchDir.processEvents();
			}
		}
	}
}
