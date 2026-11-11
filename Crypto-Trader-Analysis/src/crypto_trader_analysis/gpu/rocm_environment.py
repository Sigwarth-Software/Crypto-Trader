import os
from importlib import metadata
from pathlib import Path


def configure_rocm_environment(gpu_id: int) -> None:
    try:
        metadata.version("tensorflow-rocm")
    except metadata.PackageNotFoundError:
        return

    os.environ.setdefault("HIP_VISIBLE_DEVICES", str(gpu_id))
    rocm_root = os.getenv("ROCM_PATH") or os.getenv("ROCM_ROOT")
    if not rocm_root:
        if Path("/opt/rocm").is_dir():
            rocm_root = "/opt/rocm"
        else:
            roots = [
                root for root in Path("/opt").glob("rocm-*")
                if (root / "llvm/bin/ld.lld").is_file()
            ]
            if len(roots) == 1:
                rocm_root = str(roots[0])
    if rocm_root:
        os.environ.setdefault("ROCM_PATH", rocm_root)
        os.environ.setdefault("ROCM_ROOT", rocm_root)
