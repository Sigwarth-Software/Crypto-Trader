import * as fs from 'fs';
import * as path from 'path';
import { Project, SyntaxKind } from 'ts-morph';

/**
 * Script to generate an Angular component and automatically register it.
 * Usage: npm run g:c -- <component-path>
 * Example: npm run g:c -- pages/new-feature
 */

interface AngularConfig {
    projects: {
        [key: string]: {
            architect: {
                build: {
                    options: {
                        styles: string[];
                    };
                };
            };
        };
    };
}

const rawComponentPathArg = process.argv[2];

if (!rawComponentPathArg) {
    console.error('Error: Please provide a component path (e.g., pages/my-component)');
    process.exit(1);
}

// Normalize path separators, remove leading ./ or .\, and strip angular/components/ prefix
const componentPathArg = rawComponentPathArg
    .replace(/\\/g, '/')
    .replace(/^\.\//, '')
    .replace(/^angular\/components\//, '');

const rootDir = process.cwd();
const angularJsonPath = path.join(rootDir, 'angular.json');

if (!fs.existsSync(angularJsonPath)) {
    console.error('Error: angular.json not found in the current directory.');
    process.exit(1);
}

// 1. Prepare paths and names
const componentParts = componentPathArg.split('/');
const category = componentParts[0]; // e.g., "elements" or "pages"
const componentName = componentParts.pop()!;
const fullComponentPath = `angular/components/${componentPathArg}`;

// Generate Class Name (e.g., "my-feature" -> "MyFeatureComponent")
const className = componentName
    .split('-')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join('') + 'Component';

console.log(`Generating component: ${fullComponentPath}...`);

// 2. Generate component files directly
const targetDir = path.join(rootDir, fullComponentPath);
const htmlFileName = `${componentName}.component.html`;
const scssFileName = `${componentName}.component.scss`;
const tsFileName = `${componentName}.component.ts`;

if (fs.existsSync(targetDir) && fs.readdirSync(targetDir).length > 0) {
    console.log('Nothing to be done.');
} else {
    if (!fs.existsSync(targetDir)) {
        fs.mkdirSync(targetDir, { recursive: true });
    }

    // HTML content
    const htmlContent = `<!-- ${htmlFileName} -->
<ng-container>


</ng-container>`;

    // TypeScript content
    const tsContent = `// ${tsFileName}
import { Component } from '@angular/core';

@Component({
    selector: '${componentName}',
    templateUrl: './${htmlFileName}',
    styleUrls: ['./${scssFileName}'],
    standalone: false
})
export class ${className} {
    constructor() {

    }
}
`;

    // SCSS content: compute relative path to angular/styles/globals
    // fullComponentPath segments after 'angular/' determine the depth
    const pathSegments = fullComponentPath.split('/');
    const levelsUp = pathSegments.length - 1; // subtract 'angular' prefix
    const relativeLevels = '../'.repeat(levelsUp);
    const scssContent = `// ${scssFileName}
@use '${relativeLevels}styles/globals' as *;

${componentName} {

}
`;

    fs.writeFileSync(path.join(targetDir, htmlFileName), htmlContent, 'utf8');
    fs.writeFileSync(path.join(targetDir, tsFileName), tsContent, 'utf8');
    fs.writeFileSync(path.join(targetDir, scssFileName), scssContent, 'utf8');

    console.log(`CREATE ${fullComponentPath}/${tsFileName} (${Buffer.byteLength(tsContent)} bytes)`);
    console.log(`CREATE ${fullComponentPath}/${scssFileName} (${Buffer.byteLength(scssContent)} bytes)`);
    console.log(`CREATE ${fullComponentPath}/${htmlFileName} (${Buffer.byteLength(htmlContent)} bytes)`);
}

// 3. Update angular.json styles array
const scssPath = `${fullComponentPath}/${componentName}.component.scss`.replace(/\\/g, '/').replace(/\/\.\//, '/');

try {
    const angularConfig: AngularConfig = JSON.parse(fs.readFileSync(angularJsonPath, 'utf8'));
    const project = angularConfig.projects['crypto-trader'];

    if (project) {
        const styles = project.architect.build.options.styles;
        if (!styles.includes(scssPath)) {
            styles.push(scssPath);

            // Sort styles to keep it organized
            const globals = styles.filter(s => s.startsWith('node_modules') || s.includes('_globals') || s.includes('angular/styles'));
            const components = styles.filter(s => !globals.includes(s));
            components.sort();

            project.architect.build.options.styles = [...components, ...globals];
            fs.writeFileSync(angularJsonPath, JSON.stringify(angularConfig, null, 4));
            console.log(`Successfully added ${scssPath} to angular.json styles.`);
        }
    }
} catch (error) {
    console.error('Error: Failed to update angular.json.', error);
}

// 4. Update Registration Files (elements.ts or pages.ts) using ts-morph
if (category === 'elements' || category === 'pages') {
    try {
        const regFileName = `${category}.ts`;
        const regFilePath = path.join(rootDir, 'angular', 'components', category, regFileName);

        if (fs.existsSync(regFilePath)) {
            const project = new Project();
            const sourceFile = project.addSourceFileAtPath(regFilePath);

            // Calculate relative import path: components/elements/elements.ts -> components/elements/...
            const relativeImportPath = `./${componentParts.slice(1).join('/')}/${componentName}/${componentName}.component`.replace(/\/+/g, '/');

            // Add the import if not present
            if (!sourceFile.getImportDeclaration(i => i.getModuleSpecifierValue() === relativeImportPath)) {
                sourceFile.addImportDeclaration({
                    namedImports: [className],
                    moduleSpecifier: relativeImportPath
                });
            }

            // Find the array and add the element
            const arrayVar = sourceFile.getVariableDeclaration(category);
            if (arrayVar) {
                const initializer = arrayVar.getInitializerIfKind(SyntaxKind.ArrayLiteralExpression);
                if (initializer) {
                    const elements = initializer.getElements().map(e => e.getText());
                    if (!elements.includes(className)) {
                        initializer.addElement(className);

                        // Sort the elements in the array
                        const sortedElements = initializer.getElements()
                            .map(e => e.getText())
                            .sort((a, b) => a.localeCompare(b));

                        // Re-create the array content to maintain sorting and formatting
                        while (initializer.getElements().length > 0) {
                            initializer.removeElement(0);
                        }
                        initializer.addElements(sortedElements);
                    }
                }
            }

            sourceFile.formatText({
                indentSize: 4
            });
            sourceFile.saveSync();
            console.log(`Successfully registered ${className} in ${regFileName} using ts-morph.`);
        }
    } catch (error) {
        console.error(`Error: Failed to update registration file for ${category}.`, error);
    }
}
