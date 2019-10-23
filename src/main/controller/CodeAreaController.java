package main.controller;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import main.model.Model;
import main.model.statement.ComplexStatement;
import main.parser.CodeParser;
import main.utility.GameConstants;
import main.view.CodeArea;
import main.view.CodeField;
import main.view.View;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

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
            text.setFont(new Font(currentCodeField.getFont().getName(), GameConstants.FONT_SIZE));
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
                    if(bracketCodeField != null){
                        codeAreaClone.addNewCodeFieldAtIndex(currentIndex+1,bracketCodeField);
                    }
//                    if(recompileCode(codeAreaClone)==null){
//                        codeAreaClone.removeCodeField(newCodeField);
//                        codeAreaClone.removeCodeField(bracketCodeField);
//                    }currentIndex++;
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

                        if(currentIndex==codeArea.getSize())currentIndex--;
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
                    if(event.isControlDown()){
                        codeAreaClone.moveCodeField(currentIndex,true);
                    }
                    //TODO: pass selection to method
//                    codeArea.deselectAll();
                    currentIndex--;
//                    codeArea.select(currentIndex,Selection.END);
                    silentError = false;

                    break;
                case DOWN:
                    if (currentIndex >= codeArea.getSize()-1){
                        currentIndex = codeArea.getSize() -1;
//                        codeArea.select(currentIndex,Selection.END);
                        return;
                    }
                    if(event.isControlDown()){
                        codeAreaClone.moveCodeField(currentIndex,false);
                    }
                    //TODO: pass selection to method
//                    codeArea.deselectAll();
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
//        currentCodeField.setOnKeyTyped(event -> {
//            if(event.getCharacter().equals(";")||event.getCharacter().equals("{")){
//                if(currentCodeField.getCaretPosition() == currentCodeField.getText().length()){
//                    tryToRecompileCodeArea();
//                    return;
//                }
//            }
//        });
    }


    //TODO: fix this mess!
    private void recreateCodeAreaIfCodeCorrect(CodeArea codeAreaClone, CodeField currentCodeField, boolean isAi) {
//        Platform.runLater(()->{
            CodeArea newCodeArea = tryToRecompileCodeArea(codeAreaClone,silentError,isAi);

            if(newCodeArea != null){
                isError = false;
                view.setCodeArea(newCodeArea,isAi);
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
            }
//            return true;
        }
        else {
            view.getBtnExecute().setDisable(true);
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
        CodeParser codeParser =  new CodeParser(codeArea.getAllText(),!codeArea.isAi());
        ComplexStatement complexStatement = null;
        try{
            complexStatement = codeParser.parseProgramCode();
            codeArea.setEditable(true);
            if(model.getCurrentLevel().getOriginalMap().findSpawn().getX()!=-1){
                view.getBtnExecute().setDisable(false);
                if(codeArea.isAi())view.getLevelEditorModule().getSaveLevelBtn().setDisable(false);
            }
            if(codeArea.isAi())view.getCodeArea().setDisable(false);
            else view.getAICodeArea().setDisable(false);
        }catch (Exception e){
//            errorLine = codeParser.getCurrentLine()-1;
            errorMessage = e.getMessage();
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

        setAllHandlersForCodeArea((CodeArea) evt.getNewValue());
    }

    public boolean isError() {
        return isError;
    }
}