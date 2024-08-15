package fi.hsl.jore4.hastus.service.exporting.validation

import fi.hsl.jore4.hastus.data.jore.JoreLine

interface IExportLineValidator {
    /**
     * Validates line before it is exported.
     *
     * @param [line] The line to be validated
     *
     * @throws RuntimeException if there is any validation error present
     */
    fun validateLine(line: JoreLine)
}
