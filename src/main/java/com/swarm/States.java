package com.swarm;

import com.intellij.debugger.jdi.StackFrameProxyImpl;
import com.swarm.models.Session;

import java.util.List;

public class States {
    public static boolean isSteppedInto = false;
    public static List<StackFrameProxyImpl> lastStackFrames;

    public static Session currentSession = new Session();

    public static String URL = "http://localhost:8080/graphql";
}
