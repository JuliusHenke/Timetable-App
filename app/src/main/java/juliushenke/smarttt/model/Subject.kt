package juliushenke.smarttt.model

import java.io.Serializable

internal class Subject : Serializable {
    var name: String
        private set
    var room: String? = null
        private set
    var teacher: String? = null
        private set
    var notes: String? = null
        private set
    var color: Int
        private set

    constructor(name: String, color: Int, room: String?, teacher: String?, notes: String?) {
        this.name = name
        this.color = color
        this.room = room
        this.teacher = teacher
        this.notes = notes
    }

    constructor(name: String, color: Int) {
        this.name = name
        this.color = color
    }

    val isNew: Boolean
        get() = teacher == null

    fun markNotNew() {
        teacher = ""
    }

    companion object {
        private const val serialVersionUID = -29238982928391L
    }
}