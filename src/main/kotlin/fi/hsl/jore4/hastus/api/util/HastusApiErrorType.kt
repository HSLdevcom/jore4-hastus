package fi.hsl.jore4.hastus.api.util

import fi.hsl.jore4.hastus.graphql.converter.GraphQLAuthenticationFailedException
import fi.hsl.jore4.hastus.service.importing.CannotFindJourneyPatternRefByRouteLabelAndDirectionException
import fi.hsl.jore4.hastus.service.importing.CannotFindJourneyPatternRefByStopPointLabelsException
import fi.hsl.jore4.hastus.service.importing.CannotFindJourneyPatternRefByTimingPlaceLabelsException
import fi.hsl.jore4.hastus.service.importing.ErrorWhileProcessingHastusDataException
import fi.hsl.jore4.hastus.service.importing.InvalidHastusDataException

enum class HastusApiErrorType {
    CannotFindJourneyPatternRefByRouteLabelAndDirectionError,
    CannotFindJourneyPatternRefByStopPointLabelsError,
    CannotFindJourneyPatternRefByTimingPlaceLabelsError,
    ErrorWhileProcessingHastusDataError,
    GraphQLAuthenticationFailedError,
    IllegalArgumentError,
    InvalidHastusDataError,
    UnknownError;

    companion object {
        fun from(exception: Exception): HastusApiErrorType = when (exception) {
            is InvalidHastusDataException -> InvalidHastusDataError
            is CannotFindJourneyPatternRefByStopPointLabelsException -> CannotFindJourneyPatternRefByStopPointLabelsError
            is CannotFindJourneyPatternRefByTimingPlaceLabelsException -> CannotFindJourneyPatternRefByTimingPlaceLabelsError
            is CannotFindJourneyPatternRefByRouteLabelAndDirectionException -> CannotFindJourneyPatternRefByRouteLabelAndDirectionError
            is ErrorWhileProcessingHastusDataException -> ErrorWhileProcessingHastusDataError
            is GraphQLAuthenticationFailedException -> GraphQLAuthenticationFailedError
            is IllegalArgumentException -> IllegalArgumentError
            else -> UnknownError
        }
    }
}
