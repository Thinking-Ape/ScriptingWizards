package controller;

import javafx.application.Platform;
import javafx.scene.Node;
import model.util.GameConstants;
import model.Model;
import model.statement.ComplexStatement;
import parser.CodeParser;
import view.CodeArea;
import view.CodeField;
import view.View;

public class CodeAreaController {

    private CodeArea codeArea;
    private View view;
    private Model model;
    private int errorLine = 0;
    private int currentIndex = 0;
    private boolean selectEnd = false;
    private String errorMessage;
    private boolean silentError = false;
    private boolean addBefore;
    private int addedStatementsBalance = 0;
    private boolean isError = false;
    private boolean gameRunning = false;

    public CodeAreaController(View view, Model model) {
        this.model =model;
        this.view = view;
    }
    public void setAllHandlersForCodeArea(boolean isAi) {
        this.codeArea = isAi ? view.getAICodeArea() : view.getCodeArea();
        for (CodeField codeField : codeArea.getCodeFieldListClone()) {
            setHandlerForCodeField(codeField,isAi);

            codeField.textProperty().addListener((observable, oldValue, newValue) -> {
                Platform.runLater(()->codeField.setEmptyFlag(newValue.equals("")));

            });
        }
        codeArea.getScrollBar().valueProperty().addListener((observableValue, number, t1) -> {
           codeArea.scroll(Math.round(t1.floatValue()));
        });
    }
    private void setHandlerForCodeField(CodeField currentCodeField, boolean isAi) {
        currentCodeField.setOnMousePressed(event -> {
            if(gameRunning)return;
            view.getCodeArea().deselectAll();
            if(view.getAICodeArea()!=null)view.getAICodeArea().deselectAll();
            addedStatementsBalance = 0;
            codeArea = !isAi ? view.getCodeArea() : view.getAICodeArea();
            codeArea.select(currentCodeField, true);
            silentError = false;
            selectEnd = true;
            currentIndex = codeArea.indexOfCodeField(currentCodeField);
            if(codeArea.getSelectedCodeField() == currentCodeField) return;
//            codeArea.deselectAll();
            recreateCodeAreaIfCodeCorrect(codeArea,currentCodeField,isAi);
//            if(tryToRecompileCodeArea(codeArea,silentError)!=null){
//                view.setCodeArea(codeArea);
//                setAllHandlersForCodeArea(codeArea);
//            }
//            codeArea.draw();
//            codeArea.select(currentIndex,true);
        });
        currentCodeField.setOnKeyPressed(event -> {
            if(gameRunning)return;
            addedStatementsBalance = 0;
            codeArea = !isAi ? view.getCodeArea() : view.getAICodeArea();
            CodeArea codeAreaClone = codeArea.createClone();
//            view.setCodeArea(codeAreaClone);
//            codeAreaClone.draw();
            addBefore = false;
            silentError = false;
            currentIndex = codeArea.indexOfCodeField(currentCodeField);
            switch (event.getCode()){
                case ENTER:
                    if(codeAreaClone.getCodeFieldListClone().size() >= GameConstants.MAX_CODE_LINES*2){
                        return;
                    }
//                    if(!currentCodeField.isEditable())return;
                    int selectedIndex = currentCodeField.getCaretPosition();
                    if(selectedIndex == 0 && !currentCodeField.isEmpty()){
                        addBefore = true;
                    }
                    int depth = currentCodeField.getDepth();

//                    boolean hasThoughtOfBrackets = textAfterCursor.matches(".*}");
                    String complexStatementRegex = ".*\\{ *";

                    codeArea.deselectAll();
                    CodeField bracketCodeField = null;
                    if(/*textBeforeCursor*/currentCodeField.getText().matches(complexStatementRegex)){
                        if(codeArea.getBracketBalance() > 0 /*|| hasThoughtOfBrackets*/) bracketCodeField = new CodeField("}",depth,false);
                        if(!addBefore)depth++;
                    }

                    CodeField newCodeField = new CodeField(/*textAfterCursor*/"",depth,true);

                    if(!addBefore)currentIndex++;
                    codeAreaClone.addNewCodeFieldAtIndex(currentIndex,newCodeField);
                    addedStatementsBalance++;
                    if(bracketCodeField != null){
                        codeAreaClone.addNewCodeFieldAtIndex(currentIndex+1,bracketCodeField);
                        addedStatementsBalance++;
                    }
//                    if(recompileCode(codeAreaClone)==null){
//                        codeAreaClone.removeCodeField(newCodeField);
//                        codeAreaClone.removeCodeField(bracketCodeField);
//                    }currentIndex++;
                    selectEnd = false;
                    break;

                case BACK_SPACE:
                    if(!currentCodeField.isEditable()){
                        codeArea.deselectAll();
                        currentIndex--;
                        selectEnd = true;
                        break;
                    }
                    //TODO: if Codefield isnt empty!
                    if((currentIndex == 0 || currentCodeField.getCaretPosition() != 0)&&!currentCodeField.isEmpty()){
                        silentError = true;
                        break;
                    }
                    if(currentCodeField.getCaretPosition() == 0 && !currentCodeField.isEmpty())return;
                    if(currentCodeField.isEmpty()){
                        boolean isLastCodeFieldSelected = currentIndex >= codeArea.getSize()-1;
                        if(!isLastCodeFieldSelected){
                            CodeField nextCodeField = codeArea.getCodeFieldListClone().get(currentIndex+1);
                            boolean isBraceOfSameDepth = nextCodeField.getText().equals("}")&&nextCodeField.getDepth()==currentCodeField.getDepth();
                            if(nextCodeField.getDepth() > currentCodeField.getDepth()||isBraceOfSameDepth){
                                bracketCodeField = codeArea.findNextBracket(currentIndex+1,currentCodeField.getDepth());
                                if (bracketCodeField != null){
                                    codeAreaClone.removeCodeField(bracketCodeField); //remove at position? not codefield?
                                    addedStatementsBalance--;
                                }
                            }
                        }
                        if(codeAreaClone.getCodeFieldListClone().size()>1){
                            codeAreaClone.removeCodeField(currentCodeField);
                            addedStatementsBalance--;
                        }
                        currentIndex = (currentIndex > 0) ? currentIndex-1 : currentIndex;
                        selectEnd=true;
                        break;
                    }
                    if(currentIndex == 0)break; //TODO:
                    CodeField prevCodeField = codeArea.getCodeFieldListClone().get(currentIndex-1);
                    if(!currentCodeField.getText().equals("") && prevCodeField.getText().matches(" *")){ //TODO: vereinheitlicht " *" anstelle von ""?
                        codeAreaClone.removeCodeField(prevCodeField);
                        addedStatementsBalance--;
//                        removeCodeField1 = prevCodeField;
                        currentIndex--;
                        selectEnd = false;
                    }else
                        silentError = true;
                    break;

                case DELETE:
                if(!currentCodeField.isEditable())return;
                int lastIndex = currentCodeField.getText().length();
                if(currentIndex == codeArea.getSize()-1) {
                    silentError = true;
                    break;
                }
                    CodeField nextCodeField = codeArea.getCodeFieldListClone().get(currentIndex+1);
                    if(currentCodeField.getText().equals("")){
                        codeAreaClone.removeCodeField( currentCodeField);
                        addedStatementsBalance--;
//                        codeFieldsToRemoveList.add(currentCodeField);
//                        removeCodeField1 = currentCodeField;
                        selectEnd = true;
                        silentError = true;
                    }
                    //TODO: delete "nextCodeField.getText().matches(" *")"
                    else if(currentCodeField.getCaretPosition() == lastIndex && nextCodeField.getText().matches(" *")){
                        //TODO: String oldText = nextCodeField.getText();
                        codeAreaClone.removeCodeField( nextCodeField);
                        addedStatementsBalance--;
//                        codeFieldsToRemoveList.add(nextCodeField);
//                        removeCodeField1 = nextCodeField;
                        //TODO: currentCodeField.appendText(oldText);
                        selectEnd = true;
                    }
//                    else if(currentCodeField.getCaretPosition() == lastIndex) return;
                    else silentError = true;
                    break;
                case UP:
                    if (currentIndex == 0)return;
                    //TODO: pass selection to method
                    codeArea.deselectAll();
                    currentIndex--;
                    selectEnd = true;
                    break;
                case DOWN:
                    if (currentIndex == codeArea.getSize()-1)return;
                    //TODO: pass selection to method
                    codeArea.deselectAll();
                    currentIndex++;
                    selectEnd = true;
                    break;
                case LEFT:
                case RIGHT:
                    return;
                default:
                    if(!currentCodeField.isEditable())return;
//                    codeArea.deselectAll();

//                    if(recompileCode() == null)view.getBtnExecute().setDisable(true);
//                    else view.getBtnExecute().setDisable(false);
//                    codeArea.select(currentIndex+1,true);
                    silentError = true;
            }
            recreateCodeAreaIfCodeCorrect(codeAreaClone,currentCodeField,isAi);
            setAllHandlersForCodeArea(isAi);
//            codeArea.getCodeFieldListClone().remove(newCodeField);
//            codeArea.getCodeFieldListClone().add(currentIndex,removeCodeField1);
        });
//        currentCodeField.setOnKeyTyped(event -> {
//            if(event.getCharacter().equals(";")||event.getCharacter().equals("{")){
//                if(currentCodeField.getCaretPosition() == currentCodeField.getText().length()){
//                    tryToRecompileCodeArea();
//                    return;
//                }
//            }
//        });
    }

