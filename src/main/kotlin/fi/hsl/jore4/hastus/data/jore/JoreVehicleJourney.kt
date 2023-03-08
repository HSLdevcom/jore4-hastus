package fi.hsl.jore4.hastus.data.jore

import fi.hsl.jore4.hastus.data.format.JoreJourneyType
import java.util.UUID
import kotlin.time.Duration

/**
 * Jore vehicle journey
 *
 * @property name Displayed name of the vehicle journey
 * @property turnaroundTime Turnaround time
 * @property layoverTime Layover time
 * @property journeyType Type of the vehicle journey
 * @property displayedName Displayed name for the journey
 * @property isVehicleTypeMandatory Is the vehicle type mandatory
 * @property isBackupJourney Is this a backup journey
 * @property isExtraJourney Is this an extra journey
 * @property journeyPatternId Id of the referenced journey pattern
 * @property passingTimes List of stop passing times in order on the vehicle journey
 */
data class JoreVehicleJourney(
    val name: String,
    val turnaroundTime: Duration,
    val layoverTime: Duration,
    val journeyType: JoreJourneyType,
    val displayedName: String,
    val isVehicleTypeMandatory: Boolean,
    val isBackupJourney: Boolean,
    val isExtraJourney: Boolean,
    val journeyPatternId: UUID,
    val passingTimes: List<JorePassingTime>
)
