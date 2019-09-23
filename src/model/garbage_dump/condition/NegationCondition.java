package model.garbage_dump.condition;

public class NegationCondition implements  Condition{
    Condition condition;

    public NegationCondition(Condition condition){
        this.condition = condition;
    }

    @Override
    public boolean isTrue() {
        return !condition.isTrue();
    }

    public String toText(){
        return condition == null ? "" :"!(" + condition.toText() +")";
    }
}
