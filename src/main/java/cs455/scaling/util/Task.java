package cs455.scaling.util;

import java.nio.channels.SelectionKey;

public class Task {

    private SelectionKey key;

    public Task(SelectionKey key){
        this.key = key;
    }
    public SelectionKey getKey(){
        return key;
    }
}
