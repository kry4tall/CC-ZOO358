package org.apache.zookeeper.server.util;

import org.apache.zookeeper.server.Request;
import org.apache.zookeeper.server.quorum.Follower;
import org.apache.zookeeper.server.quorum.Leader;

import java.io.*;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

public class StateCollectingUtil {

    long nodeId;

    int type; // PROPOSE | ACK | COMMIT

    long zxid;

    String context;

    String messageToSend;

    static String stateFileDir;

    static {
        Properties properties = new Properties();
        try{
            InputStream in = ClassLoader.getSystemResourceAsStream("test.properties");
            properties.load(in);
            Objects.requireNonNull(in).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stateFileDir = properties.getProperty("state-file-dir"); //read from config
    }

    public StateCollectingUtil(long nodeId, String type, long zxid, String messageToSend) {
        this.nodeId = nodeId;
        switch (type) {
            case "PROPOSE":
                this.type = 0;
                break;
            case "ACK":
                this.type = 1;
                break;
            case "COMMIT":
                this.type = 2;
                break;
            case "INIT":
                this.type = 3;
                break;
        }
        this.zxid = zxid;
        this.messageToSend = messageToSend;
    }

    public void generateStateContextInit(Leader leader){
        this.context = leader.getLeaderStateString();
    }

    public void generateStateContextInit(Follower follower){
        this.context = follower.getFollowerStateString();
    }

    public void generateStateContextPropose(Leader leader){
        this.context = leader.getLeaderStateString();
    }

    public void generateStateContextPropose(Follower follower){
        this.context = follower.getFollowerStateString();
    }

    public void generateStateContextACK(Leader leader){
        this.context = leader.getLeaderStateString();
    }

    public void generateStateContextCommit(Follower follower){
        this.context = follower.getFollowerStateString();
    }

    public void generateStateContextCommit(Leader leader){
        this.context = leader.getLeaderStateString();
    }

    public void writeToFile(){
        String serverState = getServerState();
        //write to file path with \n
        try {
            String targetPath = stateFileDir+"/"+nodeId;
            File file = new File(targetPath);
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));
            bufferedWriter.append(serverState);
            bufferedWriter.append("\n");
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String typeToString(int type){
        if(type == 0)
            return "PROPOSE";
        else if(type == 1)
            return "ACK";
        else if(type == 2)
            return "COMMIT";
        else
            return "INIT";
    }

    public String getServerState() {
        return "StateCollectingUtil{" +
                "nodeId=" + nodeId +
                ", type=" + typeToString(type) +
                ", zxid=" + zxid +
                ", context='" + context + '\'' +
                ", messageToSend='" + messageToSend + '\'' +
                '}';
    }

}
