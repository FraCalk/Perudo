# 6-Sided Dice Roller App Implementation Plan (Updated)

Implement a modern Jetpack Compose application for rolling 6-sided dice with dynamic dice management:
- **Add dice**: A "+" button adds a new 6-sided die to the collection.
- **Remove dice**: Tapping directly on a die removes it from the collection.
- **Roll dice**: A "Lancia Dadi" button rolls all active dice with an engaging visual effect/animation state.
- **Summary**: Displays total sum and individual die values.

## User Review Required

> [!IMPORTANT]
> - Interactive dice management: Tap "+" to add a die, tap any individual die to remove it.
> - Clean Material 3 UI with responsive grid/flow of dice.

## Proposed Changes

### Dice Roller Feature

#### [NEW] [DiceViewModel.kt](file:///C:/Users/frank/Desktop/Progetti/Perudo/Perudo/app/src/main/java/com/example/perudo/DiceViewModel.kt)
- State management for the list of dice (each die has an ID and a current value 1-6), methods to add a die, remove a die by ID, and roll all dice.

#### [NEW] [DiceView.kt](file:///C:/Users/frank/Desktop/Progetti/Perudo/Perudo/app/src/main/java/com/example/perudo/ui/components/DiceView.kt)
- Composable component representing a 6-sided die (with pips/dots or numbers 1-6), clickable to trigger removal.

#### [NEW] [DiceScreen.kt](file:///C:/Users/frank/Desktop/Progetti/Perudo/Perudo/app/src/main/java/com/example/perudo/ui/screen/DiceScreen.kt)
- Main screen with header, grid of dice, "+" action button to add a die, and "Lancia" button.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/frank/Desktop/Progetti/Perudo/Perudo/app/src/main/java/com/example/perudo/MainActivity.kt)
- Host `DiceScreen`.

## Verification Plan

### Automated Tests
- Gradle build (`app:assembleDebug`).

### Manual Verification
- Test adding dice via "+" button, removing dice by clicking them, and rolling dice.
