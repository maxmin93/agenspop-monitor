package net.bitnine.ag3.agensalert.gremlin

// **NOTE : interface 는 생성자를 가질 수 없다
// interface AgensElement(

abstract class AgensElement(
        open val id:String,
        open val datasource:String,
        open val label:String,
        open val properties:List<AgensProperty> = emptyList()
)

data class AgensProperty(
        val key:String,
        val type:String,
        val value:String
)

data class AgensVertex(
        override val id:String,
        override val datasource:String,
        override val label:String,
        override val properties:List<AgensProperty> = emptyList()
) : AgensElement(id, datasource, label, properties)

data class AgensEdge(
        override val id:String,
        override val datasource:String,
        override val label:String,
        override val properties:List<AgensProperty> = emptyList(),
        val src:String,
        val dst:String
) : AgensElement(id, datasource, label, properties)
