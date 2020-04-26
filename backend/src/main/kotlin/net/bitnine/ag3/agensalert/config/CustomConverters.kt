package net.bitnine.ag3.agensalert.config

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import java.io.IOException
import java.util.*


@ReadingConverter
class ArrayToListConverter(val mapper: ObjectMapper) : Converter<String, MutableList<String>> {

    private val logger = LoggerFactory.getLogger(ArrayToListConverter::class.java)

    override fun convert(json: String): MutableList<String> {
        val reader = mapper.readerFor(object: TypeReference<List<String>>(){})
        try {
            return reader.readValue(json)
        } catch (e: IOException) {
            logger.error("Problem while parsing ARRAY: []", json, e)
        }
        return mutableListOf<String>()
    }
}

@WritingConverter
class ListToArrayConverter(val mapper: ObjectMapper) : Converter<List<String>, String> {

    private val logger = LoggerFactory.getLogger(ListToArrayConverter::class.java)

    override fun convert(source: List<String>): String {
        val writer = mapper.writerFor(object: TypeReference<List<String>>(){})
        try {
            return writer.writeValueAsString(source)
        } catch (e: JsonProcessingException) {
            logger.error("Error occurred while serializing map to List<String>: []", source, e)
        }
        return "[]"
    }
}
