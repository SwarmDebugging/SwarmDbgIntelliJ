package com.swarm.toolWindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.Topic;

public class treeSelectionProvider {
    private static Object treeNode;
    private static final MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();

    static Object getTreeNode() {return treeNode;}

    public static void setTreeNode(Object newNode){
        treeNode = newNode;
        final Handler treeSelectionProvider = messageBus.syncPublisher(Handler.TREE_SELECTION_TOPIC);
        treeSelectionProvider.treeSelectionAction(treeNode);
    }

    public interface Handler {
        Topic<treeSelectionProvider.Handler> TREE_SELECTION_TOPIC = Topic.create("Swarm Selected Tree Node", treeSelectionProvider.Handler.class);

        void treeSelectionAction(Object treeNode);
    }
}
