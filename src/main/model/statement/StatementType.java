package main.model.statement;

import main.utility.GameConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum StatementType {
    //TODO: isUnlocked not used
    // visit https://regex101.com/ for more info!
    FOR("^ *for *\\(( *int +"+ GameConstants.VARIABLE_NAME_REGEX+" *= *.+;.+;.+ *)\\) *\\{ *$",false),
    WHILE("^ *while *\\( *(.*) *\\) *\\{ *$",false),
    IF("^ *if *\\( *(.*) *\\) *\\{ *$",false),
    ELSE("^ *else( +if *\\( *(.*) *\\) *)? *\\{ *$",false),
    METHOD_CALL("^ *(("+GameConstants.VARIABLE_NAME_REGEX+")\\.)?("+GameConstants.VARIABLE_NAME_REGEX+")\\( *(.*|(.* *, *)*.* *) *\\) *; *$",true),
    METHOD_DECLARATION(GameConstants.METHOD_DECLARATION_REGEX,false),
    ASSIGNMENT("^ *("+ GameConstants.VARIABLE_NAME_REGEX+" *= *.+;|"+ GameConstants.VARIABLE_NAME_REGEX+" *\\+\\+ *;|"+ GameConstants.VARIABLE_NAME_REGEX+" *-- *;) *$",false),
    DECLARATION("^ *([A-Za-z]+ +"+ GameConstants.VARIABLE_NAME_REGEX+" *= *.+;)|([A-Za-z]+ +"+ GameConstants.VARIABLE_NAME_REGEX+" *;) *$",false),
    COMPLEX(".*",true),
    SIMPLE(" *",true)
    ;
    private String regex;
    private boolean isUnlocked;

    StatementType(String regex,boolean isUnlocked){
        this.regex = regex;
        this.isUnlocked = isUnlocked;
    }

    public static StatementType statementTypeFromString(String codeLine) {

        for(StatementType statementType : values()){
            Pattern pattern = Pattern.compile(statementType.regex);
            Matcher matcher = pattern.matcher(codeLine);
            if(matcher.matches())return statementType;
        }
        return COMPLEX;
    }
    public static Matcher getMatcher(StatementType statementType,String code) {

        Pattern pattern = Pattern.compile(statementType.regex);
        return pattern.matcher(code);
    }
    public void setUnlocked(boolean isUnlocked){
        this.isUnlocked = isUnlocked;
    }
    public String getRegex(){
        return regex;
    }
}
