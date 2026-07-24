package hu.blu3berry.avalon.core.domain.model

/** Outcome of the whole game. Entry names mirror the generated `Info.Winner` DTO enum. */
enum class Winner {
    NOT_DECIDED,
    EVIL,
    GOOD,
}

/** Outcome of a single adventure. Entry names mirror the generated `Info.ScoresItem` DTO enum. */
enum class Score {
    EVIL,
    GOOD,
    UNDECIDED,
}
