package com.swarm.utils;

import com.intellij.debugger.jdi.StackFrameProxyImpl;

import java.util.List;

public class States {
    public static boolean isSteppedInto = false;
    public static List<StackFrameProxyImpl> lastStackFrames;
}
