# Issue 134 detector screening protocol

This exploratory pilot compares two existing classifiers before testing repository-specific enforcement. The task list, grouping, policy and measurements below were fixed before running inference. The report records the run and its limitations.

**Inputs.** Use the 20 tasks in `pilot_tasks.jsonl`. Each has a public source revision from 2019 or earlier and a new implementation generated in this chat from the task name. Historical source is a provenance proxy. The benchmark has no individual author attestations. Both versions and all derived variants stay in the same task family. Two families per language are calibration and two are holdout. Projects and generator are shared across splits. Never use this holdout to tune a replacement policy.

**Inputs seen by the classifier.** Each case passes through the GitHub diff reader. Use contiguous added text, preserve whitespace, count nonblank lines for weights, require full eligible-line coverage, and abstain on components exceeding 512 model tokens including special tokens. The minimum of one line is for measuring size effects in research. VKR supports Python. Evaluate GraphCodeBERT on all five languages as a generalization experiment. Its documented in-domain languages in this pilot are Java and Python. The other three are exploratory.

**Policy.** Score class 1 (machine) as 100 times the model softmax, without interpreting it as verified authorship. Select a separate exploratory maximum for each detector from calibration only: the largest scored acceptable example (including the one-line assist), rounded upward to six decimals. With the required strict-greater-than comparison, this gives the lowest six-decimal maximum with zero observed false blocks on calibration. If there are no scored examples of either class in calibration, reject the run. No trained threshold or minimum is installed in the repository.

**Readout.** The headline holdout compares original and generated inputs. Report TP, FP, TN, FN, precision, recall, FPR and FNR among scored inputs; include abstentions by class, coverage and detections among all positive inputs. Break results out by language, eligible-line size (1–9, 10–29, 30+), variant and split. Show the separate light-assist, mixed-file, blank-line reformatting and unchanged-context challenges. The mixed case adds an entire generated implementation alongside a historical implementation; the light-assist case appends one generated comment. A generated snippet is a positive provenance proxy; the pilot cannot determine the required development effort for a real change.

**Blocking decision.** Prioritize legitimate PRs. For screening, use one false block per 100 acceptable PRs as a conservative example budget. Even zero observed errors would require at least 299 independent acceptable PRs to put the exact one-sided 95% binomial upper bound below 1%. This pilot contains ten held-out historical examples across five languages, so a favorable result can justify collecting better evidence only. Related variants do not increase the count of independent families. Establish actual traffic, acceptable costs, recall and coverage floors before a fresh release evaluation. Stop enforcement work if the evidence is insufficient.

## Reproduce the published run

Use Python 3.12 in a dedicated virtual environment. The following commands use PowerShell from the repository root. Set `$work` to a fresh local directory. Staging reads public release files. Inference uses the resulting local files only. Each checkpoint uses roughly 500 MB, in addition to the CPU runtime.

~~~~powershell
$work = Join-Path $env:TEMP ('issue-134-replay-' + [guid]::NewGuid())
python -m venv (Join-Path $work 'venv')
$py = Join-Path $work 'venv/Scripts/python.exe'
& $py -m pip install --only-binary=:all: --index-url https://download.pytorch.org/whl/cpu torch==2.8.0
& $py -m pip install --only-binary=:all: transformers==4.57.6 safetensors==0.7.0 tokenizers==0.22.2
& $py -m scripts.ai_detection.research.prepare_pilot (Join-Path $work 'pilot')
foreach ($name in 'VKR_Model', 'GraphCodeBERT') {
    & $py -m scripts.ai_detection.research.stage_model $name (Join-Path $work $name)
}
$env:HF_HUB_OFFLINE = '1'
$env:HF_HUB_DISABLE_TELEMETRY = '1'
foreach ($name in 'VKR_Model', 'GraphCodeBERT') {
    & $py -m scripts.ai_detection.research.benchmark (Join-Path $work 'pilot/manifest.jsonl') --candidate $name --model-dir (Join-Path $work $name) --output (Join-Path $work "$name.json")
}
~~~~

Inspect the two output JSON files. Each records the manifest and checkpoint hashes, one exact rational score per scored case, line counts, policy, confusion counts, coverage and timing. Compare decisions and hashes with the [recorded results](results). Wall time depends on the host. The reference run used CPU PyTorch 2.8.0+cpu with four threads on Python 3.12.3. The model adapter verifies weights, configuration and tokenizer hashes before loading. The ordinary unit tests use only the Python standard library.

For a new approved corpus, provide one JSON object per line in `manifest.jsonl`. Use a unique `sample_id`, a shared `family_id` for related variants, a `split` of `calibration` or `holdout`, a policy `label` of `acceptable` or `generated`, a `variant`, a provenance reference, and a relative `patch_path` plus its byte-level `patch_sha256`. The referenced JSON holds API-shaped `before` and `after` PR snapshots and an array of file-response `pages`. Keep author evidence private. In this screening runner `original` and `generated` are the primary comparison variants. Other variants are counted separately. A release study must assign entire related projects, contributors and workflows to splits before scoring.
