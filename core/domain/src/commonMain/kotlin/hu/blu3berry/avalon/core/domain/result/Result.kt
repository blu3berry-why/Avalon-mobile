package hu.blu3berry.avalon.core.domain.result

sealed interface Result<out D, out E> {
    data class Success<out D, out E>(val data: D) : Result<D, E>
    data class Failure<out D, out E>(val error: E) : Result<D, E>
}
