package org.apache.zookeeper.server.util;

import java.util.HashSet;

public class TypeSelectingUtil {

    static HashSet<Integer> typeSet = new HashSet<>();

    static HashSet<Long> passedZxids = new HashSet<>();

    public static boolean isContained(int type){
        return type == 1 || type == 5;
    }

    public static void addPassedZxids(long passedZxid){
        passedZxids.add(passedZxid);
    }

    public static boolean isZxidPassed(long zxid){
        return passedZxids.contains(zxid);
    }

}
