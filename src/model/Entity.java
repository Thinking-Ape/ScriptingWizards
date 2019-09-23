package model;

// an interface for classes that use Behaviour
// maybe make abstract class instead?

import model.enums.CContent;
import model.enums.Direction;
import model.enums.EntityType;
import model.enums.ItemType;

import java.util.stream.Stream;

//maybe no use at all?? -> instead behavior map in level
public class Entity {

    private String name;
    private Direction direction;
    private EntityType entityType;
    private ItemType item;

    public Entity(String name, Direction direction, EntityType entityType){
        this.name = name;
        this.direction = direction;
        this.entityType =entityType;
    }

    public Direction getDirection(){
        return direction;
    }
    public void setDirection(Direction direction){
        this.direction = direction;
    }
    public String getName(){return name;}

    public EntityType getEntityType() {
        return entityType;
    }

    public void setItem(ItemType item) {
        this.item = item;
    }

    public ItemType getItem() {
        return item;
    }

    public void deleteIdentity() {
        name = "";
    }
}
