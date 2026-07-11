import eslint from '@eslint/js';
import angular from 'angular-eslint';
import prettierConfig from 'eslint-config-prettier';
import jsdoc from 'eslint-plugin-jsdoc';
import prettierPlugin from 'eslint-plugin-prettier';
import globals from 'globals';
import tseslint from 'typescript-eslint';

export default tseslint.config(
    {
        linterOptions: {
            reportUnusedDisableDirectives: true,
        },
    },
    {
        ignores: [
            'dist/',
            'coverage/',
            '.angular/',
            'node_modules/',
            '**/*.min.*',
            'src/assets/',
            'angular/**/*.js',
        ],
    },
    {
        files: ['**/*.ts'],
        extends: [
            eslint.configs.recommended,
            ...tseslint.configs.recommendedTypeChecked,
            ...angular.configs.tsRecommended,
        ],
        processor: angular.processInlineTemplates,
        languageOptions: {
            globals: {
                ...globals.browser,
            },
            parserOptions: {
                projectService: true,
                tsconfigRootDir: import.meta.dirname,
            },
        },
        rules: {
            '@typescript-eslint/no-unused-vars': ['warn', { argsIgnorePattern: '^_' }],
            '@typescript-eslint/no-namespace': 'off',
            '@typescript-eslint/restrict-template-expressions': 'off',
            '@typescript-eslint/consistent-type-imports': [
                'warn',
                {
                    prefer: 'type-imports',
                    fixStyle: 'inline-type-imports',
                },
            ],
            '@typescript-eslint/no-explicit-any': 'warn',
            '@typescript-eslint/no-non-null-assertion': 'warn',
            '@typescript-eslint/explicit-function-return-type': [
                'error',
                {
                    allowExpressions: false,
                    allowTypedFunctionExpressions: false,
                    allowHigherOrderFunctions: false,
                    allowDirectConstAssertionInArrowFunctions: false,
                    allowConciseArrowFunctionExpressionsStartingWithVoid: false,
                },
            ],
            'no-console': 'off',
            'prefer-template': 'warn',
            'prefer-const': 'error',
            '@typescript-eslint/no-unsafe-assignment': 'warn',
            '@typescript-eslint/no-unsafe-call': 'warn',
            '@typescript-eslint/no-unsafe-member-access': 'warn',
            '@typescript-eslint/no-unsafe-return': 'warn',
            '@typescript-eslint/no-floating-promises': 'error',
            '@typescript-eslint/await-thenable': 'error',
            '@typescript-eslint/no-misused-promises': [
                'error',
                {
                    checksVoidReturn: {
                        attributes: false,
                    },
                },
            ],
            '@typescript-eslint/no-unnecessary-condition': 'warn',
            '@typescript-eslint/explicit-member-accessibility': [
                'warn',
                {
                    accessibility: 'explicit',
                    overrides: {
                        accessors: 'explicit',
                        constructors: 'no-public',
                        methods: 'explicit',
                        properties: 'explicit',
                        parameterProperties: 'explicit',
                    },
                },
            ],
            '@typescript-eslint/typedef': [
                'warn',
                {
                    arrayDestructuring: true,
                    arrowParameter: true,
                    memberVariableDeclaration: true,
                    objectDestructuring: true,
                    parameter: true,
                    propertyDeclaration: true,
                    variableDeclaration: true,
                    variableDeclarationIgnoreFunction: true,
                },
            ],
            '@typescript-eslint/no-inferrable-types': 'off',
            '@typescript-eslint/prefer-readonly': 'warn',
            'no-multiple-empty-lines': ['warn', { max: 1, maxEOF: 0, maxBOF: 0 }],
            'max-len': [
                'error',
                {
                    code: 100,
                    ignoreUrls: true,
                    ignoreStrings: true,
                    ignoreTemplateLiterals: true,
                    ignoreRegExpLiterals: true,
                },
            ],
            '@typescript-eslint/member-ordering': 'off',
            '@angular-eslint/prefer-inject': 'off',
            '@angular-eslint/prefer-standalone': 'off',
            'jsdoc/require-jsdoc': [
                'warn',
                {
                    publicOnly: true,
                    checkConstructors: false,
                    require: {
                        ArrowFunctionExpression: true,
                        ClassDeclaration: true,
                        ClassExpression: true,
                        FunctionDeclaration: true,
                        FunctionExpression: true,
                        MethodDefinition: true,
                    },
                    contexts: [
                        'TSInterfaceDeclaration',
                        'TSEnumDeclaration',
                        'TSTypeAliasDeclaration',
                    ],
                },
            ],
            'jsdoc/require-description': 'warn',
            'jsdoc/require-param': 'warn',
            'jsdoc/require-returns': 'warn',
            'id-length': [
                'warn',
                {
                    min: 3,
                    exceptions: ['i', 'j', 'x', 'y', 'z', '_', 'a', 'b', 'd3', 'id', 'db', 'vh', 'vw'],
                },
            ],
        },
    },
    {
        files: ['**/*.ts'],
        plugins: {
            jsdoc,
        },
    },
    {
        files: ['**/*.html'],
        extends: [...angular.configs.templateRecommended, ...angular.configs.templateAccessibility],
        rules: {
            '@angular-eslint/template/interactive-supports-focus': 'off',
            '@angular-eslint/template/click-events-have-key-events': 'off',
        },
    },
    {
        files: ['**/*.spec.ts', 'src/test.ts', 'src/**/*.test.ts'],
        rules: {
            '@typescript-eslint/explicit-function-return-type': 'off',
            '@typescript-eslint/no-explicit-any': 'off',
            '@typescript-eslint/no-unused-vars': 'off',
            'jsdoc/require-jsdoc': 'off',
        },
    },
    {
        files: [
            '**/*.cjs',
            'scripts/**/*.mjs',
            'eslint.config.js',
            'prettier.config.js',
            'jest.config.cjs',
        ],
        languageOptions: {
            globals: {
                ...globals.node,
            },
        },
        rules: {
            'no-undef': 'off',
            'jsdoc/require-jsdoc': 'off',
        },
    },
    {
        files: ['**/*.ts', '**/*.html'],
        plugins: {
            prettier: prettierPlugin,
        },
        rules: {
            'prettier/prettier': 'warn',
        },
    },
    {
        files: ['**/*.html'],
        rules: {
            'prettier/prettier': 'off',
        },
    },
    prettierConfig,
);
