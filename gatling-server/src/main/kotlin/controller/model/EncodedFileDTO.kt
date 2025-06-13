package org.misarch.gatlingserver.controller.model

data class EncodedFileDTO(
    val fileName: String,
    val encodedWorkFileContent: String,
    val encodedUserStepsFileContent: String,
)
