package budowa;

import java.util.ArrayList;
import java.util.List;

/* Realizacja zakupu pretow dla strategii maksymalistycznej */
public class Strategia_Maksymalistyczna implements Strategia {

	@Override
	public List<Pret> kup_prety(List<Pret> dostepne_prety, List<Integer> dlugosci) {
		
		List<Pret> l = new ArrayList <Pret>();
		
		while (dlugosci.size() > 0) {
			Pret p = dostepne_prety.get(dostepne_prety.size()-1);
			p = new Pret (p.getDlugosc(), p.getCena());
			
			int zostalo = p.getDlugosc();
			List<Integer> podzial = new ArrayList<Integer>();
			
			int i = 0;
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
