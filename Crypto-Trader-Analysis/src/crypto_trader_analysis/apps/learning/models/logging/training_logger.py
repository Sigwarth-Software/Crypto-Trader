import logging
import time
from typing import Optional

import tensorflow as tf
from rich.logging import RichHandler
from rich.panel import Panel
from rich.progress import BarColumn, MofNCompleteColumn, Progress, \
    SpinnerColumn, TextColumn, TimeElapsedColumn, TimeRemainingColumn
from rich.table import Table
from rich.text import Text

from src.crypto_trader_analysis.core.logger import get_console

logger = logging.getLogger(__name__)

# Fixed column widths so the per-epoch lines stay aligned like a table
LOSS_WIDTH = 10
DELTA_WIDTH = 10
LR_WIDTH = 10
DURATION_WIDTH = 6

# Muted 256-color palette, softer than the basic (often very bright) ANSI colors
HEADER_STYLE = "bold grey70"
MUTED_STYLE = "grey50"
EPOCH_STYLE = "grey82"
TRAIN_LOSS_STYLE = "dark_sea_green"
VAL_LOSS_STYLE = "light_sky_blue3"
IMPROVED_STYLE = "dark_olive_green3"
WORSENED_STYLE = "indian_red"
LR_STYLE = "khaki3"
LR_CHANGED_STYLE = "plum3"
BEST_STYLE = "gold3"
ACCENT_STYLE = "steel_blue1"
SUCCESS_STYLE = "dark_sea_green"
SUCCESS_BORDER_STYLE = "dark_sea_green4"
WARNING_STYLE = "light_goldenrod3"


def format_duration(seconds: float) -> str:
    minutes, secs = divmod(int(round(seconds)), 60)
    hours, minutes = divmod(minutes, 60)
    if hours:
        return f"{hours}h {minutes:02d}m {secs:02d}s"
    if minutes:
        return f"{minutes}m {secs:02d}s"
    return f"{seconds:.1f}s"


def _model_type_name(model_type) -> str:
    return getattr(model_type, "name", str(model_type))


def _cell(text: str, width: int, style: str, align: str = ">") -> str:
    return f"[{style}]{text:{align}{width}}[/{style}]"


def _log_markup(message: str, level: int = logging.INFO) -> None:
    root_handlers = logging.getLogger().handlers
    if any(isinstance(handler, RichHandler) for handler in root_handlers):
        logger.log(level, message, extra={"markup": True, "highlighter": None})
    else:
        logger.log(level, Text.from_markup(message).plain)


def print_training_header(target_currency: str,
                          model_type=None,
                          index: Optional[int] = None,
                          total: Optional[int] = None,
                          gpu_id: Optional[int] = None) -> None:
    parts = [f"[bold {ACCENT_STYLE}]{target_currency}[/]"]
    if model_type is not None:
        parts.append(f"[{LR_CHANGED_STYLE}]{_model_type_name(model_type)}[/]")
    if gpu_id is not None:
        parts.append(f"[{LR_STYLE}]GPU {gpu_id}[/]")
    if index is not None and total:
        parts.append(f"[{MUTED_STYLE}]{index}/{total}[/]")
    get_console().rule(" • ".join(parts), style="steel_blue")


def print_training_summary(target_currency: str,
                           model_type=None,
                           rows: Optional[int] = None,
                           model_path: Optional[str] = None,
                           predicted_price=None,
                           elapsed: Optional[float] = None) -> None:
    grid = Table.grid(padding=(0, 2))
    grid.add_column(style="bold")
    grid.add_column()
    if model_type is not None:
        grid.add_row("Model", _model_type_name(model_type))
    if rows is not None:
        grid.add_row("Rows", f"{rows:,}")
    if predicted_price is not None:
        grid.add_row("Predicted price", f"[bold {SUCCESS_STYLE}]{float(predicted_price):,.6f}[/]")
    if elapsed is not None:
        grid.add_row("Duration", format_duration(elapsed))
    if model_path:
        grid.add_row("Saved to", Text(model_path, style=MUTED_STYLE))
    get_console().print(Panel(grid,
                              title=f"[bold {SUCCESS_STYLE}]✔ {target_currency} trained[/]",
                              border_style=SUCCESS_BORDER_STYLE,
                              expand=False))


