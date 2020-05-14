package net.bitnine.ag3.agensalert.gremlin

// **NOTE : interface 는 생성자를 가질 수 없다
// interface AgensElement(

abstract class AgensElement(
        open val id: String,
        open val datasource: String,
        open val label: String,
        open val properties: Map<String,Any> = emptyMap(),
        open val scratch: Map<String,Any> = emptyMap()
)

data class AgensProperty(
        val key: String,
        val type: String,
        val value: String
)

data class AgensVertex(
        override val id: String,
        override val datasource: String,
        override val label: String,
        override val properties: Map<String,Any> = emptyMap(),
        override val scratch: Map<String,Any> = emptyMap()
)
: AgensElement(id, datasource, label, properties, scratch)

data class AgensEdge(
        override val id: String,
        override val datasource: String,
        override val label: String,
        override val properties: Map<String,Any> = emptyMap(),
        override val scratch: Map<String,Any> = emptyMap(),
        val src: String,
        val dst: String
)
: AgensElement(id, datasource, label, properties, scratch)
