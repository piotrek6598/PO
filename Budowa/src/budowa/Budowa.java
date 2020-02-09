package budowa;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

public class Budowa {

	public static void main(String[] args) {
		
		int c,p;
		String strategia;
		Scanner scan = new Scanner(System.in);
		List <Pret> prety = new ArrayList <Pret> ();
		List<Integer> dlugosci = new ArrayList <Integer> ();
		Sklep s;

		c = scan.nextInt();
		for (int i = 0; i < c; i++) {
			int dlugosc = scan.nextInt();
			int cena = scan.nextInt();
			prety.add(new Pret(dlugosc, cena));
		}
		
		s = new Sklep (prety);
		
		p = scan.nextInt();
		
		for (int i = 0; i < p; i++) {
			dlugosci.add(0, scan.nextInt());
		}
		
		strategia = scan.next();
		
		Projekt projekt = null;
		
		
		if (strategia.equals("minimalistyczna"))
			projekt = new Projekt (dlugosci, new Strategia_Minimalistyczna());
		if (strategia.equals("maksymalistyczna"))
			projekt = new Projekt (dlugosci, new Strategia_Maksymalistyczna());
		if (strategia.equals("ekonomiczna"))
			projekt = new Projekt (dlugosci, new Strategia_Ekonomiczna());
		if (strategia.equals("ekologiczna"))
			projekt = new Projekt (dlugosci, new Strategia_Ekologiczna());
		
		projekt.realizacja_projektu(s);
	}

}
