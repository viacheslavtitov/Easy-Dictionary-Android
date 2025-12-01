package org.easydictionary.app.domain.utils

import java.time.format.DateTimeFormatter
import javax.inject.Inject

interface DateRangeFormatter {
    fun format(): DateTimeFormatter
}

class DateRangeFormatterImpl @Inject constructor(): DateRangeFormatter {
    override fun format(): DateTimeFormatter {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
}