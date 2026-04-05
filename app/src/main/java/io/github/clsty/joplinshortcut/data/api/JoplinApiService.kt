package io.github.clsty.joplinshortcut.data.api

import retrofit2.http.GET
import retrofit2.http.Query

data class JoplinNotesResponse(val items: List<JoplinNoteDto>, val has_more: Boolean)
data class JoplinFoldersResponse(val items: List<JoplinFolderDto>, val has_more: Boolean)
data class JoplinNoteDto(val id: String, val title: String, val parent_id: String, val updated_time: Long = 0)
data class JoplinFolderDto(val id: String, val title: String, val parent_id: String = "", val updated_time: Long = 0)

interface JoplinApiService {
    @GET("notes")
    suspend fun getNotes(
        @Query("token") token: String,
        @Query("fields") fields: String = "id,title,parent_id,updated_time",
        @Query("limit") limit: Int = 100,
        @Query("page") page: Int = 1
    ): JoplinNotesResponse

    @GET("folders")
    suspend fun getFolders(
        @Query("token") token: String,
        @Query("fields") fields: String = "id,title,parent_id,updated_time",
        @Query("limit") limit: Int = 100,
        @Query("page") page: Int = 1
    ): JoplinFoldersResponse
}
