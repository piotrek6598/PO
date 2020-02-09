package budowa;

import java.util.List;
/* Objekt sklep przechowuje liste
 * dostepnych pretow i umozliwia przekazanie
 * tej informacji */
public class Sklep {
	private List<Pret> prety;

	public List<Pret> getPrety() {
		return prety;
	}
	
	public Sklep (List<Pret> prety) {
		this.prety = prety;
	}
}
