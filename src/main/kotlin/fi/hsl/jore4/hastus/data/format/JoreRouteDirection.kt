package fi.hsl.jore4.hastus.data.format

import fi.hsl.jore4.hastus.generated.enums.route_direction_enum
import fi.hsl.jore4.hastus.generated.enums.timetables_route_direction_enum

enum class JoreRouteDirection {

    ANTICLOCKWISE,
    CLOCKWISE,
    EASTBOUND,
    INBOUND, // integer code 2
    NORTHBOUND,
    OUTBOUND, // integer code 1
    SOUTHBOUND,
    WESTBOUND;

    fun toGraphQLInNetworkScope(): timetables_route_direction_enum = when (this) {
        ANTICLOCKWISE -> timetables_route_direction_enum.ANTICLOCKWISE
        CLOCKWISE -> timetables_route_direction_enum.CLOCKWISE
        INBOUND -> timetables_route_direction_enum.INBOUND
        EASTBOUND -> timetables_route_direction_enum.EASTBOUND
        NORTHBOUND -> timetables_route_direction_enum.NORTHBOUND
        OUTBOUND -> timetables_route_direction_enum.OUTBOUND
        SOUTHBOUND -> timetables_route_direction_enum.SOUTHBOUND
        WESTBOUND -> timetables_route_direction_enum.WESTBOUND
    }

    companion object {

        // Source type relates to the network (default) scope in the GraphQL API.
        fun from(routeDirection: route_direction_enum): JoreRouteDirection = when (routeDirection) {
            // the most common first
            route_direction_enum.INBOUND -> INBOUND
            route_direction_enum.OUTBOUND -> OUTBOUND
            // the rest
            route_direction_enum.ANTICLOCKWISE -> ANTICLOCKWISE
            route_direction_enum.CLOCKWISE -> CLOCKWISE
            route_direction_enum.EASTBOUND -> EASTBOUND
            route_direction_enum.NORTHBOUND -> NORTHBOUND
            route_direction_enum.SOUTHBOUND -> SOUTHBOUND
            route_direction_enum.WESTBOUND -> WESTBOUND
            else -> throw IllegalStateException("Unsupported route direction: $routeDirection")
        }

        // Source type relates to the "timetables" scope in the GraphQL API.
        fun from(routeDirection: timetables_route_direction_enum): JoreRouteDirection = when (routeDirection) {
            // the most common first
            timetables_route_direction_enum.INBOUND -> INBOUND
            timetables_route_direction_enum.OUTBOUND -> OUTBOUND
            // the rest
            timetables_route_direction_enum.ANTICLOCKWISE -> ANTICLOCKWISE
            timetables_route_direction_enum.CLOCKWISE -> CLOCKWISE
            timetables_route_direction_enum.EASTBOUND -> EASTBOUND
            timetables_route_direction_enum.NORTHBOUND -> NORTHBOUND
            timetables_route_direction_enum.SOUTHBOUND -> SOUTHBOUND
            timetables_route_direction_enum.WESTBOUND -> WESTBOUND
            else -> throw IllegalStateException("Unsupported route direction: $routeDirection")
        }
    }
}
