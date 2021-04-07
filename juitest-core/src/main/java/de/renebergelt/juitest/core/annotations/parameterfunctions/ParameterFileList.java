package de.renebergelt.juitest.core.annotations.parameterfunctions;

import de.renebergelt.juitest.core.annotations.TestParameterMarker;
import de.renebergelt.juitest.core.annotations.parameterfunctions.containers.ParameterFileListContainer;
import de.renebergelt.juitest.core.annotations.parameterfunctions.containers.ParameterSetContainer;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(ParameterFileListContainer.class)
@TestParameterMarker(evaluationClass = ParameterFileListEvaluationFunc.class)
public @interface ParameterFileList {
    int index();
    String name() default "";

    String path() default "";
    String filemask() default "";
}

class ParameterFileListEvaluationFunc implements TestParameterEvaluationFunc<ParameterFileList> {

    @Override
    public List<Object> eval(ParameterFileList parameterAnnotation) {

        String path = parameterAnnotation.path();
        String filemask = parameterAnnotation.filemask();

        List<Object> filenames = new ArrayList<>();

        File[] files = new File(path).listFiles((FilenameFilter)new WildcardFileFilter(filemask));
        for(File f: files) {
            filenames.add(f.getPath());
        }

        return filenames;
    }
}
