package io.github.mrairing.interpolation;

import com.google.auto.service.AutoService;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;

@AutoService(Plugin.class)
public class InterpolationPlugin implements Plugin {
    @Override
    public String getName() {
        return "StringInterpolation";
    }

    @Override
    public void init(JavacTask task, String... args) {
        task.addTaskListener(new InterpolationTaskListener(task));
    }
}

