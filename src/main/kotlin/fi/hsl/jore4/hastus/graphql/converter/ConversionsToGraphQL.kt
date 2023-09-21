package fi.hsl.jore4.hastus.graphql.converter

import com.expediagroup.graphql.client.jackson.types.OptionalInput
import fi.hsl.jore4.hastus.Constants
import fi.hsl.jore4.hastus.data.jore.JoreBlock
import fi.hsl.jore4.hastus.data.jore.JoreJourneyPatternReference
import fi.hsl.jore4.hastus.data.jore.JorePassingTime
import fi.hsl.jore4.hastus.data.jore.JoreStopPoint
import fi.hsl.jore4.hastus.data.jore.JoreStopReference
import fi.hsl.jore4.hastus.data.jore.JoreVehicleJourney
import fi.hsl.jore4.hastus.data.jore.JoreVehicleScheduleFrame
import fi.hsl.jore4.hastus.data.jore.JoreVehicleService
import fi.hsl.jore4.hastus.generated.inputs.timetables_passing_times_timetabled_passing_time_arr_rel_insert_input
import fi.hsl.jore4.hastus.generated.inputs.timetables_passing_times_timetabled_passing_time_insert_input
import fi.hsl.jore4.hastus.generated.inputs.timetables_service_pattern_scheduled_stop_point_in_journey_pattern_ref_insert_input
import fi.hsl.jore4.hastus.generated.inputs.timetables_vehicle_journey_vehicle_journey_arr_rel_insert_input
import fi.hsl.jore4.hastus.generated.inputs.timetables_vehicle_journey_vehicle_journey_insert_input
import fi.hsl.jore4.hastus.generated.inputs.timetables_vehicle_schedule_vehicle_schedule_frame_insert_input
import fi.hsl.jore4.hastus.generated.inputs.timetables_vehicle_service_block_arr_rel_insert_input
import fi.hsl.jore4.hastus.generated.inputs.timetables_vehicle_service_block_insert_input
import fi.hsl.jore4.hastus.generated.inputs.timetables_vehicle_service_vehicle_service_arr_rel_insert_input
import fi.hsl.jore4.hastus.generated.inputs.timetables_vehicle_service_vehicle_service_insert_input
import fi.hsl.jore4.hastus.graphql.IJSONB
import java.util.UUID
import kotlin.time.toJavaDuration

object ConversionsToGraphQL {

    const val PRIORITY_STAGING = 40

    fun mapToGraphQL(stop: JoreStopPoint): timetables_service_pattern_scheduled_stop_point_in_journey_pattern_ref_insert_input {
        return timetables_service_pattern_scheduled_stop_point_in_journey_pattern_ref_insert_input(
            scheduled_stop_point_label = OptionalInput.Defined(stop.label),
            scheduled_stop_point_sequence = OptionalInput.Defined(stop.sequenceNumber)
        )
    }

    fun mapToGraphQL(
        vehicleScheduleFrame: JoreVehicleScheduleFrame,
        journeyPatternRefIndex: Map<UUID, JoreJourneyPatternReference>
    ): timetables_vehicle_schedule_vehicle_schedule_frame_insert_input {
        return timetables_vehicle_schedule_vehicle_schedule_frame_insert_input(
            vehicle_schedule_frame_id = OptionalInput.Defined(UUID.randomUUID()),
            label = OptionalInput.Defined(vehicleScheduleFrame.label),
            booking_label = OptionalInput.Defined(vehicleScheduleFrame.bookingLabel),
            booking_description_i18n = mapToFiJson(vehicleScheduleFrame.bookingDescription),
            name_i18n = mapToFiJson(vehicleScheduleFrame.name),
            priority = OptionalInput.Defined(PRIORITY_STAGING),
            validity_start = OptionalInput.Defined(vehicleScheduleFrame.validityStart),
            validity_end = OptionalInput.Defined(vehicleScheduleFrame.validityEnd),
            vehicle_services = OptionalInput.Defined(
                timetables_vehicle_service_vehicle_service_arr_rel_insert_input(
                    vehicleScheduleFrame.vehicleServices.map { mapToGraphQL(it, journeyPatternRefIndex) }
                )
            )
        )
    }

