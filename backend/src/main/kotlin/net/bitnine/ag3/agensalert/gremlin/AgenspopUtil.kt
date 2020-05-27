package net.bitnine.ag3.agensalert.gremlin

import net.bitnine.ag3.agensalert.event.EventQry
import net.bitnine.ag3.agensalert.event.EventQryRepository
import net.bitnine.ag3.agensalert.event.EventRow

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException


object AgenspopUtil {

    @JvmField
    val dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    @JvmStatic
    fun str2date(value:String):LocalDate?{
        try {
            return LocalDate.parse(value, dtFormatter);
        }
        catch (e: DateTimeParseException){
            return null;
        }
    }

    @JvmStatic
    fun str2datetime(value:String):LocalDateTime?{
        try {
            return LocalDateTime.parse(value, dtFormatter);
        }
        catch (e: DateTimeParseException){
            return null;
        }
    }

    @JvmStatic
    fun makeRowFromResults(defaultDatetime:LocalDateTime, qry: EventQry, results:List<Map<String,Any>>): EventRow {
        val groups: Map<String,Int> = results
                .groupingBy { it.get("group").toString() }.eachCount()
        val groupValue = groups.maxBy { it.value }!!.key

        val labels: Map<String,Int> = results
                .groupingBy { it.get("label").toString() }.eachCount()

        val ids: List<String> = results
                .map { it.get("id").toString() }
                .distinct()

        val minDate = results
                .minBy { it.get("timestamp").toString() }!!.get("timestamp").toString()
        val evtDateTime = str2datetime(minDate)

//        val maxDate = results!!
//                .maxBy { it.get("timestamp").toString() }!!.get("timestamp").toString()

        return EventRow(id = null, qid = qry.id!!, type = groupValue,
                ids_cnt = ids.size.toLong(), ids = ids.joinToString(separator = ","),
                labels = labels.keys.joinToString(separator = ","),
                edate = evtDateTime?.toLocalDate() ?: defaultDatetime.toLocalDate(),
                etime = evtDateTime?.toLocalTime() ?: defaultDatetime.toLocalTime()
            )
    }

}

