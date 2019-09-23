package model;

import model.statement.ComplexStatement;

@Deprecated
public class Enemy extends Entity {
    private int health; //Todo: notwendig?
    ComplexStatement behaviour;
    public Enemy(){
        super(null,null,null);
    }

}
