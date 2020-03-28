package main.controller;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import main.model.LevelDataType;
import main.model.Model;
import main.model.statement.ComplexStatement;
import main.parser.CodeParser;
import main.utility.GameConstants;
import main.utility.SimpleSet;
import main.utility.Util;
import main.view.CodeArea;
import main.view.CodeField;
import main.view.SceneState;
import main.view.View;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeAreaController implements PropertyChangeListener {

    private View view;
    private int currentIndex = 0;
    private boolean addBefore = false;
    private boolean isError = false;
//    private boolean isErrorAI = false;
    private boolean gameRunning = false;
    private SimpleSet<Integer> selectedIndexSet = new SimpleSet<>();
    private boolean needsRecreation = false;
    private boolean showError;
    private boolean codeLineAdded;
    private boolean compilerActive = true;
//    private List<String> codeLines

    public CodeAreaController(View view) {
        this.view = view;
        view.getCodeArea().addPropertyChangeListener(this);
        view.getAICodeArea().addPropertyChangeListener(this);
    }
    private void setAllHandlersForCodeArea(CodeArea currentCodeArea) {
//        boolean isError = currentCodeArea.isAi() ? isErrorAI : isErrorPlayer;
        for (CodeField codeField : currentCodeArea.getCodeFieldListClone()) {
            setHandlerForCodeField(codeField,currentCodeArea);

        }
        if(currentCodeArea.isAi() && view.getCurrentSceneState() != SceneState.LEVEL_EDITOR) currentCodeArea.setEditable(false);
        currentCodeArea.setOnScroll(evt -> {
            if(isError)return;
            double y = evt.getDeltaY();
            int dy = currentCodeArea.getScrollAmount();
            if(y < 0 && dy+1<= currentCodeArea.getSize()-GameConstants.MAX_CODE_LINES)dy ++;
            if(y > 0 && dy-1 >= 0)dy--;
            currentCodeArea.scroll(dy);

        });
        currentCodeArea.getUpBtn().setOnAction(actionEvent -> {
            if(isError)return;
            currentCodeArea.scroll(currentCodeArea.getScrollAmount() - 1);
        });
        currentCodeArea.getDownBtn().setOnAction(actionEvent -> {
            if(isError)return;
            currentCodeArea.scroll(currentCodeArea.getScrollAmount()+ 1);
        });
        currentCodeArea.getUpBtn().setOnMouseEntered(actionEvent -> {
            currentCodeArea.getUpBtn().setEffect(GameConstants.HIGHLIGHT_BTN_EFFECT);
        });
        currentCodeArea.getDownBtn().setOnMouseEntered(actionEvent -> {

            currentCodeArea.getDownBtn().setEffect(GameConstants.HIGHLIGHT_BTN_EFFECT);
        });
        currentCodeArea.getUpBtn().setOnMouseExited(actionEvent -> {
            currentCodeArea.getUpBtn().setEffect(GameConstants.GLOW_BTN_EFFECT);
        });
        currentCodeArea.getDownBtn().setOnMouseExited(actionEvent -> {

            currentCodeArea.getDownBtn().setEffect(GameConstants.GLOW_BTN_EFFECT);
        });
    }
    private void setHandlerForCodeField(CodeField currentCodeField, CodeArea currentCodeArea) {
        currentCodeField.setOnMousePressed(event -> {
            if(gameRunning || !currentCodeArea.isEditable())return;
            needsRecreation = false;
            codeLineAdded = false;
//            boolean isError = currentCodeArea.isAi() ? isErrorAI : isErrorPlayer;

            if(currentCodeArea.getSelectedCodeField() == currentCodeField) return;
            currentCodeArea.deselectAll();
            showError = true;
            if(compilerActive)handleCodeFieldEvent(currentCodeArea.getAllText(),currentCodeArea);
            if(!isError){
                currentIndex = currentCodeArea.indexOfCodeField(currentCodeField);
                currentCodeArea.deselectAll();
                currentCodeArea.select(currentCodeField, Selection.NONE);
            }
        });

        currentCodeField.addListener((observableValue, s, t1) -> {
            // without this ctrl-backspace will delete a lot of stuff
            if(!s.equals(t1) &&!currentCodeField.isEditable()){
                currentCodeField.setText(s);
                return;
            }
            //TODO: is this necessary?
            Platform.runLater(()->currentCodeField.setEmptyFlag(t1.equals("")));
            currentCodeField.autosize();
            if(Util.isTooLongForCodefield(t1,currentCodeField.getDepth()))currentCodeField.setText(s);

        });

        currentCodeField.setOnKeyPressed(event -> {
            if (gameRunning || (!currentCodeField.isEditable()&&event.isControlDown())){
                currentCodeArea.requestFocus();
                currentCodeField.requestFocus();
                return;
            }
            needsRecreation = false;
            if(currentCodeArea.isAi())view.getCodeArea().deselectAll();
            else view.getAICodeArea().deselectAll();
            List<String> codeLines = currentCodeArea.getAllText();
            //TODO: needed?
            addBefore = false;
            showError = false;
            codeLineAdded = false;
            currentIndex = currentCodeArea.indexOfCodeField(currentCodeField);
            if(compilerActive && !isError && event.isControlDown() && event.getCode() == KeyCode.R){
                Matcher variableMatcher = Pattern.compile("^[a-zA-Z]+ +("+GameConstants.VARIABLE_NAME_REGEX+")( *;| +=.*;)$").matcher(currentCodeField.getText());
                if(variableMatcher.matches()){
                    String varName = variableMatcher.group(1);
                    TextInputDialog textInput = new TextInputDialog(varName);
                    textInput.setContentText("Rename variable:");
                    textInput.getEditor().textProperty().addListener((observableValue, s, t1) -> {
                       if(!t1.matches(GameConstants.VARIABLE_NAME_REGEX))textInput.getEditor().setText(s);
                        Text text = new Text(t1);
                        text.setFont(textInput.getEditor().getFont());
                        if(text.getLayoutBounds().getWidth() > textInput.getEditor().getLayoutBounds().getWidth())textInput.getEditor().setText(s);
                    });
                    Optional<String> result = textInput.showAndWait();
                    if(result.isPresent()){
                        String newName = result.get();
                        for(int i = currentIndex; i< codeLines.size(); i++){
                            String oldString = codeLines.get(i);
                            String newString = codeLines.get(i).replaceAll("^(.*[^a-zA-Z0-9]+|)"+varName+"([^a-zA-Z0-9]+.*)$","$1"+newName+"$2");
                            while(!oldString.equals(newString)){
                                oldString = newString;
                                newString = oldString.replaceAll("^(.*[^a-zA-Z0-9]+|)"+varName+"([^a-zA-Z0-9]+.*)$","$1"+newName+"$2");
                            }
                            if(i == currentIndex)if(Util.isTooLongForCodefield(newString, currentCodeField.getDepth())) {
                                new Alert(Alert.AlertType.ERROR, "The resulting code field is too long!").showAndWait();
                                return;
                            }
                            codeLines.set(i, newString);
                        }
                    }
                }
                needsRecreation = true;
                handleCodeFieldEvent(codeLines, currentCodeArea);
                return;
            }
            switch (event.getCode()) {
                case F5:
                    compilerActive = !compilerActive;
                    currentCodeArea.setIconActive(compilerActive);
                    disableControlElements(!compilerActive,currentCodeArea);
                    break;
                case ENTER:
                    showError = true;
//                    if(!currentCodeField.isEditable())return;
                    int selectedIndex = currentCodeField.getCaretPosition();
                    if (selectedIndex == 0 && !currentCodeField.isEmpty()) {
                        addBefore = true;
                    }
//                    boolean hasThoughtOfBrackets = textAfterCursor.matches(".*}");
                    String complexStatementRegex = GameConstants.COMPLEX_STATEMENT_REGEX;
                    // visit https://regex101.com/ for more info
                    String simpleStatementRegex = "^([^{]+\\.[^{]+ *\\( *[^{]* *\\) *|[^{]+ *[^{]+? *= *[^{]+?|[^{]+ *[^{]+?);(.++)$";

                    currentCodeArea.deselectAll();
                    String textAfterBracket = "";
                    Matcher matcherComplex = Pattern.compile(complexStatementRegex).matcher(currentCodeField.getText());
                    Matcher matcherSimple = Pattern.compile(simpleStatementRegex).matcher(currentCodeField.getText());
                    if (matcherSimple.matches()) {
                        textAfterBracket = matcherSimple.group(2);
                        codeLines.set(currentIndex, currentCodeField.getText().replaceAll(";.++", ";"));
                    } else if (matcherComplex.matches()) {
                        textAfterBracket = matcherComplex.group(2);
                        codeLines.set(currentIndex, currentCodeField.getText().replaceAll("\\{.++", "{"));
                    }
                    boolean needsBrackets = false;
                    if (matcherComplex.matches()) {
                        if (currentCodeArea.getBracketBalance() > 0) needsBrackets = true;
                    }
                    int scrollAmount = currentCodeArea.getScrollAmount() + 1 < currentCodeArea.getSize() ? currentCodeArea.getScrollAmount() + 1 : currentCodeArea.getSize() - 1 - GameConstants.MAX_CODE_LINES;
                    //TODO:

                    if (needsBrackets) {
                        codeLines.add(currentIndex + 1, "}");
                    }
                    if (textAfterBracket.matches(complexStatementRegex)) // && currentCodeArea.getBracketBalance() >= 0)
                        codeLines.add(currentIndex + 1, "}");

                    codeLines.add(addBefore ? currentIndex : currentIndex+1, textAfterBracket);
                    if (!addBefore)
                        codeLineAdded = true;
                    if (currentIndex + 1 >= GameConstants.MAX_CODE_LINES + currentCodeArea.getScrollAmount())
                        currentCodeArea.scroll(scrollAmount);
                    break;
                //TODO: bad code!
                case BACK_SPACE:
                    if (!currentCodeField.isEditable()) {
                        currentCodeArea.deselectAll();
                        if (currentIndex > 0) currentIndex--;
                        break;
                    }
                    //TODO: if Codefield isnt empty!
//                    if((currentIndex == 0 || currentCodeField.getCaretPosition() != 0)&&!currentCodeField.isEmpty()){
//                        silentError = true;
//                        break;
//                    }
                    if (currentCodeField.getCaretPosition() == 0 && !currentCodeField.isEmpty()) {
//                        System.out.println("dasfds");
                        break;
                    }
                    if(currentCodeField.isEmpty()){
                        boolean isLastCodeFieldSelected = currentIndex >= currentCodeArea.getSize()-1;
                        if(!isLastCodeFieldSelected){
                            CodeField nextCodeField = currentCodeArea.getCodeFieldListClone().get(currentIndex+1);
                            boolean isBraceOfSameDepth = nextCodeField.getText().equals("}")&&nextCodeField.getDepth()==currentCodeField.getDepth();
                            if(nextCodeField.getDepth() > currentCodeField.getDepth()||isBraceOfSameDepth){
                                int bracketIndex = currentCodeArea.findNextBracketIndex(currentIndex+1,currentCodeField.getDepth());
                                if (bracketIndex > -1){
                                    showError = true;
                                    codeLines.remove(bracketIndex);
                                }
                            }
                        }
                        if(currentCodeArea.getCodeFieldListClone().size()>1){
                            showError = true;
                            codeLines.remove(currentIndex);
                        }
                        currentIndex = (currentIndex > 0) ? currentIndex-1 : currentIndex;
                        if(codeLines.size()==0)codeLines.add("");
                        break;
                    }
                    if (currentIndex == 0) break;
                    if(!currentCodeField.getText().equals("") && codeLines.get(currentIndex-1).matches(" *")&&currentCodeField.getCaretPosition() == 0){ //TODO: vereinheitlicht " *" anstelle von ""?
                        codeLines.remove(currentIndex-1);
                        scrollAmount = currentCodeArea.getScrollAmount()-1 > 0 ? currentCodeArea.getScrollAmount()-1 : 0;
                        if(currentIndex<= currentCodeArea.getScrollAmount())
                            currentCodeArea.scroll(scrollAmount);
                        currentIndex--;
                    }
                    break;

                case DELETE:
                    if (!currentCodeField.isEditable()) {
                        if (currentIndex < currentCodeArea.getSize() - 1) currentIndex++;
                        currentCodeArea.select(currentIndex, Selection.END);
                        break;
                    }

                    if(currentIndex == currentCodeArea.getSize()-1) {
                        break;
                    }
                    if (currentCodeField.isEmpty() || currentCodeField.getText().matches(" *")) {
                        showError = true;
                        codeLines.remove(currentIndex);

                        if (currentIndex == currentCodeArea.getSize()) {

                            scrollAmount = currentCodeArea.getScrollAmount() - 1 > 0 ? currentCodeArea.getScrollAmount() - 1 : 0;
                            if (currentIndex <= currentCodeArea.getScrollAmount())
                                currentCodeArea.scroll(scrollAmount);
                            currentIndex--;
                        }
                    }

                    //TODO: delete "nextCodeField.getText().matches(" *")"
                    else if(currentCodeField.isEmpty()||currentCodeField.getText().matches(" *")||currentCodeField.getCaretPosition() == currentCodeField.getText().length() ){
                        CodeField nextCodeField = currentCodeArea.getCodeFieldListClone().get(currentIndex+1);
                        if ((nextCodeField.isEmpty()||nextCodeField.getText().matches(""))){
                        //TODO: String oldText = nextCodeField.getText();
                            showError = true;
                            codeLines.remove(currentIndex+1);
                        }
                    }
                    break;
                case UP:
                    showError = true;
                    if (isError) break;
                    if (currentIndex <= 0) {
                        currentIndex = 0;
                        currentCodeArea.select(currentIndex,Selection.END);
                        return;
                    }
                    scrollAmount = currentCodeArea.getScrollAmount() - 1 > 0 ? currentCodeArea.getScrollAmount() - 1 : 0;
                    if (currentIndex <= currentCodeArea.getScrollAmount())
                        currentCodeArea.scroll(scrollAmount);
                    if(event.isAltDown()){
                        int startIndex = currentIndex;
                        int endIndex = startIndex;
                        boolean isBalanced = currentCodeArea.getBracketBalance() == 0;
                        if(isBalanced && codeLines.get(startIndex).matches(GameConstants.COMPLEX_STATEMENT_REGEX))
                            endIndex = currentCodeArea.findNextBracketIndex(startIndex, currentCodeField.getDepth());
                        codeLines = Util.moveItems(codeLines,startIndex, endIndex,-1);
                        needsRecreation = true;
//                        codeAreaClone.moveCodeField(currentIndex, true);
//                        for(Integer i : selectedIndexSet)
//                            codeAreaClone.moveCodeField(i,true);
                    }
//                    else if(event.isShiftDown()){
//                        selectedIndexSet.add(currentIndex);
//                        int nextDepth = codeArea.getCodeFieldListClone().get(codeArea.indexOfCodeField(currentCodeField)-1).getDepth();
//                        if(currentCodeField.getDepth() == nextDepth)selectedIndexSet.add(currentIndex-1);
//                    }
//                    else {
//                        selectedIndexSet.clear();
//                        selectedIndexSet.add(currentIndex-1);
//                    }
                    currentIndex--;

                    currentCodeArea.select(currentIndex, Selection.END);
                    break;
                case DOWN:
                    showError = true;
                    if (isError) break;
                    if (currentIndex >= currentCodeArea.getSize() - 1) {
                        currentIndex = currentCodeArea.getSize() - 1;
                        currentCodeArea.select(currentIndex, Selection.END);
                        return;
                    }
                    scrollAmount = currentCodeArea.getScrollAmount() + 1 < currentCodeArea.getSize() ? currentCodeArea.getScrollAmount() + 1 : currentCodeArea.getSize() - 1 - GameConstants.MAX_CODE_LINES;
                    if (currentIndex + 1 >= GameConstants.MAX_CODE_LINES + currentCodeArea.getScrollAmount())
                        currentCodeArea.scroll(scrollAmount);
                    if(event.isAltDown()){
                        int startIndex = currentIndex;
                        int endIndex = startIndex;
                        boolean isBalanced = currentCodeArea.getBracketBalance() == 0;
                        if(isBalanced && codeLines.get(startIndex).matches(GameConstants.COMPLEX_STATEMENT_REGEX))
                            endIndex = currentCodeArea.findNextBracketIndex(startIndex, currentCodeField.getDepth());
                        codeLines = Util.moveItems(codeLines,startIndex, endIndex,1);
                        needsRecreation = true;
//                        codeAreaClone.moveCodeField(currentIndex, false);
//                        for(Integer i : selectedIndexSet)
//                            codeAreaClone.moveCodeField(i,false);
                    }
//                    else if(event.isShiftDown()){
//                        selectedIndexSet.add(currentIndex);
//                        int nextDepth = codeArea.getCodeFieldListClone().get(codeArea.indexOfCodeField(currentCodeField)+1).getDepth();
//                        if(currentCodeField.getDepth() == nextDepth)selectedIndexSet.add(currentIndex+1);
//                    }
//                    else {
//                        selectedIndexSet.clear();
//                        selectedIndexSet.add(currentIndex+1);
//                    }
                    currentIndex++;
                    currentCodeArea.select(currentIndex, Selection.END);
                    break;
                case LEFT:
                case RIGHT:
                    return;
                //LINES BELOW ARE A WORKAROUND FOR ANOTHER JAVAFX BUG
                case RECORD:
                    break;
                default:
//                    selectedIndexSet.clear();
//                    selectedIndexSet.add(currentIndex);
                    if (!currentCodeField.isEditable()) return;
                    Platform.runLater(() -> currentCodeField.fireEvent(new KeyEvent(event.getEventType(), event.getCharacter(), event.getText(), KeyCode.RECORD, false, false, false, false)));
                    return;
//
//                    codeArea.deselectAll();

//                    if(recompileCode() == null)view.getBtnExecute().setDisable(true);
//                    else view.getBtnExecute().setDisable(false);
//                    codeArea.select(currentIndex+1,true);
            }
            if(compilerActive)handleCodeFieldEvent(codeLines,currentCodeArea);
        });
    }

    private void handleCodeFieldEvent(List<String> codeLines, CodeArea currentCodeArea) {
        needsRecreation = needsRecreation || (codeLines.size() != currentCodeArea.getSize());
        Label errorLabel = currentCodeArea.isAi() ? view.getErrorLabelAI() : view.getErrorLabel();
        errorLabel.setVisible(false);
        if(currentCodeArea.isAi())view.getCodeArea().deselectAll();
        else view.getAICodeArea().deselectAll();
        try {
            ComplexStatement behaviour = CodeParser.parseProgramCode(codeLines,!currentCodeArea.isAi());
            disableControlElements(false, currentCodeArea);
            if(isError){
                currentCodeArea.resetStyle(currentIndex);
            }
            isError = false;
            currentCodeArea.setEditable(true);
            // this will ensure that the current status of the AI is always tracked as a LevelChange
            // not necessary for PlayerCode
            if(currentCodeArea.isAi())
                Model.changeCurrentLevel(LevelDataType.AI_CODE,behaviour);
            if(needsRecreation) {
                currentCodeArea.updateCodeFields(behaviour);
                setAllHandlersForCodeArea(currentCodeArea);
                if(codeLineAdded)
                    currentIndex++;
                currentCodeArea.select(currentIndex, Selection.END);
            }
        }catch (Exception e){
            isError = true;
            disableControlElements(true,currentCodeArea);
            currentCodeArea.setEditable(false);
            currentCodeArea.setEditable(currentIndex,true);

            errorLabel.setText(e.getMessage());
            if(showError){
                currentCodeArea.highlightError(currentIndex);
                errorLabel.setVisible(true);
            }

        }
    }

    private void disableControlElements(boolean b, CodeArea codeArea) {
        view.getBtnExecute().setDisable(b);
        view.getStoreCodeBtn().setDisable(b);
        if(codeArea.isAi())view.getCodeArea().setDisable(b);
        else view.getAICodeArea().setDisable(b);
        if(view.getCurrentSceneState() == SceneState.LEVEL_EDITOR)
            view.getLevelEditorModule().getSaveLevelBtn().setDisable(b);
    }

    public void setGameRunning(boolean gameRunning) {
        this.gameRunning = gameRunning;
        if(gameRunning)view.getCodeArea().deselectAll();
        if(gameRunning&&(boolean)Model.getDataFromCurrentLevel(LevelDataType.HAS_AI))view.getAICodeArea().deselectAll();
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        CodeArea codeArea = (CodeArea) evt.getNewValue();
//        if(codeArea.isEditable())
        setAllHandlersForCodeArea(codeArea);
    }
}