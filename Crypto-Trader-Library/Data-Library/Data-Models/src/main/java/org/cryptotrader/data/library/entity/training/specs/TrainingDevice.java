package org.cryptotrader.data.library.entity.training.specs;

import org.jetbrains.annotations.NotNull;

public enum TrainingDevice {
    CPU("cpu"),
    GPU_ZERO("AMD_6700_XT"),
    GPU_ONE("AMD_6900_XT");
    public final String device;

    TrainingDevice(final String device) {
        this.device = device;
    }

    public static @NotNull TrainingDevice from(final @NotNull String device) {
        return switch (device) {
            case "cpu" -> TrainingDevice.CPU;
            case "AMD_6700_XT", "gpu_0" -> TrainingDevice.GPU_ZERO;
            case "AMD_6900_XT", "gpu_1" -> TrainingDevice.GPU_ONE;
            default -> throw new IllegalArgumentException("Unknown training device: " + device);
        };
    }
}