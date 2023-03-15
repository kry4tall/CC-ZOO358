package org.apache.zookeeper.server.util;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class DropUtil {

    long nodeId;

    int type; // PROPOSE | ACK | COMMIT

    long zxid;

    static String scenarioFilePath;

    static int nodeNum;

    static CountDownLatch proposeCDL;

    static CountDownLatch ackCDL;

    static CountDownLatch commitCDL;

    static{
        Properties properties = new Properties();
        try{
            InputStream in = ClassLoader.getSystemResourceAsStream("test.properties");
            properties.load(in);
            Objects.requireNonNull(in).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        scenarioFilePath = properties.getProperty("scenario-file-path"); //read from config
    }

    public DropUtil(long nodeId, String type, long zxid) {
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
        }
        this.zxid = zxid;
    }

    public boolean isToDrop(Boolean stdoutFlag) {
        ArrayList<String> lines = new ArrayList<>();
        try {
            File f = new File(scenarioFilePath);
            InputStream in = Files.newInputStream(f.toPath());
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while(true)
            {
                line = br.readLine();
                if(line != null) {
                    lines.add(line);
                }
                else
                    break;
            }
            Objects.requireNonNull(in).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String current = nodeId +  " " + type + " " + zxid;
        for(String line : lines) {
            if(line.equals(current)) {
                if (stdoutFlag)
                    System.out.println("Drop message: {sid: " + nodeId + ", type: " + typeToString(type) + ", zxid: " + zxid + "} before processing.");
                return true;
            }
        }
        return false;
    }

    public String typeToString(int type){
        if(type == 0)
            return "PROPOSE";
        else if(type == 1)
            return "ACK";
        else
            return "COMMIT";
    }

    public static void syncWrite(String type){
        switch (type) {
            case "PROPOSE":
                proposeCDL.countDown();
                break;
            case "ACK":
                ackCDL.countDown();
                break;
            case "COMMIT":
                commitCDL.countDown();
                break;
        }
    }

    public static void syncRead(String type) throws InterruptedException {
        switch (type) {
            case "PROPOSE":
                proposeCDL.await();
                break;
            case "ACK":
                ackCDL.await();
                break;
            case "COMMIT":
                commitCDL.await();
                break;
        }
    }

    public static void readNodeNumber() throws IOException {
        Properties properties = new Properties();
        InputStream in = ClassLoader.getSystemResourceAsStream("test.properties");
        properties.load(in);
        nodeNum = Integer.parseInt(properties.getProperty("nodeNum"));
        proposeCDL = new CountDownLatch(nodeNum);
        ackCDL = new CountDownLatch(nodeNum);
        commitCDL = new CountDownLatch(nodeNum);
    }
}
