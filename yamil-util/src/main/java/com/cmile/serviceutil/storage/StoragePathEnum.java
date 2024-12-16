/*
 * Copyright 2024 cmile inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cmile.serviceutil.storage;

public enum StoragePathEnum {
    CONTAINER_UPLOAD("container/upload"),
    CONTAINER_INPROGRESS("container/inprogress"),
    CONTAINER_COMPLETED("container/completed"),
    CONTAINER_FAILED("container/failed"),
    RATES_UPLOAD("rates/upload"),
    RATES_INPROGRESS("rates/inprogress"),
    RATES_COMPLETED("rates/completed"),
    RATES_FAILED("rates/failed");

    private final String path;

    StoragePathEnum(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
