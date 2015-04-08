/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import static de.charite.compbio.exomiser.core.ExomiserSettings.REMOVE_KNOWN_VARIANTS_OPTION;
import org.apache.commons.cli.OptionBuilder;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FrequencyKnownVariantOptionMarshaller extends AbstractOptionMarshaller {

    public FrequencyKnownVariantOptionMarshaller() {
        option = OptionBuilder
                .hasOptionalArg()
                .withType(Boolean.class)
                .withArgName("true/false")
                .withDescription("Filter out all variants with an entry in dbSNP/ESP (regardless of frequency).")
                .withLongOpt(REMOVE_KNOWN_VARIANTS_OPTION) 
                .create();
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, ExomiserSettings.SettingsBuilder settingsBuilder) {
        if (values == null) {
            //default is not to remove variants with a frequency from a study
            //having this triggered from the command line is the same as saying values[0] == true
            settingsBuilder.removeKnownVariants(true);
        } else {
            //but the json/properties file specify true or false
            settingsBuilder.removeKnownVariants(Boolean.parseBoolean(values[0]));
        }
    }

}
