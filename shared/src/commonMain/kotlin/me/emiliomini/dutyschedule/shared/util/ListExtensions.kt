package me.emiliomini.dutyschedule.shared.util

fun <T, P> List<T>.countByProperty(mapper: (T) -> P): Map<P, Float> {
    val result = mutableMapOf<P, Float>()
    this.forEach {
        val prop = mapper(it)
        if (result.containsKey(prop)) {
            result[prop] = result[prop]!! + 1f
        } else {
            result[prop] = 1f
        }
    }

    return result
}
