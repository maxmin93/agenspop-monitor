package net.bitnine.ag3.agensalert.storage

import kotlinx.coroutines.delay
import kotlinx.coroutines.reactive.awaitFirstOrNull
import net.bitnine.ag3.agensalert.gremlin.AgenspopClient
import java.io.BufferedReader
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Path
import java.util.*
import java.util.concurrent.atomic.AtomicLong

//class RealtimeTester(val files:List<Path>, val client: AgenspopClient, val datasource:String) {
object RealtimeTester {

    // **NOTE: 문자열 구분자를 (') 으로 하면 데이터 안의 (')과 혼동되어 오류 발생
    //    => (')을 encoding / decoding 하고, 사전 확인도 필요함
    //    => 오류 발생한 구문은 agenspop 에서 실패하여 데이터가 들어가지 않음

    fun makeNodeScript(datasource: String, label:String, cols:List<String>, line:String):String{
        val values:List<String> = line.split(",")
        if (cols.size != values.size) throw InvalidPropertiesFormatException("Wrong column size(${cols.size}) => ${line}")

        val idValue = "${datasource}_${label}_${values.first()}"
        var script = "g.addV(\"$label\").property(T.id,\"${idValue}\")"
        for ( (i, value) in values.withIndex()){
            var checkedValue = value.replace('"','\'')
            script += ".property(\"${cols[i]}\",\"$checkedValue\")"
        }
        return script
    }

/*
    "g.V('modern_2').as('src').V('modern_3').as('dst')
            .addE('like').from('src').to('dst')
                    .property(T.id,'modern_16').property('since',2018)"
 */

    fun makeEdgeScript(datasource: String, label:String, srcLabel:String, dstLabel:String, cols:List<String>, line:String):String{
        val values:List<String> = line.split(",")
        if (cols.size != values.size) throw InvalidPropertiesFormatException("Wrong column size(${cols.size}) => ${line}")

        val idValue = "${datasource}_${label}_${values.first()}"
        val srcIdValue = "${datasource}_${srcLabel}_${values[1]}"
        val dstIdValue = "${datasource}_${dstLabel}_${values[2]}"

        var script = "g.V(\"$srcIdValue\").as(\"src\").V(\"$dstIdValue\").as(\"dst\").addE(\"${label}\").from(\"src\").to(\"dst\").property(T.id,\"${idValue}\")"
        for ( (i, value) in values.withIndex()){
            var checkedValue = value.replace('"','\'')
            script += ".property(\"${cols[i]}\",\"$checkedValue\")"
        }
        return script
    }

    suspend fun importNodesCountry(filePath:Path, client: AgenspopClient, datasource:String){
        val label = "country"
        val counterResponse = AtomicLong(1L)
        // val counterLine = AtomicLong(1L)

        val reader: BufferedReader = File(filePath.toUri()).bufferedReader(charset = Charset.forName("UTF-8"))
        var line:String? = reader.readLine()        // _id,code,desc
        val cols = line!!.trim().split(",")

        line = reader.readLine()                    // first line after skip header
        while( line.isNullOrBlank().not() ){
            delay(10L)                   // for safety
            val script:String = makeNodeScript(datasource, label, cols, line!!)
            client.execGremlin(datasource, script).subscribe {
                val currResponse = counterResponse.incrementAndGet()
//                val idValue = (it.get("data") as Map<String,Any>).get("id")
//                println("  -> ${currResponse} : ${it.isNotEmpty()} '${idValue}'")
                println("  -> ${currResponse} : ${it.isNotEmpty()}")
            }
            // next line
            line = reader.readLine()
        }

        reader.close()
    }

    suspend fun importNodesAirport(filePath:Path, client: AgenspopClient, datasource:String){
        val label = "airport"
        val counterResponse = AtomicLong(1L)
        // val counterLine = AtomicLong(1L)

        val reader: BufferedReader = File(filePath.toUri()).bufferedReader(charset = Charset.forName("UTF-8"))
        var line:String? = reader.readLine()        // _id,code,icao,desc,region,runways,longest,elev,country,city,lat,lon
        val cols = line!!.trim().split(",")

        line = reader.readLine()                    // first line after skip header
        while( line.isNullOrBlank().not() ){
            delay(10L)                   // for safety
            val script:String = makeNodeScript(datasource, label, cols, line!!)
            client.execGremlin(datasource, script).subscribe {
                val currResponse = counterResponse.incrementAndGet()
                println("  -> ${currResponse} : ${it.isNotEmpty()}")
            }
            line = reader.readLine()                // next line
        }

        reader.close()
    }

    suspend fun importEdgesContains(filePath:Path, client: AgenspopClient, datasource:String){
        val label = "contains"
        val counterResponse = AtomicLong(1L)
        // val counterLine = AtomicLong(1L)

        val reader: BufferedReader = File(filePath.toUri()).bufferedReader(charset = Charset.forName("UTF-8"))
        var line:String? = reader.readLine()        // _id,group,item
        val cols = line!!.trim().split(",")

        line = reader.readLine()                    // first line after skip header
        while( line.isNullOrBlank().not() ){
            delay(10L)                   // for safety
            val script:String = makeEdgeScript(datasource, label, "country", "airport", cols, line!!)
            client.execGremlin(datasource, script).subscribe {
                val currResponse = counterResponse.incrementAndGet()
                println("  -> ${currResponse} : ${it.isNotEmpty()}")
            }
            line = reader.readLine()                // next line
        }

        reader.close()
    }

    suspend fun importEdgesRoute(filePath:Path, client: AgenspopClient, datasource:String){
        val label = "route"
        val counterResponse = AtomicLong(1L)
        // val counterLine = AtomicLong(1L)

        val reader: BufferedReader = File(filePath.toUri()).bufferedReader(charset = Charset.forName("UTF-8"))
        var line:String? = reader.readLine()        // _id,group,item
        val cols = line!!.trim().split(",")

        line = reader.readLine()                     // first line after skip header
        while( line.isNullOrBlank().not() ){
            var randNum = (100..500).random()   // random delay
            delay(randNum.toLong())                  // for safety

            val script:String = makeEdgeScript(datasource, label, "airport", "airport", cols, line!!)
            client.execGremlin(datasource, script).subscribe {
                val currResponse = counterResponse.incrementAndGet()
                println("  -> ${currResponse} : ${it.isNotEmpty()}")
            }
            line = reader.readLine()                // next line
        }

        reader.close()
    }

}