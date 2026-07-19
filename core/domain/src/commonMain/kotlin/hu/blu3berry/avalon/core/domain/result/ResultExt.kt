package hu.blu3berry.avalon.core.domain.result

inline fun <D, R, E> Result<D, E>.map(transform: (D) -> R): Result<R, E> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Failure -> Result.Failure(error)
}

inline fun <D, E> Result<D, E>.onSuccess(action: (D) -> Unit): Result<D, E> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <D, E> Result<D, E>.onFailure(action: (E) -> Unit): Result<D, E> {
    if (this is Result.Failure) action(error)
    return this
}

fun <D, E> Result<D, E>.asEmptyDataResult(): EmptyResult<E> = map { }
