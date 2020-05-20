package net.bitnine.ag3.agensalert.gremlin

// **NOTE : interface 는 생성자를 가질 수 없다
// interface AgensElement(


abstract class CyElement(
        open val group: String,
        open val data: CyData,
        open val scratch: Map<String,Any> = emptyMap()
)

data class CyData(
        val id: String,
        val datasource: String,
        val label: String,
        val properties: Map<String,Any> = emptyMap(),
        val source: String?,
        val target: String?
)

data class CyProperty(
        val key: String,
        val type: String,
        val value: String
)

data class CyVertex(
        override val group: String,
        override val data: CyData,
        override val scratch: Map<String,Any> = emptyMap()
)
: CyElement(group, data, scratch)

data class AgensEdge(
        override val group: String,
        override val data: CyData,
        override val scratch: Map<String,Any> = emptyMap()
)
: CyElement(group, data, scratch)
