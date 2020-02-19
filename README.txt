                                   ____  _____    _    ____  __  __ _____
                                  |  _ \| ____|  / \  |  _ \|  \/  | ____|
                                  | |_) |  _|   / _ \ | | | | |\/| |  _|
                                  |  _ <| |___ / ___ \| |_| | |  | | |___
                                  |_| \_\_____/_/   \_\____/|_|  |_|_____|
========================================================================================================================
                                           G L I E D E R U N G
------------------------------------------------------------------------------------------------------------------------
                                             1.  Organisatorisches
                                             2.  Das Spiel
                                              2.1 Das Tutorial
                                              2.2 Der Leveleditor
                                             3. Feedback
------------------------------------------------------------------------------------------------------------------------
                                      O R G A N I S A T O R I S C H E S
------------------------------------------------------------------------------------------------------------------------

Das Ausführen des Programms ist momentan nur unter Windows möglich.
Falls kein Zugang zu einem Windows-Rechner besteht:
    sind eine Entwicklungsumgebung für Java, Java11, sowie das zugehörige JavaFX11 sowie Kenntnisse, wie man sich mit
    git ein Projekt in ein eigenes Repository clonen kann und dort daraus dann ein eigenes Projekt machen kann
    erforderlich. Falls all diese Bedingungen erfüllt sind und kein Windows PC erreichbar ist, kann man mich persönlich
    kontaktieren, um Zugang zum git Repository zu erhalten.
Ansonsten muss einfach die image.zip in einen beliebigen Ordner entpackt werden. Anschließend kann das Spiel durch
Aufrufen der Datei "Programmierprojekt.bat" im Ordner "bin" gestartet werden.
Wenn das Projekt auf den neuesten Stand gebracht werden soll, ohne den bisherigen Spielstand zu verlieren, muss die
Datei "data.json" im Ordner "bin/resources" kopiert werden und nach herunterladen der neuesten Version (über den Link
https://cloud.chrz.de/s/ieiSggK6YfwJq28) die neue data.json durch die alte ersetzt werden.

------------------------------------------------------------------------------------------------------------------------
                                             D A S   S P I E L
------------------------------------------------------------------------------------------------------------------------
Das Tutorial:
-------------
Das Tutorial besteht aus mehreren Levels sowie einer kurzen Einführung. Wichtige Informationen sind, dass man den
Mauszeiger über den Knöpfen am unteren Bildschirmrand verweilen lassen kann, um eine kurze Erklärung ihrer Funktion zu
erhalten. Von links nach rechts, gibt es einen Knopf, der dich zurück ins Hauptmenü bringt, einen der den momentanen
Code ausführt, einen Slider, der kontrolliert, wie schnell der Code ausgeführt wird, einen Knopf der das Level
zurücksetzt und einen, der ein "Zauberbuch" öffnet, das alle bisher freigeschalteten Zaubersprüche (Methoden,
Kontrollstrukturen, etc.) enthält. Rechts oben befindet sich das Skript, in dem man selber programmieren kann, indem man
zuerst die Anweisungen im Tutorial befolgt. In späteren Levels wird sich zudem Links ein Skript welches Gegner steuert
befinden. Nachdem das gesamte Tutorial durchgespielt wurde, kann man danach beliebige Tutorial-Levels auswählen, um
diese erneut auszuprobieren. Weitere Levels, um sein Können zu testen befinden sich zudem unter "Challenges".
----------------
Der Leveleditor:
----------------
Der Leveleditor ermöglicht es, eigene Level zu erstellen, vorhandene zu bearbeiten oder zu löschen. Dafür gibt es
entsprechende Knöpfe oben, unterhalb einer Leiste mit wichtigen Informationen zum momentanen Level, welche auch über
einen Knopf in dieser bearbeitet werden können. Über das Auswählen einer Zelle und drücken eines der Knöpfe rechts davon
kann der Inhalt der Zelle bearbeitet werden. Über Drücken der Str/Shift-Taste lassen sich mehrere einzelne/zusammen-
hängende Zellen markieren und gleichzeitig bearbeiten. Links lässt sich zudem der Code der gegnerischen Einheiten
bestimmen, welcher gegebenenfalls erst in der obersten Zeile durch drücken des entsprechenden Knopfes und abhaken des
entsprechenden Kästchens, aktiviert werden muss. Zudem lässt sich hier bestimmen, ob ein Level zum Tutorial gehört, oder
nicht, ob es erst freigeschaltet werden muss und welcher Text im Tutorial angezeigt wird.

------------------------------------------------------------------------------------------------------------------------
                                             F E E D B A C K
------------------------------------------------------------------------------------------------------------------------

Feedback sowohl zu den Informationen in dieser README als auch zum Spiel selber bitte an folgende Mail-Adresse:
sgunzelmann@arcor.de
Ich würde mich über jegliche Anregungen, Wünsche, didaktische Einschätzungen, gefundene Fehler und allgemeine Kritik
freuen. Folgende Fragen sind für mich von besonderem Interesse:
- Was sind deine Vorkenntnisse in Java und konntest du das Tutorial durchspielen?
- Wenn nein, bei welchem Level bist du gescheitert und warum?
- Wenn ja, was ist deine Einschätzung zur Schwierigkeit und dem Tempo des Tutorials?
- Wie leicht ist es dir gefallen, dich in den Umgang mit dem Programm einzufinden?
- Wie war die Darstellung/Übersichtlichkeit auf deinem Bildschirm (evt Screenshot mitschicken)
- Waren alle Elemente da wo sie hingehören?
- Konntest du auch bei höchster Ausführgeschwindigkeit das Spiel problemlos laufen lassen? (Wenn nein, bitte ein paar
                                                                                            Infos zur Hardware)
Und zuletzt noch eine weitere Bitte: Falls dir das Projekt gut gefallen hat und es dir Spaß macht, selber Level zu
erstellen: Benutze doch den Leveleditor, um ein eigenes Level für die Challenges (oder auch Tutorials) zu erstellen und
schicke mir dieses dann über Mail. (Nach der Erstellung, auf speichern drücken und dann ist die Datei unter
"bin/resources/levels/<levelname>.json" zu finden)
------------------------------------------------------------------------------------------------------------------------