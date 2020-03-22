package main.controller;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import main.model.Model;
import main.model.statement.ComplexStatement;
import main.parser.CodeParser;
import main.utility.GameConstants;
import main.utility.SimpleSet;
import main.view.CodeArea;
import main.view.CodeField;
import main.view.SceneState;
import main.view.View;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeAreaController implements PropertyChangeListener {

    private View view;
    private Model model;
    private int errorLine = 0;
    private int currentIndex = 0;
    private String errorMessage;
    private boolean silentError = false;
    private boolean addBefore;
    private boolean isError = false;
    private boolean gameRunning = false;
    private SimpleSet<Integer> selectedIndexSet = new SimpleSet<>();

    public CodeAreaController(View view, Model model) {
        this.model =model;
        this.view = view;
        view.addPropertyChangeListener(this);
    }
    private void setAllHandlersForCodeArea(CodeArea codeArea) {
//        this.isAi = odeArea.isAi();
//        this.codeArea = odeArea;
        for (CodeField codeField : codeArea.getCodeFieldListClone()) {
            setHandlerForCodeField(codeField,codeArea.isAi());

            codeField.textProperty().addListener((observable, oldValue, newValue) -> {
                Platform.runLater(()->codeField.setEmptyFlag(newValue.equals("")));

            });
        }
        if(codeArea.isAi() && view.getCurrentSceneState() != SceneState.LEVEL_EDITOR)codeArea.setEditable(false);
//        codeArea.addListenerToScrollbar((observableValue, number, t1) -> codeArea.scroll(Math.round(t1.floatValue())));
        codeArea.setOnScroll(evt -> {
            if(isError)return;
            double y = evt.getDeltaY();
            int dy = codeArea.getScrollAmount();
            if(y < 0 && dy+1<=codeArea.getSize()-GameConstants.MAX_CODE_LINES)dy ++;
            if(y > 0 && dy-1 >= 0)dy--;
            codeArea.scroll(dy);

        });
        codeArea.getUpBtn().setOnAction(actionEvent -> {
            if(isError)return;
            codeArea.scroll(codeArea.getScrollAmount() - 1);
        });
        codeArea.getDownBtn().setOnAction(actionEvent -> {
            if(isError)return;
            codeArea.scroll(codeArea.getScrollAmount()+ 1);
        });
        codeArea.getUpBtn().setOnMouseEntered(actionEvent -> {
            codeArea.getUpBtn().setEffect(GameConstants.HIGHLIGHT_BTN_EFFECT);
        });
        codeArea.getDownBtn().setOnMouseEntered(actionEvent -> {

            codeArea.getDownBtn().setEffect(GameConstants.HIGHLIGHT_BTN_EFFECT);
        });
        codeArea.getUpBtn().setOnMouseExited(actionEvent -> {
            codeArea.getUpBtn().setEffect(GameConstants.GLOW_BTN_EFFECT);
        });
        codeArea.getDownBtn().setOnMouseExited(actionEvent -> {

            codeArea.getDownBtn().setEffect(GameConstants.GLOW_BTN_EFFECT);
        });
    }
    private void setHandlerForCodeField(CodeField currentCodeField,boolean isAi) {
        currentCodeField.setOnMousePressed(event -> {

            if(gameRunning)return;

            CodeArea codeArea = !isAi ? view.getCodeArea() : view.getAICodeArea();
            if(codeArea.getSelectedCodeField() == currentCodeField) return;
            if(!isError){
            if(view.getCodeArea().getSelectedCodeField()!=currentCodeField)view.getCodeArea().deselectAll();
            if(view.getAICodeArea().getSelectedCodeField()!=currentCodeField)view.getAICodeArea().deselectAll();
                codeArea.select(currentCodeField, Selection.NONE);}
            silentError = false;
            currentIndex = codeArea.indexOfCodeField(currentCodeField);
            recreateCodeAreaIfCodeCorrect(codeArea,currentCodeField,isAi);
        });
        currentCodeField.addListener((observableValue, s, t1) -> {
            currentCodeField.autosize();
            Text text = new Text(t1);
            text.setFont(GameConstants.CODE_FONT);
            if(text.getLayoutBounds().getWidth() > currentCodeField.getMaxWidth()-GameConstants.SCREEN_WIDTH/130)currentCodeField.setText(s);
            // without this ctrl-backspace will delete "}" (dont know why though)
            if(s.equals("}")&&!t1.equals("}"))currentCodeField.setText("}");
        });
        currentCodeField.setOnKeyPressed(event -> {
            if(gameRunning)return;
            CodeArea codeArea = !isAi ? view.getCodeArea() : view.getAICodeArea();
            CodeArea codeAreaClone = codeArea.createClone();
//            view.setCodeArea(codeAreaClone);
//            codeAreaClone.draw();
            addBefore = false;
            silentError = false;
            currentIndex = codeArea.indexOfCodeField(currentCodeField);
            switch (event.getCode()){
                case ENTER:
//                    if(!currentCodeField.isEditable())return;
                    int selectedIndex = currentCodeField.getCaretPosition();
                    if(selectedIndex == 0 && !currentCodeField.isEmpty()){
                        addBefore = true;
                    }
                    int depth = currentCodeField.getDepth();
//                    boolean hasThoughtOfBrackets = textAfterCursor.matches(".*}");
                    String complexStatementRegex = "^[^{]+?\\{(.*)$";
                    String simpleStatementRegex = "^(?!(for *\\())[^{]+?;(.++)$";

                    codeArea.deselectAll();
                    CodeField bracketCodeField = null;
                    String textAfterBracket = "";
                    Matcher matcherComplex = Pattern.compile(complexStatementRegex).matcher( currentCodeField.getText());
                    Matcher matcherSimple = Pattern.compile(simpleStatementRegex).matcher( currentCodeField.getText());
                    if(matcherSimple.matches()){
                        textAfterBracket = matcherSimple.group(2);
                        currentCodeField.setText(currentCodeField.getText().replaceAll(";.++", ";"));
                    }
                    else if(matcherComplex.matches()){
                        textAfterBracket = matcherComplex.group(1);
                        currentCodeField.setText(currentCodeField.getText().replaceAll("\\{.++", "{"));
                    }

                    if(matcherComplex.matches()){
                        if(codeArea.getBracketBalance() > 0 /*|| hasThoughtOfBrackets*/) bracketCodeField = new CodeField("}",depth,false);
                        if(!addBefore)depth++;
                    }CodeField newCodeField = new CodeField(textAfterBracket,depth,true);
                    if(codeArea.getBracketBalance() != 0 && textAfterBracket.matches(complexStatementRegex))codeAreaClone.addNewCodeFieldAtIndex(currentIndex+1, new CodeField("}",depth,false));
                    int scrollAmount = codeArea.getScrollAmount()+1 < codeArea.getSize() ? codeArea.getScrollAmount()+1 : codeArea.getSize()-1-GameConstants.MAX_CODE_LINES;
                    //TODO:
                    if(currentIndex+1>=GameConstants.MAX_CODE_LINES+ codeArea.getScrollAmount())codeArea.scroll(scrollAmount);
                    if(!addBefore)currentIndex++;
                    codeAreaClone.addNewCodeFieldAtIndex(currentIndex,newCodeField);
                    if(bracketCodeField != null){
                        codeAreaClone.addNewCodeFieldAtIndex(currentIndex+1,bracketCodeField);
                    }
                    break;

                case BACK_SPACE:
                    if(!currentCodeField.isEditable()){
                        codeArea.deselectAll();
                        if(currentIndex>0)currentIndex--;
                        break;
                    }
                    //TODO: if Codefield isnt empty!
                    if((currentIndex == 0 || currentCodeField.getCaretPosition() != 0)&&!currentCodeField.isEmpty()){
                        silentError = true;
                        break;
                    }
                    if(currentCodeField.getCaretPosition() == 0 && !currentCodeField.isEmpty()){
//                        System.out.println("dasfds");
                        return;
                    }
                    if(currentCodeField.isEmpty()){
                        boolean isLastCodeFieldSelected = currentIndex >= codeArea.getSize()-1;
                        if(!isLastCodeFieldSelected){
                            CodeField nextCodeField = codeArea.getCodeFieldListClone().get(currentIndex+1);
                            boolean isBraceOfSameDepth = nextCodeField.getText().equals("}")&&nextCodeField.getDepth()==currentCodeField.getDepth();
                            if(nextCodeField.getDepth() > currentCodeField.getDepth()||isBraceOfSameDepth){
                                bracketCodeField = codeArea.findNextBracket(currentIndex+1,currentCodeField.getDepth());
                                if (bracketCodeField != null){
                                    codeAreaClone.removeCodeField(bracketCodeField); //remove at position? not codefield?

                                }
                            }
                        }
                        if(codeAreaClone.getCodeFieldListClone().size()>1){
                            codeAreaClone.removeCodeField(currentCodeField);

                        }
                        currentIndex = (currentIndex > 0) ? currentIndex-1 : currentIndex;

                        break;
                    }
                    if(currentIndex == 0)break;
                    CodeField prevCodeField = codeArea.getCodeFieldListClone().get(currentIndex-1);
                    if(!currentCodeField.getText().equals("") && prevCodeField.getText().matches(" *")){ //TODO: vereinheitlicht " *" anstelle von ""?
                        codeAreaClone.removeCodeField(prevCodeField);

                        scrollAmount = codeArea.getScrollAmount()-1 > 0 ? codeArea.getScrollAmount()-1 : 0;
                        if(currentIndex<= codeArea.getScrollAmount())
                            codeArea.scroll(scrollAmount);
//                        removeCodeField1 = prevCodeField;
                        currentIndex--;

                    }else
                        silentError = true;
                    break;

                case DELETE:
                    if(!currentCodeField.isEditable()){
                        codeArea.deselectAll();
                        if(currentIndex<codeArea.getSize()-1)currentIndex++;

                        break;
                    }

                    if(currentIndex == codeArea.getSize()-1) {
                        silentError = true;
                        break;
                    }
                    if(currentCodeField.isEmpty()||currentCodeField.getText().matches(" *")){
                        codeAreaClone.removeCodeField( currentCodeField);

                        if(currentIndex==codeArea.getSize()){

                            scrollAmount = codeArea.getScrollAmount()-1 > 0 ? codeArea.getScrollAmount()-1 : 0;
                            if(currentIndex<= codeArea.getScrollAmount())
                                codeArea.scroll(scrollAmount);
                            currentIndex--;
                        }
                        //                        codeFieldsToRemoveList.add(currentCodeField);
                        //                        removeCodeField1 = currentCodeField;

//                        silentError = true;
                    }

                    //TODO: delete "nextCodeField.getText().matches(" *")"
                    else if(currentCodeField.isEmpty()||currentCodeField.getText().matches(" *")||currentCodeField.getCaretPosition() == currentCodeField.getText().length() ){
                        CodeField nextCodeField = codeArea.getCodeFieldListClone().get(currentIndex+1);
                        if ((nextCodeField.isEmpty()||nextCodeField.getText().matches(""))){
                        //TODO: String oldText = nextCodeField.getText();
                        codeAreaClone.removeCodeField( nextCodeField);

    //                        codeFieldsToRemoveList.add(nextCodeField);
    //                        removeCodeField1 = nextCodeField;
                        //TODO: currentCodeField.appendText(oldText);

                    }}
    //                    else if(currentCodeField.getCaretPosition() == lastIndex) return;
                    else {
                        silentError = true;
                    }
                    break;
                case UP:
                    if (currentIndex <= 0){
                        currentIndex = 0;
//                        codeArea.select(currentIndex,Selection.END);
                        return;
                    }
                    scrollAmount = codeArea.getScrollAmount()-1 > 0 ? codeArea.getScrollAmount()-1 : 0;
                    if(currentIndex<= codeArea.getScrollAmount())
                        codeArea.scroll(scrollAmount);
                    if(event.isControlDown()){
                        codeAreaClone.moveCodeField(currentIndex, true);
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
                    silentError = false;

                    break;
                case DOWN:
                    if (currentIndex >= codeArea.getSize()-1){
                        currentIndex = codeArea.getSize() -1;
                        codeArea.select(currentIndex,Selection.END);
                        return;
                    }
                    scrollAmount = codeArea.getScrollAmount()+1 < codeArea.getSize() ? codeArea.getScrollAmount()+1 : codeArea.getSize()-1-GameConstants.MAX_CODE_LINES;
                    if(currentIndex+1>=GameConstants.MAX_CODE_LINES+ codeArea.getScrollAmount())codeArea.scroll(scrollAmount);
                    if(event.isControlDown()){
                        codeAreaClone.moveCodeField(currentIndex, false);
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
                    silentError = false;

                    break;
                case LEFT:
                case RIGHT:
                    return;
                    //LINES BELOW ARE A WORKAROUND FOR ANOTHER JAVAFX BUG
                case RECORD:
                    silentError = true;
                    break;
                default:
//                    selectedIndexSet.clear();
//                    selectedIndexSet.add(currentIndex);
                    if(!currentCodeField.isEditable())return;
                    Platform.runLater(()->currentCodeField.fireEvent(new KeyEvent(event.getEventType(), event.getCharacter(), event.getText(), KeyCode.RECORD, false,false,false,false )));
                    return;
//
//                    codeArea.deselectAll();

//                    if(recompileCode() == null)view.getBtnExecute().setDisable(true);
//                    else view.getBtnExecute().setDisable(false);
//                    codeArea.select(currentIndex+1,true);
            }
            recreateCodeAreaIfCodeCorrect(codeAreaClone,currentCodeField,isAi);
        });
    }


    //TODO: fix this mess!
    private void recreateCodeAreaIfCodeCorrect(CodeArea codeAreaClone, CodeField currentCodeField, boolean isAi) {
//        Platform.runLater(()->{
            CodeArea newCodeArea = tryToRecompileCodeArea(codeAreaClone,silentError,isAi);


            if(newCodeArea != null){
                isError = false;
                view.setCodeArea(newCodeArea,isAi);
//                newCodeArea.markCodeFields(selectedIndexSet);
                if(!silentError)
//                    Platform.runLater(()->
                        newCodeArea.select(currentIndex,Selection.END);
//                );
            } else{
                if(!isError){
                    CodeArea codeArea = isAi ? view.getAICodeArea() : view.getCodeArea();
                    errorLine = currentIndex;
                    boolean isEditable = currentCodeField.isEditable();
                    codeArea.setEditable(false);
                    if(isEditable)currentCodeField.setEditable(true);
                    isError = true;
                }
            }
            if(silentError)currentCodeField.setStyle(null);
//            });
    }

    private CodeArea tryToRecompileCodeArea(CodeArea codeArea2, boolean silentError,boolean isAi) {
        ComplexStatement complexStatement = recompileCode(codeArea2);
        CodeArea codeArea = isAi ? view.getAICodeArea() : view.getCodeArea();
        if(complexStatement != null){
            if(!isAi){
                model.getCurrentLevel().setPlayerBehaviour(complexStatement);
            }
            else {
                model.getCurrentLevel().setAiBehaviour(complexStatement);
//                setAllHandlersForCodeArea();
            }

            if(codeArea.getSize()>errorLine)
            codeArea.getCodeFieldListClone().get(errorLine).resetStyle();
            view.getMsgLabel().setText("");
            if(silentError)return null;
            try {
                return new CodeArea(complexStatement,codeArea.isAi());
            }catch (IllegalArgumentException e){
                view.getMsgLabel().setText(e.getMessage());
                if(GameConstants.DEBUG)e.printStackTrace();
            }
//            return true;
        }
        else {
            codeArea.getUpBtn().setDisable(true);
            codeArea.getDownBtn().setDisable(true);
            view.getBtnExecute().setDisable(true);
            view.getStoreCodeBtn().setDisable(true);
            codeArea.setEditable(false);
            codeArea.getSelectedCodeField().setEditable(true);
            if(codeArea.isAi())view.getCodeArea().setDisable(true);
            else view.getAICodeArea().setDisable(true);
            if(codeArea.isAi())view.getLevelEditorModule().getSaveLevelBtn().setDisable(true);
            if(silentError)return null;
            view.getMsgLabel().setText(errorMessage );

            codeArea.highlightError(errorLine); //errorline
            return null;
        }
        return null; //TODO: evaluate this whole mess!

    }



    //TODO: transfer to CodeParser
    private ComplexStatement recompileCode(CodeArea codeArea) {
        ComplexStatement complexStatement = null;
        try{
            complexStatement = CodeParser.parseProgramCode(codeArea.getAllText(),!codeArea.isAi());
            codeArea.setEditable(true);
            if(model.getCurrentLevel().getOriginalMap().findSpawn().getX()!=-1){
                view.getBtnExecute().setDisable(false);

                view.getStoreCodeBtn().setDisable(false);
                if(codeArea.isAi())view.getLevelEditorModule().getSaveLevelBtn().setDisable(false);
            }
            if(codeArea.isAi())view.getCodeArea().setDisable(false);
            else view.getAICodeArea().setDisable(false);
        }catch (Exception e){
//            errorLine = codeParser.getCurrentLine()-1;
            errorMessage = e.getMessage();
            if(GameConstants.DEBUG)e.printStackTrace();
            if(errorMessage==null){
                e.printStackTrace();
            }
        }
        return complexStatement;
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
        if(codeArea.isEditable())
        setAllHandlersForCodeArea(codeArea);
    }

    public boolean isError() {
        return isError;
    }
}