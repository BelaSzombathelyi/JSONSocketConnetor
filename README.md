# JSONSocketConnetor

Ez egy olyan alkalmazás, ami egyszerűen küld és fogad TCP socket alapon JSON-öket.

Java nyelven készült alkalmazás.

Felül egy sorban a host name beviteli mező aminek kezdő értéke "localhost", és mellette a port beviteli mező ami csak egész számot fogad el, kezdeti értéke 60200. Tőlül jobbra egy "Reconnect" gomb legyen aminek hatására lebontaja majd újra felépíti a kapcsolatot a megadott hostname és port-al.

Bal oldalt egy beviteli mező textArea jellegű találht


Alkaplmazás megnyitása utána csatlakozik autómatikusan

Egy elölnézete:
<img width="605" height="387" alt="image" src="https://github.com/user-attachments/assets/68b13d1c-6b76-44e5-aa7e-40b2853ebf7b" />

A Send gomb hatására elküldi a bal oldalon látható JSON-t (ha valid, ha nem akkor hibaüzenet)

Legyen mindkét oldalon JSON syntax hightlight.


Ha nem sikerült a kapcsolatot létrehozni akkor pirossal írja ki a hibát valahogy így:
<img width="368" height="47" alt="image" src="https://github.com/user-attachments/assets/f165bf17-355c-408d-8290-9c82124c8b27" />


Fő funkciók:
- TCP socketen keresztüli JSON küldés/fogadás
- Java nyelven írt alkalmazás
Grafikus UI:
- Hostname mező (alapértelmezett: "localhost")
- Port mező (egész szám, alapértelmezett: 60200)
- "Reconnect" gomb
- Bal oldali textarea, ahová JSON-t lehet írni
- "Send" gomb, ami elküldi a JSON-t (validáció: csak akkor küld, ha helyes a JSON, különben hibaüzenet)
- Bejövő JSON megjelenítése (jobb oldal)
- Mindkét oldalon JSON syntax highlight
- Hiba esetén piros hibaüzenet jelenjen meg
- Sikeres kapcsolódás utána connected
- Angolul legyen a program
- A hálózati kommunikáció ne a UI szálon fusson (pl. külön threadben)
- Sikeres kapcsolat esetén csináljon 2 üzenetváltást a háttérben.
-   Kérje le a PID-et. {"command": "GetProcessId"} paranccsal, a válaszból {"succeeded": true, "result": { "processId": 11480 }} írja bele a státusz mezőbe szintén zölddel, hogy "Connected to Archicad.exe (pid: 11480)" a megfelelő PID-et feltűntetve.
-   Kérje le az elérhető parancsokat {"command": "GetCommands"} a válasz: {  "succeeded": true,  "result": {  "commands": [ "ACUserInterface.GetTransparentNotifications",  "API.CloneProjectMapItemToViewMap" }}} és sok elem a commands kulcs alatt, ezt tegyel el a memóriába.
A csillag gombot (kedvencek) megnyomva jöjjön fel egy modális ablak. "Select Command" címmel
Csatolok képet:
<img width="401" height="306" alt="image" src="https://github.com/user-attachments/assets/fb2a0978-b90f-460c-945c-a352e0a9336d" />

  Mindkét oldalán egy-egy singleselect list van, a bal oldalon a "commands" kulcs alatti pl "API" kezdetűek vannak külön csoportra tagolva, a "." karakter mentén kell splittelni és az első tag a domain, ez lényegében a csoport, ami megjeleik a bal oldali listában.
  Fent van egy filter is. Csak a jobb oldali listát szűri tovább. Lehetne rajta x is, hogy liírítsd a kereső mezőt.
  Ha a bal oldali listán dupla klikkelünk akkor kiküld a háttéreben egy { "command": "GetCommandParameters", "parameters": { "command": "API.CloneProjectMapItemToViewMap"}} ahol a  "command" értéke a választott command a jobb listáról, a választ pedig beteszi a főablak bal oldalába.
