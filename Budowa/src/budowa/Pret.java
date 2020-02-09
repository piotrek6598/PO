package budowa;

import java.util.List;

/* Obiekt pret, przechowuje: dlugosc, swoja cene,
 * dlugosci pretow ktore sa z niego wyciete,
 * dlugosc odpadu */
public class Pret {
	private int dlugosc;
	private int cena;
	private List<Integer> podzial;
	private int odpad;
	
	public int getDlugosc() {
		return dlugosc;
	}
	public int getCena() {
		return cena;
	}
	
	public Pret (int dlugosc, int cena) {
		this.dlugosc = dlugosc;
		this.cena = cena;
	}
	
	public void podziel (List<Integer> podzial, int odpad) {
		this.podzial = podzial;
		this.odpad = odpad;
	}
	
	public List<Integer> getPodzial() {
		return podzial;
	}
	
	public int getOdpad() {
		return odpad;
	}
}
