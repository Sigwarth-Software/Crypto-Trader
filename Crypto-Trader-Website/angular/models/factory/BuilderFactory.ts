/**
 * A factory interface for building objects.
 */
export interface BuilderFactory<BuildOutput> {
    build(): BuildOutput
}