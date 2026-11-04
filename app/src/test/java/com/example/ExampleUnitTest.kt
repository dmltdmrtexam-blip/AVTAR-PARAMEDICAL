package com.example

import com.example.data.model.PaymentMethod
import com.example.data.model.StudentCbtAnswer
import com.example.data.repository.AcademyRepository
import com.example.data.repository.PdfExtractorService
import com.example.data.repository.RazorpayPaymentService
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testPdfQuestionExtraction_parsesQuestionsAndAnswers() {
        val sampleText = PdfExtractorService.getSamplePdfExamText()
        val extracted = PdfExtractorService.extractQuestionsFromText(sampleText)

        assertTrue("Extracted questions should not be empty", extracted.isNotEmpty())
        assertEquals(4, extracted.size)

        val first = extracted[0]
        assertTrue("Statement should mention anticoagulant", first.questionText.contains("anticoagulant", ignoreCase = true))
        assertEquals("B", first.detectedAnswer)
        assertTrue("Option B should mention Sodium Citrate", first.optionB.contains("Sodium Citrate", ignoreCase = true))
        assertNotNull("Explanation should be detected", first.detectedExplanation)
    }

    @Test
    fun testRazorpaySignatureVerification_isDeterministic() {
        val order = RazorpayPaymentService.createOrder("crs_dmlt", "DMLT Master", 1499.0, "9876543210")
        val result = RazorpayPaymentService.processAndVerifyPayment(order, PaymentMethod.UPI)

        assertTrue(result.isVerified)
        assertEquals(1499.0, result.transaction.amount, 0.01)
        assertTrue(result.transaction.transactionId.startsWith("pay_"))
        assertTrue(result.transaction.signature.isNotBlank())
    }

    @Test
    fun testCbtScoreCalculation_withNegativeMarking() {
        val repo = AcademyRepository()
        val test = repo.mockTests.value.first()
        val q1 = test.questions[0]
        val q2 = test.questions[1]

        // 1 correct, 1 wrong
        val answers = mapOf(
            q1.id to StudentCbtAnswer(q1.id, selectedOptionKey = q1.correctOptionKey),
            q2.id to StudentCbtAnswer(q2.id, selectedOptionKey = "WRONG_KEY")
        )

        val result = repo.submitExamAttempt(test.id, answers, 120)
        assertEquals(1, result.correctCount)
        assertEquals(1, result.incorrectCount)
        // Score: (1 * 1.0) - (1 * 0.25) = 0.75
        assertEquals(0.75, result.scoreObtained, 0.01)
    }

    @Test
    fun testOtpAuthentication_verifiesCorrectCode() {
        val repo = AcademyRepository()
        val otp = repo.sendLoginOtp("9876543210")
        assertEquals("8447", otp)

        val success = repo.verifyLoginOtp("9876543210", "8447")
        assertTrue(success)
        assertTrue(repo.isUserLoggedIn.value)
    }
}
