package io.github.clsty.joplinshortcut.data.model

data class JoplinNotebook(
    val id: String,
    val title: String,
    val parentId: String = "",
    val updatedTime: Long = 0L,
    val isHidden: Boolean = false
)
