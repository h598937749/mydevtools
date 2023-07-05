package com.mysoft.devtools.listeners;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;

/**
 * @author hezd 2023/4/24
 */
public class MyProjectOpenCloseListener implements ProjectManagerListener {
//    /**
//     * 打开项目事件（已过时，后期版本可能会删除）
//     *
//     * @param project
//     */
//    @Override
//    public void projectOpened(@NotNull Project project) {
//        StartupManager.getInstance(project).runWhenProjectIsInitialized(() -> {
//            OverrideInspection.doOverride(project);
//
//            //ActionManager.getInstance().registerAction("UnitTestAction", new UnitTestAction());
//
////            IntentionManager.getInstance().addAction(new UnitTestIntention());
//        });
//    }

    /**
     * 关闭项目事件
     *
     * @param project closing project
     */
    @Override
    public void projectClosed(@NotNull Project project) {
        System.out.println("project closed!");
    }
}
