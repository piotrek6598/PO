package budowa;

import java.util.List;

import java.util.HashMap;

import java.util.ArrayList;

/* Realizacja zakupu pretow dla strategii ekologicznej */
public class Strategia_Ekologiczna implements Strategia {
	
	/* Alogrytm wykladniczy obliczania optymalnego wyniku
	 * opiera sie na obserwacji, ze dowolny zbior potrzebnych dlugosci
	 * da sie podzielic na podzbior (dla ktorego wynik jest juz obliczony)
	 * oraz dopelnienie, ktore wycina sie z jednego preta
	 * nowy wynik jest obliczany na podstawie najlepszego podzialu */
	@Override
	public List<Pret> kup_prety(List<Pret> dostepne_prety, List<Integer> dlugosci) {
		
		HashMap <List <Integer>, List <Pret>> mapa; 
		mapa = new HashMap <List <Integer>, List <Pret>>();
		
		/* Rozwiazanie podproblemu, gdy potrzebny jest jeden pret */
		for (int i = 0; i < dlugosci.size(); i++) {
			int k = 0;
			
			while (dostepne_prety.get(k).getDlugosc() < dlugosci.get(i))
				k++;
			
			Pret p = new Pret (dostepne_prety.get(k).getDlugosc(), 
					dostepne_prety.get(k).getCena());
			
			List <Integer> podzial = new ArrayList <Integer> ();
			
			podzial.add(dlugosci.get(i));
			p.podziel(podzial, p.getDlugosc() - dlugosci.get(i));
			
			List <Pret> prety = new ArrayList <Pret>();
			
			prety.add(p);
			mapa.put(podzial, prety);
		}
		
		/* Kolejne rozwiazywanie problemow dla n-elementowych
		 * podzbiorow wyjsciowego zbioru */
		for (int i = 2; i <= dlugosci.size(); i++) {
			wyznacz_podzbiory (mapa, dlugosci, dlugosci.size(), i, 0, 
					new ArrayList<Integer>(i), dostepne_prety);
		}
		
		return mapa.get(dlugosci);
	}
	
	/* Algorytm wyznaczania wszystkich n-elementowych podzbiorow
	 * danego zbioru, dla kazdego z nich wyznaczaj optymalne rozwiazanie */
	private void wyznacz_podzbiory (HashMap<List<Integer>, List<Pret>> map, 
			List <Integer> zbior, int rozmiar, int lewy_indeks, int aktu_index, 
			List<Integer> podzbior, List <Pret> dostepne_prety) {
		
		if (lewy_indeks == 0) {
			if (map.containsKey(podzbior) == false)
				wyznacz_rozwiazania (map, podzbior, dostepne_prety);
			
			return;
		}
		for (int i = aktu_index; i < rozmiar; i++) {
			podzbior.add(zbior.get(i));
			
			wyznacz_podzbiory (map, zbior, rozmiar, lewy_indeks - 1, i + 1, 
					new ArrayList<Integer>(podzbior), dostepne_prety);
			
			podzbior.remove(podzbior.get(podzbior.size()-1));
		}
	}
	
	/* Wyznaczanie rozwiazania dla podzbioru
	 * na podstawie powyzszej obserwacji */
	private List <Pret> wyznacz_rozwiazania (HashMap<List<Integer>, List<Pret>> map, 
			List <Integer> zbior, List<Pret> dostepne_prety) {
		
		List <Pret> wynik = new ArrayList <Pret>();
		int maks_odpad = Integer.MAX_VALUE;
		
		for (int i = 0; i < zbior.size(); i++) {
			List <Integer> podzbior = new ArrayList <Integer>();
			
			maks_odpad = doloz_optymalnie_pret_do_podzbioru(map, zbior, zbior.size(), 
					i, 0, podzbior, dostepne_prety, maks_odpad, wynik);
		}
		
		return wynik;
	}
	
	/* Algorytm wyznaczania wszystkich n-elementowych podzbiorow,
	 * danego zbioru, dla kazdego z nich stwierdza, czy rozwiazanie:
	 * wez optymalny wynik dla n-elementowego problemu a dopelnienie
	 * wytnij z jednego najlepszego preta jest lepsze od dotychczasowego
	 * rozwiazania problemu dla danego zbioru, jesli tak
	 * zmienia optymalne rozwiazanie */
	private int doloz_optymalnie_pret_do_podzbioru (
			HashMap <List<Integer>, List<Pret>> mapa, List <Integer> zbior, 
			int rozmiar, int lewy_indeks, int aktu_indeks, List<Integer> podzbior,
			List <Pret> dostepne_prety, int maks_odpad, List<Pret> opt_wynik) {
		
		if (lewy_indeks == 0) {
			long dlugosc_dopelnienia = 0;
			List <Integer> dopelnienie = new ArrayList<Integer> (zbior);
			
			for (int i = 0; i < zbior.size(); i++) 
				dlugosc_dopelnienia += zbior.get(i);
			for (int i = 0; i <podzbior.size(); i++) {
				dlugosc_dopelnienia -= podzbior.get(i);
				dopelnienie.remove(podzbior.get(i));
			}
			
			int k = 0;
			
			while (k < dostepne_prety.size() && 
					dostepne_prety.get(k).getDlugosc() < dlugosc_dopelnienia)
				k++;
			
			if (k != dostepne_prety.size()) {
				long odpadki = dostepne_prety.get(k).getDlugosc() - dlugosc_dopelnienia;
				
				if (odpadki + obliczOdpad(mapa.get(podzbior)) <= maks_odpad) {
					Pret p = new Pret (dostepne_prety.get(k).getDlugosc(), 
							dostepne_prety.get(k).getCena());
					
					p.podziel(dopelnienie, (int)odpadki);
					maks_odpad = (int)(odpadki + obliczOdpad (mapa.get(podzbior)));
					
					if (mapa.get(podzbior) == null)
						opt_wynik = new ArrayList <Pret>();
					else
						opt_wynik = new ArrayList <Pret>(mapa.get(podzbior));
					
					opt_wynik.add(p);
					mapa.put(zbior, opt_wynik);
				}
			}
			
			return maks_odpad;
		}
		for (int i = aktu_indeks; i < rozmiar; i++) {
			podzbior.add(zbior.get(i));
			
			maks_odpad = doloz_optymalnie_pret_do_podzbioru (mapa, zbior, 
					rozmiar, lewy_indeks-1, i + 1, new ArrayList <Integer>(podzbior),
					dostepne_prety, maks_odpad, opt_wynik);
			
			podzbior.remove(podzbior.size()-1);
		}
		return maks_odpad;
	}
	
	/* Dla danej listy pretow, zwraca laczna dlugosc odpadow */
	private long obliczOdpad (List<Pret> prety) {
		long wynik = 0;
		
		if (prety == null)
			return 0;
		
		for (Pret p : prety) {
			wynik += p.getOdpad();
		}
		
		return wynik;
	}
}
