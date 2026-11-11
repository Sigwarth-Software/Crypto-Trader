import logging
import os
import re
import sys
from typing import Optional, Dict

try:
    import colorlog
    HAS_COLORLOG = True
except ImportError:
    colorlog = None
    HAS_COLORLOG = False

try:
    from rich.console import Console
    from rich.logging import RichHandler
    from rich.theme import Theme
    from rich.traceback import install as install_rich_traceback
    HAS_RICH = True
except ImportError:
    Console = None
    RichHandler = None
    Theme = None
    install_rich_traceback = None
    HAS_RICH = False

DEFAULT_LOG_FORMAT = "%(light_black)s[%(asctime)s]%(reset)s %(log_color)s[%(levelname)s]%(reset)s %(message)s"
DEFAULT_DATE_FORMAT = "%Y-%m-%d %H:%M:%S"

DEFAULT_LOG_COLORS: Dict[str, str] = {
    "DEBUG": "cyan",
    "INFO": "green",
    "WARNING": "yellow",
    "ERROR": "red",
    "CRITICAL": "bold_red",
}

DEFAULT_SECONDARY_LOG_COLORS: Dict[str, Dict[str, str]] = {
    "message": {
        "ERROR": "red",
        "CRITICAL": "bold_red",
    }
}

# Muted 256-color tones; the basic ANSI colors render very bright in many consoles
DEFAULT_RICH_THEME: Dict[str, str] = {
    "log.time": "grey50",
    "logging.level.debug": "steel_blue",
    "logging.level.info": "dark_sea_green",
    "logging.level.warning": "light_goldenrod3",
    "logging.level.error": "bold indian_red",
    "logging.level.critical": "bold grey93 on red3",
    # Values highlighted automatically inside log messages
    "repr.number": "light_sky_blue3",
    "repr.str": "dark_sea_green",
    "repr.path": "plum3",
    "repr.filename": "plum3",
    "repr.bool_true": "italic dark_sea_green",
    "repr.bool_false": "italic indian_red",
    "repr.none": "italic plum3",
    "repr.url": "underline steel_blue1",
}

# Width used when output is not a terminal (IDE run console, docker logs) and COLUMNS is unset
NON_TERMINAL_WIDTH = 120

_console = None


def _is_tty(stream) -> bool:
    try:
        return bool(stream.isatty())
    except (AttributeError, ValueError, OSError):
        return False


def _ensure_encodable(stream) -> None:
    encoding = (getattr(stream, "encoding", None) or "").lower().replace("-", "")
    if not encoding or encoding.startswith("utf") or not hasattr(stream, "reconfigure"):
        return
    try:
        stream.reconfigure(errors="replace")
    except (AttributeError, ValueError, OSError):
        pass


def _color_options(stream) -> dict:
    standard_streams = (sys.stdout, sys.stderr, sys.__stdout__, sys.__stderr__)
    if not any(stream is standard for standard in standard_streams) or _is_tty(stream):
        return {}
    if any(name in os.environ for name in ("NO_COLOR", "FORCE_COLOR", "TTY_COMPATIBLE")):
        return {}
    options = {"color_system": "256", "legacy_windows": False}
    if "COLUMNS" not in os.environ:
        options["width"] = NON_TERMINAL_WIDTH
    return options


def get_console():
    global _console
    if not HAS_RICH:
        return None
    if _console is None:
        _console = Console(theme=Theme(DEFAULT_RICH_THEME), **_color_options(sys.stdout))
    return _console


def _create_rich_handler(stream, date_fmt: str) -> logging.Handler:
    global _console
    theme = Theme(DEFAULT_RICH_THEME)
    _ensure_encodable(stream)
    if stream is sys.stdout:
        _console = Console(theme=theme, **_color_options(stream))
        install_rich_traceback(console=_console, show_locals=False)
    else:
        _console = Console(file=stream, theme=theme, **_color_options(stream))
    handler = RichHandler(
        console=_console,
        rich_tracebacks=True,
        tracebacks_show_locals=False,
        markup=False,
        show_path=False,
        log_time_format=f"[{date_fmt}]",
        omit_repeated_times=False,
    )
    handler.setFormatter(logging.Formatter("%(message)s"))
    return handler


def setup_logging(
    level: int = logging.DEBUG,
    format_str: Optional[str] = None,
    date_fmt: str = DEFAULT_DATE_FORMAT,
    stream=sys.stdout,
    log_colors: Optional[Dict[str, str]] = None,
    secondary_log_colors: Optional[Dict[str, Dict[str, str]]] = None,
    use_rich: bool = True,
) -> logging.Logger:
    root_logger = logging.getLogger()
    root_logger.setLevel(level)

    # Remove existing handlers to avoid duplicate log entries
    for handler in list(root_logger.handlers):
        root_logger.removeHandler(handler)

    fmt = format_str or DEFAULT_LOG_FORMAT
    colors = log_colors or DEFAULT_LOG_COLORS
    sec_colors = secondary_log_colors or DEFAULT_SECONDARY_LOG_COLORS

    if use_rich and HAS_RICH:
        handler = _create_rich_handler(stream, date_fmt)
        handler.setLevel(level)
        root_logger.addHandler(handler)
        root_logger.debug("Logging setup complete: Debug mode is enabled.")
        return root_logger

    if HAS_COLORLOG:
        formatter = colorlog.ColoredFormatter(
            fmt=fmt,
            datefmt=date_fmt,
            reset=True,
            log_colors=colors,
            secondary_log_colors=sec_colors,
            style="%",
        )
    else:
        # Graceful fallback to standard logging formatter if colorlog is not installed
        clean_fmt = re.sub(
            r"%\((?:log_color|reset|[a-zA-Z0-9_]+_log_color|light_black|bold_black|black|white|thin_white|red|green|yellow|blue|purple|cyan)\)s",
            "",
            fmt,
        )
        formatter = logging.Formatter(fmt=clean_fmt, datefmt=date_fmt)

    handler = logging.StreamHandler(stream)
    handler.setFormatter(formatter)
    handler.setLevel(level)
    root_logger.addHandler(handler)

    root_logger.debug("Logging setup complete: Debug mode is enabled.")
    return root_logger


def get_logger(name: Optional[str] = None, level: int = logging.DEBUG) -> logging.Logger:
    root_logger = logging.getLogger()
    if not root_logger.handlers:
        setup_logging(level=level)

    logger = logging.getLogger(name)
    logger.setLevel(level)
    return logger
