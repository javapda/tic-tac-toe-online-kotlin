package tictactoeonline

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import tictactoeonline.domain.PlayingGrid
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RoutesDomainTest {

    @Test
    fun `check strings empty and not empty`() {
        assertTrue("".isEmpty())
        assertFalse("X".isEmpty())
        assertFalse("".isNotEmpty())
        assertTrue("X".isNotEmpty())
    }

    @Test
    fun `valid and invalid field dimensions`() {
        assertFalse(PlayingGrid.isValidFieldDimensionString("1x2"))
        assertFalse(PlayingGrid.isValidFieldDimensionString("2x1"))
        assertFalse(PlayingGrid.isValidFieldDimensionString("3x0"))
        assertTrue(PlayingGrid.isValidFieldDimensionString("3x2"))
        assertTrue(PlayingGrid.isValidFieldDimensionString("2x3"))
    }

    data class NewGameRequestPayloadDataAndErrors(
        val data: NewGameRequestPayload,
        val errors: Set<NewGameRequestPayload.NewGameRequestPayloadError>
    )

    @ParameterizedTest
    @MethodSource("inValidNewGameRequestPayloadDataWithErrors")
    fun `NewGameRequestPayload inValid and the reasons`(dataWithErrors: NewGameRequestPayloadDataAndErrors) {
        val (payload, errors) = dataWithErrors
        assertTrue(payload.isInvalid())
        assertEquals(errors, payload.whyInvalid())

    }

    @ParameterizedTest
    @MethodSource("validNewGameRequestPayloadData")
    fun `NewGameRequestPayload isValid`(payload: NewGameRequestPayload) {
        assertTrue(payload.isValid())
    }

    @ParameterizedTest
    @MethodSource("inValidNewGameRequestPayloadData")
    fun `NewGameRequestPayload isInvalid`(payload: NewGameRequestPayload) {
        assertTrue(payload.isInvalid())
    }

    companion object {
        @JvmStatic
        fun validNewGameRequestPayloadData(): List<NewGameRequestPayload> {
            return listOf(
                NewGameRequestPayload(player1 = "b1@comp.com", player2 = "", size = "4x3"),
                NewGameRequestPayload(player1 = "", player2 = "p2@comp.org", size = "4x3"),
            )
        }

        @JvmStatic
        fun inValidNewGameRequestPayloadData(): List<NewGameRequestPayload> {
            return listOf(
                NewGameRequestPayload(player1 = "b1@comp.com", player2 = "p2@comp.com", size = "4x3"),
                NewGameRequestPayload(player1 = "b1@comp.com", player2 = "", size = "1x2"),
                NewGameRequestPayload(player1 = "b1@comp.com", player2 = "p2@comp.com", size = "1x2"),
            )
        }

        @JvmStatic
        fun inValidNewGameRequestPayloadDataWithErrors(): List<NewGameRequestPayloadDataAndErrors> {
            return listOf(
                NewGameRequestPayloadDataAndErrors(
                    NewGameRequestPayload(player1 = "b1@comp.com", player2 = "p2@comp.com", size = "4x3"),
                    setOf(
                        NewGameRequestPayload.NewGameRequestPayloadError.BOTH_PLAYER_EMAIL_ADDRESSES_PRESENT
                    )
                ),
                NewGameRequestPayloadDataAndErrors(
                    NewGameRequestPayload(player1 = "b1@comp.com", player2 = "p2@comp.com", size = "1x2"),
                    setOf(
                        NewGameRequestPayload.NewGameRequestPayloadError.BOTH_PLAYER_EMAIL_ADDRESSES_PRESENT,
                        NewGameRequestPayload.NewGameRequestPayloadError.INVALID_FIELD_DIMENSIONS_PROVIDED,
                    )
                ),
                NewGameRequestPayloadDataAndErrors(
                    NewGameRequestPayload(player1 = "", player2 = "", size = "1x2"),
                    setOf(
                        NewGameRequestPayload.NewGameRequestPayloadError.BOTH_PLAYER_MISSING_EMAIL_ADDRESS,
                        NewGameRequestPayload.NewGameRequestPayloadError.INVALID_FIELD_DIMENSIONS_PROVIDED,
                    )
                ),
            )
        }

    }
}