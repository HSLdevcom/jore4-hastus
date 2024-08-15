package fi.hsl.jore4.hastus

object Constants {
    const val LANG_FINNISH = "fi_FI"
    const val LANG_SWEDISH = "sv_SE"

    const val DEFAULT_TIMEZONE = "Europe/Helsinki"

    const val DEFAULT_HASTUS_DATE_FORMAT = "yyyyMMdd"
    const val DEFAULT_HASTUS_TIME_FORMAT = "HHmmss"

    const val MIME_TYPE_CSV = "text/csv;charset=iso-8859-1"

    // priorities for network scope in GraphQL
    const val SCHEDULED_STOP_POINT_PRIORITY_DRAFT = 30

    // vehicle schedule frame (timetables) priorities
    const val VEHICLE_SCHEDULE_FRAME_PRIORITY_STAGING = 40

    // Hastus field length limits
    const val MAX_LENGTH_HASTUS_ROUTE_DESCRIPTION = 50
    const val MAX_LENGTH_HASTUS_ROUTE_VARIANT_DESCRIPTION = 60
    const val MAX_LENGTH_HASTUS_STOP_NAME = 100
    const val MAX_LENGTH_HASTUS_STOP_STREET_NAME = 50
}
