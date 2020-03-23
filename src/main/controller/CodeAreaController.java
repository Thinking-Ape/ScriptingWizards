package main.controller;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeAreaController implements PropertyChangeListener {

    private View view;
    private Model model;
    private int currentIndex = 0;
    private boolean addBefore;
    private boolean isError = false;
//    private boolean isErrorAI = false;
    private boolean gameRunning = false;
    private SimpleSet<Integer> selectedIndexSet = new SimpleSet<>();
    private boolean needsRecreation = false;
    private boolean showError;
//    private List<String> codeLines

    public CodeAreaController(View view, Model model) {
        this.model =model;
        this.view = view;
        view.addPropertyChangeListener(this);
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
            if(gameRunning)return;
            needsRecreation = false;
//            boolean isError = currentCodeArea.isAi() ? isErrorAI : isErrorPlayer;

            if(currentCodeArea.getSelectedCodeField() == currentCodeField) return;
            currentCodeArea.deselectAll();
            showError = true;
            handleCodefieldEvent(currentCodeArea.getAllText(),currentCodeArea);
            if(!isError){
                currentIndex = currentCodeArea.indexOfCodeField(currentCodeField);
                currentCodeArea.deselectAll();
                currentCodeArea.select(currentCodeField, Selection.NONE);
            }
        });

        currentCodeField.addListener((observableValue, s, t1) -> {
            //TODO: is this necessary?
            Platform.runLater(()->currentCodeField.setEmptyFlag(t1.equals("")));
            currentCodeField.autosize();
            Text text = new Text(t1);
            text.setFont(GameConstants.CODE_FONT);
            if(text.getLayoutBounds().getWidth() > currentCodeField.getMaxWidth()-GameConstants.SCREEN_WIDTH/110)currentCodeField.setText(s);
            // without this ctrl-backspace will delete "}" (dont know why though)
            if(s.equals("}")&&!t1.equals("}"))currentCodeField.setText("}");
        });

        currentCodeField.setOnKeyPressed(event -> {
            if (gameRunning) return;
            needsRecreation = false;
            if(currentCodeArea.isAi())view.getCodeArea().deselectAll();
            else view.getAICodeArea().deselectAll();
            List<String> codeLines = currentCodeArea.getAllText();
            //TODO: needed?
            addBefore = false;
            showError = false;
            currentIndex = currentCodeArea.indexOfCodeField(currentCodeField);
            switch (event.getCode()) {
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

                    codeLines.add(currentIndex+1, textAfterBracket);
                    if (!addBefore && !isError) currentIndex++;
                    if (currentIndex + 1 >= GameConstants.MAX_CODE_LINES + currentCodeArea.getScrollAmount())
                        currentCodeArea.scroll(scrollAmount);
                    break;

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

                        break;
                    }
                    if (currentIndex == 0) break;
                    if(!currentCodeField.getText().equals("") && codeLines.get(currentIndex-1).matches(" *")){ //TODO: vereinheitlicht " *" anstelle von ""?
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
                    if(event.isControlDown()){
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
                    if(event.isControlDown()){
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
            handleCodefieldEvent(codeLines,currentCodeArea);
        });
    }

    private void handleCodefieldEvent(List<String> codeLines, CodeArea currentCodeArea) {
        needsRecreation = needsRecreation || (codeLines.size() != currentCodeArea.getSize());
        Label errorLabel = currentCodeArea.isAi() ? view.getErrorLabelAI() : view.getErrorLabel();
        errorLabel.setVisible(false);
        try {
            ComplexStatement behaviour = CodeParser.parseProgramCode(codeLines,!currentCodeArea.isAi());
            disableControlElements(false, currentCodeArea);
            if(isError)currentCodeArea.resetStyle(currentIndex);
            isError = false;
            currentCodeArea.setEditable(true);
            if(needsRecreation) {
                currentCodeArea.updateCodeFields(behaviour);
                setAllHandlersForCodeArea(currentCodeArea);
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
        if(gameRunning&&model.getCurrentLevel().hasAi())view.getAICodeArea().deselectAll();
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(!evt.getPropertyName().equals("codeArea"))return;
        CodeArea codeArea = (CodeArea) evt.getNewValue();
//        if(codeArea.isEditable())
        setAllHandlersForCodeArea(codeArea);
    }
}