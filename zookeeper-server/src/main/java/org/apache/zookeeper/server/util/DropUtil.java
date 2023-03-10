package org.apache.zookeeper.server.util;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Properties;

public class DropUtil {

    long nodeId;

    int type; // PROPOSE | ACK | COMMIT

    long zxid;

    static String syncFileDir;

    static String scenarioFilePath;

    static int nodeNum;

    static{
        Properties properties = new Properties();
        try{
            InputStream in = ClassLoader.getSystemResourceAsStream("test.properties");
            properties.load(in);
            Objects.requireNonNull(in).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        syncFileDir = properties.getProperty("sync-file-dir"); //read from config
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
                    System.out.println("Drop message: {sid: " + nodeId + ", type: " + typeToString(type) + ", zxid: " + zxid + "}.");
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

    public void syncWrite(long nodeId,long zxid, String type){
        //make file
        try {
            String targetPath = syncFileDir + "/" + zxid + "_" + nodeId + "_" + type;
            File file = new File(targetPath);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean syncRead(long zxid, String type){
        int count = 0;
        for(int sid = 0;sid<3;sid++)
        {
            String targetPath = syncFileDir + "/" + zxid + "_" + sid + "_" + type;
            File file = new File(targetPath);
            if (file.exists()) {
                count++;
            }
        }
        //enough files exist
        return count == nodeNum;
    }

    public static void deleteSyncFiles(){
        try {
            File file = new File(syncFileDir);
            File[] listFiles = file.listFiles();
            if(listFiles != null)
            {
                for(File f: listFiles)
                {
                    f.delete();
                }
            }
        } catch (Exception e) {
            System.out.println("Exception when delete: " + e.getMessage());
        }
    }

    public static void readNodeNumber() throws IOException {
        Properties properties = new Properties();
        InputStream in = ClassLoader.getSystemResourceAsStream("test.properties");
        properties.load(in);
        nodeNum = Integer.parseInt(properties.getProperty("nodeNum"));
        System.out.println("Current node num is " + nodeNum + ".");
    }
}
