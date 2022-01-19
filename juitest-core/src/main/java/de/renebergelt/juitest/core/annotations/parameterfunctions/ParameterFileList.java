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
/**
 * A test parameter which captures filenames based on a path and a file mask
 */
public @interface ParameterFileList {
    /**
     * Index of the parameter
     * @return index
     */
    int index();

    /**
     * The name of the parameter
     * @return name
     */
    String name() default "";

    /**
     * The path to search for files (relative to the execution directory)
     * @return path
     */
    String path() default "";

    /**
     * A filename mask. Wildcards (* and ?) are supported
     * @return filemask
     */
    String filemask() default "";
}

class ParameterFileListEvaluationFunc implements TestParameterEvaluationFunc<ParameterFileList> {

    @Override
    public List<Object> eval(ParameterFileList parameterAnnotation) {

        String path = parameterAnnotation.path();
        String filemask = parameterAnnotation.filemask();

        List<Object> filenames = new ArrayList<>();

        File[] files = new File(path).listFiles((FilenameFilter)new WildcardFileFilter(filemask));
        if (files != null) {
            for (File f : files) {
                filenames.add(f.getPath());
            }
        }

        return filenames;
    }
}
