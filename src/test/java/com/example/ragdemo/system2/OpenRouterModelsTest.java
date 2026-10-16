package com.example.ragdemo.system2;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenRouterModelsTest {

    static final String JSON = """
            {"data": [
              {"id": "zeta/paid-model", "name": "Zeta: Paid", "context_length": 131072,
               "pricing": {"prompt": "0.0000003", "completion": "0.0000025"}, "supported_parameters": ["tools"]},
              {"id": "nvidia/nemotron-3-super-120b-a12b:free", "name": "NVIDIA: Nemotron 3 Super (free)",
               "context_length": 262144, "pricing": {"prompt": "0", "completion": "0"},
               "supported_parameters": ["tools", "max_tokens"]},
              {"id": "alpha/zero-priced", "name": "Alpha: Zero", "context_length": 1000000,
               "pricing": {"prompt": "0", "completion": "0"}, "architecture": {"output_modalities": ["text"]}},
              {"id": "beta/music-model", "name": "Beta: Music", "pricing": {"prompt": "0", "completion": "0"},
               "architecture": {"output_modalities": ["audio"]}}
            ]}""";

    @Test
    void freeModelsComeFirstThenPaid_eachGroupByName() {
        List<OpenRouterModels.ModelInfo> models = OpenRouterModels.parse(JSON);
        assertThat(models).extracting(OpenRouterModels.ModelInfo::id)
                .containsExactly("alpha/zero-priced", "nvidia/nemotron-3-super-120b-a12b:free", "zeta/paid-model");
        assertThat(models).extracting(OpenRouterModels.ModelInfo::free).containsExactly(true, true, false);
    }

    @Test
    void detailsLineForTheDropdownFooter() {
        List<OpenRouterModels.ModelInfo> models = OpenRouterModels.parse(JSON);
        assertThat(models.get(1).details()).isEqualTo("🆓 Free · 256K context · 🛠 tools");
        assertThat(models.get(0).details()).isEqualTo("🆓 Free · 1M context");
        assertThat(models.get(2).details()).isEqualTo("💲 $0.30 in / $2.50 out per 1M tokens · 128K context · 🛠 tools");
    }
}
