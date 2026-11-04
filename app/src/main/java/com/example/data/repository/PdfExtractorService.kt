package com.example.data.repository

import com.example.data.model.Question
import com.example.data.model.QuestionOption
import java.util.UUID

object PdfExtractorService {

    data class ExtractedMcq(
        val rawNumber: String,
        val questionText: String,
        val optionA: String,
        val optionB: String,
        val optionC: String,
        val optionD: String,
        val detectedAnswer: String?, // "A", "B", "C", "D"
        val detectedExplanation: String?,
        val confidenceScore: Double, // 0.0 to 1.0
        val isDuplicateWarning: Boolean = false
    ) {
        fun toQuestion(subject: String, topic: String): Question {
            val key = detectedAnswer ?: "A"
            return Question(
                id = "ext_${UUID.randomUUID().toString().take(8)}",
                subject = subject,
                topic = topic,
                questionText = questionText,
                options = listOf(
                    QuestionOption("A", optionA, key == "A"),
                    QuestionOption("B", optionB, key == "B"),
                    QuestionOption("C", optionC, key == "C"),
                    QuestionOption("D", optionD, key == "D")
                ),
                correctOptionKey = key,
                explanation = detectedExplanation ?: "Explanation reviewed and approved by Academy Admin.",
                difficulty = "Medium",
                isApproved = false
            )
        }
    }

    /**
     * Parses PDF/OCR text containing MCQs.
     * Handles patterns like:
     * 1. What is the anticoagulant in ESR test?
     * (A) EDTA
     * (B) Sodium Citrate
     * (C) Heparin
     * (D) Fluoride
     * Ans: B
     * Explanation: 3.8% Sodium Citrate is used in Westergren method (1:4 ratio).
     */
    fun extractQuestionsFromText(
        inputText: String,
        existingQuestions: List<Question> = emptyList()
    ): List<ExtractedMcq> {
        val results = mutableListOf<ExtractedMcq>()
        if (inputText.isBlank()) return results

        // Split by question markers: e.g. "Q1.", "1.", "Q.1", "Question 1:"
        val questionSplitRegex = Regex("""(?=(?:\n|\A)(?:Q(?:uestion)?\s*\.?\s*\d+|\d+)[\.\:\)])""", RegexOption.IGNORE_CASE)
        val rawBlocks = inputText.split(questionSplitRegex).filter { it.isNotBlank() }

        for (block in rawBlocks) {
            val trimmedBlock = block.trim()
            if (trimmedBlock.length < 20) continue

            // 1. Detect question number & statement
            val lines = trimmedBlock.lines().map { it.trim() }.filter { it.isNotBlank() }
            if (lines.isEmpty()) continue

            val firstLine = lines.first()
            val numMatch = Regex("""^(?:Q(?:uestion)?\s*\.?\s*)?(\d+)[\.\:\)]\s*(.*)$""", RegexOption.IGNORE_CASE).find(firstLine)
            val qNum = numMatch?.groupValues?.getOrNull(1) ?: "${results.size + 1}"
            val statementInitial = numMatch?.groupValues?.getOrNull(2) ?: firstLine

            // 2. Extract Options (A, B, C, D)
            var optA = ""
            var optB = ""
            var optC = ""
            var optD = ""
            var detectedAns: String? = null
            var explanation = ""

            // Combine block text to run option extraction regex
            val optARegex = Regex("""(?:\(A\)|A[\.\:\)]|\bA\s*[-–])\s*([^\n]+)""", RegexOption.IGNORE_CASE)
            val optBRegex = Regex("""(?:\(B\)|B[\.\:\)]|\bB\s*[-–])\s*([^\n]+)""", RegexOption.IGNORE_CASE)
            val optCRegex = Regex("""(?:\(C\)|C[\.\:\)]|\bC\s*[-–])\s*([^\n]+)""", RegexOption.IGNORE_CASE)
            val optDRegex = Regex("""(?:\(D\)|D[\.\:\)]|\bD\s*[-–])\s*([^\n]+)""", RegexOption.IGNORE_CASE)

            val aMatch = optARegex.find(trimmedBlock)
            val bMatch = optBRegex.find(trimmedBlock)
            val cMatch = optCRegex.find(trimmedBlock)
            val dMatch = optDRegex.find(trimmedBlock)

            if (aMatch != null) optA = aMatch.groupValues[1].trim()
            if (bMatch != null) optB = bMatch.groupValues[1].trim()
            if (cMatch != null) optC = cMatch.groupValues[1].trim()
            if (dMatch != null) optD = dMatch.groupValues[1].trim()

            // 3. Detect Answer: e.g., "Ans: B", "Answer: (C)", "Correct Option: A"
            val ansRegex = Regex("""(?:Ans(?:wer)?|Correct\s*Option)[\s\:\-]+(?:\()?([A-D])(?:\))?""", RegexOption.IGNORE_CASE)
            val ansMatch = ansRegex.find(trimmedBlock)
            if (ansMatch != null) {
                detectedAns = ansMatch.groupValues[1].uppercase()
            }

            // 4. Detect Explanation: e.g. "Exp: ...", "Explanation: ..."
            val expRegex = Regex("""(?:Exp(?:lanation)?|Notes?)[\s\:\-]+(.+)""", RegexOption.IGNORE_CASE)
            val expMatch = expRegex.find(trimmedBlock)
            if (expMatch != null) {
                explanation = expMatch.groupValues[1].trim()
            }

            // If options were not caught via regex, extract from lines
            if (optA.isBlank() || optB.isBlank()) {
                val foundOptions = mutableListOf<String>()
                for (line in lines) {
                    if (line.matches(Regex("""^[\(\[]?[A-D][\)\]\.\:\-]\s*.*""", RegexOption.IGNORE_CASE))) {
                        foundOptions.add(line.replace(Regex("""^[\(\[]?[A-D][\)\]\.\:\-]\s*""", RegexOption.IGNORE_CASE), ""))
                    }
                }
                if (foundOptions.size >= 4) {
                    optA = foundOptions[0]
                    optB = foundOptions[1]
                    optC = foundOptions[2]
                    optD = foundOptions[3]
                }
            }

            // Fallback statement extraction
            val statement = if (trimmedBlock.contains("(A)")) {
                trimmedBlock.substringBefore("(A)").replace(Regex("""^(?:Q(?:uestion)?\s*\.?\s*)?\d+[\.\:\)]\s*"""), "").trim()
            } else if (trimmedBlock.contains("A.")) {
                trimmedBlock.substringBefore("A.").replace(Regex("""^(?:Q(?:uestion)?\s*\.?\s*)?\d+[\.\:\)]\s*"""), "").trim()
            } else {
                statementInitial
            }

            if (statement.length > 5 && optA.isNotBlank() && optB.isNotBlank()) {
                // Duplicate check against existing question bank
                val isDuplicate = existingQuestions.any { 
                    it.questionText.trim().equals(statement.trim(), ignoreCase = true) 
                }

                val confidence = when {
                    detectedAns != null && optC.isNotBlank() && optD.isNotBlank() -> 0.95
                    optC.isNotBlank() && optD.isNotBlank() -> 0.82
                    else -> 0.65
                }

                results.add(
                    ExtractedMcq(
                        rawNumber = qNum,
                        questionText = statement.ifBlank { "Paramedical Question $qNum" },
                        optionA = optA.ifBlank { "Option A" },
                        optionB = optB.ifBlank { "Option B" },
                        optionC = optC.ifBlank { "Option C" },
                        optionD = optD.ifBlank { "Option D" },
                        detectedAnswer = detectedAns ?: "A",
                        detectedExplanation = explanation.ifBlank { null },
                        confidenceScore = confidence,
                        isDuplicateWarning = isDuplicate
                    )
                )
            }
        }

        return results
    }

