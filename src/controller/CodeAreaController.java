package controller;

import javafx.application.Platform;
import javafx.scene.text.Text;
import utility.GameConstants;
import model.Model;
import model.statement.ComplexStatement;
import parser.CodeParser;
import view.CodeArea;
import view.CodeField;
import view.View;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class CodeAreaController implements PropertyChangeListener {

    private View view;
    private Model model;
    private int errorLine = 0;
    private int currentIndex = 0;
    private boolean selectEnd = false;
    private String errorMessage;
    private boolean silentError = false;
    private boolean addBefore;
    private int addedStatementsBalance = 0; //TODO: erase!
    private boolean isError = false;
    private boolean gameRunning = false;

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
        codeArea.addListenerToScrollbar((observableValue, number, t1) -> codeArea.scroll(Math.round(t1.floatValue())));
    }
    private void setHandlerForCodeField(CodeField currentCodeField,boolean isAi) {
        currentCodeField.setOnMousePressed(event -> {
            if(gameRunning)return;
            if(view.getCodeArea().getSelectedCodeField()!=currentCodeField)view.getCodeArea().deselectAll();
            if(view.getAICodeArea().getSelectedCodeField()!=currentCodeField)view.getAICodeArea().deselectAll();
            addedStatementsBalance = 0;
            CodeArea codeArea = !isAi ? view.getCodeArea() : view.getAICodeArea();
//            codeArea.select(currentCodeField, true);
            silentError = false;
            selectEnd = true;
            currentIndex = codeArea.indexOfCodeField(currentCodeField);
            if(codeArea.getSelectedCodeField() == currentCodeField) return;
            recreateCodeAreaIfCodeCorrect(codeArea,currentCodeField,isAi);
        });
        currentCodeField.addListener((observableValue, s, t1) -> {
            Text text = new Text(t1);
            if(text.getLayoutBounds().getWidth() > currentCodeField.getLayoutBounds().getWidth()-15)currentCodeField.setText(s);
        });
        currentCodeField.setOnKeyPressed(event -> {
            if(gameRunning)return;
            addedStatementsBalance = 0;
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
//            setAllHandlersForCodeArea();
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

    private void recreateCodeAreaIfCodeCorrect(CodeArea codeAreaClone, CodeField currentCodeField, boolean isAi) {
        Platform.runLater(()->{
            CodeArea newCodeArea = null;

            CodeArea codeArea = isAi ? view.getAICodeArea() : view.getCodeArea();
            newCodeArea = tryToRecompileCodeArea(codeAreaClone,silentError,isAi);

            if(newCodeArea != null){
                isError = false;
//                int i = 1;
//                for(CodeField codeField : codeFieldsToAddList){
//                    codeArea.addNewCodeFieldAtIndex(currentIndex+i,codeField);
//                    System.out.println("ok");
//                    i++; //TODO: eliminate confusing code!
//                }
//                if(codeFieldsToAddList.size() > 0)currentIndex++;
                view.setCodeArea(newCodeArea,isAi);
//                setAllHandlersForCodeArea(codeArea);
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
//                else codeArea.setEditable(true);
            }
            if(silentError)currentCodeField.setStyle(null);
            });
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
            }
//            return true;
        }
        else {
            view.getBtnExecute().setDisable(true);
            if(codeArea.isAi())view.getCodeArea().setDisable(true);
            else view.getAICodeArea().setDisable(true);
            if(codeArea.isAi())view.getLevelEditorModule().getSaveLevelBtn().setDisable(true);
            if(silentError)return null;
            view.getMsgLabel().setText(errorMessage );//+" In Line " +errorLine);
//            if(errorLine > currentIndex)errorLine -= addedStatementsBalance;
            codeArea.highlightError(errorLine); //errorline
            return null;
        }
        return null; //TODO: evaluate this whole mess!
    }



    //TODO: transfer to CodeParser
    private ComplexStatement recompileCode(CodeArea codeArea) {
        CodeParser codeParser =  new CodeParser(codeArea.getAllText(),!codeArea.isAi());
        ComplexStatement complexStatement = null;
        try{
            complexStatement = codeParser.parseProgramCode();
            view.getBtnExecute().setDisable(false);
            if(codeArea.isAi())view.getLevelEditorModule().getSaveLevelBtn().setDisable(false);
            if(codeArea.isAi())view.getCodeArea().setDisable(false);
            else view.getAICodeArea().setDisable(false);
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

    public boolean isGameRunning() {
        return gameRunning;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(!evt.getPropertyName().equals("codeArea"))return;

        setAllHandlersForCodeArea((CodeArea) evt.getNewValue());
    }
}