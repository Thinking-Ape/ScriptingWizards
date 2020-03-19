package main.utility;

import java.util.*;

/** This class was created because HashSet will use hashCode() instead of the equals() Method to see whether 2 items are identical.
 *  I wanted to use the equals() Method instead, so I didnt have to implement hashCode() Methods within all my classes.
 * @param <T>
 */
public class SimpleSet<T> extends HashSet<T> {

    public SimpleSet(Set<T> set){
        super(set);
    }
    public SimpleSet(){
        super();
    }

    @Override
    public boolean add(T t) {
        for(T t2 : this){
            if(t2.equals(t))return false;
        }
        return super.add(t);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean output = true;
        for(T t : c) {
            for (T t2 : this) {
                if (t2.equals(t)) output = false;
            }
            if(output) super.add(t);
        }
        return output;
    }

    @Override
    public boolean contains(Object o){
        for(T t : this){
            if(t.equals(o))return true;
        }
        return false;
    }
}
