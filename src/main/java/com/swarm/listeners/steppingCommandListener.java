package com.swarm.listeners;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebuggerManagerListener;
import com.intellij.xdebugger.impl.XDebuggerManagerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.debugger.DebugEventListener;

public class steppingCommandListener implements XDebuggerManagerListener {

    @Override
    public void currentSessionChanged(@Nullable XDebugSession previousSession, @Nullable XDebugSession currentSession) {
        var x = previousSession.getCurrentStackFrame();
        var y = currentSession.getCurrentStackFrame();
        int i = 0;
    }
}
