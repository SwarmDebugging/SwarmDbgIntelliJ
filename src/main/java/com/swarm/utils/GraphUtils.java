package com.swarm.utils;

import com.swarm.models.Invocation;
import com.swarm.models.Type;

import javax.swing.*;
import java.util.ArrayList;

public class GraphUtils {

    public static JFrame buildGraphVisualisationFromInvocationList(ArrayList<Invocation> invocations) {

        for (Invocation invocation: invocations) {
            Type invokedType = invocation.getInvoked().getType();
            Type invokingType = invocation.getInvoking().getType();
            /*graph.addVertex(invokedType);
            graph.addVertex(invokingType);
            graph.addEdge(invokedType, invokingType);*/
        }




        return null;
    }
}
