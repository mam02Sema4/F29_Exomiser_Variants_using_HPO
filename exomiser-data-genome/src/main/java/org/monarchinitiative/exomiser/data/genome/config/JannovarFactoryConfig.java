/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.data.genome.config;

import org.monarchinitiative.exomiser.core.genome.jannovar.JannovarDataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
public class JannovarFactoryConfig {

    private final Environment environment;

    @Autowired
    public JannovarFactoryConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    JannovarDataFactory jannovarDataFactory() {
        Path jannovarIniFilePath = getPathForProperty("jannovar.ini-file");
        return JannovarDataFactory.builder(jannovarIniFilePath).build();
    }

    private Path getPathForProperty(String propertyKey) {
        String value = environment.getProperty(propertyKey, "");

        if (value.isEmpty()) {
            throw new IllegalArgumentException(propertyKey + " has not been specified!");
        }
        return Paths.get(value);
    }
}
