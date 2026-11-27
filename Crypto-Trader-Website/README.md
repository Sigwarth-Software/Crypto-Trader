# Crypto Trader — Website Module

## The public face of Crypto Trader: brand, story, and a trading gateway

---

Crypto‑Trader‑Website is the marketing and information hub of the platform. It
tells the story, showcases capabilities, and guides visitors to the right
place — whether that’s product overviews, plan comparisons, or deep technical
documentation.

!!! warning "Disclaimer"
    Past results and backtests do not guarantee future performance.
    Always start in paper mode. Liability is your own.

---

### ⭐️ Core Features

- **Value Proposition**: Presents plans, features, and safety guardrails in an approachable way for new users.
- **Ecosystem Integration**: Direct links to Guides, APIs, and Module References in the central hub.
- **Real-time Information**: Surfaces platform updates, roadmap changes, and performance directions at a glance.
- **Brand Consistency**: Uses shared visuals and conventions from the `Crypto-Trader-Assets` module.

---

### ✅ Why it matters

*   **First impressions**: turns interest into confident exploration.
*   **Orientation**: helps users find the right path — from overview to deep dive.
*   **Consistency**: shares the same brand assets and vocabulary as the product UI.

---

### 🛠️ Technology Stack

- **Framework**: Angular 20 (Modern, Performant)
- **Styling**: SCSS with shared brand variables
- **Documentation**: Compodoc (Technical Reference)
- **Deployment**: Integrated with GitHub Pages via the Docs hub

---

### 👨‍💻 Development

To generate new components with the project's specific requirements (SCSS,
no tests, automatic `angular.json` update), use the following command from the
`Crypto-Trader-Website` directory:

```bash
npm run g:c -- <path/to/component>
```

**Example:**
```bash
npm run g:c -- pages/new-feature
```

This will:
1. Create the component files in `angular/components/pages/new-feature/`.
2. Add the component's SCSS path to the `styles` array in `angular.json` in alphabetical order.
3. Automatically register the component in `pages.ts` or `elements.ts` using `ts-morph` (if applicable).

---

### 🔗 Documentation Quick Links

- [Guides](https://sigwarth-software.github.io/Crypto-Trader/guides/)
- [CT API](https://sigwarth-software.github.io/Crypto-Trader/api/)
- [Data API](https://sigwarth-software.github.io/Crypto-Trader/data/)
- [Reference Hub](https://sigwarth-software.github.io/Crypto-Trader/reference/)

---

## ❓ Questions or help

Email Oliver Lear Sigwarth (@theoliverlear): `sigwarthsoftware@gmail.com`

## 📄 License

See `LICENSE.md` in the repository root.
