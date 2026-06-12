package ui11.asset;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.plugins.ide.idea.model.IdeaModel;

public class GenerateAssetReferencesPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        GenerateAssetReferencesExtension extension =
                project.getExtensions().create("generateAssetReferences", GenerateAssetReferencesExtension.class);

        project.afterEvaluate(p -> {
            SourceSetContainer sourceSets =
                    project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets();

            /*
            SourceSet sourceSet = extension.getSourceSet().get();
            if (sourceSet.getName().contains("..") || sourceSet.getName().contains("/") ||
                    sourceSet.getName().contains("\\"))
                throw new RuntimeException("source set name contains suspicious characters: " + sourceSet.getName());
             */

            SourceSet sourceSet = sourceSets.getByName("main");

            Provider<Directory> outputDir = project.getLayout().getBuildDirectory().
                    dir("generated/sources/asset-references/java/" + sourceSet.getName());

            TaskProvider<GenerateAssetReferencesTask> task =
                    project.getTasks().register("generateAssetReferencesForMain",
                            GenerateAssetReferencesTask.class,
                            generateAssetReferencesTask -> {
                                generateAssetReferencesTask.setInput(sourceSet.getResources().getSourceDirectories());
                                generateAssetReferencesTask.getOutput().set(outputDir);
                                generateAssetReferencesTask.setClassName(extension.getClassName().get());
                            });

            sourceSet.getJava().srcDir(outputDir);
            project.getTasks().getByName(sourceSet.getCompileJavaTaskName()).dependsOn(task);

            IdeaModel m = project.getExtensions().findByType(IdeaModel.class);
            if (m != null) {
                m.getModule().getGeneratedSourceDirs().add(outputDir.get().getAsFile());
            }
        });
    }

    public static abstract class GenerateAssetReferencesExtension {

        public abstract Property<String> getClassName();
    }
}
