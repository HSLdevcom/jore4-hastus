package fi.hsl.jore4.hastus.api.util

import fi.hsl.jore4.hastus.graphql.converter.GraphQLAuthenticationFailedException
import fi.hsl.jore4.hastus.service.exporting.validation.FirstStopNotTimingPointException
import fi.hsl.jore4.hastus.service.exporting.validation.LastStopNotTimingPointException
import fi.hsl.jore4.hastus.service.exporting.validation.TooFewStopPointsException
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
    FirstStopNotTimingPointError,
    LastStopNotTimingPointError,
    GraphQLAuthenticationFailedError,
    IllegalArgumentError,
    InvalidHastusDataError,
    TooFewStopPointsError,
    UnknownError;

    companion object {
        fun from(exception: Exception): HastusApiErrorType = when (exception) {
            is InvalidHastusDataException -> InvalidHastusDataError
            is CannotFindJourneyPatternRefByStopPointLabelsException -> CannotFindJourneyPatternRefByStopPointLabelsError
            is CannotFindJourneyPatternRefByTimingPlaceLabelsException -> CannotFindJourneyPatternRefByTimingPlaceLabelsError
            is CannotFindJourneyPatternRefByRouteLabelAndDirectionException -> CannotFindJourneyPatternRefByRouteLabelAndDirectionError
            is ErrorWhileProcessingHastusDataException -> ErrorWhileProcessingHastusDataError
            is FirstStopNotTimingPointException -> FirstStopNotTimingPointError
            is LastStopNotTimingPointException -> LastStopNotTimingPointError
            is GraphQLAuthenticationFailedException -> GraphQLAuthenticationFailedError
            is IllegalArgumentException -> IllegalArgumentError
            is TooFewStopPointsException -> TooFewStopPointsError
            else -> UnknownError
        }
    }
}
