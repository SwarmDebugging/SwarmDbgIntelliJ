package com.swarm.toolWindow;

import com.intellij.application.Topics;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.TableSpeedSearch;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.OpenSourceUtil;
import com.intellij.util.ui.JBUI;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.swarm.models.*;
import com.swarm.mouseListeners.RecommendationMouseListener;
import com.swarm.services.RecommendationService;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class RecommendationToolWindow extends SimpleToolWindowPanel implements DumbAware, TreeSelectionProvider.Handler, Disposable {
    private final Project project;
    private JBTable contentTable;
    private final RecommendationService recommendationService = new RecommendationService();

    public RecommendationToolWindow(Project project) {
        super(false, true);
        this.project = project;

        Topics.subscribe(TreeSelectionProvider.Handler.TREE_SELECTION_TOPIC, this, this);

        TreeSelectionProvider.setTreeNode(TreeSelectionProvider.getTreeNode());

        createToolBar();
    }

    private void createToolBar() {
        final DefaultActionGroup group = new DefaultActionGroup();
        group.add(new JumpToSourceAction());
        group.add(new SetBreakpointAction());
        final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("swarm", group, false);
        setToolbar(JBUI.Panels.simplePanel(actionToolbar.getComponent()));
    }

    @Override
    public JComponent getContent() {
        return this.getComponent();
    }

    private void addLineBreakpoint(final Project project, final String fileUrl, final int line) {
        class MyBreakpointProperties extends XBreakpointProperties<MyBreakpointProperties> {
            public String myOption;

            public MyBreakpointProperties() {
            }

            @Override
            public MyBreakpointProperties getState() {
                return this;
            }

            @Override
            public void loadState(final MyBreakpointProperties state) {
                myOption = state.myOption;
            }
        }

        class MyLineBreakpointType extends XLineBreakpointType<MyBreakpointProperties> {
            public MyLineBreakpointType() {
                super("testId", "testTitle");
            }

            @Override
            public MyBreakpointProperties createBreakpointProperties(@NotNull VirtualFile file, final int line) {
                return null;
            }

            @Override
            public MyBreakpointProperties createProperties() {
                return new MyBreakpointProperties();
            }
        }

        final XBreakpointManager breakpointManager = XDebuggerManager.getInstance(project).getBreakpointManager();
        final MyLineBreakpointType MY_LINE_BREAKPOINT_TYPE = new MyLineBreakpointType();
        final MyBreakpointProperties MY_LINE_BREAKPOINT_PROPERTIES = new MyBreakpointProperties();

        Runnable runnable = () -> breakpointManager.addLineBreakpoint(
                MY_LINE_BREAKPOINT_TYPE,
                fileUrl,
                line,
                MY_LINE_BREAKPOINT_PROPERTIES
        );
        WriteCommandAction.runWriteCommandAction(project, runnable);

        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(new File(fileUrl));
        XDebuggerUtil.getInstance().toggleLineBreakpoint(project, virtualFile, line);
    }

    @Override
    public void dispose() {

    }

    public final class JumpToSourceAction extends DumbAwareAction {
        JumpToSourceAction() {
            super("Jump to the Method's Source Code", "Jump to the selected method's source code", AllIcons.FileTypes.Any_type);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            jumpToSource();
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);
            if (contentTable != null) {
                e.getPresentation().setEnabled(contentTable.getSelectedRow() != -1);
            } else {
                e.getPresentation().setEnabled(false);
            }
        }
    }

    public void jumpToSource() {
        if (TreeSelectionProvider.getTreeNode() instanceof Task) {
            Type type = (Type) contentTable.getValueAt(contentTable.getSelectedRow(), 0);
            PsiFile psiFile = getPsiFileFromType(type);
            OpenSourceUtil.navigate(psiFile);
        } else if (TreeSelectionProvider.getTreeNode() instanceof Session) {
            try {
                Breakpoint breakpoint = (Breakpoint) contentTable.getValueAt(contentTable.getSelectedRow(), contentTable.getSelectedColumn());

                String filePath = breakpoint.getType().getFullPath().substring(project.getName().length());
                filePath = project.getBasePath() + filePath;

                VirtualFile file = VirtualFileManager.getInstance().findFileByNioPath(Path.of(filePath));
                if (file == null) {
                    return;
                }
                OpenFileDescriptor descriptor = new OpenFileDescriptor(project, file, breakpoint.getLineNumber(), 0);
                OpenSourceUtil.navigate(descriptor);
            } catch (ClassCastException classCastException) {
                Messages.showInfoMessage("Cannot Jump to source", "No Breakpoint");
            }
        }
    }

    private PsiFile getPsiFileFromType(Type type) {
        final PsiFile[] psiFile = new PsiFile[1];
        ApplicationManager.getApplication().runReadAction(() -> {
            String filePath = type.getFullPath().substring(project.getName().length());
            filePath = project.getBasePath() + filePath;

            VirtualFile file = VirtualFileManager.getInstance().findFileByNioPath(Path.of(filePath));
            if (file == null) {
                return;
            }
            psiFile[0] = PsiManager.getInstance(project).findFile(file);
        });
        return psiFile[0];
    }

    public final class SetBreakpointAction extends DumbAwareAction {
        SetBreakpointAction() {
            super("Set a Breakpoint in the Method", "Set a line breakpoint in the selected method's first line", AllIcons.Debugger.Db_set_breakpoint);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            setBreakpoint(); //TODO: test this
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);
            if (contentTable != null) {
                e.getPresentation().setEnabled(contentTable.getSelectedRow() != -1);
            } else {
                e.getPresentation().setEnabled(false);
            }
        }
    }

    //TODO: don't know where to set breakpoint when a task is selected
    public void setBreakpoint() {
        if (TreeSelectionProvider.getTreeNode() instanceof Task) {
            /*Method method = (Method) contentTable.getValueAt(contentTable.getSelectedRow(), 0);
            PsiMethod psiMethod = getPsiMethod(method);
            var document = PsiDocumentManager.getInstance(project).getDocument(psiMethod.getContainingFile());
            int methodFirstLine = document.getLineNumber(psiMethod.getTextOffset());
            String line = "";
            do {
                methodFirstLine++;
                TextRange range = new TextRange(document.getLineStartOffset(methodFirstLine), document.getLineEndOffset(methodFirstLine));
                line = document.getText(range);
            } while (line.matches("\\A\\s*\\z"));
            int finalMethodFirstLine = methodFirstLine;
            WriteCommandAction.runWriteCommandAction(project, () -> addLineBreakpoint(project, method.getType().getFullPath(), finalMethodFirstLine));*/
        } else if (TreeSelectionProvider.getTreeNode() instanceof Session) {
            try {
                Breakpoint breakpoint = (Breakpoint) contentTable.getValueAt(contentTable.getSelectedRow(), contentTable.getSelectedColumn());
                addLineBreakpoint(project, breakpoint.getType().getFullPath(), breakpoint.getLineNumber());
            } catch (ClassCastException classCastException) {
                Messages.showInfoMessage("Cannot set a breakpoint", "No Breakpoint");
            }
        }
    }

    @Override
    public void treeSelectionAction(Object treeNode) {
        if (treeNode == null) {
            setContent(new JLabel("Select a task to get breakpoint recommendations"));
            contentTable = null;
        } else if (treeNode instanceof Task) {
            Task task = (Task) treeNode;
            ArrayList<Method> recommendedMethods = recommendationService.getRecommendedMethods(task.getId());

            LinkedHashMap<Type, Integer> typeHashMap = new LinkedHashMap<>();
            for (Method method : recommendedMethods) {
                Integer j = typeHashMap.get(method.getType());
                typeHashMap.put(method.getType(), (j == null) ? 1 : j + 1);
            }

            Set<Type> typesAlreadySeen = new HashSet<>();
            recommendedMethods.removeIf(method -> !typesAlreadySeen.add(method.getType()));

            recommendedMethods.sort((method, method2) -> typeHashMap.get(method2.getType()).compareTo(typeHashMap.get(method.getType())));

            TableModel dataModel = new AbstractTableModel() {
                @Override
                public int getRowCount() {
                    return recommendedMethods.size();
                }

                @Override
                public int getColumnCount() {
                    return 2;
                }

                @Override
                public String getColumnName(int column) {
                    if (column == 0) {
                        return "Type";
                    } else {
                        return "Number of debugging events";
                    }
                }

                @Override
                public Object getValueAt(int row, int col) {
                    if (col == 0) {
                        return recommendedMethods.get(row).getType();
                    } else {
                        Method method = recommendedMethods.get(row);
                        return typeHashMap.get(method.getType());
                    }
                }
            };

            contentTable = new JBTable(dataModel);
            contentTable.setSelectionModel(new ForcedListSelectionModel());
            TableSpeedSearch speedSearch = new TableSpeedSearch(contentTable);
            speedSearch.setClearSearchOnNavigateNoMatch(true);
            contentTable.addMouseListener(new RecommendationMouseListener(project));
            JBScrollPane scrollPane = new JBScrollPane(contentTable);
            setContent(scrollPane);
        } else if (treeNode instanceof Session) {
            Session session = (Session) treeNode;
            ArrayList<Breakpoint> breakpoints = recommendationService.getBreakpointsBySessionId(session.getId());

            TableModel dataModel = new AbstractTableModel() {
                @Override
                public int getRowCount() {
                    return breakpoints.size();
                }

                @Override
                public int getColumnCount() {
                    return 1;
                }

                @Override
                public String getColumnName(int column) {
                    if (column == 0) {
                        return "Breakpoints set in selected session(ordered by timestamp)";
                    } else if (column == 1) {
                        return "Type";
                    } else {
                        return "Number of debugging events";
                    }
                }

                @Override
                public Object getValueAt(int row, int col) {
                    if (!breakpoints.isEmpty()) {
                        return breakpoints.get(row);
                    } else {
                        return "No breakpoint set in this session";
                    }
                }
            };

            contentTable = new JBTable(dataModel);
            contentTable.setSelectionModel(new ForcedListSelectionModel());
            TableSpeedSearch speedSearch = new TableSpeedSearch(contentTable);
            speedSearch.setClearSearchOnNavigateNoMatch(true);
            contentTable.addMouseListener(new RecommendationMouseListener(project));
            JBScrollPane scrollPane = new JBScrollPane(contentTable);
            setContent(scrollPane);
        }
    }

    private static class ForcedListSelectionModel extends DefaultListSelectionModel {
        private ForcedListSelectionModel() {
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }

        @Override
        public void clearSelection() {
        }

        @Override
        public void removeSelectionInterval(int index0, int index1) {
        }
    }
}