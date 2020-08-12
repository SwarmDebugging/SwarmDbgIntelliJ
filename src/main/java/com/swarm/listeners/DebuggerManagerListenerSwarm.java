package com.swarm.listeners;

import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.intellij.debugger.engine.events.DebuggerCommandImpl;
import com.intellij.debugger.impl.DebuggerContextImpl;
import com.intellij.debugger.impl.DebuggerManagerListener;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.debugger.impl.DebuggerUtilsEx;
import com.intellij.debugger.jdi.StackFrameProxyImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.util.PsiTreeUtil;
import com.sun.jdi.Method;
import com.swarm.toolWindow.ProductToolWindow;
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
    public void sessionRemoved(DebuggerSession session) {
        int i = 0;
    }

    @Override
    public void sessionCreated(DebuggerSession session) {
        session.getContextManager().addListener((newContext, event) -> {
            if (ProductToolWindow.getCurrentSessionId() == 0) {
                return;
            }
            assert newContext.getDebugProcess() != null;
            newContext.getDebugProcess().getManagerThread().invoke(new DebuggerCommandImpl() {
                @Override
                protected void action() {
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

    private void makeInvocation() throws EvaluateException {
        Type invokedType = new Type();

        ReadAction.run(() -> {
            PsiFile file = DebuggerManagerEx.getInstanceEx(project).getContext().getSourcePosition().getFile();
            invokedType.setSourceCode(file.getText());
            invokedType.setFullPath(file.getVirtualFile().getPath());
        });

        invokedType.setSession(ProductToolWindow.getCurrentSession());

        try {
            ApplicationManager.getApplication().runReadAction(() -> {
                PsiMethod psiMethod = (PsiMethod) DebuggerUtilsEx.getContainingMethod(DebuggerManagerEx.getInstanceEx(project).getContext().getSourcePosition());
                PsiClass psiClass = PsiTreeUtil.getParentOfType(psiMethod, PsiClass.class);

                PsiClass newClass;
                while ((newClass = PsiTreeUtil.getParentOfType(psiClass, PsiClass.class)) != null) {
                    psiClass = newClass;
                }

                invokedType.setName(psiClass.getName());
                invokedType.setFullName(psiClass.getQualifiedName());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        invokedType.create();

        Method invoked = currentStackFrames.get(0).location().method();
        com.swarm.models.Method invokedSwarmMethod = new com.swarm.models.Method();
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiMethod psiMethod = (PsiMethod) DebuggerUtilsEx.getContainingMethod(DebuggerManagerEx.getInstanceEx(project).getContext().getSourcePosition());
            PsiMethod newMethod;

            while ((newMethod = PsiTreeUtil.getParentOfType(psiMethod, PsiMethod.class)) != null) {
                psiMethod = newMethod;
            }

            String methodReturnType;
            if (psiMethod.getReturnType() == null) {
                methodReturnType = "Constructor";
            } else {
                methodReturnType = psiMethod.getReturnType().getCanonicalText();
            }
            PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
            String methodSignature = encodeSignature(parameters, methodReturnType);
            invokedSwarmMethod.setSignature(methodSignature);
        });
        invokedSwarmMethod.setName(invoked.name());
        invokedSwarmMethod.setType(invokedType);
        invokedSwarmMethod.create();


        Invocation invocation = new Invocation();
        invocation.setInvoking(DebugActionListener.invokingMethod);
        invocation.setInvoked(invokedSwarmMethod);
        invocation.setSession(ProductToolWindow.getCurrentSession());
        invocation.create();
    }

    private String encodeSignature(PsiParameter[] parameters, String returnType) {
        String result = "(";
        for (int i = 0; i < parameters.length; i++) {
            result += parameters[i].getType().getCanonicalText();
            if (i != parameters.length - 1) {
                result += ",";
            }
        }
        result += ")" + returnType;
        return result;
    }
}
