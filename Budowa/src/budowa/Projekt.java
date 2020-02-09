package budowa;

import java.util.List;
/* Obiekt projekt przechowuje:
 * liste dlugosci potrzebnych pretow,
 * typ strategii,
 * liste zakupioych pretow potrzebnych do 
 * realizacji danej strategii */
public class Projekt {
	private List<Integer> dlugosci_pretow;
	private Strategia strategia;
	private List <Pret> kupione_prety;
	
	public List<Integer> getDlugosci_pretow() {
		return dlugosci_pretow;
	}
	
	public Projekt (List<Integer> dlugosci_pretow, Strategia strategia) {
		this.dlugosci_pretow = dlugosci_pretow;
		this.strategia = strategia;
	}
	/* Zwraca laczny koszt zakupionych pretow */
	private long koszt () {
		long wynik = 0;
		
		for (Pret p : kupione_prety) {
			wynik += p.getCena();
		}
		
		return wynik;
	}
	/* Zwraca laczna sume odpadow z zakupionych pretow */
	private long suma_odpadow () {
		long wynik = 0;
		
		for (Pret p : kupione_prety) {
			wynik += p.getOdpad();
		}
		
		return wynik;
	}
	
	/* Dokonuje realizacji projektu, dla pretow
	 * dostepnych w podanym sklepie */
	public void realizacja_projektu (Sklep s) {
		
		kupione_prety = this.strategia.kup_prety(s.getPrety(), dlugosci_pretow);
		
		System.out.println(this.koszt());
		System.out.println(this.suma_odpadow());
		
		for (Pret p : kupione_prety) {
			System.out.print(p.getDlugosc());
			
			for (int i : p.getPodzial()) {
				System.out.print(' ');
				System.out.print(i);
			}
			
			System.out.println();
		}
	}
}