    private void recreateCodeAreaIfCodeCorrect(CodeArea codeAreaClone, CodeField currentCodeField,boolean isAi) {
        Platform.runLater(()->{
            CodeArea newCodeArea = null;
            try {
                newCodeArea = tryToRecompileCodeArea(codeAreaClone,silentError,isAi);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if(newCodeArea != null){
                isError = false;
//                int i = 1;
//                for(CodeField codeField : codeFieldsToAddList){
//                    codeArea.addNewCodeFieldAtIndex(currentIndex+i,codeField);
//                    System.out.println("ok");
//                    i++; //TODO: eliminate confusing code!
//                }
//                if(codeFieldsToAddList.size() > 0)currentIndex++;
                if(isAi)view.setAiCodeArea(newCodeArea);
                else view.setCodeArea(newCodeArea);
                newCodeArea.draw();
                setAllHandlersForCodeArea(isAi);
                if(!silentError) newCodeArea.select(currentIndex,selectEnd);
            } else{
                if(!isError){
                    errorLine = currentIndex;
                    boolean isEditable = currentCodeField.isEditable();
                    codeArea.setEditable(false);
//                  codeArea.
                    if(isEditable)currentCodeField.setEditable(true);
//                   codeArea.select(currentIndex,selectEnd);
                    isError = true;
                }
                else codeArea.setEditable(true);
            }
            if(silentError)currentCodeField.setStyle(null);
            });
    }

    private CodeArea tryToRecompileCodeArea(CodeArea codeArea, boolean silentError, boolean isAi) throws IllegalAccessException {
        ComplexStatement complexStatement = recompileCode(codeArea,isAi);
        CodeArea codeArea1 = isAi ? view.getAICodeArea() : view.getCodeArea();
        if(complexStatement != null){
            if(!isAi)model.getCurrentLevel().setPlayerBehaviour(complexStatement);
            else {
                model.getCurrentLevel().setAiBehaviour(complexStatement);
                setAllHandlersForCodeArea(true);
            }

            if(codeArea1.getSize()>errorLine)
            codeArea1.getCodeFieldListClone().get(errorLine).resetStyle();
            view.getMsgLabel().setText("");
            if(silentError)return null;
            return new CodeArea(complexStatement);
//            return true;
        }
        else {
            view.getBtnExecute().setDisable(true);
            if(silentError)return null;
            view.getMsgLabel().setText(errorMessage );//+" In Line " +errorLine);
//            if(errorLine > currentIndex)errorLine -= addedStatementsBalance;
            codeArea1.highlightError(errorLine); //errorline
            return null;
        }
    }



    //TODO: transfer to CodeParser
    private ComplexStatement recompileCode(CodeArea codeArea,boolean isAi) {
        CodeParser codeParser =  new CodeParser(codeArea.getAllText(),!isAi);
        ComplexStatement complexStatement = null;
        try{
            complexStatement = codeParser.parseProgramCode();
            view.getBtnExecute().setDisable(false);
        }catch (Exception e){
//            errorLine = codeParser.getCurrentLine()-1;
            errorMessage = e.getMessage();
            if(errorMessage==null){
                e.printStackTrace();
            }
//            System.out.println(Arrays.toString(e.getStackTrace()));
//            view.getBtnExecute().setDisable(true);
//            view.getMsgLabel().setText(e.getMessage() +" In Line " +errorLine);
//            e.printStackTrace();
        }
        return complexStatement;
    }


    public void setGameRunning(boolean gameRunning) {
        this.gameRunning = gameRunning;
    }
}