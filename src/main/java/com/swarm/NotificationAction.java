package com.swarm;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.swarm.tools.HTTPRequests;
import org.jetbrains.annotations.NotNull;

public class NotificationAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String id = "";
        //id = HTTPRequests.createDeveloper("developer");
        NotificationGroup notificationGroup = new NotificationGroup("NotificationTest", NotificationDisplayType.BALLOON, true);

        notificationGroup.createNotification("Hello from first plugin",
                id,
                NotificationType.INFORMATION,
                null).notify(e.getProject());
    }
}
