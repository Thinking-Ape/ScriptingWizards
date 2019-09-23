package model.garbage_dump.condition;

@Deprecated
public interface Condition {
    //TODO: AND, OR, etc.
    boolean isTrue();

    String toText();
}
