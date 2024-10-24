package com.deploy.accu_project

data class AcupunctureResponse(//Defining response body
    val organ: String,
    val patterns: List<Pattern>
)
data class Pattern(
    val pattern: String,
    val symptoms: List<String>,
    val treatment_points: List<String>
)