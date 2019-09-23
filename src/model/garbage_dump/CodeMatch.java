package parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO: mit StatementType zusammenfassen!
@Deprecated
public enum CodeMatch {
    ANY, //TODO: delete?
    FOR,
    WHILE,
    IF,
    ELSE,
    ASSIGNMENT,
    METHOD_CALL;

    public static Matcher getMatcher(CodeMatch codeMatch, String target){
        String forRegex = "for\\((.*)\\)\\{";
        String whileRegex = "while\\((.*)\\)\\{";
        String ifRegex = "if\\((.*)\\)\\{";
        String elseRegex = "else( if\\(( *.* *)\\) *)? *\\{";
//        String declRegex = ".* .*=.*;";
        String asRegex = "(.*=.*;|.*\\+\\+;|.*--;)";
        String mcRegex = ".*\\..*\\(.*\\);";
        String anyRegex = "("+forRegex+"|"+whileRegex+"|"+ifRegex+"|"+elseRegex+")";
        String regex="";
        switch (codeMatch){
            case ANY:
                regex = anyRegex;
                break;
            case FOR:
                regex = forRegex;
                break;
            case WHILE:
                regex = whileRegex;
                break;
            case IF:
                regex = ifRegex;
                break;
            case ELSE:
                regex = elseRegex;
                break;
//            case DECLARATION:
//                regex = declRegex;
//                break;
            case ASSIGNMENT:
                regex = asRegex;
                break;
            case METHOD_CALL:
                regex = mcRegex;
                break;
        }

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(target);
        return matcher;
    }

}
