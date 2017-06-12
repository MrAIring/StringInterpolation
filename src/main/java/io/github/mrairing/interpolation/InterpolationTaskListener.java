package io.github.mrairing.interpolation;

import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.tree.JCTree;

class InterpolationTaskListener implements TaskListener {

    private final JavacTask task;

    InterpolationTaskListener(JavacTask task) {
        this.task = task;
    }

    @Override
    public void started(TaskEvent event) { }

    @Override
    public void finished(TaskEvent event) {
        if (event.getKind() == TaskEvent.Kind.ANALYZE) {
            JCTree.JCCompilationUnit compilationUnit = (JCTree.JCCompilationUnit) event.getCompilationUnit();

            compilationUnit.accept(new InterpolationTreeTranslator(task, event));
        }
    }
}
