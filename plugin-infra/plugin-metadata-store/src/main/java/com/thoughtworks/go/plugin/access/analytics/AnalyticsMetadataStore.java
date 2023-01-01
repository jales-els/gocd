/*
 * Copyright 2023 Thoughtworks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.thoughtworks.go.plugin.access.analytics;

import com.thoughtworks.go.plugin.access.common.MetadataStore;
import com.thoughtworks.go.plugin.domain.analytics.AnalyticsPluginInfo;

public class AnalyticsMetadataStore extends MetadataStore<AnalyticsPluginInfo> {
    private static final AnalyticsMetadataStore store = new AnalyticsMetadataStore();

    protected AnalyticsMetadataStore() {
    }

    public static AnalyticsMetadataStore instance() {
        return store;
    }

    public void updateAssetsPath(String pluginId, String assetPath) {
        AnalyticsPluginInfo pluginInfo = getPluginInfo(pluginId);
        
        pluginInfo.setStaticAssetsPath(assetPath);
    }
}