    fun mapToGraphQL(
        vehicleService: JoreVehicleService,
        journeyPatternRefIndex: Map<UUID, JoreJourneyPatternReference>
    ): timetables_vehicle_service_vehicle_service_insert_input {
        return timetables_vehicle_service_vehicle_service_insert_input(
            day_type_id = OptionalInput.Defined(vehicleService.dayType),
            name_i18n = mapToFiJson(vehicleService.name),
            blocks = OptionalInput.Defined(
                timetables_vehicle_service_block_arr_rel_insert_input(
                    vehicleService.blocks.map { mapToGraphQL(it, journeyPatternRefIndex) }
                )
            )
        )
    }

    fun mapToGraphQL(
        block: JoreBlock,
        journeyPatternRefIndex: Map<UUID, JoreJourneyPatternReference>
    ): timetables_vehicle_service_block_insert_input {
        return timetables_vehicle_service_block_insert_input(
            finishing_time = OptionalInput.Defined(block.finishingTime.toJavaDuration()),
            preparing_time = OptionalInput.Defined(block.preparingTime.toJavaDuration()),
            vehicle_journeys = OptionalInput.Defined(
                timetables_vehicle_journey_vehicle_journey_arr_rel_insert_input(
                    block.vehicleJourneys.map { mapToGraphQL(it, journeyPatternRefIndex) }
                )
            )
        )
    }

    fun mapToGraphQL(
        vehicleJourney: JoreVehicleJourney,
        journeyPatternRefIndex: Map<UUID, JoreJourneyPatternReference>
    ): timetables_vehicle_journey_vehicle_journey_insert_input {
        val associatedJourneyPatternRef = journeyPatternRefIndex[vehicleJourney.journeyPatternRefId]!!

        return timetables_vehicle_journey_vehicle_journey_insert_input(
            displayed_name = OptionalInput.Defined(vehicleJourney.displayedName),
            is_backup_journey = OptionalInput.Defined(vehicleJourney.isBackupJourney),
            is_extra_journey = OptionalInput.Defined(vehicleJourney.isExtraJourney),
            is_vehicle_type_mandatory = OptionalInput.Defined(vehicleJourney.isVehicleTypeMandatory),
            journey_name_i18n = mapToFiJson(vehicleJourney.name),
            journey_type = OptionalInput.Defined(vehicleJourney.journeyType.toString()),
            layover_time = OptionalInput.Defined(vehicleJourney.layoverTime.toJavaDuration()),
            turnaround_time = OptionalInput.Defined(vehicleJourney.turnaroundTime.toJavaDuration()),
            journey_pattern_ref_id = OptionalInput.Defined(associatedJourneyPatternRef.id),
            timetabled_passing_times = OptionalInput.Defined(
                timetables_passing_times_timetabled_passing_time_arr_rel_insert_input(
                    vehicleJourney.passingTimes.zip(associatedJourneyPatternRef.stops).map { mapToGraphQL(it) }
                )
            )
        )
    }

    fun mapToGraphQL(passingTime: Pair<JorePassingTime, JoreStopReference>): timetables_passing_times_timetabled_passing_time_insert_input {
        return timetables_passing_times_timetabled_passing_time_insert_input(
            arrival_time = OptionalInput.Defined(passingTime.first.arrivalTime?.toJavaDuration()),
            departure_time = OptionalInput.Defined(passingTime.first.departureTime?.toJavaDuration()),
            scheduled_stop_point_in_journey_pattern_ref_id = OptionalInput.Defined(passingTime.second.stopId)
        )
    }

    fun mapToFiJson(text: String): OptionalInput<IJSONB> {
        val converter = JsonbScalarConverter()
        return OptionalInput.Defined(converter.toScalar(linkedMapOf(Constants.LANG_FINNISH to text)))
    }
}
