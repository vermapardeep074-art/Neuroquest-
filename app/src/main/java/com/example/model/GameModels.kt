package com.example.model

enum class GameType(val displayName: String, val category: String, val description: String) {
    MEMORY("Memory Grid", "Memory", "Memorize and recall the highlighted tiles in a grid."),
    MATH("Speed Math", "IQ", "Solve mathematical equations as fast as possible."),
    REACTION("Quick Tap", "Reaction", "Tap the screen as fast as you can when the target color displays."),
    FOCUS("Stroop Test", "Focus", "Choose if the written word matches the active color name."),
    SEQUENCE("Simon Pattern", "Sequence", "Repeat the flashing button sequence in order.")
}

enum class Difficulty(val multiplier: Float, val starsNeeded: Int) {
    EASY(1.0f, 0),
    MEDIUM(1.5f, 5),
    HARD(2.0f, 15),
    EXPERT(2.5f, 30),
    MASTER(3.0f, 50),
    LEGEND(4.0f, 80),
    IMPOSSIBLE(5.0f, 120)
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val targetValue: Int,
    val rewardCoins: Int,
    val type: String // "games_played", "level", "streak", "coins", "score"
) {
    companion object {
        val ALL = listOf(
            Achievement("first_steps", "First Steps", "Play your first training session.", "play_arrow", 1, 50, "games_played"),
            Achievement("brain_builder", "Brain Builder", "Complete 10 training games.", "fitness_center", 10, 100, "games_played"),
            Achievement("mind_master", "Mind Master", "Complete 50 training games.", "psychology", 50, 300, "games_played"),
            Achievement("rising_star", "Rising Star", "Reach Player Level 5.", "star", 5, 100, "level"),
            Achievement("neuro_lord", "Neuro Lord", "Reach Player Level 15.", "workspace_premium", 15, 500, "level"),
            Achievement("streak_3", "Habit Builder", "Maintain a 3-day streak.", "local_fire_department", 3, 100, "streak"),
            Achievement("streak_7", "Cognitive Warrior", "Maintain a 7-day streak.", "whatshot", 7, 250, "streak"),
            Achievement("coin_collector", "Wealthy Mind", "Earn a total of 1000 coins.", "monetization_on", 1000, 200, "coins"),
            Achievement("perfect_accuracy", "Laser Sharp", "Get a 95% accuracy score.", "gps_fixed", 95, 150, "score")
        )
    }
}
