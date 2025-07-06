package com.gizemir.plantapp.data.remote.dto.plant_care

import com.google.gson.annotations.SerializedName

data class GeminiRequestDto(
    @SerializedName("contents")
    val contents: List<ContentDto>
)

data class ContentDto(
    @SerializedName("parts")
    val parts: List<PartDto>
)

data class PartDto(
    @SerializedName("text")
    val text: String
)

data class GeminiResponseDto(
    @SerializedName("candidates")
    val candidates: List<CandidateDto>?,
    @SerializedName("error")
    val error: ErrorDto?
)

data class CandidateDto(
    @SerializedName("content")
    val content: ContentDto?,
    @SerializedName("finishReason")
    val finishReason: String?,
    @SerializedName("index")
    val index: Int?
)

data class ErrorDto(
    @SerializedName("code")
    val code: Int?,
    @SerializedName("message")
    val message: String?
) 