    /**
     * Default realistic mock PDF sample text for demonstration & verification.
     */
    fun getSamplePdfExamText(): String {
        return """
            1. Which anticoagulant is used in ESR estimation by Westergren's method?
            (A) EDTA 10%
            (B) 3.8% Sodium Citrate
            (C) Heparin sodium
            (D) Double oxalate
            Ans: B
            Explanation: 3.8% trisodium citrate is used in 1:4 ratio with blood for Westergren ESR test.

            2. What is the target anode material predominantly used in standard diagnostic X-ray tubes?
            (A) Copper
            (B) Tungsten
            (C) Molybdenum
            (D) Lead
            Ans: B
            Explanation: Tungsten is preferred due to high atomic number (Z=74), high melting point (3422°C), and good thermal conductivity.

            3. In Romanowsky blood staining, which component gives blue color to the nucleus?
            (A) Eosin Y
            (B) Methylene Blue
            (C) Giemsa powder
            (D) Methanol
            Ans: B
            Explanation: Methylene blue is a basic dye that binds to acidic nuclear chromatin (DNA/RNA) producing a blue-purple hue.

            4. Which contrast media is most commonly administered for upper gastrointestinal barium meal examinations?
            (A) Barium sulphate suspension
            (B) Gadolinium DTPA
            (C) Iohexol non-ionic
            (D) Sodium diatrizoate
            Ans: A
            Explanation: High-density micro-fine barium sulphate suspension provides high radio-opacity and mucosal coating without toxic absorption.
        """.trimIndent()
    }
}
