package fi.hsl.jore4.hastus

object Constants {

    const val LANG_FINNISH = "fi_FI"
    const val LANG_SWEDISH = "se_SE"

    const val DEFAULT_HASTUS_DATE_FORMAT = "yyyyMMdd"
    const val DEFAULT_HASTUS_TIME_FORMAT = "HHmmss"

    // priorities for network scope in GraphQL
    const val SCHEDULED_STOP_POINT_PRIORITY_DRAFT = 30

    // vehicle schedule frame (timetables) priorities
    const val VEHICLE_SCHEDULE_FRAME_PRIORITY_STAGING = 40
}
