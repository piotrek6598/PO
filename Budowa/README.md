# Zadanie 1: Optymalna budowa<br/>

<b>Wprowadzenie</b><br/>

Podczas robót budowlanych zachodzi potrzeba rozwiązania problemu optymalizacyjnego dotyczącego przygotowania prętów do konstrukcji stalowych.

Projekt budowy określa liczbę i długości potrzebnych odcinków prętów. W cenniku sprzedawcy są długości prętów i ich ceny. Rozwiązanie wskazuje, ile prętów jakiej długości należy kupić i jak podzielić je na odcinki. Pręt dzielimy na odcinki, tnąc go. Niewykorzystaną część pręta, jeśli taka zostanie, odrzucamy. Łączenie prętów nie jest możliwe.

Za miarę jakości rozwiązania można przyjąć np.:

całkowity koszt zakupu prętów, lub <br/>
łączną długość odpadów, czyli części kupionych prętów, które nie zostaną wykorzystane. <br/>

Załóżmy, że w cenniku są:

pręt długości 4000, w cenie 100, <br/>
pręt długości 4500, w cenie 160, <br/>
pręt długości 10000, w cenie 200, <br/>

gdzie długość podana jest w milimetrach a cena w PLN. <br/>

Przyjmijmy, że projekt zakłada użycie: <br/>

dwóch odcinków długości 200, <br/>
trzech odcinków długości 350, <br/>
jednego odcinka długości 600, <br/>
dwóch odcinków długości 1500, <br/>
jednego odcinka długości 3000, <br/>
jednego odcinka długości 4500. <br/>

Przykładowym rozwiązaniem problemu jest kupno jednego pręta długości 4500 i trzech prętów długości 4000. Następnie: <br/>

pręt długości 4500 w całości przeznaczamy na odcinek długości 4500, <br/>
pierwszy pręt długości 4000 dzielimy na odcinki długości 3000, 600, 350 i odpad długości 50, <br/>
drugi pręt długości 4000 dzielimy na odcinki długości 1500, 1500, 350, 350, 200 i odpad długości 100, <br/>
trzeci pręt długości 4000 dzielimy na odcinek długości 200 i odpad długości 3800. <br/>

Dla tego rozwiązania: <br/>

całkowity koszt zakupu prętów jest równy 460, <br/>
łączna długość odpadów jest równa 3950. <br/>

Wśród strategii wyboru rozwiązania są:

<b>strategia minimalistyczna</b>

Działa zachłannie. Dopóki problem nie jest rozwiązany, z cennika wybiera najkrótszy pręt, w którym mieści się najdłuższy brakujący odcinek. Następnie rozważa brakujące odcinki w kolejności od najdłuższych. Jeśli rozważany odcinek mieści się w części pręta, która jeszcze pozostała, jest od niej odcinany. To, co zostanie z pręta, po rozważeniu ostatniego odcinka, jest odpadem.

<b>strategia maksymalistyczna</b>

Działa tak, jak strategia minimalistyczna, ale z cennika zawsze wybiera najdłuższy pręt.

<b>strategia ekonomiczna</b>

Znajduje jedno z, być może wielu, rozwiązań minimalizujących koszt zakupu prętów.

<b>strategia ekologiczna</b>

Znajduje jedno z, być może wielu, rozwiązań minimalizujących długość odpadów.

<b>Polecenie</b><br/>

Napisz program, który: <br/>

czyta ze standardowego wejścia cennik prętów, opis projektu i nazwę strategii, <br/>
za pomocą wskazanej strategii rozwiązuje problem optymalizacyjny, <br/>
pisze na standardowe wyjście rozwiązanie, określając jego jakość, kupione pręty i sposób ich podziału. <br/>

<b>Postać danych</b><br/>

Dane programu to ciąg wierszy. We wszystkich, z wyjątkiem ostatniego, są liczby całkowite w zapisie dziesiętnym. Między każdą parą liczb sąsiadujących w wierszu jest jedna spacja. <br/>

W pierwszym wierszu danych jest długość cennika C. <br/>
W C kolejnych wierszach są pary dodatnich liczb całkowitych. Pierwsza z tych liczb określa długość pręta a druga to jego cena. Pary są uporządkowane rosnąco po długości pręta. <br/>
Po cenniku jest wiersz z długością projektu P. <br/>
W następnym wierszu jest P dodatnich liczb całkowitych, uporządkowanych niemalejąco. Liczby określają długości odcinków, potrzebnych do realizacji projektu. <br/>
W ostatnim wierszu danych, po projekcie, jest słowo minimalistyczna, maksymalistyczna, ekonomiczna lub ekologiczna, będące nazwą wybranej strategii. <br/>

<b>Postać wyniku</b><br/>

Wynik programu jest ciągiem wierszy z dziesiętnym zapisem liczb całkowitych. Między każdą parą liczb sąsiadujących w wierszu jest jedna spacja. <br/>

W pierwszym wierszu wyniku jest koszt zakupu prętów. <br/>
W drugim wierszu jest łączna długość odpadów. <br/>
Pozostałe wiersze określają sposób podziału kupionych prętów. Dla każdego pręta jest jeden wiersz. Kolejność tych wierszy nie ma znaczenia. <br/>

Na początku wiersza określającego podział jest długość pręta. Po niej, w dowolnej kolejności, są długości odcinków, na które pręt został podzielony, z pominięciem ewentualnego pozostałego odpadu. Suma długości odcinków jest więc mniejsza lub równa długości pręta, z którego powstały.
