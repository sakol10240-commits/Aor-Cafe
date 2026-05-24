package com.example.api

import com.example.data.ClientVisit
import com.example.data.DrinkItem
import com.example.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

enum class ReportInterval {
    DAILY, WEEKLY, MONTHLY
}

object GeminiManager {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val service = retrofit.create(GeminiApiService::class.java)

    suspend fun generateDrinkSalesReport(
        visits: List<ClientVisit>,
        inventory: List<DrinkItem>,
        interval: ReportInterval = ReportInterval.DAILY
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "API_KEY_MISSING"
        }

        val now = System.currentTimeMillis()
        val limitMs = when (interval) {
            ReportInterval.DAILY -> 24 * 60 * 60 * 1000L
            ReportInterval.WEEKLY -> 7 * 24 * 60 * 60 * 1000L
            ReportInterval.MONTHLY -> 30 * 24 * 60 * 60 * 1000L
        }

        val filteredVisits = visits.filter { now - it.orderTime <= limitMs }

        val totalRevenue = filteredVisits.sumOf { it.totalPrice }
        val totalQtySold = filteredVisits.sumOf { it.qty }
        
        val salesByDrink = filteredVisits.groupBy { it.drinkName }
            .mapValues { entry -> entry.value.sumOf { it.qty } }
        val bestSeller = salesByDrink.maxByOrNull { it.value }?.key ?: "None"

        // Grouping for trend analysis
        val sdf = java.text.SimpleDateFormat("EEE, MMM dd", java.util.Locale.getDefault())
        val salesByDay = filteredVisits.groupBy { sdf.format(java.util.Date(it.orderTime)) }
            .mapValues { (_, dayVisits) -> 
                val revenue = dayVisits.sumOf { it.totalPrice }
                val qty = dayVisits.sumOf { it.qty }
                Pair(revenue, qty)
            }

        val prompt = StringBuilder().apply {
            append("Analyze this business data for a ${interval.name.lowercase(java.util.Locale.getDefault())} drink sales and inventory report:\n\n")
            append("### OVERALL ${interval.name} METRICS:\n")
            append("- Timeframe: Last ${when(interval) {
                ReportInterval.DAILY -> "24 Hours"
                ReportInterval.WEEKLY -> "7 Days"
                ReportInterval.MONTHLY -> "30 Days"
            }}\n")
            append("- Total Order Volume: ${filteredVisits.size} client visits\n")
            append("- Total Drinks Sold: $totalQtySold units\n")
            append("- Total Revenue Generated: ฿${String.format("%.2f", totalRevenue)}\n")
            append("- Top Selling Drink: $bestSeller\n\n")
            
            if (interval != ReportInterval.DAILY && salesByDay.isNotEmpty()) {
                append("### DAILY SALES TRENDS IN THIS PERIOD:\n")
                salesByDay.forEach { (date, metrics) ->
                    append("- $date: Revenue: ฿${String.format("%.2f", metrics.first)}, Qty Sold: ${metrics.second}\n")
                }
                append("\n")
            }

            append("### DRINK MENU DIRECTORY:\n")
            inventory.forEach { drink ->
                append("- ${drink.name}: Price: ฿${drink.price}\n")
            }
            append("\n")
            
            append("### RECEIVED CUSTOMER NOTES & ORDER DETAILS COMPILATION:\n")
            val notes = filteredVisits.mapNotNull { it.notes.takeIf { n -> n.isNotBlank() } }
            if (notes.isEmpty()) {
                append("- No custom order feedback or substitute requests logged in this period.\n")
            } else {
                notes.forEach { note -> append("- \"$note\"\n") }
            }
            append("\n")
            append("### GENERATION REQUEST:\n")
            append("Please compile a highly professional and insightful '${interval.name.lowercase(java.util.Locale.getDefault()).replaceFirstChar { it.uppercase(java.util.Locale.getDefault()) }} Drink Analytics & Performance Report' in Markdown. Include:\n")
            append("1. **Executive Performance Summary**: Synthesize sales volume, order frequency, and revenue momentum.\n")
            if (interval != ReportInterval.DAILY) {
                append("2. **Trend Analysis & Dynamic Spikes**: Highlight weekly/monthly patterns, peak sales days, and demand fluctuation over time.\n")
            }
            append("3. **Inventory Lifespan & Depletion Forecast**: Call out specific menu items that are underperforming or near depletion relative to sales velocity. Outline restocking priority.\n")
            append("4. **Feedback & Operational Insights**: Analyze custom notes (such as allergy accommodations, dairy substitutes, sweetness adjustments) to discover latent customer needs.\n")
            append("5. **Strategic Action Plan**: 3 high-impact strategies tailored for this timeframe (e.g. promotional packaging, volume discounts, loyalty campaign slots) to optimize next period's performance.\n\n")
            append("Keep it extremely structured and professional, with elegant spacing, caution warnings, bullet points, headers, and coffee-shop/beverage environment terminology. Act as an expert advisor.")
        }.toString()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are อ๋อคาเฟ่ AI, an elite café business consultant and inventory strategist. You generate beautiful, crisp Markdown reports that are sharp, professional, motivating, and immediately useful for baristas and cafe managers. Format with elegant visual spacing, bullet points, headers, caution emoji alerts, and clear recommendations. Do not use generic introductions or wordy apologies."))
            )
        )

        return try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No response content generated. Please check your data logs or inputs."
        } catch (e: Exception) {
            "ERROR: ${e.localizedMessage}"
        }
    }
}
