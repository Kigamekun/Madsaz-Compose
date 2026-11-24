package id.madsaz.app.feature.plan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlanUiStateTest {
    private fun state(monthly: Long?, quarterly: Long? = null, yearly: Long? = null) = PlanUiState(
        prices = buildMap {
            monthly?.let { put(PlanInterval.MONTHLY, PlanPrice("Rp$it", it)) }
            quarterly?.let { put(PlanInterval.QUARTERLY, PlanPrice("Rp$it", it)) }
            yearly?.let { put(PlanInterval.YEARLY, PlanPrice("Rp$it", it)) }
        },
    )

    @Test
    fun `savings compare each plan with paying the monthly plan for the same number of months`() {
        val prices = state(monthly = 100, quarterly = 270, yearly = 900)

        assertEquals(10, prices.savingsPercent(PlanInterval.QUARTERLY))
        assertEquals(25, prices.savingsPercent(PlanInterval.YEARLY))
        assertNull(prices.savingsPercent(PlanInterval.MONTHLY))
    }

    @Test
    fun `a plan that is not cheaper than the monthly plan has no savings`() {
        val prices = state(monthly = 100, quarterly = 300, yearly = 1300)

        assertNull(prices.savingsPercent(PlanInterval.QUARTERLY))
        assertNull(prices.savingsPercent(PlanInterval.YEARLY))
    }

    @Test
    fun `zero prices cannot be compared and never crash`() {
        assertNull(state(monthly = 0, quarterly = 0).savingsPercent(PlanInterval.QUARTERLY))
        assertNull(state(monthly = 100, quarterly = 0).savingsPercent(PlanInterval.QUARTERLY))
    }

    @Test
    fun `savings are rounded to the nearest percent`() {
        // 1 - 285/300 = 5%; 1 - 286/300 = 4.67% -> 5%; 1 - 298/300 = 0.67% -> 1%; 1 - 299/300 = 0.33% -> no badge.
        assertEquals(5, state(monthly = 100, quarterly = 285).savingsPercent(PlanInterval.QUARTERLY))
        assertEquals(5, state(monthly = 100, quarterly = 286).savingsPercent(PlanInterval.QUARTERLY))
        assertEquals(1, state(monthly = 100, quarterly = 298).savingsPercent(PlanInterval.QUARTERLY))
        assertNull(state(monthly = 100, quarterly = 299).savingsPercent(PlanInterval.QUARTERLY))
    }

    @Test
    fun `savings are unknown until the prices needed to compare them are loaded`() {
        assertNull(state(monthly = null, quarterly = 270).savingsPercent(PlanInterval.QUARTERLY))
        assertNull(state(monthly = 100).savingsPercent(PlanInterval.QUARTERLY))
    }
}
