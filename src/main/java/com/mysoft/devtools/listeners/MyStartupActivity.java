package com.mysoft.devtools.listeners;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * IDEA 启动活动监听器（注：runActivity触发时插件可能还没有加载完成）
 *
 * @author hezd 2023/5/26
 */
public class MyStartupActivity implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {

    }
}
