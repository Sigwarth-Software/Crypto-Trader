package org.cryptotrader.version.library.model.element;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cryptotrader.version.library.model.config.ConfigFileType;
import org.cryptotrader.version.library.model.dependency.type.PomDependency;
import org.cryptotrader.version.library.model.module.ModuleLibrary;
import org.cryptotrader.version.library.model.module.type.Pom;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/** Represents an element of a pom.xml file */
@NoArgsConstructor
@Data
@Getter
public class PomElement {
    private static final Namespace MAVEN_NAMESPACE =
        Namespace.getNamespace("http://maven.apache.org/POM/4.0.0");
    private Optional<PomElement> parent;
    private Element baseElement;
    private Path path;

    public PomElement(final Element baseElement, final Path path) {
        this.baseElement = baseElement;
        this.path = path;
        this.parent = resolveParent();
    }

    public @NotNull String textFromNamespace(final String name) {
        Element child = this.baseElement.getChild(name, this.baseElement.getNamespace());

        if (child == null) {
            child = this.baseElement.getChild(name, MAVEN_NAMESPACE);
        }

        if (child == null) {
            child = this.baseElement.getChild(name);
        }

        if (child != null) {
            final String text = child.getText();

            if (text != null && !text.isBlank()) {
                return text;
            }
        }
        return this.getParent().map(parent -> parent.textFromNamespace(name)).orElse("");
    }

    public String directText(final String name) {
        Element child = this.baseElement.getChild(name, this.baseElement.getNamespace());

        if (child == null) {
            child = this.baseElement.getChild(name, MAVEN_NAMESPACE);
        }

        if (child == null) {
            child = this.baseElement.getChild(name);
        }
        return child == null ? "" : (child.getText() == null ? "" : child.getText());
    }

    public @Nullable PomDependency getParentDependency() {
        Element parentEl = this.baseElement.getChild(
            "parent",
            this.baseElement.getNamespace()
        );

        if (parentEl == null) {
            parentEl = this.baseElement.getChild("parent", MAVEN_NAMESPACE);
        }

        if (parentEl == null) {
            parentEl = this.baseElement.getChild("parent");
        }

        if (parentEl == null) {
            return null;
        }
        final String groupId = getChildText(parentEl, "groupId");
        final String artifactId = getChildText(parentEl, "artifactId");
        final String version = getChildText(parentEl, "version");
        final String name = "parent";
        return new PomDependency(name, version, groupId, artifactId);
    }

    public @Nullable Pom getParentPomModel() {
        final PomDependency coords = this.getParentDependency();

        if (coords == null) {
            return null;
        }
        final Optional<PomElement> possibleParent = this.getParent();

        if (possibleParent.isEmpty()) {
            return null;
        }
        final PomElement parentElement = possibleParent.get();
        final Path modulePath = Path.of(parentElement.getModulePath());
        final ConfigFileType fileType = ConfigFileType.POM;
        final ModuleLibrary module = ModuleLibrary.resolveFromPath(modulePath);
        return new Pom(module, modulePath, fileType, coords);
    }

    private String getChildText(final @NotNull Element element, final String name) {
        Element child = element.getChild(name, element.getNamespace());

        if (child == null) {
            child = element.getChild(name, MAVEN_NAMESPACE);
        }

        if (child == null) {
            child = element.getChild(name);
        }

        if (child == null) {
            return "";
        }
        return child.getText() == null ? "" : child.getText();
    }

    private @NotNull Optional<PomElement> resolveParent() {
        Element parentElement = this.baseElement.getChild("parent", this.baseElement.getNamespace());

        if (parentElement == null) {
            parentElement = this.baseElement.getChild("parent", MAVEN_NAMESPACE);
        }

        if (parentElement == null) {
            parentElement = this.baseElement.getChild("parent");
        }

        if (parentElement == null) {
            return Optional.empty();
        }
        Element relativePath = parentElement.getChild("relativePath", parentElement.getNamespace());

        if (relativePath == null) {
            relativePath = parentElement.getChild("relativePath", MAVEN_NAMESPACE);
        }

        if (relativePath == null) {
            relativePath = parentElement.getChild("relativePath");
        }
        String relativePathText = null;
        boolean relPresent = false;

        if (relativePath != null) {
            relPresent = true;
            final String text = relativePath.getText();

            if (text != null && !text.isBlank()) {
                relativePathText = text.trim();
            } else {
                return Optional.empty();
            }
        }

        if (!relPresent) {
            relativePathText = "..\\pom.xml";
        }
        final Path parentPomPath = this.path.getParent().resolve(relativePathText).normalize();

        try {
            final Document doc = new SAXBuilder().build(parentPomPath.toFile());
            final Element project = doc.getRootElement();
            return Optional.of(new PomElement(project, parentPomPath));
        } catch (@NotNull final IOException | JDOMException exception) {
            return Optional.empty();
        }
    }

    public String getVersion() {
        return this.textFromNamespace("version");
    }

    public String getPackaging() {
        return this.directText("packaging");
    }

    public @NotNull Pom getPom() {
        final ConfigFileType fileType = ConfigFileType.POM;
        final Path modulePath = Path.of(getModulePath());
        final String name = this.directText("name");
        final String artifactId = this.directText("artifactId");
        final String version = this.directText("version");
        final String groupId = this.directText("groupId");
        final ModuleLibrary module = ModuleLibrary.resolveFromPath(modulePath);
        final PomDependency moduleDependency = new PomDependency(name, version, groupId, artifactId);
        return new Pom(module, modulePath, fileType, moduleDependency);
    }

    public @NotNull String getModulePath() {
        String modulePath = this.path.toString();
        modulePath = modulePath.replace("..", System.getenv("REPO_NAME"));
        modulePath = modulePath.replace("\\pom.xml", "");
        return modulePath;
    }
}
