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
