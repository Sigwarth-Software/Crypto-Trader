# run_gpu_one.py
# AMD RX 6900 XT
from src.crypto_trader_analysis.gpu.rocm_environment import configure_rocm_environment

# Configure ROCm before the training imports initialize TensorFlow.
configure_rocm_environment(1)

from src.crypto_trader_analysis.gpu.gpu_yml_reader import train_from_config


def main():
    train_from_config(1)


if __name__ == "__main__":
    main()
