package com.swarm.toolWindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.Topic;
import com.swarm.models.Task;

public class CurrentTaskProvider {
    private static Task task;
    private static MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();

    static Task getTask() {return task;}

    static void setTask(Task newTask){
        task = newTask;
        final Handler currentTaskProvider = messageBus.syncPublisher(Handler.CURRENT_TASK_TOPIC);
        currentTaskProvider.currentTaskAction(task);
    }

    public interface Handler {
        Topic<CurrentTaskProvider.Handler> CURRENT_TASK_TOPIC = Topic.create("Swarm current task", CurrentTaskProvider.Handler.class);

        void currentTaskAction(Task task);
    }
}
