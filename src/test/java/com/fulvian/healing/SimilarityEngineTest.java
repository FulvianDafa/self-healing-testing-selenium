package com.fulvian.healing;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.Point;

import static org.junit.jupiter.api.Assertions.*;

class SimilarityEngineTest {

    @Test
    void normalizesCamelCaseAndSpecialCharacters() {
        assertEquals("product search input refactor",
                SimilarityEngine.normalizeText("productSearchInputRefactor"));
        assertEquals("save button", SimilarityEngine.normalizeText("Save-Button"));
        assertEquals("", SimilarityEngine.normalizeText(null));
    }

    @Test
    @SuppressWarnings("deprecation")
    void recoversRelatedRenamedLocators() {
        assertTrue(SimilarityEngine.calculateSimilarity(
                "searchInput", "productSearchInputRefactor") >= 0.50);
        assertEquals(1.0,
                SimilarityEngine.calculateSimilarity("saveBtn", "saveBtn"), 1e-9);
        assertEquals(0.0,
                SimilarityEngine.calculateSimilarity("saveBtn", ""), 1e-9);
    }

    @Test
    void favorsNearerScreenPositions() {
        Point original = new Point(100, 100);
        double same = SimilarityEngine.calculatePositionSimilarity(original, original);
        double near = SimilarityEngine.calculatePositionSimilarity(original, new Point(120, 110));
        double far = SimilarityEngine.calculatePositionSimilarity(original, new Point(400, 400));
        assertEquals(1.0, same, 1e-9);
        assertTrue(same > near);
        assertTrue(near > far);
    }

    @Test
    void handlesMissingPositionWithoutInventingSimilarity() {
        assertEquals(0.0, SimilarityEngine.calculatePositionSimilarity(null, new Point(10, 10)), 1e-9);
        assertEquals(0.0, SimilarityEngine.calculatePositionSimilarity(new Point(10, 10), null), 1e-9);
        SimilarityEngine.ScoreResult score =
                SimilarityEngine.calculateCombinedScoreWithDetail(new ElementProfile("Save", "saveBtn", "id"), null);
        assertEquals(0.0, score.combinedScore, 1e-9);
    }
}
