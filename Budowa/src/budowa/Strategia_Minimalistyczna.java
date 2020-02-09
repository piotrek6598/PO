package budowa;

import java.util.ArrayList;
import java.util.List;

/* Realizacja zakupu pretow dla strategii minimalistycznej */
public class Strategia_Minimalistyczna implements Strategia {
	public List<Pret> kup_prety (List<Pret> dost_prety, List<Integer> dlugosci){
		
		List<Pret> l = new ArrayList <Pret>();
		
		while (dlugosci.size() > 0) {
			int i = 0;
			while (dost_prety.get(i).getDlugosc() < dlugosci.get(0))
				i++;
			
			Pret p = new Pret(dost_prety.get(i).getDlugosc(), 
					dost_prety.get(i).getCena());
			
			int zostalo = p.getDlugosc();
			List<Integer> podzial = new ArrayList<Integer>();
			
			i = 0;
			while (i < dlugosci.size()) {
				if (dlugosci.get(i) <= zostalo) {
					zostalo -= dlugosci.get(i);
					podzial.add(dlugosci.remove(i));
				}
				else {
					i++;
				}
			}
			
			p.podziel(podzial, zostalo);
			l.add(p);
		}
		
		return l;
	}
}
