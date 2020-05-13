package net.bitnine.ag3.agensalert.gremlin

import net.bitnine.ag3.agensalert.model.event.EventQry
import net.bitnine.ag3.agensalert.model.event.EventQryRepository
import net.bitnine.ag3.agensalert.model.event.EventRow
import java.time.LocalDate
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
    fun makeRowFromResults(dt:LocalDate, qry:EventQry, results:List<Map<String,Any>>): EventRow{
        val groups: Map<String,Int> = results
                .groupingBy { it.get("group").toString() }.eachCount()
        val groupValue = groups.maxBy { it.value }!!.key

        val labels: Map<String,Int> = results
                .groupingBy { it.get("label").toString() }.eachCount()

        val ids: List<String> = results
                .map { it.get("id").toString() }
                .distinct()

        val minDate = results
                .minBy { it.get("created").toString() }!!.get("created").toString()
        val maxDate = results!!
                .maxBy { it.get("created").toString() }!!.get("created").toString()

//        println("    --> groups = ${groups}")
//        println("    --> labels = ${labels}")
//        println("    --> ids.cnt = ${ids.size}, ${if (ids.size > 0) ids.first()+" .." else "<none>"}")
//        println("    --> created = ${minDate} ~ ${maxDate}")

        return EventRow(id = null, qid=qry.id!!, type=groupValue,
                ids_cnt=ids.size.toLong(), ids=ids.joinToString(separator=","),
                labels=labels.keys.joinToString(separator=","),
                edate= str2date(minDate), etime=LocalTime.now())
    }

    @JvmStatic
    fun getQueries(dt: LocalDate, qryRepository: EventQryRepository, apiClient: AgenspopClient) {
        // **NOTE: flux to list or map
        // https://grokonez.com/reactive-programming/reactor/reactor-convert-flux-into-list-map-reactive-programming

        val fromDate = "2018-01-01";
        val toDate = "2020-05-01";

        val queries = qryRepository.findAllNotDeleted()
                .collectList().block();
        for( query in queries!!){
            print("\n${query.id}: ${query.datasource}_${query.script} ==> ")
            val results = apiClient.execGremlin(query.datasource, query.script, fromDate, toDate)
                    .filter{ e-> !e.isNullOrEmpty() && e.containsKey("group") && e.containsKey("data") && e.containsKey("scratch") }
                    .map{
                        e-> mapOf<String,String>(
                            "group" to e.get("group").toString(),
                            "id" to (e.get("data") as Map<String,Any>).get("id").toString(),
                            "label" to (e.get("data") as Map<String,Any>).get("label").toString(),
                            "created" to (e.get("scratch") as Map<String,Any>).get("_\$\$created").toString()
                    )
                    }
                    .filter{ e-> !e.isEmpty() }
                    .collectList().block()

            print("${results?.size}")

            var row:EventRow
            if( results.isNullOrEmpty().not() ) {
                row = AgenspopUtil.makeRowFromResults(dt, query, results!!)
            }
            else{
                row = EventRow(id = null, qid=query.id!!, type="nodes",
                        ids_cnt=0L, ids="", labels="",
                        edate=dt, etime= LocalTime.now())
            }
        }
    }

}

