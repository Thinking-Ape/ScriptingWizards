package model.garbage_dump.condition;


public class AndCondition implements Condition {

    Condition condition1;
    Condition condition2;

    public AndCondition(Condition condition1,Condition condition2){
        this.condition1 = condition1;
        this.condition2 = condition2;
    }


    @Override
    public boolean isTrue(){
        return condition1.isTrue() && condition2.isTrue();
    }


    public String toText(){
        return condition1 == null || condition2 == null ? "" : "(" + condition1.toText() + ") && (" + condition2.toText() + ")";
    }
}
