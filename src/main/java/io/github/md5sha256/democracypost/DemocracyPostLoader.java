package io.github.md5sha256.democracypost;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

import javax.annotation.Nonnull;

/**
 * Resolves the runtime-only libraries which used to be declared via the {@code libraries}
 * section of {@code plugin.yml}. Paper plugins resolve libraries through a loader instead.
 */
public final class DemocracyPostLoader implements PluginLoader {

    @Override
    public void classloader(@Nonnull PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(new RemoteRepository.Builder(
                "central",
                "default",
                MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR
        ).build());
        resolver.addDependency(new Dependency(
                new DefaultArtifact("org.mariadb.jdbc:mariadb-java-client:3.5.10"),
                null
        ));
        resolver.addDependency(new Dependency(
                new DefaultArtifact("com.zaxxer:HikariCP:7.1.0"),
                null
        ));
        classpathBuilder.addLibrary(resolver);
    }
}
