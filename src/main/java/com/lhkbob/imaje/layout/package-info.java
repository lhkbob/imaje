/**
 * TODO put nomenclature definitions for channel, band, field, etc. here
 * Channel = a logical value that corresponds to some aspect of color, this is a color channel like red, green, blue
 *    or alpha. It can also be a skip channel, meaning it's ignored.
 * Field = a numeric value that can be a channel, however, it may also be skipped. An arrangement of
 *    fields may produce different active channels depending on which are skipped.
 * Band = a primitive in the data buffer based on the data layout; some arrays map a field to a band
 *    in a one to one manner, some map all fields to the same band.
 */
package com.lhkbob.imaje.layout;