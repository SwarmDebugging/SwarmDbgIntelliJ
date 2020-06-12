package com.swarm.toolWindow;

import com.intellij.debugger.DebuggerManager;
import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.xdebugger.impl.frame.XDebugView;
import com.swarm.States;
import com.swarm.tools.HTTPRequests;

import javax.swing.*;

public class PopupMenuBuilder {

    public static JPopupMenu buildProductPopupMenu(int productId, Project project, int developerId) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem createNewTask = new JMenuItem("Create a new Task");
        createNewTask.addActionListener(actionEvent -> {
            CreateTaskDialog createTaskDialog = new CreateTaskDialog(project, productId, developerId);//States.developerId);
            createTaskDialog.showAndGet();
        });
        popupMenu.add(createNewTask);
        return popupMenu;
    }

    public static JPopupMenu buildTaskPopupMenu(Project project, int taskId, int developerId, ToolWindow toolWindow) {
        JMenuItem markAsDone = new JMenuItem("Mark As Done");
        markAsDone.addActionListener(actionEvent -> {
            HTTPRequests.taskDone(taskId);
        });

        JMenuItem newSwarmSession = new JMenuItem("Start a New Swarm Debugging Session");
        newSwarmSession.addActionListener(actionEvent -> {
            //try to launch a new session
            var instance = DebuggerManagerEx.getInstanceEx(project).getSessions();
            if(DebuggerManagerEx.getInstanceEx(project).getSessions().size()>0) {
                //there's a debugging session
                int sessionId = HTTPRequests.sessionStart(developerId, taskId);
                States.currentSessionId = sessionId;
                if(sessionId != -1) {
                    //then change view to session in progress
                    SessionToolWindow sessionToolWindow = new SessionToolWindow(sessionId, toolWindow, project);
                    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
                    Content content = contentFactory.createContent(sessionToolWindow.getContent(), "", false);
                    toolWindow.getContentManager().removeAllContents(true);
                    toolWindow.getContentManager().addContent(content);
                } else {
                    //there has been an error in session creation
                }
            } else {
                //there's no debugging session show error message or start new session

            }
        });

        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(newSwarmSession);
        popupMenu.add(markAsDone);
        return popupMenu;
    }
}
