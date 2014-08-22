/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.exomiser.core.pathogenicity.CaddScore;
import de.charite.compbio.exomiser.core.pathogenicity.MutationTasterScore;
import de.charite.compbio.exomiser.core.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.pathogenicity.PolyPhenScore;
import de.charite.compbio.exomiser.core.pathogenicity.SiftScore;
import jannovar.common.Constants;
import jannovar.common.VariantType;
import jannovar.exome.Variant;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author jj8
 */
@Repository
public class PathogenicityDao {
    
    private final Logger logger = LoggerFactory.getLogger(PathogenicityDao.class);

    @Autowired
    private DataSource dataSource;

    public PathogenicityDao() {
    }

    public PathogenicityData getPathogenicityData(Variant variant) {

        //if a variant is not classified as missense then we don't need to hit 
        //the database as we're going to assign it a constant pathogenicity score.
        VariantType variantType = variant.getVariantTypeConstant();
        if (variantType != VariantType.MISSENSE) {
            return new PathogenicityData(null, null, null, null);
        }

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = createPreparedStatement(connection, variant);
                ResultSet rs = preparedStatement.executeQuery()) {

            return processResults(rs, variant);

        } catch (SQLException e) {
            logger.error("Error executing pathogenicity query: ", e);
        }
        return null;
    }

    private PreparedStatement createPreparedStatement(Connection connection, Variant variant) throws SQLException {
        String query = String.format("SELECT sift,"
                + "polyphen, mut_taster, cadd, phyloP "
                + "FROM variant "
                + "WHERE chromosome = ? "
                + "AND position = ? "
                + "AND ref = ? "
                + "AND alt = ? ");

        PreparedStatement ps = connection.prepareStatement(query);
        int chrom = variant.get_chromosome();
        int position = variant.get_position();
        // Note: when we get here, we have tested above that we have a nonsynonymous substitution
        char ref = variant.get_ref().charAt(0);
        char alt = variant.get_alt().charAt(0);

        ps.setInt(1, chrom);
        ps.setInt(2, position);
        ps.setString(3, Character.toString(ref));
        ps.setString(4, Character.toString(alt));

        return ps;
    }

    PathogenicityData processResults(ResultSet rs, Variant variant) throws SQLException {

        float polyphen = Float.NaN;
        float mutationTaster = Float.NaN;
        float sift = Float.NaN;
        float cadd = Float.NaN;

        PolyPhenScore polyPhenScore = null;
        MutationTasterScore mutationTasterScore = null;
        SiftScore siftScore = null;
        CaddScore caddScore = null;
        /* 
         * Switched db back to potentially having multiple rows per variant
         * if alt transcripts leads to diff aa changes and pathogenicities.
         * In future if know which transcript is more likely in the disease
         * tissue can use the most appropriate row but for now take max
         */
        //yukkity yuk-yuk
        while (rs.next()) {
            float rowSift = rs.getFloat(1);
            //TODO - remove the Constants once the database build has been fixed
            if (!rs.wasNull() && rowSift != Constants.UNINITIALIZED_FLOAT && rowSift != Constants.NOPARSE_FLOAT) {
                if (Float.isNaN(sift) || rowSift < sift) {
                    sift = rowSift;
                }
            }
            float rowPoly = rs.getFloat(2);
            if (!rs.wasNull() && rowPoly != Constants.UNINITIALIZED_FLOAT && rowPoly != Constants.NOPARSE_FLOAT) {
                if (Float.isNaN(polyphen) || rowPoly > polyphen) {
                    polyphen = rowPoly;
                }
            }
            float rowMut = rs.getFloat(3);
            if (!rs.wasNull() && rowMut != Constants.UNINITIALIZED_FLOAT && rowMut != Constants.NOPARSE_FLOAT) {
                if (Float.isNaN(mutationTaster) || rowMut > mutationTaster) {
                    mutationTaster = rowMut;
                }
            }
            float rowCadd = rs.getFloat(4);
            if (!rs.wasNull() && rowCadd != Constants.UNINITIALIZED_FLOAT && rowCadd != Constants.NOPARSE_FLOAT) {
                if (Float.isNaN(cadd) || rowCadd > cadd) {
                    cadd = rowCadd;
                }
            }
        }
        //yukkity yuk-yuk-yuk
        if (!Float.isNaN(sift)) {
            siftScore = new SiftScore(sift);
        }
        if (!Float.isNaN(polyphen)) {
            polyPhenScore = new PolyPhenScore(polyphen);
        }
        if (!Float.isNaN(mutationTaster)) {
            mutationTasterScore = new MutationTasterScore(mutationTaster);
        }
        if (!Float.isNaN(cadd)) {
            caddScore = new CaddScore(cadd);
        }
        
        return new PathogenicityData(polyPhenScore, mutationTasterScore, siftScore, caddScore);

    }

}