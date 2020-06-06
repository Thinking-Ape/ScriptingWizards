package main.model.statement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum StatementType {
    // visit https://regex101.com/ for more info!
    WHILE("^ *while *\\( *+(.+?) *+\\) *\\{ *$",false),
    FOR("^ *for *\\( *+(.+?) *+\\) *\\{ *$",false),
    IF("^ *if *\\( *+(.+?) *+\\) *\\{ *$",false),
    ELSE("^ *else( +if *\\( *+(.+?) *+\\) *+)? *+\\{ *+$",false),
    METHOD_CALL("^ *(([^ ]+?)\\.)([^ ]+?) *+\\( *+(.*) *+\\) *; *$",true),
    DECLARATION("^ *+([^ =]+?) +([^ ]+?) *+(|= *+(.+?) *+); *$",false),
    ASSIGNMENT("^ *+([^ =]+?) *+(= *+(.+?)|\\+\\+|--) *; *$",false),
    COMPLEX(".*",true),
    SIMPLE(" *",true)
    ;
    private final String regex;
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
