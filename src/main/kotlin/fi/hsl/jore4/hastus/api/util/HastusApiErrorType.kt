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
    GraphQLAuthenticationFailedError,
    IllegalArgumentError,
    InvalidHastusDataError,
    LastStopNotTimingPointError,
    TooFewStopPointsError,
    UnknownError;

    companion object {
        fun from(exception: Exception): HastusApiErrorType =
            when (exception) {
                is CannotFindJourneyPatternRefByRouteLabelAndDirectionException -> CannotFindJourneyPatternRefByRouteLabelAndDirectionError
                is CannotFindJourneyPatternRefByStopPointLabelsException -> CannotFindJourneyPatternRefByStopPointLabelsError
                is CannotFindJourneyPatternRefByTimingPlaceLabelsException -> CannotFindJourneyPatternRefByTimingPlaceLabelsError
                is ErrorWhileProcessingHastusDataException -> ErrorWhileProcessingHastusDataError
                is FirstStopNotTimingPointException -> FirstStopNotTimingPointError
                is GraphQLAuthenticationFailedException -> GraphQLAuthenticationFailedError
                is IllegalArgumentException -> IllegalArgumentError
                is InvalidHastusDataException -> InvalidHastusDataError
                is LastStopNotTimingPointException -> LastStopNotTimingPointError
                is TooFewStopPointsException -> TooFewStopPointsError
                else -> UnknownError
            }
    }
}
