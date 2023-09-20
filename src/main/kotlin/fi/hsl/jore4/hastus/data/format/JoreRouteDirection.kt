package fi.hsl.jore4.hastus.data.format

import fi.hsl.jore4.hastus.generated.enums.route_direction_enum

enum class JoreRouteDirection {

    ANTICLOCKWISE,
    CLOCKWISE,
    EASTBOUND,
    INBOUND, // integer code 2
    NORTHBOUND,
    OUTBOUND, // integer code 1
    SOUTHBOUND,
    WESTBOUND;

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
    }
}
