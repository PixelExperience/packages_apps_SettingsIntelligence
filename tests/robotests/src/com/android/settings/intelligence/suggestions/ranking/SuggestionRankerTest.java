package com.android.settings.intelligence.suggestions.ranking;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.service.settings.suggestions.Suggestion;

import com.android.settings.intelligence.SettingsIntelligenceRobolectricTestRunner;
import com.android.settings.intelligence.TestConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SettingsIntelligenceRobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION)
public class SuggestionRankerTest {

    @Mock
    private SuggestionRanker mSuggestionRanker;
    @Mock
    private SuggestionFeaturizer mSuggestionFeaturizer;

    private Map<String, Map<String, Double>> mFeatures;
    private List<Suggestion> mSuggestions;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mFeatures = new HashMap<>();
        mFeatures.put("pkg1", new HashMap<>());
        mFeatures.put("pkg2", new HashMap<>());
        mFeatures.put("pkg3", new HashMap<>());
        mSuggestions = new ArrayList<Suggestion>() {
            {
                add(new Suggestion.Builder("pkg1").build());
                add(new Suggestion.Builder("pkg2").build());
                add(new Suggestion.Builder("pkg3").build());
            }
        };
        mSuggestionFeaturizer = mock(SuggestionFeaturizer.class);
        mSuggestionRanker = new SuggestionRanker(mSuggestionFeaturizer);
        when(mSuggestionFeaturizer.featurize(mSuggestions)).thenReturn(mFeatures);
        mSuggestionRanker = spy(mSuggestionRanker);
        when(mSuggestionRanker.getRelevanceMetric(same(mFeatures.get("pkg1")))).thenReturn(0.9);
        when(mSuggestionRanker.getRelevanceMetric(same(mFeatures.get("pkg2")))).thenReturn(0.1);
        when(mSuggestionRanker.getRelevanceMetric(same(mFeatures.get("pkg3")))).thenReturn(0.5);
    }

    @Test
    public void testRank() {
        List<Suggestion> expectedOrderdList = new ArrayList<Suggestion>() {
            {
                add(mSuggestions.get(0)); // relevance = 0.9
                add(mSuggestions.get(2)); // relevance = 0.5
                add(mSuggestions.get(1)); // relevance = 0.1
            }
        };
        mSuggestionRanker.rankSuggestions(mSuggestions);
        assertThat(mSuggestions).isEqualTo(expectedOrderdList);
    }
}