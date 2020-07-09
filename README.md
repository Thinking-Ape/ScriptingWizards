# ScriptingWizards
![StartScreen](https://github.com/Thinking-Ape/ScriptingWizards/blob/master/PresentationImages/StartScreen.png "Ein Level im Leveleditor")
## Das Spiel starten

Leider ist aus technischen Gründen das Image momentan nur unter Windows lauffähig. Zum Ausführen einfach die Datei "ScriptingWizards.bat" entweder im Hauptverzeichnis
oder im Ordner "bin" ausführen.

## Der Funktionsumfang
Es handelt sich um ein level-basiertes Lernspiel geschrieben in Java, derzeit ausgelegt für den Informatik-Unterricht der 10. Klasse. In diesem lernen die Schüler programmieren, indem sie anhand von "Zaubersprüchen" Ritter beschwören und diese mithilfe von Variablen und Kontrollstrukturen sowie Methodenaufrufen steuern. Die einzig momentan verfügbare (Ingame-)Programmiersprache ist Java. Es können sowohl eigene Level erstellt, als auch vorhandene Level beliebig bearbeitet werden. Es gibt die Möglichkeit, für die Level eigene Hinweise zu schreiben, die begleitend zu den Level angezeigt werden (wie zu sehen in folgender Grafik).
Das Ziel eines jeden Levels ist es, Ritter zu beschwören, diese einen Schlüssel einsammeln zu lassen und mit diesem einen Ausgang aufzusperren. Die dabei verwendete Lösung wird zudem anhand 3 Kriterien bewertet:
1. Wie viele Code-Zeilen wurden gebraucht
2. Wie viele Züge haben die Ritter gebraucht
3. Wie viele Ritter wurden beschworen
Es können für eine Lösung zwischen 1 und 3 Sternen erhalten werden, wobei die Kriterien für die Vergabe im Level-Editor festgelegt werden kann.
In manchen Leveln gibt es zudem Gegner, die auf dieselbe Art programmiert wurden, wie die eigenen Ritter. Das zugehörige Skript wird auch angezeigt, kann aber nicht bearbeitet werden.
### Das Kurssystem

![Courses](https://github.com/Thinking-Ape/ScriptingWizards/blob/master/PresentationImages/Courses.png "Ein Level im Leveleditor")
Es gibt die Möglichkeit, eigene Kurse zu erstellen und diese mit Leveln für die Schüler zu füllen.
Das Spiel wird bereits mit einem Tutorial geliefert, welches einfach ein Kurs mit 13 Level ist, in dem den SuS einige Programmierkonzepte erklärt werden. Zu Beginn eines jeden Kurses wird automatisch eine Einführun in die Bedienung des Spiels angezeigt. In der Einführung werden alle wichtigen Steuerungselemente erklärt. Hält man seinen Mauszeiger über diesen wird zudem eine kurze Beschreibung angezeigt. Die einzelnen Level des Tutorials dienen dazu, wichtige Befehle und Kontrollstrukturen, etc. zu erklären. Dafür gibt es einen "Tutorial-Wizard", der neue Befehle einführt und wichtige Hinweise gibt.
Derzeit gibt es 13 Levels mit Erklärungen zu folgenden Bestandteilen der (Objektorientierten) Programmierung:
- Konstruktoren
- Variablendeklarationen und -zuweisungen
- Methodenaufrufe und Parameter
- Bedingungen ('&&', '||', '!' sowie Vergleichszeichen und boolean-Methodenaufrufe)
- If-Else-Konstrukte
- For- und While-Schleifen
![Tutorial](https://github.com/Thinking-Ape/ScriptingWizards/blob/master/PresentationImages/Tutorial_Level_Opposition.png "Das Kurs-Auswahl Menü")
### Die Challenges
![Challenges](https://github.com/Thinking-Ape/ScriptingWizards/blob/master/PresentationImages/Challenges.png "Das Levelauswahlmenü der Challenges")
Wie die Level in den Kursen, nur dass keine Tipps angezeigt werden. Momentan gibt es 11 Herausforderungslevel. Wurden alle 11 mit Bestleistung gelöst, wird zudem der Level-Editor freigeschaltet.
### Der Level-Editor
![LevelEditor](https://github.com/Thinking-Ape/ScriptingWizards/blob/master/PresentationImages/LevelEditor_new.png "Ein Level im Leveleditor")
Im Level-Editor kann man nun eigene Level erstellen und bestehende Level löschen oder bearbeiten. Dafür gibt es entsprechende Knöpfe oben, unterhalb einer Leiste mit wichtigen Informationen zum momentanen Level, welche auch über einen Knopf "Edit" in dieser bearbeitet werden können. Über das Auswählen einer Zelle und Drücken eines der Knöpfe rechts davon, kann der Inhalt der Zelle bearbeitet werden. Über Drücken der Strg/Shift-Taste lassen sich mehrere einzelne bzw zusammenhängende Zellen markieren und gleichzeitig bearbeiten. Rechts vom Spielfeld gibt es einige Einstellungsmöglichkeiten, wie das wählen des Inhalts oder der Items sowie weiteren Eigenschaften. So kann man beispielsweise Druckplatten bestimmte Ids zuweisen und sie anhand dieser mit Toren verbinden. Diese Tore öffnen sich dann nur solange die entsprechenden Druckplatten gleichzeitig betätigt werden. Links vom Spielfeld lässt sich zudem der Code der gegnerischen Einheiten bestimmen, welcher gegebenenfalls erst in der obersten Zeile durch drücken von "Edit" und abhaken des entsprechenden Kästchens, aktiviert werden muss. Zudem lässt sich hier bestimmen, zu welchem Kurs das Level gehört und welcher Text im Tutorial angezeigt wird. Hier können auch neue Kurse erstellt werden.
### Das Zauberbuch
![Zauberbuch](https://github.com/Thinking-Ape/ScriptingWizards/blob/master/PresentationImages/SpellBook.png "Das fast vollständig ausgefüllte Zauberbuch")

In allen 3 Modi kann jederzeit (außer wenn ein Level gerade ausgeführt wird) das sogenannte Zauberbuch geöffnet werden. Darin enthalten sind alle bisher erfolgreich angewandten Zaubersprüche. Außerdem gibt es die Möglichkeit, dieses frei zu verschieben und eine Liste mit allen verfügbaren Tastenkombinationen anzuzeigen.
### Das Magische Skript

![CodeArea](https://github.com/Thinking-Ape/ScriptingWizards/blob/master/PresentationImages/CodeArea.png "3 Beispiele für das Magische Skript")

Zum Programmieren, wird das sogenannte Magische Skript verwendet, welches sich rechts oben befindet. Es kann immer nur eine Zeile gleichzeitig bearbeitet, über die Enter-Taste können neue Zeilen hinzugefügt und durch die Zurück- oder Entfernen-Taste können leere Felder wieder gelöscht werden. Falls die SuS einen Fehler begangen haben, kann das Skript erst weiter bearbeitet werden, bis die fehlerhafte Zeile wieder korrigiert wurde. Dies kann aber auch über einen Knopf über dem Magischen Skript deaktiviert werden, woraufhin die Syntax-Überprüfung ausgesetzt wird. Darüber hinaus werden Vorschläge angezeigt, worin ein eventueller Fehler bestehen könnte.
