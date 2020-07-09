package com.swarm.listeners;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointListener;
import com.swarm.utils.States;
import com.swarm.models.Breakpoint;
import com.swarm.models.Event;
import com.swarm.models.Method;
import com.swarm.models.Type;
import org.jetbrains.annotations.NotNull;


public class BreakpointListener implements XBreakpointListener<XBreakpoint<?>>, DumbAware {

    private final Project project;

    public BreakpointListener(Project project) {
        this.project = project;
    }

    @Override
    public void breakpointAdded(@NotNull XBreakpoint xBreakpoint) {
        if(States.currentSession.getId() == 0) {
            return;
        }
        if(xBreakpoint.getSourcePosition() == null) {
            return;
        }
        int typeId = handleBreakpointEvent("Breakpoint Added", xBreakpoint);
        Breakpoint breakpoint = new Breakpoint();
        Type type = new Type();
        type.setId(typeId);
        breakpoint.setType(type);
        breakpoint.setLineNumber(xBreakpoint.getSourcePosition().getLine());
        breakpoint.create();
    }

    @Override
    public void breakpointRemoved(@NotNull XBreakpoint<?> breakpoint) {
        if(States.currentSession.getId() == 0) {
            return;
        }
        if(breakpoint.getSourcePosition() == null) {
            return;
        }
        handleBreakpointEvent("Breakpoint Removed", breakpoint);
    }

    private int handleBreakpointEvent(String eventKind, @NotNull XBreakpoint breakpoint) {
        Type type = new Type();
        Method method = new Method();

        ReadAction.run(() -> {
            PsiJavaFile file = (PsiJavaFile) PsiManager.getInstance(project).findFile(breakpoint.getSourcePosition().getFile());
            type.setName(file.getName());
            type.setFullPath(file.getVirtualFile().getPath());
            type.setSourceCode(file.getText());

            type.setFullName(file.getPackageName());
            if(!type.getFullName().equals("")) {
                type.setFullName(type.getFullName() + "." + file.getName());
            }
            type.setSession(States.currentSession);
            type.create();
            PsiMethod psiMethod = findMethodByBreakpointAndFile(breakpoint, file);
            if(psiMethod != null){
                method.setName(psiMethod.getName());
                method.setType(type);
                String methodReturnType;
                if(psiMethod.getReturnType() == null){
                    methodReturnType = "Constructor";
                } else {
                    methodReturnType = psiMethod.getReturnType().getCanonicalText();
                }
                PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
                String methodSignature = encodeSignature(parameters, methodReturnType);
                method.setSignature(methodSignature);

            }
        });

        method.create();

        int lineNumber = breakpoint.getSourcePosition().getLine();

        Event event = new Event();
        event.setKind(eventKind);
        event.setLineNumber(lineNumber);
        event.setSession(States.currentSession);
        event.setMethod(method);
        event.create();

        return type.getId();
    }

    private PsiMethod findMethodByBreakpointAndFile(XBreakpoint breakpoint, PsiJavaFile file) {
        int offset = breakpoint.getSourcePosition().getOffset();
        var element = file.findElementAt(offset);

        if(element == null) {
            return null;
        }

        PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        if(method == null && element.getNextSibling() instanceof PsiMethod) {
            method = PsiTreeUtil.getNextSiblingOfType(element, PsiMethod.class);
        }

        return method;
    }

    private String encodeSignature(PsiParameter[] parameters, String returnType) {
        String result = "(";
        for (int i = 0; i < parameters.length; i++) {
            result += parameters[i].getType().getCanonicalText();
            if(i != parameters.length-1){
                result += ",";
            }
        }
        result += ")" + returnType;
        return result;
    }
}
