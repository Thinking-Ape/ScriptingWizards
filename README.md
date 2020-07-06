# ScriptingWizards
![StartScreen](https://github.com/Thinking-Ape/ScriptingWizards/blob/master/PresentationImages/StartScreen.png "Ein Level im Leveleditor")
## Das Spiel starten

Leider ist aus technischen Gründen das Spiel momentan nur unter Windows lauffähig.
Zum Ausführen einfach die Datei "ScriptingWizards.bat" entweder im Hauptverzeichnis
oder im Ordner "bin" ausführen.

## Der Funktionsumfang
Es handelt sich um ein level-basiertes Lernspiel geschrieben in Java, derzeit ausgelegt für den Informatik-Unterricht der 10. Klasse. Die einzig momentan verfügbare (Ingame-)Programmiersprache ist Java. Es können sowohl eigene Level erstellt, als auch vorhandene Level beliebig bearbeitet werden. Es gibt die Möglichkeit, für die Level eigene Hinweise zu schreiben, die begleitend zu den Level angezeigt werden (wie zu sehen in folgender Grafik).
### Das Tutorial
![Tutorial](https://github.com/Thinking-Ape/ScriptingWizards/blob/master/PresentationImages/Tutorial_Level_Opposition.png "Ein Level im Tutorial, das gerade ausgeführt wird")
Das Tutorial besteht aus einer kurzen Einführung und mehreren Level. In der Einführung werden alle wichtigen Steuerungselemente erklärt. Hält man seinen Mauszeiger über diesen wird zudem eine kurze Beschreibung angezeigt. Die einzelnen Level des Tutorials dienen dazu, wichtige Befehle und Kontrollstrukturen, etc. zu erklären. Dafür gibt es einen "Tutorial-Wizard", der neue Befehle einführt und wichtige Hinweise gibt.
Derzeit gibt es 13 Levels mit Erklärungen zu folgenden Bestandteilen der (Objektorientierten) Programmierung:
- Konstruktoren
- Variablendeklarationen und -zuweisungen
- Methodenaufrufe und Parameter
- Bedingungen ('&&', '||', '!' sowie Vergleichszeichen und boolean-Methodenaufrufe)
- If-Else-Konstrukte
- For- und While-Schleifen
### Die Challenges
![Challenges](https://github.com/Thinking-Ape/ScriptingWizards/blob/master/PresentationImages/Challenges.png "Das Levelauswahlmenü der Challenges")
Nachdem das Tutorial durchgespielt wurde, wird der Spiel-Modus Challenges freigeschaltet. Hier findet sich eine Reihe an Level ohne Hinweise, die auf den Tutorial-Level aufbauen.
### Der Leveleditor
![LevelEditor](https://github.com/Thinking-Ape/ScriptingWizards/blob/master/PresentationImages/LevelEditor_new.png "Ein Level im Leveleditor")
Wurden alle Challenges durchgespielt, wird zuletzt der Leveleditor freigeschaltet. Hier kann man nun eigene Level erstellen und bestehende Level löschen oder bearbeiten. Dafür gibt es entsprechende Knöpfe oben, unterhalb einer Leiste mit wichtigen Informationen zum momentanen Level, welche auch über einen Knopf "Edit" in dieser bearbeitet werden können. Über das Auswählen einer Zelle und Drücken eines der Knöpfe rechts davon, kann der Inhalt der Zelle bearbeitet werden. Über Drücken der Strg/Shift-Taste lassen sich mehrere einzelne bzw zusammenhängende Zellen markieren und gleichzeitig bearbeiten. Links lässt sich zudem der Code der gegnerischen Einheiten bestimmen, welcher gegebenenfalls erst in der obersten Zeile durch drücken von "Edit" und abhaken des entsprechenden Kästchens, aktiviert werden muss. Zudem lässt sich hier bestimmen, ob ein Level zum Tutorial gehört, oder nicht, ob es erst freigeschaltet werden muss und welcher Text im Tutorial angezeigt wird.
### Das Zauberbuch
![Zauberbuch](https://github.com/Thinking-Ape/ScriptingWizards/blob/master/PresentationImages/SpellBook.png "Das fast vollständig ausgefüllte Zauberbuch")

In allen 3 Modi kann jederzeit (außer wenn ein Level gerade ausgeführt wird) das sogenannte Zauberbuch geöffnet werden. Darin enthalten sind alle bisher erfolgreich angewandten Zaubersprüche abgespeichert.
