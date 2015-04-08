/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.exomiser.core.dao.TestVariantFactory;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.io.JannovarData;
import de.charite.compbio.jannovar.pedigree.Genotype;
import de.charite.compbio.jannovar.reference.HG19RefDictBuilder;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import htsjdk.variant.variantcontext.VariantContext;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import de.charite.compbio.jannovar.annotation.AnnotationList;
import static org.hamcrest.CoreMatchers.is;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantAnnotatorTest {

    private VariantAnnotator instance;
    private TestVariantFactory varFactory;
    private TranscriptModel tmFGFR2;
    private TranscriptModel tmSHH;

    @Before
    public void setUp() throws Exception {
        varFactory = new TestVariantFactory();
        tmFGFR2 = varFactory.buildTMForFGFR2();
        tmSHH = varFactory.buildTMForSHH();
        JannovarData jannovarData = new JannovarData(HG19RefDictBuilder.build(), ImmutableList.<TranscriptModel> of(
                tmFGFR2, tmSHH));
        instance = new VariantAnnotator(jannovarData);
    }

    @Test(expected = NullPointerException.class)
    public void testAnnotationOfNullThrowsNullPointer() {
        instance.annotateVariantContext(null);
    }

    @Test
    public void testIntergenicAnnotationNoNeighbour() {
        // intergenic variants should have empty annotation lists if there is no neighbouring gene
        VariantContext vc = varFactory.constructVariantContext(2, 2, "A", "T", Genotype.HETEROZYGOUS, 30);

        List<Variant> vars = instance.annotateVariantContext(vc);
//        Variant variant = vars.get(0);
//        AnnotationList annotationList = variant.getAnnotationList();
        assertThat(vars.isEmpty(), is(true));
//        assertThat(annotationList.size(), equalTo(0));
    }

    @Test
    public void testIntergenicAnnotationWithNeighbour() {
        // intergenic variants should have an entry for neighbouring genes
        VariantContext vc = varFactory.constructVariantContext(10, 2, "A", "T", Genotype.HETEROZYGOUS, 30);

        List<Variant> vars = instance.annotateVariantContext(vc);
        Variant variant = vars.get(0);
        AnnotationList annotationList = variant.getAnnotationList();
        
        assertThat(vars.size(), equalTo(1));
        assertThat(annotationList.size(), equalTo(1));
        assertThat(annotationList.get(0).effects,
                equalTo(ImmutableSortedSet.of(VariantEffect.INTERGENIC_VARIANT)));
        assertThat(annotationList.get(0).transcript, equalTo(tmFGFR2));
    }

    @Test
    public void testExonicAnnotationInFGFR2Gene() {
        // a variant in the FGFR2 gene should be annotated correctly
        VariantContext vc = varFactory.constructVariantContext(10, 123353320, "A", "T", Genotype.HETEROZYGOUS, 30);

        List<Variant> vars = instance.annotateVariantContext(vc);
        Variant variant = vars.get(0);
        AnnotationList annotationList = variant.getAnnotationList();
        
        assertThat(vars.size(), equalTo(1));
        assertThat(annotationList.size(), equalTo(1));
        assertThat(annotationList.get(0).effects,
                equalTo(ImmutableSortedSet.of(VariantEffect.STOP_GAINED)));
        assertThat(annotationList.get(0).transcript, equalTo(tmFGFR2));
    }

    @Test
    public void testExonicAnnotationInSHHGene() {
        // a variant in the SHH gene should be annotated correctly
        VariantContext vc = varFactory.constructVariantContext(7, 155604810, "A", "T", Genotype.HETEROZYGOUS, 30);

        List<Variant> vars = instance.annotateVariantContext(vc);
        Variant variant = vars.get(0);
        AnnotationList annotationList = variant.getAnnotationList();
        
        assertThat(vars.size(), equalTo(1));
        assertThat(annotationList.size(), equalTo(1));
        assertThat(annotationList.get(0).effects,
                equalTo(ImmutableSortedSet.of(VariantEffect.SYNONYMOUS_VARIANT)));
        assertThat(annotationList.get(0).transcript, equalTo(tmSHH));
    }

}
