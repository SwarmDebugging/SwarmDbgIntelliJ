package com.swarm;

import com.intellij.debugger.jdi.StackFrameProxyImpl;
import com.sun.jdi.Method;

import java.util.List;
import java.util.Vector;

public class States {
    //contains last event type(StepInto, StepOver, ...)
    public static String eventType = "";

    //contains all invocation in this format -> (invoking, invoked)
    public static Vector<Vector<Method>> callStack = new Vector<Vector<Method>>();

    public static List<StackFrameProxyImpl> lastStackFrames;
}
