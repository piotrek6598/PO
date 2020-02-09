package budowa;

import java.util.List;
/* Interfejs zawiera metode ktora dla:
 * listy pretow mozliwych do kupienia oraz
 * listy dlugosci potrzebny pretow
 * zwraca liste pretow kupionych */
public interface Strategia {
	public List<Pret> kup_prety (List<Pret> dostepne_prety, List<Integer> dlugosci);
}
