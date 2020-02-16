package main.model.statement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum StatementType {

    FOR("for *\\(( *int +.* *= *.+;.+;.+ *)\\) *\\{",false),
    WHILE("while *\\( *(.*) *\\) *\\{",false),
    IF("if *\\( *(.*) *\\) *\\{",false),
    ELSE("else( +if *\\( *(.*) *\\) *)? *\\{",false),
    METHOD_CALL(".+\\..+\\( *(.*|(.* *, *)*.* *) *\\);",true),
    ASSIGNMENT("(.+ *= *.+;|.+\\+\\+;|.+--;)",false),
    DECLARATION("(.+ +.+ *= *.+;)|(.+ +.+;)",true),
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