class RichTrainingLogger(tf.keras.callbacks.Callback):
    def __init__(self, title: str = "Training", show_progress: Optional[bool] = None):
        super().__init__()
        self.title = title
        self.show_progress = show_progress
        self.console = None
        self.progress: Optional[Progress] = None
        self.epoch_task = None
        self.batch_task = None
        self.history: list[dict] = []
        self.best_epoch: Optional[int] = None
        self.best_loss = float("inf")
        self.monitor = "loss"
        self.start_time = 0.0
        self.epoch_start_time = 0.0
        self.header_logged = False

    def on_train_begin(self, logs=None):
        self.close()
        self.console = get_console()
        self.history = []
        self.best_epoch = None
        self.best_loss = float("inf")
        self.monitor = "loss"
        self.start_time = time.monotonic()
        self.header_logged = False

        epochs = self.params.get("epochs") if self.params else None
        steps = self.params.get("steps") if self.params else None
        details = [f"{epochs} epochs" if epochs else None,
                   f"{steps} batches/epoch" if steps else None]
        details_text = ", ".join(detail for detail in details if detail)
        _log_markup(f"[bold {ACCENT_STYLE}]▶ {self.title}[/]"
                    + (f" [{MUTED_STYLE}]({details_text})[/]" if details_text else ""))

        if self._should_show_progress():
            self.progress = Progress(
                SpinnerColumn(),
                TextColumn("[bold]{task.description}"),
                BarColumn(bar_width=None),
                MofNCompleteColumn(),
                TextColumn("•"),
                TimeElapsedColumn(),
                TextColumn("•"),
                TimeRemainingColumn(),
                console=self.console,
                transient=True,
                expand=True,
            )
            self.epoch_task = self.progress.add_task("Epochs", total=epochs)
            self.batch_task = self.progress.add_task("Batches", total=steps)
            self.progress.start()

    def on_epoch_begin(self, epoch, logs=None):
        self.epoch_start_time = time.monotonic()
        if self.progress is not None:
            self.progress.reset(self.batch_task,
                                total=self.params.get("steps"),
                                description=f"Epoch {epoch + 1}")

    def on_train_batch_end(self, batch, logs=None):
        if self.progress is None:
            return
        loss = (logs or {}).get("loss")
        description = f"Epoch {len(self.history) + 1}"
        if loss is not None:
            description += f" [{MUTED_STYLE}]loss {loss:.6f}[/]"
        self.progress.update(self.batch_task, completed=batch + 1, description=description)

    def on_epoch_end(self, epoch, logs=None):
        logs = logs or {}
        train_loss = logs.get("loss")
        val_loss = logs.get("val_loss")
        self.monitor = "val_loss" if val_loss is not None else "loss"
        monitored = val_loss if val_loss is not None else train_loss

        previous = self.history[-1] if self.history else None
        delta = None
        if previous is not None and monitored is not None and previous["monitored"] is not None:
            delta = monitored - previous["monitored"]

        improved = monitored is not None and monitored < self.best_loss
        if improved:
            self.best_loss = monitored
            self.best_epoch = epoch + 1

        lr = self._get_learning_rate()
        row = {
            "epoch": epoch + 1,
            "train_loss": train_loss,
            "val_loss": val_loss,
            "monitored": monitored,
            "delta": delta,
            "lr": lr,
            "lr_changed": previous is not None and lr is not None and previous["lr"] is not None
                          and lr != previous["lr"],
            "duration": time.monotonic() - self.epoch_start_time,
            "improved": improved,
        }
        self.history.append(row)
        if not self.header_logged:
            # Logged with the first row, once it is known whether validation loss exists
            for header_line in self._format_header_lines(val_loss is not None):
                _log_markup(header_line)
            self.header_logged = True
        _log_markup(self._format_epoch_line(row))

        if self.progress is not None:
            self.progress.update(self.epoch_task, advance=1)

    def on_train_end(self, logs=None):
        self.close()
        self._print_summary()

    def close(self):
        """Stops the progress display; safe to call multiple times."""
        if self.progress is not None:
            self.progress.stop()
            self.progress = None

    def _should_show_progress(self) -> bool:
        if self.show_progress is not None:
            return self.show_progress
        # Outside an interactive terminal a live bar can't redraw in place and would
        # only appear once at the end, so rely on the per-epoch log lines instead
        return self.console is not None and self.console.is_terminal \
            and not self.console.is_dumb_terminal

    def _get_learning_rate(self) -> Optional[float]:
        try:
            return float(tf.keras.backend.get_value(self.model.optimizer.learning_rate))
        except Exception:
            return None

    def _epoch_width(self) -> int:
        total_epochs = self.params.get("epochs") if self.params else None
        digits = len(str(total_epochs)) if total_epochs else 3
        return max(len("Epoch"), 2 * digits + 1 if total_epochs else digits)

    def _columns(self, has_val_loss: bool) -> list[tuple[str, int, str]]:
        columns = [("Epoch", self._epoch_width(), ">"), ("Train Loss", LOSS_WIDTH, ">")]
        if has_val_loss:
            columns.append(("Val Loss", LOSS_WIDTH, ">"))
        columns += [("Δ", DELTA_WIDTH, ">"), ("LR", LR_WIDTH, "<"), ("Time", DURATION_WIDTH, ">")]
        return columns

    @staticmethod
    def _join_cells(cells: list[str]) -> str:
        return f" [{MUTED_STYLE}]│[/] ".join(cells)

    def _format_header_lines(self, has_val_loss: bool) -> list[str]:
        columns = self._columns(has_val_loss)
        header = self._join_cells([_cell(title, width, HEADER_STYLE, align)
                                   for title, width, align in columns])
        divider = "─┼─".join("─" * width for _, width, _ in columns)
        return [header, f"[{MUTED_STYLE}]{divider}[/]"]

    def _format_epoch_line(self, row: dict) -> str:
        total_epochs = self.params.get("epochs") if self.params else None
        digits = len(str(total_epochs)) if total_epochs else 1
        epoch_text = f"{row['epoch']:>{digits}}/{total_epochs}" if total_epochs else str(row["epoch"])
        cells = [
            _cell(epoch_text, self._epoch_width(), EPOCH_STYLE, ">"),
            self._format_loss(row["train_loss"], TRAIN_LOSS_STYLE),
        ]
        if row["val_loss"] is not None:
            cells.append(self._format_loss(row["val_loss"], VAL_LOSS_STYLE))
        cells.append(self._format_delta(row["delta"]))
        cells.append(self._format_lr(row["lr"], row["lr_changed"]))
        cells.append(_cell(format_duration(row["duration"]), DURATION_WIDTH, MUTED_STYLE, ">"))
        line = self._join_cells(cells)
        if row["improved"]:
            line += f" [{BEST_STYLE}]★ best[/]"
        return line

    @staticmethod
    def _format_loss(value, style: str) -> str:
        if value is None:
            return _cell("—", LOSS_WIDTH, MUTED_STYLE, "^")
        return _cell(f"{value:.6f}", LOSS_WIDTH, style, ">")

    @staticmethod
    def _format_delta(delta) -> str:
        if delta is None:
            return _cell("—", DELTA_WIDTH, MUTED_STYLE, "^")
        if delta < 0:
            return _cell(f"▼ {abs(delta):.2e}", DELTA_WIDTH, IMPROVED_STYLE, ">")
        if delta > 0:
            return _cell(f"▲ {delta:.2e}", DELTA_WIDTH, WORSENED_STYLE, ">")
        return _cell("= 0", DELTA_WIDTH, MUTED_STYLE, "^")

    @staticmethod
    def _format_lr(lr, changed: bool) -> str:
        if lr is None:
            return _cell("—", LR_WIDTH, MUTED_STYLE, "^")
        # Unchanged rates reserve the space of the " ↓" marker to keep later columns aligned
        if changed:
            return _cell(f"{lr:.2e} ↓", LR_WIDTH, LR_CHANGED_STYLE, "<")
        return _cell(f"{lr:.2e}", LR_WIDTH, LR_STYLE, "<")

    def _print_summary(self):
        if not self.history or self.console is None:
            return
        total_epochs = self.params.get("epochs") if self.params else None
        epochs_run = len(self.history)
        epochs_text = f"{epochs_run}/{total_epochs}" if total_epochs else str(epochs_run)
        if total_epochs and epochs_run < total_epochs:
            epochs_text += f" [{WARNING_STYLE}](early stopped)[/]"

        grid = Table.grid(padding=(0, 2))
        grid.add_column(style="bold")
        grid.add_column()
        grid.add_row("Epochs", epochs_text)
        if self.best_epoch is not None:
            grid.add_row("Best epoch", f"[{BEST_STYLE}]★ {self.best_epoch}[/]")
            grid.add_row(f"Best {self.monitor}", f"[bold {SUCCESS_STYLE}]{self.best_loss:.6f}[/]")
        final_lr = self.history[-1]["lr"]
        if final_lr is not None:
            grid.add_row("Final learning rate", f"{final_lr:.2e}")
        grid.add_row("Duration", format_duration(time.monotonic() - self.start_time))
        self.console.print(Panel(grid,
                                 title=f"[bold {SUCCESS_STYLE}]{self.title} complete[/]",
                                 border_style=SUCCESS_BORDER_STYLE,
                                 expand=False))
