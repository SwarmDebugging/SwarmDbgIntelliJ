package com.swarm.listeners;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointListener;
import com.swarm.models.*;
import com.swarm.toolWindow.ProductToolWindow;
import org.jetbrains.annotations.NotNull;


public class BreakpointListener implements XBreakpointListener<XBreakpoint<?>>, DumbAware {

    private final Project project;
    private ProductToolWindow productToolWindow;

    public BreakpointListener(Project project) {
        this.project = project;
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Swarm Debugging Manager");
        productToolWindow = (ProductToolWindow) toolWindow.getContentManager().getContent(0).getComponent();
    }

    @Override
    public void breakpointAdded(@NotNull XBreakpoint xBreakpoint) {
        if(productToolWindow.getCurrentSession().getId() == 0) {
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
        if(productToolWindow.getCurrentSession().getId() == 0) {
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
            PsiFile file = PsiManager.getInstance(project).findFile(breakpoint.getSourcePosition().getFile());
            String projectBasePath = project.getBasePath();
            String fileAbsolutePath = file.getVirtualFile().getPath();
            String filePath = fileAbsolutePath.substring(projectBasePath.length());
            filePath = project.getName().toLowerCase() + filePath;

            type.setFullPath(filePath);
            type.setSourceCode(file.getText());
            type.setSession(productToolWindow.getCurrentSession());

            PsiMethod psiMethod = findMethodByBreakpointAndFile(breakpoint, file);
            PsiClass psiClass = PsiTreeUtil.getParentOfType(psiMethod, PsiClass.class);

            PsiClass newClass;
            while((newClass = PsiTreeUtil.getParentOfType(psiClass, PsiClass.class)) != null) {
                psiClass = newClass;
            }

            type.setName(psiClass.getName());
            type.setFullName(psiClass.getQualifiedName());

            type.create();

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
        event.setSession(productToolWindow.getCurrentSession());
        event.setMethod(method);
        event.create();

        return type.getId();
    }

    private PsiMethod findMethodByBreakpointAndFile(XBreakpoint breakpoint, PsiFile file) {
        int offset = breakpoint.getSourcePosition().getOffset();
        var element = file.findElementAt(offset);

        if(element == null) {
            return null;
        }

        PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        if(method == null && element.getNextSibling() instanceof PsiMethod) {
            method = PsiTreeUtil.getNextSiblingOfType(element, PsiMethod.class);
        }

        PsiMethod newMethod;
        while((newMethod = PsiTreeUtil.getParentOfType(method, PsiMethod.class)) != null) {
            method = newMethod;
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
