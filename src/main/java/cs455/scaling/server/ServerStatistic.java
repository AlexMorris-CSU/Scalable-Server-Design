package cs455.scaling.server;

import java.util.ArrayList;

public class ServerStatistic {

    private int sentSum;
    private int totalClients;
    private ArrayList<Integer> arrayList;

    public ServerStatistic(int sentSum, int totalClients){
        this.sentSum = sentSum;
        this.totalClients = totalClients;
        arrayList = new ArrayList<>();
    }

    public void addToArray(int value){
        arrayList.add(value);
    }
    public ArrayList<Integer> getArray(){
        return arrayList;
    }
    public int getSentSum(){
        return sentSum;
    }
    public int getTotalClients(){
        return totalClients;
    }
}
