package com.example.mygamelist

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// DATA CLASSES
data class RawgResponse(
    val results: List<GameResult>
)

data class GameResult(
    val id: Int,
    val name: String,
    val background_image: String?,
    val rating: Float,
    val released: String?
)

data class RawgGameDetails(
    val id: Int,
    val name: String,
    val background_image: String?,
    val genres: List<Genre>,
    val description_raw: String?,
    val rating: Float,
    val ratings_count: Int,
    val ratings: List<RatingBreakdown>,
    val platforms: List<PlatformWrapper>?
)

data class Genre(
    val name: String
)

data class RatingBreakdown(
    val id: Int,         // valores como 5, 4, 3, etc.
    val title: String,   // "exceptional", "recommended", etc.
    val count: Int,
    val percent: Float
)

data class PlatformWrapper(
    val platform: Platform
)

data class Platform(
    val id: Int,
    val name: String,
    val slug: String
)

// INTERFACE
interface RawgApiService {
    @GET("games")
    suspend fun searchGames(
        @Query("key") apiKey: String,
        @Query("search") query: String
    ): RawgResponse

    @GET("games/{id}")
    suspend fun getGameDetails(
        @Path("id") gameId: Int,
        @Query("key") apiKey: String,
        @Query("locale") locale: String = "pt-BR"
    ): RawgGameDetails
}

// RETROFIT CLIENT
object RetrofitClient {
    private const val BASE_URL = "https://api.rawg.io/api/"

    val instance: RawgApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(RawgApiService::class.java)
    }
}
