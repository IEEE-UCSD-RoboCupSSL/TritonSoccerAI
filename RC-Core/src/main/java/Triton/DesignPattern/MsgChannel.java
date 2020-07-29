package Triton.DesignPattern;

import java.util.HashMap;

import Triton.Detection.DetectionData;

public class MsgChannel { 
    
    private static MsgChannel single_instance = null; 

    private HashMap<String, AbstractData> channel; 
  
    // private constructor restricted to this class itself 
    private MsgChannel() {
        channel = new HashMap<String, AbstractData>();
    }
  
    public static MsgChannel getInstance() { 
        if (single_instance == null) 
            single_instance = new MsgChannel(); 
  
        return single_instance; 
    } 

    public static AbstractData get(String s) {
        AbstractData data = null;
        try {
            data = getInstance().channel.get(s);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public static void publish(String s, AbstractData data) {
        getInstance().channel.put(s, data);
    }
} 