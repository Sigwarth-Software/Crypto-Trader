package org.cryptotrader.version.script;

import lombok.extern.slf4j.Slf4j;
import org.cryptotrader.version.library.model.element.PomElement;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class PomParser {
    private static List<String> skipDirs = List.of("target", "node_modules", ".angular", "logs", ".git");
    private static Path rootPath = Path.of("..");
    public static List<PomElement> getAllPoms() {
        final List<PomElement> modules;
        final List<Path> pomPaths = getPomPaths();
        modules = pomPaths.stream().map(PomParser::getPom).collect(Collectors.toList());
        return modules;
    }

    public static PomElement getPom(final Path pomPath) {
        try {
            final Document doc = new SAXBuilder().build(pomPath.toFile());
            final Element project = doc.getRootElement();
            final PomElement pom = new PomElement(project, pomPath);
            return pom;
        } catch (final IOException | JDOMException exception) {
            throw new RuntimeException(exception);
        }

    }

    public static String getVersionString() {
        final List<PomElement> pomElements = getAllPoms();
        final StringBuilder versionString = new StringBuilder();
        pomElements.forEach(pomElement -> {
            final String name = pomElement.getPom().getModuleDependency().getName();
            final String version = pomElement.getVersion();
            versionString.append("%s: %s\n".formatted(name, version));
        });
        return versionString.toString();
    }

    public static String textFromNamespace(final Element element,
                                           final Namespace namespace,
                                           final String name) {
        return element.getChild(name, namespace).getText();
    }

    public static String getModulePath(final Path path) {
        String modulePath = path.toString();
        modulePath = modulePath.replace("..", "Crypto-Trader");
        modulePath = modulePath.replace("\\pom.xml", "");
        return modulePath;
    }

    public static List<Path> getPomPaths() {
        final List<Path> paths = new ArrayList<>();
        try {
            Files.walkFileTree(rootPath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
                    final Path name = dir.getFileName();
                    if (name != null && skipDirs.contains(name.toString())) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
                    if (attrs.isRegularFile() && "pom.xml".equals(file.getFileName().toString())) {
                        paths.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (final IOException exception) {
            throw new IllegalStateException("Error in searching for pom.xml files.", exception);
        }
        return paths;
    }
}
