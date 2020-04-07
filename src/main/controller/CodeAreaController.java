package main.controller;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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
import main.utility.SimpleEventListener;
import main.utility.Util;
import main.view.*;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeAreaController implements SimpleEventListener {

    private View view;
    private int currentIndex = 0;
    private boolean addBefore = false;
    private boolean isError = false;
    private boolean needsRecreation = false;
    private boolean showError;
    private boolean needToIncreaseCurrentIndex;
    private boolean compilerActive = true;
    private boolean neverShown = true;
    private boolean needToDecreaseCurrentIndex;
    // Maybe a feature for future versions: Select Multiple CodeFields
//    private SimpleSet<Integer> selectedIndexSet = new SimpleSet<>();

    public CodeAreaController(View view) {
        this.view = view;

        CodeArea.getInstance(CodeAreaType.PLAYER).addListener(this);
        CodeArea.getInstance(CodeAreaType.AI).addListener(this);

        setAllHandlersForCodeArea(CodeArea.getInstance(CodeAreaType.PLAYER));
        setAllHandlersForCodeArea(CodeArea.getInstance(CodeAreaType.AI));
    }
    public void setAllHandlersForCodeArea(CodeArea currentCodeArea) {
        for (CodeField codeField : currentCodeArea.getCodeFieldListClone()) {
            setHandlerForCodeField(codeField,currentCodeArea);
        }
        // Clicking the Icon above any of the two(three) CodeAreas will toggle the Compiler for that CodeArea
        currentCodeArea.getIcon().setOnMousePressed(keyEvent -> {
            if(View.getCurrentSceneState() != SceneState.LEVEL_EDITOR && currentCodeArea.isAi())return;
            compilerActive = !compilerActive;
            // if the compiler is inactive every code line may be edited
            if(!compilerActive){
                currentCodeArea.setEditable(true,true);
            }
            // upon reactivation the CodeArea will be recreated
            else {
                needsRecreation = true;
                handleCodeFieldEvent(currentCodeArea.getAllText(), currentCodeArea);
            }
            currentCodeArea.setIconActive(compilerActive);
            disableControlElements(!compilerActive,currentCodeArea);
        });

        // Make sure that the AI-CodeArea may only be edited within the Editor
        if(currentCodeArea.isAi() && View.getCurrentSceneState() != SceneState.LEVEL_EDITOR) currentCodeArea.setEditable(false);

        // When scrolling with the mouse on a CodeArea will scollTo through the codefields
        currentCodeArea.setOnScroll(evt -> {
            if(isError)return;
            // scrolling up or down will return a negative / positive value for y
            double y = evt.getDeltaY();
            int scroll = currentCodeArea.getScrollAmount();
            if(y < 0 && scroll+1<= currentCodeArea.getSize()-GameConstants.MAX_CODE_LINES)scroll ++;
            if(y > 0 && scroll-1 >= 0)scroll--;
            currentCodeArea.scollTo(scroll);

        });
        currentCodeArea.getUpBtn().setOnAction(actionEvent -> {
            if(isError)return;
            // Only to be safe, as UpBtn should be disabled in this case
            if(currentCodeArea.getScrollAmount() - 1<0)return;
            currentCodeArea.scollTo(currentCodeArea.getScrollAmount() - 1);
        });
        currentCodeArea.getDownBtn().setOnAction(actionEvent -> {
            if(isError)return;
            // Only to be safe, as DownBtn should be disabled in this case
            if(currentCodeArea.getScrollAmount() + 1 + GameConstants.MAX_CODE_LINES > currentCodeArea.getSize())return;
            currentCodeArea.scollTo(currentCodeArea.getScrollAmount()+ 1);
        });

        // Only visual
        currentCodeArea.getUpBtn().setOnMouseEntered(actionEvent -> currentCodeArea.getUpBtn().setEffect(GameConstants.HIGHLIGHT_BTN_EFFECT));
        currentCodeArea.getDownBtn().setOnMouseEntered(actionEvent -> currentCodeArea.getDownBtn().setEffect(GameConstants.HIGHLIGHT_BTN_EFFECT));
        currentCodeArea.getUpBtn().setOnMouseExited(actionEvent -> currentCodeArea.getUpBtn().setEffect(GameConstants.GLOW_BTN_EFFECT));
        currentCodeArea.getDownBtn().setOnMouseExited(actionEvent -> currentCodeArea.getDownBtn().setEffect(GameConstants.GLOW_BTN_EFFECT));

        // To prevent a bug where non-editable codefields can be edited by pressing Ctrl
        currentCodeArea.setOnKeyPressed(evt -> {
            if(currentCodeArea.isFocused() && !evt.isControlDown())
                if(currentCodeArea.getSelectedCodeField()!=null)currentCodeArea.getSelectedCodeField().requestFocus();
        });
    }


    private void setHandlerForCodeField(CodeField currentCodeField, CodeArea currentCodeArea) {
        currentCodeField.setOnMousePressed(event -> {
            if(View.getCurrentSceneState() != SceneState.LEVEL_EDITOR && currentCodeArea.isAi())return;
            if(currentCodeArea.isAi())view.getCodeArea().deselectAll();
            else view.getAICodeArea().deselectAll();

            needsRecreation = false;
            needToIncreaseCurrentIndex = false;
            needToDecreaseCurrentIndex = false;
            // clicking the current CodeField will do nothing
            if(currentCodeArea.getSelectedCodeField() == currentCodeField)return;

//            currentCodeArea.deselectAll();
            showError = true;

            if(compilerActive)
                handleCodeFieldEvent(currentCodeArea.getAllText(),currentCodeArea);

            if(!currentCodeArea.isEditable() )return;
            if(!isError){
                currentIndex = currentCodeArea.indexOfCodeField(currentCodeField);
                currentCodeArea.deselectAll();
                currentCodeArea.select(currentCodeField, Selection.NONE);
            }
        });

        currentCodeField.addListener((observableValue, s, t1) -> {

            //without this line the query currentCodeField.isEmpty() will return outdated values
            Platform.runLater(()->currentCodeField.setEmptyFlag(t1.matches(" *")));
            currentCodeField.autosize();
            if(Util.textIsTooLongForCodefield(t1,currentCodeField.getDepth()))currentCodeField.setText(s);
        });

        currentCodeField.setOnKeyPressed(event -> {
            // this is to circumvent a JavaFX Bug, which makes it possible to edit non-editable CodeFields by pressing Ctrl
            if(compilerActive && event.isControlDown() && !currentCodeField.isEditable())
                currentCodeArea.requestFocus();
            if(View.getCurrentSceneState() != SceneState.LEVEL_EDITOR && currentCodeArea.isAi())return;
//            if (gameRunning){
//                return;
//            }
            needsRecreation = false;
            addBefore = false;
            showError = false;
            needToIncreaseCurrentIndex = false;
            List<String> codeLines = currentCodeArea.getAllText();
            String forVariablePatternString = " *for *\\( *int +("+GameConstants.VARIABLE_NAME_REGEX+")(.+\\{)$";
            currentIndex = currentCodeArea.indexOfCodeField(currentCodeField);

            if(compilerActive && !isError && event.isControlDown() && event.getCode() == KeyCode.R){
                Matcher variableMatcher = Pattern.compile("^[a-zA-Z]+ +("+GameConstants.VARIABLE_NAME_REGEX+")( *;| +=.*;)$").matcher(currentCodeField.getText());
                if(!variableMatcher.matches())
                    variableMatcher = Pattern.compile(forVariablePatternString).matcher(currentCodeField.getText());
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
                        int depth = currentCodeArea.getSelectedCodeField().getDepth();
                        for(int i = currentIndex; i< codeLines.size(); i++){
                            if(depth > currentCodeArea.getCodeFieldListClone().get(i).getDepth())break;
                            // special case for-variable as it is declared at a higher depth than its visibility!
                            if(i==currentIndex && variableMatcher.pattern().toString().equals(forVariablePatternString))depth++;
                            String oldString = codeLines.get(i);
                            String newString = codeLines.get(i).replaceAll("^(.*[^a-zA-Z0-9]+|)"+varName+"([^a-zA-Z0-9]+.*)$","$1"+newName+"$2");

                            while(!oldString.equals(newString)){
                                oldString = newString;
                                newString = oldString.replaceAll("^(.*[^a-zA-Z0-9]+|)"+varName+"([^a-zA-Z0-9]+.*)$","$1"+newName+"$2");
                            }
                            if(i == currentIndex)if(Util.textIsTooLongForCodefield(newString, currentCodeField.getDepth())) {
                                new Alert(Alert.AlertType.ERROR, "The resulting code field is too long!").showAndWait();
                                return;
                            }
                            codeLines.set(i, newString);
                        }
                    }
                }
                needsRecreation = true;
                showError = true;
                handleCodeFieldEvent(codeLines, currentCodeArea);
                return;
            }

            switch (event.getCode()) {
                case F5:
                    compilerActive = !compilerActive;
                    if(!compilerActive){
                        if(neverShown){
                            new Alert(Alert.AlertType.NONE,"Compiler has been deactivated!\nPress F5 or click the icon above the codearea to reactivate it!", ButtonType.OK).showAndWait();
                            neverShown = false;
                        }
                        currentCodeArea.setEditable(true,true);
                    }
                    else {
                        needsRecreation = true;
                    }
                    currentCodeArea.setIconActive(compilerActive);
                    disableControlElements(!compilerActive,currentCodeArea);
                    break;

                case ENTER:
                    showError = true;
                    int selectedIndex = currentCodeField.getCaretPosition();
                    if (selectedIndex == 0 && !currentCodeField.isEmpty()) {
                        addBefore = true;
                    }
                    String complexStatementRegex = GameConstants.COMPLEX_STATEMENT_REGEX;
                    // visit https://regex101.com/ for more info
                    String simpleStatementRegex = GameConstants.SIMPLE_STATEMENT_REGEX;

                    if(!isError)currentCodeArea.deselectAll();
                    String textAfterBracket = "";

                    Matcher matcherSimple = Pattern.compile(simpleStatementRegex).matcher(currentCodeField.getText());
                    Matcher matcherComplex = Pattern.compile(complexStatementRegex).matcher(currentCodeField.getText());

                    /* The following code lines are comments, because MethodDeclarations have been postponed
                     * until their usage or necessity is clear to me
                     */

//                    Matcher matcherMethodDec = Pattern.compile(GameConstants.METHOD_DECLARATION_REGEX).matcher(currentCodeField.getText());
//                    if(matcherMethodDec.matches()){
//                        textAfterBracket = matcherMethodDec.group(6);
//                        codeLines.set(currentIndex, currentCodeField.getText().replaceAll("\\{.++", "{"));
//                    }
//                    else
                    if (matcherSimple.matches()) {
                        textAfterBracket = matcherSimple.group(2);
                        codeLines.set(currentIndex, currentCodeField.getText().replaceAll(";.++", ";"));
                    } else if (matcherComplex.matches()) {
                        textAfterBracket = matcherComplex.group(2);
                        codeLines.set(currentIndex, currentCodeField.getText().replaceAll("\\{.++", "{"));
                    }

                    boolean needsBrackets = false;
                    if (matcherComplex.matches() /*|| matcherMethodDec.matches()*/) {
                        if (currentCodeArea.getBracketBalance() > 0) needsBrackets = true;
                    }
                    int scrollAmount = currentCodeArea.getScrollAmount() + 1;
                    //TODO: dont understand this: < currentCodeArea.getSize() ? currentCodeArea.getScrollAmount() + 1 : currentCodeArea.getSize() - 1 - GameConstants.MAX_CODE_LINES;

                    if (needsBrackets) {
                        codeLines.add(currentIndex + 1, "}");
                    }
                    // if the text after '{' is also complex add another '}'
                    if (textAfterBracket.matches(complexStatementRegex))
                        codeLines.add(currentIndex + 1, "}");

                    if (!textAfterBracket.matches(" *} *"))
                        codeLines.add(addBefore ? currentIndex : currentIndex+1, textAfterBracket);
                    else
                        codeLines.add(addBefore ? currentIndex : currentIndex+1, "");

                    // If addBefore is true, we dont want to increase the currentIndex which will happen if needToIncreaseCurrentIndex is true
                    if (!addBefore)
                        needToIncreaseCurrentIndex = true;
                    // If the added CodeField we want to edit is outside of our visible CodeArea we need to scroll!
                    if (!addBefore && currentIndex + 1 >= GameConstants.MAX_CODE_LINES + currentCodeArea.getScrollAmount())
                        currentCodeArea.scollTo(scrollAmount);
                    break;
                case BACK_SPACE:
                    if (currentCodeArea.getSize() == 1) break;
                    if (!currentCodeField.isEditable()) {
                        if(!compilerActive){
                            currentCodeField.setText("");
                            break;
                        }
                        if(currentIndex == 0) break;
                        CodeField prevCodeField = currentCodeArea.getCodeFieldListClone().get(currentIndex-1);
                        if( prevCodeField != null && prevCodeField.isEmpty()) codeLines.remove(currentIndex-1);
                        else if (currentIndex > 0){
                            currentIndex--;
                            currentCodeArea.select(currentIndex, Selection.END);
                        }
                        break;
                    }
                    if(currentCodeField.isEmpty()){
                        // The following lines are there to make sure that if you remove a complex statement,
                        // the closing bracket will be removed as well
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
                        // If there will be at least 1 CodeField left remove the current one
                        if(currentCodeArea.getCodeFieldListClone().size()>1){
                            showError = true;
                            codeLines.remove(currentIndex);
                        }
                        currentIndex = (currentIndex > 0) ? currentIndex-1 : currentIndex;
                        // This should never happen but better be safe than sorry
                        if(codeLines.size()==0)codeLines.add("");
                        // Deleting a line will put us into the CodeField above -> we need to scroll up
                        scrollAmount = currentCodeArea.getScrollAmount();
                        if(scrollAmount > 0)
                            currentCodeArea.scollTo(scrollAmount-1);
                        break;
                    }
                    else if(currentIndex == 0) break;
                    else if(codeLines.get(currentIndex-1).matches(" *")&&currentCodeField.getCaretPosition() == 0){
                        codeLines.remove(currentIndex-1);
                        scrollAmount = currentCodeArea.getScrollAmount()-1 > 0 ? currentCodeArea.getScrollAmount()-1 : 0;
                        if(currentIndex<= currentCodeArea.getScrollAmount())
                            currentCodeArea.scollTo(scrollAmount);
                        needToDecreaseCurrentIndex = true;
                    }
                    break;
                case DELETE:
                    if(currentCodeArea.getSize()==1) {
                        break;
                    }
                    if (!currentCodeField.isEditable()) {
                        if(!compilerActive){
                            currentCodeField.setText("");
                            break;
                        }
                        if(currentIndex == currentCodeArea.getSize()-1) {
                            break;
                        }
                        CodeField nextCodeField = currentCodeArea.getCodeFieldListClone().get(currentIndex+1);
                        if( nextCodeField!= null && nextCodeField.isEmpty()) codeLines.remove(currentIndex+1);
                        else if (currentIndex < currentCodeArea.getSize() - 1){
                            currentIndex++;
                            currentCodeArea.select(currentIndex, Selection.END);
                        }
                        break;
                    }

                    if (currentCodeField.isEmpty()) {
                        if(currentIndex == currentCodeArea.getSize()-1) {
                            break;
                        }
                        showError = true;
                        codeLines.remove(currentIndex);

                        if (currentIndex == currentCodeArea.getSize()) {

                            scrollAmount = currentCodeArea.getScrollAmount() - 1 > 0 ? currentCodeArea.getScrollAmount() - 1 : 0;
                            if (currentIndex <= currentCodeArea.getScrollAmount())
                                currentCodeArea.scollTo(scrollAmount);
                            needToDecreaseCurrentIndex = true;
                        }
                    }
                    else if(currentCodeField.getCaretPosition() == currentCodeField.getText().length() && currentIndex < currentCodeArea.getSize()-1){
                        CodeField nextCodeField = currentCodeArea.getCodeFieldListClone().get(currentIndex+1);
                        if ((nextCodeField.isEmpty()||nextCodeField.getText().matches(" *"))){
                            showError = true;
                            codeLines.remove(currentIndex+1);
                        }
                    }
                    break;
                case UP:
                    showError = true;
                    if (isError && compilerActive) break;
                    if (currentIndex <= 0) {
                        currentIndex = 0;
                        currentCodeArea.select(currentIndex,Selection.END);
                        return;
                    }
                    scrollAmount = currentCodeArea.getScrollAmount() - 1 > 0 ? currentCodeArea.getScrollAmount() - 1 : 0;

                    if (currentIndex <= currentCodeArea.getScrollAmount())
                        currentCodeArea.scollTo(scrollAmount);
                    // Pressing Alt enables moving the current CodeField
                    if(event.isAltDown()){
                        int startIndex = currentIndex;
                        int endIndex = startIndex;
                        boolean isBalanced = currentCodeArea.getBracketBalance() == 0;
                        if(isBalanced && codeLines.get(startIndex).matches(GameConstants.COMPLEX_STATEMENT_REGEX))
                            endIndex = currentCodeArea.findNextBracketIndex(startIndex, currentCodeField.getDepth());
                        codeLines = Util.moveItems(codeLines,startIndex, endIndex,-1);
                        needsRecreation = true;
                    }
                    currentIndex--;
                    currentCodeArea.select(currentIndex, Selection.END);
                    break;
                case DOWN:
                    showError = true;
                    if (isError && compilerActive) break;

                    if (currentIndex >= currentCodeArea.getSize() - 1) {
                        currentIndex = currentCodeArea.getSize() - 1;
                        currentCodeArea.select(currentIndex, Selection.END);
                        return;
                    }
                    scrollAmount = currentCodeArea.getScrollAmount() + 1 < currentCodeArea.getSize() ? currentCodeArea.getScrollAmount() + 1 : currentCodeArea.getSize() - 1 - GameConstants.MAX_CODE_LINES;
                    if (currentIndex + 1 >= GameConstants.MAX_CODE_LINES + currentCodeArea.getScrollAmount())
                        currentCodeArea.scollTo(scrollAmount);
                    // Pressing Alt enables moving the current CodeField
                    if(event.isAltDown()){
                        int startIndex = currentIndex;
                        int endIndex = startIndex;
                        boolean isBalanced = currentCodeArea.getBracketBalance() == 0;
                        if(isBalanced && codeLines.get(startIndex).matches(GameConstants.COMPLEX_STATEMENT_REGEX))
                            endIndex = currentCodeArea.findNextBracketIndex(startIndex, currentCodeField.getDepth());
                        codeLines = Util.moveItems(codeLines,startIndex, endIndex,1);
                        needsRecreation = true;
                    }
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
                    if (!currentCodeField.isEditable()) return;
                    Platform.runLater(() -> currentCodeField.fireEvent(new KeyEvent(event.getEventType(), event.getCharacter(), event.getText(), KeyCode.RECORD, false, false, false, false)));
                    return;
            }
            if(compilerActive) handleCodeFieldEvent(codeLines,currentCodeArea);
        });
    }

    private void handleCodeFieldEvent(List<String> codeLines, CodeArea currentCodeArea) {
        needsRecreation = needsRecreation || (codeLines.size() != currentCodeArea.getSize());
        Label errorLabel = currentCodeArea.isAi() ? view.getErrorLabelAI() : view.getErrorLabel();
        errorLabel.setVisible(false);
        if(currentCodeArea.isAi())view.getCodeArea().deselectAll();
        else view.getAICodeArea().deselectAll();
        try {
            ComplexStatement behaviour = CodeParser.parseProgramCode(codeLines,currentCodeArea.getCodeAreaType());
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
                if(needToIncreaseCurrentIndex)
                    currentIndex++;
                if(needToDecreaseCurrentIndex)
                    currentIndex--;
                currentCodeArea.select(currentIndex, Selection.END);
            }
        }catch (Exception e){
            isError = true;
            disableControlElements(true,currentCodeArea);
            currentCodeArea.setEditable(false);
            // After the compiler is inactive the current codefield might be a '}'
            if(!currentCodeArea.getCodeFieldListClone().get(currentIndex).getText().equals("}"))
                currentCodeArea.setEditable(currentIndex,true);
            if(GameConstants.DEBUG)e.printStackTrace();
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
        else if(View.getCurrentSceneState() == SceneState.LEVEL_EDITOR) view.getAICodeArea().setDisable(b);
        if(View.getCurrentSceneState() == SceneState.LEVEL_EDITOR && codeArea.isAi())
            view.getLevelEditorModule().getSaveLevelBtn().setDisable(b);
    }

    @Override
    public void update(Object o) {
        CodeArea codeArea = (CodeArea) o;
        setAllHandlersForCodeArea(codeArea);
    }
}