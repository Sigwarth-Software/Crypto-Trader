import { type PixelCalculator } from './PixelCalculator'
import { type PixelVariables } from './types'
import { BuilderFactory } from '@models/factory/BuilderFactory'

/**
 * A builder class for creating pixel variables based on viewport width (vw),
 * viewport height (vh), and root em (rem) values.
 */
export class PixelCalculatorBuilder implements BuilderFactory<PixelVariables> {
    private _vw: number = 0
    private _vh: number = 0
    private _rem: number = 0

    constructor(private readonly pixelCalculator: PixelCalculator) {}

    /**
     * Sets the viewport width (vw) value.
     * @param vw - The viewport width value to set.
     * @returns The current instance for method chaining.
     */
    public vw(vw: number): PixelCalculatorBuilder {
        this._vw = vw;
        return this;
    }

    /**
     * Sets the viewport height (vh) value.
     * @param vh - The viewport height value to set.
     * @returns The current instance for method chaining.
     */
    public vh(vh: number): PixelCalculatorBuilder {
        this._vh = vh;
        return this;
    }

    /**
     * Sets the root em (rem) value.
     * @param rem - The root em value to set.
     * @returns The current instance for method chaining.
     */
    public rem(rem: number): PixelCalculatorBuilder {
        this._rem = rem
        return this
    }

    /**
     * Calculates the pixel value based on the set vw, vh, and rem values.
     * @returns The calculated pixel value.
     */
    public pixels(): number {
        const vwPixels: number = this.pixelCalculator.vwToPixels(this._vw)
        const vhPixels: number = this.pixelCalculator.vhToPixels(this._vh)
        const remPixels: number = this.pixelCalculator.remToPixels(this._rem)
        return this.pixelCalculator.getByViewportRem(
            vwPixels,
            vhPixels,
            remPixels,
        )
    }

    /**
     * Builds and returns the pixel variables object containing the set vw,
     * vh, and rem values.
     *
     * @returns An object containing the pixel variables.
     */
    public build(): PixelVariables {
        return {
            vw: this._vw,
            vh: this._vh,
            rem: this._rem,
        }
    }
}
