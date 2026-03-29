---
name: Workout Page UI Specification
description: Detailed UI design contract for redesigning the Workout page using Stitch, covering layout, components, interactions, and data handling.
type: reference
---

# Workout Page UI Specification

## 1. Overview
The Workout page provides a complete end‑to‑end experience for a gym session. It is divided into five logical zones:
1. **Session Header (Control Tower)** – macro overview, pinned at top.
2. **Exercise Card (Instructional Hub)** – per‑exercise details.
3. **Logging Table (Data Entry)** – fast set logging.
4. **Rest & Recovery Engine** – timers, cues, hydration.
5. **Post‑Workout Summary** – PR badges, tonnage, notes.

The design follows **Glassmorphism**: frosted‑glass containers with subtle blur, semi‑transparent gradients, and smooth motion.

---

## 2. Session Header – “Control Tower”
| Component | Description | Interaction |
|-----------|-------------|-------------|
| **Active Chronometer** | Live timer showing total workout duration. | Starts when workout begins; pauses on manual stop.
| **Overall Progress Bar** | Horizontal bar (e.g., 40 % complete) reflecting total planned sets vs. completed sets. | Updates automatically as sets are marked complete.
| **Volume Tracker** | Counter of cumulative weight lifted (kg/lb). | Increments as each set weight × reps is logged.

**Layout** – full‑width glass container pinned (`position: sticky; top: 0`). Contains three horizontally‑aligned items with equal spacing.

---

## 3. Exercise Card – “Instructional Hub”
Each exercise appears as an independent card stacked vertically.

| Sub‑Component | Details |
|---------------|---------|
| **Form Visualization** | Looping Lottie animation or high‑quality GIF showing proper form. Embedded via `<lottie-player>` with `autoplay` and `loop`.
| **Muscle Heatmap** | Small anatomical icon highlighting primary (red) and secondary (orange) muscles.
| **Historical Reference** | Text label `Last time: 80 kg × 8` pulled from previous workout data.
| **Plate Calculator** | Button **"Load Plates"** opens modal. Modal lists plate combinations (20 kg, 10 kg, 5 kg) to reach target weight.

**Card Styling** – glass pane with rounded corners, subtle drop‑shadow, and inner padding. Card height adapts to content; animation area occupies 40 % of card height.

---

## 4. Logging Table – “Data Entry”
High‑speed entry for each set.

| Field | Type | UX Details |
|------|------|------------|
| **Set Number** | Auto‑increment label |
| **Weight** | Numeric input (kg) |
| **Reps** | Numeric input |
| **RPE** | Slider 1‑10 (or dropdown) |
| **1RM (auto)** | Read‑only calculated field (`1RM = weight * (1 + reps/30)`) |
| **Complete** | Large checkbox (tap‑friendly) |

**Features**
- **Previous Set Ghosting** – placeholder shows last session’s weight/reps.
- **Auto‑Calculated 1RM** updates in real time as weight/reps change.
- **RPE = 10** triggers suggestion: *"Consider lowering weight for next set to maintain safety."*
- **Status Checkboxes** have a 44 px tap target for accessibility.

---

## 5. Rest & Recovery Engine
| Element | Behavior |
|----------|----------|
| **Interstitial Timer** | When a set is checked, a semi‑transparent overlay slides up showing a countdown (default 90 s). Overlay uses glass effect and fills with a blue gradient as time expires.
| **Sound/Haptic Cues** | - 30 s left: short vibration.<br>- 5 s left: three beeps.<br>- Time up: long pulse.
| **Hydration Reminder** | After every 3 sets, a subtle toast “💧 Stay hydrated!” appears.

---

## 6. Intelligence & Edge Cases
1. **Swap Feature** – Press **Swap** on a card → modal shows equivalent alternatives (e.g., Cable Fly → Dumbbell Fly) with same rep/weight targets.
2. **Superset Logic** – Two cards can be linked; UI draws a thin vertical connector and removes the rest timer between them.
3. **Drop‑Set Support** – Button **Drop‑Set** adds a special row where weight auto‑decrements after failure.

---

## 7. Post‑Workout Summary
When **Finish Workout** is tapped, transition to a summary screen:
- **New Personal Records (PRs)** – Badges with confetti animation for any new PR.
- **Total Tonnage** – Sum of all weight × reps.
- **Notes/Journaling** – Multiline text area for user reflections; persisted to the workout record and displayed on next session start.

---

## 8. Data Flow & Integration
1. **Load Workout** – API `GET /workouts/{id}` provides planned exercises and historical data.
2. **Log Set** – `POST /workouts/{id}/sets` with `{exerciseId, weight, reps, rpe}`.
3. **Swap / Superset** – `PATCH /workouts/{id}/exercises` to update ordering or substitution.
4. **Finish** – `POST /workouts/{id}/complete` returns summary payload used to render the post‑workout screen.

All endpoints return JSON; the frontend uses a lightweight Redux‑style store for offline capability and sync.

---

## 9. Accessibility & Responsiveness
- All interactive elements meet WCAG 2.1 AA contrast (minimum 4.5:1).
- Tap targets ≥ 44 px.
- Keyboard navigation supported (tab order, aria‑labels).
- Screen‑reader friendly labels for timers, progress bars, and dynamic updates (ARIA live regions).
- Responsive layout: single‑column on mobile, two‑column on tablets, full‑width on desktop.

---

## 10. Design Tokens (Glassmorphism)
```json
{
  "color": {
    "background": "rgba(255,255,255,0.25)",
    "gradientStart": "rgba(0,122,255,0.2)",
    "gradientEnd": "rgba(0,122,255,0.05)"
  },
  "blur": "12px",
  "borderRadius": "12px",
  "shadow": "0 4px 30px rgba(0,0,0,0.1)"
}
```
Apply these tokens to every glass container (Session Header, Exercise Card, Rest Overlay, Summary modal).

---

*Generated by Claude Code – UI‑SPEC for the Workout page.*
