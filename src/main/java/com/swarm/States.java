package com.swarm;

import com.intellij.debugger.jdi.StackFrameProxyImpl;

import java.util.List;

public class States {
    public static boolean isSteppedInto = false;

    public static List<StackFrameProxyImpl> lastStackFrames;

    public static int currentSessionId = 20;
}