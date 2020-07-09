package com.swarm.listeners;

import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.intellij.debugger.engine.events.DebuggerCommandImpl;
import com.intellij.debugger.impl.DebuggerContextImpl;
import com.intellij.debugger.impl.DebuggerManagerListener;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.debugger.jdi.StackFrameProxyImpl;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiJavaFile;
import com.sun.jdi.Method;
import com.swarm.utils.States;
import com.swarm.models.Invocation;
import com.swarm.models.Type;

import java.util.List;


public class DebuggerManagerListenerSwarm implements DebuggerManagerListener, DumbAware {

    private final Project project;
    private List<StackFrameProxyImpl> currentStackFrames;

    public DebuggerManagerListenerSwarm(Project project) {
        this.project = project;
    }

    @Override
    public void sessionCreated(DebuggerSession session) {
        session.getContextManager().addListener((newContext, event) -> {
            if(States.currentSession.getId() == 0) {
                return;
            }
            assert newContext.getDebugProcess() != null;
            newContext.getDebugProcess().getManagerThread().invoke(new DebuggerCommandImpl() {
                @Override
                protected void action(){
                    if (event.name().equals("PAUSE") && States.isSteppedInto) {
                        handleStepInto(newContext);
                    }
                    if (event.name().equals("PAUSE")) {
                        try {
                            assert newContext.getThreadProxy() != null;
                            States.lastStackFrames = newContext.getThreadProxy().frames();
                        } catch (EvaluateException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        });
    }

    private void handleStepInto(DebuggerContextImpl newContext) {
        try {
            States.isSteppedInto = false;
            assert newContext.getThreadProxy() != null;
            currentStackFrames = newContext.getThreadProxy().frames();

            if (isInvocation(currentStackFrames)) {
                makeInvocation();
            }
        } catch (EvaluateException e) {
            e.printStackTrace();
        }
    }

    private boolean isInvocation(List<StackFrameProxyImpl> currentStackFrames) {
        if (currentStackFrames.size() < States.lastStackFrames.size()) {
            //here it's certain that it wasn't an invocation because the current stackFrames count is less than before. We just return true
            return false;
        } else
            return currentStackFrames.size() != States.lastStackFrames.size(); //Here if it's not the same frame count, it's an invocation
    }

    //TODO:test this(run read action changed)
    private void makeInvocation() throws EvaluateException {
        Type invokedType = new Type();

        ReadAction.run(() -> {
            PsiJavaFile file = (PsiJavaFile) DebuggerManagerEx.getInstanceEx(project).getContext().getSourcePosition().getFile();
            invokedType.setSourceCode(file.getText());
            invokedType.setName(file.getName());
            invokedType.setFullPath(file.getVirtualFile().getPath());

            invokedType.setFullName(file.getPackageName());
            if(!invokedType.getFullName().equals("")) {
                invokedType.setFullName(invokedType.getFullName() + "." + file.getName());
            }
        });

        invokedType.setSession(States.currentSession);
        invokedType.create();

        Method invoked = currentStackFrames.get(0).location().method();
        com.swarm.models.Method invokedSwarmMethod = new com.swarm.models.Method();
        invokedSwarmMethod.setSignature(invoked.signature());
        invokedSwarmMethod.setName(invoked.name());
        invokedSwarmMethod.setType(invokedType);
        invokedSwarmMethod.create();


        Invocation invocation = new Invocation();
        invocation.setInvoking(DebugActionListener.invokingMethod);
        invocation.setInvoked(invokedSwarmMethod);
        invocation.setSession(States.currentSession);
        invocation.create();
    }
}
