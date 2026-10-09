# Walkthrough - 6-Sided Dice Roller App (Perudo)

Successfully implemented a modern Jetpack Compose 6-sided dice roller application featuring dynamic dice management (add dice with "+" button, remove dice by tapping on them, roll all dice).

## Changes

### Dice Roller Feature

#### [DiceViewModel.kt](file:///C:/Users/frank/Desktop/Progetti/Perudo/Perudo/app/src/main/java/com/example/perudo/DiceViewModel.kt)
- Manages the state of active dice (starting with 5 default dice).
- Provides methods `addDie()`, `removeDie(id)`, and `rollDice()` (randomizing values 1-6).

#### [DiceView.kt](file:///C:/Users/frank/Desktop/Progetti/Perudo/Perudo/app/src/main/java/com/example/perudo/ui/components/DiceView.kt)
- Material 3 Card component representing a 6-sided die.
- Fully clickable to remove the specific die from the collection.

#### [DiceScreen.kt](file:///C:/Users/frank/Desktop/Progetti/Perudo/Perudo/app/src/main/java/com/example/perudo/ui/screen/DiceScreen.kt)
- Main screen layout with TopAppBar, adaptive grid of dice, total dice counter, floating action button (`+`) to add dice, and prominent "LANCIA I DADI" button.

#### [MainActivity.kt](file:///C:/Users/frank/Desktop/Progetti/Perudo/Perudo/app/src/main/java/com/example/perudo/MainActivity.kt)
- Updated to host `DiceScreen`.

#### [build.gradle.kts](file:///C:/Users/frank/Desktop/Progetti/Perudo/Perudo/app/build.gradle.kts) & [libs.versions.toml](file:///C:/Users/frank/Desktop/Progetti/Perudo/Perudo/gradle/libs.versions.toml)
- Added Lifecycle ViewModel Compose dependency and updated compileSdk/targetSdk to 37.

## Verification Results

### Automated Tests
- Gradle build `app:assembleDebug` completed successfully with **0 errors**.
