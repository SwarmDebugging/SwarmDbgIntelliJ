package com.swarm.listeners;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointListener;
import org.jetbrains.annotations.NotNull;

public class breakpointListener implements XBreakpointListener<XBreakpoint<?>> {

    private final Project project;

    public breakpointListener(Project project) {
        this.project = project;
    }

    @Override
    public void breakpointAdded(@NotNull XBreakpoint breakpoint) {
        NotificationGroup notificationGroup = new NotificationGroup("NotificationBreakpointAdded", NotificationDisplayType.BALLOON, true);

        int x = breakpoint.getSourcePosition().getLine();

        notificationGroup.createNotification("Hello from first plugin",
                "line number: " + x,
                NotificationType.INFORMATION,
                null).notify(project);
    }

    @Override
    public void breakpointRemoved(@NotNull XBreakpoint<?> breakpoint) {
        NotificationGroup notificationGroup = new NotificationGroup("NotificationBreakpointAdded", NotificationDisplayType.BALLOON, true);

        int x = breakpoint.getSourcePosition().getLine();

        notificationGroup.createNotification("Hello from first plugin",
                "line number: " + x,
                NotificationType.INFORMATION,
                null).notify(project);
    }
}
